package com.devemberx.rivo

import android.app.Application
import androidx.lifecycle.ViewModelStore
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.devemberx.rivo.core.database.AppDatabase
import com.devemberx.rivo.core.database.RoomCompanionRepository
import com.devemberx.rivo.core.domain.Clock
import com.devemberx.rivo.core.domain.CompanionSettings
import com.devemberx.rivo.core.domain.DrivingState
import com.devemberx.rivo.core.domain.IdGenerator
import com.devemberx.rivo.core.domain.ProgressionIdentity
import com.devemberx.rivo.core.domain.QuestEvaluator
import com.devemberx.rivo.core.domain.QuestType
import com.devemberx.rivo.core.domain.RewardCalculator
import com.devemberx.rivo.core.domain.SettingsRepository
import com.devemberx.rivo.core.domain.SignalQuality
import com.devemberx.rivo.core.domain.SignalSource
import com.devemberx.rivo.core.domain.VehicleRepository
import com.devemberx.rivo.core.domain.VehicleSnapshot
import com.devemberx.rivo.core.domain.WriteResult
import com.devemberx.rivo.feature.pet.PetViewModel
import com.devemberx.rivo.feature.quest.QuestMessage
import com.devemberx.rivo.feature.quest.QuestViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.SQLiteMode
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
@SQLiteMode(SQLiteMode.Mode.NATIVE)
class Q01JourneyTest {
    private val dispatcher = StandardTestDispatcher()
    private val databaseExecutor = Executors.newSingleThreadExecutor()
    private val store = ViewModelStore()
    private val vehicle = JourneyVehicle()
    private val settings = JourneySettings()
    private val identity = ProgressionIdentity("journey-profile", SignalSource.SIMULATED)
    private val evaluator = QuestEvaluator(maxAgeMillis = 15_000)

    @Volatile
    private var nowMillis = 10_000L
    private var nextId = 0
    private lateinit var database: AppDatabase
    private lateinit var repository: RoomCompanionRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        database =
            Room
                .inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase::class.java)
                .allowMainThreadQueries()
                // Queue invalidation queries until the suspending transaction has left its thread context.
                .setQueryExecutor(databaseExecutor)
                .setTransactionExecutor(databaseExecutor)
                .build()
        repository =
            RoomCompanionRepository(
                database,
                identity,
                Clock { nowMillis },
                IdGenerator { "journey-${++nextId}" },
                evaluator,
            )
    }

    @After
    fun tearDown() {
        store.clear()
        database.close()
        databaseExecutor.shutdown()
        try {
            assertTrue("Room executor did not stop", databaseExecutor.awaitTermination(5, TimeUnit.SECONDS))
        } finally {
            databaseExecutor.shutdownNow()
        }
        Dispatchers.resetMain()
    }

    @Test
    fun newerDisplayedSnapshotAwardsOnceAndViewModelRecreationKeepsGrowth() =
        runTest(dispatcher) {
            try {
                val firstPet = petModel()
                val firstQuest = questModel()
                val initial = firstPet.state.awaitState("initial profile") { !it.isLoading && it.profile != null }
                assertEquals(0, initial.profile?.totalXp)
                firstQuest.state.awaitState("initial quest availability") { it.canManageQuest }

                firstQuest.start(QuestType.Q01)
                val active = firstQuest.state.awaitState("Q01 started") { it.progress.activeRun != null && !it.isBusy }
                assertFalse(active.canAcknowledge)
                assertTrue(active.progress.completions.isEmpty())

                nowMillis = 11_000
                vehicle.snapshots.value =
                    vehicle.snapshots.value.copy(
                        id = "displayed-after-start",
                        sequence = 2,
                        receivedAtMillis = nowMillis,
                    )
                val displayed = firstQuest.state.awaitState("new snapshot is acknowledgeable") { it.canAcknowledge }
                firstQuest.acknowledge(displayed.snapshot.id)

                val completed =
                    firstQuest.state.awaitState("Q01 completion observed") {
                        it.progress.completions.size == 1 && !it.isBusy
                    }
                val grown = firstPet.state.awaitState("80 XP observed") { it.profile?.totalXp == 80 }
                assertNull(completed.progress.activeRun)
                assertEquals(
                    "displayed-after-start",
                    completed.progress.completions
                        .single()
                        .snapshotId,
                )
                assertEquals(2, RewardCalculator().stage(requireNotNull(grown.profile).totalXp))
                val completionId =
                    completed.progress.completions
                        .single()
                        .id

                // Recreate feature owners while retaining the actual local database.
                store.clear()
                runCurrent()
                val recreatedPet = petModel()
                val recreatedQuest = questModel()
                val restored =
                    recreatedPet.state.awaitState(
                        "recreated profile",
                    ) { !it.isLoading && it.profile != null }
                recreatedQuest.state.awaitState("recreated completion") { it.progress.completions.size == 1 }
                assertEquals(80, restored.profile?.totalXp)

                recreatedQuest.start(QuestType.Q01)
                recreatedQuest.state.awaitState("repeat start rejected") {
                    !it.isBusy && it.message == QuestMessage.ALREADY_COMPLETED
                }
                recreatedQuest.acknowledge(recreatedQuest.state.value.snapshot.id)
                recreatedQuest.state.awaitState("repeat acknowledge rejected") {
                    !it.isBusy && it.message == QuestMessage.REFRESH_REQUIRED
                }

                assertEquals(80, repository.profile.first().totalXp)
                assertEquals(
                    completionId,
                    repository.progress
                        .first()
                        .completions
                        .single()
                        .id,
                )
                assertEquals(2, RewardCalculator().stage(requireNotNull(recreatedPet.state.value.profile).totalXp))
            } finally {
                // Cancel recurring ViewModel freshness work before runTest advances toward idle.
                store.clear()
                runCurrent()
            }
        }

    private fun petModel() = PetViewModel(repository, settings).also { store.put("pet", it) }

    private fun questModel() =
        QuestViewModel(repository, repository, vehicle, identity, Clock { nowMillis }, evaluator)
            .also { store.put("quest", it) }

    private suspend fun <T> StateFlow<T>.awaitState(
        stage: String,
        predicate: (T) -> Boolean,
    ): T =
        // Room performs real IO; the ViewModel ticker must not advance a virtual timeout ahead of that work.
        withContext(Dispatchers.Default) {
            try {
                withTimeout(5_000) { first(predicate) }
            } catch (timeout: TimeoutCancellationException) {
                throw AssertionError("Timed out at '$stage'; current state: $value", timeout)
            }
        }

    private class JourneyVehicle : VehicleRepository {
        override val snapshots =
            MutableStateFlow(
                VehicleSnapshot(
                    id = "start-card",
                    epoch = "journey-epoch",
                    sequence = 1,
                    receivedAtMillis = 10_000,
                    source = SignalSource.SIMULATED,
                    drivingState = DrivingState.PARKED,
                    quality = SignalQuality.VALID,
                    batteryPercent = 72,
                ),
            )

        override fun start() = Unit

        override fun stop() = Unit
    }

    private class JourneySettings : SettingsRepository {
        override val settings = MutableStateFlow(CompanionSettings())

        override suspend fun setShowOnVehicleHome(enabled: Boolean): WriteResult {
            settings.value = settings.value.copy(showOnVehicleHome = enabled)
            return WriteResult.Success
        }

        override suspend fun setReducedMotion(enabled: Boolean): WriteResult {
            settings.value = settings.value.copy(reducedMotion = enabled)
            return WriteResult.Success
        }
    }
}
