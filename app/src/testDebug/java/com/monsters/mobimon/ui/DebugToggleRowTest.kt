package com.monsters.mobimon.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DebugToggleRowTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun toggleRowDisplaysLabelAndTogglesState() {
        var toggledState = false
        compose.setContent {
            var checked by remember { mutableStateOf(false) }
            DebugToggleRow(
                label = "배터리 적정 충전 완료",
                checked = checked,
                onCheckedChange = {
                    checked = it
                    toggledState = it
                },
            )
        }

        compose.onNodeWithText("배터리 적정 충전 완료").assertIsDisplayed()
        val switchNode = compose.onNode(isToggleable())
        switchNode.assertIsOff()
        assertFalse(toggledState)

        switchNode.performClick()
        switchNode.assertIsOn()
        assertTrue(toggledState)

        switchNode.performClick()
        switchNode.assertIsOff()
        assertFalse(toggledState)
    }
}
