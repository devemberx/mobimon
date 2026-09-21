// Generated from docs/vss.csv.
// Regenerate with: python3 core/core-vss/tools/generate_vss_signals.py

package com.monsters.mobimon.core.vss.generated

data class VssChassis(
    val accelerator: Accelerator = Accelerator(),
    val axle: Axle = Axle(),
    val axleCount: Int = 2,
    val brake: Brake = Brake(),
    val parkingBrake: ParkingBrake = ParkingBrake(),
    val steeringWheel: SteeringWheel = SteeringWheel(),
    val wheelbase: Int = 0,
) {
    data class Accelerator(
        val pedalPosition: Int = 0,
    )

    data class Axle(
        val row1: Row1 = Row1(),
        val row2: Row2 = Row2(),
    ) {
        data class Row1(
            val axleWidth: Int = 0,
            val steeringAngle: Float = 0f,
            val tireAspectRatio: Int = 0,
            val tireDiameter: Float = 0f,
            val tireWidth: Int = 0,
            val trackWidth: Int = 0,
            val treadWidth: Int = 0,
            val wheel: Wheel = Wheel(),
            val wheelCount: Int = 0,
            val wheelDiameter: Float = 0f,
            val wheelWidth: Float = 0f,
        ) {
            data class Wheel(
                val left: Left = Left(),
                val right: Right = Right(),
            ) {
                data class Left(
                    val angularSpeed: Float = 0f,
                    val brake: Brake = Brake(),
                    val speed: Float = 0f,
                    val tire: Tire = Tire(),
                ) {
                    data class Brake(
                        val fluidLevel: Int = 0,
                        val isBrakesWorn: Boolean = false,
                        val isFluidLevelLow: Boolean = false,
                        val padWear: Int = 0,
                    )

                    data class Tire(
                        val airTemperature: Float = 0f,
                        val isPressureLow: Boolean = false,
                        val pressure: Int = 0,
                        val rubberTemperature: Float = 0f,
                        val temperature: Float = 0f,
                    )
                }

                data class Right(
                    val angularSpeed: Float = 0f,
                    val brake: Brake = Brake(),
                    val speed: Float = 0f,
                    val tire: Tire = Tire(),
                ) {
                    data class Brake(
                        val fluidLevel: Int = 0,
                        val isBrakesWorn: Boolean = false,
                        val isFluidLevelLow: Boolean = false,
                        val padWear: Int = 0,
                    )

                    data class Tire(
                        val airTemperature: Float = 0f,
                        val isPressureLow: Boolean = false,
                        val pressure: Int = 0,
                        val rubberTemperature: Float = 0f,
                        val temperature: Float = 0f,
                    )
                }
            }
        }

        data class Row2(
            val axleWidth: Int = 0,
            val steeringAngle: Float = 0f,
            val tireAspectRatio: Int = 0,
            val tireDiameter: Float = 0f,
            val tireWidth: Int = 0,
            val trackWidth: Int = 0,
            val treadWidth: Int = 0,
            val wheel: Wheel = Wheel(),
            val wheelCount: Int = 0,
            val wheelDiameter: Float = 0f,
            val wheelWidth: Float = 0f,
        ) {
            data class Wheel(
                val left: Left = Left(),
                val right: Right = Right(),
            ) {
                data class Left(
                    val angularSpeed: Float = 0f,
                    val brake: Brake = Brake(),
                    val speed: Float = 0f,
                    val tire: Tire = Tire(),
                ) {
                    data class Brake(
                        val fluidLevel: Int = 0,
                        val isBrakesWorn: Boolean = false,
                        val isFluidLevelLow: Boolean = false,
                        val padWear: Int = 0,
                    )

                    data class Tire(
                        val airTemperature: Float = 0f,
                        val isPressureLow: Boolean = false,
                        val pressure: Int = 0,
                        val rubberTemperature: Float = 0f,
                        val temperature: Float = 0f,
                    )
                }

                data class Right(
                    val angularSpeed: Float = 0f,
                    val brake: Brake = Brake(),
                    val speed: Float = 0f,
                    val tire: Tire = Tire(),
                ) {
                    data class Brake(
                        val fluidLevel: Int = 0,
                        val isBrakesWorn: Boolean = false,
                        val isFluidLevelLow: Boolean = false,
                        val padWear: Int = 0,
                    )

                    data class Tire(
                        val airTemperature: Float = 0f,
                        val isPressureLow: Boolean = false,
                        val pressure: Int = 0,
                        val rubberTemperature: Float = 0f,
                        val temperature: Float = 0f,
                    )
                }
            }
        }
    }

    data class Brake(
        val isDriverEmergencyBrakingDetected: Boolean = false,
        val pedalPosition: Int = 0,
    )

    data class ParkingBrake(
        val isAutoApplyEnabled: Boolean = false,
        val isEngaged: Boolean = false,
    )

    data class SteeringWheel(
        val angle: Int = 0,
        val extension: Int = 0,
        val heatingCooling: Int = 0,
        val tilt: Int = 0,
    )
}
