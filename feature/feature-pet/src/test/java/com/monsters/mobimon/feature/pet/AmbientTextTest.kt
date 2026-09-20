package com.monsters.mobimon.feature.pet

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.monsters.mobimon.core.ui.LocalMobiMonMotionEnabled
import com.monsters.mobimon.core.ui.MobiMonTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AmbientTextTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun periodPhrasesContainsRequiredQuotes() {
        assertEquals(5, morningPhrases.size)
        assertEquals(5, dayPhrases.size)
        assertEquals(5, afternoonPhrases.size)
        assertEquals(5, sunsetPhrases.size)
        assertEquals(5, nightPhrases.size)

        assertTrue(morningPhrases.contains("오늘의 첫 길, 함께할게요."))
        assertTrue(dayPhrases.contains("오늘도 나란히 달려볼까요?"))
        assertTrue(afternoonPhrases.contains("잠깐, 창밖을 봐도 좋아요."))
        assertTrue(sunsetPhrases.contains("오늘도 수고 많았어요."))
        assertTrue(nightPhrases.contains("이 밤도, 천천히 가요."))
    }

    @Test
    fun consecutivePhraseRepetitionIsAvoided() {
        val lastPhrase = morningPhrases.first()
        repeat(50) {
            val next = getNextAmbientPhrase("morning", lastPhrase)
            assertNotEquals(lastPhrase, next)
            assertTrue(morningPhrases.contains(next))
        }
    }

    @Test
    fun ambientTextHeaderDisplaysPhraseForPeriod() {
        val timeState = mutableStateOf("Morning")
        compose.setContent {
            CompositionLocalProvider(LocalMobiMonMotionEnabled provides false) {
                MobiMonTheme {
                    AmbientTextHeader(backgroundTimeOfDay = timeState.value)
                }
            }
        }

        compose.onNodeWithTag("home-ambient-text-container").assertIsDisplayed()
        compose.onNodeWithTag("home-ambient-text").assertIsDisplayed()

        val morningText =
            compose
                .onNodeWithTag("home-ambient-text")
                .fetchSemanticsNode()
                .config
                .getOrNull(
                    SemanticsProperties.Text,
                )?.firstOrNull()
                ?.text
        assertTrue("Morning phrase expected, got: $morningText", morningPhrases.contains(morningText))

        compose.runOnIdle { timeState.value = "Night" }

        val nightText =
            compose
                .onNodeWithTag("home-ambient-text")
                .fetchSemanticsNode()
                .config
                .getOrNull(
                    SemanticsProperties.Text,
                )?.firstOrNull()
                ?.text
        assertTrue("Night phrase expected, got: $nightText", nightPhrases.contains(nightText))
    }
}
