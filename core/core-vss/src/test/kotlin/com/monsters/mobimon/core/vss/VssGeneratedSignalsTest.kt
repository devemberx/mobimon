package com.monsters.mobimon.core.vss

import com.monsters.mobimon.core.vss.generated.VssADAS
import com.monsters.mobimon.core.vss.generated.VssSignals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class VssGeneratedSignalsTest {
    @Test
    fun generatedRootExposesNestedVssBranchesWithDefaults() {
        val signals = VssSignals()

        assertEquals(0, signals.powertrain.transmission.selectedGear)
        assertFalse(signals.powertrain.tractionBattery.charging.isCharging)
        assertEquals("", signals.currentLocation.timestamp)
    }

    @Test
    fun generatedTopLevelClassesCanBeUsedIndependently() {
        val adas = VssADAS()

        assertFalse(adas.cruiseControl.isEnabled)
        assertEquals(0f, adas.obstacleDetection.front.center.distance)
    }
}
