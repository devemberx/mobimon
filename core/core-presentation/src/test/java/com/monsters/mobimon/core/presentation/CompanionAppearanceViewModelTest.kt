package com.monsters.mobimon.core.presentation

import androidx.lifecycle.ViewModelStore
import com.monsters.mobimon.core.domain.CosmeticInventory
import com.monsters.mobimon.core.domain.EquipResult
import com.monsters.mobimon.core.domain.PointAwardResult
import com.monsters.mobimon.core.domain.PointEconomy
import com.monsters.mobimon.core.domain.PointWallet
import com.monsters.mobimon.core.domain.PurchaseResult
import com.monsters.mobimon.core.domain.VehicleSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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
class CompanionAppearanceViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val store = ViewModelStore()

    @Before fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After fun tearDown() {
        store.clear()
        Dispatchers.resetMain()
    }

    @Test fun committedEquipmentReachesEveryObserverAndLaterSubscribers() =
        runTest(dispatcher) {
            val inventory =
                MutableStateFlow(
                    CosmeticInventory(
                        setOf("friend:mobi", "friend:luna"),
                        mapOf(
                            com.monsters.mobimon.core.domain.CosmeticSlot.FRIEND to "friend:mobi",
                        ),
                    ),
                )
            val points = FakePoints(inventory)
            val vehicle = CompanionAppearanceViewModel(points).also { store.put("vehicle", it) }
            val quest = CompanionAppearanceViewModel(points).also { store.put("quest", it) }
            runCurrent()
            inventory.value =
                CosmeticInventory(
                    setOf("friend:luna", "accessory:luna_cap"),
                    mapOf(
                        com.monsters.mobimon.core.domain.CosmeticSlot.FRIEND to "friend:luna",
                        com.monsters.mobimon.core.domain.CosmeticSlot.ACCESSORY to "accessory:luna_cap",
                        com.monsters.mobimon.core.domain.CosmeticSlot.BACKGROUND to "background:night",
                    ),
                )
            runCurrent()
            assertEquals("friend:luna", vehicle.state.value.friendId)
            assertEquals("accessory:luna_cap", quest.state.value.accessoryId)
            assertEquals("background:night", quest.state.value.backgroundId)
            assertEquals(vehicle.state.value, quest.state.value)
            val reopened = CompanionAppearanceViewModel(points).also { store.put("reopened", it) }
            runCurrent()
            assertEquals(quest.state.value, reopened.state.value)
        }

    @Test fun failedObservationRetainsCommittedLookAndRetryDoesNotDuplicateCollectors() =
        runTest(dispatcher) {
            var subscriptions = 0
            val inventory: Flow<CosmeticInventory> =
                flow {
                    subscriptions++
                    emit(
                        CosmeticInventory(
                            setOf("friend:luna"),
                            mapOf(
                                com.monsters.mobimon.core.domain.CosmeticSlot.FRIEND to "friend:luna",
                            ),
                        ),
                    )
                    if (subscriptions == 1) throw IOException("disk unavailable")
                    kotlinx.coroutines.awaitCancellation()
                }
            val model = CompanionAppearanceViewModel(FakePoints(inventory)).also { store.put("look", it) }
            runCurrent()
            assertEquals(true, model.state.value.failed)
            assertEquals("friend:luna", model.state.value.friendId)
            model.retry()
            model.retry()
            runCurrent()
            assertEquals(false, model.state.value.failed)
            assertEquals(2, subscriptions)
        }

    private class FakePoints(
        override val inventory: Flow<CosmeticInventory>,
    ) : PointEconomy {
        override val wallet = flowOf(PointWallet(0))
        override val catalog = flowOf(emptyList<com.monsters.mobimon.core.domain.CosmeticItem>())

        override suspend fun purchase(
            itemId: String,
            expectedPrice: Long,
        ) = PurchaseResult.ItemUnavailable

        override suspend fun equip(itemId: String) = EquipResult.ItemUnavailable

        override suspend fun awardQuest(
            questId: String,
            displayedSnapshot: VehicleSnapshot,
        ) = PointAwardResult.QuestUnavailable
    }
}
