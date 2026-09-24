// Generated from docs/vss.csv.
// Regenerate with: python3 core/core-vss/tools/generate_vss_signals.py

package com.monsters.mobimon.core.vss.generated

data class VssService(
    val distanceToService: Float = 0f,
    val isServiceDue: Boolean = false,
    val timeToService: Int = 0,
)
