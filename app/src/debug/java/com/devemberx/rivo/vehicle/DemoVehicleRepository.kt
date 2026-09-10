package com.devemberx.rivo.vehicle

import com.devemberx.rivo.core.domain.Clock
import com.devemberx.rivo.core.domain.DrivingState
import com.devemberx.rivo.core.domain.IdGenerator
import com.devemberx.rivo.core.domain.SignalQuality
import com.devemberx.rivo.core.domain.SignalSource
import com.devemberx.rivo.core.domain.VehicleRepository
import com.devemberx.rivo.core.domain.VehicleSnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class DemoVehicleRepository(
    private val clock: Clock,
    private val ids: IdGenerator,
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
        var sequence = 1L

        fun publish() {
            val snapshot =
                VehicleSnapshot(
                    id = "$epoch-$sequence",
                    epoch = epoch,
                    sequence = sequence,
                    receivedAtMillis = clock.nowMillis(),
                    source = SignalSource.SIMULATED,
                    drivingState = DrivingState.PARKED,
                    quality = SignalQuality.VALID,
                    batteryPercent = 72,
                )
            synchronized(this) {
                if (generation == currentGeneration) {
                    mutableSnapshots.value = snapshot
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
    }

    @Synchronized
    override fun stop() {
        generation++
        observation?.cancel()
        observation = null
        mutableSnapshots.value =
            mutableSnapshots.value.copy(quality = SignalQuality.UNAVAILABLE, drivingState = DrivingState.UNKNOWN)
    }
}
