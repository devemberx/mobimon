package com.monsters.mobimon.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DebugInputRowTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun numberInputCanBeClearedAndAppliesZeroWhenEmpty() {
        var appliedValue = 40
        compose.setContent {
            var value by remember { mutableStateOf(appliedValue.toString()) }
            DebugInputRow(
                label = "speed",
                value = value,
                isNumber = true,
                onValueChange = {
                    val parsed = it.toIntOrNull() ?: 0
                    appliedValue = parsed
                    value = parsed.toString()
                },
            )
        }

        val inputNode = compose.onNodeWithTag("debug-input-speed")
        inputNode.assertTextEquals("40")
        assertEquals(40, appliedValue)

        // Clear all text (simulate deleting 40 -> 4 -> empty)
        inputNode.performTextClearance()
        assertEquals(0, appliedValue)
        inputNode.assertTextEquals("")

        // Now type 0
        inputNode.performTextInput("0")
        inputNode.assertTextEquals("0")
        assertEquals(0, appliedValue)

        // Clear again and type 5
        inputNode.performTextClearance()
        assertEquals(0, appliedValue)
        inputNode.performTextInput("5")
        inputNode.assertTextEquals("5")
        assertEquals(5, appliedValue)
    }
}
