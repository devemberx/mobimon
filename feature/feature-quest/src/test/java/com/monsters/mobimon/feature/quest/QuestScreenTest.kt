package com.monsters.mobimon.feature.quest

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.monsters.mobimon.core.domain.DrivingState
import com.monsters.mobimon.core.domain.QuestProgress
import com.monsters.mobimon.core.domain.SignalQuality
import com.monsters.mobimon.core.domain.SignalSource
import com.monsters.mobimon.core.domain.VehicleSnapshot
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "ko-rKR")
class QuestScreenTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun drivingQuestsAreRenderedInQuestList() {
        compose.setContent {
            MaterialTheme {
                QuestScreen(QuestProgress(), canManageQuest = true, {}, {}, {}, snapshot(), true)
            }
        }

        compose.onNodeWithText("놓지마 생명줄!").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("엉덩이 뗄 때까지 안전하게").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("우리가 함께 달린 100km").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("발끝의 미학").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("작심오일은 없다! 5일 연속 무사고").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("배터리 지킴이").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("쉼표가 있는 여정").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("시야를 맑게!").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("네 바퀴의 균형").performScrollTo().assertIsDisplayed()

        // Hidden quests must NOT be in regular list
        compose.onNodeWithText("멋쟁이 모비몬").assertDoesNotExist()
        compose.onNodeWithText("특수효과 뿜뿜!!").assertDoesNotExist()
        compose.onNodeWithText("새로운 나의 작은 친구").assertDoesNotExist()
    }

    @Test
    fun questDetailDisplaysBackButtonAndReturnsToQuestList() {
        var selected: String? = "quest_seatbelt"
        compose.setContent {
            MaterialTheme {
                QuestScreen(
                    progress = QuestProgress(),
                    canManageQuest = true,
                    onAcknowledgeVehicle = {},
                    vehicleSnapshot = snapshot(),
                    canAcknowledgeVehicle = true,
                    selectedQuestId = selected,
                    onSelectQuest = { selected = it },
                )
            }
        }

        compose
            .onNodeWithTag("quest-detail-back-button")
            .performScrollTo()
            .assertIsDisplayed()
            .performClick()
        compose.onNodeWithText("놓지마 생명줄!").performScrollTo().assertIsDisplayed()
    }

    @Test
    @Config(qualifiers = "ko-rKR-w2560dp-h1268dp")
    fun hiddenQuestDialogPopsUpWhenCostumeIsEquipped() {
        var claimedQuestId: String? = null
        compose.setContent {
            MaterialTheme {
                QuestScreen(
                    progress = QuestProgress(),
                    canManageQuest = true,
                    onAcknowledgeVehicle = {},
                    vehicleSnapshot = snapshot(),
                    canAcknowledgeVehicle = true,
                    accessoryId = "acc_glasses",
                    onClaimReward = { claimedQuestId = it },
                )
            }
        }

        compose.onNodeWithTag("quest-hidden-claim-modal").assertIsDisplayed()
        compose.onNodeWithText("멋쟁이 모비몬").assertIsDisplayed()
        compose.onNodeWithTag("quest-hidden-btn-claim").assertIsDisplayed().performClick()
        assertEquals("quest_hidden_costume", claimedQuestId)
    }

    @Test
    @Config(qualifiers = "ko-rKR-w2560dp-h1268dp")
    fun hiddenQuestDialogPopsUpWhenBackgroundIsEquipped() {
        var claimedQuestId: String? = null
        compose.setContent {
            MaterialTheme {
                QuestScreen(
                    progress = QuestProgress(),
                    canManageQuest = true,
                    onAcknowledgeVehicle = {},
                    vehicleSnapshot = snapshot(),
                    canAcknowledgeVehicle = true,
                    backgroundId = "background:star",
                    onClaimReward = { claimedQuestId = it },
                )
            }
        }

        compose.onNodeWithTag("quest-hidden-claim-modal").assertIsDisplayed()
        compose.onNodeWithText("특수효과 뿜뿜!!").assertIsDisplayed()
        compose.onNodeWithTag("quest-hidden-btn-claim").assertIsDisplayed().performClick()
        assertEquals("quest_hidden_background", claimedQuestId)
    }

    @Test
    @Config(qualifiers = "ko-rKR-w2560dp-h1268dp")
    fun hiddenQuestDialogPopsUpWhenNewFriendIsEquipped() {
        var claimedQuestId: String? = null
        compose.setContent {
            MaterialTheme {
                QuestScreen(
                    progress = QuestProgress(),
                    canManageQuest = true,
                    onAcknowledgeVehicle = {},
                    vehicleSnapshot = snapshot(),
                    canAcknowledgeVehicle = true,
                    friendId = "friend:luna",
                    onClaimReward = { claimedQuestId = it },
                )
            }
        }

        compose.onNodeWithTag("quest-hidden-claim-modal").assertIsDisplayed()
        compose.onNodeWithText("새로운 나의 작은 친구").assertIsDisplayed()
        compose.onNodeWithTag("quest-hidden-btn-claim").assertIsDisplayed().performClick()
        assertEquals("quest_hidden_new_friend", claimedQuestId)
    }

    @Test
    @Config(qualifiers = "ko-rKR-w2560dp-h1268dp")
    fun hiddenQuestDialogCanBeDismissed() {
        compose.setContent {
            MaterialTheme {
                QuestScreen(
                    progress = QuestProgress(),
                    canManageQuest = true,
                    onAcknowledgeVehicle = {},
                    vehicleSnapshot = snapshot(),
                    canAcknowledgeVehicle = true,
                    friendId = "friend:luna",
                )
            }
        }

        compose.onNodeWithTag("quest-hidden-claim-modal").assertIsDisplayed()
        compose.onNodeWithTag("quest-hidden-btn-dismiss").assertIsDisplayed().performClick()
        compose.onNodeWithTag("quest-hidden-claim-modal").assertDoesNotExist()
    }

    private fun render(
        progress: QuestProgress,
        canManageQuest: Boolean,
    ) {
        compose.setContent {
            MaterialTheme {
                QuestScreen(progress, canManageQuest, {}, {}, {}, snapshot(), canManageQuest)
            }
        }
    }

    private fun snapshot(
        id: String = "snapshot-2",
        quality: SignalQuality = SignalQuality.VALID,
        drivingState: DrivingState = DrivingState.PARKED,
    ) = VehicleSnapshot(
        id = id,
        epoch = "epoch",
        sequence = 2,
        receivedAtMillis = 200,
        source = SignalSource.SIMULATED,
        drivingState = drivingState,
        quality = quality,
        batteryPercent = 67,
    )
}
