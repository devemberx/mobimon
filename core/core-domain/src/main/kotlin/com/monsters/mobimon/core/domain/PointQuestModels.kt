package com.monsters.mobimon.core.domain

sealed interface PointQuestSchedule {
    data object OneTime : PointQuestSchedule

    data class Daily(
        val resetZoneId: String,
    ) : PointQuestSchedule

    data class Weekly(
        val resetZoneId: String,
    ) : PointQuestSchedule

    data class PerDrive(
        val driveId: String,
    ) : PointQuestSchedule

    data class CappedDaily(
        val resetZoneId: String,
        val maxPerDay: Int,
        val currentCount: Int = 1,
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

object DrivingQuestIds {
    const val SEATBELT = "quest_seatbelt"
    const val SAFE_DRIVE = "quest_safe_drive"
    const val DISTANCE_100KM = "quest_100km"
    const val CLEAN_DRIVE = "quest_clean_drive"
    const val FIRST_DRIVE = "quest_first_drive"
    const val FOCUS_DRIVE = "quest_focus_drive"
    const val LANE_KEEP = "quest_lane_keep"
    const val MAINTENANCE = "quest_maintenance"
    const val TURN_SIGNAL = "quest_turn_signal"
    const val SAFE_5DAYS = "quest_5days_safe"
}

class DefaultPointQuestCatalog(
    private val resetZoneId: String = "Asia/Seoul",
) : PointQuestCatalog {
    private val definitions =
        mapOf(
            DrivingQuestIds.SEATBELT to
                PointQuestDefinition(
                    id = DrivingQuestIds.SEATBELT,
                    rewardPoints = 5L,
                    schedule = PointQuestSchedule.PerDrive("default"),
                ),
            DrivingQuestIds.SAFE_DRIVE to
                PointQuestDefinition(
                    id = DrivingQuestIds.SAFE_DRIVE,
                    rewardPoints = 20L,
                    schedule = PointQuestSchedule.PerDrive("default"),
                ),
            DrivingQuestIds.DISTANCE_100KM to
                PointQuestDefinition(
                    id = DrivingQuestIds.DISTANCE_100KM,
                    rewardPoints = 25L,
                    schedule = PointQuestSchedule.OneTime,
                ),
            DrivingQuestIds.CLEAN_DRIVE to
                PointQuestDefinition(
                    id = DrivingQuestIds.CLEAN_DRIVE,
                    rewardPoints = 15L,
                    schedule = PointQuestSchedule.PerDrive("default"),
                ),
            DrivingQuestIds.FIRST_DRIVE to
                PointQuestDefinition(
                    id = DrivingQuestIds.FIRST_DRIVE,
                    rewardPoints = 10L,
                    schedule = PointQuestSchedule.Daily(resetZoneId),
                ),
            DrivingQuestIds.FOCUS_DRIVE to
                PointQuestDefinition(
                    id = DrivingQuestIds.FOCUS_DRIVE,
                    rewardPoints = 10L,
                    schedule = PointQuestSchedule.PerDrive("default"),
                ),
            DrivingQuestIds.LANE_KEEP to
                PointQuestDefinition(
                    id = DrivingQuestIds.LANE_KEEP,
                    rewardPoints = 10L,
                    schedule = PointQuestSchedule.PerDrive("default"),
                ),
            DrivingQuestIds.MAINTENANCE to
                PointQuestDefinition(
                    id = DrivingQuestIds.MAINTENANCE,
                    rewardPoints = 10L,
                    schedule = PointQuestSchedule.OneTime,
                ),
            DrivingQuestIds.TURN_SIGNAL to
                PointQuestDefinition(
                    id = DrivingQuestIds.TURN_SIGNAL,
                    rewardPoints = 1L,
                    schedule = PointQuestSchedule.CappedDaily(resetZoneId, maxPerDay = 10),
                ),
            DrivingQuestIds.SAFE_5DAYS to
                PointQuestDefinition(
                    id = DrivingQuestIds.SAFE_5DAYS,
                    rewardPoints = 50L,
                    schedule = PointQuestSchedule.Weekly(resetZoneId),
                ),
        )

    override fun find(questId: String): PointQuestDefinition? = definitions[questId]
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
