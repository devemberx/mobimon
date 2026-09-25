package com.monsters.mobimon.core.presentation

import com.monsters.mobimon.core.domain.DrivingState
import com.monsters.mobimon.core.domain.SignalQuality
import com.monsters.mobimon.core.domain.SignalSource
import com.monsters.mobimon.core.domain.VehicleSnapshot
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ParkingBadgeStatusTest {
    private val parked =
        VehicleSnapshot(
            id = "vehicle",
            epoch = "epoch",
            sequence = 1,
            receivedAtMillis = 100,
            source = SignalSource.SIMULATED,
            drivingState = DrivingState.PARKED,
            quality = SignalQuality.VALID,
            speed = 0,
            gear = "P",
        )

    @Test
    fun confirmsOnlyCompleteValidParkEvidence() {
        assertTrue(parked.parkingBadgeConfirmed)
        assertFalse(parked.copy(speed = 1).parkingBadgeConfirmed)
        assertFalse(parked.copy(gear = "D").parkingBadgeConfirmed)
        assertFalse(parked.copy(drivingState = DrivingState.MOVING).parkingBadgeConfirmed)
        assertFalse(parked.copy(quality = SignalQuality.STALE).parkingBadgeConfirmed)
        assertFalse(parked.copy(quality = SignalQuality.UNAVAILABLE).parkingBadgeConfirmed)
        assertFalse(parked.copy(speed = null).parkingBadgeConfirmed)
        assertFalse(parked.copy(gear = null).parkingBadgeConfirmed)
    }
}
