package com.monsters.mobimon.ui

import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DebugOverlayFrameTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun minimizeHidesContentAndRestoreShowsItAgain() {
        compose.setContent {
            DebugOverlayFrame(title = "디버깅 모드 (테스트 신호)") {
                Text("차량 신호 (VSS)")
            }
        }

        compose.onNodeWithText("차량 신호 (VSS)").assertIsDisplayed()

        compose.onNodeWithContentDescription("디버그 창 축소").performClick()
        compose.onNodeWithText("차량 신호 (VSS)").assertDoesNotExist()
        compose.onNodeWithText("디버깅 모드 (테스트 신호)").assertIsDisplayed()

        compose.onNodeWithContentDescription("디버그 창 펼치기").performClick()
        compose.onNodeWithText("차량 신호 (VSS)").assertIsDisplayed()
    }

    @Test
    fun closeHidesTheFloatingWindow() {
        compose.setContent {
            DebugOverlayFrame(title = "디버깅 모드 (테스트 신호)") {
                Text("차량 신호 (VSS)")
            }
        }

        compose.onNodeWithContentDescription("디버그 창 닫기").performClick()

        compose.onNodeWithText("디버깅 모드 (테스트 신호)").assertDoesNotExist()
        compose.onNodeWithText("차량 신호 (VSS)").assertDoesNotExist()
    }
}
