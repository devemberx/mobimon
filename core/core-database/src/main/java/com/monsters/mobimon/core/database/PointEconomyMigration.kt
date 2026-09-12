package com.monsters.mobimon.core.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2: Migration =
    object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `point_accounts` (`profileId` TEXT NOT NULL, `balance` INTEGER NOT NULL, " +
                    "PRIMARY KEY(`profileId`), FOREIGN KEY(`profileId`) REFERENCES `pet_profiles`(`id`) " +
                    "ON UPDATE NO ACTION ON DELETE CASCADE)",
            )
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `point_ledger` (`id` TEXT NOT NULL, `profileId` TEXT NOT NULL, " +
                    "`referenceKey` TEXT NOT NULL, `amount` INTEGER NOT NULL, " +
                    "`occurredAtUtcMillis` INTEGER NOT NULL, " +
                    "PRIMARY KEY(`id`), FOREIGN KEY(`profileId`) REFERENCES `pet_profiles`(`id`) " +
                    "ON UPDATE NO ACTION ON DELETE CASCADE)",
            )
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS `index_point_ledger_profileId_referenceKey` " +
                    "ON `point_ledger` (`profileId`, `referenceKey`)",
            )
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `point_quest_completions` " +
                    "(`id` TEXT NOT NULL, `profileId` TEXT NOT NULL, " +
                    "`questId` TEXT NOT NULL, `occurrenceKey` TEXT NOT NULL, `rewardPoints` INTEGER NOT NULL, " +
                    "`completedAtUtcMillis` INTEGER NOT NULL, `snapshotId` TEXT NOT NULL, " +
                    "`snapshotEpoch` TEXT NOT NULL, " +
                    "`snapshotSequence` INTEGER NOT NULL, `snapshotSource` TEXT NOT NULL, PRIMARY KEY(`id`), " +
                    "FOREIGN KEY(`profileId`) REFERENCES `pet_profiles`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)",
            )
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS `index_point_quest_completions_profileId_questId_occurrenceKey` " +
                    "ON `point_quest_completions` (`profileId`, `questId`, `occurrenceKey`)",
            )
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `cosmetic_items` (`id` TEXT NOT NULL, `slot` TEXT NOT NULL, " +
                    "`price` INTEGER NOT NULL, `compatibleFriendId` TEXT, PRIMARY KEY(`id`))",
            )
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `owned_cosmetics` (`profileId` TEXT NOT NULL, `itemId` TEXT NOT NULL, " +
                    "PRIMARY KEY(`profileId`, `itemId`), " +
                    "FOREIGN KEY(`profileId`) REFERENCES `pet_profiles`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE, " +
                    "FOREIGN KEY(`itemId`) REFERENCES `cosmetic_items`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT)",
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_owned_cosmetics_itemId` ON `owned_cosmetics` (`itemId`)")
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `equipped_cosmetics` (`profileId` TEXT NOT NULL, `slot` TEXT NOT NULL, " +
                    "`itemId` TEXT NOT NULL, PRIMARY KEY(`profileId`, `slot`), " +
                    "FOREIGN KEY(`profileId`) REFERENCES `pet_profiles`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE, " +
                    "FOREIGN KEY(`itemId`) REFERENCES `cosmetic_items`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT)",
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_equipped_cosmetics_itemId` ON `equipped_cosmetics` (`itemId`)",
            )

            db.execSQL("INSERT OR IGNORE INTO `cosmetic_items` VALUES ('friend:mobi', 'FRIEND', 0, NULL)")
            db.execSQL("INSERT OR IGNORE INTO `cosmetic_items` VALUES ('friend:luna', 'FRIEND', 0, NULL)")
            db.execSQL("INSERT OR IGNORE INTO `point_accounts` SELECT `id`, 0 FROM `pet_profiles`")
            db.execSQL("INSERT OR IGNORE INTO `owned_cosmetics` SELECT `id`, 'friend:mobi' FROM `pet_profiles`")
            db.execSQL("INSERT OR IGNORE INTO `owned_cosmetics` SELECT `id`, 'friend:luna' FROM `pet_profiles`")
            db.execSQL(
                "INSERT OR IGNORE INTO `equipped_cosmetics` SELECT `id`, 'FRIEND', 'friend:mobi' FROM `pet_profiles`",
            )
        }
    }
