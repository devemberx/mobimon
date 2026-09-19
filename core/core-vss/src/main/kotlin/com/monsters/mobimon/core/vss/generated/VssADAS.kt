// Generated from docs/vss.csv.
// Regenerate with: python3 core/core-vss/tools/generate_vss_signals.py

package com.monsters.mobimon.core.vss.generated

data class VssADAS(
    val abs: ABS = ABS(),
    val activeAutonomyLevel: String = "",
    val cruiseControl: CruiseControl = CruiseControl(),
    val dms: DMS = DMS(),
    val eba: EBA = EBA(),
    val ebd: EBD = EBD(),
    val esc: ESC = ESC(),
    val isAutoPowerOptimize: Boolean = false,
    val laneDepartureDetection: LaneDepartureDetection = LaneDepartureDetection(),
    val obstacleDetection: ObstacleDetection = ObstacleDetection(),
    val powerOptimizeLevel: Int = 0,
    val supportedAutonomyLevel: String = "",
    val tcs: TCS = TCS(),
) {
    data class ABS(
        val isEnabled: Boolean = false,
        val isEngaged: Boolean = false,
        val isError: Boolean = false,
    )

    data class CruiseControl(
        val adaptiveDistanceSet: Float = 0f,
        val adaptiveIntervalSet: Int = 0,
        val isActive: Boolean = false,
        val isAdaptive: Boolean = false,
        val isEnabled: Boolean = false,
        val isError: Boolean = false,
        val speedSet: Float = 0f,
    )

    data class DMS(
        val isEnabled: Boolean = false,
        val isError: Boolean = false,
        val isWarning: Boolean = false,
    )

    data class EBA(
        val isEnabled: Boolean = false,
        val isEngaged: Boolean = false,
        val isError: Boolean = false,
    )

    data class EBD(
        val isEnabled: Boolean = false,
        val isEngaged: Boolean = false,
        val isError: Boolean = false,
    )

    data class ESC(
        val isEnabled: Boolean = false,
        val isEngaged: Boolean = false,
        val isError: Boolean = false,
        val isStrongCrossWindDetected: Boolean = false,
        val roadFriction: RoadFriction = RoadFriction(),
    ) {
        data class RoadFriction(
            val lowerBound: Float = 0f,
            val mostProbable: Float = 0f,
            val upperBound: Float = 0f,
        )
    }

    data class LaneDepartureDetection(
        val isEnabled: Boolean = false,
        val isError: Boolean = false,
        val isWarning: Boolean = false,
    )

    data class ObstacleDetection(
        val front: Front = Front(),
        val rear: Rear = Rear(),
    ) {
        data class Front(
            val center: Center = Center(),
            val left: Left = Left(),
            val right: Right = Right(),
        ) {
            data class Center(
                val distance: Float = 0f,
                val isEnabled: Boolean = false,
                val isError: Boolean = false,
                val isWarning: Boolean = false,
                val timeGap: Int = 0,
                val warningType: String = "",
            )

            data class Left(
                val distance: Float = 0f,
                val isEnabled: Boolean = false,
                val isError: Boolean = false,
                val isWarning: Boolean = false,
                val timeGap: Int = 0,
                val warningType: String = "",
            )

            data class Right(
                val distance: Float = 0f,
                val isEnabled: Boolean = false,
                val isError: Boolean = false,
                val isWarning: Boolean = false,
                val timeGap: Int = 0,
                val warningType: String = "",
            )
        }

        data class Rear(
            val center: Center = Center(),
            val left: Left = Left(),
            val right: Right = Right(),
        ) {
            data class Center(
                val distance: Float = 0f,
                val isEnabled: Boolean = false,
                val isError: Boolean = false,
                val isWarning: Boolean = false,
                val timeGap: Int = 0,
                val warningType: String = "",
            )

            data class Left(
                val distance: Float = 0f,
                val isEnabled: Boolean = false,
                val isError: Boolean = false,
                val isWarning: Boolean = false,
                val timeGap: Int = 0,
                val warningType: String = "",
            )

            data class Right(
                val distance: Float = 0f,
                val isEnabled: Boolean = false,
                val isError: Boolean = false,
                val isWarning: Boolean = false,
                val timeGap: Int = 0,
                val warningType: String = "",
            )
        }
    }

    data class TCS(
        val isEnabled: Boolean = false,
        val isEngaged: Boolean = false,
        val isError: Boolean = false,
    )
}
