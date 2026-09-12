package com.monsters.mobimon.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.monsters.mobimon.core.domain.AppUseState
import com.monsters.mobimon.core.domain.Clock
import com.monsters.mobimon.core.domain.CosmeticSlot
import com.monsters.mobimon.core.domain.CurrentAppUse
import com.monsters.mobimon.core.domain.CurrentVehicleEvidence
import com.monsters.mobimon.core.domain.DrivingState
import com.monsters.mobimon.core.domain.EquipResult
import com.monsters.mobimon.core.domain.IdGenerator
import com.monsters.mobimon.core.domain.PointAwardResult
import com.monsters.mobimon.core.domain.PointQuestCatalog
import com.monsters.mobimon.core.domain.PointQuestDefinition
import com.monsters.mobimon.core.domain.PointQuestSchedule
import com.monsters.mobimon.core.domain.PurchaseResult
import com.monsters.mobimon.core.domain.QuestEvaluator
import com.monsters.mobimon.core.domain.SignalQuality
import com.monsters.mobimon.core.domain.SignalSource
import com.monsters.mobimon.core.domain.UtcClock
import com.monsters.mobimon.core.domain.VehicleSnapshot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.SQLiteMode
import java.time.ZoneId
import java.util.concurrent.atomic.AtomicInteger

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@SQLiteMode(SQLiteMode.Mode.NATIVE)
class PointEconomyRepositoryTest {
    private lateinit var database: AppDatabase
    private lateinit var repository: PointEconomyRepository
    private val ids = AtomicInteger()
    private var appUse = AppUseState.ALLOWED
    private var vehicle =
        VehicleSnapshot("current", "epoch", 1, 10_000, SignalSource.REAL, DrivingState.PARKED, SignalQuality.VALID)
    private var utcNow = 1_800_000_000_000L
    private var catalog: PointQuestDefinition? = null

    @Before
    fun setUp() =
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<Context>()
            database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()
            database.companionDao().insertProfile(PetProfileEntity("profile", "GOLDEN", 80))
            database.economyDao().insertAccount(PointAccountEntity("profile", 100))
            repository =
                PointEconomyRepository(
                    database,
                    "profile",
                    UtcClock { utcNow },
                    IdGenerator { "entry-${ids.incrementAndGet()}" },
                    SignalSource.REAL,
                    CurrentVehicleEvidence { vehicle },
                    CurrentAppUse { appUse },
                    Clock { 10_000 },
                    QuestEvaluator(15_000),
                    PointQuestCatalog { id -> catalog?.takeIf { it.id == id } },
                )
        }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun purchaseChargesOnceAndEquipRequiresASeparateOwnedItemCommand() =
        runBlocking {
            database.economyDao().insertItem(CosmeticItemEntity("hat", CosmeticSlot.ACCESSORY.name, 30, null))

            assertEquals(PurchaseResult.PriceChanged(30), repository.purchase("hat", expectedPrice = 25))
            assertEquals(PurchaseResult.Purchased(70), repository.purchase("hat", expectedPrice = 30))
            assertEquals(PurchaseResult.AlreadyOwned, repository.purchase("hat", expectedPrice = 25))
            assertEquals(70, repository.wallet.first().balance)
            assertEquals(1, database.economyDao().ledger("profile").size)
            assertTrue(database.economyDao().owned("profile", "hat") != null)
            assertNull(database.economyDao().equipped("profile", CosmeticSlot.ACCESSORY.name))

            assertEquals(EquipResult.Applied, repository.equip("hat"))
            assertEquals("hat", database.economyDao().equipped("profile", CosmeticSlot.ACCESSORY.name)?.itemId)
            assertEquals(70, repository.wallet.first().balance)
        }

    @Test
    fun purchaseRejectsRestrictedOrUnknownInteractionWithoutDebit() =
        runBlocking {
            database.economyDao().insertItem(CosmeticItemEntity("hat", CosmeticSlot.ACCESSORY.name, 30, null))
            appUse = AppUseState.RESTRICTED
            assertEquals(PurchaseResult.InteractionRestricted, repository.purchase("hat", 30))
            appUse = AppUseState.ALLOWED
            vehicle = vehicle.copy(drivingState = DrivingState.UNKNOWN)
            assertEquals(PurchaseResult.InteractionRestricted, repository.purchase("hat", 30))
            assertEquals(100L, repository.wallet.first().balance)
            assertTrue(database.economyDao().ledger("profile").isEmpty())
        }

    @Test
    fun oneTimeAwardCreditsOnceWithEvidenceAndLedger() =
        runBlocking {
            catalog = PointQuestDefinition("welcome", 25, PointQuestSchedule.OneTime)
            assertEquals(PointAwardResult.Awarded(25, 125, "once"), repository.awardQuest("welcome", vehicle))
            assertEquals(PointAwardResult.AlreadyAwarded, repository.awardQuest("welcome", vehicle))
            assertEquals(125L, repository.wallet.first().balance)
            assertEquals(1, database.economyDao().questCompletions("profile").size)
            assertEquals(1, database.economyDao().ledger("profile").size)
        }

    @Test
    fun dailyAwardUsesDefinedZoneAndOccurrenceWhileRejectingChangedEvidence() =
        runBlocking {
            catalog = PointQuestDefinition("daily-check", 10, PointQuestSchedule.Daily("Asia/Seoul"))
            val displayed = vehicle
            vehicle = vehicle.copy(id = "new-card", sequence = 2)
            assertEquals(PointAwardResult.EvidenceChanged, repository.awardQuest("daily-check", displayed))
            val first = repository.awardQuest("daily-check", vehicle)
            assertTrue(first is PointAwardResult.Awarded)
            assertEquals(PointAwardResult.AlreadyAwarded, repository.awardQuest("daily-check", vehicle))
            utcNow += 86_400_000L
            val second = repository.awardQuest("daily-check", vehicle)
            assertTrue(second is PointAwardResult.Awarded)
            assertEquals(120L, repository.wallet.first().balance)
            assertEquals(2, database.economyDao().questCompletions("profile").size)
        }

    @Test
    fun ledgerFailureRollsBackPurchaseOwnershipAndDebit() =
        runBlocking {
            database.economyDao().insertItem(CosmeticItemEntity("hat", CosmeticSlot.ACCESSORY.name, 30, null))
            database.economyDao().insertLedger(PointLedgerEntity("entry-1", "profile", "collision", 0, utcNow))

            assertEquals(PurchaseResult.StorageFailure, repository.purchase("hat", 30))
            assertEquals(100L, repository.wallet.first().balance)
            assertNull(database.economyDao().owned("profile", "hat"))
        }

    @Test
    fun earnedTodayCountsCreditsEvenAfterPurchase() =
        runBlocking {
            catalog = PointQuestDefinition("welcome", 25, PointQuestSchedule.OneTime)
            assertTrue(repository.awardQuest("welcome", vehicle) is PointAwardResult.Awarded)
            database.economyDao().insertItem(CosmeticItemEntity("hat", CosmeticSlot.ACCESSORY.name, 20, null))
            assertEquals(PurchaseResult.Purchased(105), repository.purchase("hat", 20))
            assertEquals(25L, repository.earnedToday(ZoneId.of("Asia/Seoul")))
            assertEquals(105L, repository.wallet.first().balance)
        }

    @Test
    fun changingFriendUnequipsIncompatibleAccessoryWithoutRefund() =
        runBlocking {
            val dao = database.economyDao()
            dao.insertItem(CosmeticItemEntity("friend:mobi", "FRIEND", 0, null))
            dao.insertItem(CosmeticItemEntity("friend:luna", "FRIEND", 0, null))
            dao.insertItem(CosmeticItemEntity("mobi-hat", "ACCESSORY", 20, "friend:mobi"))
            dao.insertOwned(OwnedCosmeticEntity("profile", "friend:mobi"))
            dao.insertOwned(OwnedCosmeticEntity("profile", "friend:luna"))
            dao.insertOwned(OwnedCosmeticEntity("profile", "mobi-hat"))
            assertEquals(EquipResult.Applied, repository.equip("friend:mobi"))
            assertEquals(EquipResult.Applied, repository.equip("mobi-hat"))
            assertEquals(EquipResult.Applied, repository.equip("friend:luna"))
            assertNull(dao.equipped("profile", "ACCESSORY"))
            assertTrue(dao.owned("profile", "mobi-hat") != null)
            assertEquals(100L, repository.wallet.first().balance)
        }
}
