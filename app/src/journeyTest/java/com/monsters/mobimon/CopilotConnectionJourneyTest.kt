package com.monsters.mobimon

import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.isEnabled
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.monsters.mobimon.core.domain.DrivingState
import com.monsters.mobimon.core.domain.SignalQuality
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
    fun lossOfParkingDisablesConnectionAndLaterStillReturnsHome() {
        ActivityScenario.launch(MainActivity::class.java).use {
            waitFor(hasText(text(PetR.string.pet_talk_action)) and isEnabled())
            compose.onNodeWithText(text(PetR.string.pet_talk_action)).ensureDisplayed().performClick()
            waitFor(hasText(text(AuthR.string.copilot_connect)) and !isEnabled())
            vehicle.publish(DrivingState.UNKNOWN, SignalQuality.UNAVAILABLE)
            waitFor(hasText(text(AuthR.string.copilot_connect)) and !isEnabled())
            compose.onNodeWithText(text(AuthR.string.copilot_connect)).ensureDisplayed().assertIsNotEnabled()
            compose.onNodeWithText(text(AuthR.string.copilot_later)).ensureDisplayed().performClick()
            waitFor(hasContentDescription(text(PetR.string.pet_open_menu)))
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
