package com.monsters.mobimon.vehicle

import com.monsters.mobimon.core.domain.Clock
import com.monsters.mobimon.core.domain.DrivingState
import com.monsters.mobimon.core.domain.IdGenerator
import com.monsters.mobimon.core.domain.SettingsRepository
import com.monsters.mobimon.core.domain.SignalQuality
import com.monsters.mobimon.core.domain.SignalSource
import com.monsters.mobimon.core.domain.VehicleRepository
import com.monsters.mobimon.core.domain.VehicleSnapshot
import com.monsters.mobimon.debug.DebugVssProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class DemoVehicleRepository(
    private val clock: Clock,
    private val ids: IdGenerator,
    private val settingsRepository: SettingsRepository,
    private val debugStore: DebugVssProvider,
    private val scope: CoroutineScope,
    private val vehicleDatabase: com.monsters.mobimon.core.database.VehicleDatabase? = null,
) : VehicleRepository {
    private val mutableSnapshots =
        MutableStateFlow(
            VehicleSnapshot(
                "unavailable",
                "none",
                0,
                0,
                SignalSource.SIMULATED,
                DrivingState.UNKNOWN,
                SignalQuality.UNAVAILABLE,
            ),
        )
    override val snapshots = mutableSnapshots.asStateFlow()
    private var observation: Job? = null
    private var debugObservation: Job? = null
    private var generation = 0L

    @Synchronized
    override fun start() {
        if (observation?.isActive == true) return
        val currentGeneration = ++generation
        val epoch = ids.nextId()
        var sequence = 1L

        fun publish() {
            scope.launch {
                val observedAt = clock.nowMillis()
                val isDebugOn = settingsRepository.settings.first().launcherCharacterEnabled
                val debugState = debugStore.state.value

                val snapshot =
                    if (isDebugOn) {
                        VehicleSnapshot(
                            id = "$epoch-$sequence",
                            epoch = epoch,
                            sequence = sequence,
                            receivedAtMillis = observedAt,
                            source = SignalSource.SIMULATED,
                            drivingState = if (debugState.isMoving) DrivingState.MOVING else DrivingState.PARKED,
                            quality = SignalQuality.VALID,
                            batteryPercent = debugState.batteryPercent,
                            batteryReceivedAtMillis = observedAt,
                            batteryQuality = SignalQuality.VALID,
                            isDistracted = debugState.isDistracted,
                            isDrowsy = debugState.isDrowsy,
                            attentionLevel = debugState.attentionLevel,
                            isEmergencyBraking = debugState.isEmergencyBraking,
                            distanceToFrontVehicle = debugState.distanceToFrontVehicle,
                            isCharging = debugState.isCharging,
                            outsideTemperature = debugState.outsideTemperature,
                            isRaining = debugState.isRaining,
                            washerFluidLevel = debugState.washerFluidLevel,
                            isEngineWarning = debugState.isEngineWarning,
                            tirePressureStatus = debugState.tirePressureStatus,
                            speed = debugState.speed,
                            gear = debugState.gear,
                            isNavigating = debugState.isNavigating,
                            distanceToDestination = debugState.distanceToDestination,
                            isEngineOn = debugState.isEngineOn,
                        )
                    } else {
                        VehicleSnapshot(
                            id = "$epoch-$sequence",
                            epoch = epoch,
                            sequence = sequence,
                            receivedAtMillis = observedAt,
                            source = SignalSource.SIMULATED,
                            drivingState = DrivingState.PARKED,
                            quality = SignalQuality.VALID,
                            batteryPercent = 72,
                            batteryReceivedAtMillis = observedAt,
                            batteryQuality = SignalQuality.VALID,
                        )
                    }

                synchronized(this@DemoVehicleRepository) {
                    if (generation == currentGeneration) {
                        mutableSnapshots.value = snapshot
                    }
                }

                vehicleDatabase?.let { db ->
                    try {
                        db.vehicleDao().insertVehicleStatus(
                            com.monsters.mobimon.core.database.VehicleStatusEntity(
                                id = "current",
                                drivingState = snapshot.drivingState.name,
                                isCharging = snapshot.isCharging ?: false,
                                batteryPercent = snapshot.batteryPercent ?: 72,
                                speed = snapshot.speed ?: 0,
                                gear = snapshot.gear ?: "P",
                                isEngineOn = snapshot.isEngineOn ?: false,
                                isMoving = debugState.isMoving,
                                isDistracted = snapshot.isDistracted ?: false,
                                isDrowsy = snapshot.isDrowsy ?: false,
                                attentionLevel = snapshot.attentionLevel ?: 100,
                                isEmergencyBraking = snapshot.isEmergencyBraking ?: false,
                                distanceToFrontVehicle = snapshot.distanceToFrontVehicle ?: 50,
                                outsideTemperature = snapshot.outsideTemperature ?: 20,
                                isRaining = snapshot.isRaining ?: false,
                                washerFluidLevel = snapshot.washerFluidLevel ?: 100,
                                isEngineWarning = snapshot.isEngineWarning ?: false,
                                tirePressureStatus = snapshot.tirePressureStatus ?: "OK",
                                isNavigating = snapshot.isNavigating ?: false,
                                distanceToDestination = snapshot.distanceToDestination ?: 0,
                                updatedAtMillis = observedAt,
                            ),
                        )
                    } catch (e: Exception) {
                        // ignore in demo
                    }
                }
            }
        }
        publish()
        observation =
            scope.launch {
                while (isActive) {
                    delay(2_000)
                    sequence++
                    publish()
                }
            }
        debugObservation =
            scope.launch {
                debugStore.state.collect {
                    sequence++
                    publish()
                }
            }
    }

    @Synchronized
    override fun stop() {
        generation++
        observation?.cancel()
        observation = null
        debugObservation?.cancel()
        debugObservation = null
        mutableSnapshots.value =
            mutableSnapshots.value.copy(
                quality = SignalQuality.UNAVAILABLE,
                drivingState = DrivingState.UNKNOWN,
                batteryQuality = SignalQuality.UNAVAILABLE,
            )
    }
}
