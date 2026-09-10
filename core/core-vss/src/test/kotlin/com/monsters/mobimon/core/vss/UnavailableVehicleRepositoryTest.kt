package com.monsters.mobimon.core.vss

import com.monsters.mobimon.core.domain.DrivingState
import com.monsters.mobimon.core.domain.SignalQuality
import com.monsters.mobimon.core.domain.SignalSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

class UnavailableVehicleRepositoryTest {
    @Test
    fun `reports stable unavailable REAL vehicle state across lifecycle calls`() {
        val repository = UnavailableVehicleRepository()
        val initial = repository.snapshots.value

        repository.start()
        repository.start()
        repository.stop()
        repository.stop()

        assertSame(initial, repository.snapshots.value)
        assertEquals(SignalSource.REAL, initial.source)
        assertEquals(DrivingState.UNKNOWN, initial.drivingState)
        assertEquals(SignalQuality.UNAVAILABLE, initial.quality)
        assertNull(initial.batteryPercent)
    }
}
