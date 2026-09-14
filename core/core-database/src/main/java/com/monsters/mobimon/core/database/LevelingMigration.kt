package com.monsters.mobimon.core.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_2_3: Migration =
    object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `user_profiles` (" +
                    "`id` TEXT NOT NULL, `totalPoints` INTEGER NOT NULL, " +
                    "`currentLevel` INTEGER NOT NULL, `totalDistanceKm` REAL NOT NULL, " +
                    "`selectedPetId` TEXT NOT NULL, `safeDriveDaysTotal` INTEGER NOT NULL DEFAULT 0, " +
                    "`safeDriveDaysCount` INTEGER NOT NULL DEFAULT 0, " +
                    "`tutorialClearFlag` INTEGER NOT NULL DEFAULT 0, PRIMARY KEY(`id`))",
            )
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `drive_daily_summaries` (" +
                    "`date` TEXT NOT NULL, `distanceKm` REAL NOT NULL, `safeBeltMinutes` INTEGER NOT NULL, " +
                    "`hardBrakeCount` INTEGER NOT NULL, `hardAccelCount` INTEGER NOT NULL, " +
                    "`overspeedCount` INTEGER NOT NULL, `earnedPoints` INTEGER NOT NULL, " +
                    "`bonusPoints` INTEGER NOT NULL, `safeDriveScore` INTEGER NOT NULL DEFAULT 0, " +
                    "`turnSignalOnCount` INTEGER NOT NULL DEFAULT 0, PRIMARY KEY(`date`))",
            )
        }
    }
