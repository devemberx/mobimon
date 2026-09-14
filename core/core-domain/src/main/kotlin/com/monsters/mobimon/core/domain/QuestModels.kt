package com.monsters.mobimon.core.domain

enum class QuestType { Q01, Q02, Q03 }

enum class QuestStatus { ACTIVE, CANCELLED, COMPLETED }

data class QuestRun(
    val id: String,
    val profileId: String,
    val type: QuestType,
    val status: QuestStatus,
    val revision: Long,
    val ruleVersion: Int,
    val rewardXp: Int,
    val startEpoch: String,
    val startSequence: Long,
    val startedAtMillis: Long,
    val source: SignalSource,
)

data class QuestCompletion(
    val id: String,
    val runId: String,
    val profileId: String,
    val type: QuestType,
    val awardedXp: Int,
    val completedAtMillis: Long,
    val snapshotId: String,
)

data class QuestProgress(
    val activeRun: QuestRun? = null,
    val completions: List<QuestCompletion> = emptyList(),
)

enum class QuestRejection {
    NOT_PARKED,
    UNAVAILABLE,
    STALE,
    WRONG_SOURCE,
    WRONG_EPOCH,
    BEFORE_START,
    RUN_CHANGED,
    UNSUPPORTED_QUEST,
    INVALID_SIGNAL,
    APP_USE_RESTRICTED,
}

sealed interface QuestCommandResult {
    data class Started(
        val run: QuestRun,
    ) : QuestCommandResult

    data object Cancelled : QuestCommandResult

    data object AlreadyCompleted : QuestCommandResult

    data object AlreadyActive : QuestCommandResult

    data class Rejected(
        val reason: QuestRejection,
    ) : QuestCommandResult

    data object StorageFailure : QuestCommandResult
}

sealed interface RewardResult {
    data class Applied(
        val completion: QuestCompletion,
    ) : RewardResult

    data object AlreadyAwarded : RewardResult

    data class Rejected(
        val reason: QuestRejection,
    ) : RewardResult

    data object StorageFailure : RewardResult
}
