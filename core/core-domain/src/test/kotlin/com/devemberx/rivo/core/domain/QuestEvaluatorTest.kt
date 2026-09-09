package com.devemberx.rivo.core.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class QuestEvaluatorTest {
    private val now = 20_000L
    private val evaluator = QuestEvaluator(maxAgeMillis = 15_000L)
    private val run =
        QuestRun(
            id = "run-1",
            profileId = "profile-1",
            type = QuestType.Q01,
            status = QuestStatus.ACTIVE,
            revision = 4,
            ruleVersion = 1,
            rewardXp = 80,
            startEpoch = "epoch-1",
            startSequence = 10,
            startedAtMillis = 10_000L,
            source = SignalSource.REAL,
        )
    private val snapshot =
        VehicleSnapshot(
            id = "snapshot-11",
            epoch = "epoch-1",
            sequence = 11,
            receivedAtMillis = 19_000L,
            source = SignalSource.REAL,
            drivingState = DrivingState.PARKED,
            quality = SignalQuality.VALID,
            batteryPercent = 65,
        )

    @Test
    fun `fresh parked post-start evidence completes fixed Q01 rule`() {
        assertNull(evaluator.evaluate(run, run.revision, snapshot, SignalSource.REAL, now))
    }

    @Test
    fun `snapshot validation rejects unavailable stale and non-parked evidence`() {
        assertEquals(
            QuestRejection.UNAVAILABLE,
            evaluator.validateSnapshot(snapshot.copy(quality = SignalQuality.UNAVAILABLE), SignalSource.REAL, now),
        )
        assertEquals(
            QuestRejection.STALE,
            evaluator.validateSnapshot(snapshot.copy(quality = SignalQuality.STALE), SignalSource.REAL, now),
        )
        assertEquals(
            QuestRejection.NOT_PARKED,
            evaluator.validateSnapshot(snapshot.copy(drivingState = DrivingState.MOVING), SignalSource.REAL, now),
        )
        assertEquals(
            QuestRejection.NOT_PARKED,
            evaluator.validateSnapshot(snapshot.copy(drivingState = DrivingState.UNKNOWN), SignalSource.REAL, now),
        )
    }

    @Test
    fun `snapshot validation rejects source mismatch and expired evidence`() {
        assertEquals(
            QuestRejection.WRONG_SOURCE,
            evaluator.validateSnapshot(snapshot.copy(source = SignalSource.SIMULATED), SignalSource.REAL, now),
        )
        assertEquals(
            QuestRejection.STALE,
            evaluator.validateSnapshot(snapshot.copy(receivedAtMillis = 4_999L), SignalSource.REAL, now),
        )
        assertNull(
            evaluator.validateSnapshot(snapshot.copy(receivedAtMillis = 5_000L), SignalSource.REAL, now),
        )
    }

    @Test
    fun `snapshot validation rejects malformed identity time order and battery`() {
        val malformed =
            listOf(
                snapshot.copy(id = ""),
                snapshot.copy(epoch = ""),
                snapshot.copy(sequence = -1),
                snapshot.copy(receivedAtMillis = -1),
                snapshot.copy(receivedAtMillis = now + 1),
                snapshot.copy(batteryPercent = -1),
                snapshot.copy(batteryPercent = 101),
            )

        malformed.forEach {
            assertEquals(QuestRejection.INVALID_SIGNAL, evaluator.validateSnapshot(it, SignalSource.REAL, now))
        }
    }

    @Test
    fun `evaluation rejects unsupported quest rule version and reward`() {
        val unsupported =
            listOf(
                run.copy(type = QuestType.Q02),
                run.copy(type = QuestType.Q03),
                run.copy(ruleVersion = 2),
                run.copy(rewardXp = 79),
                run.copy(rewardXp = 81),
            )

        unsupported.forEach {
            assertEquals(
                QuestRejection.UNSUPPORTED_QUEST,
                evaluator.evaluate(it, it.revision, snapshot, SignalSource.REAL, now),
            )
        }
    }

    @Test
    fun `evaluation rejects inactive or revised run`() {
        assertEquals(
            QuestRejection.RUN_CHANGED,
            evaluator.evaluate(
                run.copy(status = QuestStatus.CANCELLED),
                run.revision,
                snapshot,
                SignalSource.REAL,
                now,
            ),
        )
        assertEquals(
            QuestRejection.RUN_CHANGED,
            evaluator.evaluate(
                run.copy(status = QuestStatus.COMPLETED),
                run.revision,
                snapshot,
                SignalSource.REAL,
                now,
            ),
        )
        assertEquals(
            QuestRejection.RUN_CHANGED,
            evaluator.evaluate(run, run.revision - 1, snapshot, SignalSource.REAL, now),
        )
    }

    @Test
    fun `evaluation rejects source and epoch mismatches`() {
        assertEquals(
            QuestRejection.WRONG_SOURCE,
            evaluator.evaluate(
                run.copy(source = SignalSource.SIMULATED),
                run.revision,
                snapshot,
                SignalSource.REAL,
                now,
            ),
        )
        assertEquals(
            QuestRejection.WRONG_EPOCH,
            evaluator.evaluate(run, run.revision, snapshot.copy(epoch = "epoch-2"), SignalSource.REAL, now),
        )
    }

    @Test
    fun `evaluation rejects evidence at or before the start boundary`() {
        assertEquals(
            QuestRejection.BEFORE_START,
            evaluator.evaluate(run, run.revision, snapshot.copy(sequence = run.startSequence), SignalSource.REAL, now),
        )
        assertEquals(
            QuestRejection.BEFORE_START,
            evaluator.evaluate(
                run,
                run.revision,
                snapshot.copy(receivedAtMillis = run.startedAtMillis - 1),
                SignalSource.REAL,
                now,
            ),
        )
        assertNull(
            evaluator.evaluate(
                run,
                run.revision,
                snapshot.copy(receivedAtMillis = run.startedAtMillis),
                SignalSource.REAL,
                now,
            ),
        )
    }

    @Test
    fun `evaluation rejects malformed run boundary`() {
        val malformed =
            listOf(
                run.copy(startEpoch = ""),
                run.copy(startSequence = -1),
                run.copy(startedAtMillis = -1),
            )

        malformed.forEach {
            assertEquals(
                QuestRejection.INVALID_SIGNAL,
                evaluator.evaluate(it, it.revision, snapshot, SignalSource.REAL, now),
            )
        }
    }

    @Test
    fun `negative freshness policy is rejected`() {
        assertThrows(IllegalArgumentException::class.java) { QuestEvaluator(maxAgeMillis = -1) }
    }
}
