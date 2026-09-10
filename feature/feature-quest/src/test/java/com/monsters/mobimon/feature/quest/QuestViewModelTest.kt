package com.monsters.mobimon.feature.quest

import androidx.lifecycle.ViewModelStore
import com.monsters.mobimon.core.domain.Clock
import com.monsters.mobimon.core.domain.DrivingState
import com.monsters.mobimon.core.domain.ProgressionIdentity
import com.monsters.mobimon.core.domain.QuestCommandResult
import com.monsters.mobimon.core.domain.QuestEvaluator
import com.monsters.mobimon.core.domain.QuestProgress
import com.monsters.mobimon.core.domain.QuestRepository
import com.monsters.mobimon.core.domain.QuestRun
import com.monsters.mobimon.core.domain.QuestStatus
import com.monsters.mobimon.core.domain.QuestType
import com.monsters.mobimon.core.domain.RewardRepository
import com.monsters.mobimon.core.domain.RewardResult
import com.monsters.mobimon.core.domain.SignalQuality
import com.monsters.mobimon.core.domain.SignalSource
import com.monsters.mobimon.core.domain.VehicleRepository
import com.monsters.mobimon.core.domain.VehicleSnapshot
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class QuestViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val store = ViewModelStore()
    private val vehicle = TestVehicle()
    private val local = TestQuests()
    private var now = 2_000L

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        store.clear()
        Dispatchers.resetMain()
    }

    private fun runModelTest(block: suspend TestScope.() -> Unit) =
        runTest(dispatcher) {
            try {
                block()
            } finally {
                // Cancel the recurring freshness ticker before runTest drains virtual time.
                store.clear()
                runCurrent()
            }
        }

    private fun subject() =
        QuestViewModel(
            local,
            local,
            vehicle,
            ProgressionIdentity("profile", SignalSource.REAL),
            Clock {
                now
            },
            QuestEvaluator(15_000),
        ).also { store.put("quest", it) }

    @Test
    fun staleDisplayedCardCannotCompleteTheNewerSnapshot() =
        runModelTest {
            val vm = subject()
            runCurrent()
            vm.acknowledge("previous-card")
            runCurrent()
            assertEquals(0, local.rewardCalls)
            assertEquals(QuestMessage.REFRESH_REQUIRED, vm.state.value.message)
        }

    @Test
    fun movingAndUnknownStateBlockCommands() =
        runModelTest {
            val vm = subject()
            for (driving in listOf(DrivingState.MOVING, DrivingState.UNKNOWN)) {
                vehicle.snapshots.value = vehicle.snapshots.value.copy(drivingState = driving)
                runCurrent()
                vm.start(QuestType.Q01)
                vm.cancel()
                vm.acknowledge(vehicle.snapshots.value.id)
                runCurrent()
                assertFalse(vm.state.value.canManageQuest)
                assertFalse(vm.state.value.canAcknowledge)
            }
            assertEquals(0, local.commandCalls)
            assertEquals(0, local.rewardCalls)
        }

    @Test
    fun pendingRewardPreventsDoubleTapAndFailureDoesNotInventCompletion() =
        runModelTest {
            val vm = subject()
            runCurrent()
            assertTrue(vm.state.value.canAcknowledge)
            vm.acknowledge("card-2")
            vm.acknowledge("card-2")
            runCurrent()
            assertTrue(vm.state.value.isBusy)
            assertEquals(1, local.rewardCalls)
            local.rewardGate.complete(RewardResult.StorageFailure)
            runCurrent()
            assertFalse(vm.state.value.isBusy)
            assertEquals(QuestMessage.STORAGE_FAILURE, vm.state.value.message)
            assertTrue(
                vm.state.value.progress.completions
                    .isEmpty(),
            )
        }

    @Test
    fun snapshotFreshnessExpiresWithoutAnotherVehicleEmission() =
        runModelTest {
            val vm = subject()
            runCurrent()
            assertTrue(vm.state.value.canAcknowledge)
            now = 30_000
            dispatcher.scheduler.advanceTimeBy(1_000)
            runCurrent()
            assertFalse(vm.state.value.canAcknowledge)
            assertEquals(SignalQuality.STALE, vm.state.value.snapshot.quality)
        }

    @Test
    fun failedObservationCanBeRetriedWithoutRecreatingViewModel() =
        runModelTest {
            local.failObservation = true
            val vm = subject()
            runCurrent()
            assertEquals(QuestMessage.STORAGE_FAILURE, vm.state.value.message)
            assertTrue(vm.state.value.observationFailed)
            vm.start(QuestType.Q01)
            runCurrent()
            assertEquals(0, local.commandCalls)
            local.failObservation = false
            vm.retry()
            runCurrent()
            assertTrue(vm.state.value.canAcknowledge)
            assertFalse(vm.state.value.observationFailed)
            assertNull(vm.state.value.message)
        }

    @Test
    fun validVehicleRecoveryClearsObsoleteValidationError() =
        runModelTest {
            val parked = vehicle.snapshots.value
            vehicle.snapshots.value = parked.copy(drivingState = DrivingState.UNKNOWN)
            val vm = subject()
            runCurrent()
            vm.start(QuestType.Q01)
            runCurrent()
            assertEquals(QuestMessage.NOT_PARKED, vm.state.value.message)
            vehicle.snapshots.value = parked
            runCurrent()
            assertNull(vm.state.value.message)
        }

    @Test
    fun freshNewEpochCanCancelInterruptedRunToAllowRestart() =
        runModelTest {
            vehicle.snapshots.value = vehicle.snapshots.value.copy(epoch = "new-epoch", sequence = 1)
            val vm = subject()
            runCurrent()
            assertFalse(vm.state.value.canAcknowledge)
            vm.cancel()
            runCurrent()
            assertEquals(1, local.commandCalls)
        }

    private class TestVehicle : VehicleRepository {
        override val snapshots =
            MutableStateFlow(
                VehicleSnapshot(
                    "card-2",
                    "epoch",
                    2,
                    2_000,
                    SignalSource.REAL,
                    DrivingState.PARKED,
                    SignalQuality.VALID,
                    72,
                ),
            )

        override fun start() = Unit

        override fun stop() = Unit
    }

    private class TestQuests :
        QuestRepository,
        RewardRepository {
        private val storedProgress =
            MutableStateFlow(
                QuestProgress(
                    QuestRun(
                        "run",
                        "profile",
                        QuestType.Q01,
                        QuestStatus.ACTIVE,
                        1,
                        1,
                        80,
                        "epoch",
                        1,
                        1_000,
                        SignalSource.REAL,
                    ),
                ),
            )
        var failObservation = false
        override val progress get() =
            flow {
                check(!failObservation) { "controlled observation failure" }
                emitAll(storedProgress)
            }
        var commandCalls = 0
        var rewardCalls = 0
        val rewardGate = CompletableDeferred<RewardResult>()

        override suspend fun start(
            type: QuestType,
            snapshot: VehicleSnapshot,
        ): QuestCommandResult {
            commandCalls++
            return QuestCommandResult.AlreadyActive
        }

        override suspend fun cancel(
            runId: String,
            expectedRevision: Long,
            snapshot: VehicleSnapshot,
        ): QuestCommandResult {
            commandCalls++
            return QuestCommandResult.Cancelled
        }

        override suspend fun complete(
            runId: String,
            expectedRevision: Long,
            snapshot: VehicleSnapshot,
        ): RewardResult {
            rewardCalls++
            return rewardGate.await()
        }
    }
}
