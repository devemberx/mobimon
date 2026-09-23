package com.monsters.mobimon.core.presentation

import com.monsters.mobimon.core.domain.DrivingState
import com.monsters.mobimon.core.domain.SignalQuality
import com.monsters.mobimon.core.domain.SignalSource
import com.monsters.mobimon.core.domain.VehicleSnapshot
import com.monsters.mobimon.core.domain.VehicleWarning
import com.monsters.mobimon.core.domain.WarningSeverity
import org.junit.Assert.assertEquals
import org.junit.Test

class VehicleConditionTest {
    private val normal =
        VehicleSnapshot(
            id = "vehicle",
            epoch = "epoch",
            sequence = 1,
            receivedAtMillis = 100,
            source = SignalSource.SIMULATED,
            drivingState = DrivingState.PARKED,
            quality = SignalQuality.VALID,
            batteryPercent = 80,
            tirePressureStatus = "OK",
            isEmergencyBraking = false,
            isDrowsy = false,
            isDistracted = false,
        )

    @Test
    fun sharedConditionPreservesExistingWarningAndFreshnessRules() {
        val warning =
            VehicleWarning(
                "tire",
                severity = WarningSeverity.CAUTION,
                description = "Low pressure",
                nextAction = "Check",
                observedAtMillis = 100,
            )
        assertEquals(VehicleCondition.CHECKED, normal.vehicleCondition())
        assertEquals(VehicleCondition.WARNING, normal.copy(warnings = listOf(warning)).vehicleCondition())
        assertEquals(
            VehicleCondition.CHECKED,
            normal.copy(warnings = listOf(warning.copy(quality = SignalQuality.STALE))).vehicleCondition(),
        )
        assertEquals(
            VehicleCondition.CHECKED,
            normal.copy(warnings = listOf(warning.copy(severity = WarningSeverity.NOTICE))).vehicleCondition(),
        )
        assertEquals(VehicleCondition.WARNING, normal.copy(isDrowsy = true).vehicleCondition())
        assertEquals(
            VehicleCondition.STALE,
            normal.copy(quality = SignalQuality.STALE, isDrowsy = true).vehicleCondition(),
        )
        assertEquals(VehicleCondition.UNAVAILABLE, normal.copy(quality = SignalQuality.UNAVAILABLE).vehicleCondition())
        assertEquals(VehicleCondition.LOW_BATTERY, normal.copy(batteryPercent = 10).vehicleCondition())
        assertEquals(VehicleCondition.PARTIAL, normal.copy(tirePressureStatus = null).vehicleCondition())
    }
}
