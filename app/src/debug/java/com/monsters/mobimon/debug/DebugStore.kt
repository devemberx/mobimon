package com.monsters.mobimon.debug

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class DebugVssState(
    val isDistracted: Boolean = false,
    val isDrowsy: Boolean = false,
    val attentionLevel: Int = 100,
    val isEmergencyBraking: Boolean = false,
    val distanceToFrontVehicle: Int = 50,
    val isCharging: Boolean = false,
    val batteryPercent: Int = 72,
    val outsideTemperature: Int = 20,
    val isRaining: Boolean = false,
    val washerFluidLevel: Int = 100,
    val isEngineWarning: Boolean = false,
    val tirePressureStatus: String = "OK",
    val isMoving: Boolean = false,
    val speed: Int = 0,
    val gear: String = "P",
    val isNavigating: Boolean = false,
    val distanceToDestination: Int = 0,
    val isEngineOn: Boolean = false
)

interface DebugVssProvider {
    val state: StateFlow<DebugVssState>
}

@Singleton
class DebugStore @Inject constructor(
    @ApplicationContext context: Context
) : DebugVssProvider {
    private val prefs: SharedPreferences = context.getSharedPreferences("debug_vss_prefs", Context.MODE_PRIVATE)

    private val _state = MutableStateFlow(loadState())
    override val state: StateFlow<DebugVssState> = _state.asStateFlow()

    private fun loadState(): DebugVssState {
        return DebugVssState(
            isDistracted = prefs.getBoolean("isDistracted", false),
            isDrowsy = prefs.getBoolean("isDrowsy", false),
            attentionLevel = prefs.getInt("attentionLevel", 100),
            isEmergencyBraking = prefs.getBoolean("isEmergencyBraking", false),
            distanceToFrontVehicle = prefs.getInt("distanceToFrontVehicle", 50),
            isCharging = prefs.getBoolean("isCharging", false),
            batteryPercent = prefs.getInt("batteryPercent", 72),
            outsideTemperature = prefs.getInt("outsideTemperature", 20),
            isRaining = prefs.getBoolean("isRaining", false),
            washerFluidLevel = prefs.getInt("washerFluidLevel", 100),
            isEngineWarning = prefs.getBoolean("isEngineWarning", false),
            tirePressureStatus = prefs.getString("tirePressureStatus", "OK") ?: "OK",
            isMoving = prefs.getBoolean("isMoving", false),
            speed = prefs.getInt("speed", 0),
            gear = prefs.getString("gear", "P") ?: "P",
            isNavigating = prefs.getBoolean("isNavigating", false),
            distanceToDestination = prefs.getInt("distanceToDestination", 0),
            isEngineOn = prefs.getBoolean("isEngineOn", false)
        )
    }

    fun updateState(reducer: (DebugVssState) -> DebugVssState) {
        val newState = reducer(_state.value)
        _state.value = newState
        prefs.edit().apply {
            putBoolean("isDistracted", newState.isDistracted)
            putBoolean("isDrowsy", newState.isDrowsy)
            putInt("attentionLevel", newState.attentionLevel)
            putBoolean("isEmergencyBraking", newState.isEmergencyBraking)
            putInt("distanceToFrontVehicle", newState.distanceToFrontVehicle)
            putBoolean("isCharging", newState.isCharging)
            putInt("batteryPercent", newState.batteryPercent)
            putInt("outsideTemperature", newState.outsideTemperature)
            putBoolean("isRaining", newState.isRaining)
            putInt("washerFluidLevel", newState.washerFluidLevel)
            putBoolean("isEngineWarning", newState.isEngineWarning)
            putString("tirePressureStatus", newState.tirePressureStatus)
            putBoolean("isMoving", newState.isMoving)
            putInt("speed", newState.speed)
            putString("gear", newState.gear)
            putBoolean("isNavigating", newState.isNavigating)
            putInt("distanceToDestination", newState.distanceToDestination)
            putBoolean("isEngineOn", newState.isEngineOn)
        }.apply()
    }
}
