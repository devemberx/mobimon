package com.monsters.mobimon.ui

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ReleaseDebugOverlayTest {
    @Test
    fun releaseBuildIncludesDebuggerUiFrame() {
        val methods = Class.forName("com.monsters.mobimon.ui.DebugOverlayKt").declaredMethods

        assertTrue(methods.any { it.name == "DebugOverlayFrame" })
    }

    @Test
    fun releaseBuildHidesDebuggerUiBeforeHiddenUnlockEvenWhenSettingWasEnabled() {
        assertFalse(
            shouldShowDebugOverlay(
                debugModeEnabled = true,
                debugSettingsAvailable = false,
            ),
        )
    }
}
