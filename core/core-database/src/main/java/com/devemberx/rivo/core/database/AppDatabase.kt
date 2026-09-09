package com.devemberx.rivo.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [PetProfileEntity::class, QuestRunEntity::class, QuestCompletionEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun companionDao(): CompanionDao
}
