// Generated from docs/vss.csv.
// Regenerate with: python3 core/core-vss/tools/generate_vss_signals.py

package com.monsters.mobimon.core.vss.generated

data class VssBody(
    val bodyType: String = "",
    val hood: Hood = Hood(),
    val horn: Horn = Horn(),
    val isAutoPowerOptimize: Boolean = false,
    val lights: Lights = Lights(),
    val mirrors: Mirrors = Mirrors(),
    val powerOptimizeLevel: Int = 0,
    val raindetection: Raindetection = Raindetection(),
    val rearMainSpoilerPosition: Float = 0f,
    val trunk: Trunk = Trunk(),
    val windshield: Windshield = Windshield(),
) {
    data class Hood(
        val isOpen: Boolean = false,
        val position: Int = 0,
        val switch: String = "",
    )

    data class Horn(
        val isActive: Boolean = false,
    )

    data class Lights(
        val backup: Backup = Backup(),
        val beam: Beam = Beam(),
        val brake: Brake = Brake(),
        val directionIndicator: DirectionIndicator = DirectionIndicator(),
        val fog: Fog = Fog(),
        val hazard: Hazard = Hazard(),
        val isHighBeamSwitchOn: Boolean = false,
        val licensePlate: LicensePlate = LicensePlate(),
        val lightSwitch: String = "",
        val parking: Parking = Parking(),
        val running: Running = Running(),
    ) {
        data class Backup(
            val isDefect: Boolean = false,
            val isOn: Boolean = false,
        )

        data class Beam(
            val high: High = High(),
            val low: Low = Low(),
        ) {
            data class High(
                val isDefect: Boolean = false,
                val isOn: Boolean = false,
            )

            data class Low(
                val isDefect: Boolean = false,
                val isOn: Boolean = false,
            )
        }

        data class Brake(
            val isActive: String = "",
            val isDefect: Boolean = false,
        )

        data class DirectionIndicator(
            val left: Left = Left(),
            val right: Right = Right(),
        ) {
            data class Left(
                val isDefect: Boolean = false,
                val isSignaling: Boolean = false,
            )

            data class Right(
                val isDefect: Boolean = false,
                val isSignaling: Boolean = false,
            )
        }

        data class Fog(
            val front: Front = Front(),
            val rear: Rear = Rear(),
        ) {
            data class Front(
                val isDefect: Boolean = false,
                val isOn: Boolean = false,
            )

            data class Rear(
                val isDefect: Boolean = false,
                val isOn: Boolean = false,
            )
        }

        data class Hazard(
            val isDefect: Boolean = false,
            val isSignaling: Boolean = false,
        )

        data class LicensePlate(
            val isDefect: Boolean = false,
            val isOn: Boolean = false,
        )

        data class Parking(
            val isDefect: Boolean = false,
            val isOn: Boolean = false,
        )

        data class Running(
            val isDefect: Boolean = false,
            val isOn: Boolean = false,
        )
    }

    data class Mirrors(
        val driverSide: DriverSide = DriverSide(),
        val passengerSide: PassengerSide = PassengerSide(),
    ) {
        data class DriverSide(
            val isFolded: Boolean = false,
            val isHeatingOn: Boolean = false,
            val isLocked: Boolean = false,
            val pan: Int = 0,
            val tilt: Int = 0,
            val yaw: Int = 0,
        )

        data class PassengerSide(
            val isFolded: Boolean = false,
            val isHeatingOn: Boolean = false,
            val isLocked: Boolean = false,
            val pan: Int = 0,
            val tilt: Int = 0,
            val yaw: Int = 0,
        )
    }

    data class Raindetection(
        val intensity: Int = 0,
    )

    data class Trunk(
        val front: Front = Front(),
        val rear: Rear = Rear(),
    ) {
        data class Front(
            val isLightOn: Boolean = false,
            val isLocked: Boolean = false,
            val isOpen: Boolean = false,
            val position: Int = 0,
            val switch: String = "",
        )

        data class Rear(
            val isLightOn: Boolean = false,
            val isLocked: Boolean = false,
            val isOpen: Boolean = false,
            val position: Int = 0,
            val switch: String = "",
        )
    }

    data class Windshield(
        val front: Front = Front(),
        val rear: Rear = Rear(),
    ) {
        data class Front(
            val isHeatingOn: Boolean = false,
            val washerFluid: WasherFluid = WasherFluid(),
            val wiping: Wiping = Wiping(),
        ) {
            data class WasherFluid(
                val isLevelLow: Boolean = false,
                val level: Int = 0,
            )

            data class Wiping(
                val intensity: Int = 0,
                val isWipersWorn: Boolean = false,
                val mode: String = "",
                val system: System = System(),
                val wiperWear: Int = 0,
            ) {
                data class System(
                    val actualPosition: Float = 0f,
                    val driveCurrent: Float = 0f,
                    val frequency: Int = 0,
                    val isBlocked: Boolean = false,
                    val isEndingWipeCycle: Boolean = false,
                    val isOverheated: Boolean = false,
                    val isPositionReached: Boolean = false,
                    val isWiperError: Boolean = false,
                    val isWiping: Boolean = false,
                    val mode: String = "",
                    val targetPosition: Float = 0f,
                )
            }
        }

        data class Rear(
            val isHeatingOn: Boolean = false,
            val washerFluid: WasherFluid = WasherFluid(),
            val wiping: Wiping = Wiping(),
        ) {
            data class WasherFluid(
                val isLevelLow: Boolean = false,
                val level: Int = 0,
            )

            data class Wiping(
                val intensity: Int = 0,
                val isWipersWorn: Boolean = false,
                val mode: String = "",
                val system: System = System(),
                val wiperWear: Int = 0,
            ) {
                data class System(
                    val actualPosition: Float = 0f,
                    val driveCurrent: Float = 0f,
                    val frequency: Int = 0,
                    val isBlocked: Boolean = false,
                    val isEndingWipeCycle: Boolean = false,
                    val isOverheated: Boolean = false,
                    val isPositionReached: Boolean = false,
                    val isWiperError: Boolean = false,
                    val isWiping: Boolean = false,
                    val mode: String = "",
                    val targetPosition: Float = 0f,
                )
            }
        }
    }
}
