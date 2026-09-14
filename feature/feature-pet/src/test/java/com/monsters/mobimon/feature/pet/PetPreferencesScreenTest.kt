package com.monsters.mobimon.feature.pet

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.monsters.mobimon.core.domain.CompanionSettings
import com.monsters.mobimon.core.ui.MobiMonTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "ko-rKR")
@OptIn(androidx.compose.ui.test.ExperimentalTestApi::class)
class PetPreferencesScreenTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    @Config(qualifiers = "ko-rKR-w1792dp-h888dp")
    fun headUnitKeepsSharedRowsReachableAndDoneVisible() {
        compose.setContent {
            MobiMonTheme { SettingsScreen(CompanionSettings(), {}, {}, parkedVerified = true) }
        }
        compose.onNodeWithText("GitHub Copilot").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("방해 금지").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("settings-done").assertIsDisplayed().assertHeightIsAtLeast(76.dp)
        compose.onNodeWithTag("settings-back").assertHeightIsAtLeast(76.dp)
        compose.onNodeWithText("차량 홈 캐릭터").performScrollTo().assertHeightIsAtLeast(76.dp)
    }

    @Test
    fun visibilityToggleRequestsChangeButKeepsCommittedSetting() {
        var requested: Boolean? = null
        compose.setContent {
            MobiMonTheme {
                SettingsScreen(
                    settings = CompanionSettings(showOnVehicleHome = true),
                    parkedVerified = true,
                    onShowOnVehicleHomeChange = { requested = it },
                    onReducedMotionChange = {},
                )
            }
        }

        compose.onNodeWithText("차량 홈 캐릭터").performScrollTo().performClick()

        assertEquals(false, requested)
        compose.onNodeWithText("차량 홈 캐릭터").assertIsOn()
    }

    @Test
    fun unavailableServicesAndUnknownParkingDoNotInvokeSettingsActions() {
        var visibilityCalls = 0
        var motionCalls = 0
        compose.setContent {
            MobiMonTheme {
                SettingsScreen(
                    settings = CompanionSettings(),
                    onShowOnVehicleHomeChange = { visibilityCalls++ },
                    onReducedMotionChange = { motionCalls++ },
                    parkedVerified = false,
                )
            }
        }

        compose.onNodeWithText("주차 확인 불가").assertExists()
        compose.onNodeWithText("GitHub Copilot").assertIsNotEnabled()
        compose.onNodeWithText("차량 홈 캐릭터").performScrollTo().performClick()
        compose.onNodeWithText("움직임 줄이기").performScrollTo().performClick()
        assertEquals(0, visibilityCalls)
        assertEquals(0, motionCalls)
    }

    @Test
    fun failedLoadOffersRetryAndNavigationRemainsAvailable() {
        var retries = 0
        var backs = 0
        compose.setContent {
            MobiMonTheme {
                SettingsScreen(
                    settings = CompanionSettings(),
                    onShowOnVehicleHomeChange = {},
                    onReducedMotionChange = {},
                    settingsAvailable = false,
                    settingsLoadFailed = true,
                    onRetry = { retries++ },
                    onBack = { backs++ },
                )
            }
        }

        compose.onNodeWithText("저장된 설정을 불러오지 못했어요.").assertExists()
        compose
            .onNodeWithText("다시 시도")
            .performScrollTo()
            .assertIsDisplayed()
            .performClick()
        compose.onNodeWithTag("settings-back").performClick()
        assertEquals(1, retries)
        assertEquals(1, backs)
    }

    @Test
    fun savingDisablesOnlyItsOwnSettingAndRetainsTheCommittedValue() {
        var motion: Boolean? = null
        compose.setContent {
            MobiMonTheme {
                SettingsScreen(
                    CompanionSettings(showOnVehicleHome = true),
                    {},
                    { motion = it },
                    visibilitySaving = true,
                    parkedVerified = true,
                )
            }
        }
        compose
            .onNodeWithText("차량 홈 캐릭터")
            .performScrollTo()
            .assertIsNotEnabled()
            .assertIsOn()
        compose
            .onNodeWithText("움직임 줄이기")
            .performScrollTo()
            .assertIsEnabled()
            .performClick()
        assertEquals(true, motion)
    }

    @Test
    fun failedSaveKeepsSavedValueAndAllowsRetryThroughTheSameControl() {
        var requested: Boolean? = null
        compose.setContent {
            MobiMonTheme {
                SettingsScreen(
                    CompanionSettings(showOnVehicleHome = true),
                    { requested = it },
                    {},
                    visibilityError = "저장 실패",
                    parkedVerified = true,
                )
            }
        }
        compose.onNodeWithText("저장 실패").performScrollTo().assertExists()
        compose.onNodeWithText("차량 홈 캐릭터").assertIsOn().performClick()
        assertEquals(false, requested)
    }

    @Test
    @Config(qualifiers = "ko-rKR-w1000dp-h700dp")
    fun enlargedTextKeepsDoneReachableAndKeyboardOperable() {
        var done = 0
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 1.5f)) {
                MobiMonTheme { SettingsScreen(CompanionSettings(), {}, {}, onDone = { done++ }) }
            }
        }
        compose
            .onNodeWithTag("settings-done")
            .assertIsDisplayed()
            .performSemanticsAction(SemanticsActions.RequestFocus)
        compose.onNodeWithTag("settings-done").assertIsFocused().performKeyInput { pressKey(Key.Enter) }
        assertEquals(1, done)
    }

    @Test
    fun loadingDoesNotExposeDefaultPreferencesAsLoaded() {
        compose.setContent {
            MobiMonTheme { SettingsScreen(CompanionSettings(), {}, {}, settingsAvailable = false) }
        }
        compose.onNodeWithText("설정을 불러오고 있어요.").assertExists()
        compose.onNodeWithText("차량 홈 캐릭터").assertDoesNotExist()
        compose.onNodeWithTag("settings-back").assertIsEnabled()
    }

    @Test
    fun simulatedParkingIsExplicitlyLabeled() {
        compose.setContent {
            MobiMonTheme { SettingsScreen(CompanionSettings(), {}, {}, parkedVerified = true, simulatedVehicle = true) }
        }
        compose.onNodeWithText("P · 주차 중 · 시뮬레이션").assertExists()
    }

    @Test
    fun settingsDoesNotAdvertisePersonalMemory() {
        compose.setContent {
            MobiMonTheme {
                SettingsScreen(
                    settings = CompanionSettings(),
                    onShowOnVehicleHomeChange = {},
                    onReducedMotionChange = {},
                )
            }
        }

        compose.onNodeWithText("기억 관리", substring = true).assertDoesNotExist()
    }
}
