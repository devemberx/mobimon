package com.monsters.mobimon.core.ui

import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "ko-rKR-w1000dp-h700dp")
class MobiMonComponentsTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun disabledPrimaryActionKeepsItsTouchTargetAndDoesNotDispatch() {
        var clicks = 0
        compose.setContent {
            MobiMonTheme {
                MobiMonButton(onClick = { clicks++ }, modifier = Modifier.testTag("action"), enabled = false) {
                    Text("Apply")
                }
            }
        }
        compose
            .onNodeWithTag("action")
            .assertHeightIsAtLeast(76.dp)
            .assertWidthIsAtLeast(76.dp)
            .assertIsNotEnabled()
            .performClick()
        assertEquals(0, clicks)
    }

    @Test
    fun destinationDispatchesIndependentBackAndHomeActions() {
        var back = 0
        var home = 0
        compose.setContent {
            MobiMonTheme {
                MobiMonDestination("Details", { back++ }, { home++ }) { Text("Feature content") }
            }
        }
        compose.onNodeWithText("Feature content").assertExists()
        compose
            .onNodeWithText("뒤로")
            .assertWidthIsAtLeast(76.dp)
            .assertHeightIsAtLeast(76.dp)
            .performClick()
        compose
            .onNodeWithText("홈으로")
            .assertWidthIsAtLeast(76.dp)
            .assertHeightIsAtLeast(76.dp)
            .performClick()
        assertEquals(1, back)
        assertEquals(1, home)
    }
}
