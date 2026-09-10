package com.monsters.mobimon.core.domain

class QuestEvaluator(
    private val maxAgeMillis: Long,
) {
    init {
        require(maxAgeMillis >= 0) { "Snapshot maximum age cannot be negative" }
    }

    fun validateSnapshot(
        snapshot: VehicleSnapshot,
        source: SignalSource,
        nowMillis: Long,
    ): QuestRejection? {
        if (
            snapshot.id.isBlank() ||
            snapshot.epoch.isBlank() ||
            snapshot.sequence < 0 ||
            snapshot.receivedAtMillis < 0 ||
            nowMillis < 0 ||
            snapshot.receivedAtMillis > nowMillis ||
            snapshot.batteryPercent?.let { it !in 0..100 } == true
        ) {
            return QuestRejection.INVALID_SIGNAL
        }
        if (snapshot.source != source) return QuestRejection.WRONG_SOURCE

        when (snapshot.quality) {
            SignalQuality.UNAVAILABLE -> return QuestRejection.UNAVAILABLE
            SignalQuality.STALE -> return QuestRejection.STALE
            SignalQuality.VALID -> Unit
        }

        if (snapshot.drivingState != DrivingState.PARKED) return QuestRejection.NOT_PARKED
        if (nowMillis - snapshot.receivedAtMillis > maxAgeMillis) return QuestRejection.STALE

        return null
    }

    fun evaluate(
        run: QuestRun,
        expectedRevision: Long,
        snapshot: VehicleSnapshot,
        source: SignalSource,
        nowMillis: Long,
    ): QuestRejection? {
        if (run.type != QuestType.Q01 || run.ruleVersion != Q01_RULE_VERSION || run.rewardXp != Q01_REWARD_XP) {
            return QuestRejection.UNSUPPORTED_QUEST
        }
        if (run.status != QuestStatus.ACTIVE || run.revision != expectedRevision) {
            return QuestRejection.RUN_CHANGED
        }
        if (run.startEpoch.isBlank() || run.startSequence < 0 || run.startedAtMillis < 0) {
            return QuestRejection.INVALID_SIGNAL
        }
        if (run.source != source) return QuestRejection.WRONG_SOURCE

        validateSnapshot(snapshot, source, nowMillis)?.let { return it }

        if (snapshot.epoch != run.startEpoch) return QuestRejection.WRONG_EPOCH
        if (snapshot.sequence <= run.startSequence || snapshot.receivedAtMillis < run.startedAtMillis) {
            return QuestRejection.BEFORE_START
        }

        return null
    }

    private companion object {
        const val Q01_RULE_VERSION = 1
        const val Q01_REWARD_XP = 80
    }
}
