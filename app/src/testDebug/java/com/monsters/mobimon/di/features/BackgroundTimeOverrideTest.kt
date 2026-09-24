package com.monsters.mobimon.di.features

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.monsters.mobimon.core.domain.Clock
import com.monsters.mobimon.core.domain.CompanionSettings
import com.monsters.mobimon.core.domain.IdGenerator
import com.monsters.mobimon.core.domain.SettingsRepository
import com.monsters.mobimon.core.domain.WeatherCondition
import com.monsters.mobimon.core.domain.WriteResult
import com.monsters.mobimon.core.vss.DefaultParkedVssRawVehicleSource
import com.monsters.mobimon.debug.DebugStore
import com.monsters.mobimon.debug.deriveWeatherCondition
import com.monsters.mobimon.vehicle.DemoVehicleRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BackgroundTimeOverrideTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun clearPreferences() {
        context
            .getSharedPreferences("debug_vss_prefs", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
    }

    @After
    fun clearPreferencesAfterTest() {
        clearPreferences()
    }

    @Test
    fun vssTimeOverrideDoesNotReplaceDecorativeLocalClock() =
        runTest {
            val settings = TestSettings()
            val debug = DebugStore(context)
            val override = VehicleFeatureModule.backgroundTimeOverride(settings, debug, backgroundScope)

            runCurrent()
            assertEquals(null, override.value)
            debug.updateState { it.copy(overrides = it.overrides.copy(timeOfDay = "Sunrise")) }
            runCurrent()
            assertEquals(null, override.value)
            debug.setBackgroundTimeOverride("Midnight")
            runCurrent()
            assertEquals(null, override.value)
            settings.values.value = CompanionSettings(debugModeEnabled = true)
            runCurrent()
            assertEquals("Midnight", override.value)
            debug.updateState { it.copy(overrides = it.overrides.copy(timeOfDay = "Night")) }
            runCurrent()
            assertEquals("Midnight", override.value)
            debug.setBackgroundTimeOverride(null)
            runCurrent()
            assertEquals(null, override.value)
            assertEquals(null, DebugStore(context).backgroundTimeOverride.value)
        }

    @Test
    fun previewSelectionDoesNotPublishVehicleEvidenceOrChangeQuestWeather() =
        runTest {
            val settings = TestSettings().apply { values.value = CompanionSettings(debugModeEnabled = true) }
            val debug = DebugStore(context)
            val override = VehicleFeatureModule.backgroundTimeOverride(settings, debug, backgroundScope)
            val vehicle =
                DemoVehicleRepository(
                    Clock { testScheduler.currentTime },
                    IdGenerator { "epoch" },
                    settings,
                    debug,
                    DefaultParkedVssRawVehicleSource(),
                    backgroundScope,
                )
            vehicle.start()
            runCurrent()
            val before = vehicle.snapshots.value
            assertEquals("Morning", before.timeOfDay)
            assertEquals(WeatherCondition.CLEAR, debug.state.value.deriveWeatherCondition())

            debug.setBackgroundTimeOverride("01:00")
            runCurrent()
            assertEquals("01:00", override.value)
            assertEquals(before, vehicle.snapshots.value)
            assertEquals(WeatherCondition.CLEAR, debug.state.value.deriveWeatherCondition())
            assertEquals("01:00", DebugStore(context).backgroundTimeOverride.value)

            debug.updateState { it.copy(overrides = it.overrides.copy(timeOfDay = "NIGHT")) }
            runCurrent()
            assertEquals("Night", vehicle.snapshots.value.timeOfDay)
            assertEquals(WeatherCondition.CLOUDY_OR_NIGHT, debug.state.value.deriveWeatherCondition())
            assertEquals("01:00", override.value)
            vehicle.stop()
        }

    private class TestSettings : SettingsRepository {
        val values = MutableStateFlow(CompanionSettings())
        override val settings = values

        override suspend fun setReducedMotion(enabled: Boolean) = WriteResult.Success

        override suspend fun setLauncherCharacterEnabled(enabled: Boolean) = WriteResult.Success

        override suspend fun setDebugModeEnabled(enabled: Boolean) = WriteResult.Success
    }
}
