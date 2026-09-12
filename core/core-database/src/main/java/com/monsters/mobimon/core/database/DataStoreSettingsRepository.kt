package com.monsters.mobimon.core.database

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.monsters.mobimon.core.domain.CompanionSettings
import com.monsters.mobimon.core.domain.SettingsRepository
import com.monsters.mobimon.core.domain.WriteResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.util.concurrent.CancellationException

class DataStoreSettingsRepository(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository {
    override val settings: Flow<CompanionSettings> =
        dataStore.data.map { preferences ->
            CompanionSettings(
                showOnVehicleHome = preferences[SHOW_ON_VEHICLE_HOME] ?: true,
                reducedMotion = preferences[REDUCED_MOTION] ?: false,
                launcherCharacterEnabled = preferences[LAUNCHER_CHARACTER_ENABLED] ?: false,
            )
        }

    override suspend fun setShowOnVehicleHome(enabled: Boolean): WriteResult =
        writePreference(SHOW_ON_VEHICLE_HOME, enabled)

    override suspend fun setReducedMotion(enabled: Boolean): WriteResult = writePreference(REDUCED_MOTION, enabled)

    override suspend fun setLauncherCharacterEnabled(enabled: Boolean): WriteResult =
        writePreference(LAUNCHER_CHARACTER_ENABLED, enabled)

    private suspend fun writePreference(
        key: Preferences.Key<Boolean>,
        value: Boolean,
    ): WriteResult =
        try {
            dataStore.edit { preferences -> preferences[key] = value }
            WriteResult.Success
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: IOException) {
            WriteResult.Failure
        }

    private companion object {
        val SHOW_ON_VEHICLE_HOME = booleanPreferencesKey("show_on_vehicle_home")
        val REDUCED_MOTION = booleanPreferencesKey("reduced_motion")
        val LAUNCHER_CHARACTER_ENABLED = booleanPreferencesKey("launcher_character_enabled")
    }
}
