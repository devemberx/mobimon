package com.monsters.mobimon.core.vss

import com.monsters.mobimon.core.domain.DrivingState
import com.monsters.mobimon.core.domain.SignalQuality
import com.monsters.mobimon.core.domain.SignalSource
import com.monsters.mobimon.core.domain.SignalUnavailableReason
import org.junit.Assert.assertEquals
import org.junit.Test

class VssVehicleInterpreterTest {
    @Test
    fun unavailableRawSourceDefaultsToParkGearStandingStill() {
        val raw = DefaultParkedVssRawVehicleSource().state.value

        assertEquals(126, raw?.selectedGear)
        assertEquals(false, raw?.vehicleIsMoving)
        assertEquals(0f, raw?.vehicleSpeedKmh)
    }

    @Test
    fun unavailableRawStateReportsNotReportedRealVehicleData() {
        val snapshot =
            VssVehicleInterpreter.snapshot(
                raw = null,
                id = "id-1",
                epoch = "epoch",
                sequence = 1,
                observedAtMillis = 100,
                source = SignalSource.REAL,
            )

        assertEquals(SignalSource.REAL, snapshot.source)
        assertEquals(DrivingState.UNKNOWN, snapshot.drivingState)
        assertEquals(SignalQuality.UNAVAILABLE, snapshot.quality)
        assertEquals(SignalQuality.UNAVAILABLE, snapshot.batteryQuality)
        assertEquals(SignalUnavailableReason.NOT_REPORTED, snapshot.parkingUnavailableReason)
        assertEquals(SignalUnavailableReason.NOT_REPORTED, snapshot.batteryUnavailableReason)
    }

    @Test
    fun rawVssSignalsComputeVehicleSnapshotInterpretation() {
        val snapshot =
            VssVehicleInterpreter.snapshot(
                raw =
                    VssRawVehicleState(
                        driverDistractionLevel = 72f,
                        driverFatigueLevel = 75f,
                        dmsIsWarning = true,
                        driverEmergencyBrakingDetected = true,
                        obstacleFrontCenterDistance = 16f,
                        tractionBatterySocDisplayed = 49f,
                        chargingCableConnected = true,
                        chargingAveragePowerKw = 3.5f,
                        exteriorAirTemperature = 27f,
                        rainIntensity = 5,
                        washerFluidLevel = 34,
                        row1LeftTirePressureLow = true,
                        diagnosticsDtcCount = 2,
                        vehicleIsMoving = false,
                        vehicleSpeedKmh = 20f,
                        selectedGear = 126,
                        combustionEngineRunning = true,
                        currentLocationTimestamp = "2026-10-08T09:00:00Z",
                    ),
                id = "id-1",
                epoch = "epoch",
                sequence = 1,
                observedAtMillis = 100,
                source = SignalSource.REAL,
            )

        assertEquals(SignalSource.REAL, snapshot.source)
        assertEquals(DrivingState.MOVING, snapshot.drivingState)
        assertEquals(SignalQuality.VALID, snapshot.quality)
        assertEquals(49, snapshot.batteryPercent)
        assertEquals(20, snapshot.speed)
        assertEquals("P", snapshot.gear)
        assertEquals(true, snapshot.isDistracted)
        assertEquals(true, snapshot.isDrowsy)
        assertEquals(28, snapshot.attentionLevel)
        assertEquals(true, snapshot.isEmergencyBraking)
        assertEquals(16, snapshot.distanceToFrontVehicle)
        assertEquals(true, snapshot.isCharging)
        assertEquals(27, snapshot.outsideTemperature)
        assertEquals(true, snapshot.isRaining)
        assertEquals(34, snapshot.washerFluidLevel)
        assertEquals("NG", snapshot.tirePressureStatus)
        assertEquals(true, snapshot.isEngineWarning)
        assertEquals(true, snapshot.isEngineOn)
        assertEquals("Morning", snapshot.timeOfDay)
    }

    @Test
    fun overridesWinOverRawVssInterpretation() {
        val snapshot =
            VssVehicleInterpreter.snapshot(
                raw =
                    VssRawVehicleState(
                        driverDistractionLevel = 100f,
                        vehicleIsMoving = true,
                        vehicleSpeedKmh = 80f,
                        selectedGear = 127,
                    ),
                id = "id-1",
                epoch = "epoch",
                sequence = 1,
                observedAtMillis = 100,
                source = SignalSource.SIMULATED,
                overrides =
                    VssInterpretationOverrides(
                        isDistracted = false,
                        isMoving = false,
                        speed = 0,
                        gear = "P",
                        batteryPercent = 11,
                        timeOfDay = "밤",
                    ),
            )

        assertEquals(SignalSource.SIMULATED, snapshot.source)
        assertEquals(DrivingState.PARKED, snapshot.drivingState)
        assertEquals(false, snapshot.isDistracted)
        assertEquals(0, snapshot.speed)
        assertEquals("P", snapshot.gear)
        assertEquals(11, snapshot.batteryPercent)
        assertEquals("Night", snapshot.timeOfDay)
    }
}
