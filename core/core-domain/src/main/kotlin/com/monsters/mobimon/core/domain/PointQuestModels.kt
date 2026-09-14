package com.monsters.mobimon.core.domain

sealed interface PointQuestSchedule {
    data object OneTime : PointQuestSchedule

    data class Daily(
        val resetZoneId: String,
    ) : PointQuestSchedule
}

data class PointQuestDefinition(
    val id: String,
    val rewardPoints: Long,
    val schedule: PointQuestSchedule,
)

fun interface PointQuestCatalog {
    fun find(questId: String): PointQuestDefinition?
}

sealed interface PointAwardResult {
    data class Awarded(
        val points: Long,
        val resultingBalance: Long,
        val occurrenceKey: String,
    ) : PointAwardResult

    data object AlreadyAwarded : PointAwardResult

    data object QuestUnavailable : PointAwardResult

    data object InteractionRestricted : PointAwardResult

    data object EvidenceChanged : PointAwardResult

    data object StorageFailure : PointAwardResult
}
