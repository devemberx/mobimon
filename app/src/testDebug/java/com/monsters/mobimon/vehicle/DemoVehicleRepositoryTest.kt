package com.monsters.mobimon.vehicle

import com.monsters.mobimon.core.domain.Clock
import com.monsters.mobimon.core.domain.DrivingState
import com.monsters.mobimon.core.domain.IdGenerator
import com.monsters.mobimon.core.domain.SignalQuality
import com.monsters.mobimon.core.domain.SignalSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

@OptIn(ExperimentalCoroutinesApi::class)
class DemoVehicleRepositoryTest {
    @Test
    fun simulationHasOneObservationJobAndANewEpochAfterEveryGap() =
        runTest {
            var id = 0
            val repository =
                DemoVehicleRepository(
                    Clock { testScheduler.currentTime },
                    IdGenerator { "id-${++id}" },
                    backgroundScope,
                )
            repository.start()
            runCurrent()
            val first = repository.snapshots.value
            assertEquals(SignalSource.SIMULATED, first.source)
            assertEquals(DrivingState.PARKED, first.drivingState)
            assertEquals(SignalQuality.VALID, first.quality)
            repository.start()
            assertEquals(first.epoch, repository.snapshots.value.epoch)
            advanceTimeBy(2_000)
            runCurrent()
            assertTrue(repository.snapshots.value.sequence > first.sequence)
            repository.stop()
            val stopped = repository.snapshots.value
            assertEquals(SignalQuality.UNAVAILABLE, stopped.quality)
            advanceTimeBy(4_000)
            runCurrent()
            assertEquals(stopped, repository.snapshots.value)
            repository.start()
            runCurrent()
            assertNotEquals(first.epoch, repository.snapshots.value.epoch)
            repository.stop()
        }

    @Test
    fun stopWinsWhenAPeriodicPublishIsAlreadyInFlight() {
        val scheduler = TestCoroutineScheduler()
        val publishEntered = CountDownLatch(1)
        val releasePublish = CountDownLatch(1)
        val clockCalls = AtomicInteger()
        val parent = SupervisorJob()
        val scope = CoroutineScope(parent + StandardTestDispatcher(scheduler))
        val worker = Executors.newSingleThreadExecutor()
        val repository =
            DemoVehicleRepository(
                clock =
                    Clock {
                        if (clockCalls.incrementAndGet() == 2) {
                            publishEntered.countDown()
                            check(releasePublish.await(5, TimeUnit.SECONDS))
                        }
                        scheduler.currentTime
                    },
                ids = IdGenerator { "epoch" },
                scope = scope,
            )

        try {
            repository.start()
            scheduler.advanceTimeBy(2_000)
            val inFlightPublish = worker.submit { scheduler.runCurrent() }
            assertTrue(publishEntered.await(5, TimeUnit.SECONDS))

            repository.stop()
            releasePublish.countDown()
            inFlightPublish.get(5, TimeUnit.SECONDS)

            assertEquals(SignalQuality.UNAVAILABLE, repository.snapshots.value.quality)
            assertEquals(DrivingState.UNKNOWN, repository.snapshots.value.drivingState)
        } finally {
            releasePublish.countDown()
            parent.cancel()
            worker.shutdownNow()
        }
    }
}
