package com.monsters.mobimon.core.ui

import android.content.Context
import android.graphics.BitmapFactory
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class CompanionBackgroundTest {
    private val resources = ApplicationProvider.getApplicationContext<Context>().resources

    @Test
    fun everyHourSelectsItsBackgroundAtPeriodBoundaries() {
        val expected =
            listOf(
                "night",
                "night",
                "night",
                "night",
                "night",
                "night",
                "morning",
                "morning",
                "morning",
                "morning",
                "morning",
                "morning",
                "day",
                "day",
                "day",
                "day",
                "afternoon",
                "afternoon",
                "sunset",
                "sunset",
                "night",
                "night",
                "night",
                "night",
            )
        expected.forEachIndexed { hour, period ->
            assertEquals("Hour $hour", "pet_home_background_$period", resourceName(hour.toString()))
        }
    }

    @Test
    fun labelsAcceptBothLanguagesAndIgnoreCaseAndWhitespace() {
        mapOf(
            " Morning " to "morning",
            "아침" to "morning",
            "DAY" to "day",
            "낮" to "day",
            "Afternoon" to "afternoon",
            "늦은 오후" to "afternoon",
            "오후" to "afternoon",
            "Sunset" to "sunset",
            "노을" to "sunset",
            "저녁" to "sunset",
            "NIGHT" to "night",
            "밤" to "night",
        ).forEach { (input, period) ->
            assertEquals(input, "pet_home_background_$period", resourceName(input))
        }
    }

    @Test
    fun missingOrInvalidTimeKeepsDeterministicNightFallback() {
        listOf(null, "", " ", "unknown", "-1", "24", "99").forEach {
            assertEquals("pet_home_background_night", resourceName(it))
        }
    }

    @Test
    fun allPeriodsDecodeWithTheMasterCanvasDimensions() {
        listOf("Morning", "Day", "Afternoon", "Sunset", "Night").forEach { period ->
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeResource(resources, companionBackgroundRes(period), options)
            assertEquals("$period width", 2560, options.outWidth)
            assertEquals("$period height", 1440, options.outHeight)
        }
    }

    private fun resourceName(value: String?): String = resources.getResourceEntryName(companionBackgroundRes(value))
}
