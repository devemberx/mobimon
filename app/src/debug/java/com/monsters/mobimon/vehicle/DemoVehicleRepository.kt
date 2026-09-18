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
import com.monsters.mobimon.debug.DebugVssState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch

class DemoVehicleRepository(
    private val clock: Clock,
    private val ids: IdGenerator,
    private val settingsRepository: SettingsRepository,
    private val debugStore: DebugVssProvider,
    private val scope: CoroutineScope,
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
    private var generation = 0L

    @Synchronized
    override fun start() {
        if (observation?.isActive == true) return
        val currentGeneration = ++generation
        val epoch = ids.nextId()
        observation =
            scope.launch {
                var sequence = 0L
                publicationRequests().collect {
                    val nextSequence = ++sequence
                    val observedAt = clock.nowMillis()
                    val isDebugOn = settingsRepository.settings.first().debugModeEnabled
                    val debugState = debugStore.state.value

                    val snapshot =
                        if (isDebugOn) {
                            VehicleSnapshot(
                                id = "$epoch-$nextSequence",
                                epoch = epoch,
                                sequence = nextSequence,
                                receivedAtMillis = observedAt,
                                source = SignalSource.SIMULATED,
                                drivingState = debugState.drivingState(),
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
                                timeOfDay = debugState.timeOfDay,
                            )
                        } else {
                            VehicleSnapshot(
                                id = "$epoch-$nextSequence",
                                epoch = epoch,
                                sequence = nextSequence,
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
                }
            }
    }

    private fun publicationRequests() =
        merge(
            flow {
                emit(Unit)
                while (true) {
                    delay(2_000)
                    emit(Unit)
                }
            },
            debugStore.state.drop(1).map { Unit },
            settingsRepository.settings
                .map { it.debugModeEnabled }
                .distinctUntilChanged()
                .drop(1)
                .map { Unit },
        )

    @Synchronized
    override fun stop() {
        generation++
        observation?.cancel()
        observation = null
        mutableSnapshots.value =
            mutableSnapshots.value.copy(
                quality = SignalQuality.UNAVAILABLE,
                drivingState = DrivingState.UNKNOWN,
                batteryQuality = SignalQuality.UNAVAILABLE,
            )
    }
}

private fun DebugVssState.drivingState(): DrivingState =
    when {
        isMoving || speed > 0 -> DrivingState.MOVING
        speed < 0 -> DrivingState.UNKNOWN
        gear == "P" -> DrivingState.PARKED
        else -> DrivingState.UNKNOWN
    }
