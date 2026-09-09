package com.devemberx.rivo.feature.pet

import androidx.lifecycle.ViewModelStore
import com.devemberx.rivo.core.domain.CompanionSettings
import com.devemberx.rivo.core.domain.PetAppearance
import com.devemberx.rivo.core.domain.PetProfile
import com.devemberx.rivo.core.domain.PetRepository
import com.devemberx.rivo.core.domain.SettingsRepository
import com.devemberx.rivo.core.domain.WriteResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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

    private class TestPetRepository :
        PetRepository,
        SettingsRepository {
        override val profile = MutableStateFlow(PetProfile("profile", totalXp = 80))
        override val settings = MutableStateFlow(CompanionSettings())
        var failInitialization = false

        override suspend fun initialize() {
            check(!failInitialization) { "controlled read failure" }
        }

        override suspend fun setAppearance(appearance: PetAppearance) = WriteResult.Failure

        override suspend fun setShowOnVehicleHome(enabled: Boolean): WriteResult {
            settings.value = settings.value.copy(showOnVehicleHome = enabled)
            return WriteResult.Success
        }

        override suspend fun setReducedMotion(enabled: Boolean): WriteResult {
            settings.value = settings.value.copy(reducedMotion = enabled)
            return WriteResult.Success
        }
    }
}
