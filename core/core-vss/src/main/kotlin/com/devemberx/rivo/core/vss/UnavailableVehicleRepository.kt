package com.devemberx.rivo.core.vss

import com.devemberx.rivo.core.domain.DrivingState
import com.devemberx.rivo.core.domain.SignalQuality
import com.devemberx.rivo.core.domain.SignalSource
import com.devemberx.rivo.core.domain.VehicleRepository
import com.devemberx.rivo.core.domain.VehicleSnapshot
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
            ),
        )

    override val snapshots: StateFlow<VehicleSnapshot> = mutableSnapshots.asStateFlow()

    override fun start() = Unit

    override fun stop() = Unit
}
