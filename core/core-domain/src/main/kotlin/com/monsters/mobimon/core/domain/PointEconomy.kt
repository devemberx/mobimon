package com.monsters.mobimon.core.domain

import kotlinx.coroutines.flow.Flow

fun interface UtcClock {
    fun nowEpochMillis(): Long
}

enum class CosmeticSlot { FRIEND, OUTFIT, ACCESSORY, BACKGROUND }

data class PointWallet(
    val balance: Long,
)

data class CosmeticItem(
    val id: String,
    val slot: CosmeticSlot,
    val price: Long,
    val compatibleFriendId: String? = null,
)

data class CosmeticInventory(
    val ownedItemIds: Set<String>,
    val equippedItemIds: Map<CosmeticSlot, String>,
)

sealed interface PointQuestSchedule {
    data object OneTime : PointQuestSchedule

    data class Daily(
        val resetZoneId: String,
    ) : PointQuestSchedule
}

data class PointQuestDefinition(
    val id: String,
    val rewardPoints: Long,
    val schedule: PointQuestSchedule,
)

fun interface PointQuestCatalog {
    fun find(questId: String): PointQuestDefinition?
}

sealed interface PointAwardResult {
    data class Awarded(
        val points: Long,
        val resultingBalance: Long,
        val occurrenceKey: String,
    ) : PointAwardResult

    data object AlreadyAwarded : PointAwardResult

    data object QuestUnavailable : PointAwardResult

    data object InteractionRestricted : PointAwardResult

    data object EvidenceChanged : PointAwardResult

    data object StorageFailure : PointAwardResult
}

sealed interface PurchaseResult {
    data class Purchased(
        val resultingBalance: Long,
    ) : PurchaseResult

    data object AlreadyOwned : PurchaseResult

    data object ItemUnavailable : PurchaseResult

    data class PriceChanged(
        val currentPrice: Long,
    ) : PurchaseResult

    data class InsufficientPoints(
        val shortfall: Long,
    ) : PurchaseResult

    data object Incompatible : PurchaseResult

    data object InteractionRestricted : PurchaseResult

    data object StorageFailure : PurchaseResult
}

sealed interface EquipResult {
    data object Applied : EquipResult

    data object AlreadyApplied : EquipResult

    data object NotOwned : EquipResult

    data object ItemUnavailable : EquipResult

    data object Incompatible : EquipResult

    data object InteractionRestricted : EquipResult

    data object StorageFailure : EquipResult
}

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
