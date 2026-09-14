package com.monsters.mobimon.core.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_3_4: Migration =
    object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.addMissingFields(
                "user_profiles",
                listOf("safeDriveDaysTotal", "safeDriveDaysCount", "tutorialClearFlag"),
            )
            db.addMissingFields("drive_daily_summaries", listOf("safeDriveScore", "turnSignalOnCount"))
        }
    }

private fun SupportSQLiteDatabase.addMissingFields(
    table: String,
    fields: List<String>,
) {
    // Both the original and expanded leveling tables were shipped under version 3.
    val existing =
        query("PRAGMA table_info(`$table`)").use { cursor ->
            val nameIndex = cursor.getColumnIndexOrThrow("name")
            buildSet {
                while (cursor.moveToNext()) add(cursor.getString(nameIndex))
            }
        }
    fields.filterNot { it in existing }.forEach { field ->
        execSQL("ALTER TABLE `$table` ADD COLUMN `$field` INTEGER NOT NULL DEFAULT 0")
    }
}
