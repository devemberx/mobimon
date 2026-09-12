package com.monsters.mobimon.ui

import androidx.lifecycle.ViewModelStore
import com.monsters.mobimon.core.domain.CosmeticInventory
import com.monsters.mobimon.core.domain.CosmeticSlot
import com.monsters.mobimon.core.domain.EquipResult
import com.monsters.mobimon.core.domain.PointAwardResult
import com.monsters.mobimon.core.domain.PointEconomy
import com.monsters.mobimon.core.domain.PointWallet
import com.monsters.mobimon.core.domain.PurchaseResult
import com.monsters.mobimon.core.domain.VehicleSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class CosmeticInventoryViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val store = ViewModelStore()

    @Before fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After fun tearDown() {
        store.clear()
        Dispatchers.resetMain()
    }

    @Test fun retryResubscribesAfterInventoryObservationFails() =
        runTest(dispatcher) {
            val expected = CosmeticInventory(setOf("friend:mobi"), mapOf(CosmeticSlot.FRIEND to "friend:mobi"))
            var subscriptions = 0
            val inventory: Flow<CosmeticInventory> =
                flow {
                    subscriptions++
                    if (subscriptions == 1) throw IOException("temporary Room failure")
                    emit(expected)
                }
            val model = CosmeticInventoryViewModel(FakePoints(inventory)).also { store.put("inventory", it) }

            runCurrent()
            assertEquals(true, model.state.value.loadFailed)
            assertEquals(null, model.state.value.inventory)

            model.retry()
            runCurrent()
            assertEquals(2, subscriptions)
            assertEquals(false, model.state.value.loadFailed)
            assertEquals(expected, model.state.value.inventory)
        }

    private class FakePoints(
        override val inventory: Flow<CosmeticInventory>,
    ) : PointEconomy {
        override val wallet = flowOf(PointWallet(0))

        override suspend fun purchase(
            itemId: String,
            expectedPrice: Long,
        ) = PurchaseResult.ItemUnavailable

        override suspend fun equip(itemId: String): EquipResult = EquipResult.ItemUnavailable

        override suspend fun awardQuest(
            questId: String,
            displayedSnapshot: VehicleSnapshot,
        ): PointAwardResult = PointAwardResult.QuestUnavailable
    }
}
