package com.monsters.mobimon.debug

import com.monsters.mobimon.core.domain.DrivingQuestEvaluator
import com.monsters.mobimon.core.domain.DrivingQuestIds
import com.monsters.mobimon.core.domain.WeatherCondition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DebugQuestEvidenceTest {
    private val evaluator = DrivingQuestEvaluator()

    private fun state(raw: DebugRawVssState) = DebugVssState(raw = raw)

    @Test
    fun seatbeltSignalWithDistanceSatisfiesSeatbeltQuestButUnrelatedQuestsStayUnaffected() {
        val evidence =
            state(
                DebugRawVssState(
                    driverSeatBelted = true,
                    tripDurationSeconds = 600f,
                    traveledDistanceSinceStartKm = 6f,
                ),
            ).toDriveEvaluationData(WeatherCondition.CLEAR)

        assertTrue(evaluator.evaluateById(DrivingQuestIds.SEATBELT, evidence)!!.isSatisfied)
        // 6km is below the 30km continuous requirement, so the focus quest is unaffected.
        assertFalse(evaluator.evaluateById(DrivingQuestIds.FOCUS_DRIVE, evidence)!!.isSatisfied)
    }

    @Test
    fun clearingTheSeatbeltSignalLeavesTheSeatbeltQuestUnsatisfied() {
        val evidence =
            state(DebugRawVssState(driverSeatBelted = false, traveledDistanceSinceStartKm = 6f))
                .toDriveEvaluationData(WeatherCondition.CLEAR)

        assertEquals(0, evidence.safeBeltMinutes)
        assertFalse(evaluator.evaluateById(DrivingQuestIds.SEATBELT, evidence)!!.isSatisfied)
    }

    @Test
    fun turnSignalAndTireAndBatterySignalsDriveTheirQuests() {
        val evidence =
            state(
                DebugRawVssState(
                    leftIndicatorSignaling = true,
                    rightIndicatorSignaling = true,
                    tractionBatteryChargingIsCharging = true,
                    tractionBatterySocDisplayed = 90f,
                ),
            ).toDriveEvaluationData(WeatherCondition.CLEAR)

        assertEquals(2, evidence.turnSignalOnCount)
        assertTrue(evaluator.evaluateById(DrivingQuestIds.TURN_SIGNAL, evidence)!!.isSatisfied)
        assertTrue(evidence.isTirePressureNormalWeekly)
        assertTrue(evaluator.evaluateById(DrivingQuestIds.TIRE_CHECK, evidence)!!.isSatisfied)
        assertTrue(evaluator.evaluateById(DrivingQuestIds.BATTERY_CARE, evidence)!!.isSatisfied)
    }

    @Test
    fun tirePressureWarningFailsTheTireQuest() {
        val evidence =
            state(DebugRawVssState(row1LeftTirePressureLow = true))
                .toDriveEvaluationData(WeatherCondition.CLEAR)

        assertFalse(evidence.isTirePressureNormalWeekly)
        assertFalse(evaluator.evaluateById(DrivingQuestIds.TIRE_CHECK, evidence)!!.isSatisfied)
    }

    @Test
    fun safeDriveScoreIsSynthesizedFromSafetySignals() {
        val clean =
            state(DebugRawVssState(traveledDistanceSinceStartKm = 6f))
                .toDriveEvaluationData(WeatherCondition.CLEAR)
        assertEquals(100, clean.safeDriveScore)
        assertTrue(evaluator.evaluateById(DrivingQuestIds.SAFE_DRIVE, clean)!!.isSatisfied)

        val risky =
            state(
                DebugRawVssState(
                    traveledDistanceSinceStartKm = 6f,
                    driverDistractionLevel = 80f,
                    driverEmergencyBrakingDetected = true,
                ),
            ).toDriveEvaluationData(WeatherCondition.CLEAR)
        assertEquals(45, risky.safeDriveScore)
        assertFalse(evaluator.evaluateById(DrivingQuestIds.SAFE_DRIVE, risky)!!.isSatisfied)
    }

    @Test
    fun laneDepartureWarningFailsLaneKeepingOverAContinuousDrive() {
        val evidence =
            state(
                DebugRawVssState(
                    traveledDistanceSinceStartKm = 35f,
                    laneDepartureWarning = true,
                ),
            ).toDriveEvaluationData(WeatherCondition.CLEAR)

        assertEquals(DrivingQuestEvaluator.MAX_LANE_DEPARTURES, evidence.laneDepartureCount)
        assertFalse(evaluator.evaluateById(DrivingQuestIds.LANE_KEEP, evidence)!!.isSatisfied)
    }

    @Test
    fun odometerSignalDrivesTheCumulativeDistanceQuest() {
        val below =
            state(DebugRawVssState(traveledDistanceKm = 99f)).toDriveEvaluationData(WeatherCondition.CLEAR)
        assertFalse(evaluator.evaluateById(DrivingQuestIds.DISTANCE_100KM, below)!!.isSatisfied)

        val reached =
            state(DebugRawVssState(traveledDistanceKm = 120f)).toDriveEvaluationData(WeatherCondition.CLEAR)
        assertEquals(120f, reached.totalDistanceKm, 0.001f)
        assertTrue(evaluator.evaluateById(DrivingQuestIds.DISTANCE_100KM, reached)!!.isSatisfied)
    }

    @Test
    fun accumulatedSafeDriveCountDrivesTheFiveSafeDriveQuest() {
        val four =
            state(DebugRawVssState()).toDriveEvaluationData(WeatherCondition.CLEAR, safeDriveCount = 4)
        assertFalse(evaluator.evaluateById(DrivingQuestIds.SAFE_5DAYS, four)!!.isSatisfied)

        val five =
            state(DebugRawVssState()).toDriveEvaluationData(WeatherCondition.CLEAR, safeDriveCount = 5)
        assertEquals(5, five.safeDriveCount)
        assertTrue(evaluator.evaluateById(DrivingQuestIds.SAFE_5DAYS, five)!!.isSatisfied)
    }

    @Test
    fun longTripWithRestMarginDrivesTheLongTripRestQuest() {
        // 120km at a nominal 80km/h is ~5400s of driving; 7200s elapsed leaves a >15min rest margin.
        val rested =
            state(
                DebugRawVssState(traveledDistanceSinceStartKm = 120f, tripDurationSeconds = 7200f),
            ).toDriveEvaluationData(WeatherCondition.CLEAR)
        assertTrue(rested.hasRestedDuringLongDrive)
        assertTrue(evaluator.evaluateById(DrivingQuestIds.LONG_TRIP_REST, rested)!!.isSatisfied)

        // Same distance but no extra time over pure driving → no rest inferred.
        val nonStop =
            state(
                DebugRawVssState(traveledDistanceSinceStartKm = 120f, tripDurationSeconds = 5400f),
            ).toDriveEvaluationData(WeatherCondition.CLEAR)
        assertFalse(nonStop.hasRestedDuringLongDrive)
        assertFalse(evaluator.evaluateById(DrivingQuestIds.LONG_TRIP_REST, nonStop)!!.isSatisfied)

        // A short trip with a long pause is not a long trip, so the quest stays unaffected.
        val shortTrip =
            state(
                DebugRawVssState(traveledDistanceSinceStartKm = 10f, tripDurationSeconds = 6000f),
            ).toDriveEvaluationData(WeatherCondition.CLEAR)
        assertFalse(shortTrip.hasRestedDuringLongDrive)
    }
}
