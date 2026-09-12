package com.monsters.mobimon.core.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class VehicleFreshnessPolicyTest {
    private val policy = VehicleFreshnessPolicy(maxAgeMillis = 15_000)

    @Test
    fun freshBatteryDoesNotRefreshStaleParking() {
        val snapshot = vehicleSnapshot(receivedAtMillis = 1_000, batteryReceivedAtMillis = 20_000)

        val result = policy.displaySnapshot(snapshot, SignalSource.REAL, 20_000)

        assertEquals(SignalQuality.STALE, result.quality)
        assertEquals(SignalQuality.VALID, result.batteryQuality)
        assertEquals(72, result.batteryPercent)
        assertEquals(19_000L, result.parkingAgeMillis)
        assertEquals(0L, result.batteryAgeMillis)
    }

    @Test
    fun invalidBatteryDoesNotInvalidateFreshParking() {
        val snapshot = vehicleSnapshot(receivedAtMillis = 20_000, batteryReceivedAtMillis = 1_000)

        val result = policy.displaySnapshot(snapshot, SignalSource.REAL, 20_000)

        assertEquals(SignalQuality.VALID, result.quality)
        assertEquals(SignalQuality.STALE, result.batteryQuality)
        assertEquals(0L, result.parkingAgeMillis)
        assertEquals(19_000L, result.batteryAgeMillis)
        assertNull(QuestEvaluator(15_000).validateSnapshot(result, SignalSource.REAL, 20_000))
    }

    @Test
    fun warningKeepsItsOwnFreshnessWhenParkingIsCurrent() {
        val snapshot =
            vehicleSnapshot(receivedAtMillis = 20_000, batteryReceivedAtMillis = 20_000).copy(
                warnings =
                    listOf(
                        VehicleWarning("앞바퀴", "왼쪽", WarningSeverity.CAUTION, "압력을 확인하세요", "안전한 곳에서 점검", 1_000),
                    ),
            )

        val result = policy.displaySnapshot(snapshot, SignalSource.REAL, 20_000)

        assertEquals(SignalQuality.VALID, result.quality)
        assertEquals(SignalQuality.STALE, result.warnings.single().quality)
    }

    private fun vehicleSnapshot(
        receivedAtMillis: Long,
        batteryReceivedAtMillis: Long,
    ) = VehicleSnapshot(
        id = "snapshot",
        epoch = "epoch",
        sequence = 1,
        receivedAtMillis = receivedAtMillis,
        source = SignalSource.REAL,
        drivingState = DrivingState.PARKED,
        quality = SignalQuality.VALID,
        batteryPercent = 72,
        batteryReceivedAtMillis = batteryReceivedAtMillis,
        batteryQuality = SignalQuality.VALID,
    )
}
