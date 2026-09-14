package com.monsters.mobimon.core.domain

import kotlinx.coroutines.flow.StateFlow

interface VehicleRepository {
    val snapshots: StateFlow<VehicleSnapshot>

    fun start()

    fun stop()
}
