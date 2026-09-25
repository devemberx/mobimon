package com.monsters.mobimon.feature.vehicle

import com.monsters.mobimon.core.domain.DrivingState
import com.monsters.mobimon.core.domain.SignalQuality
import com.monsters.mobimon.core.domain.SignalSource
import com.monsters.mobimon.core.domain.VehicleCardVssDefaults
import com.monsters.mobimon.core.domain.VehicleSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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

    @Test
    fun simulatedDefaultsCoverEveryCardPathAndRenderAllThirtyCards() {
        val definitions = VehicleCardVssDefaults.definitions.associateBy { it.path }
        val paths = (VehicleCardCatalog.cards + VehicleCardCatalog.defaultSlots).flatMap { it.vssPaths }
        assertTrue(paths.all { it in definitions })

        val current = snapshot().copy(vssCardSignals = VehicleCardVssDefaults.values)
        VehicleCardCatalog.cards.forEach { card ->
            assertNotNull(card.id, VehicleCardCatalog.reading(card.id, current)?.value)
        }
    }

    @Test
    fun fourWheelWarningCombinesAllFourSignals() {
        val signals = VehicleCardVssDefaults.values.toMutableMap()
        signals["Vehicle.Chassis.Axle.Row2.Wheel.Right.Brake.IsFluidLevelLow"] = "true"

        assertEquals("부족", VehicleCardCatalog.reading("brake-fluid", snapshot().copy(vssCardSignals = signals))?.value)
        signals["Vehicle.Chassis.Axle.Row1.Wheel.Left.Brake.PadWear"] = "65"
        signals["Vehicle.Chassis.Axle.Row2.Wheel.Right.Brake.IsBrakesWorn"] = "true"
        signals["Vehicle.Chassis.Axle.Row2.Wheel.Left.Tire.IsPressureLow"] = "true"
        val current = snapshot().copy(vssCardSignals = signals)
        assertEquals("65%", VehicleCardCatalog.reading("pad-wear", current)?.value)
        assertEquals("경고 있음", VehicleCardCatalog.reading("pad-warning", current)?.value)
        assertEquals("경고 있음", VehicleCardCatalog.reading("tire-low", current)?.value)
        signals.remove("Vehicle.Chassis.Axle.Row1.Wheel.Right.Brake.PadWear")
        assertNull(VehicleCardCatalog.reading("pad-wear", snapshot().copy(vssCardSignals = signals))?.value)
    }

    @Test
    fun chargingTimeRequiresChargingState() {
        val signals = VehicleCardVssDefaults.values.toMutableMap()
        assertEquals(
            "충전 안 함",
            VehicleCardCatalog.reading("charging-time", snapshot().copy(vssCardSignals = signals))?.value,
        )
        signals["Vehicle.Powertrain.TractionBattery.Charging.IsCharging"] = "true"
        signals["Vehicle.Powertrain.TractionBattery.Charging.TimeToComplete"] = "3600"
        assertEquals(
            "1시간",
            VehicleCardCatalog.reading("charging-time", snapshot().copy(vssCardSignals = signals))?.value,
        )
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
