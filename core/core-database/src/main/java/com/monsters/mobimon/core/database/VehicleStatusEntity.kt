package com.monsters.mobimon.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vehicle_status")
data class VehicleStatusEntity(
    @PrimaryKey val id: String = "current",
    val drivingState: String,
    val isCharging: Boolean,
    val batteryPercent: Int,
    val speed: Int,
    val gear: String,
    val isEngineOn: Boolean,
    val isMoving: Boolean,
    val isDistracted: Boolean = false,
    val isDrowsy: Boolean = false,
    val attentionLevel: Int = 100,
    val isEmergencyBraking: Boolean = false,
    val distanceToFrontVehicle: Int = 50,
    val outsideTemperature: Int = 20,
    val isRaining: Boolean = false,
    val washerFluidLevel: Int = 100,
    val isEngineWarning: Boolean = false,
    val tirePressureStatus: String = "OK",
    val isNavigating: Boolean = false,
    val distanceToDestination: Int = 0,
    val updatedAtMillis: Long = System.currentTimeMillis(),
)
