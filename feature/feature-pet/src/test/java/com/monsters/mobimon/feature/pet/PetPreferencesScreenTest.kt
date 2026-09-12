package com.monsters.mobimon.feature.pet

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.monsters.mobimon.core.domain.CompanionSettings
import com.monsters.mobimon.core.domain.PetAppearance
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "ko-rKR")
class PetPreferencesScreenTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun appearancePreviewDoesNotApplyUntilExplicitAction() {
        var requested: PetAppearance? = null
        compose.setContent {
            MaterialTheme {
                AppearanceScreen(PetAppearance.GOLDEN, { requested = it })
            }
        }

        compose.onNodeWithText("크림").performScrollTo().performClick()

        assertEquals(null, requested)
        compose.onNodeWithText("크림").performScrollTo().assertIsSelected()
        compose.onNodeWithText("미리 보는 중").performScrollTo().assertExists()
        compose.onNodeWithText("이 모습 적용").performScrollTo().performClick()
        assertEquals(PetAppearance.CREAM, requested)
    }

    @Test
    fun visibilityToggleRequestsChangeButKeepsCommittedSetting() {
        var requested: Boolean? = null
        compose.setContent {
            MaterialTheme {
                SettingsScreen(
                    settings = CompanionSettings(showOnVehicleHome = true),
                    onShowOnVehicleHomeChange = { requested = it },
                    onReducedMotionChange = {},
                )
            }
        }

        compose.onNodeWithText("앱 내 차량 홈 미리보기에 친구 표시").performScrollTo().performClick()

        assertEquals(false, requested)
        compose.onNodeWithText("앱 내 차량 홈 미리보기에 친구 표시").assertIsOn()
    }

    @Test
    fun settingsDoesNotAdvertisePersonalMemory() {
        compose.setContent {
            MaterialTheme {
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
