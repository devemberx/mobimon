package com.monsters.mobimon.di.features

import android.content.SharedPreferences
import com.monsters.mobimon.feature.vehicle.VehicleCardSelectionStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class VehicleCardSelectionPreferences(
    private val preferences: SharedPreferences,
) : VehicleCardSelectionStore {
    private val state =
        MutableStateFlow(
            VehicleCardSelectionStore.validOrDefaults(
                preferences.getString(KEY, null)?.split(",") ?: emptyList(),
            ),
        )
    override val selectedCards: StateFlow<List<String>> = state

    override fun save(cards: List<String>) {
        val valid = VehicleCardSelectionStore.validOrDefaults(cards)
        preferences.edit().putString(KEY, valid.joinToString(",")).apply()
        state.value = valid
    }

    private companion object {
        const val KEY = "slots"
    }
}
