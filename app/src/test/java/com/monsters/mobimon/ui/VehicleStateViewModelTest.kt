package com.monsters.mobimon.ui

import androidx.lifecycle.ViewModelStore
import com.monsters.mobimon.core.domain.Clock
import com.monsters.mobimon.core.domain.DrivingState
import com.monsters.mobimon.core.domain.ProgressionIdentity
import com.monsters.mobimon.core.domain.SignalQuality
import com.monsters.mobimon.core.domain.SignalSource
import com.monsters.mobimon.core.domain.VehicleFreshnessPolicy
import com.monsters.mobimon.core.domain.VehicleRepository
import com.monsters.mobimon.core.domain.VehicleSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VehicleStateViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val store = ViewModelStore()
    private val vehicle =
        object : VehicleRepository {
            override val snapshots =
                MutableStateFlow(
                    VehicleSnapshot(
                        id = "moving-card",
                        epoch = "epoch",
                        sequence = 1,
                        receivedAtMillis = 2_000,
                        source = SignalSource.REAL,
                        drivingState = DrivingState.MOVING,
                        quality = SignalQuality.VALID,
                        batteryPercent = 72,
                    ),
                )

            override fun start() = Unit

            override fun stop() = Unit
        }
    private var now = 2_000L

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        store.clear()
        Dispatchers.resetMain()
    }

    @Test
    fun movingReadingExpiresIndependentlyOfQuestObservation() =
        runTest(dispatcher) {
            try {
                val model =
                    VehicleStateViewModel(
                        vehicle,
                        ProgressionIdentity("profile", SignalSource.REAL),
                        Clock { now },
                        VehicleFreshnessPolicy(15_000),
                    ).also { store.put("vehicle", it) }
                runCurrent()
                assertEquals(SignalQuality.VALID, model.state.value.quality)
                now = 20_000
                advanceTimeBy(1_000)
                runCurrent()
                assertEquals(SignalQuality.STALE, model.state.value.quality)
                assertEquals(72, model.state.value.batteryPercent)
            } finally {
                store.clear()
                runCurrent()
            }
        }
}
