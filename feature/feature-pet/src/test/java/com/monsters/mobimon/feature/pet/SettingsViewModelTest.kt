package com.monsters.mobimon.feature.pet

import androidx.lifecycle.ViewModelStore
import com.monsters.mobimon.core.domain.CompanionSettings
import com.monsters.mobimon.core.domain.SettingsRepository
import com.monsters.mobimon.core.domain.WriteResult
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class SettingsViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val store = ViewModelStore()
    private val preferences = FakeSettings()

    @Before fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After fun tearDown() {
        store.clear()
        Dispatchers.resetMain()
    }

    private fun subject() = SettingsViewModel(preferences).also { store.put("settings", it) }

    @Test fun preferenceSaveDoesNotDependOnProfileInitialization() =
        runTest(dispatcher) {
            val model = subject()
            runCurrent()
            model.setReducedMotion(true)
            runCurrent()
            assertTrue(model.state.value.settings.reducedMotion)
            assertFalse(model.state.value.reducedMotionSaving)
        }

    @Test fun pendingPreferenceDoesNotBlockOtherKeysOrPublishUncommittedValues() =
        runTest(dispatcher) {
            val pending = CompletableDeferred<WriteResult>()
            preferences.motionResult = pending
            val model = subject()
            runCurrent()
            model.setReducedMotion(true)
            model.setReducedMotion(true)
            model.setDebugMode(true)
            runCurrent()
            assertEquals(1, preferences.motionWrites)
            assertTrue(model.state.value.reducedMotionSaving)
            assertFalse(model.state.value.settings.reducedMotion)
            assertTrue(model.state.value.settings.debugModeEnabled)
            pending.complete(WriteResult.Failure)
            runCurrent()
            assertTrue(model.state.value.reducedMotionSaveFailed)
            assertFalse(model.state.value.reducedMotionSaving)
        }

    @Test fun unrelatedSuccessfulWriteDoesNotClearFailure() =
        runTest(dispatcher) {
            preferences.motionResult = CompletableDeferred(WriteResult.Failure)
            val model = subject()
            runCurrent()
            model.setReducedMotion(true)
            runCurrent()
            model.setDebugMode(true)
            runCurrent()
            assertTrue(model.state.value.reducedMotionSaveFailed)
            assertFalse(model.state.value.debugModeSaveFailed)
        }

    @Test fun retryRecoversSettingsWithoutDuplicateHealthyCollectors() =
        runTest(dispatcher) {
            preferences.failReads = true
            val model = subject()
            runCurrent()
            assertTrue(model.state.value.loadFailed)
            model.setDebugMode(true)
            runCurrent()
            assertFalse(preferences.saved.value.debugModeEnabled)
            preferences.failReads = false
            model.retry()
            runCurrent()
            model.retry()
            runCurrent()
            assertEquals(2, preferences.subscriptions)
            assertTrue(model.state.value.loaded)
            assertFalse(model.state.value.loadFailed)
        }

    @Test fun cancellationEndsPendingSaveWithoutReportingStorageFailure() =
        runTest(dispatcher) {
            preferences.motionResult = CompletableDeferred()
            val model = subject()
            runCurrent()
            model.setReducedMotion(true)
            runCurrent()
            store.clear()
            runCurrent()
            assertFalse(model.state.value.reducedMotionSaving)
            assertFalse(model.state.value.reducedMotionSaveFailed)
        }

    @Test fun launcherCharacterPreferenceSavesSuccessfully() =
        runTest(dispatcher) {
            val model = subject()
            runCurrent()
            model.setLauncherCharacter(true)
            runCurrent()
            assertTrue(model.state.value.settings.launcherCharacterEnabled)
            assertFalse(model.state.value.launcherSaving)
        }

    private class FakeSettings : SettingsRepository {
        val saved = MutableStateFlow(CompanionSettings())
        var failReads = false
        var subscriptions = 0
        var motionWrites = 0
        var motionResult: CompletableDeferred<WriteResult>? = null
        override val settings =
            flow {
                subscriptions++
                check(!failReads)
                emitAll(saved)
            }

        override suspend fun setReducedMotion(enabled: Boolean): WriteResult {
            motionWrites++
            val result = motionResult?.await() ?: WriteResult.Success
            if (result == WriteResult.Success) saved.value = saved.value.copy(reducedMotion = enabled)
            return result
        }

        override suspend fun setDebugModeEnabled(enabled: Boolean): WriteResult {
            saved.value = saved.value.copy(debugModeEnabled = enabled)
            return WriteResult.Success
        }

        override suspend fun setLauncherCharacterEnabled(enabled: Boolean): WriteResult {
            saved.value = saved.value.copy(launcherCharacterEnabled = enabled)
            return WriteResult.Success
        }
    }
}
