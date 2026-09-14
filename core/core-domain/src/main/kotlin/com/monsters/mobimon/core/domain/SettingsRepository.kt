package com.monsters.mobimon.core.domain

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<CompanionSettings>

    suspend fun setShowOnVehicleHome(enabled: Boolean): WriteResult

    suspend fun setReducedMotion(enabled: Boolean): WriteResult

    suspend fun setLauncherCharacterEnabled(enabled: Boolean): WriteResult
}
