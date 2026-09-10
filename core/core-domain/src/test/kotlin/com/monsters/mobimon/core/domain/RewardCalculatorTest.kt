package com.monsters.mobimon.core.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class RewardCalculatorTest {
    private val calculator = RewardCalculator()

    @Test
    fun `stage changes at the fixed XP boundaries`() {
        assertEquals(1, calculator.stage(0))
        assertEquals(1, calculator.stage(79))
        assertEquals(2, calculator.stage(80))
        assertEquals(2, calculator.stage(239))
        assertEquals(3, calculator.stage(240))
    }

    @Test
    fun `remaining XP targets the next stage boundary`() {
        assertEquals(80, calculator.xpUntilNextStage(0))
        assertEquals(1, calculator.xpUntilNextStage(79))
        assertEquals(160, calculator.xpUntilNextStage(80))
        assertEquals(1, calculator.xpUntilNextStage(239))
        assertNull(calculator.xpUntilNextStage(240))
    }

    @Test
    fun `negative XP is rejected by both calculations`() {
        assertThrows(IllegalArgumentException::class.java) { calculator.stage(-1) }
        assertThrows(IllegalArgumentException::class.java) { calculator.xpUntilNextStage(-1) }
    }
}
