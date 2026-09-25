package com.monsters.mobimon.core.domain

enum class CardVssType { BOOLEAN, NUMBER, TEXT }

data class CardVssDefinition(
    val path: String,
    val type: CardVssType,
    val defaultValue: String,
)

/** Explicit readings for the demo vehicle. These values are never evidence from a real adapter. */
object VehicleCardVssDefaults {
    private fun boolean(
        path: String,
        value: Boolean = false,
    ) = CardVssDefinition(path, CardVssType.BOOLEAN, value.toString())

    private fun number(
        path: String,
        value: Number,
    ) = CardVssDefinition(path, CardVssType.NUMBER, value.toString())

    private fun text(
        path: String,
        value: String,
    ) = CardVssDefinition(path, CardVssType.TEXT, value)

    private const val BATTERY = "Vehicle.Powertrain.TractionBattery."
    private const val AXLE = "Vehicle.Chassis.Axle."
    private val wheels = listOf("Row1.Wheel.Left", "Row1.Wheel.Right", "Row2.Wheel.Left", "Row2.Wheel.Right")

    val definitions: List<CardVssDefinition> =
        buildList {
            add(number("${BATTERY}StateOfCharge.Displayed", 72))
            add(number("${BATTERY}StateOfHealth", 94))
            add(number("${BATTERY}Range", 312_000))
            add(number("${BATTERY}TimeRemaining", 24_000))
            add(text("${BATTERY}ErrorCodes", ""))
            add(boolean("Vehicle.Cabin.Door.Row1.DriverSide.IsLocked", true))
            add(boolean("Vehicle.Service.IsServiceDue"))
            add(number("${BATTERY}Charging.TimeToComplete", 0))
            add(number("Vehicle.Service.DistanceToService", 5_000))
            add(number("Vehicle.Service.TimeToService", 15_552_000))
            wheels.forEach { add(boolean("$AXLE$it.Brake.IsFluidLevelLow")) }
            add(boolean("Vehicle.Body.Lights.Beam.Low.IsDefect"))
            add(boolean("Vehicle.Body.Lights.Brake.IsDefect"))
            add(boolean("Vehicle.Chassis.ParkingBrake.IsEngaged", true))
            wheels.forEach { add(boolean("$AXLE$it.Tire.IsPressureLow")) }
            add(boolean("Vehicle.Cabin.Seat.Row1.DriverSide.IsBelted"))
            wheels.forEach { add(number("$AXLE$it.Brake.PadWear", 0)) }
            wheels.forEach { add(boolean("$AXLE$it.Brake.IsBrakesWorn")) }
            add(boolean("Vehicle.ADAS.ABS.IsError"))
            add(boolean("Vehicle.Body.Hood.IsOpen"))
            add(boolean("Vehicle.Body.Trunk.Rear.IsOpen"))
            add(boolean("Vehicle.Body.Windshield.Front.WasherFluid.IsLevelLow"))
            add(number("Vehicle.Body.Windshield.Front.WasherFluid.Level", 100))
            add(number("Vehicle.Exterior.AirTemperature", 20))
            add(number("Vehicle.Body.Raindetection.Intensity", 0))
            add(number("Vehicle.Cabin.HVAC.AmbientAirTemperature", 20))
            add(number("Vehicle.TraveledDistance", 0))
            add(number("Vehicle.Diagnostics.DTCCount", 0))
            add(number("Vehicle.Driver.FatigueLevel", 0))
            add(number("Vehicle.Driver.DistractionLevel", 0))
            add(boolean("Vehicle.IsBrokenDown"))
            add(boolean("${BATTERY}Charging.IsCharging"))
        }

    val values: Map<String, String> = definitions.associate { it.path to it.defaultValue }
}
