package com.monsters.mobimon.ui

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.monsters.mobimon.core.domain.AppUseState
import com.monsters.mobimon.core.domain.DrivingState
import com.monsters.mobimon.core.domain.PetProfile
import com.monsters.mobimon.core.domain.SignalQuality
import com.monsters.mobimon.core.domain.SignalSource
import com.monsters.mobimon.core.domain.VehicleSnapshot
import com.monsters.mobimon.feature.pet.PetUiState
import com.monsters.mobimon.feature.quest.QuestUiState
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
    fun menuDestinationClosesOverlayAndUsesFullScreenBeforeReturningHome() {
        showHome()
        compose.onNodeWithContentDescription("메뉴 열기").performClick()
        compose.onNodeWithText("퀘스트 정보").performClick()
        compose.onNodeWithText("닫기").assertDoesNotExist()
        compose.onNodeWithText("뒤로").performClick()
        compose.onNodeWithContentDescription("메뉴 열기").assertExists()
    }

    @Test
    fun returnHomeSkipsMenuFromDestination() {
        showHome()
        compose.onNodeWithContentDescription("메뉴 열기").performClick()
        compose.onNodeWithText("퀘스트 정보").performClick()
        compose.onNodeWithText("홈으로").performClick()
        compose.onNodeWithText("닫기").assertDoesNotExist()
        compose.onNodeWithContentDescription("메뉴 열기").assertExists()
    }

    @Test
    fun menuCloseReturnsToHome() {
        showHome()
        compose.onNodeWithContentDescription("메뉴 열기").performClick()
        compose.onNodeWithText("닫기").performClick()
        compose.onNodeWithContentDescription("메뉴 열기").assertExists()
    }

    @Test
    fun questObservationFailureKeepsHomeAndVehicleDetailsReachable() {
        showHome(questState = defaultQuestState().copy(observationFailed = true))
        compose.onNodeWithContentDescription("메뉴 열기").assertExists()
        compose.onNodeWithText("차량 정보 자세히 보기").assertExists()
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
