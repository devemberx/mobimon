package com.monsters.mobimon.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w1200dp-h1000dp-mdpi")
class DebugOverlayPlacementTest {
    @get:Rule val compose = createComposeRule()

    @Test fun reversingDragAtAnEdgeMovesImmediately() {
        compose.setContent {
            Box(Modifier.size(1000.dp, 800.dp).testTag("host")) {
                DebugOverlayPlacement {
                    Box(Modifier.fillMaxWidth().height(600.dp).testTag("panel"))
                }
            }
        }
        compose.onNodeWithTag("host").performTouchInput {
            down(Offset(400f, 200f))
            moveBy(Offset(600f, 500f))
        }
        val bottomRight = compose.onNodeWithTag("panel").getUnclippedBoundsInRoot()
        assertEquals(280f, bottomRight.left.value, 1f)
        assertEquals(200f, bottomRight.top.value, 1f)
        compose.onNodeWithTag("host").performTouchInput {
            moveBy(Offset(-40f, -40f))
            up()
        }
        val reversed = compose.onNodeWithTag("panel").getUnclippedBoundsInRoot()
        assertEquals(240f, reversed.left.value, 1f)
        assertEquals(160f, reversed.top.value, 1f)

        compose.onNodeWithTag("host").performTouchInput {
            down(Offset(400f, 200f))
            moveBy(Offset(-600f, -500f))
            moveBy(Offset(40f, 40f))
            up()
        }
        val topLeft = compose.onNodeWithTag("panel").getUnclippedBoundsInRoot()
        assertEquals(40f, topLeft.left.value, 1f)
        assertEquals(40f, topLeft.top.value, 1f)
        assertInside()
    }

    @Test fun draggingAndWindowResizeKeepTheWholePanelInside() {
        val height = mutableStateOf(800.dp)
        compose.setContent {
            Box(Modifier.size(1000.dp, height.value).testTag("host")) {
                DebugOverlayPlacement {
                    Box(Modifier.fillMaxWidth().height(600.dp).testTag("panel"))
                }
            }
        }
        compose.onNodeWithTag("panel").performTouchInput { swipe(center, center + Offset(2000f, 2000f)) }
        assertInside()
        compose.runOnIdle { height.value = 400.dp }
        assertInside()
    }

    private fun assertInside() {
        val host = compose.onNodeWithTag("host").getUnclippedBoundsInRoot()
        val panel = compose.onNodeWithTag("panel").getUnclippedBoundsInRoot()
        assertEquals(720f, (panel.right - panel.left).value, 1f)
        assertTrue(panel.left >= host.left && panel.top >= host.top)
        assertTrue(panel.right <= host.right && panel.bottom <= host.bottom)
    }
}
