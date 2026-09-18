package com.monsters.mobimon.core.domain

import kotlinx.coroutines.flow.Flow

data class PointWallet(
    val balance: Long,
)

interface PointEconomy {
    val wallet: Flow<PointWallet>
    val inventory: Flow<CosmeticInventory>
    val catalog: Flow<List<CosmeticItem>>
    val completedQuestIds: Flow<Set<String>> get() = kotlinx.coroutines.flow.emptyFlow()
    val driveEvaluation: Flow<DriveEvaluationData> get() = kotlinx.coroutines.flow.flowOf(DriveEvaluationData())

    fun updateDriveEvaluation(data: DriveEvaluationData) {}

    suspend fun purchase(
        itemId: String,
        expectedPrice: Long,
    ): PurchaseResult

    suspend fun equip(itemId: String): EquipResult

    suspend fun unequip(
        slot: CosmeticSlot,
        friendId: String? = null,
    ): EquipResult = EquipResult.Applied

    suspend fun awardQuest(
        questId: String,
        displayedSnapshot: VehicleSnapshot,
    ): PointAwardResult
}
