package com.monsters.mobimon.di.features

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class VehicleCardSelectionPreferencesTest {
    @Test
    fun confirmedSelectionSurvivesStoreRecreation() {
        val preferences =
            ApplicationProvider
                .getApplicationContext<Context>()
                .getSharedPreferences("vehicle-card-test", Context.MODE_PRIVATE)
        preferences.edit().clear().commit()
        val first = VehicleCardSelectionPreferences(preferences)
        first.save(listOf("battery-health", "charging", "tire", "washer", "environment", "assist"))

        assertEquals("battery-health", VehicleCardSelectionPreferences(preferences).selectedCards.value.first())
    }

    @Test
    fun malformedOrUnknownSlotsFallBackToFigmaDefaults() {
        val preferences =
            ApplicationProvider
                .getApplicationContext<Context>()
                .getSharedPreferences("vehicle-card-invalid-test", Context.MODE_PRIVATE)
        preferences.edit().putString("slots", "battery,invalid,tire,washer,environment,assist").commit()

        assertEquals(
            listOf("battery", "charging", "tire", "washer", "environment", "assist"),
            VehicleCardSelectionPreferences(preferences).selectedCards.value,
        )
    }
}
