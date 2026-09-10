package com.monsters.mobimon.runtime

import com.monsters.mobimon.core.domain.VehicleRepository

/** Owns the single vehicle observation across every feature in this process. */
class CompanionRuntime(
    private val vehicleRepository: VehicleRepository,
) {
    private var running = false

    @Synchronized
    fun start() {
        if (!running) {
            vehicleRepository.start()
            running = true
        }
    }

    @Synchronized
    fun stop() {
        if (running) {
            vehicleRepository.stop()
            running = false
        }
    }
}
