package com.monsters.mobimon.service

import org.junit.Assert.assertEquals
import org.junit.Test

class OverlayMovementBoundsTest {
    @Test fun largerBarsReclampAnExistingPosition() {
        val old = overlayMovementBounds(2560, 1440, 0, 76, 0, 96, 140, 140)
        val current = overlayMovementBounds(2560, 1440, 0, 96, 0, 160, 140, 140)
        assertEquals(2420 to 1204, old.clamp(3000, 1300))
        assertEquals(2420 to 1140, current.clamp(2420, 1204))
        assertEquals(0 to 96, current.clamp(-100, -100))
    }

    @Test fun measuredSizeAndSideInsetsBoundBothAxes() {
        val bounds = overlayMovementBounds(2560, 1440, 80, 96, 120, 160, 280, 280)
        assertEquals(80 to 96, bounds.clamp(0, 0))
        assertEquals(2160 to 1000, bounds.clamp(3000, 3000))
    }

    @Test fun tinyWindowsHaveNonInvertedMovementRanges() {
        val bounds = overlayMovementBounds(200, 160, 20, 30, 20, 50, 180, 140)
        assertEquals(20 to 30, bounds.clamp(500, 500))
    }
}
