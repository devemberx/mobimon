package com.monsters.mobimon.preview

import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.monsters.mobimon.feature.auth.R
import com.monsters.mobimon.feature.auth.preview.CopilotPreviewActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.monsters.mobimon.feature.auth.R as AuthR

@RunWith(AndroidJUnit4::class)
class CopilotPreviewJourneyTest {
    @get:Rule val compose = createEmptyComposeRule()

    @Test
    fun approvalHelpRecreationChatSettingsAndDisconnectAreConnected() {
        ActivityScenario.launch(CopilotPreviewActivity::class.java).use { scenario ->
            waitFor(R.string.copilot_preview_disclosure)
            click(AuthR.string.copilot_connect)
            click(AuthR.string.copilot_qr_help)
            scenario.recreate()
            waitFor(AuthR.string.copilot_address_heading)
            click(AuthR.string.copilot_back_to_qr)
            click(AuthR.string.copilot_approved)
            waitFor(AuthR.string.copilot_connected_heading)
            compose
                .onNodeWithText(
                    text(AuthR.string.copilot_chat, text(AuthR.string.copilot_mobi)),
                ).ensureDisplayed()
                .performClick()
            waitFor(R.string.copilot_preview_chat)
            click(R.string.copilot_preview_return)
            click(AuthR.string.copilot_settings)
            waitFor(R.string.copilot_preview_settings)
            click(AuthR.string.copilot_disconnect)
            click(AuthR.string.copilot_keep)
            waitFor(R.string.copilot_preview_settings)
            click(AuthR.string.copilot_disconnect)
            click(AuthR.string.copilot_disconnect)
            waitFor(AuthR.string.copilot_intro_heading)
            compose.onNodeWithText("@mobimon-driver").assertDoesNotExist()
        }
    }

    @Test
    fun expiryReconnectAccessReviewAndCancellationRemainReachable() {
        ActivityScenario.launch(CopilotPreviewActivity::class.java).use {
            waitFor(R.string.copilot_preview_disclosure)
            click(R.string.copilot_preview_expire)
            waitFor(AuthR.string.copilot_expired_heading)
            compose.onNodeWithText("ABCD · 1234").assertDoesNotExist()
            click(AuthR.string.copilot_new_code)
            compose.onNodeWithContentDescription(text(AuthR.string.copilot_close)).ensureDisplayed().performClick()
            waitFor(AuthR.string.copilot_intro_heading)
            click(R.string.copilot_preview_reconnect)
            waitFor(AuthR.string.copilot_reconnect_heading)
            click(AuthR.string.copilot_reconnect)
            waitFor(AuthR.string.copilot_waiting_heading)
            compose.onNodeWithContentDescription(text(AuthR.string.copilot_back)).ensureDisplayed().performClick()
            waitFor(AuthR.string.copilot_intro_heading)
            click(R.string.copilot_preview_access)
            click(AuthR.string.copilot_review_access)
            waitFor(R.string.copilot_preview_access_notice)
            click(R.string.copilot_preview_close)
            click(AuthR.string.copilot_recheck)
            waitFor(AuthR.string.copilot_connected_heading)
        }
    }

    private fun click(resource: Int) = compose.onNodeWithText(text(resource)).ensureDisplayed().performClick()

    private fun SemanticsNodeInteraction.ensureDisplayed(): SemanticsNodeInteraction {
        if (!isDisplayed()) performScrollTo()
        return assertIsDisplayed()
    }

    private fun text(
        resource: Int,
        vararg args: Any,
    ) = InstrumentationRegistry.getInstrumentation().targetContext.getString(resource, *args)

    private fun waitFor(resource: Int) {
        compose.waitUntil(timeoutMillis = 10_000) {
            compose
                .onAllNodes(
                    androidx.compose.ui.test
                        .hasText(text(resource)),
                ).fetchSemanticsNodes()
                .size ==
                1
        }
    }
}
