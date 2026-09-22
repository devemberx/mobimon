package com.monsters.mobimon.feature.quest

import com.monsters.mobimon.core.navigation.AppRoute
import com.monsters.mobimon.core.presentation.CompanionAppearanceState
import com.monsters.mobimon.core.presentation.PointBalanceState

enum class QuestFilterTab { ALL, IN_PROGRESS, COMPLETED }

enum class QuestItemStatus(
    val sortPriority: Int,
) {
    CLAIMABLE(0),
    IN_PROGRESS(1),
    COMPLETED(2),
}

enum class QuestActionType { CLAIM_REWARD, VIEW_DETAIL, ALREADY_CLAIMED }

data class QuestItemUiModel(
    val id: String,
    val title: String,
    val description: String,
    val detailLine1: String,
    val detailLine2: String,
    val scheduleText: String,
    val scheduleFullText: String,
    val rewardPoints: Long,
    val status: QuestItemStatus,
    val actionType: QuestActionType,
    val targetRoute: AppRoute,
)

data class HiddenQuestUiModel(
    val id: String,
    val title: String,
    val description: String,
    val rewardPoints: Long,
)

data class QuestScreenState(
    val quests: List<QuestItemUiModel> = emptyList(),
    val hiddenQuests: List<HiddenQuestUiModel> = emptyList(),
    val appearance: CompanionAppearanceState = CompanionAppearanceState(),
    val pointBalance: PointBalanceState = PointBalanceState.Loading,
    val parkedVerified: Boolean = false,
    val canClaim: Boolean = false,
    val pendingQuestId: String? = null,
    val isLoading: Boolean = false,
    val observationFailed: Boolean = false,
    val errorMessage: String? = null,
    val rewardSuccess: QuestRewardSuccess? = null,
)
