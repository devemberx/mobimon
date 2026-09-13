package com.monsters.mobimon.ui

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
class PointBalanceViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val store = ViewModelStore()

    @Before fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After fun tearDown() {
        store.clear()
        Dispatchers.resetMain()
    }

    @Test fun zeroIsACommittedBalanceOnlyAfterWalletEmits() =
        runTest(dispatcher) {
            val wallet = MutableStateFlow(PointWallet(0))
            val model = PointBalanceViewModel(FakePoints(wallet)).also { store.put("balance", it) }
            assertEquals(PointBalanceState.Loading, model.state.value)
            runCurrent()
            assertEquals(PointBalanceState.Ready(0), model.state.value)
        }

    @Test fun walletFailureIsNotShownAsZero() =
        runTest(dispatcher) {
            val failed: Flow<PointWallet> = flow { throw IOException("disk unavailable") }
            val model = PointBalanceViewModel(FakePoints(failed)).also { store.put("balance", it) }
            runCurrent()
            assertEquals(PointBalanceState.Failed, model.state.value)
        }

    private class FakePoints(
        override val wallet: Flow<PointWallet>,
    ) : PointEconomy {
        override val inventory = flowOf(CosmeticInventory(emptySet(), emptyMap()))
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
