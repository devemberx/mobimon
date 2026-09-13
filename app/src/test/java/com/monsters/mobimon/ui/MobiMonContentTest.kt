package com.monsters.mobimon.ui

import android.app.Application
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.unit.dp
import com.monsters.mobimon.core.domain.AppUseState
import com.monsters.mobimon.core.domain.DrivingState
import com.monsters.mobimon.core.domain.PetProfile
import com.monsters.mobimon.core.domain.SignalQuality
import com.monsters.mobimon.core.domain.SignalSource
import com.monsters.mobimon.core.domain.VehicleSnapshot
import com.monsters.mobimon.feature.pet.PetUiState
import com.monsters.mobimon.feature.quest.QuestUiState
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
    fun settingsConnectionShowsHonestUnavailableFeedbackAndReturnsToSettings() {
        showHome(petState = PetUiState(profile = PetProfile("demo-profile"), isLoading = false, settingsLoaded = true))
        compose.onNodeWithContentDescription("메뉴 열기").performClick()
        compose.onNodeWithText("설정").performClick()
        compose.onNodeWithText("GitHub Copilot").performScrollTo().performClick()
        compose.onNodeWithText("QR로 연결하기").performScrollTo().performClick()
        compose.onNodeWithText("아직 계정 연결을 이용할 수 없어요.", substring = true).assertExists()
        compose.onNodeWithText("연결이 완료됐어요.").assertDoesNotExist()
        compose.onNodeWithContentDescription("뒤로").performScrollTo().performClick()
        compose.onNodeWithTag("settings-done").assertExists()
    }

    @Test
    fun homeConversationOpensConnectionAndCancelReturnsHome() {
        showHome()
        compose.onNodeWithText("대화하기").performScrollTo().performClick()
        compose.onNodeWithText("이야기를 시작할 준비").assertExists()
        compose.onNodeWithText("나중에").performScrollTo().performClick()
        compose.onNodeWithContentDescription("메뉴 열기").assertExists()
    }

    @Test
    @Config(qualifiers = "ko-rKR-w1792dp-h888dp")
    fun menuRemainsProportionalToTheHostWindow() {
        showHome()
        compose.onNodeWithContentDescription("메뉴 열기").performClick()
        val host = compose.onNodeWithTag("menu-host").fetchSemanticsNode().boundsInRoot
        val panel = compose.onNodeWithTag("companion-menu").fetchSemanticsNode().boundsInRoot
        assertTrue(panel.width <= host.width * 0.25f)
        assertTrue(panel.height < host.height)
        val scale = panel.width / 608f
        assertEquals(576f * scale, panel.height, 2f)
        val closeVisual =
            compose
                .onNodeWithTag(
                    "menu-close-visual",
                    useUnmergedTree = true,
                ).fetchSemanticsNode()
                .boundsInRoot
        assertEquals(72f * scale, closeVisual.width, 2f)
        assertEquals(72f * scale, closeVisual.height, 2f)
        assertEquals(panel.left + 504f * scale, closeVisual.left, 2f)
        assertEquals(panel.top + 34f * scale, closeVisual.top, 2f)
        compose.onNodeWithContentDescription("닫기").assertWidthIsAtLeast(76.dp).assertHeightIsAtLeast(76.dp)
        listOf(AppRoute.VEHICLE_INFO, AppRoute.QUESTS, AppRoute.SETTINGS).forEachIndexed { index, route ->
            val visual =
                compose
                    .onNodeWithTag(
                        "menu-item-visual-${route.name}",
                        useUnmergedTree = true,
                    ).fetchSemanticsNode()
                    .boundsInRoot
            assertEquals(544f * scale, visual.width, 2f)
            assertEquals(104f * scale, visual.height, 2f)
            assertEquals(panel.top + (136 + index * 128) * scale, visual.top, 2f)
        }
        val closeTouch = compose.onNodeWithContentDescription("닫기").fetchSemanticsNode().boundsInRoot
        val firstTouch =
            compose
                .onNodeWithText("차량 상태")
                .assertHeightIsAtLeast(76.dp)
                .fetchSemanticsNode()
                .boundsInRoot
        assertTrue(closeTouch.bottom <= firstTouch.top)
        val vehicleLabel = compose.onNodeWithText("차량 상태", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
        assertEquals(panel.left + 320f * scale, vehicleLabel.center.x, 2f)
        compose.onNodeWithText("차량 상태").assertIsFocused()
    }

    @Test
    fun menuDestinationClosesOverlayAndUsesFullScreenBeforeReturningHome() {
        showHome()
        compose.onNodeWithContentDescription("메뉴 열기").performClick()
        compose.onNodeWithText("퀘스트").performClick()
        compose.onNodeWithContentDescription("닫기").assertDoesNotExist()
        compose.onNodeWithText("뒤로").performClick()
        compose.onNodeWithContentDescription("메뉴 열기").assertExists()
    }

    @Test
    fun returnHomeSkipsMenuFromDestination() {
        showHome()
        compose.onNodeWithContentDescription("메뉴 열기").performClick()
        compose.onNodeWithText("퀘스트").performClick()
        compose.onNodeWithText("홈으로").performClick()
        compose.onNodeWithContentDescription("닫기").assertDoesNotExist()
        compose.onNodeWithContentDescription("메뉴 열기").assertExists()
    }

    @Test
    fun menuCloseReturnsToHome() {
        showHome()
        compose
            .onNodeWithContentDescription(
                "메뉴 열기",
            ).performSemanticsAction(SemanticsActions.RequestFocus)
            .performClick()
        compose.onNodeWithText("차량 상태").assertIsFocused()
        compose.onNodeWithContentDescription("닫기").performClick()
        compose.onNodeWithContentDescription("메뉴 열기").assertIsFocused()
    }

    @Test
    fun settingsDoneClosesTheScreenAndReturnsToHome() {
        showHome(petState = PetUiState(profile = PetProfile("demo-profile"), isLoading = false, settingsLoaded = true))
        compose.onNodeWithContentDescription("메뉴 열기").performClick()
        compose.onNodeWithText("설정").performClick()
        compose.onNodeWithContentDescription("닫기").assertDoesNotExist()
        compose.onNodeWithTag("settings-done").performScrollTo().performClick()
        compose.onNodeWithContentDescription("메뉴 열기").assertExists()
    }

    @Test
    fun questObservationFailureKeepsHomeAndVehicleDetailsReachable() {
        showHome(questState = defaultQuestState().copy(observationFailed = true))
        compose.onNodeWithContentDescription("메뉴 열기").assertExists()
        compose.onNodeWithContentDescription("차량 정보 자세히 보기").assertExists()
    }

    @Test
    fun profileObservationFailureRetainsLastCommittedHome() {
        showHome(petState = PetUiState(profile = PetProfile("demo-profile"), isLoading = false, loadFailed = true))
        compose.onNodeWithContentDescription("메뉴 열기").assertExists()
        compose.onNodeWithText("친구 정보를 다시 확인하지 못했어요", substring = true).assertExists()
    }

    @Test
    fun appearanceSaveFailureDoesNotAppearOnSettingsScreen() {
        showHome(
            petState = PetUiState(profile = PetProfile("demo-profile"), isLoading = false, appearanceSaveFailed = true),
        )
        compose.onNodeWithContentDescription("메뉴 열기").performClick()
        compose.onNodeWithText("설정").performClick()
        compose.onNodeWithText("저장하지 못했어요", substring = true).assertDoesNotExist()
    }

    @Test
    fun uxRestrictionPausesRoutesWithoutDiscardingProfile() {
        compose.setContent {
            MobiMonContent(
                petState = PetUiState(profile = PetProfile("demo-profile"), isLoading = false),
                questState = defaultQuestState(),
                actions = MobiMonActions({}, {}, {}, {}, {}, {}, {}),
                appUseState = AppUseState.RESTRICTED,
            )
        }
        compose.onNodeWithText("지금은 MobiMon 사용이 제한돼요").assertExists()
        compose.onNodeWithContentDescription("메뉴 열기").assertDoesNotExist()
    }

    private fun showHome(
        petState: PetUiState = PetUiState(profile = PetProfile("demo-profile"), isLoading = false),
        questState: QuestUiState = defaultQuestState(),
    ) {
        compose.setContent {
            MobiMonContent(
                petState,
                questState,
                MobiMonActions({}, {}, {}, {}, {}, {}, {}),
            )
        }
    }

    private fun defaultQuestState() =
        QuestUiState(
            VehicleSnapshot(
                "snapshot",
                "epoch",
                1,
                1_000,
                SignalSource.SIMULATED,
                DrivingState.PARKED,
                SignalQuality.VALID,
                72,
            ),
        )
}
