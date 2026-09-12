package com.monsters.mobimon.core.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "point_accounts",
    primaryKeys = ["profileId"],
    foreignKeys = [
        ForeignKey(
            entity = PetProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class PointAccountEntity(
    val profileId: String,
    val balance: Long,
)

@Entity(
    tableName = "point_ledger",
    indices = [Index(value = ["profileId", "referenceKey"], unique = true)],
    foreignKeys = [
        ForeignKey(
            entity = PetProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class PointLedgerEntity(
    @androidx.room.PrimaryKey val id: String,
    val profileId: String,
    val referenceKey: String,
    val amount: Long,
    val occurredAtUtcMillis: Long,
)

@Entity(
    tableName = "point_quest_completions",
    indices = [Index(value = ["profileId", "questId", "occurrenceKey"], unique = true)],
    foreignKeys = [
        ForeignKey(
            entity = PetProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class PointQuestCompletionEntity(
    @androidx.room.PrimaryKey val id: String,
    val profileId: String,
    val questId: String,
    val occurrenceKey: String,
    val rewardPoints: Long,
    val completedAtUtcMillis: Long,
    val snapshotId: String,
    val snapshotEpoch: String,
    val snapshotSequence: Long,
    val snapshotSource: String,
)

@Entity(tableName = "cosmetic_items")
data class CosmeticItemEntity(
    @androidx.room.PrimaryKey val id: String,
    val slot: String,
    val price: Long,
    val compatibleFriendId: String?,
)

@Entity(
    tableName = "owned_cosmetics",
    primaryKeys = ["profileId", "itemId"],
    indices = [Index("itemId")],
    foreignKeys = [
        ForeignKey(
            entity = PetProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = CosmeticItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
)
data class OwnedCosmeticEntity(
    val profileId: String,
    val itemId: String,
)

@Entity(
    tableName = "equipped_cosmetics",
    primaryKeys = ["profileId", "slot"],
    indices = [Index("itemId")],
    foreignKeys = [
        ForeignKey(
            entity = PetProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = CosmeticItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
)
data class EquippedCosmeticEntity(
    val profileId: String,
    val slot: String,
    val itemId: String,
)
