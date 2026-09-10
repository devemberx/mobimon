package com.devemberx.rivo.core.database

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

data class ProfileAggregate(
    @Embedded val profile: PetProfileEntity,
    @Relation(parentColumn = "id", entityColumn = "profileId")
    val runs: List<QuestRunEntity>,
    @Relation(parentColumn = "id", entityColumn = "profileId")
    val completions: List<QuestCompletionEntity>,
)

@Dao
interface CompanionDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertProfile(profile: PetProfileEntity): Long

    @Query("SELECT * FROM pet_profiles WHERE id = :profileId")
    suspend fun profile(profileId: String): PetProfileEntity?

    @Query("SELECT * FROM pet_profiles WHERE id = :profileId")
    fun observeProfile(profileId: String): Flow<PetProfileEntity?>

    @Query("UPDATE pet_profiles SET appearance = :appearance WHERE id = :profileId")
    suspend fun updateAppearance(
        profileId: String,
        appearance: String,
    ): Int

    @Query("UPDATE pet_profiles SET totalXp = totalXp + :amount WHERE id = :profileId")
    suspend fun incrementXp(
        profileId: String,
        amount: Int,
    ): Int

    @Insert
    suspend fun insertRun(run: QuestRunEntity)

    @Query(
        "SELECT * FROM quest_runs " +
            "WHERE profileId = :profileId AND status = 'ACTIVE' LIMIT 1",
    )
    suspend fun activeRun(profileId: String): QuestRunEntity?

    @Query("SELECT * FROM quest_runs WHERE id = :runId")
    suspend fun run(runId: String): QuestRunEntity?

    @Query(
        "UPDATE quest_runs SET status = :newStatus, revision = revision + 1 " +
            "WHERE id = :runId AND profileId = :profileId " +
            "AND status = 'ACTIVE' AND revision = :expectedRevision",
    )
    suspend fun finishRun(
        runId: String,
        profileId: String,
        expectedRevision: Long,
        newStatus: String,
    ): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCompletion(completion: QuestCompletionEntity): Long

    @Query("SELECT * FROM quest_completions WHERE runId = :runId")
    suspend fun completionForRun(runId: String): QuestCompletionEntity?

    @Query(
        "SELECT * FROM quest_completions " +
            "WHERE profileId = :profileId AND type = :type",
    )
    suspend fun completionForType(
        profileId: String,
        type: String,
    ): QuestCompletionEntity?

    @Transaction
    @Query("SELECT * FROM pet_profiles WHERE id = :profileId")
    fun observeAggregate(profileId: String): Flow<ProfileAggregate?>
}
