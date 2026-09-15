package com.monsters.mobimon.feature.auth

import androidx.lifecycle.ViewModelStore
import com.monsters.mobimon.core.domain.CompanionSettings
import com.monsters.mobimon.core.domain.CosmeticInventory
import com.monsters.mobimon.core.domain.CosmeticItem
import com.monsters.mobimon.core.domain.CosmeticSlot
import com.monsters.mobimon.core.domain.EquipResult
import com.monsters.mobimon.core.domain.PetAppearance
import com.monsters.mobimon.core.domain.PetProfile
import com.monsters.mobimon.core.domain.PetRepository
import com.monsters.mobimon.core.domain.PointAwardResult
import com.monsters.mobimon.core.domain.PointEconomy
import com.monsters.mobimon.core.domain.PointWallet
import com.monsters.mobimon.core.domain.PurchaseResult
import com.monsters.mobimon.core.domain.SettingsRepository
import com.monsters.mobimon.core.domain.VehicleSnapshot
import com.monsters.mobimon.core.domain.WriteResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.channels.Channel
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class AiCompanionViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val store = ViewModelStore()
    private val pets = FakePets()
    private val settings = FakeSettings()
    private val inventory = CosmeticInventory(setOf("friend:mobi"), mapOf(CosmeticSlot.FRIEND to "friend:mobi"))

    @Before fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After fun tearDown() {
        store.clear()
        Dispatchers.resetMain()
    }

    @Test fun readsCommittedContextAndDoesNotDuplicateActiveObserversOnRetry() =
        runTest(dispatcher) {
            val model = model(flowOf(inventory))
            runCurrent()
            assertEquals(pets.profile.value, model.state.value.profile)
            assertEquals(inventory, model.state.value.inventory)

            model.retry()
            settings.settings.value = CompanionSettings(reducedMotion = true)
            pets.profile.value = PetProfile("saved", appearance = PetAppearance.CREAM)
            runCurrent()

            assertTrue(model.state.value.settings.reducedMotion)
            assertEquals(
                PetAppearance.CREAM,
                model.state.value.profile
                    ?.appearance,
            )
            assertEquals(1, pets.initializations)
        }

    @Test fun observationFailureRetainsCommittedContextAndRetryReconnects() =
        runTest(dispatcher) {
            val fail = Channel<Unit>(Channel.CONFLATED)
            var subscriptions = 0
            val model =
                model(
                    flow {
                        subscriptions++
                        emit(inventory)
                        if (subscriptions == 1) {
                            fail.receive()
                            throw IOException("temporary inventory read failure")
                        }
                        awaitCancellation()
                    },
                )
            runCurrent()
            fail.send(Unit)
            runCurrent()
            assertTrue(model.state.value.failed)
            assertEquals(inventory, model.state.value.inventory)
            assertEquals(pets.profile.value, model.state.value.profile)

            model.retry()
            runCurrent()
            assertFalse(model.state.value.failed)
            assertEquals(2, subscriptions)
        }

    @Test fun cancelledInitializationIsNotReportedAsStorageFailure() =
        runTest(dispatcher) {
            pets.initializationFailure = CancellationException("owner ended")
            val model = model(flowOf(inventory))
            runCurrent()
            assertFalse(model.state.value.failed)
            assertNull(model.state.value.profile)

            pets.initializationFailure = null
            model.retry()
            runCurrent()
            assertEquals(pets.profile.value, model.state.value.profile)
        }

    private fun model(inventory: Flow<CosmeticInventory>) =
        AiCompanionViewModel(pets, settings, FakePoints(inventory)).also { store.put("ai", it) }

    private class FakePets : PetRepository {
        override val profile = MutableStateFlow(PetProfile("saved"))
        var initializations = 0
        var initializationFailure: Exception? = null

        override suspend fun initialize() {
            initializations++
            initializationFailure?.let { throw it }
        }

        override suspend fun setAppearance(appearance: PetAppearance) = WriteResult.Failure
    }

    private class FakeSettings : SettingsRepository {
        override val settings = MutableStateFlow(CompanionSettings())

        override suspend fun setReducedMotion(enabled: Boolean) = WriteResult.Failure

        override suspend fun setLauncherCharacterEnabled(enabled: Boolean) = WriteResult.Failure
    }

    private class FakePoints(
        override val inventory: Flow<CosmeticInventory>,
    ) : PointEconomy {
        override val wallet = flowOf(PointWallet(0))
        override val catalog = flowOf(emptyList<CosmeticItem>())

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
