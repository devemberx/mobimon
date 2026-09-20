package com.monsters.mobimon.feature.pet

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
import com.monsters.mobimon.core.domain.CompanionSettings
import com.monsters.mobimon.core.domain.DrivingState
import com.monsters.mobimon.core.domain.PetProfile
import com.monsters.mobimon.core.domain.SignalQuality
import com.monsters.mobimon.core.domain.SignalSource
import com.monsters.mobimon.core.domain.VehicleSnapshot
import com.monsters.mobimon.core.ui.LocalMobiMonMotionEnabled
import com.monsters.mobimon.core.ui.MobiMonTheme
import com.monsters.mobimon.core.ui.companionBackgroundRes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import kotlin.math.abs
import kotlin.math.roundToInt

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "ko-rKR-w2560dp-h1332dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class CompanionReviewTest {
    @get:Rule val compose = createComposeRule()

    @Test fun homeReferenceRender() {
        homeRender("Night")
        val avatar = compose.onNodeWithContentDescription("Mobi 강아지").fetchSemanticsNode().boundsInRoot
        assertEquals(980f, avatar.left, 1f)
        assertEquals(392f, avatar.top, 1f)
        assertEquals(600f, avatar.width, 1f)
        assertEquals(avatar.width, avatar.height, 1f)
        val menu = compose.onNodeWithContentDescription("메뉴 열기").fetchSemanticsNode().boundsInRoot
        assertEquals(72f, menu.left, 1f)
        assertEquals(56f, menu.top, 1f)
        assertEquals(104f, menu.width, 1f)
        val bubble = compose.onNodeWithTag("home-companion-message").fetchSemanticsNode().boundsInRoot
        val phrase = compose.onNodeWithTag("home-ambient-text").fetchSemanticsNode().boundsInRoot
        assertEquals(1280f, (phrase.left + phrase.right) / 2f, 1f)
        assertEquals(292f, phrase.top, 1f)
        assertEquals(1576f, bubble.left, 1f)
        assertEquals(520f, bubble.top, 1f)
        assertEquals(324f, bubble.width, 1f)
        assertEquals(174.6f, bubble.height, 1f)
        val action = compose.onNodeWithTag("home-conversation-action").fetchSemanticsNode().boundsInRoot
        assertEquals(1013.6f, action.left, 1f)
        assertEquals(1068f, action.top, 1f)
        assertEquals(532.8f, action.width, 1f)
        assertEquals(100.8f, action.height, 1f)
    }

    @Test fun morningHomeReferenceRender() = homeRender("Morning")

    @Test fun dayHomeReferenceRender() = homeRender("Day")

    @Test fun afternoonHomeReferenceRender() = homeRender("Afternoon")

    @Test fun sunsetHomeReferenceRender() = homeRender("Sunset")

    @Test
    @Config(qualifiers = "ko-rKR-w800dp-h600dp-mdpi")
    fun compactHomeUpdatesAllBackgroundsWithoutRecreatingContent() {
        val time = mutableStateOf("Morning")
        lateinit var view: View
        compose.setContent {
            val current = LocalView.current
            SideEffect { view = current }
            CompositionLocalProvider(
                LocalMobiMonMotionEnabled provides false,
                LocalDensity provides Density(1f, 1.5f),
            ) {
                MobiMonTheme { ReviewHome(time.value) }
            }
        }
        val skyColors = mutableSetOf<Int>()
        listOf("Morning", "Day", "Afternoon", "Sunset", "Night").forEach { period ->
            compose.runOnIdle { time.value = period }
            assertLightlyTintedCrop(view, period)
            compose.runOnIdle {
                val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
                view.draw(Canvas(bitmap))
                skyColors += bitmap.getPixel(12, 12)
                val directory = File("build/reports/companion-ui").apply { mkdirs() }
                File(directory, "home-compact-${period.lowercase()}.png").outputStream().use {
                    assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it))
                }
                bitmap.recycle()
            }
        }
        assertEquals("Each period must render its own background", 5, skyColors.size)
        compose.onNodeWithTag("home-companion-message").performScrollTo().assertIsDisplayed()
        capture(view, "home-compact-bubble")
        compose.onNodeWithTag("home-conversation-action").performScrollTo().assertIsDisplayed()
        capture(view, "home-compact-action")
    }

    private fun homeRender(period: String) {
        val view = render(if (period == "Night") "home" else "home-${period.lowercase()}") { ReviewHome(period) }
        assertLightlyTintedCrop(view, period)
    }

    @Composable
    private fun ReviewHome(period: String) {
        PetHomeScreen(
            profile = PetProfile("review"),
            snapshot =
                VehicleSnapshot(
                    "review",
                    "epoch",
                    1,
                    1000,
                    SignalSource.SIMULATED,
                    DrivingState.PARKED,
                    SignalQuality.VALID,
                    72,
                    timeOfDay = "Night",
                ),
            onOpenMenu = {},
            onPetClick = {},
            pointBalance = 1200,
            interactionAllowed = true,
            connectionAvailable = true,
            backgroundTimeOfDay = period,
        )
    }

    @Test fun settingsReferenceRender() {
        render("settings") {
            SettingsScreen(CompanionSettings(reducedMotion = true), {}, parkedVerified = true, onOpenCopilot = {})
        }
        compose.onNodeWithTag("settings-done").assertIsDisplayed()
    }

    @Test
    @Config(qualifiers = "ko-rKR-w800dp-h600dp-mdpi")
    fun compactSettingsKeepsDoneReachableAtEnlargedText() {
        render("settings-compact") {
            CompositionLocalProvider(LocalDensity provides Density(1f, 1.5f)) {
                SettingsScreen(CompanionSettings(reducedMotion = true), {}, parkedVerified = true, onOpenCopilot = {})
            }
        }
        compose.onNodeWithTag("settings-done").assertIsDisplayed()
    }

    private fun render(
        name: String,
        content: @Composable () -> Unit,
    ): View {
        lateinit var view: View
        compose.setContent {
            val current = LocalView.current
            SideEffect { view = current }
            CompositionLocalProvider(LocalMobiMonMotionEnabled provides false) { MobiMonTheme(content = content) }
        }
        capture(view, name)
        return view
    }

    private fun assertLightlyTintedCrop(
        view: View,
        period: String,
    ) {
        compose.runOnIdle {
            val source = BitmapFactory.decodeResource(view.resources, companionBackgroundRes(period))
            val expected = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            val actual = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            val scale = maxOf(view.width.toFloat() / source.width, view.height.toFloat() / source.height)
            val width = (source.width * scale).roundToInt()
            val height = (source.height * scale).roundToInt()
            val left = ((view.width - width) / 2f).roundToInt().toFloat()
            val top = ((view.height - height) / 2f).roundToInt().toFloat()
            Canvas(expected).drawBitmap(
                source,
                null,
                RectF(left, top, left + width, top + height),
                Paint(Paint.FILTER_BITMAP_FLAG),
            )
            view.draw(Canvas(actual))
            val tint = Color.rgb(9, 21, 37)
            var visibleTint = false
            // Bound the effect against the cropped source, without coupling to exact gradient stops.
            for (x in listOf(12, view.width - 12)) {
                for (y in listOf(12, view.height / 3, view.height * 2 / 3, view.height - 12)) {
                    val wanted = expected.getPixel(x, y)
                    val rendered = actual.getPixel(x, y)
                    listOf(Color::red, Color::green, Color::blue).forEach { channel ->
                        val original = channel(wanted).toFloat()
                        val strongestTint = original + (channel(tint) - original) * 0.13f
                        assertTrue(
                            "$period keeps its crop and light tint at ($x, $y)",
                            channel(rendered).toFloat() in
                                (minOf(original, strongestTint) - 3f)..(maxOf(original, strongestTint) + 3f),
                        )
                        visibleTint = visibleTint || abs(channel(rendered) - original) > 3f
                    }
                }
            }
            if (period == "Day") assertTrue("Daylight receives a visible but subtle window tint", visibleTint)
            source.recycle()
            expected.recycle()
            actual.recycle()
        }
    }

    private fun capture(
        view: View,
        name: String,
    ) {
        compose.runOnIdle {
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            view.draw(Canvas(bitmap))
            val directory = File("build/reports/companion-ui").apply { mkdirs() }
            File(directory, "$name.png").outputStream().use {
                assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it))
            }
            bitmap.recycle()
        }
    }
}
