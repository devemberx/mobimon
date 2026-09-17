package com.monsters.mobimon.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DebugInterpretationRowTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun formulaButtonShowsCalculationDetails() {
        compose.setContent {
            DebugInterpretationRow(
                label = "isRaining",
                value = "false",
                inputType = DebugInterpretationInputType.Boolean,
                formula = "Vehicle.Body.Raindetection.Intensity > 0",
                onManualValueChange = {},
                onClearManualValue = {},
            )
        }

        compose.onNodeWithContentDescription("isRaining 계산식 보기").performClick()

        compose
            .onNodeWithText("Vehicle.Body.Raindetection.Intensity > 0")
            .assertIsDisplayed()
    }

    @Test
    fun booleanInterpretationUsesToggleInsteadOfTextInput() {
        var manualValue by mutableStateOf("")
        compose.setContent {
            DebugInterpretationRow(
                label = "isMoving",
                value = "false",
                inputType = DebugInterpretationInputType.Boolean,
                formula = "Vehicle.IsMoving || Vehicle.Speed > 0",
                manualValue = manualValue,
                onManualValueChange = { manualValue = it },
                onClearManualValue = { manualValue = "" },
            )
        }

        compose
            .onNodeWithTag("debug-interpretation-input-isMoving")
            .assertDoesNotExist()
        compose
            .onNodeWithTag("debug-interpretation-toggle-isMoving")
            .assertIsDisplayed()
            .assertIsOff()

        compose.onNodeWithTag("debug-interpretation-toggle-isMoving").performClick()

        compose.onNodeWithTag("debug-interpretation-toggle-isMoving").assertIsOn()
    }

    @Test
    fun textInterpretationKeepsSingleLineInput() {
        compose.setContent {
            DebugInterpretationRow(
                label = "gear",
                value = "P",
                inputType = DebugInterpretationInputType.Text,
                formula = "SelectedGear: 126=P, 127=D, 0=N, negative=R",
                onManualValueChange = {},
                onClearManualValue = {},
            )
        }

        compose
            .onNodeWithTag("debug-interpretation-input-gear")
            .assertIsDisplayed()
    }
}
