package com.devemberx.rivo.runtime

import com.devemberx.rivo.core.domain.DrivingState
import com.devemberx.rivo.core.domain.SignalQuality
import com.devemberx.rivo.core.domain.SignalSource
import com.devemberx.rivo.core.domain.VehicleRepository
import com.devemberx.rivo.core.domain.VehicleSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Test

class CompanionRuntimeTest {
    @Test
    fun repeatedForegroundNotificationsDoNotDuplicateConnection() {
        val vehicle = RecordingVehicleRepository()
        val runtime = CompanionRuntime(vehicle)
        repeat(5) { runtime.start() }
        assertEquals(1, vehicle.starts)
        repeat(5) { runtime.stop() }
        assertEquals(1, vehicle.stops)
        runtime.start()
        assertEquals(2, vehicle.starts)
        runtime.stop()
    }

    private class RecordingVehicleRepository : VehicleRepository {
        override val snapshots =
            MutableStateFlow(
                VehicleSnapshot(
                    "missing",
                    "none",
                    0,
                    0,
                    SignalSource.REAL,
                    DrivingState.UNKNOWN,
                    SignalQuality.UNAVAILABLE,
                ),
            )
        var starts = 0
        var stops = 0

        override fun start() {
            starts++
        }

        override fun stop() {
            stops++
        }
    }
}
