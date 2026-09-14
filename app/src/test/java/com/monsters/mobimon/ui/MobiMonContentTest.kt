package com.monsters.mobimon.ui

import android.app.Application
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.monsters.mobimon.core.domain.AppUseState
import com.monsters.mobimon.core.navigation.AiRoute
import com.monsters.mobimon.core.navigation.AppRoute
import com.monsters.mobimon.core.navigation.CompanionRoute
import com.monsters.mobimon.core.navigation.FeatureEntry
import com.monsters.mobimon.core.navigation.FeatureNavigator
import com.monsters.mobimon.core.navigation.QuestRoute
import com.monsters.mobimon.core.navigation.VehicleRoute
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "ko-rKR-w1000dp-h700dp")
class MobiMonContentTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun menuNavigationClosesOverlayAndBackReturnsHome() {
        show()
        compose.onNodeWithText("Open menu").performClick()
        compose.onNodeWithText("퀘스트").performClick()
        compose.onNodeWithContentDescription("닫기").assertDoesNotExist()
        compose.onNodeWithText("Route QUESTS").assertExists()
        compose.onNodeWithText("Back").performClick()
        compose.onNodeWithText("Route HOME").assertExists()
    }

    @Test
    fun connectionReturnsToItsSettingsOrigin() {
        show()
        compose.onNodeWithText("Open menu").performClick()
        compose.onNodeWithText("설정").performClick()
        compose.onNodeWithText("Connect").performClick()
        compose.onNodeWithText("Route COPILOT").assertExists()
        compose.onNodeWithText("Back").performClick()
        compose.onNodeWithText("Route SETTINGS").assertExists()
    }

    @Test
    fun restrictionGatePreservesDestinationAcrossRecovery() {
        val appUse = mutableStateOf(AppUseState.ALLOWED)
        compose.setContent { MobiMonContent(entries, appUseState = appUse.value) }
        compose.onNodeWithText("Open menu").performClick()
        compose.onNodeWithText("퀘스트").performClick()
        compose.runOnIdle { appUse.value = AppUseState.RESTRICTED }
        compose.onNodeWithText("Route QUESTS").assertDoesNotExist()
        compose.onNodeWithText("지금은 MobiMon 사용이 제한돼요").assertExists()
        compose.runOnIdle { appUse.value = AppUseState.ALLOWED }
        compose.onNodeWithText("Route QUESTS").assertExists()
    }

    @Test
    fun missingAppUseEvidenceFailsClosed() {
        compose.setContent { MobiMonContent(entries) }
        compose.onNodeWithText("Route HOME").assertDoesNotExist()
        compose.onNodeWithText("지금은 MobiMon 사용이 제한돼요").assertExists()
    }

    @Test
    @Config(qualifiers = "ko-rKR-w1792dp-h888dp")
    fun menuRetainsReferenceProportionsAndAccessibleTargets() {
        show()
        compose.onNodeWithText("Open menu").performClick()
        val host = compose.onNodeWithTag("menu-host").fetchSemanticsNode().boundsInRoot
        val panel = compose.onNodeWithTag("companion-menu").fetchSemanticsNode().boundsInRoot
        assertTrue(panel.width <= host.width * 0.25f)
        val scale = panel.width / 608f
        assertEquals(576f * scale, panel.height, 2f)
        compose.onNodeWithContentDescription("닫기").assertWidthIsAtLeast(76.dp).assertHeightIsAtLeast(76.dp)
        listOf(VehicleRoute.VEHICLE_INFO, QuestRoute.QUESTS, CompanionRoute.SETTINGS).forEachIndexed { index, route ->
            val visual =
                compose
                    .onNodeWithTag("menu-item-visual-${route.name}", useUnmergedTree = true)
                    .fetchSemanticsNode()
                    .boundsInRoot
            assertEquals(544f * scale, visual.width, 2f)
            assertEquals(panel.top + (136 + index * 128) * scale, visual.top, 2f)
        }
        compose.onNodeWithText("차량 상태").assertIsFocused()
    }

    private fun show() {
        compose.setContent { MobiMonContent(entries, appUseState = AppUseState.ALLOWED) }
    }

    private val entries =
        setOf(
            object : FeatureEntry {
                override val routes = AppRoute.entries.toSet()

                @Composable
                override fun Content(
                    route: AppRoute,
                    navigator: FeatureNavigator,
                    modifier: Modifier,
                ) {
                    Column(modifier) {
                        Text("Route ${route.name}")
                        TextButton(onClick = navigator.openMenu) { Text("Open menu") }
                        TextButton(onClick = { navigator.navigate(AiRoute.COPILOT) }) { Text("Connect") }
                        TextButton(onClick = navigator.back) { Text("Back") }
                    }
                }
            },
        )
}
