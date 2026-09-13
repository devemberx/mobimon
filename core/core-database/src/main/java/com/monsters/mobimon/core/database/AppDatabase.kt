package com.monsters.mobimon.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserProfileEntity::class,
        PetProfileEntity::class,
        DriveDailySummaryEntity::class,
        QuestRunEntity::class,
        QuestCompletionEntity::class,
        PointAccountEntity::class,
        PointLedgerEntity::class,
        PointQuestCompletionEntity::class,
        CosmeticItemEntity::class,
        OwnedCosmeticEntity::class,
        EquippedCosmeticEntity::class,
    ],
    version = 3,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun companionDao(): CompanionDao

    abstract fun economyDao(): PointEconomyDao
}
