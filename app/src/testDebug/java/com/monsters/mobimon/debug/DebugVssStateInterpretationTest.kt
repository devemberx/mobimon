package com.monsters.mobimon.debug

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DebugVssStateInterpretationTest {
    @Test
    fun rawVssSignalsComputeInterpretationValues() {
        val state =
            DebugVssState(
                raw =
                    DebugRawVssState(
                        driverDistractionLevel = 72f,
                        driverFatigueLevel = 10f,
                        dmsIsWarning = false,
                        chargingCableConnected = true,
                        chargingAveragePowerKw = 3.5f,
                        rainIntensity = 40,
                        obdMilOn = true,
                        diagnosticsDtcCount = 2,
                        row1LeftTirePressureLow = false,
                        row1RightTirePressureLow = false,
                        row2LeftTirePressureLow = true,
                        row2RightTirePressureLow = false,
                        vehicleIsMoving = false,
                        vehicleSpeedKmh = 12f,
                        selectedGear = 126,
                        destinationLatitude = 37.5665,
                        destinationLongitude = 126.9780,
                        currentLatitude = 37.5651,
                        currentLongitude = 126.9895,
                    ),
            )

        assertTrue(state.isDistracted)
        assertFalse(state.isDrowsy)
        assertEquals(28, state.attentionLevel)
        assertTrue(state.isCharging)
        assertTrue(state.isRaining)
        assertTrue(state.isEngineWarning)
        assertEquals("NG", state.tirePressureStatus)
        assertTrue(state.isMoving)
        assertEquals(12, state.speed)
        assertEquals("P", state.gear)
        assertTrue(state.isNavigating)
        assertTrue(state.distanceToDestination > 0)
    }

    @Test
    fun interpretationOverridesWinOverRawCalculations() {
        val state =
            DebugVssState(
                raw =
                    DebugRawVssState(
                        driverDistractionLevel = 90f,
                        vehicleSpeedKmh = 40f,
                        selectedGear = 127,
                    ),
                overrides =
                    DebugInterpretationOverrides(
                        isDistracted = false,
                        speed = 5,
                        gear = "N",
                        isMoving = false,
                    ),
            )

        assertFalse(state.isDistracted)
        assertEquals(5, state.speed)
        assertEquals("N", state.gear)
        assertFalse(state.isMoving)
    }

    @Test
    fun timeOfDayDefaultsToDayAndAcceptsOverrides() {
        val defaultState = DebugVssState()
        assertEquals("낮", defaultState.timeOfDay)

        val overriddenState =
            DebugVssState(
                overrides = DebugInterpretationOverrides(timeOfDay = "아침"),
            )
        assertEquals("아침", overriddenState.timeOfDay)

        val nightState =
            DebugVssState(
                overrides = DebugInterpretationOverrides(timeOfDay = "밤"),
            )
        assertEquals("밤", nightState.timeOfDay)
    }
}
