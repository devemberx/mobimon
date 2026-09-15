package com.monsters.mobimon.core.domain

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<CompanionSettings>

    suspend fun setReducedMotion(enabled: Boolean): WriteResult

    suspend fun setLauncherCharacterEnabled(enabled: Boolean): WriteResult

    suspend fun setDebugModeEnabled(enabled: Boolean): WriteResult
}
