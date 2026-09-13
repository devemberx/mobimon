package com.monsters.mobimon.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LevelingDao {
    @Query("SELECT * FROM user_profiles WHERE id = :id LIMIT 1")
    fun observeUserProfile(id: String): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profiles WHERE id = :id LIMIT 1")
    suspend fun getUserProfile(id: String): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUserProfile(profile: UserProfileEntity)

    @Query(
        "UPDATE user_profiles " +
            "SET totalPoints = :totalPoints, currentLevel = :currentLevel, totalDistanceKm = :totalDistanceKm " +
            "WHERE id = :id",
    )
    suspend fun updateUserProgress(
        id: String,
        totalPoints: Long,
        currentLevel: Int,
        totalDistanceKm: Float,
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDriveSummary(summary: DriveDailySummaryEntity)

    @Query("SELECT * FROM drive_daily_summaries ORDER BY date DESC LIMIT :limit")
    fun observeRecentDriveSummaries(limit: Int): Flow<List<DriveDailySummaryEntity>>

    @Query("SELECT * FROM drive_daily_summaries WHERE date = :date LIMIT 1")
    suspend fun getDriveSummary(date: String): DriveDailySummaryEntity?
}
