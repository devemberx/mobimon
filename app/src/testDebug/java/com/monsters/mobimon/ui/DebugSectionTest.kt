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
class DebugSectionTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun sectionCanCollapseAndExpandContent() {
        compose.setContent {
            DebugSection("차량 신호 (VSS)") {
                Text("Vehicle.Speed")
            }
        }

        compose.onNodeWithText("Vehicle.Speed").assertDoesNotExist()

        compose.onNodeWithContentDescription("차량 신호 (VSS) 펼치기").performClick()
        compose.onNodeWithText("Vehicle.Speed").assertIsDisplayed()

        compose.onNodeWithContentDescription("차량 신호 (VSS) 접기").performClick()
        compose.onNodeWithText("Vehicle.Speed").assertDoesNotExist()
    }
}
