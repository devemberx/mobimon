package com.monsters.mobimon.core.ui

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MobiWarningAnimationTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun torsoBreathCannotMoveGroundWheelFaceOrSprout() {
        for (x in 0..408) {
            assertEquals(0f, MobiCollapsedGeometry.torsoOffset(x.toFloat(), 302f, 1f), 0f)
        }
        for (y in 0..408) {
            for (x in 150..408) {
                assertEquals(0f, MobiCollapsedGeometry.torsoOffset(x.toFloat(), y.toFloat(), 1f), 0f)
            }
        }
        assertEquals(-1.4f, MobiCollapsedGeometry.torsoOffset(114f, 258f, 1f), 0f)
        assertEquals(0f, MobiCollapsedGeometry.torsoOffset(114f, 258f, 0f), 0f)
    }

    @Test
    fun tiredBlinkOpensBrieflyEverySixAndAHalfSeconds() {
        assertEquals(0f, MobiCollapsedTimeline.eyeOpenAt(5_400_000_000), 0f)
        assertEquals(0.5f, MobiCollapsedTimeline.eyeOpenAt(5_700_000_000), 0.001f)
        assertEquals(1f, MobiCollapsedTimeline.eyeOpenAt(6_000_000_000), 0f)
        assertEquals(0.5f, MobiCollapsedTimeline.eyeOpenAt(6_300_000_000), 0.001f)
        assertEquals(0f, MobiCollapsedTimeline.eyeOpenAt(6_500_000_000), 0f)
        assertEquals(1f, MobiCollapsedTimeline.eyeOpenAt(12_500_000_000), 0f)
    }

    @Test
    fun breathingClosesSmoothlyOverFourSeconds() {
        val period = MobiCollapsedTimeline.PERIOD_NANOS
        assertEquals(0f, MobiCollapsedTimeline.breathAt(0), 0f)
        assertEquals(1f, MobiCollapsedTimeline.breathAt(period / 2), 0f)
        assertEquals(0f, MobiCollapsedTimeline.breathAt(period), 0f)
        assertEquals(
            MobiCollapsedTimeline.breathAt(period - 16_000_000),
            MobiCollapsedTimeline.breathAt(period + 16_000_000),
            0.0001f,
        )
    }

    @Test
    fun crossfadeTakesTwoHundredMillisecondsAndReversesCurrentOpacity() {
        val warning = mutableStateOf(false)
        val blend = MobiWarningBlend()
        compose.mainClock.autoAdvance = false
        compose.setContent { LaunchedEffect(warning.value) { blend.target(warning.value, true) } }

        fun change(value: Boolean) {
            compose.runOnIdle {
                warning.value = value
                Snapshot.sendApplyNotifications()
            }
            compose.mainClock.advanceTimeByFrame()
        }
        change(true)
        compose.mainClock.advanceTimeBy(100)
        val halfway = blend.opacity.value
        assertTrue(halfway in 0.35f..0.65f)
        change(false)
        assertEquals(halfway, blend.opacity.value, 0.1f)
        compose.mainClock.advanceTimeBy(240)
        assertEquals(0f, blend.opacity.value)
        change(true)
        compose.mainClock.advanceTimeBy(240)
        assertEquals(1f, blend.opacity.value)
        compose.mainClock.advanceTimeBy(9000)
        assertEquals(1f, blend.opacity.value)
        change(false)
        compose.mainClock.advanceTimeBy(240)
        assertEquals(0f, blend.opacity.value)
    }

    @Test
    fun reducedMotionSelectsEndpointsWithoutFading() {
        val warning = mutableStateOf(true)
        val blend = MobiWarningBlend()
        compose.setContent { LaunchedEffect(warning.value) { blend.target(warning.value, false) } }
        compose.waitForIdle()
        assertEquals(1f, blend.opacity.value)
        compose.runOnIdle { warning.value = false }
        compose.waitForIdle()
        assertEquals(0f, blend.opacity.value)
    }
}
