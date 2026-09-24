// Generated from docs/vss.csv.
// Regenerate with: python3 core/core-vss/tools/generate_vss_signals.py

package com.monsters.mobimon.core.vss.generated

data class VssMotionManagement(
    val brake: Brake = Brake(),
    val electricAxle: ElectricAxle = ElectricAxle(),
    val steering: Steering = Steering(),
    val suspension: Suspension = Suspension(),
) {
    data class Brake(
        val axle: Axle = Axle(),
        val vehicleForceDistributionFrontMaximum: Int = 0,
        val vehicleForceDistributionFrontMinimum: Int = 0,
        val vehicleForceElectric: Int = 0,
        val vehicleForceElectricMinimumArbitrated: Int = 0,
        val vehicleForceMaximum: Int = 0,
    ) {
        data class Axle(
            val row1: Row1 = Row1(),
            val row2: Row2 = Row2(),
        ) {
            data class Row1(
                val torqueDistributionFrictionRightMaximum: Int = 0,
                val torqueDistributionFrictionRightMinimum: Int = 0,
                val torqueElectricMinimum: Int = 0,
                val torqueFrictionDifferenceMaximum: Int = 0,
                val wheel: Wheel = Wheel(),
            ) {
                data class Wheel(
                    val left: Left = Left(),
                    val right: Right = Right(),
                ) {
                    data class Left(
                        val omegaLower: Int = 0,
                        val omegaUpper: Int = 0,
                        val torque: Int = 0,
                        val torqueArbitrated: Int = 0,
                        val torqueFrictionMaximum: Int = 0,
                        val torqueFrictionMinimum: Int = 0,
                    )

                    data class Right(
                        val omegaLower: Int = 0,
                        val omegaUpper: Int = 0,
                        val torque: Int = 0,
                        val torqueArbitrated: Int = 0,
                        val torqueFrictionMaximum: Int = 0,
                        val torqueFrictionMinimum: Int = 0,
                    )
                }
            }

            data class Row2(
                val torqueDistributionFrictionRightMaximum: Int = 0,
                val torqueDistributionFrictionRightMinimum: Int = 0,
                val torqueElectricMinimum: Int = 0,
                val torqueFrictionDifferenceMaximum: Int = 0,
                val wheel: Wheel = Wheel(),
            ) {
                data class Wheel(
                    val left: Left = Left(),
                    val right: Right = Right(),
                ) {
                    data class Left(
                        val omegaLower: Int = 0,
                        val omegaUpper: Int = 0,
                        val torque: Int = 0,
                        val torqueArbitrated: Int = 0,
                        val torqueFrictionMaximum: Int = 0,
                        val torqueFrictionMinimum: Int = 0,
                    )

                    data class Right(
                        val omegaLower: Int = 0,
                        val omegaUpper: Int = 0,
                        val torque: Int = 0,
                        val torqueArbitrated: Int = 0,
                        val torqueFrictionMaximum: Int = 0,
                        val torqueFrictionMinimum: Int = 0,
                    )
                }
            }
        }
    }

    data class ElectricAxle(
        val row1: Row1 = Row1(),
        val row2: Row2 = Row2(),
    ) {
        data class Row1(
            val rotationalSpeed: Int = 0,
            val rotationalSpeedMaximumLimit: Int = 0,
            val rotationalSpeedMinimumLimit: Int = 0,
            val rotationalSpeedTarget: Int = 0,
            val torque: Int = 0,
            val torqueMaximum: Int = 0,
            val torqueMaximumLimit: Int = 0,
            val torqueMinimum: Int = 0,
            val torqueMinimumLimit: Int = 0,
            val torqueTarget: Int = 0,
        )

        data class Row2(
            val rotationalSpeed: Int = 0,
            val rotationalSpeedMaximumLimit: Int = 0,
            val rotationalSpeedMinimumLimit: Int = 0,
            val rotationalSpeedTarget: Int = 0,
            val torque: Int = 0,
            val torqueMaximum: Int = 0,
            val torqueMaximumLimit: Int = 0,
            val torqueMinimum: Int = 0,
            val torqueMinimumLimit: Int = 0,
            val torqueTarget: Int = 0,
        )
    }

    data class Steering(
        val axle: Axle = Axle(),
        val steeringWheel: SteeringWheel = SteeringWheel(),
    ) {
        data class Axle(
            val row1: Row1 = Row1(),
            val row2: Row2 = Row2(),
        ) {
            data class Row1(
                val positionOffsetTargetMode: Int = 0,
                val positionTargetMode: Int = 0,
                val rackPosition: Int = 0,
                val rackPositionOffsetTarget: Int = 0,
                val rackPositionTarget: Int = 0,
                val steerAngle: Int = 0,
                val steerAngleOffsetTarget: Int = 0,
                val steerAngleTarget: Int = 0,
            )

            data class Row2(
                val steerAngle: Int = 0,
                val steerAngleTarget: Int = 0,
                val steerAngleVelocityTarget: Int = 0,
            )
        }

        data class SteeringWheel(
            val angle: Int = 0,
            val angleTarget: Int = 0,
            val angleTargetMode: Int = 0,
            val torque: Int = 0,
            val torqueOffsetTarget: Int = 0,
            val torqueOffsetTargetMode: Int = 0,
            val torqueTarget: Int = 0,
            val torqueTargetMode: Int = 0,
        )
    }

    data class Suspension(
        val axle: Axle = Axle(),
        val dampingPrioTarget: Int = 0,
        val rollPrioTarget: Int = 0,
        val rollTorqueDistributionFrontMaximum: Int = 0,
        val rollTorqueDistributionFrontMinimum: Int = 0,
        val rollTorqueTarget: Int = 0,
    ) {
        data class Axle(
            val row1: Row1 = Row1(),
            val row2: Row2 = Row2(),
        ) {
            data class Row1(
                val rollTorque: Int = 0,
                val wheel: Wheel = Wheel(),
            ) {
                data class Wheel(
                    val left: Left = Left(),
                    val right: Right = Right(),
                ) {
                    data class Left(
                        val dampingForce: Int = 0,
                        val dampingForceTarget: Int = 0,
                        val dampingRate: Int = 0,
                        val dampingRateTarget: Int = 0,
                    )

                    data class Right(
                        val dampingForce: Int = 0,
                        val dampingForceTarget: Int = 0,
                        val dampingRate: Int = 0,
                        val dampingRateTarget: Int = 0,
                    )
                }
            }

            data class Row2(
                val rollTorque: Int = 0,
                val wheel: Wheel = Wheel(),
            ) {
                data class Wheel(
                    val left: Left = Left(),
                    val right: Right = Right(),
                ) {
                    data class Left(
                        val dampingForce: Int = 0,
                        val dampingForceTarget: Int = 0,
                        val dampingRate: Int = 0,
                        val dampingRateTarget: Int = 0,
                    )

                    data class Right(
                        val dampingForce: Int = 0,
                        val dampingForceTarget: Int = 0,
                        val dampingRate: Int = 0,
                        val dampingRateTarget: Int = 0,
                    )
                }
            }
        }
    }
}
