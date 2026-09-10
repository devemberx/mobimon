package com.devemberx.rivo.feature.quest

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.devemberx.rivo.core.domain.QuestCompletion
import com.devemberx.rivo.core.domain.QuestProgress
import com.devemberx.rivo.core.domain.QuestRun
import com.devemberx.rivo.core.domain.QuestStatus
import com.devemberx.rivo.core.domain.QuestType
import com.devemberx.rivo.core.domain.SignalSource
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
    fun firstQuestStartsQ01WhileFutureQuestsRemainDisabled() {
        var started: QuestType? = null
        compose.setContent {
            MaterialTheme {
                QuestScreen(QuestProgress(), true, { started = it }, {}, {})
            }
        }

        compose.onNodeWithText("Q01 시작하기").performScrollTo().performClick()
        assertEquals(QuestType.Q01, started)
        compose.onNodeWithText("Q02 준비 중").performScrollTo().assertIsNotEnabled()
        compose.onNodeWithText("Q03 준비 중").performScrollTo().assertIsNotEnabled()
    }

    @Test
    fun unavailableVehicleDisablesStartingAQuest() {
        render(QuestProgress(), canManageQuest = false)

        compose.onNodeWithText("Q01 시작하기").performScrollTo().assertIsNotEnabled()
    }

    @Test
    fun activeQuestOpensStatusAndCancelsWithoutStartingAnotherRun() {
        var opened = false
        var cancelled = false
        compose.setContent {
            MaterialTheme {
                QuestScreen(
                    progress = QuestProgress(activeRun = activeRun()),
                    canManageQuest = true,
                    onStartQuest = {},
                    onCancelQuest = { cancelled = true },
                    onOpenVehicleInfo = { opened = true },
                )
            }
        }

        compose.onNodeWithText("차량 상태 확인하기").performScrollTo().performClick()
        compose.onNodeWithText("퀘스트 취소").performScrollTo().performClick()

        assertTrue(opened)
        assertTrue(cancelled)
        compose.onNodeWithText("Q01 시작하기").assertDoesNotExist()
    }

    @Test
    fun unavailableActiveQuestStillAllowsInspectingVehicleButNotCancellation() {
        render(QuestProgress(activeRun = activeRun()), canManageQuest = false)

        compose.onNodeWithText("차량 상태 확인하기").performScrollTo().assertIsEnabled()
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

    private fun render(
        progress: QuestProgress,
        canManageQuest: Boolean,
    ) {
        compose.setContent {
            MaterialTheme {
                QuestScreen(progress, canManageQuest, {}, {}, {})
            }
        }
    }

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
