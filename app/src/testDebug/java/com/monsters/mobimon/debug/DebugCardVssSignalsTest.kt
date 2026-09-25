package com.monsters.mobimon.debug

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.monsters.mobimon.core.domain.CardVssType
import com.monsters.mobimon.core.domain.VehicleCardVssDefaults
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DebugCardVssSignalsTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun clear() {
        context
            .getSharedPreferences("debug_vss_prefs", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
    }

    @Test
    fun everyCardSignalStartsWithAValueAndCanBeEdited() {
        val store = DebugStore(context)
        val values =
            store.state.value.raw
                .toCardSignalValues() + store.state.value.cardExtraSignals
        assertEquals(VehicleCardVssDefaults.values.keys, values.keys)
        VehicleCardVssDefaults.definitions.forEach { definition ->
            val actual = values[definition.path]
            if (definition.type == CardVssType.NUMBER) {
                assertEquals(definition.path, definition.defaultValue.toDouble(), actual!!.toDouble(), 0.001)
            } else {
                assertEquals(definition.path, definition.defaultValue, actual)
            }
        }
        assertEquals("72.0", values["Vehicle.Powertrain.TractionBattery.StateOfCharge.Displayed"])
        assertEquals("false", values["Vehicle.Chassis.Axle.Row2.Wheel.Right.Brake.IsFluidLevelLow"])

        store.updateState {
            it.copy(
                cardExtraSignals =
                    it.cardExtraSignals + ("Vehicle.Chassis.Axle.Row2.Wheel.Right.Brake.IsFluidLevelLow" to "true"),
            )
        }
        store.updateState { it.copy(raw = it.raw.copy(tractionBatterySocDisplayed = 45f)) }

        val restored = DebugStore(context).state.value
        assertTrue(restored.cardExtraSignals["Vehicle.Chassis.Axle.Row2.Wheel.Right.Brake.IsFluidLevelLow"] == "true")
        assertEquals(
            "45.0",
            restored.raw.toCardSignalValues()["Vehicle.Powertrain.TractionBattery.StateOfCharge.Displayed"],
        )
    }

    @Test
    fun legacyOdometerKilometersMigrateToVssMeters() {
        context
            .getSharedPreferences("debug_vss_prefs", Context.MODE_PRIVATE)
            .edit()
            .putFloat("Vehicle.TraveledDistance", 120f)
            .commit()

        val store = DebugStore(context)
        assertEquals(120_000f, store.state.value.raw.traveledDistanceMeters, 0.001f)
        store.updateState { it }
        assertEquals(
            120_000f,
            DebugStore(context)
                .state.value.raw.traveledDistanceMeters,
            0.001f,
        )
    }
}
