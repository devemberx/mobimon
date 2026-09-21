package com.monsters.mobimon.preview

import android.content.Intent
import android.graphics.Bitmap
import android.provider.Settings
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.monsters.mobimon.feature.auth.preview.ConversationPreviewActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class ConversationKeyboardDeviceTest {
    @get:Rule val compose = createEmptyComposeRule()

    @Test
    fun realImeResizesPanelsAndBackRetainsDraft() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val setting = "show_ime_with_hard_keyboard"
        val previous = Settings.Secure.getString(context.contentResolver, setting)?.toIntOrNull()

        fun shell(command: String) {
            android.os.ParcelFileDescriptor
                .AutoCloseInputStream(
                    instrumentation.uiAutomation.executeShellCommand(command),
                ).use { it.readBytes() }
        }
        shell("settings put secure $setting 1")
        try {
            val intent = Intent(context, ConversationPreviewActivity::class.java).putExtra("state", "keyboard")
            ActivityScenario.launch<ConversationPreviewActivity>(intent).use { scenario ->
                compose.onNodeWithTag("chat-input").assertIsDisplayed()
                val full = compose.onNodeWithTag("chat-panel").fetchSemanticsNode().boundsInRoot
                compose.onNodeWithTag("chat-input").performClick()

                fun waitForIme(visible: Boolean) {
                    compose.waitUntil(15_000) {
                        var shown = false
                        scenario.onActivity {
                            shown =
                                WindowInsetsCompat
                                    .toWindowInsetsCompat(it.window.decorView.rootWindowInsets)
                                    .isVisible(WindowInsetsCompat.Type.ime())
                        }
                        shown == visible
                    }
                }
                waitForIme(true)
                compose.waitForIdle()
                val resized = compose.onNodeWithTag("chat-panel").fetchSemanticsNode().boundsInRoot
                val composer = compose.onNodeWithTag("chat-composer").fetchSemanticsNode().boundsInRoot
                assertEquals(full.left, resized.left, 1f)
                assertEquals(full.top, resized.top, 1f)
                assertEquals(full.width, resized.width, 1f)
                assertTrue(resized.height < full.height)
                assertTrue(composer.bottom <= resized.bottom)
                val output = File(context.filesDir, "test-screenshots").apply { mkdirs() }
                instrumentation.uiAutomation.waitForIdle(500, 5_000)
                val screenshot = checkNotNull(instrumentation.uiAutomation.takeScreenshot())
                File(output, "conversation-native-keyboard.png").outputStream().use {
                    assertTrue(screenshot.compress(Bitmap.CompressFormat.PNG, 100, it))
                }
                screenshot.recycle()
                compose.onNodeWithContentDescription("뒤로").performClick()
                waitForIme(false)
                compose.onNodeWithText("오늘 하루가 조금 힘들었어").assertIsDisplayed()
                compose.onNodeWithContentDescription("뒤로").performClick()
                compose.waitUntil(5_000) { scenario.state == Lifecycle.State.DESTROYED }
            }
        } finally {
            shell(if (previous == null) "settings delete secure $setting" else "settings put secure $setting $previous")
        }
    }
}
