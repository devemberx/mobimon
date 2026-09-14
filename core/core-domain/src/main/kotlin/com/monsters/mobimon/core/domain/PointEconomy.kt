package com.monsters.mobimon.core.domain

import kotlinx.coroutines.flow.Flow

data class PointWallet(
    val balance: Long,
)

interface PointEconomy {
    val wallet: Flow<PointWallet>
    val inventory: Flow<CosmeticInventory>
    val catalog: Flow<List<CosmeticItem>>

    suspend fun purchase(
        itemId: String,
        expectedPrice: Long,
    ): PurchaseResult

    suspend fun equip(itemId: String): EquipResult

    suspend fun awardQuest(
        questId: String,
        displayedSnapshot: VehicleSnapshot,
    ): PointAwardResult
}
