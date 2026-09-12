package com.monsters.mobimon.feature.pet

import androidx.lifecycle.ViewModelStore
import com.monsters.mobimon.core.domain.CompanionSettings
import com.monsters.mobimon.core.domain.PetAppearance
import com.monsters.mobimon.core.domain.PetProfile
import com.monsters.mobimon.core.domain.PetRepository
import com.monsters.mobimon.core.domain.SettingsRepository
import com.monsters.mobimon.core.domain.WriteResult
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PetViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val store = ViewModelStore()
    private val repository = TestPetRepository()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        store.clear()
        Dispatchers.resetMain()
    }

    private fun subject() = PetViewModel(repository, repository).also { store.put("pet", it) }

    @Test
    fun initializationFailureCanRetryWithoutReplacingSavedProgress() =
        runTest(dispatcher) {
            repository.failInitialization = true
            val vm = subject()
            runCurrent()
            assertTrue(vm.state.value.loadFailed)
            repository.failInitialization = false
            vm.retry()
            runCurrent()
            assertFalse(vm.state.value.loadFailed)
            assertEquals(
                80,
                vm.state.value.profile
                    ?.totalXp,
            )
        }

    @Test
    fun failedAppearanceSavePreservesCommittedSelectionAndExposesFailure() =
        runTest(dispatcher) {
            val vm = subject()
            runCurrent()
            vm.setAppearance(PetAppearance.CREAM)
            runCurrent()
            assertTrue(vm.state.value.saveFailed)
            assertFalse(vm.state.value.isSaving)
            assertEquals(
                PetAppearance.GOLDEN,
                vm.state.value.profile
                    ?.appearance,
            )
        }

    @Test
    fun preferenceChangesArePublishedFromTheSavedObservation() =
        runTest(dispatcher) {
            val vm = subject()
            runCurrent()
            vm.setReducedMotion(true)
            runCurrent()
            assertTrue(vm.state.value.settings.reducedMotion)
            assertFalse(vm.state.value.saveFailed)
        }

    @Test
    fun successfulPreferenceWriteDoesNotClearUnrelatedAppearanceFailure() =
        runTest(dispatcher) {
            val vm = subject()
            runCurrent()
            vm.setAppearance(PetAppearance.CREAM)
            runCurrent()
            assertTrue(vm.state.value.saveFailed)

            vm.setReducedMotion(true)
            runCurrent()

            assertTrue(vm.state.value.settings.reducedMotion)
            assertTrue(vm.state.value.saveFailed)
        }

    @Test
    fun onePendingPreferenceDoesNotDropAnotherPreferenceWrite() =
        runTest(dispatcher) {
            repository.visibilityGate = CompletableDeferred()
            val vm = subject()
            runCurrent()
            vm.setShowOnVehicleHome(false)
            runCurrent()
            assertTrue(vm.state.value.isSaving)

            vm.setReducedMotion(true)
            runCurrent()
            assertTrue(vm.state.value.settings.reducedMotion)
            repository.visibilityGate?.complete(Unit)
            runCurrent()
            assertFalse(vm.state.value.isSaving)
        }

    @Test
    fun settingsReadFailureDoesNotHideAnAvailableProfile() =
        runTest(dispatcher) {
            repository.failSettingsObservation = true
            val vm = subject()
            runCurrent()

            assertEquals(
                "profile",
                vm.state.value.profile
                    ?.id,
            )
            assertFalse(vm.state.value.loadFailed)
        }

    private class TestPetRepository :
        PetRepository,
        SettingsRepository {
        override val profile = MutableStateFlow(PetProfile("profile", totalXp = 80))
        private val savedSettings = MutableStateFlow(CompanionSettings())
        override val settings: Flow<CompanionSettings> =
            flow {
                if (failSettingsObservation) error("controlled preference read failure")
                emitAll(savedSettings)
            }
        var failInitialization = false
        var failSettingsObservation = false
        var visibilityGate: CompletableDeferred<Unit>? = null

        override suspend fun initialize() {
            check(!failInitialization) { "controlled read failure" }
        }

        override suspend fun setAppearance(appearance: PetAppearance) = WriteResult.Failure

        override suspend fun setShowOnVehicleHome(enabled: Boolean): WriteResult {
            visibilityGate?.await()
            savedSettings.value = savedSettings.value.copy(showOnVehicleHome = enabled)
            return WriteResult.Success
        }

        override suspend fun setReducedMotion(enabled: Boolean): WriteResult {
            savedSettings.value = savedSettings.value.copy(reducedMotion = enabled)
            return WriteResult.Success
        }

        override suspend fun setLauncherCharacterEnabled(enabled: Boolean): WriteResult {
            savedSettings.value = savedSettings.value.copy(launcherCharacterEnabled = enabled)
            return WriteResult.Success
        }
    }
}
