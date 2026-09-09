package com.devemberx.rivo.core.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "pet_profiles")
data class PetProfileEntity(
    @PrimaryKey val id: String,
    val appearance: String,
    val totalXp: Int,
)

@Entity(
    tableName = "quest_runs",
    foreignKeys = [
        ForeignKey(
            entity = PetProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("profileId")],
)
data class QuestRunEntity(
    @PrimaryKey val id: String,
    val profileId: String,
    val type: String,
    val status: String,
    val revision: Long,
    val ruleVersion: Int,
    val rewardXp: Int,
    val startEpoch: String,
    val startSequence: Long,
    val startedAtMillis: Long,
    val source: String,
)

@Entity(
    tableName = "quest_completions",
    foreignKeys = [
        ForeignKey(
            entity = QuestRunEntity::class,
            parentColumns = ["id"],
            childColumns = ["runId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = PetProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["runId"], unique = true),
        Index(value = ["profileId", "type"], unique = true),
    ],
)
data class QuestCompletionEntity(
    @PrimaryKey val id: String,
    val runId: String,
    val profileId: String,
    val type: String,
    val awardedXp: Int,
    val completedAtMillis: Long,
    val snapshotId: String,
    val snapshotEpoch: String,
    val snapshotSequence: Long,
    val snapshotSource: String,
)
