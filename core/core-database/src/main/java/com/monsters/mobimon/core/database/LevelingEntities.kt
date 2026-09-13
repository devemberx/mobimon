package com.monsters.mobimon.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey val id: String,
    val totalPoints: Long,
    val currentLevel: Int,
    val totalDistanceKm: Float,
    val selectedPetId: String,
)

@Entity(tableName = "drive_daily_summaries")
data class DriveDailySummaryEntity(
    @PrimaryKey val date: String,
    val distanceKm: Float,
    val safeBeltMinutes: Int,
    val hardBrakeCount: Int,
    val hardAccelCount: Int,
    val overspeedCount: Int,
    val earnedPoints: Long,
    val bonusPoints: Long,
)
