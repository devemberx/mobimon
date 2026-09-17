package com.monsters.mobimon.feature.quest

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.monsters.mobimon.core.domain.DrivingState
import com.monsters.mobimon.core.domain.QuestCompletion
import com.monsters.mobimon.core.domain.QuestProgress
import com.monsters.mobimon.core.domain.QuestRun
import com.monsters.mobimon.core.domain.QuestStatus
import com.monsters.mobimon.core.domain.QuestType
import com.monsters.mobimon.core.domain.SignalQuality
import com.monsters.mobimon.core.domain.SignalSource
import com.monsters.mobimon.core.domain.VehicleSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
    fun firstQuestStartsQ01WithoutAdvertisingLegacyFutureQuests() {
        var started: QuestType? = null
        compose.setContent {
            MaterialTheme {
                QuestScreen(QuestProgress(), true, { started = it }, {}, {}, snapshot(), true)
            }
        }

        compose.onNodeWithText("Q01 시작하기").performScrollTo().performClick()
        assertEquals(QuestType.Q01, started)
        compose.onNodeWithText("Q02", substring = true).assertDoesNotExist()
        compose.onNodeWithText("Q03", substring = true).assertDoesNotExist()
    }

    @Test
    fun unavailableVehicleDisablesStartingAQuest() {
        render(QuestProgress(), canManageQuest = false)

        compose.onNodeWithText("Q01 시작하기").performScrollTo().assertIsNotEnabled()
    }

    @Test
    fun activeQuestAcknowledgesDisplayedSnapshotAndCancelsWithoutStartingAnotherRun() {
        var acknowledged: String? = null
        var cancelled = false
        compose.setContent {
            MaterialTheme {
                QuestScreen(
                    progress = QuestProgress(activeRun = activeRun()),
                    canManageQuest = true,
                    onStartQuest = {},
                    onCancelQuest = { cancelled = true },
                    onAcknowledgeVehicle = { acknowledged = it },
                    vehicleSnapshot = snapshot(id = "displayed"),
                    canAcknowledgeVehicle = true,
                )
            }
        }

        compose.onNodeWithText("상태 확인 완료").performScrollTo().performClick()
        compose.onNodeWithText("퀘스트 취소").performScrollTo().performClick()

        assertEquals("displayed", acknowledged)
        assertTrue(cancelled)
        compose.onNodeWithText("Q01 시작하기").assertDoesNotExist()
    }

    @Test
    fun unavailableActiveQuestStillAllowsInspectingVehicleButNotCancellation() {
        render(QuestProgress(activeRun = activeRun()), canManageQuest = false)

        compose.onNodeWithText("상태 확인 완료").performScrollTo().assertIsNotEnabled()
        compose.onNodeWithText("퀘스트 취소").performScrollTo().assertIsNotEnabled()
    }

    @Test
    fun committedCompletionShowsRewardAndRemovesStartAndCancel() {
        val completion = QuestCompletion("result", "run", "profile", QuestType.Q01, 80, 500, "snapshot")
        render(QuestProgress(completions = listOf(completion)), canManageQuest = true)

        compose.onNodeWithText("80 XP를 받았어요").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Q01 시작하기").assertDoesNotExist()
        compose.onNodeWithText("퀘스트 취소").assertDoesNotExist()
    }

    @Test
    fun drivingQuestsAreRenderedInQuestList() {
        compose.setContent {
            MaterialTheme {
                QuestScreen(QuestProgress(), canManageQuest = true, {}, {}, {}, snapshot(), true)
            }
        }

        compose.onNodeWithText("안전벨트 착용 상태로 주행 시작").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("안전 주행 종료").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("100km 주행 완료").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("급가속/급제동/과속 없음 보너스").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("5일 연속 안전 주행").performScrollTo().assertIsDisplayed()
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

    private fun activeRun() =
        QuestRun(
            id = "run",
            profileId = "profile",
            type = QuestType.Q01,
            status = QuestStatus.ACTIVE,
            revision = 0,
            ruleVersion = 1,
            rewardXp = 80,
            startEpoch = "epoch",
            startSequence = 1,
            startedAtMillis = 100,
            source = SignalSource.SIMULATED,
        )
}
