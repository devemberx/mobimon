package com.monsters.mobimon.core.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "ko-rKR-w1000dp-h900dp")
class MobiMonThemeComponentsTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun themeUsesSemanticRolesAndBundledTypography() {
        compose.setContent {
            MobiMonTheme {
                assertEquals(MobiMonColors.background, MaterialTheme.colorScheme.background)
                assertEquals(MobiMonColors.button, MaterialTheme.colorScheme.primary)
                assertEquals(MobiMonFontFamily, MaterialTheme.typography.bodySmall.fontFamily)
                assertEquals(MobiMonFontFamily, MaterialTheme.typography.displayLarge.fontFamily)
            }
        }
    }

    @Test
    fun allActionStylesKeepDisabledTouchTargetsAndCannotDispatch() {
        var calls = 0
        compose.setContent {
            MobiMonTheme {
                Column {
                    MobiMonButtonStyle.entries.forEach { style ->
                        MobiMonButton(
                            { calls++ },
                            Modifier.testTag(style.name),
                            enabled = false,
                            style = style,
                        ) { Text(style.name) }
                    }
                }
            }
        }
        MobiMonButtonStyle.entries.forEach {
            compose
                .onNodeWithTag(
                    it.name,
                ).assertHeightIsAtLeast(76.dp)
                .assertWidthIsAtLeast(76.dp)
                .assertIsNotEnabled()
                .performClick()
        }
        assertEquals(0, calls)
    }

    @OptIn(androidx.compose.ui.test.ExperimentalTestApi::class)
    @Test
    fun selectionIsCallerOwnedAndKeyboardOperable() {
        var calls = 0
        compose.setContent {
            MobiMonTheme { MobiMonTab(false, { calls++ }, Modifier.testTag("tab")) { Text("Friends") } }
        }
        compose
            .onNodeWithTag("tab")
            .assertHeightIsAtLeast(76.dp)
            .assertIsNotSelected()
            .performSemanticsAction(SemanticsActions.RequestFocus)
            .assertIsFocused()
            .performKeyInput { pressKey(Key.Enter) }
        assertEquals(1, calls)
        compose.onNodeWithTag("tab").assertIsNotSelected()
    }

    @Test
    fun disabledSelectedCardRetainsSelectionWithoutDispatch() {
        var calls = 0
        compose.setContent {
            MobiMonTheme {
                MobiMonSelectionCard(
                    true,
                    { calls++ },
                    Modifier.testTag("card"),
                    enabled = false,
                ) { Text("Owned") }
            }
        }
        compose
            .onNodeWithTag(
                "card",
            ).assertIsSelected()
            .assertIsNotEnabled()
            .assertHeightIsAtLeast(76.dp)
            .performClick()
        assertEquals(0, calls)
    }

    @Test
    fun mixedSizeAccountContentsShareOneVerticalCenter() {
        compose.setContent {
            MobiMonTheme {
                MobiMonListItem(
                    Modifier.size(900.dp, 132.dp).testTag("row"),
                    leading = { Box(Modifier.size(46.dp).testTag("icon")) },
                    trailing = { Text("Example", Modifier.testTag("label"), fontSize = 26.sp) },
                ) { Text("@mobimon-driver", Modifier.testTag("name"), fontSize = 38.sp) }
            }
        }
        val center =
            compose
                .onNodeWithTag("row")
                .fetchSemanticsNode()
                .boundsInRoot.center.y
        listOf("name", "icon", "label").forEach {
            assertEquals(
                center,
                compose
                    .onNodeWithTag(it)
                    .fetchSemanticsNode()
                    .boundsInRoot.center.y,
                1f,
            )
        }
    }

    @Test
    fun tabsAndInformationReflowWithEnlargedText() {
        compose.setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f, 2f)) {
                MobiMonTheme {
                    Column(Modifier.width(360.dp)) {
                        MobiMonTabs {
                            MobiMonTab(true, {}, Modifier.width(240.dp).testTag("first")) { Text("Friends") }
                            MobiMonTab(false, {}, Modifier.width(240.dp).testTag("second")) { Text("Backgrounds") }
                        }
                        MobiMonListItem(trailing = { Text("Available", Modifier.testTag("status")) }) {
                            Text("Vehicle reading", Modifier.testTag("reading"))
                        }
                    }
                }
            }
        }
        val first = compose.onNodeWithTag("first").fetchSemanticsNode().boundsInRoot
        val second = compose.onNodeWithTag("second").fetchSemanticsNode().boundsInRoot
        assertTrue(second.top >= first.bottom)
        val label = compose.onNodeWithTag("reading").fetchSemanticsNode().boundsInRoot
        assertTrue(
            compose
                .onNodeWithTag("status")
                .fetchSemanticsNode()
                .boundsInRoot.top >= label.bottom,
        )
    }
}
