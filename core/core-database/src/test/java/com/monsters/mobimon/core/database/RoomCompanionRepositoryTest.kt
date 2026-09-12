package com.monsters.mobimon.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.monsters.mobimon.core.domain.Clock
import com.monsters.mobimon.core.domain.CurrentVehicleEvidence
import com.monsters.mobimon.core.domain.DrivingState
import com.monsters.mobimon.core.domain.IdGenerator
import com.monsters.mobimon.core.domain.PetAppearance
import com.monsters.mobimon.core.domain.ProgressionIdentity
import com.monsters.mobimon.core.domain.QuestCommandResult
import com.monsters.mobimon.core.domain.QuestEvaluator
import com.monsters.mobimon.core.domain.QuestRejection
import com.monsters.mobimon.core.domain.QuestStatus
import com.monsters.mobimon.core.domain.QuestType
import com.monsters.mobimon.core.domain.RewardResult
import com.monsters.mobimon.core.domain.SignalQuality
import com.monsters.mobimon.core.domain.SignalSource
import com.monsters.mobimon.core.domain.VehicleSnapshot
import com.monsters.mobimon.core.domain.WriteResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.SQLiteMode
import java.util.concurrent.atomic.AtomicInteger

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@SQLiteMode(SQLiteMode.Mode.NATIVE)
class RoomCompanionRepositoryTest {
    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var clock: MutableClock
    private lateinit var repository: RoomCompanionRepository
    private lateinit var observed: VehicleSnapshot
    private val currentVehicle = CurrentVehicleEvidence { observed }

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        database =
            Room
                .inMemoryDatabaseBuilder(context, AppDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        clock = MutableClock(10_000L)
        observed = rawSnapshot(sequence = 10)
        repository = repository(database, REAL_PROFILE, SignalSource.REAL, clock, currentVehicle)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `profile initialization is idempotent and does not reset saved values`() =
        runBlocking {
            repository.initialize()
            assertEquals(WriteResult.Success, repository.setAppearance(PetAppearance.CREAM))

            repository.initialize()

            assertEquals(PetAppearance.CREAM, repository.profile.first().appearance)
            assertEquals(0, repository.profile.first().totalXp)
        }

    @Test
    fun `completion schema rejects duplicate run and duplicate profile quest type`() =
        runBlocking {
            val dao = database.companionDao()
            dao.insertProfile(profileEntity("profile-a"))
            dao.insertProfile(profileEntity("profile-b"))
            dao.insertRun(runEntity("run-a", "profile-a", QuestType.Q01))
            dao.insertRun(runEntity("run-b", "profile-a", QuestType.Q01))
            dao.insertRun(runEntity("run-c", "profile-b", QuestType.Q01))

            assertTrue(dao.insertCompletion(completionEntity("completion-a", "run-a", "profile-a", QuestType.Q01)) > 0)
            assertEquals(
                -1L,
                dao.insertCompletion(completionEntity("completion-b", "run-b", "profile-a", QuestType.Q01)),
            )
            assertEquals(
                -1L,
                dao.insertCompletion(completionEntity("completion-c", "run-a", "profile-b", QuestType.Q01)),
            )
        }

    @Test
    fun `concurrent starts create only one active run for the profile`() =
        runBlocking {
            repository.initialize()
            val results =
                listOf(repository, repository(database, REAL_PROFILE, SignalSource.REAL, clock, currentVehicle))
                    .map { candidate ->
                        async(Dispatchers.Default) {
                            candidate.start(QuestType.Q01, snapshot(sequence = 10))
                        }
                    }.awaitAll()

            assertEquals(1, results.count { it is QuestCommandResult.Started })
            assertEquals(1, results.count { it == QuestCommandResult.AlreadyActive })
            assertNotNull(repository.progress.first().activeRun)
        }

    @Test
    fun `duplicate concurrent completion grants exactly one reward`() =
        runBlocking {
            val run = startRun(sequence = 10)
            clock.value = 11_000L
            val evidence = snapshot(sequence = 11, receivedAtMillis = 11_000L)

            val results =
                listOf(repository, repository(database, REAL_PROFILE, SignalSource.REAL, clock, currentVehicle))
                    .map { candidate ->
                        async(Dispatchers.Default) {
                            candidate.complete(run.id, run.revision, evidence)
                        }
                    }.awaitAll()

            assertEquals(1, results.count { it is RewardResult.Applied })
            assertEquals(1, results.count { it == RewardResult.AlreadyAwarded })
            assertEquals(80, repository.profile.first().totalXp)
            assertEquals(
                1,
                repository.progress
                    .first()
                    .completions.size,
            )
            assertNull(repository.progress.first().activeRun)
        }

    @Test
    fun `cancel rejects stale revision and run owned by another profile`() =
        runBlocking {
            val run = startRun(sequence = 10)
            val other = repository(database, "profile-other", SignalSource.REAL, clock, currentVehicle)
            other.initialize()

            assertEquals(
                QuestCommandResult.Rejected(QuestRejection.RUN_CHANGED),
                repository.cancel(run.id, run.revision + 1, snapshot(sequence = 10)),
            )
            assertEquals(
                QuestCommandResult.Rejected(QuestRejection.RUN_CHANGED),
                other.cancel(run.id, run.revision, snapshot(sequence = 10)),
            )
            assertEquals(
                run.id,
                repository.progress
                    .first()
                    .activeRun
                    ?.id,
            )
        }

    @Test
    fun `cancel accepts a fresh parked snapshot after the observation epoch changes`() =
        runBlocking {
            val run = startRun(sequence = 10)
            clock.value = 11_000L
            val latest = snapshot(sequence = 0, receivedAtMillis = 11_000L).copy(epoch = "epoch-after-restart")
            observed = latest

            val result =
                repository.cancel(
                    run.id,
                    run.revision,
                    latest,
                )

            assertEquals(QuestCommandResult.Cancelled, result)
            assertNull(repository.progress.first().activeRun)
        }

    @Test
    fun `cancel racing completion leaves one consistent terminal result`() =
        runBlocking {
            val run = startRun(sequence = 10)
            clock.value = 11_000L
            val evidence = snapshot(sequence = 11, receivedAtMillis = 11_000L)

            val cancel = async(Dispatchers.Default) { repository.cancel(run.id, run.revision, evidence) }
            val complete =
                async(Dispatchers.Default) {
                    repository(database, REAL_PROFILE, SignalSource.REAL, clock, currentVehicle)
                        .complete(run.id, run.revision, evidence)
                }
            val cancelResult = cancel.await()
            val completionResult = complete.await()

            val completionApplied = completionResult is RewardResult.Applied
            assertEquals(completionApplied, repository.profile.first().totalXp == 80)
            assertEquals(
                completionApplied,
                repository.progress
                    .first()
                    .completions.size == 1,
            )
            assertNull(repository.progress.first().activeRun)
            assertTrue(
                (
                    cancelResult == QuestCommandResult.Cancelled &&
                        completionResult == RewardResult.Rejected(QuestRejection.RUN_CHANGED)
                ) ||
                    (cancelResult == QuestCommandResult.Rejected(QuestRejection.RUN_CHANGED) && completionApplied),
            )
        }

    @Test
    fun `real repository rejects simulated start and completion evidence`() =
        runBlocking {
            repository.initialize()
            assertEquals(
                QuestCommandResult.Rejected(QuestRejection.WRONG_SOURCE),
                repository.start(QuestType.Q01, snapshot(sequence = 10, source = SignalSource.SIMULATED)),
            )
            val run = startRun(sequence = 10)
            clock.value = 11_000L

            assertEquals(
                RewardResult.Rejected(QuestRejection.WRONG_SOURCE),
                repository.complete(
                    run.id,
                    run.revision,
                    snapshot(sequence = 11, receivedAtMillis = 11_000L, source = SignalSource.SIMULATED),
                ),
            )
            assertEquals(0, repository.profile.first().totalXp)
            assertTrue(
                repository.progress
                    .first()
                    .completions
                    .isEmpty(),
            )
        }

    @Test
    fun `sqlite failure during xp update rolls back completion and run status`() =
        runBlocking {
            val run = startRun(sequence = 10)
            database.openHelper.writableDatabase.execSQL(
                """
                CREATE TRIGGER fail_xp_update
                BEFORE UPDATE OF totalXp ON pet_profiles
                WHEN NEW.totalXp > OLD.totalXp
                BEGIN
                    SELECT RAISE(ABORT, 'forced xp failure');
                END
                """.trimIndent(),
            )
            clock.value = 11_000L

            val result =
                repository.complete(
                    run.id,
                    run.revision,
                    snapshot(sequence = 11, receivedAtMillis = 11_000L),
                )

            assertEquals(RewardResult.StorageFailure, result)
            assertEquals(0, repository.profile.first().totalXp)
            assertTrue(
                repository.progress
                    .first()
                    .completions
                    .isEmpty(),
            )
            assertEquals(run, repository.progress.first().activeRun)
            database.openHelper.writableDatabase.execSQL("DROP TRIGGER fail_xp_update")
        }

    @Test
    fun `progress observation never publishes a partial reward transaction`() =
        runBlocking {
            val run = startRun(sequence = 10)
            val emissions = async { repository.progress.take(2).toList() }
            yield()
            clock.value = 11_000L

            assertTrue(
                repository.complete(
                    run.id,
                    run.revision,
                    snapshot(sequence = 11, receivedAtMillis = 11_000L),
                ) is RewardResult.Applied,
            )

            val committed = emissions.await().last()
            assertNull(committed.activeRun)
            assertEquals(1, committed.completions.size)
        }

    @Test
    fun `file database reopen restores appearance xp and completion`() =
        runBlocking {
            database.close()
            val databaseName = "companion-reopen-${System.nanoTime()}.db"
            context.deleteDatabase(databaseName)
            try {
                var fileDatabase = openFileDatabase(databaseName)
                var fileClock = MutableClock(10_000L)
                var fileRepository =
                    repository(fileDatabase, REAL_PROFILE, SignalSource.REAL, fileClock, currentVehicle)
                fileRepository.initialize()
                assertEquals(WriteResult.Success, fileRepository.setAppearance(PetAppearance.CREAM))
                val started = fileRepository.start(QuestType.Q01, snapshot(sequence = 10))
                assertTrue(started is QuestCommandResult.Started)
                val run = (started as QuestCommandResult.Started).run
                fileClock.value = 11_000L
                assertTrue(
                    fileRepository.complete(
                        run.id,
                        run.revision,
                        snapshot(sequence = 11, receivedAtMillis = 11_000L),
                    ) is RewardResult.Applied,
                )
                fileDatabase.close()

                fileDatabase = openFileDatabase(databaseName)
                fileClock = MutableClock(12_000L)
                fileRepository = repository(fileDatabase, REAL_PROFILE, SignalSource.REAL, fileClock, currentVehicle)

                assertEquals(PetAppearance.CREAM, fileRepository.profile.first().appearance)
                assertEquals(80, fileRepository.profile.first().totalXp)
                assertFalse(
                    fileRepository.progress
                        .first()
                        .completions
                        .isEmpty(),
                )
                assertNull(fileRepository.progress.first().activeRun)
                fileDatabase.close()
            } finally {
                context.deleteDatabase(databaseName)
                database =
                    Room
                        .inMemoryDatabaseBuilder(context, AppDatabase::class.java)
                        .allowMainThreadQueries()
                        .build()
            }
        }

    private suspend fun startRun(sequence: Long) =
        repository.run {
            initialize()
            val result = start(QuestType.Q01, snapshot(sequence = sequence))
            assertTrue(result is QuestCommandResult.Started)
            (result as QuestCommandResult.Started).run
        }

    private fun openFileDatabase(name: String): AppDatabase =
        Room
            .databaseBuilder(context, AppDatabase::class.java, name)
            .allowMainThreadQueries()
            .build()

    private fun snapshot(
        sequence: Long,
        receivedAtMillis: Long = 10_000L,
        source: SignalSource = SignalSource.REAL,
    ): VehicleSnapshot = rawSnapshot(sequence, receivedAtMillis, source).also { observed = it }
}

private const val REAL_PROFILE = "profile-real"

private class MutableClock(
    var value: Long,
) : Clock {
    override fun nowMillis(): Long = value
}

private class SequenceIds : IdGenerator {
    private val value = AtomicInteger()

    override fun nextId(): String = "generated-${value.incrementAndGet()}"
}

private fun repository(
    database: AppDatabase,
    profileId: String,
    source: SignalSource,
    clock: Clock,
    currentVehicle: CurrentVehicleEvidence,
) = RoomCompanionRepository(
    database = database,
    identity = ProgressionIdentity(profileId, source),
    clock = clock,
    ids = SequenceIds(),
    evaluator = QuestEvaluator(maxAgeMillis = 15_000L),
    currentVehicle = currentVehicle,
    currentAppUse =
        com.monsters.mobimon.core.domain
            .CurrentAppUse { com.monsters.mobimon.core.domain.AppUseState.ALLOWED },
)

private fun rawSnapshot(
    sequence: Long,
    receivedAtMillis: Long = 10_000L,
    source: SignalSource = SignalSource.REAL,
) = VehicleSnapshot(
    id = "snapshot-$sequence-${source.name}",
    epoch = "epoch-1",
    sequence = sequence,
    receivedAtMillis = receivedAtMillis,
    source = source,
    drivingState = DrivingState.PARKED,
    quality = SignalQuality.VALID,
    batteryPercent = 60,
)

private fun profileEntity(id: String) = PetProfileEntity(id = id, appearance = PetAppearance.GOLDEN.name, totalXp = 0)

private fun runEntity(
    id: String,
    profileId: String,
    type: QuestType,
) = QuestRunEntity(
    id = id,
    profileId = profileId,
    type = type.name,
    status = QuestStatus.ACTIVE.name,
    revision = 0,
    ruleVersion = 1,
    rewardXp = 80,
    startEpoch = "epoch-1",
    startSequence = 1,
    startedAtMillis = 10_000L,
    source = SignalSource.REAL.name,
)

private fun completionEntity(
    id: String,
    runId: String,
    profileId: String,
    type: QuestType,
) = QuestCompletionEntity(
    id = id,
    runId = runId,
    profileId = profileId,
    type = type.name,
    awardedXp = 80,
    completedAtMillis = 11_000L,
    snapshotId = "snapshot-2",
    snapshotEpoch = "epoch-1",
    snapshotSequence = 2,
    snapshotSource = SignalSource.REAL.name,
)
