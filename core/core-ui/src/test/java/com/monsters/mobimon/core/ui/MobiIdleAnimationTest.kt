package com.monsters.mobimon.core.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MobiIdleAnimationTest {
    @Test
    fun forwardTimelineUsesEveryCellAndWrapsWithoutExtraEndpoints() {
        val visited = (0 until MobiIdleTimeline.cycleMs).map { MobiIdleTimeline.frameAt(it * 1_000_000) }.distinct()
        assertEquals((0..23).toList(), visited)
        assertTrue(MobiIdleTimeline.cycleMs in 3_500..4_500)
        assertEquals(23, MobiIdleTimeline.frameAt((MobiIdleTimeline.cycleMs - 1) * 1_000_000))
        assertEquals(0, MobiIdleTimeline.frameAt(MobiIdleTimeline.cycleMs * 1_000_000))
    }

    @Test
    fun delayedFramesAndLongPlaybackDoNotAccumulateTimingDrift() {
        val elapsed = 2_731L
        val expected = MobiIdleTimeline.frameAt(elapsed * 1_000_000)
        assertEquals(expected, MobiIdleTimeline.frameAt((MobiIdleTimeline.cycleMs * 100_000 + elapsed) * 1_000_000))
        assertEquals(0, MobiIdleTimeline.frameAt(-1))
    }

    @Test
    fun tiltIsSlowSmoothAndIndependentOfBreathing() {
        val period = MobiIdleTimeline.TILT_PERIOD_MS * 1_000_000
        assertEquals(0f, MobiIdleTimeline.tiltAt(0), 0.0001f)
        assertEquals(2.35f, MobiIdleTimeline.tiltAt(period / 4), 0.0001f)
        assertEquals(-2.35f, MobiIdleTimeline.tiltAt(period * 3 / 4), 0.0001f)
        assertEquals(0f, MobiIdleTimeline.tiltAt(period), 0.0001f)
        assertTrue(MobiIdleTimeline.TILT_PERIOD_MS != MobiIdleTimeline.cycleMs)
    }

    @Test
    fun blendingIsContinuousAtEverySourceBoundaryIncludingWrap() {
        var previousFrame = 0
        for (microseconds in 1..(MobiIdleTimeline.cycleMs * 1_000)) {
            val time = microseconds * 1_000
            val frame = MobiIdleTimeline.frameAt(time)
            if (frame != previousFrame) {
                assertEquals((previousFrame + 1) % 24, frame)
                assertTrue(MobiIdleTimeline.blendAt(time - 1_000) > 0.999f)
                assertTrue(MobiIdleTimeline.blendAt(time) < 0.001f)
            }
            previousFrame = frame
        }
    }

    @Test
    fun breathAndSettleStayControlledAndMoveIndependently() {
        for (millis in 0L..26_400L step 16) {
            val time = millis * 1_000_000
            assertTrue(MobiIdleTimeline.scaleXAt(time) in 1f..1.0171f)
            assertTrue(MobiIdleTimeline.scaleYAt(time) in 0.9959f..1.0241f)
            assertTrue(MobiIdleTimeline.liftFractionAt(time) in -0.01071f..0f)
        }
        assertEquals(1f, MobiIdleTimeline.breathAt(MobiIdleTimeline.cycleMs * 500_000), 0.0001f)
        assertTrue(MobiIdleTimeline.BOB_PERIOD_MS != MobiIdleTimeline.TILT_PERIOD_MS)
    }
}
