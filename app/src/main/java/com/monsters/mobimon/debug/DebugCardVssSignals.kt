package com.monsters.mobimon.debug

import com.monsters.mobimon.core.domain.CardVssDefinition
import com.monsters.mobimon.core.domain.VehicleCardVssDefaults

private const val AXLE = "Vehicle.Chassis.Axle."

fun DebugRawVssState.toCardSignalValues(): Map<String, String> =
    mapOf(
        "Vehicle.Driver.FatigueLevel" to driverFatigueLevel.toString(),
        "Vehicle.Driver.DistractionLevel" to driverDistractionLevel.toString(),
        "Vehicle.Powertrain.TractionBattery.StateOfCharge.Displayed" to tractionBatterySocDisplayed.toString(),
        "Vehicle.Powertrain.TractionBattery.Charging.IsCharging" to tractionBatteryChargingIsCharging.toString(),
        "Vehicle.Cabin.HVAC.AmbientAirTemperature" to cabinAmbientAirTemperature.toString(),
        "Vehicle.Exterior.AirTemperature" to exteriorAirTemperature.toString(),
        "Vehicle.Body.Raindetection.Intensity" to rainIntensity.toString(),
        "Vehicle.Body.Windshield.Front.WasherFluid.IsLevelLow" to washerFluidLow.toString(),
        "Vehicle.Body.Windshield.Front.WasherFluid.Level" to washerFluidLevel.toString(),
        "${AXLE}Row1.Wheel.Left.Brake.PadWear" to row1LeftBrakePadWear.toString(),
        "${AXLE}Row1.Wheel.Right.Brake.PadWear" to row1RightBrakePadWear.toString(),
        "${AXLE}Row2.Wheel.Left.Brake.PadWear" to row2LeftBrakePadWear.toString(),
        "${AXLE}Row2.Wheel.Right.Brake.PadWear" to row2RightBrakePadWear.toString(),
        "${AXLE}Row1.Wheel.Left.Tire.IsPressureLow" to row1LeftTirePressureLow.toString(),
        "${AXLE}Row1.Wheel.Right.Tire.IsPressureLow" to row1RightTirePressureLow.toString(),
        "${AXLE}Row2.Wheel.Left.Tire.IsPressureLow" to row2LeftTirePressureLow.toString(),
        "${AXLE}Row2.Wheel.Right.Tire.IsPressureLow" to row2RightTirePressureLow.toString(),
        "Vehicle.Diagnostics.DTCCount" to diagnosticsDtcCount.toString(),
        "Vehicle.Service.IsServiceDue" to serviceDue.toString(),
        "Vehicle.TraveledDistance" to traveledDistanceMeters.toString(),
        "Vehicle.Cabin.Seat.Row1.DriverSide.IsBelted" to driverSeatBelted.toString(),
    )

object DebugCardVssSignals {
    val additional: List<CardVssDefinition> =
        VehicleCardVssDefaults.definitions.filterNot { it.path in DebugRawVssState().toCardSignalValues() }

    val defaults: Map<String, String> = additional.associate { it.path to it.defaultValue }
}
