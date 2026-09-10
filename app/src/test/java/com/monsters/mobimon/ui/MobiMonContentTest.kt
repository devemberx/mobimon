package com.monsters.mobimon.ui

import android.app.Application
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
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
@OptIn(ExperimentalTestApi::class)
class MobiMonContentTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun detailBackReturnsToMenuAndCloseDismissesTheWholeDrawer() {
        showHome()
        compose.onNodeWithContentDescription("메뉴 열기").performClick()
        compose.onNodeWithText("퀘스트 정보").performClick()
        compose.onNodeWithText("메뉴로").performClick()
        compose.onNodeWithText("퀘스트 정보").assertIsFocused()
        compose.onNodeWithText("캐릭터 선택").assertExists()
        compose.onNodeWithText("닫기").performClick()
        compose.onNodeWithContentDescription("메뉴 열기").assertExists()
    }

    @Test
    fun escapeReturnsFromDetailToMenuBeforeClosingTheDrawer() {
        showHome()
        compose.onNodeWithContentDescription("메뉴 열기").performClick()
        compose.onNodeWithText("퀘스트 정보").performClick()

        compose.onNodeWithText("메뉴로").performKeyInput { pressKey(Key.Escape) }

        compose.onNodeWithText("퀘스트 정보").assertIsFocused()
        compose.onNodeWithText("캐릭터 선택").assertExists()
        compose.onNodeWithText("퀘스트 정보").performKeyInput { pressKey(Key.Escape) }

        compose.onNodeWithText("닫기").assertDoesNotExist()
        compose.onNodeWithContentDescription("메뉴 열기").assertExists()
    }

    @Test
    fun outsideDismissClosesDetailsAndSimulationLabelRemainsVisibleInDrawer() {
        showHome()
        compose.onNodeWithContentDescription("메뉴 열기").performClick()
        compose.onNodeWithText("설정").performClick()
        compose.onNodeWithText("시뮬레이션 · 개발용 차량 정보").assertExists()
        compose.onNodeWithContentDescription("메뉴 전체 닫기").performClick()
        compose.onNodeWithContentDescription("메뉴 열기").assertExists()
    }

    private fun showHome() {
        compose.setContent {
            MobiMonContent(
                PetUiState(profile = PetProfile("demo-profile"), isLoading = false),
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
                ),
                MobiMonActions({}, {}, {}, {}, {}, {}, {}),
            )
        }
    }
}
