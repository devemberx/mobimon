package com.monsters.mobimon.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [VehicleStatusEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class VehicleDatabase : RoomDatabase() {
    abstract fun vehicleDao(): VehicleDao
}
