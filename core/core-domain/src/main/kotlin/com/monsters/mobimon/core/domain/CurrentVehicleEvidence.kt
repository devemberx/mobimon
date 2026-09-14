package com.monsters.mobimon.core.domain

fun interface CurrentVehicleEvidence {
    fun snapshot(): VehicleSnapshot
}
