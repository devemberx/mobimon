package com.devemberx.rivo.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.devemberx.rivo.core.domain.Clock
import com.devemberx.rivo.core.domain.DrivingState
import com.devemberx.rivo.core.domain.IdGenerator
import com.devemberx.rivo.core.domain.PetAppearance
import com.devemberx.rivo.core.domain.ProgressionIdentity
import com.devemberx.rivo.core.domain.QuestCommandResult
import com.devemberx.rivo.core.domain.QuestEvaluator
import com.devemberx.rivo.core.domain.QuestStatus
import com.devemberx.rivo.core.domain.QuestType
import com.devemberx.rivo.core.domain.RewardResult
import com.devemberx.rivo.core.domain.SignalQuality
import com.devemberx.rivo.core.domain.SignalSource
import com.devemberx.rivo.core.domain.VehicleSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicInteger

@RunWith(AndroidJUnit4::class)
class RoomCompanionRepositoryAndroidTest {
    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var clock: AndroidMutableClock
    private lateinit var repository: RoomCompanionRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        database =
            Room
                .inMemoryDatabaseBuilder(context, AppDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        clock = AndroidMutableClock(10_000L)
        repository = androidRepository(database, clock)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun uniqueSqlConstraintsAndConcurrentRewardAllowOneCompletion() =
        runBlocking {
            repository.initialize()
            val started = repository.start(QuestType.Q01, androidSnapshot(10, 10_000L))
            assertTrue(started is QuestCommandResult.Started)
            val run = (started as QuestCommandResult.Started).run
            clock.value = 11_000L

            val results =
                listOf(repository, androidRepository(database, clock))
                    .map { candidate ->
                        async(Dispatchers.Default) {
                            candidate.complete(run.id, run.revision, androidSnapshot(11, 11_000L))
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
        }

    @Test
    fun sqliteAbortRollsBackEveryRewardWrite() =
        runBlocking {
            repository.initialize()
            val started = repository.start(QuestType.Q01, androidSnapshot(10, 10_000L))
            val run = (started as QuestCommandResult.Started).run
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

            assertEquals(
                RewardResult.StorageFailure,
                repository.complete(run.id, run.revision, androidSnapshot(11, 11_000L)),
            )
            assertEquals(0, repository.profile.first().totalXp)
            assertTrue(
                repository.progress
                    .first()
                    .completions
                    .isEmpty(),
            )
            assertEquals(
                QuestStatus.ACTIVE,
                repository.progress
                    .first()
                    .activeRun
                    ?.status,
            )
        }

    @Test
    fun fileDatabaseReopenRestoresCommittedProfileAndReward() =
        runBlocking {
            database.close()
            val databaseName = "android-reopen-${System.nanoTime()}.db"
            context.deleteDatabase(databaseName)
            try {
                var fileDatabase =
                    Room
                        .databaseBuilder(context, AppDatabase::class.java, databaseName)
                        .allowMainThreadQueries()
                        .build()
                var fileClock = AndroidMutableClock(10_000L)
                var fileRepository = androidRepository(fileDatabase, fileClock)
                fileRepository.initialize()
                fileRepository.setAppearance(PetAppearance.CREAM)
                val started = fileRepository.start(QuestType.Q01, androidSnapshot(10, 10_000L))
                val run = (started as QuestCommandResult.Started).run
                fileClock.value = 11_000L
                assertTrue(
                    fileRepository.complete(run.id, run.revision, androidSnapshot(11, 11_000L)) is
                        RewardResult.Applied,
                )
                fileDatabase.close()

                fileDatabase =
                    Room
                        .databaseBuilder(context, AppDatabase::class.java, databaseName)
                        .allowMainThreadQueries()
                        .build()
                fileClock = AndroidMutableClock(12_000L)
                fileRepository = androidRepository(fileDatabase, fileClock)
                assertEquals(PetAppearance.CREAM, fileRepository.profile.first().appearance)
                assertEquals(80, fileRepository.profile.first().totalXp)
                assertEquals(
                    1,
                    fileRepository.progress
                        .first()
                        .completions.size,
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
}

private class AndroidMutableClock(
    var value: Long,
) : Clock {
    override fun nowMillis(): Long = value
}

private class AndroidIds : IdGenerator {
    private val next = AtomicInteger()

    override fun nextId(): String = "android-${next.incrementAndGet()}"
}

private fun androidRepository(
    database: AppDatabase,
    clock: Clock,
) = RoomCompanionRepository(
    database = database,
    identity = ProgressionIdentity("android-profile", SignalSource.REAL),
    clock = clock,
    ids = AndroidIds(),
    evaluator = QuestEvaluator(15_000L),
)

private fun androidSnapshot(
    sequence: Long,
    receivedAtMillis: Long,
) = VehicleSnapshot(
    id = "android-snapshot-$sequence",
    epoch = "android-epoch",
    sequence = sequence,
    receivedAtMillis = receivedAtMillis,
    source = SignalSource.REAL,
    drivingState = DrivingState.PARKED,
    quality = SignalQuality.VALID,
    batteryPercent = 50,
)
