package com.monsters.mobimon.vehicle

import com.monsters.mobimon.core.domain.Clock
import com.monsters.mobimon.core.domain.CompanionSettings
import com.monsters.mobimon.core.domain.DrivingState
import com.monsters.mobimon.core.domain.IdGenerator
import com.monsters.mobimon.core.domain.SettingsRepository
import com.monsters.mobimon.core.domain.SignalQuality
import com.monsters.mobimon.core.domain.SignalSource
import com.monsters.mobimon.core.domain.VehicleSnapshot
import com.monsters.mobimon.core.domain.WriteResult
import com.monsters.mobimon.debug.DebugVssProvider
import com.monsters.mobimon.debug.DebugVssState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class FakeSettingsRepository : SettingsRepository {
    val mutableSettings = MutableStateFlow(CompanionSettings())
    override val settings: Flow<CompanionSettings> = mutableSettings

    override suspend fun setReducedMotion(enabled: Boolean): WriteResult = WriteResult.Success

    override suspend fun setLauncherCharacterEnabled(enabled: Boolean): WriteResult = WriteResult.Success

    override suspend fun setDebugModeEnabled(enabled: Boolean): WriteResult = WriteResult.Success
}

class FakeDebugStore : DebugVssProvider {
    val mutableState = MutableStateFlow(DebugVssState())
    override val state: StateFlow<DebugVssState> = mutableState
}

@OptIn(ExperimentalCoroutinesApi::class)
class DemoVehicleRepositoryTest {
    @Test
    fun debugParkingRequiresParkGearAndNonContradictoryMotion() =
        runTest {
            val settings = FakeSettingsRepository()
            settings.mutableSettings.value = CompanionSettings(debugModeEnabled = true)
            val debug = FakeDebugStore()
            val repository =
                DemoVehicleRepository(
                    Clock { testScheduler.currentTime },
                    IdGenerator { "epoch" },
                    settings,
                    debug,
                    backgroundScope,
                )
            repository.start()
            runCurrent()
            assertEquals(DrivingState.PARKED, repository.snapshots.value.drivingState)

            listOf("D", "R", "N").forEach { gear ->
                debug.mutableState.value = DebugVssState(gear = gear, isMoving = false, speed = 0)
                runCurrent()
                assertEquals(DrivingState.UNKNOWN, repository.snapshots.value.drivingState)
            }

            debug.mutableState.value = DebugVssState(gear = "P", isMoving = true, speed = 0)
            runCurrent()
            assertEquals(DrivingState.MOVING, repository.snapshots.value.drivingState)

            debug.mutableState.value = DebugVssState(gear = "P", isMoving = false, speed = 1)
            runCurrent()
            assertEquals(DrivingState.MOVING, repository.snapshots.value.drivingState)
            repository.stop()
        }

    @Test
    fun concurrentTimerAndDebugChangesUseOneMonotonicSequence() =
        runTest {
            val settings = FakeSettingsRepository()
            settings.mutableSettings.value = CompanionSettings(debugModeEnabled = true)
            val debug = FakeDebugStore()
            val repository =
                DemoVehicleRepository(
                    Clock { testScheduler.currentTime },
                    IdGenerator { "epoch" },
                    settings,
                    debug,
                    backgroundScope,
                )
            val published = mutableListOf<VehicleSnapshot>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                repository.snapshots
                    .drop(1)
                    .take(4)
                    .toList(published)
            }

            repository.start()
            runCurrent()
            assertEquals(1L, repository.snapshots.value.sequence)

            advanceTimeBy(2_000)
            debug.mutableState.value = debug.mutableState.value.copy(batteryPercent = 71)
            runCurrent()

            assertEquals(3L, repository.snapshots.value.sequence)
            assertEquals("epoch-3", repository.snapshots.value.id)
            assertEquals(listOf(1L, 2L, 3L), published.map { it.sequence })
            assertEquals(listOf("epoch-1", "epoch-2", "epoch-3"), published.map { it.id })
            debug.mutableState.value = debug.mutableState.value.copy(batteryPercent = 70)
            runCurrent()
            assertEquals(4L, repository.snapshots.value.sequence)
            assertEquals("epoch-4", repository.snapshots.value.id)
            repository.stop()
        }

    @Test
    fun simulationHasOneObservationJobAndANewEpochAfterEveryGap() =
        runTest {
            var id = 0
            val repository =
                DemoVehicleRepository(
                    Clock { testScheduler.currentTime },
                    IdGenerator { "id-${++id}" },
                    FakeSettingsRepository(),
                    FakeDebugStore(),
                    backgroundScope,
                )
            repository.start()
            runCurrent()
            val first = repository.snapshots.value
            assertEquals(SignalSource.SIMULATED, first.source)
            assertEquals(DrivingState.PARKED, first.drivingState)
            assertEquals(SignalQuality.VALID, first.quality)
            repository.start()
            assertEquals(first.epoch, repository.snapshots.value.epoch)
            advanceTimeBy(2_000)
            runCurrent()
            assertTrue(repository.snapshots.value.sequence > first.sequence)
            repository.stop()
            val stopped = repository.snapshots.value
            assertEquals(SignalQuality.UNAVAILABLE, stopped.quality)
            advanceTimeBy(4_000)
            runCurrent()
            assertEquals(stopped, repository.snapshots.value)
            repository.start()
            runCurrent()
            assertNotEquals(first.epoch, repository.snapshots.value.epoch)
            repository.stop()
        }

    @Test
    fun stopWinsWhenAPeriodicPublishIsAlreadyInFlight() {
        val scheduler = TestCoroutineScheduler()
        val publishEntered = CountDownLatch(1)
        val releasePublish = CountDownLatch(1)
        val clockCalls = AtomicInteger()
        val parent = SupervisorJob()
        val scope = CoroutineScope(parent + StandardTestDispatcher(scheduler))
        val worker = Executors.newSingleThreadExecutor()
        val repository =
            DemoVehicleRepository(
                clock =
                    Clock {
                        if (clockCalls.incrementAndGet() == 2) {
                            publishEntered.countDown()
                            check(releasePublish.await(5, TimeUnit.SECONDS))
                        }
                        scheduler.currentTime
                    },
                ids = IdGenerator { "epoch" },
                settingsRepository = FakeSettingsRepository(),
                debugStore = FakeDebugStore(),
                scope = scope,
            )

        try {
            repository.start()
            scheduler.advanceTimeBy(2_000)
            val inFlightPublish = worker.submit { scheduler.runCurrent() }
            assertTrue(publishEntered.await(5, TimeUnit.SECONDS))

            repository.stop()
            releasePublish.countDown()
            inFlightPublish.get(5, TimeUnit.SECONDS)

            assertEquals(SignalQuality.UNAVAILABLE, repository.snapshots.value.quality)
            assertEquals(DrivingState.UNKNOWN, repository.snapshots.value.drivingState)
        } finally {
            releasePublish.countDown()
            parent.cancel()
            worker.shutdownNow()
        }
    }
}
