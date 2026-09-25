package com.monsters.mobimon.feature.vehicle

import com.monsters.mobimon.core.domain.DrivingState
import com.monsters.mobimon.core.domain.SignalQuality
import com.monsters.mobimon.core.domain.SignalSource
import com.monsters.mobimon.core.domain.VehicleSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class VehicleCardCatalogTest {
    @Test
    fun initialSlotsMatchTheSixFigmaCards() {
        assertEquals(
            listOf("battery", "charging", "tire", "washer", "environment", "assist"),
            VehicleCardCatalog.defaultSlots.map { it.id },
        )
    }

    @Test
    fun catalogOffersThirtyDistinctVssMappedCards() {
        val cards = VehicleCardCatalog.cards
        assertEquals(30, cards.size)
        assertEquals(30, cards.map { it.id }.distinct().size)
        cards.forEach { card ->
            assertEquals(true, card.vssPaths.isNotEmpty())
            assertEquals(true, card.vssPaths.all { it.startsWith("Vehicle.") })
        }
        assertEquals(4, cards.first { it.id == "brake-fluid" }.vssPaths.size)
        assertEquals(4, cards.first { it.id == "pad-wear" }.vssPaths.size)
        assertEquals(4, cards.first { it.id == "pad-warning" }.vssPaths.size)
        assertEquals(4, cards.first { it.id == "tire-low" }.vssPaths.size)
    }

    @Test
    fun missingOrStaleSignalsNeverBecomeHealthyExamples() {
        val unavailable = snapshot(SignalQuality.UNAVAILABLE)
        val stale = snapshot(SignalQuality.STALE).copy(batteryPercent = 82, washerFluidLevel = 68)
        assertNull(VehicleCardCatalog.reading("battery", unavailable)?.value)
        assertNull(VehicleCardCatalog.reading("washer", stale)?.value)
        assertNull(VehicleCardCatalog.reading("battery-health", snapshot())?.value)
        assertNull(VehicleCardCatalog.reading("brake-fluid", snapshot())?.value)
    }

    @Test
    fun supportedSignalsUseCurrentSnapshotValues() {
        val current =
            snapshot().copy(
                batteryPercent = 18,
                isCharging = true,
                tirePressureStatus = "NG",
                outsideTemperature = 18,
                washerFluidLevel = 68,
            )
        assertEquals("18%", VehicleCardCatalog.reading("battery", current)?.value)
        assertEquals("충전 중", VehicleCardCatalog.reading("charging", current)?.value)
        assertEquals("NG", VehicleCardCatalog.reading("tire", current)?.value)
        assertEquals("18°", VehicleCardCatalog.reading("environment", current)?.value)
        assertEquals("68%", VehicleCardCatalog.reading("washer", current)?.value)
    }

    private fun snapshot(quality: SignalQuality = SignalQuality.VALID) =
        VehicleSnapshot(
            id = "catalog",
            epoch = "test",
            sequence = 1,
            receivedAtMillis = 1,
            source = SignalSource.SIMULATED,
            drivingState = DrivingState.PARKED,
            quality = quality,
        )
}
