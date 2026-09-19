package com.monsters.mobimon.vehicle

import com.monsters.mobimon.core.domain.Clock
import com.monsters.mobimon.core.domain.DrivingState
import com.monsters.mobimon.core.domain.IdGenerator
import com.monsters.mobimon.core.domain.SettingsRepository
import com.monsters.mobimon.core.domain.SignalQuality
import com.monsters.mobimon.core.domain.SignalSource
import com.monsters.mobimon.core.domain.VehicleRepository
import com.monsters.mobimon.core.domain.VehicleSnapshot
import com.monsters.mobimon.core.vss.VssInterpretationOverrides
import com.monsters.mobimon.core.vss.VssRawVehicleSource
import com.monsters.mobimon.core.vss.VssRawVehicleState
import com.monsters.mobimon.core.vss.VssVehicleInterpreter
import com.monsters.mobimon.debug.DebugInterpretationOverrides
import com.monsters.mobimon.debug.DebugRawVssState
import com.monsters.mobimon.debug.DebugVssProvider
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
    private val vssRawSource: VssRawVehicleSource,
    private val scope: CoroutineScope,
) : VehicleRepository {
    private val mutableSnapshots =
        MutableStateFlow(
            VssVehicleInterpreter.snapshot(
                raw = vssRawSource.state.value,
                id = INITIAL_SNAPSHOT_ID,
                epoch = INITIAL_SNAPSHOT_EPOCH,
                sequence = 0,
                observedAtMillis = clock.nowMillis(),
                source = SignalSource.REAL,
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
        vssRawSource.start()
        observation =
            scope.launch {
                var sequence = 0L
                publicationRequests().collect {
                    val nextSequence = ++sequence
                    val observedAt = clock.nowMillis()
                    val isDebugOn = settingsRepository.settings.first().debugModeEnabled
                    val debugState = debugStore.state.value

                    val snapshot: VehicleSnapshot =
                        if (isDebugOn) {
                            VssVehicleInterpreter.snapshot(
                                raw = debugState.raw.toVssRawVehicleState(),
                                id = "$epoch-$nextSequence",
                                epoch = epoch,
                                sequence = nextSequence,
                                observedAtMillis = observedAt,
                                source = SignalSource.SIMULATED,
                                overrides = debugState.overrides.toVssInterpretationOverrides(),
                            )
                        } else {
                            VssVehicleInterpreter.snapshot(
                                raw = vssRawSource.state.value,
                                id = "$epoch-$nextSequence",
                                epoch = epoch,
                                sequence = nextSequence,
                                observedAtMillis = observedAt,
                                source = SignalSource.REAL,
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
            vssRawSource.state.drop(1).map { Unit },
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
        vssRawSource.stop()
        mutableSnapshots.value =
            mutableSnapshots.value.copy(
                quality = SignalQuality.UNAVAILABLE,
                drivingState = DrivingState.UNKNOWN,
                batteryQuality = SignalQuality.UNAVAILABLE,
            )
    }
}

private const val INITIAL_SNAPSHOT_ID = "initial"
private const val INITIAL_SNAPSHOT_EPOCH = "initial"

private fun DebugRawVssState.toVssRawVehicleState(): VssRawVehicleState =
    VssRawVehicleState(
        driverFatigueLevel = driverFatigueLevel,
        driverDistractionLevel = driverDistractionLevel,
        dmsIsWarning = dmsIsWarning,
        driverEmergencyBrakingDetected = driverEmergencyBrakingDetected,
        obstacleFrontCenterDistance = obstacleFrontCenterDistance,
        tractionBatterySocDisplayed = tractionBatterySocDisplayed,
        chargingCableConnected = chargingCableConnected,
        tractionBatteryChargingIsCharging = tractionBatteryChargingIsCharging,
        chargingAveragePowerKw = chargingAveragePowerKw,
        exteriorAirTemperature = exteriorAirTemperature,
        rainIntensity = rainIntensity,
        washerFluidLevel = washerFluidLevel,
        row1LeftTirePressureLow = row1LeftTirePressureLow,
        row1RightTirePressureLow = row1RightTirePressureLow,
        row2LeftTirePressureLow = row2LeftTirePressureLow,
        row2RightTirePressureLow = row2RightTirePressureLow,
        diagnosticsDtcCount = diagnosticsDtcCount,
        obdMilOn = obdMilOn,
        vehicleIsMoving = vehicleIsMoving,
        vehicleSpeedKmh = vehicleSpeedKmh,
        selectedGear = selectedGear,
        destinationLatitude = destinationLatitude,
        destinationLongitude = destinationLongitude,
        currentLocationTimestamp = currentLocationTimestamp,
        currentLatitude = currentLatitude,
        currentLongitude = currentLongitude,
        combustionEngineRunning = combustionEngineRunning,
    )

private fun DebugInterpretationOverrides.toVssInterpretationOverrides(): VssInterpretationOverrides =
    VssInterpretationOverrides(
        isDistracted = isDistracted,
        isDrowsy = isDrowsy,
        attentionLevel = attentionLevel,
        isEmergencyBraking = isEmergencyBraking,
        distanceToFrontVehicle = distanceToFrontVehicle,
        isCharging = isCharging,
        batteryPercent = batteryPercent,
        outsideTemperature = outsideTemperature,
        isRaining = isRaining,
        washerFluidLevel = washerFluidLevel,
        isEngineWarning = isEngineWarning,
        tirePressureStatus = tirePressureStatus,
        isMoving = isMoving,
        speed = speed,
        gear = gear,
        isNavigating = isNavigating,
        distanceToDestination = distanceToDestination,
        isEngineOn = isEngineOn,
        timeOfDay = timeOfDay,
    )
