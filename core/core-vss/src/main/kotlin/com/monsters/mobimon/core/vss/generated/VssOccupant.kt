// Generated from docs/vss.csv.
// Regenerate with: python3 core/core-vss/tools/generate_vss_signals.py

package com.monsters.mobimon.core.vss.generated

data class VssOccupant(
    val row1: Row1 = Row1(),
    val row2: Row2 = Row2(),
) {
    data class Row1(
        val driverSide: DriverSide = DriverSide(),
        val middle: Middle = Middle(),
        val passengerSide: PassengerSide = PassengerSide(),
    ) {
        data class DriverSide(
            val headPosition: HeadPosition = HeadPosition(),
            val identifier: Identifier = Identifier(),
            val midEyeGaze: MidEyeGaze = MidEyeGaze(),
        ) {
            data class HeadPosition(
                val pitch: Float = 0f,
                val roll: Float = 0f,
                val x: Int = 0,
                val y: Int = 0,
                val yaw: Float = 0f,
                val z: Int = 0,
            )

            data class Identifier(
                val issuer: String = "",
                val subject: String = "",
            )

            data class MidEyeGaze(
                val azimuth: Float = 0f,
                val elevation: Float = 0f,
            )
        }

        data class Middle(
            val headPosition: HeadPosition = HeadPosition(),
            val identifier: Identifier = Identifier(),
            val midEyeGaze: MidEyeGaze = MidEyeGaze(),
        ) {
            data class HeadPosition(
                val pitch: Float = 0f,
                val roll: Float = 0f,
                val x: Int = 0,
                val y: Int = 0,
                val yaw: Float = 0f,
                val z: Int = 0,
            )

            data class Identifier(
                val issuer: String = "",
                val subject: String = "",
            )

            data class MidEyeGaze(
                val azimuth: Float = 0f,
                val elevation: Float = 0f,
            )
        }

        data class PassengerSide(
            val headPosition: HeadPosition = HeadPosition(),
            val identifier: Identifier = Identifier(),
            val midEyeGaze: MidEyeGaze = MidEyeGaze(),
        ) {
            data class HeadPosition(
                val pitch: Float = 0f,
                val roll: Float = 0f,
                val x: Int = 0,
                val y: Int = 0,
                val yaw: Float = 0f,
                val z: Int = 0,
            )

            data class Identifier(
                val issuer: String = "",
                val subject: String = "",
            )

            data class MidEyeGaze(
                val azimuth: Float = 0f,
                val elevation: Float = 0f,
            )
        }
    }

    data class Row2(
        val driverSide: DriverSide = DriverSide(),
        val middle: Middle = Middle(),
        val passengerSide: PassengerSide = PassengerSide(),
    ) {
        data class DriverSide(
            val headPosition: HeadPosition = HeadPosition(),
            val identifier: Identifier = Identifier(),
            val midEyeGaze: MidEyeGaze = MidEyeGaze(),
        ) {
            data class HeadPosition(
                val pitch: Float = 0f,
                val roll: Float = 0f,
                val x: Int = 0,
                val y: Int = 0,
                val yaw: Float = 0f,
                val z: Int = 0,
            )

            data class Identifier(
                val issuer: String = "",
                val subject: String = "",
            )

            data class MidEyeGaze(
                val azimuth: Float = 0f,
                val elevation: Float = 0f,
            )
        }

        data class Middle(
            val headPosition: HeadPosition = HeadPosition(),
            val identifier: Identifier = Identifier(),
            val midEyeGaze: MidEyeGaze = MidEyeGaze(),
        ) {
            data class HeadPosition(
                val pitch: Float = 0f,
                val roll: Float = 0f,
                val x: Int = 0,
                val y: Int = 0,
                val yaw: Float = 0f,
                val z: Int = 0,
            )

            data class Identifier(
                val issuer: String = "",
                val subject: String = "",
            )

            data class MidEyeGaze(
                val azimuth: Float = 0f,
                val elevation: Float = 0f,
            )
        }

        data class PassengerSide(
            val headPosition: HeadPosition = HeadPosition(),
            val identifier: Identifier = Identifier(),
            val midEyeGaze: MidEyeGaze = MidEyeGaze(),
        ) {
            data class HeadPosition(
                val pitch: Float = 0f,
                val roll: Float = 0f,
                val x: Int = 0,
                val y: Int = 0,
                val yaw: Float = 0f,
                val z: Int = 0,
            )

            data class Identifier(
                val issuer: String = "",
                val subject: String = "",
            )

            data class MidEyeGaze(
                val azimuth: Float = 0f,
                val elevation: Float = 0f,
            )
        }
    }
}
