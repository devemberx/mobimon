package com.monsters.mobimon.ui

import androidx.lifecycle.ViewModelStore
import com.monsters.mobimon.core.domain.CompanionSettings
import com.monsters.mobimon.core.domain.SettingsRepository
import com.monsters.mobimon.core.domain.WriteResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
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
class MotionPreferencesViewModelTest {
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

    @Test fun decorationWaitsForCommittedPreferenceAndStopsAfterChanges() =
        runTest(dispatcher) {
            val model = MotionPreferencesViewModel(preferences).also { store.put("motion", it) }
            assertTrue(model.reducedMotion.value)
            backgroundScope.launch { model.reducedMotion.collect {} }
            runCurrent()
            assertFalse(model.reducedMotion.value)
            preferences.saved.value = CompanionSettings(reducedMotion = true)
            runCurrent()
            assertTrue(model.reducedMotion.value)
        }

    @Test fun failedObservationPausesMotionAndResubscribesAfterForegroundReturn() =
        runTest(dispatcher) {
            preferences.failReads = true
            val model = MotionPreferencesViewModel(preferences).also { store.put("motion", it) }
            val collector = backgroundScope.launch { model.reducedMotion.collect {} }
            runCurrent()
            assertTrue(model.reducedMotion.value)
            collector.cancel()
            advanceTimeBy(5_001)
            runCurrent()
            preferences.failReads = false
            backgroundScope.launch { model.reducedMotion.collect {} }
            runCurrent()
            assertFalse(model.reducedMotion.value)
            assertEquals(2, preferences.subscriptions)
        }

    @Test fun transientReadFailureRecoversWithoutLeavingTheActivity() =
        runTest(dispatcher) {
            preferences.failReads = true
            val model = MotionPreferencesViewModel(preferences).also { store.put("motion", it) }
            backgroundScope.launch { model.reducedMotion.collect {} }
            runCurrent()
            assertTrue(model.reducedMotion.value)
            preferences.failReads = false
            advanceTimeBy(5_001)
            runCurrent()
            assertFalse(model.reducedMotion.value)
            assertEquals(2, preferences.subscriptions)
        }

    private class FakeSettings : SettingsRepository {
        val saved = MutableStateFlow(CompanionSettings())
        var failReads = false
        var subscriptions = 0
        override val settings =
            flow {
                subscriptions++
                check(!failReads)
                emitAll(saved)
            }

        override suspend fun setReducedMotion(enabled: Boolean) = WriteResult.Failure

        override suspend fun setDebugModeEnabled(enabled: Boolean) = WriteResult.Failure

        override suspend fun setLauncherCharacterEnabled(enabled: Boolean) = WriteResult.Failure
    }
}
