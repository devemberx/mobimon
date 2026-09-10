package com.devemberx.rivo.core.domain

enum class SignalSource { REAL, SIMULATED }

enum class DrivingState { UNKNOWN, PARKED, MOVING }

enum class SignalQuality { UNAVAILABLE, VALID, STALE }

enum class PetAppearance { GOLDEN, CREAM }

enum class QuestType { Q01, Q02, Q03 }

enum class QuestStatus { ACTIVE, CANCELLED, COMPLETED }

data class VehicleSnapshot(
    val id: String,
    val epoch: String,
    val sequence: Long,
    val receivedAtMillis: Long,
    val source: SignalSource,
    val drivingState: DrivingState,
    val quality: SignalQuality,
    val batteryPercent: Int? = null,
)

data class PetProfile(
    val id: String,
    val appearance: PetAppearance = PetAppearance.GOLDEN,
    val totalXp: Int = 0,
)

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

data class CompanionSettings(
    val showOnVehicleHome: Boolean = true,
    val reducedMotion: Boolean = false,
)

fun interface Clock {
    fun nowMillis(): Long
}

fun interface IdGenerator {
    fun nextId(): String
}

data class ProgressionIdentity(
    val profileId: String,
    val source: SignalSource,
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
}

sealed interface WriteResult {
    data object Success : WriteResult

    data object Failure : WriteResult
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
