package com.monsters.mobimon.service

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import com.monsters.mobimon.core.domain.CompanionSettings
import com.monsters.mobimon.core.domain.SettingsRepository
import com.monsters.mobimon.core.domain.WriteResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class FloatingCompanionServiceTest {
    @Test
    fun overlayLifecycleOwnerInitializesInResumedStateAndDestroysCleanly() {
        val owner = OverlayLifecycleOwner()
        assertEquals(Lifecycle.State.RESUMED, owner.lifecycle.currentState)
        assertNotNull(owner.savedStateRegistry)
        assertNotNull(owner.viewModelStore)

        // Store a test ViewModel to verify cleanup
        class TestViewModel : ViewModel() {
            var cleared = false

            override fun onCleared() {
                cleared = true
            }
        }
        val testVm = TestViewModel()
        owner.viewModelStore.put("key", testVm)

        owner.destroy()
        assertEquals(Lifecycle.State.DESTROYED, owner.lifecycle.currentState)
        assertEquals(true, testVm.cleared)
    }

    @Test
    fun serviceFailsClosedWithoutOverlayPermission() {
        val service = FloatingCompanionService()
        // In Robolectric test without SYSTEM_ALERT_WINDOW granted by default:
        val result = service.onStartCommand(null, 0, 1)
        // Service should return START_NOT_STICKY and stop itself when permission is missing
        assertEquals(android.app.Service.START_NOT_STICKY, result)
    }

    private class FakeSettingsRepository : SettingsRepository {
        val settingsFlow = MutableStateFlow(CompanionSettings(launcherCharacterEnabled = false))
        override val settings: Flow<CompanionSettings> = settingsFlow

        override suspend fun setReducedMotion(enabled: Boolean): WriteResult = WriteResult.Success

        override suspend fun setLauncherCharacterEnabled(enabled: Boolean): WriteResult = WriteResult.Success

        override suspend fun setDebugModeEnabled(enabled: Boolean): WriteResult = WriteResult.Success
    }
}
