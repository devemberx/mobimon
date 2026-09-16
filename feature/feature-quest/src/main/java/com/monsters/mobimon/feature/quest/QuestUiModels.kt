package com.monsters.mobimon.feature.quest

import com.monsters.mobimon.core.domain.QuestType
import com.monsters.mobimon.core.navigation.AppRoute

enum class QuestFilterTab {
    ALL,
    IN_PROGRESS,
    COMPLETED,
}

enum class QuestItemStatus {
    IN_PROGRESS,
    CLAIMABLE,
    COMPLETED,
}

enum class QuestActionType {
    CLAIM_REWARD,
    CHAT,
    VIEW_DETAIL,
    ALREADY_CLAIMED,
}

data class QuestItemUiModel(
    val id: String,
    val type: QuestType?,
    val title: String,
    val description: String,
    val detailLine1: String,
    val detailLine2: String,
    val scheduleText: String = "1회",
    val scheduleFullText: String = "한 번만 완료",
    val rewardPoints: Long = 50,
    val status: QuestItemStatus = QuestItemStatus.IN_PROGRESS,
    val actionType: QuestActionType = QuestActionType.VIEW_DETAIL,
    val completedDate: String? = null,
    val targetRoute: AppRoute? = null,
)

data class RewardSuccessModalState(
    val points: Long,
    val questTitle: String,
)
