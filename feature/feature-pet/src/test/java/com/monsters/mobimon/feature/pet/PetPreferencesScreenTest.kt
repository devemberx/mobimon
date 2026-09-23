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
@Config(sdk = [34], qualifiers = "ko-rKR-w2560dp-h1332dp-mdpi")
@OptIn(androidx.compose.ui.test.ExperimentalTestApi::class)
class PetPreferencesScreenTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    @Config(qualifiers = "ko-rKR-w1792dp-h952dp-mdpi")
    fun headUnitKeepsSharedRowsReachableAndDoneVisible() {
        compose.setContent {
            MobiMonTheme {
                SettingsScreen(
                    CompanionSettings(),
                    {},
                    debugModeAvailable = true,
                    parkedVerified = true,
                )
            }
        }
        compose.onNodeWithText("GitHub Copilot").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("차량 홈 캐릭터").performScrollTo().assertIsNotEnabled()
        compose.onNodeWithText("방해 금지").assertDoesNotExist()
        compose.onNodeWithText("Debugger").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("settings-done").assertIsDisplayed().assertHeightIsAtLeast(76.dp)
        compose.onNodeWithTag("settings-back").assertHeightIsAtLeast(76.dp)
    }

    @Test
    fun debugModeToggleRequestsChangeButKeepsCommittedSetting() {
        var requested: Boolean? = null
        compose.setContent {
            MobiMonTheme {
                SettingsScreen(
                    settings = CompanionSettings(debugModeEnabled = false),
                    parkedVerified = true,
                    onReducedMotionChange = {},
                    onDebugModeChange = { requested = it },
                    debugModeAvailable = true,
                )
            }
        }

        compose.onNodeWithText("Debugger").performScrollTo().performClick()

        assertEquals(true, requested)
    }

    @Test
    fun launcherCharacterToggleRequestsChangeWhenProvided() {
        var requested: Boolean? = null
        compose.setContent {
            MobiMonTheme {
                SettingsScreen(
                    settings = CompanionSettings(launcherCharacterEnabled = false),
                    parkedVerified = true,
                    onReducedMotionChange = {},
                    onLauncherCharacterChange = { requested = it },
                )
            }
        }

        compose.onNodeWithText("차량 홈 캐릭터").performScrollTo().performClick()

        assertEquals(true, requested)
    }

    @Test
    fun launcherCharacterShowsPermissionWarningWhenEnabledWithoutOverlayPermission() {
        compose.setContent {
            MobiMonTheme {
                SettingsScreen(
                    settings = CompanionSettings(launcherCharacterEnabled = true),
                    parkedVerified = true,
                    onReducedMotionChange = {},
                    onLauncherCharacterChange = {},
                    hasOverlayPermission = false,
                )
            }
        }

        compose.onNodeWithText("차량 홈 캐릭터").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("다른 앱 위에 표시 권한 허용이 필요합니다.").assertIsDisplayed()
    }

    @Test
    fun launcherCharacterToggleOffRequestsFalseWhenEnabled() {
        var requested: Boolean? = null
        compose.setContent {
            MobiMonTheme {
                SettingsScreen(
                    settings = CompanionSettings(launcherCharacterEnabled = true),
                    parkedVerified = true,
                    onReducedMotionChange = {},
                    onLauncherCharacterChange = { requested = it },
                    hasOverlayPermission = true,
                )
            }
        }

        compose.onNodeWithText("차량 홈 캐릭터").performScrollTo().performClick()

        assertEquals(false, requested)
    }

    @Test
    fun unavailableServicesAndUnknownParkingDoNotInvokeSettingsActions() {
        var motionCalls = 0
        var debugCalls = 0
        compose.setContent {
            MobiMonTheme {
                SettingsScreen(
                    settings = CompanionSettings(),
                    onReducedMotionChange = { motionCalls++ },
                    onDebugModeChange = { debugCalls++ },
                    debugModeAvailable = true,
                    parkedVerified = false,
                )
            }
        }

        compose.onNodeWithText("주차 확인 불가").assertExists()
        compose.onNodeWithText("GitHub Copilot").assertIsNotEnabled()
        compose.onNodeWithText("움직임 줄이기").performScrollTo().performClick()
        compose.onNodeWithText("Debugger").performScrollTo().performClick()
        assertEquals(0, motionCalls)
        assertEquals(0, debugCalls)
    }

    @Test
    fun failedLoadOffersRetryAndNavigationRemainsAvailable() {
        var retries = 0
        var backs = 0
        compose.setContent {
            MobiMonTheme {
                SettingsScreen(
                    settings = CompanionSettings(),
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
    fun savingDisablesMotionSettingAndRetainsTheCommittedValue() {
        compose.setContent {
            MobiMonTheme {
                SettingsScreen(
                    CompanionSettings(reducedMotion = true),
                    {},
                    motionSaving = true,
                    parkedVerified = true,
                )
            }
        }
        compose
            .onNodeWithText("움직임 줄이기")
            .performScrollTo()
            .assertIsNotEnabled()
            .performClick()
        compose.onNodeWithText("변경 사항을 저장하고 있어요.").assertExists()
    }

    @Test
    fun enlargedTextKeepsDoneReachableAndKeyboardOperable() {
        var done = 0
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 1.5f)) {
                MobiMonTheme { SettingsScreen(CompanionSettings(), {}, onDone = { done++ }) }
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
            MobiMonTheme { SettingsScreen(CompanionSettings(), {}, settingsAvailable = false) }
        }
        compose.onNodeWithText("설정을 불러오고 있어요.").assertExists()
        compose.onNodeWithText("차량 홈 캐릭터").assertDoesNotExist()
        compose.onNodeWithTag("settings-back").assertIsEnabled()
    }

    @Test
    fun settingsOmitsSimulationBadge() {
        compose.setContent {
            MobiMonTheme { SettingsScreen(CompanionSettings(), {}, parkedVerified = true, simulatedVehicle = true) }
        }
        compose.onNodeWithText("주차 확인됨").assertExists()
        compose.onNodeWithText("시뮬레이션").assertDoesNotExist()
    }

    @Test
    fun settingsDoesNotAdvertisePersonalMemory() {
        compose.setContent {
            MobiMonTheme {
                SettingsScreen(
                    settings = CompanionSettings(),
                    onReducedMotionChange = {},
                )
            }
        }

        compose.onNodeWithText("기억 관리", substring = true).assertDoesNotExist()
    }

    @Test
    fun productionSettingsOmitsDoNotDisturbAndDebugMode() {
        compose.setContent {
            MobiMonTheme {
                SettingsScreen(
                    settings = CompanionSettings(debugModeEnabled = true),
                    onReducedMotionChange = {},
                    debugModeAvailable = false,
                )
            }
        }

        compose.onNodeWithText("방해 금지").assertDoesNotExist()
        compose.onNodeWithText("Debugger").assertDoesNotExist()
    }
}
