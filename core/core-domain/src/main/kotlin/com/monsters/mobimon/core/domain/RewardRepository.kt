package com.monsters.mobimon.core.domain

interface RewardRepository {
    suspend fun complete(
        runId: String,
        expectedRevision: Long,
        snapshot: VehicleSnapshot,
    ): RewardResult
}
