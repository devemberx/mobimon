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
import com.monsters.mobimon.debug.DebugRawVssState
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

            listOf(127, -1, 0).forEach { selectedGear ->
                debug.mutableState.value =
                    DebugVssState(
                        raw =
                            DebugRawVssState(
                                selectedGear = selectedGear,
                                vehicleIsMoving = false,
                                vehicleSpeedKmh = 0f,
                            ),
                    )
                runCurrent()
                assertEquals(DrivingState.UNKNOWN, repository.snapshots.value.drivingState)
            }

            debug.mutableState.value =
                DebugVssState(
                    raw =
                        DebugRawVssState(
                            selectedGear = 126,
                            vehicleIsMoving = true,
                            vehicleSpeedKmh = 0f,
                        ),
                )
            runCurrent()
            assertEquals(DrivingState.MOVING, repository.snapshots.value.drivingState)

            debug.mutableState.value =
                DebugVssState(
                    raw =
                        DebugRawVssState(
                            selectedGear = 126,
                            vehicleIsMoving = false,
                            vehicleSpeedKmh = 1f,
                        ),
                )
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
            debug.mutableState.value =
                debug.mutableState.value.copy(
                    raw =
                        debug.mutableState.value.raw
                            .copy(tractionBatterySocDisplayed = 71f),
                )
            runCurrent()

            assertEquals(3L, repository.snapshots.value.sequence)
            assertEquals("epoch-3", repository.snapshots.value.id)
            assertEquals(listOf(1L, 2L, 3L), published.map { it.sequence })
            assertEquals(listOf("epoch-1", "epoch-2", "epoch-3"), published.map { it.id })
            debug.mutableState.value =
                debug.mutableState.value.copy(
                    raw =
                        debug.mutableState.value.raw
                            .copy(tractionBatterySocDisplayed = 70f),
                )
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

    @Test
    fun debugRawVssSignalsPropagateToVehicleSnapshot() =
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

            debug.mutableState.value =
                DebugVssState(
                    raw =
                        DebugRawVssState(
                            tractionBatterySocDisplayed = 45f,
                            vehicleSpeedKmh = 60f,
                            selectedGear = 127,
                            vehicleIsMoving = true,
                            exteriorAirTemperature = 25f,
                            rainIntensity = 3,
                            driverDistractionLevel = 80f,
                            driverFatigueLevel = 75f,
                            dmsIsWarning = true,
                            driverEmergencyBrakingDetected = true,
                            obstacleFrontCenterDistance = 15f,
                            tractionBatteryChargingIsCharging = true,
                            washerFluidLevel = 30,
                            row1LeftTirePressureLow = true,
                            obdMilOn = true,
                            combustionEngineRunning = true,
                            currentLocationTimestamp = "2026-10-08T14:00:00Z",
                        ),
                )
            runCurrent()

            val snapshot = repository.snapshots.value
            assertEquals(45, snapshot.batteryPercent)
            assertEquals(60, snapshot.speed)
            assertEquals("D", snapshot.gear)
            assertEquals(DrivingState.MOVING, snapshot.drivingState)
            assertEquals(25, snapshot.outsideTemperature)
            assertEquals(true, snapshot.isRaining)
            assertEquals(true, snapshot.isDistracted)
            assertEquals(true, snapshot.isDrowsy)
            assertEquals(20, snapshot.attentionLevel)
            assertEquals(true, snapshot.isEmergencyBraking)
            assertEquals(15, snapshot.distanceToFrontVehicle)
            assertEquals(true, snapshot.isCharging)
            assertEquals(30, snapshot.washerFluidLevel)
            assertEquals("NG", snapshot.tirePressureStatus)
            assertEquals(true, snapshot.isEngineWarning)
            assertEquals(true, snapshot.isEngineOn)
            assertEquals("Day", snapshot.timeOfDay)

            repository.stop()
        }
}
