package com.monsters.mobimon.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicleStatus(status: VehicleStatusEntity)

    @Query("SELECT * FROM vehicle_status WHERE id = :id")
    fun observeVehicleStatus(id: String = "current"): Flow<VehicleStatusEntity?>

    @Query("SELECT * FROM vehicle_status WHERE id = :id")
    suspend fun getVehicleStatus(id: String = "current"): VehicleStatusEntity?
}
