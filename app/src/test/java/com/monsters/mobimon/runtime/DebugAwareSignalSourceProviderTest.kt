package com.monsters.mobimon.runtime

import com.monsters.mobimon.core.domain.CompanionSettings
import com.monsters.mobimon.core.domain.SettingsRepository
import com.monsters.mobimon.core.domain.SignalSource
import com.monsters.mobimon.core.domain.WriteResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DebugAwareSignalSourceProviderTest {
    @Test
    fun debugModeSwitchesReleaseExpectedSourceToSimulated() =
        runTest {
            val settings = FakeSettingsRepository()
            val provider = DebugAwareSignalSourceProvider(SignalSource.REAL, settings, backgroundScope)

            runCurrent()
            assertEquals(SignalSource.REAL, provider.source())

            settings.mutableSettings.value = CompanionSettings(debugModeEnabled = true)
            runCurrent()

            assertEquals(SignalSource.SIMULATED, provider.source())
        }

    private class FakeSettingsRepository : SettingsRepository {
        val mutableSettings = MutableStateFlow(CompanionSettings())
        override val settings: Flow<CompanionSettings> = mutableSettings

        override suspend fun setReducedMotion(enabled: Boolean): WriteResult = WriteResult.Success

        override suspend fun setLauncherCharacterEnabled(enabled: Boolean): WriteResult = WriteResult.Success

        override suspend fun setDebugModeEnabled(enabled: Boolean): WriteResult = WriteResult.Success
    }
}
