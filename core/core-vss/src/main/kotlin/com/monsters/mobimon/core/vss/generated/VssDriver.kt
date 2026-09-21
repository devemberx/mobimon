// Generated from docs/vss.csv.
// Regenerate with: python3 core/core-vss/tools/generate_vss_signals.py

package com.monsters.mobimon.core.vss.generated

data class VssDriver(
    val attentiveProbability: Float = 0f,
    val distractionLevel: Float = 0f,
    val fatigueLevel: Float = 0f,
    val heartRate: Int = 0,
    val isEyesOnRoad: Boolean = false,
    val isHandsOnWheel: Boolean = false,
)
