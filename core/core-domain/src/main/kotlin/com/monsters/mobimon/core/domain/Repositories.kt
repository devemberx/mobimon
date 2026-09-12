package com.monsters.mobimon.core.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface PetRepository {
    val profile: Flow<PetProfile>

    suspend fun initialize()

    suspend fun setAppearance(appearance: PetAppearance): WriteResult
}

interface QuestRepository {
    val progress: Flow<QuestProgress>

    suspend fun start(
        type: QuestType,
        snapshot: VehicleSnapshot,
    ): QuestCommandResult

    suspend fun cancel(
        runId: String,
        expectedRevision: Long,
        snapshot: VehicleSnapshot,
    ): QuestCommandResult
}

interface RewardRepository {
    suspend fun complete(
        runId: String,
        expectedRevision: Long,
        snapshot: VehicleSnapshot,
    ): RewardResult
}

interface SettingsRepository {
    val settings: Flow<CompanionSettings>

    suspend fun setShowOnVehicleHome(enabled: Boolean): WriteResult

    suspend fun setReducedMotion(enabled: Boolean): WriteResult

    suspend fun setLauncherCharacterEnabled(enabled: Boolean): WriteResult
}

interface VehicleRepository {
    val snapshots: StateFlow<VehicleSnapshot>

    fun start()

    fun stop()
}

fun interface CurrentVehicleEvidence {
    fun snapshot(): VehicleSnapshot
}

enum class AppUseState { ALLOWED, RESTRICTED, UNAVAILABLE }

fun interface CurrentAppUse {
    fun state(): AppUseState
}
