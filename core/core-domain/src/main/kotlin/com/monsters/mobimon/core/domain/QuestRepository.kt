package com.monsters.mobimon.core.domain

import kotlinx.coroutines.flow.Flow

interface QuestRepository {
    val progress: Flow<QuestProgress>

    suspend fun start(
        type: QuestType,
        snapshot: VehicleSnapshot,
    ): QuestCommandResult

    suspend fun cancel(
        runId: String,
        expectedRevision: Long,
        snapshot: VehicleSnapshot,
    ): QuestCommandResult
}
