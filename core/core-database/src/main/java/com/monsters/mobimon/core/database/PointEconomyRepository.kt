package com.monsters.mobimon.core.database

import android.database.sqlite.SQLiteException
import androidx.room.withTransaction
import com.monsters.mobimon.core.domain.AppUseState
import com.monsters.mobimon.core.domain.Clock
import com.monsters.mobimon.core.domain.CosmeticInventory
import com.monsters.mobimon.core.domain.CosmeticSlot
import com.monsters.mobimon.core.domain.CurrentAppUse
import com.monsters.mobimon.core.domain.CurrentVehicleEvidence
import com.monsters.mobimon.core.domain.EquipResult
import com.monsters.mobimon.core.domain.IdGenerator
import com.monsters.mobimon.core.domain.PointAwardResult
import com.monsters.mobimon.core.domain.PointEconomy
import com.monsters.mobimon.core.domain.PointQuestCatalog
import com.monsters.mobimon.core.domain.PointQuestSchedule
import com.monsters.mobimon.core.domain.PointWallet
import com.monsters.mobimon.core.domain.PurchaseResult
import com.monsters.mobimon.core.domain.QuestEvaluator
import com.monsters.mobimon.core.domain.SignalSource
import com.monsters.mobimon.core.domain.UtcClock
import com.monsters.mobimon.core.domain.VehicleSnapshot
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapNotNull
import java.time.DateTimeException
import java.time.Instant
import java.time.ZoneId

class PointEconomyRepository(
    private val database: AppDatabase,
    private val profileId: String,
    private val utcClock: UtcClock,
    private val ids: IdGenerator,
    private val source: SignalSource,
    private val vehicle: CurrentVehicleEvidence,
    private val appUse: CurrentAppUse,
    private val clock: Clock,
    private val evaluator: QuestEvaluator,
    private val quests: PointQuestCatalog,
) : PointEconomy {
    private val dao = database.economyDao()

    override val wallet =
        dao
            .observeAccount(
                profileId,
            ).mapNotNull { it?.let { account -> PointWallet(account.balance) } }

    override val inventory =
        combine(dao.observeAccount(profileId), dao.observeOwned(profileId), dao.observeEquipped(profileId)) {
                account,
                owned,
                equipped,
            ->
            if (account == null) {
                null
            } else {
                CosmeticInventory(
                    ownedItemIds = owned.mapTo(mutableSetOf()) { it.itemId },
                    equippedItemIds = equipped.associate { CosmeticSlot.valueOf(it.slot) to it.itemId },
                )
            }
        }.mapNotNull { it }

    override suspend fun purchase(
        itemId: String,
        expectedPrice: Long,
    ): PurchaseResult =
        try {
            database.withTransaction {
                if (appUse.state() != AppUseState.ALLOWED ||
                    evaluator.validateSnapshot(vehicle.snapshot(), source, clock.nowMillis()) != null
                ) {
                    return@withTransaction PurchaseResult.InteractionRestricted
                }
                val item = dao.item(itemId) ?: return@withTransaction PurchaseResult.ItemUnavailable
                if (dao.owned(profileId, itemId) != null) return@withTransaction PurchaseResult.AlreadyOwned
                if (item.price < 0) return@withTransaction PurchaseResult.ItemUnavailable
                if (item.price != expectedPrice) return@withTransaction PurchaseResult.PriceChanged(item.price)
                if (!item.isCompatible()) return@withTransaction PurchaseResult.Incompatible
                val account = dao.account(profileId) ?: return@withTransaction PurchaseResult.StorageFailure
                if (account.balance < item.price) {
                    return@withTransaction PurchaseResult.InsufficientPoints(item.price - account.balance)
                }
                if (dao.insertOwned(OwnedCosmeticEntity(profileId, itemId)) == -1L) {
                    return@withTransaction PurchaseResult.AlreadyOwned
                }
                if (dao.debit(profileId, item.price) != 1) {
                    throw SQLiteException("Account balance changed during purchase")
                }
                val purchaseId = ids.nextId()
                dao.insertLedger(
                    PointLedgerEntity(
                        id = purchaseId,
                        profileId = profileId,
                        referenceKey = "purchase:$itemId",
                        amount = -item.price,
                        occurredAtUtcMillis = utcClock.nowEpochMillis(),
                    ),
                )
                PurchaseResult.Purchased(account.balance - item.price)
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: SQLiteException) {
            PurchaseResult.StorageFailure
        }

    override suspend fun equip(itemId: String): EquipResult =
        try {
            database.withTransaction {
                if (appUse.state() != AppUseState.ALLOWED ||
                    evaluator.validateSnapshot(vehicle.snapshot(), source, clock.nowMillis()) != null
                ) {
                    return@withTransaction EquipResult.InteractionRestricted
                }
                val item = dao.item(itemId) ?: return@withTransaction EquipResult.ItemUnavailable
                if (dao.owned(profileId, itemId) == null) return@withTransaction EquipResult.NotOwned
                if (!item.isCompatible()) return@withTransaction EquipResult.Incompatible
                val slot = CosmeticSlot.valueOf(item.slot)
                if (dao.equipped(profileId, slot.name)?.itemId == itemId) {
                    return@withTransaction EquipResult.AlreadyApplied
                }
                dao.putEquipped(EquippedCosmeticEntity(profileId, slot.name, itemId))
                if (slot == CosmeticSlot.FRIEND) dao.unequipIncompatible(profileId, itemId)
                EquipResult.Applied
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: SQLiteException) {
            EquipResult.StorageFailure
        }

    override suspend fun awardQuest(
        questId: String,
        displayedSnapshot: VehicleSnapshot,
    ): PointAwardResult =
        try {
            database.withTransaction {
                val definition =
                    quests.find(questId)
                        ?: return@withTransaction PointAwardResult.QuestUnavailable
                if (definition.id != questId || questId.isBlank() || definition.rewardPoints <= 0) {
                    return@withTransaction PointAwardResult.QuestUnavailable
                }
                val current = vehicle.snapshot()
                if (appUse.state() != AppUseState.ALLOWED ||
                    evaluator.validateSnapshot(current, source, clock.nowMillis()) != null
                ) {
                    return@withTransaction PointAwardResult.InteractionRestricted
                }
                if (current != displayedSnapshot) return@withTransaction PointAwardResult.EvidenceChanged
                val completedAt = utcClock.nowEpochMillis()
                val occurrence =
                    definition.schedule.occurrenceKey(completedAt)
                        ?: return@withTransaction PointAwardResult.QuestUnavailable
                if (dao.questCompletion(profileId, questId, occurrence) != null) {
                    return@withTransaction PointAwardResult.AlreadyAwarded
                }
                val account = dao.account(profileId) ?: return@withTransaction PointAwardResult.StorageFailure
                if (account.balance > Long.MAX_VALUE - definition.rewardPoints) {
                    return@withTransaction PointAwardResult.StorageFailure
                }
                val completionId = ids.nextId()
                val completion =
                    PointQuestCompletionEntity(
                        id = completionId,
                        profileId = profileId,
                        questId = questId,
                        occurrenceKey = occurrence,
                        rewardPoints = definition.rewardPoints,
                        completedAtUtcMillis = completedAt,
                        snapshotId = current.id,
                        snapshotEpoch = current.epoch,
                        snapshotSequence = current.sequence,
                        snapshotSource = current.source.name,
                    )
                if (dao.insertQuestCompletion(completion) == -1L) {
                    return@withTransaction if (dao.questCompletion(profileId, questId, occurrence) != null) {
                        PointAwardResult.AlreadyAwarded
                    } else {
                        PointAwardResult.StorageFailure
                    }
                }
                if (dao.credit(profileId, definition.rewardPoints, Long.MAX_VALUE - definition.rewardPoints) != 1) {
                    throw SQLiteException("Account changed during point award")
                }
                dao.insertLedger(
                    PointLedgerEntity(
                        id = ids.nextId(),
                        profileId = profileId,
                        referenceKey = "quest:$questId:$occurrence",
                        amount = definition.rewardPoints,
                        occurredAtUtcMillis = completedAt,
                    ),
                )
                PointAwardResult.Awarded(definition.rewardPoints, account.balance + definition.rewardPoints, occurrence)
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: SQLiteException) {
            PointAwardResult.StorageFailure
        }

    suspend fun earnedToday(zoneId: ZoneId): Long {
        val date = Instant.ofEpochMilli(utcClock.nowEpochMillis()).atZone(zoneId).toLocalDate()
        return dao.earnedBetween(
            profileId,
            date.atStartOfDay(zoneId).toInstant().toEpochMilli(),
            date
                .plusDays(1)
                .atStartOfDay(zoneId)
                .toInstant()
                .toEpochMilli(),
        )
    }

    private suspend fun CosmeticItemEntity.isCompatible(): Boolean =
        compatibleFriendId == null ||
            dao.equipped(profileId, CosmeticSlot.FRIEND.name)?.itemId == compatibleFriendId
}

private fun PointQuestSchedule.occurrenceKey(utcMillis: Long): String? =
    when (this) {
        PointQuestSchedule.OneTime -> "once"
        is PointQuestSchedule.Daily ->
            try {
                "daily:${Instant.ofEpochMilli(utcMillis).atZone(ZoneId.of(resetZoneId)).toLocalDate()}"
            } catch (_: DateTimeException) {
                null
            }
    }
