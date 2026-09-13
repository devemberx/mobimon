package com.monsters.mobimon.core.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.SQLiteMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@SQLiteMode(SQLiteMode.Mode.NATIVE)
class PointEconomyMigrationTest {
    @Test
    fun versionOneProfileAndEvidenceSurviveWithNoInventedPointCredit() =
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val name = "point-migration-${System.nanoTime()}.db"
            val file = context.getDatabasePath(name)
            file.parentFile?.mkdirs()
            try {
                val legacy = SQLiteDatabase.openOrCreateDatabase(file, null)
                try {
                    legacy.execSQL(
                        "CREATE TABLE pet_profiles (id TEXT NOT NULL PRIMARY KEY, appearance TEXT NOT NULL, totalXp INTEGER NOT NULL)",
                    )
                    legacy.execSQL(
                        "CREATE TABLE quest_runs (id TEXT NOT NULL PRIMARY KEY, profileId TEXT NOT NULL, " +
                            "type TEXT NOT NULL, status TEXT NOT NULL, revision INTEGER NOT NULL, " +
                            "ruleVersion INTEGER NOT NULL, " +
                            "rewardXp INTEGER NOT NULL, startEpoch TEXT NOT NULL, startSequence INTEGER NOT NULL, " +
                            "startedAtMillis INTEGER NOT NULL, source TEXT NOT NULL, " +
                            "FOREIGN KEY(profileId) REFERENCES pet_profiles(id) ON DELETE CASCADE)",
                    )
                    legacy.execSQL("CREATE INDEX index_quest_runs_profileId ON quest_runs(profileId)")
                    legacy.execSQL(
                        "CREATE TABLE quest_completions (id TEXT NOT NULL PRIMARY KEY, runId TEXT NOT NULL, " +
                            "profileId TEXT NOT NULL, type TEXT NOT NULL, awardedXp INTEGER NOT NULL, " +
                            "completedAtMillis INTEGER NOT NULL, snapshotId TEXT NOT NULL, " +
                            "snapshotEpoch TEXT NOT NULL, " +
                            "snapshotSequence INTEGER NOT NULL, snapshotSource TEXT NOT NULL, " +
                            "FOREIGN KEY(runId) REFERENCES quest_runs(id) ON DELETE CASCADE, " +
                            "FOREIGN KEY(profileId) REFERENCES pet_profiles(id) ON DELETE CASCADE)",
                    )
                    legacy.execSQL("CREATE UNIQUE INDEX index_quest_completions_runId ON quest_completions(runId)")
                    legacy.execSQL(
                        "CREATE UNIQUE INDEX index_quest_completions_profileId_type ON quest_completions(profileId,type)",
                    )
                    legacy.execSQL("INSERT INTO pet_profiles VALUES ('profile', 'CREAM', 80)")
                    legacy.execSQL(
                        "INSERT INTO quest_runs VALUES ('run', 'profile', 'Q01', 'COMPLETED', 1, 1, 80, 'epoch', 1, 10000, 'REAL')",
                    )
                    legacy.execSQL(
                        "INSERT INTO quest_completions VALUES " +
                            "('done', 'run', 'profile', 'Q01', 80, 11000, 'snap', 'epoch', 2, 'REAL')",
                    )
                    legacy.version = 1
                } finally {
                    legacy.close()
                }

                val migrated =
                    Room
                        .databaseBuilder(context, AppDatabase::class.java, name)
                        .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                        .allowMainThreadQueries()
                        .build()
                try {
                    assertEquals(80, migrated.companionDao().profile("profile")?.totalXp)
                    assertNotNull(migrated.companionDao().completionForRun("run"))
                    assertEquals(0L, migrated.economyDao().account("profile")?.balance)
                    assertTrue(migrated.economyDao().ledger("profile").isEmpty())
                    assertTrue(migrated.economyDao().questCompletions("profile").isEmpty())
                    assertNotNull(migrated.economyDao().owned("profile", "friend:mobi"))
                    assertNotNull(migrated.economyDao().owned("profile", "friend:luna"))
                    assertEquals("friend:mobi", migrated.economyDao().equipped("profile", "FRIEND")?.itemId)
                } finally {
                    migrated.close()
                }
            } finally {
                context.deleteDatabase(name)
            }
        }
}
