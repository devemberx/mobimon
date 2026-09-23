package com.monsters.mobimon.ui

import android.app.Application
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import com.monsters.mobimon.core.domain.AppUseState
import com.monsters.mobimon.core.navigation.AiRoute
import com.monsters.mobimon.core.navigation.AppRoute
import com.monsters.mobimon.core.navigation.CompanionRoute
import com.monsters.mobimon.core.navigation.FeatureEntry
import com.monsters.mobimon.core.navigation.FeatureNavigator
import com.monsters.mobimon.core.navigation.LocalDebugSettingsAvailable
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "ko-rKR-w2560dp-h1268dp-mdpi")
class MobiMonContentTest {
    @get:Rule val compose = createComposeRule()
    private val mountedRoutes = mutableStateListOf<AppRoute>()

    private fun clickMenuItem(text: String) {
        compose.onNodeWithText(text).performScrollTo().performClick()
        compose.waitForIdle()
    }

    @Test
    fun menuNavigationClosesOverlayAndBackReturnsHome() {
        show()
        compose.onNodeWithText("Open menu").performClick()
        clickMenuItem("퀘스트")
        compose.onNodeWithContentDescription("닫기").assertDoesNotExist()
        compose.onNodeWithText("Route QUESTS").assertExists()
        compose.onNodeWithText("Back").performClick()
        compose.onNodeWithText("Route HOME").assertExists()
    }

    @Test
    fun menuRoutesConversationAndDismissesFromBackdrop() {
        show()
        compose.onNodeWithText("Open menu").performClick()
        compose.onNodeWithTag("menu-backdrop").performTouchInput {
            click(Offset(width - 24f, height / 2f))
        }
        compose.onNodeWithTag("companion-menu").assertDoesNotExist()
        compose.onNodeWithText("Open menu").performClick()
        clickMenuItem("대화하기")
        compose.onNodeWithText("Route COPILOT").assertExists()
    }

    @Test
    fun chatRoutesFromHomeAndSettingsTrackAccountWithoutAConnectionLoop() {
        val authenticated = mutableStateOf(false)
        compose.setContent {
            MobiMonContent(entries, appUseState = AppUseState.ALLOWED, conversationAuthenticated = authenticated.value)
        }
        compose.onNodeWithText("Chat").performClick()
        compose.onNodeWithText("Route COPILOT").assertExists()
        compose.onNodeWithText("Back").performClick()
        compose.onNodeWithText("Open menu").performClick()
        clickMenuItem("설정")
        compose.onNodeWithText("Chat").performClick()
        compose.onNodeWithText("Route COPILOT").assertExists()
        compose.onNodeWithText("Back").performClick()
        compose.onNodeWithText("Route SETTINGS").assertExists()
        compose.runOnIdle { authenticated.value = true }
        compose.onNodeWithText("Open menu").performClick()
        clickMenuItem("대화하기")
        compose.onNodeWithText("Route CONVERSATION").assertExists()
        compose.runOnIdle { authenticated.value = false }
        compose.onNodeWithText("Route CONVERSATION").assertDoesNotExist()
        compose.onNodeWithText("Route COPILOT").assertExists()
        compose.onNodeWithText("Back").performClick()
        compose.onNodeWithText("Route HOME").assertExists()
    }

    @Test
    fun menuRoutesVehicleAndCustomizationAndClosesWithBackAndClose() {
        show()
        compose.onNodeWithText("Open menu").performClick()
        compose.onNodeWithContentDescription("닫기").performClick()
        compose.onNodeWithTag("companion-menu").assertDoesNotExist()
        listOf("차량 상태" to "VEHICLE_INFO", "꾸미기" to "APPEARANCE").forEach { (label, route) ->
            compose.onNodeWithText("Open menu").performClick()
            clickMenuItem(label)
            compose.onNodeWithText("Route $route").assertExists()
            compose.onNodeWithText("Back").performClick()
        }
        compose.onNodeWithText("Open menu").performClick()
        clickMenuItem("홈")
        compose.onNodeWithText("Route HOME").assertExists()
    }

    @Test
    fun menuProfileTracksEquippedFriend() {
        val friend = mutableStateOf("friend:mobi")
        compose.setContent { MobiMonContent(entries, appUseState = AppUseState.ALLOWED, activeFriendId = friend.value) }
        compose.onNodeWithText("Open menu").performClick()
        compose.onNodeWithText("Mobi").assertExists()
        compose.runOnIdle { friend.value = "friend:luna" }
        compose.onNodeWithText("Luna").assertExists()
        compose.onNodeWithText("Mobi").assertDoesNotExist()
    }

    @Test
    fun connectionReturnsToItsSettingsOrigin() {
        show()
        compose.onNodeWithText("Open menu").performClick()
        clickMenuItem("설정")
        compose.onNodeWithText("Route SETTINGS").assertExists()
        compose.onNodeWithText("Connect").performClick()
        compose.onNodeWithText("Route COPILOT").assertExists()
        compose.onNodeWithText("Back").performClick()
        compose.onNodeWithText("Route SETTINGS").assertExists()
    }

    @Test
    fun destinationChangeKeepsOutgoingFrameUntilIncomingScreenArrives() {
        show()
        compose.mainClock.autoAdvance = false

        compose.onNodeWithText("Connect").performClick()
        compose.mainClock.advanceTimeBy(80)

        compose.runOnIdle {
            assertTrue(mountedRoutes.containsAll(listOf(CompanionRoute.HOME, AiRoute.COPILOT)))
        }
        compose.onNodeWithText("Route HOME").assertDoesNotExist()
        compose.onNodeWithText("Route COPILOT").assertExists()

        compose.mainClock.advanceTimeBy(300)
        compose.runOnIdle {
            assertTrue(
                "mounted: ${mountedRoutes.toList()}",
                mountedRoutes.toList() == listOf(AiRoute.COPILOT),
            )
        }
    }

    @Test
    fun restrictionGatePreservesDestinationAcrossRecovery() {
        val appUse = mutableStateOf(AppUseState.ALLOWED)
        compose.setContent { MobiMonContent(entries, appUseState = appUse.value) }
        compose.onNodeWithText("Open menu").performClick()
        clickMenuItem("퀘스트")
        compose.runOnIdle { appUse.value = AppUseState.RESTRICTED }
        compose.onNodeWithText("Route QUESTS").assertDoesNotExist()
        compose.onNodeWithText("지금은 MobiMon 사용이 제한돼요").assertExists()
        compose.runOnIdle { appUse.value = AppUseState.ALLOWED }
        compose.onNodeWithText("Route QUESTS").assertExists()
    }

    @Test
    fun restrictionGateRemovesDebugControlsBeforeTheyCanReceiveInput() {
        val appUse = mutableStateOf(AppUseState.ALLOWED)
        var debugMutations = 0
        compose.setContent {
            MobiMonContent(
                entries = entries,
                appUseState = appUse.value,
                debugOverlay = {
                    TextButton(onClick = { debugMutations++ }) { Text("Debug mutation") }
                },
            )
        }
        compose.onNodeWithText("Debug mutation").performClick()
        assertTrue(debugMutations == 1)

        compose.runOnIdle { appUse.value = AppUseState.RESTRICTED }

        compose.onNodeWithText("Debug mutation").assertDoesNotExist()
        assertTrue(debugMutations == 1)
    }

    @Test
    fun missingAppUseEvidenceFailsClosed() {
        compose.setContent { MobiMonContent(entries) }
        compose.onNodeWithText("Route HOME").assertDoesNotExist()
        compose.onNodeWithText("지금은 MobiMon 사용이 제한돼요").assertExists()
    }

    @Test
    fun releaseMenuVersionTapsUnlockDebuggerSettings() {
        show(debugSettingsAvailableByDefault = false)
        compose.onNodeWithText("Open menu").performClick()
        clickMenuItem("설정")
        compose.onNodeWithText("Debugger").assertDoesNotExist()

        compose.onNodeWithText("Open menu").performClick()
        repeat(10) {
            compose.onNodeWithTag("menu-version").performScrollTo().performClick()
        }
        clickMenuItem("설정")

        compose.onNodeWithText("Route SETTINGS").assertExists()
        compose.onNodeWithText("Debugger").assertExists()
    }

    @Test
    fun releaseMenuVersionUnlockResetsDebuggerModeToDisabled() {
        var resetRequests = 0
        show(
            debugSettingsAvailableByDefault = false,
            onReleaseDebuggerUnlocked = { resetRequests++ },
        )
        compose.onNodeWithText("Open menu").performClick()

        repeat(9) {
            compose.onNodeWithTag("menu-version").performScrollTo().performClick()
        }
        compose.runOnIdle { assertTrue(resetRequests == 0) }

        compose.onNodeWithTag("menu-version").performScrollTo().performClick()
        compose.runOnIdle { assertTrue(resetRequests == 1) }

        compose.onNodeWithTag("menu-version").performScrollTo().performClick()
        compose.runOnIdle { assertTrue(resetRequests == 1) }
    }

    @Test
    fun releaseMenuVersionTapsShowUnlockToastCountdown() {
        show(debugSettingsAvailableByDefault = false)
        compose.onNodeWithText("Open menu").performClick()

        repeat(4) {
            compose.onNodeWithTag("menu-version").performScrollTo().performClick()
        }
        compose.onNodeWithText("debugger 버튼 활성화까지 5회 남았습니다").assertDoesNotExist()

        listOf(
            "debugger 버튼 활성화까지 5회 남았습니다",
            "debugger 버튼 활성화까지 4회 남았습니다",
            "debugger 버튼 활성화까지 3회 남았습니다",
            "debugger 버튼 활성화까지 2회 남았습니다",
            "debugger 버튼 활성화까지 1회 남았습니다",
            "debugger 버튼이 활성화 되었습니다",
        ).forEach { message ->
            compose.onNodeWithTag("menu-version").performScrollTo().performClick()
            compose.onNodeWithText(message).assertExists()
        }
    }

    @Test
    fun releaseMenuVersionTapCountResetsAfterThreeSeconds() {
        show(debugSettingsAvailableByDefault = false, reducedMotion = true)
        compose.onNodeWithText("Open menu").performClick()
        compose.waitForIdle()
        compose.onNodeWithTag("menu-version").assertExists()

        repeat(5) {
            compose.onNodeWithTag("menu-version").performScrollTo().performClick()
        }
        compose.onNodeWithText("debugger 버튼 활성화까지 5회 남았습니다").assertExists()

        compose.mainClock.advanceTimeBy(3_100)
        compose.waitForIdle()

        repeat(5) {
            compose.onNodeWithTag("menu-version").performScrollTo().performClick()
        }
        compose.onNodeWithText("debugger 버튼 활성화까지 5회 남았습니다").assertExists()

        clickMenuItem("설정")
        compose.onNodeWithText("Debugger").assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = "ko-rKR-w1792dp-h888dp")
    fun menuFitsItsWindowAndKeepsAccessibleTargets() {
        show()
        compose.onNodeWithText("Open menu").performClick()
        val host = compose.onNodeWithTag("menu-host").fetchSemanticsNode().boundsInRoot
        val panel = compose.onNodeWithTag("companion-menu").fetchSemanticsNode().boundsInRoot
        assertTrue(panel.width <= host.width)
        assertTrue(panel.height <= host.height)
        compose.onNodeWithContentDescription("닫기").assertWidthIsAtLeast(76.dp).assertHeightIsAtLeast(76.dp)
        listOf("홈", "대화하기", "퀘스트", "차량 상태", "꾸미기", "설정").forEach { label ->
            compose.onNodeWithText(label).assertHeightIsAtLeast(76.dp).assertWidthIsAtLeast(76.dp)
        }
        compose.onNodeWithText("홈").assertIsFocused()
    }

    private fun show(
        debugSettingsAvailableByDefault: Boolean = true,
        reducedMotion: Boolean = false,
        onReleaseDebuggerUnlocked: () -> Unit = {},
    ) {
        compose.setContent {
            MobiMonContent(
                entries,
                appUseState = AppUseState.ALLOWED,
                reducedMotion = reducedMotion,
                debugSettingsAvailableByDefault = debugSettingsAvailableByDefault,
                onReleaseDebuggerUnlocked = onReleaseDebuggerUnlocked,
            )
        }
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
                    DisposableEffect(route) {
                        mountedRoutes.add(route)
                        onDispose { mountedRoutes.remove(route) }
                    }
                    Column(modifier) {
                        Text("Route ${route.name}")
                        if (route == CompanionRoute.SETTINGS && LocalDebugSettingsAvailable.current) {
                            Text("Debugger")
                        }
                        TextButton(onClick = navigator.openMenu) { Text("Open menu") }
                        TextButton(onClick = { navigator.navigate(AiRoute.COPILOT) }) { Text("Connect") }
                        TextButton(onClick = { navigator.navigate(AiRoute.CONVERSATION) }) { Text("Chat") }
                        TextButton(onClick = navigator.back) { Text("Back") }
                    }
                }
            },
        )
}
