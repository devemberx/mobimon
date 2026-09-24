package com.monsters.mobimon.core.presentation

import androidx.lifecycle.ViewModelStore
import com.monsters.mobimon.core.domain.Clock
import com.monsters.mobimon.core.domain.DrivingState
import com.monsters.mobimon.core.domain.ProgressionIdentity
import com.monsters.mobimon.core.domain.SignalQuality
import com.monsters.mobimon.core.domain.SignalSource
import com.monsters.mobimon.core.domain.SignalSourceProvider
import com.monsters.mobimon.core.domain.UtcClock
import com.monsters.mobimon.core.domain.VehicleFreshnessPolicy
import com.monsters.mobimon.core.domain.VehicleRepository
import com.monsters.mobimon.core.domain.VehicleSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class VehicleStateViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val store = ViewModelStore()
    private val vehicle =
        object : VehicleRepository {
            override val snapshots =
                MutableStateFlow(
                    VehicleSnapshot(
                        id = "moving-card",
                        epoch = "epoch",
                        sequence = 1,
                        receivedAtMillis = 2_000,
                        source = SignalSource.REAL,
                        drivingState = DrivingState.MOVING,
                        quality = SignalQuality.VALID,
                        batteryPercent = 72,
                    ),
                )

            override fun start() = Unit

            override fun stop() = Unit
        }
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

    @Test
    fun vehicleTimestampPeriodDoesNotOverrideDecorativeLocalTime() =
        runTest(dispatcher) {
            val original = vehicle.snapshots.value.copy(timeOfDay = "Morning")
            vehicle.snapshots.value = original
            val model =
                VehicleStateViewModel(
                    vehicle,
                    ProgressionIdentity("profile", SignalSource.REAL),
                    Clock { now },
                    VehicleFreshnessPolicy(15_000),
                    UtcClock { Instant.parse("2026-09-20T09:00:00Z").toEpochMilli() },
                    { ZoneId.of("Asia/Seoul") },
                ).also { store.put("vehicle", it) }
            try {
                assertEquals("18", model.state.value.backgroundTimeOfDay)
                assertEquals("Morning", model.state.value.evidence.timeOfDay)
            } finally {
                store.clear()
                runCurrent()
            }
        }

    @Test
    fun backgroundUsesWallClockAndTracksHourZoneAndClockChangesWithoutVehicleEmissions() =
        runTest(dispatcher) {
            var wallTime = Instant.parse("2026-09-20T08:59:59Z").toEpochMilli()
            var zone = ZoneId.of("Asia/Seoul")
            val original = vehicle.snapshots.value.copy(quality = SignalQuality.UNAVAILABLE)
            vehicle.snapshots.value = original
            val model =
                VehicleStateViewModel(
                    vehicle,
                    ProgressionIdentity("profile", SignalSource.REAL),
                    Clock { now },
                    VehicleFreshnessPolicy(15_000),
                    UtcClock { wallTime },
                    { zone },
                ).also { store.put("vehicle", it) }
            try {
                assertEquals("17", model.state.value.backgroundTimeOfDay)
                runCurrent()
                wallTime += 1_000
                advanceTimeBy(1_000)
                runCurrent()
                assertEquals("18", model.state.value.backgroundTimeOfDay)
                zone = ZoneId.of("UTC")
                advanceTimeBy(1_000)
                runCurrent()
                assertEquals("9", model.state.value.backgroundTimeOfDay)
                wallTime = Instant.parse("2026-09-20T05:00:00Z").toEpochMilli()
                advanceTimeBy(1_000)
                runCurrent()
                assertEquals("5", model.state.value.backgroundTimeOfDay)
                assertEquals(original, model.state.value.evidence)
                assertEquals(null, model.state.value.snapshot.timeOfDay)
                assertEquals(false, model.state.value.snapshot.parkedVerified)
            } finally {
                store.clear()
                runCurrent()
            }
        }

    @Test
    fun explicitBackgroundOverrideResumesLocalTimeWhenCleared() =
        runTest(dispatcher) {
            val backgroundOverride = MutableStateFlow<String?>("Sunset")
            vehicle.snapshots.value = vehicle.snapshots.value.copy(timeOfDay = "Morning")
            val model =
                VehicleStateViewModel(
                    vehicle,
                    ProgressionIdentity("profile", SignalSource.REAL),
                    Clock { now },
                    VehicleFreshnessPolicy(15_000),
                    UtcClock { Instant.parse("2026-09-20T03:00:00Z").toEpochMilli() },
                    { ZoneId.of("Asia/Seoul") },
                    backgroundOverride = backgroundOverride,
                ).also { store.put("vehicle", it) }
            try {
                assertEquals("Sunset", model.state.value.backgroundTimeOfDay)
                runCurrent()
                advanceTimeBy(1_000)
                runCurrent()
                assertEquals("Sunset", model.state.value.backgroundTimeOfDay)
                backgroundOverride.value = null
                runCurrent()
                assertEquals("12", model.state.value.backgroundTimeOfDay)
                backgroundOverride.value = " "
                runCurrent()
                assertEquals("12", model.state.value.backgroundTimeOfDay)
            } finally {
                store.clear()
                runCurrent()
            }
        }

    @Test
    fun displayFreshnessKeepsEvidenceFromTheSameProviderEmission() =
        runTest(dispatcher) {
            try {
                val original = vehicle.snapshots.value
                val model =
                    VehicleStateViewModel(
                        vehicle,
                        ProgressionIdentity("profile", SignalSource.REAL),
                        Clock { now },
                        VehicleFreshnessPolicy(15_000),
                        UtcClock { 0L },
                    ).also { store.put("vehicle", it) }
                assertEquals(original, model.state.value.evidence)
                assertEquals(SignalQuality.VALID, model.state.value.snapshot.batteryQuality)
                assertEquals(0L, model.state.value.snapshot.parkingAgeMillis)

                runCurrent()
                now = 20_000
                advanceTimeBy(1_000)
                runCurrent()
                assertEquals(SignalQuality.STALE, model.state.value.snapshot.quality)
                assertEquals(original, model.state.value.evidence)

                val next = original.copy(id = "next-card", sequence = 2, receivedAtMillis = now)
                vehicle.snapshots.value = next
                runCurrent()
                assertEquals(next, model.state.value.evidence)
                assertEquals("next-card", model.state.value.snapshot.id)
                assertEquals(SignalQuality.VALID, model.state.value.snapshot.quality)
            } finally {
                store.clear()
                runCurrent()
            }
        }

    @Test
    fun firstStateAlreadyRejectsExpiredParkingEvidence() =
        runTest(dispatcher) {
            try {
                now = 20_000
                vehicle.snapshots.value = vehicle.snapshots.value.copy(drivingState = DrivingState.PARKED)
                val model =
                    VehicleStateViewModel(
                        vehicle,
                        ProgressionIdentity("profile", SignalSource.REAL),
                        Clock { now },
                        VehicleFreshnessPolicy(15_000),
                        UtcClock { 0L },
                    ).also { store.put("vehicle", it) }

                assertEquals(SignalQuality.STALE, model.state.value.snapshot.quality)
                assertEquals(false, model.state.value.snapshot.parkedVerified)
            } finally {
                store.clear()
                runCurrent()
            }
        }

    @Test
    fun firstStateAlreadyRejectsAnUnexpectedSource() =
        runTest(dispatcher) {
            try {
                val model =
                    VehicleStateViewModel(
                        vehicle,
                        ProgressionIdentity("profile", SignalSource.SIMULATED),
                        Clock { now },
                        VehicleFreshnessPolicy(15_000),
                        UtcClock { 0L },
                    ).also { store.put("vehicle", it) }

                assertEquals(SignalQuality.UNAVAILABLE, model.state.value.snapshot.quality)
                assertEquals(SignalQuality.UNAVAILABLE, model.state.value.snapshot.batteryQuality)
            } finally {
                store.clear()
                runCurrent()
            }
        }

    @Test
    fun debuggerSourceProviderAcceptsSimulatedParkingInReleaseIdentity() =
        runTest(dispatcher) {
            try {
                vehicle.snapshots.value =
                    vehicle.snapshots.value.copy(
                        source = SignalSource.SIMULATED,
                        drivingState = DrivingState.PARKED,
                    )
                val model =
                    VehicleStateViewModel(
                        vehicle,
                        ProgressionIdentity("profile", SignalSource.REAL),
                        Clock { now },
                        VehicleFreshnessPolicy(15_000),
                        UtcClock { 0L },
                        sourceProvider = SignalSourceProvider { SignalSource.SIMULATED },
                    ).also { store.put("vehicle", it) }

                assertEquals(SignalQuality.VALID, model.state.value.snapshot.quality)
                assertEquals(true, model.state.value.snapshot.parkedVerified)
            } finally {
                store.clear()
                runCurrent()
            }
        }

    @Test
    fun freshReadingBetweenTimerTicksDoesNotBecomeUnavailable() =
        runTest(dispatcher) {
            try {
                val model =
                    VehicleStateViewModel(
                        vehicle,
                        ProgressionIdentity("profile", SignalSource.REAL),
                        Clock { now },
                        VehicleFreshnessPolicy(15_000),
                        UtcClock { 0L },
                    ).also { store.put("vehicle", it) }
                runCurrent()
                repeat(5) {
                    now += 400
                    vehicle.snapshots.value = vehicle.snapshots.value.copy(receivedAtMillis = now, sequence = it + 2L)
                    runCurrent()
                    assertEquals(SignalQuality.VALID, model.state.value.snapshot.quality)
                    assertEquals(SignalQuality.VALID, model.state.value.snapshot.batteryQuality)
                }
                vehicle.snapshots.value = vehicle.snapshots.value.copy(receivedAtMillis = now + 1_000)
                runCurrent()
                assertEquals(SignalQuality.UNAVAILABLE, model.state.value.snapshot.quality)
                assertEquals(SignalQuality.UNAVAILABLE, model.state.value.snapshot.batteryQuality)
            } finally {
                store.clear()
                runCurrent()
            }
        }

    @Test
    fun movingReadingExpiresIndependentlyOfQuestObservation() =
        runTest(dispatcher) {
            try {
                val model =
                    VehicleStateViewModel(
                        vehicle,
                        ProgressionIdentity("profile", SignalSource.REAL),
                        Clock { now },
                        VehicleFreshnessPolicy(15_000),
                        UtcClock { 0L },
                    ).also { store.put("vehicle", it) }
                runCurrent()
                assertEquals(SignalQuality.VALID, model.state.value.snapshot.quality)
                now = 20_000
                advanceTimeBy(1_000)
                runCurrent()
                assertEquals(SignalQuality.STALE, model.state.value.snapshot.quality)
                assertEquals(72, model.state.value.snapshot.batteryPercent)
            } finally {
                store.clear()
                runCurrent()
            }
        }
}
