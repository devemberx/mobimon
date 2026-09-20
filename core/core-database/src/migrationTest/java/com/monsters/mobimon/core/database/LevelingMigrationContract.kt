package com.monsters.mobimon.core.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test

abstract class LevelingMigrationContract {
    @Test
    fun originalVersionThreeRetainsRecordsAndDefaultsMissingFields() =
        verifyUpgrade("legacy-schema-3.json", expanded = false)

    @Test
    fun expandedVersionThreeRetainsNonzeroFields() =
        verifyUpgrade("com.monsters.mobimon.core.database.AppDatabase/3.json", expanded = true)

    private fun verifyUpgrade(
        schemaPath: String,
        expanded: Boolean,
    ) = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "leveling-migration-${System.nanoTime()}.db"
        val file = context.getDatabasePath(name)
        file.parentFile?.mkdirs()
        try {
            val schema =
                InstrumentationRegistry.getInstrumentation().context.assets.open(schemaPath).use {
                    JSONObject(it.bufferedReader().readText()).getJSONObject("database")
                }
            SQLiteDatabase.openOrCreateDatabase(file, null).use { database ->
                database.setForeignKeyConstraintsEnabled(true)
                createSchema(database, schema)
                seedRecords(database, expanded)
                database.version = schema.getInt("version")
            }
            repeat(2) {
                val database =
                    Room
                        .databaseBuilder(context, AppDatabase::class.java, name)
                        .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                        .allowMainThreadQueries()
                        .build()
                try {
                    assertEquals(4, database.openHelper.writableDatabase.version)
                    assertEquals(
                        UserProfileEntity(
                            "profile",
                            1200,
                            3,
                            42.5f,
                            "friend:luna",
                            if (expanded) 14 else 0,
                            if (expanded) 4 else 0,
                            expanded,
                        ),
                        database.levelingDao().getUserProfile("profile"),
                    )
                    assertEquals(
                        DriveDailySummaryEntity(
                            "2026-09-14",
                            12.5f,
                            30,
                            1,
                            2,
                            3,
                            75,
                            25,
                            if (expanded) 91 else 0,
                            if (expanded) 8 else 0,
                        ),
                        database.levelingDao().getDriveSummary("2026-09-14"),
                    )
                    assertEquals(
                        PetProfileEntity("profile", "CREAM", 80),
                        database.companionDao().profile("profile"),
                    )
                    assertEquals(
                        QuestCompletionEntity(
                            "done",
                            "run",
                            "profile",
                            "Q01",
                            80,
                            11000,
                            "snapshot",
                            "epoch",
                            2,
                            "REAL",
                        ),
                        database.companionDao().completionForRun("run"),
                    )
                    assertEquals(PointAccountEntity("profile", 450), database.economyDao().account("profile"))
                    assertEquals(
                        listOf(PointLedgerEntity("ledger", "profile", "quest:test:once", 450, 12000)),
                        database.economyDao().ledger("profile"),
                    )
                    assertEquals(
                        listOf(
                            PointQuestCompletionEntity(
                                "points",
                                "profile",
                                "test",
                                "once",
                                450,
                                12000,
                                "snapshot",
                                "epoch",
                                2,
                                "REAL",
                            ),
                        ),
                        database.economyDao().questCompletions("profile"),
                    )
                    assertEquals(
                        OwnedCosmeticEntity("profile", "friend:luna"),
                        database.economyDao().owned("profile", "friend:luna"),
                    )
                    assertEquals(
                        EquippedCosmeticEntity("profile", "FRIEND", "friend:luna"),
                        database.economyDao().equipped("profile", "FRIEND"),
                    )
                } finally {
                    database.close()
                }
            }
        } finally {
            context.deleteDatabase(name)
        }
    }

    private fun createSchema(
        database: SQLiteDatabase,
        schema: JSONObject,
    ) {
        val entities = schema.getJSONArray("entities")
        for (index in 0 until entities.length()) {
            val entity = entities.getJSONObject(index)
            val table = entity.getString("tableName")
            database.execSQL(entity.getString("createSql").replace("\${TABLE_NAME}", table))
            val indices = entity.getJSONArray("indices")
            for (indexIndex in 0 until indices.length()) {
                database.execSQL(
                    indices.getJSONObject(indexIndex).getString("createSql").replace("\${TABLE_NAME}", table),
                )
            }
        }
        val setup = schema.getJSONArray("setupQueries")
        for (index in 0 until setup.length()) database.execSQL(setup.getString(index))
    }

    private fun seedRecords(
        database: SQLiteDatabase,
        expanded: Boolean,
    ) {
        database.execSQL(
            "INSERT INTO user_profiles (id, totalPoints, currentLevel, totalDistanceKm, selectedPetId" +
                (if (expanded) ", safeDriveDaysTotal, safeDriveDaysCount, tutorialClearFlag" else "") +
                ") VALUES ('profile', 1200, 3, 42.5, 'friend:luna'" + (if (expanded) ", 14, 4, 1" else "") + ")",
        )
        database.execSQL(
            "INSERT INTO drive_daily_summaries (date, distanceKm, safeBeltMinutes, hardBrakeCount, " +
                "hardAccelCount, overspeedCount, earnedPoints, bonusPoints" +
                (if (expanded) ", safeDriveScore, turnSignalOnCount" else "") +
                ") VALUES ('2026-09-14', 12.5, 30, 1, 2, 3, 75, 25" + (if (expanded) ", 91, 8" else "") + ")",
        )
        database.execSQL("INSERT INTO pet_profiles VALUES ('profile', 'CREAM', 80)")
        database.execSQL(
            "INSERT INTO quest_runs VALUES ('run', 'profile', 'Q01', 'COMPLETED', 1, 1, 80, 'epoch', 1, 10000, 'REAL')",
        )
        database.execSQL(
            "INSERT INTO quest_completions VALUES ('done', 'run', 'profile', 'Q01', 80, 11000, 'snapshot', 'epoch', 2, 'REAL')",
        )
        database.execSQL("INSERT INTO point_accounts VALUES ('profile', 450)")
        database.execSQL("INSERT INTO point_ledger VALUES ('ledger', 'profile', 'quest:test:once', 450, 12000)")
        database.execSQL(
            "INSERT INTO point_quest_completions VALUES ('points', 'profile', 'test', 'once', 450, 12000, 'snapshot', 'epoch', 2, 'REAL')",
        )
        database.execSQL("INSERT INTO cosmetic_items VALUES ('friend:luna', 'FRIEND', 0, NULL)")
        database.execSQL("INSERT INTO owned_cosmetics VALUES ('profile', 'friend:luna')")
        database.execSQL("INSERT INTO equipped_cosmetics VALUES ('profile', 'FRIEND', 'friend:luna')")
    }
}
