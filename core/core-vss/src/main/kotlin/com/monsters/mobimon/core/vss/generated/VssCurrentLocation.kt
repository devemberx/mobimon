// Generated from docs/vss.csv.
// Regenerate with: python3 core/core-vss/tools/generate_vss_signals.py

package com.monsters.mobimon.core.vss.generated

data class VssCurrentLocation(
    val altitude: Float = 0f,
    val gNSSReceiver: GNSSReceiver = GNSSReceiver(),
    val heading: Float = 0f,
    val horizontalAccuracy: Float = 0f,
    val latitude: Float = 0f,
    val longitude: Float = 0f,
    val timestamp: String = "",
    val verticalAccuracy: Float = 0f,
) {
    data class GNSSReceiver(
        val fixType: String = "",
        val mountingPosition: MountingPosition = MountingPosition(),
    ) {
        data class MountingPosition(
            val x: Int = 0,
            val y: Int = 0,
            val z: Int = 0,
        )
    }
}
