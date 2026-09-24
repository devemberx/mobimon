package com.monsters.mobimon

import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.isEnabled
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.monsters.mobimon.core.domain.DrivingState
import com.monsters.mobimon.core.domain.SignalQuality
import com.monsters.mobimon.testing.JourneyAuthentication
import com.monsters.mobimon.testing.JourneyStorage
import com.monsters.mobimon.testing.JourneyVehicle
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject
import com.monsters.mobimon.feature.auth.R as AuthR
import com.monsters.mobimon.feature.pet.R as PetR

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class CopilotConnectionJourneyTest {
    @get:Rule(order = 0)
    val hilt = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val compose = createEmptyComposeRule()

    @Inject lateinit var vehicle: JourneyVehicle

    @Inject lateinit var storage: JourneyStorage

    @Inject lateinit var authentication: JourneyAuthentication

    @Before
    fun setUp() = hilt.inject()

    @After
    fun tearDown() {
        if (::storage.isInitialized) storage.close()
    }

    @Test
    fun settingsConnectionSurvivesRecreationAndBackReturnsToSettings() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            waitFor(hasContentDescription(text(PetR.string.pet_open_menu)))
            compose.onNodeWithContentDescription(text(PetR.string.pet_open_menu)).ensureDisplayed().performClick()
            compose.onNodeWithText(text(R.string.drawer_settings)).ensureDisplayed().performClick()
            waitFor(hasText(text(PetR.string.pet_settings_ai_title)) and isEnabled())
            compose.onNodeWithText(text(PetR.string.pet_settings_ai_title)).ensureDisplayed().performClick()
            waitFor(hasText(text(AuthR.string.copilot_connect)))
            compose.onNodeWithText(text(AuthR.string.copilot_connect)).ensureDisplayed().assertIsNotEnabled()
            compose.onNodeWithText("아직 계정 연결을 이용할 수 없어요.", substring = true).assertExists()
            scenario.recreate()
            waitFor(hasText("아직 계정 연결을 이용할 수 없어요.", substring = true))
            compose.onNodeWithText(text(AuthR.string.copilot_connected_heading)).assertDoesNotExist()
            compose.onNodeWithContentDescription(text(AuthR.string.copilot_back)).ensureDisplayed().performClick()
            compose.onNodeWithTag("settings-done").assertExists()
        }
    }

    @Test
    fun lossOfParkingDisablesChatAndBackReturnsHome() {
        authentication.approve()
        ActivityScenario.launch(MainActivity::class.java).use {
            waitFor(hasText(text(PetR.string.pet_talk_action)) and isEnabled())
            compose.onNodeWithText(text(PetR.string.pet_talk_action)).ensureDisplayed().performClick()
            waitFor(hasTestTag("chat-input"))
            compose.onNodeWithTag("chat-input").assertIsEnabled()
            vehicle.publish(DrivingState.UNKNOWN, SignalQuality.UNAVAILABLE)
            waitFor(hasTestTag("chat-input") and !isEnabled())
            compose.onNodeWithTag("chat-input").assertIsNotEnabled()
            compose.onNodeWithContentDescription(text(AuthR.string.copilot_back)).ensureDisplayed().performClick()
            waitFor(hasContentDescription(text(PetR.string.pet_open_menu)))
        }
    }

    @Test
    fun conversationSendsDirectlyAndKeepsRepliesAcrossRecreationUntilNewConversation() {
        authentication.approve()
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            waitFor(hasText(text(PetR.string.pet_talk_action)) and isEnabled())
            compose.onNodeWithText(text(PetR.string.pet_talk_action)).ensureDisplayed().performClick()
            waitFor(hasTestTag("chat-input"))
            compose.onNodeWithTag("chat-input").performTextInput("오늘도 반가워")
            waitFor(hasTestTag("chat-send") and isEnabled())
            compose.onNodeWithTag("chat-send").performClick()
            waitFor(hasText("이야기를 들려줘서 고마워요."))
            compose.onNodeWithText("오늘도 반가워").assertExists()
            scenario.recreate()
            waitFor(hasText("이야기를 들려줘서 고마워요."))
            var keyboardVisible = false
            scenario.onActivity { activity ->
                keyboardVisible = ViewCompat
                    .getRootWindowInsets(activity.window.decorView)
                    ?.isVisible(WindowInsetsCompat.Type.ime()) == true
            }
            if (keyboardVisible) {
                compose.onNodeWithContentDescription(text(AuthR.string.copilot_back)).ensureDisplayed().performClick()
            }
            waitFor(hasText(text(AuthR.string.chat_new)))
            compose.onNodeWithText(text(AuthR.string.chat_new)).ensureDisplayed().performClick()
            compose.onNodeWithText("이야기를 들려줘서 고마워요.").assertDoesNotExist()
            compose.onNodeWithTag("chat-send").assertIsNotEnabled()
        }
    }

    @Test
    fun signedOutHomeAndMenuChatOpenConnectionDirectly() {
        ActivityScenario.launch(MainActivity::class.java).use {
            waitFor(hasText(text(PetR.string.pet_talk_action)) and isEnabled())
            compose.onNodeWithText(text(PetR.string.pet_talk_action)).ensureDisplayed().performClick()
            waitFor(hasText(text(AuthR.string.copilot_connect)))
            compose.onNodeWithTag("chat-input").assertDoesNotExist()
            compose.onNodeWithContentDescription(text(AuthR.string.copilot_back)).ensureDisplayed().performClick()
            waitFor(hasContentDescription(text(PetR.string.pet_open_menu)))
            compose.onNodeWithContentDescription(text(PetR.string.pet_open_menu)).performClick()
            compose
                .onNode(hasText(text(R.string.drawer_menu_chat)) and hasAnyAncestor(hasTestTag("companion-menu")))
                .ensureDisplayed()
                .performClick()
            waitFor(hasText(text(AuthR.string.copilot_connect)))
            compose.onNodeWithTag("chat-input").assertDoesNotExist()
        }
    }

    @Test
    fun authenticationSuccessOffersConversationBeforeSettings() {
        ActivityScenario.launch(MainActivity::class.java).use {
            waitFor(hasText(text(PetR.string.pet_talk_action)) and isEnabled())
            compose.onNodeWithText(text(PetR.string.pet_talk_action)).ensureDisplayed().performClick()
            waitFor(hasText(text(AuthR.string.copilot_connect)))
            authentication.approve()
            val chat =
                InstrumentationRegistry.getInstrumentation().targetContext.getString(
                    AuthR.string.copilot_chat,
                    "모비",
                )
            waitFor(hasText(chat) and isEnabled())
            compose.onNodeWithText(text(AuthR.string.copilot_settings)).ensureDisplayed()
            compose.onNodeWithText(chat).ensureDisplayed().performClick()
            waitFor(hasTestTag("chat-input"))
            compose.onNodeWithTag("chat-send").assertIsNotEnabled()
        }
    }

    @Test
    fun authenticatedMenuChatOpensComposer() {
        authentication.approve()
        ActivityScenario.launch(MainActivity::class.java).use {
            waitFor(hasContentDescription(text(PetR.string.pet_open_menu)))
            compose.onNodeWithContentDescription(text(PetR.string.pet_open_menu)).performClick()
            compose
                .onNode(hasText(text(R.string.drawer_menu_chat)) and hasAnyAncestor(hasTestTag("companion-menu")))
                .ensureDisplayed()
                .performClick()
            waitFor(hasTestTag("chat-input"))
            compose.onNodeWithTag("chat-input").assertIsEnabled()
            compose.onNodeWithTag("chat-send").assertIsNotEnabled()
        }
    }

    private fun SemanticsNodeInteraction.ensureDisplayed(): SemanticsNodeInteraction {
        if (!isDisplayed()) performScrollTo()
        return assertIsDisplayed()
    }

    private fun text(resource: Int) = InstrumentationRegistry.getInstrumentation().targetContext.getString(resource)

    private fun waitFor(matcher: SemanticsMatcher) {
        compose.waitUntil(timeoutMillis = 10_000) { compose.onAllNodes(matcher).fetchSemanticsNodes().size == 1 }
    }
}
