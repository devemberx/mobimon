package com.monsters.mobimon.ui

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.inspector.WindowInspector
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
import com.monsters.mobimon.core.navigation.CompanionRoute
import com.monsters.mobimon.core.ui.LocalMobiMonMotionEnabled
import com.monsters.mobimon.core.ui.MobiMonTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import com.monsters.mobimon.core.ui.R as CoreUiR

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "ko-rKR-w2560dp-h1268dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class CompanionMenuReviewTest {
    @get:Rule val compose = createComposeRule()

    @Test fun menuReferenceRender() {
        show()
        val panel = compose.onNodeWithTag("companion-menu").fetchSemanticsNode().boundsInRoot
        assertEquals(690f, panel.width, 1f)
        assertEquals(1268f, panel.height, 1f)
        val home = compose.onNodeWithText("홈").fetchSemanticsNode().boundsInRoot
        assertEquals(44f, home.left, 1f)
        assertEquals(400f, home.top, 1f)
        assertEquals(596f, home.width, 1f)
        assertEquals(94f, home.height, 1f)
        val name = compose.onNodeWithText("Mobi").fetchSemanticsNode().boundsInRoot
        assertEquals(244f, name.left, 1f)
        compose.onNodeWithText("v0.1.0").assertIsDisplayed()
        capture("menu")
    }

    @Test
    @Config(qualifiers = "ko-rKR-w800dp-h600dp-mdpi")
    fun compactMenuKeepsFooterAndSettingsReachableAtEnlargedText() {
        show(fontScale = 1.5f)
        capture("menu-compact")
        compose.onNodeWithText("설정").performScrollTo().assertIsDisplayed()
        capture("menu-compact-settings")
        compose.onNodeWithText("v0.1.0").performScrollTo().assertIsDisplayed()
        compose.onNodeWithContentDescription("닫기").performScrollTo().performClick()
        compose.onNodeWithTag("companion-menu").assertDoesNotExist()
    }

    private fun show(fontScale: Float = 1f) {
        val visible = mutableStateOf(true)
        compose.setContent {
            CompositionLocalProvider(
                LocalMobiMonMotionEnabled provides false,
                LocalDensity provides Density(1f, fontScale),
            ) {
                MobiMonTheme {
                    Box(Modifier.fillMaxSize()) {
                        Image(painterResource(CoreUiR.drawable.pet_home_background_night), null, Modifier.fillMaxSize())
                        CompanionMenu(
                            visible.value,
                            CompanionRoute.HOME,
                            { visible.value = false },
                            {},
                            activeFriendId = "friend:mobi",
                        )
                    }
                }
            }
        }
        compose.waitForIdle()
    }

    private fun capture(name: String) {
        compose.runOnIdle {
            val roots = WindowInspector.getGlobalWindowViews()
            val popup = roots.last { it.javaClass.simpleName == "PopupLayout" }
            val bitmap = Bitmap.createBitmap(popup.width, popup.height, Bitmap.Config.ARGB_8888)
            popup.draw(Canvas(bitmap))
            val directory = File("build/reports/menu-ui").apply { mkdirs() }
            File(
                directory,
                "$name.png",
            ).outputStream().use { assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) }
            bitmap.recycle()
        }
    }
}
