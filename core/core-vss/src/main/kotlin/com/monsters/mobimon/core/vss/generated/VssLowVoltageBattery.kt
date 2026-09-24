// Generated from docs/vss.csv.
// Regenerate with: python3 core/core-vss/tools/generate_vss_signals.py

package com.monsters.mobimon.core.vss.generated

data class VssLowVoltageBattery(
    val currentCurrent: Float = 0f,
    val currentVoltage: Float = 0f,
    val nominalCapacity: Int = 0,
    val nominalVoltage: Int = 0,
)
