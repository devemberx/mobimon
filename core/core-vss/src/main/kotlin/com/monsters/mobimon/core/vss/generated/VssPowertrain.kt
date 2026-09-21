// Generated from docs/vss.csv.
// Regenerate with: python3 core/core-vss/tools/generate_vss_signals.py

package com.monsters.mobimon.core.vss.generated

data class VssPowertrain(
    val accumulatedBrakingEnergy: Float = 0f,
    val combustionEngine: CombustionEngine = CombustionEngine(),
    val electricMotor: ElectricMotor = ElectricMotor(),
    val fuelSystem: FuelSystem = FuelSystem(),
    val isAutoPowerOptimize: Boolean = false,
    val powerOptimizeLevel: Int = 0,
    val range: Int = 0,
    val rangeExtender: RangeExtender = RangeExtender(),
    val timeRemaining: Int = 0,
    val tractionBattery: TractionBattery = TractionBattery(),
    val transmission: Transmission = Transmission(),
    val type: String = "",
) {
    data class CombustionEngine(
        val aspirationType: String = "UNKNOWN",
        val bore: Float = 0f,
        val compressionRatio: String = "",
        val configuration: String = "UNKNOWN",
        val dieselExhaustFluid: DieselExhaustFluid = DieselExhaustFluid(),
        val dieselParticulateFilter: DieselParticulateFilter = DieselParticulateFilter(),
        val displacement: Int = 0,
        val eop: Int = 0,
        val engineCode: String = "",
        val engineCoolant: EngineCoolant = EngineCoolant(),
        val engineHours: Float = 0f,
        val engineOil: EngineOil = EngineOil(),
        val idleHours: Float = 0f,
        val isRunning: Boolean = false,
        val maf: Int = 0,
        val map: Int = 0,
        val maxPower: Int = 0,
        val maxTorque: Int = 0,
        val numberOfCylinders: Int = 0,
        val numberOfValvesPerCylinder: Int = 0,
        val power: Int = 0,
        val speed: Float = 0f,
        val strokeLength: Float = 0f,
        val tps: Int = 0,
        val torque: Int = 0,
    ) {
        data class DieselExhaustFluid(
            val capacity: Float = 0f,
            val isLevelLow: Boolean = false,
            val level: Int = 0,
            val range: Int = 0,
        )

        data class DieselParticulateFilter(
            val deltaPressure: Float = 0f,
            val inletTemperature: Float = 0f,
            val outletTemperature: Float = 0f,
        )

        data class EngineCoolant(
            val capacity: Float = 0f,
            val level: String = "",
            val lifeRemaining: Int = 0,
            val temperature: Float = 0f,
        )

        data class EngineOil(
            val capacity: Float = 0f,
            val level: String = "",
            val lifeRemaining: Int = 0,
            val temperature: Float = 0f,
        )
    }

    data class ElectricMotor(
        val engineCode: String = "",
        val engineCoolant: EngineCoolant = EngineCoolant(),
        val maxPower: Int = 0,
        val maxRegenPower: Int = 0,
        val maxRegenTorque: Int = 0,
        val maxTorque: Int = 0,
        val power: Int = 0,
        val speed: Float = 0f,
        val temperature: Float = 0f,
        val timeInUse: Float = 0f,
        val torque: Int = 0,
    ) {
        data class EngineCoolant(
            val capacity: Float = 0f,
            val level: String = "",
            val lifeRemaining: Int = 0,
            val temperature: Float = 0f,
        )
    }

    data class FuelSystem(
        val absoluteLevel: Float = 0f,
        val afterRefuelingFuelEconomy: Float = 0f,
        val averageConsumption: Float = 0f,
        val consumptionSinceLastRefuel: Float = 0f,
        val consumptionSinceStart: Float = 0f,
        val cumulativeFuelEconomy: Float = 0f,
        val driveFuelEconomy: Float = 0f,
        val hybridType: String = "UNKNOWN",
        val instantConsumption: Float = 0f,
        val instantantFuelEconomy: Float = 0f,
        val isEngineStopStartEnabled: Boolean = false,
        val isFuelLevelEmpty: Boolean = false,
        val isFuelLevelLow: Boolean = false,
        val isFuelPortFlapOpen: Boolean = false,
        val range: Int = 0,
        val refuelPortPosition: List<String> = emptyList(),
        val relativeLevel: Int = 0,
        val supportedFuel: List<String> = emptyList(),
        val supportedFuelTypes: List<String> = emptyList(),
        val tankCapacity: Float = 0f,
        val timeRemaining: Int = 0,
    )

    data class RangeExtender(
        val chargeDepleting: ChargeDepleting = ChargeDepleting(),
        val chargeSustaining: ChargeSustaining = ChargeSustaining(),
        val combinedFuelEconomy: Float = 0f,
        val operatingMode: String = "",
    ) {
        data class ChargeDepleting(
            val energyConsumption: Float = 0f,
            val range: Int = 0,
        )

        data class ChargeSustaining(
            val fuelEconomy: Float = 0f,
            val range: Int = 0,
        )
    }

    data class TractionBattery(
        val accumulatedChargedEnergy: Float = 0f,
        val accumulatedChargedThroughput: Float = 0f,
        val accumulatedConsumedEnergy: Float = 0f,
        val accumulatedConsumedThroughput: Float = 0f,
        val batteryConditioning: BatteryConditioning = BatteryConditioning(),
        val cellVoltage: CellVoltage = CellVoltage(),
        val charging: Charging = Charging(),
        val currentCurrent: Float = 0f,
        val currentPower: Float = 0f,
        val currentVoltage: Float = 0f,
        val dcdc: DCDC = DCDC(),
        val errorCodes: List<String> = emptyList(),
        val grossCapacity: Int = 0,
        val id: String = "",
        val isGroundConnected: Boolean = false,
        val isPowerConnected: Boolean = false,
        val maxVoltage: Int = 0,
        val netCapacity: Int = 0,
        val nominalVoltage: Int = 0,
        val powerLoss: Float = 0f,
        val productionDate: String = "",
        val range: Int = 0,
        val stateOfCharge: StateOfCharge = StateOfCharge(),
        val stateOfHealth: Float = 0f,
        val temperature: Temperature = Temperature(),
        val timeRemaining: Int = 0,
    ) {
        data class BatteryConditioning(
            val isActive: Boolean = false,
            val isOngoing: Boolean = false,
            val requestedMode: String = "",
            val startTime: String = "",
            val targetTemperature: Float = 0f,
            val targetTime: String = "",
        )

        data class CellVoltage(
            val cellVoltages: List<Float> = emptyList(),
            val idMax: Int = 0,
            val idMin: Int = 0,
            val max: Float = 0f,
            val min: Float = 0f,
        )

        data class Charging(
            val averagePower: Float = 0f,
            val chargeCurrent: ChargeCurrent = ChargeCurrent(),
            val chargeLimit: Int = 100,
            val chargeRate: Float = 0f,
            val chargeVoltage: ChargeVoltage = ChargeVoltage(),
            val chargingPort: ChargingPort = ChargingPort(),
            val evseId: String = "",
            val isCharging: Boolean = false,
            val isDischarging: Boolean = false,
            val location: Location = Location(),
            val maxPower: Float = 0f,
            val maximumChargingCurrent: MaximumChargingCurrent = MaximumChargingCurrent(),
            val powerLoss: Float = 0f,
            val startStopCharging: String = "",
            val temperature: Float = 0f,
            val timeToComplete: Int = 0,
            val timer: Timer = Timer(),
        ) {
            data class ChargeCurrent(
                val dc: Float = 0f,
                val phase1: Float = 0f,
                val phase2: Float = 0f,
                val phase3: Float = 0f,
            )

            data class ChargeVoltage(
                val dc: Float = 0f,
                val phase1: Float = 0f,
                val phase2: Float = 0f,
                val phase3: Float = 0f,
            )

            data class ChargingPort(
                val anyPosition: AnyPosition = AnyPosition(),
                val frontLeft: FrontLeft = FrontLeft(),
                val frontMiddle: FrontMiddle = FrontMiddle(),
                val frontRight: FrontRight = FrontRight(),
                val rearLeft: RearLeft = RearLeft(),
                val rearMiddle: RearMiddle = RearMiddle(),
                val rearRight: RearRight = RearRight(),
            ) {
                data class AnyPosition(
                    val isChargingCableConnected: Boolean = false,
                    val isChargingCableLocked: Boolean = false,
                    val isFlapOpen: Boolean = false,
                    val supportedInletTypes: List<String> = emptyList(),
                )

                data class FrontLeft(
                    val isChargingCableConnected: Boolean = false,
                    val isChargingCableLocked: Boolean = false,
                    val isFlapOpen: Boolean = false,
                    val supportedInletTypes: List<String> = emptyList(),
                )

                data class FrontMiddle(
                    val isChargingCableConnected: Boolean = false,
                    val isChargingCableLocked: Boolean = false,
                    val isFlapOpen: Boolean = false,
                    val supportedInletTypes: List<String> = emptyList(),
                )

                data class FrontRight(
                    val isChargingCableConnected: Boolean = false,
                    val isChargingCableLocked: Boolean = false,
                    val isFlapOpen: Boolean = false,
                    val supportedInletTypes: List<String> = emptyList(),
                )

                data class RearLeft(
                    val isChargingCableConnected: Boolean = false,
                    val isChargingCableLocked: Boolean = false,
                    val isFlapOpen: Boolean = false,
                    val supportedInletTypes: List<String> = emptyList(),
                )

                data class RearMiddle(
                    val isChargingCableConnected: Boolean = false,
                    val isChargingCableLocked: Boolean = false,
                    val isFlapOpen: Boolean = false,
                    val supportedInletTypes: List<String> = emptyList(),
                )

                data class RearRight(
                    val isChargingCableConnected: Boolean = false,
                    val isChargingCableLocked: Boolean = false,
                    val isFlapOpen: Boolean = false,
                    val supportedInletTypes: List<String> = emptyList(),
                )
            }

            data class Location(
                val altitude: Float = 0f,
                val latitude: Float = 0f,
                val longitude: Float = 0f,
            )

            data class MaximumChargingCurrent(
                val dc: Float = 0f,
                val phase1: Float = 0f,
                val phase2: Float = 0f,
                val phase3: Float = 0f,
            )

            data class Timer(
                val mode: String = "",
                val time: String = "",
            )
        }

        data class DCDC(
            val powerLoss: Float = 0f,
            val temperature: Float = 0f,
        )

        data class StateOfCharge(
            val current: Float = 0f,
            val currentEnergy: Float = 0f,
            val displayed: Float = 0f,
        )

        data class Temperature(
            val average: Float = 0f,
            val cellTemperature: List<Float> = emptyList(),
            val max: Float = 0f,
            val min: Float = 0f,
        )
    }

    data class Transmission(
        val clutchEngagement: Float = 0f,
        val clutchWear: Int = 0,
        val currentGear: Int = 0,
        val diffLockFrontEngagement: Float = 0f,
        val diffLockRearEngagement: Float = 0f,
        val driveType: String = "UNKNOWN",
        val gearChangeMode: String = "",
        val gearCount: Int = 0,
        val isElectricalPowertrainEngaged: Boolean = false,
        val isLowRangeEngaged: Boolean = false,
        val isParkLockEngaged: Boolean = false,
        val performanceMode: String = "",
        val selectedGear: Int = 0,
        val temperature: Float = 0f,
        val torqueDistribution: Float = 0f,
        val travelledDistance: Float = 0f,
        val type: String = "UNKNOWN",
    )
}
