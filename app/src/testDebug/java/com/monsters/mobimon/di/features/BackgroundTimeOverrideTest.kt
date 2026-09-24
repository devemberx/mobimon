package com.monsters.mobimon.di.features

import com.monsters.mobimon.core.domain.CompanionSettings
import com.monsters.mobimon.core.domain.SettingsRepository
import com.monsters.mobimon.core.domain.WriteResult
import com.monsters.mobimon.debug.DebugInterpretationOverrides
import com.monsters.mobimon.debug.DebugVssProvider
import com.monsters.mobimon.debug.DebugVssState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BackgroundTimeOverrideTest {
    @Test
    fun onlyExplicitDebugTimeWhileDebugEnabledOverridesLocalClock() =
        runTest {
            val settingsState = MutableStateFlow(CompanionSettings())
            val debugState = MutableStateFlow(DebugVssState())
            val settings =
                object : SettingsRepository {
                    override val settings = settingsState

                    override suspend fun setReducedMotion(enabled: Boolean) = WriteResult.Success

                    override suspend fun setLauncherCharacterEnabled(enabled: Boolean) = WriteResult.Success

                    override suspend fun setDebugModeEnabled(enabled: Boolean) = WriteResult.Success
                }
            val debug =
                object : DebugVssProvider {
                    override val state = debugState
                }
            val override = VehicleFeatureModule.backgroundTimeOverride(settings, debug, backgroundScope)

            runCurrent()
            assertEquals(null, override.value)
            debugState.value = DebugVssState(overrides = DebugInterpretationOverrides(timeOfDay = "Sunrise"))
            runCurrent()
            assertEquals(null, override.value)
            settingsState.value = CompanionSettings(debugModeEnabled = true)
            runCurrent()
            assertEquals("Sunrise", override.value)
            debugState.value = DebugVssState()
            runCurrent()
            assertEquals(null, override.value)
            debugState.value = DebugVssState(overrides = DebugInterpretationOverrides(timeOfDay = "Midnight"))
            runCurrent()
            assertEquals("Midnight", override.value)
            settingsState.value = CompanionSettings(debugModeEnabled = false)
            runCurrent()
            assertEquals(null, override.value)
        }
}
