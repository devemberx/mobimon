package com.monsters.mobimon.core.vss

import com.monsters.mobimon.core.domain.DrivingState
import com.monsters.mobimon.core.domain.SignalQuality
import com.monsters.mobimon.core.domain.SignalSource
import com.monsters.mobimon.core.domain.SignalUnavailableReason
import com.monsters.mobimon.core.domain.VehicleRepository
import com.monsters.mobimon.core.domain.VehicleSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UnavailableVehicleRepository : VehicleRepository {
    private val mutableSnapshots =
        MutableStateFlow(
            VehicleSnapshot(
                id = "unavailable",
                epoch = "unavailable",
                sequence = 0,
                receivedAtMillis = 0,
                source = SignalSource.REAL,
                drivingState = DrivingState.UNKNOWN,
                quality = SignalQuality.UNAVAILABLE,
                parkingUnavailableReason = SignalUnavailableReason.UNSUPPORTED,
                batteryUnavailableReason = SignalUnavailableReason.UNSUPPORTED,
            ),
        )

    override val snapshots: StateFlow<VehicleSnapshot> = mutableSnapshots.asStateFlow()

    override fun start() = Unit

    override fun stop() = Unit
}
