package com.monsters.mobimon.core.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DrivingQuestEvaluatorTest {
    private val evaluator = DrivingQuestEvaluator()

    @Test
    fun seatbeltQuestRequiresMinimumDistanceAndSeatbeltUsage() {
        val tooShort = DriveEvaluationData(distanceKm = 4.9f, safeBeltMinutes = 10)
        val shortResult = evaluator.evaluateSeatbelt(tooShort)
        assertFalse(shortResult.isSatisfied)
        assertEquals(0L, shortResult.earnedPoints)

        val noBelt = DriveEvaluationData(distanceKm = 5.0f, safeBeltMinutes = 0)
        val noBeltResult = evaluator.evaluateSeatbelt(noBelt)
        assertFalse(noBeltResult.isSatisfied)
        assertEquals(0L, noBeltResult.earnedPoints)

        val valid = DriveEvaluationData(distanceKm = 5.0f, safeBeltMinutes = 10)
        val validResult = evaluator.evaluateSeatbelt(valid)
        assertTrue(validResult.isSatisfied)
        assertEquals(5L, validResult.basePoints)
        assertEquals(5L, validResult.earnedPoints)
    }

    @Test
    fun safeDriveQuestRequiresMinimumDistanceAndThresholdScore() {
        val lowScore = DriveEvaluationData(distanceKm = 10.0f, safeDriveScore = 79)
        val lowScoreResult = evaluator.evaluateSafeDriveCompletion(lowScore)
        assertFalse(lowScoreResult.isSatisfied)

        val shortDrive = DriveEvaluationData(distanceKm = 4.9f, safeDriveScore = 90)
        val shortResult = evaluator.evaluateSafeDriveCompletion(shortDrive)
        assertFalse(shortResult.isSatisfied)

        val valid = DriveEvaluationData(distanceKm = 5.0f, safeDriveScore = 80)
        val validResult = evaluator.evaluateSafeDriveCompletion(valid)
        assertTrue(validResult.isSatisfied)
        assertEquals(20L, validResult.basePoints)
        assertEquals(20L, validResult.earnedPoints)
    }

    @Test
    fun total100KmDrivePassesWhenCumulativeDistanceReaches100Km() {
        val below = DriveEvaluationData(totalDistanceKm = 99.9f)
        assertFalse(evaluator.evaluate100KmDrive(below).isSatisfied)

        val exact = DriveEvaluationData(totalDistanceKm = 100.0f)
        val exactResult = evaluator.evaluate100KmDrive(exact)
        assertTrue(exactResult.isSatisfied)
        assertEquals(25L, exactResult.basePoints)
        assertEquals(25L, exactResult.earnedPoints)

        val over = DriveEvaluationData(totalDistanceKm = 150.0f)
        assertTrue(evaluator.evaluate100KmDrive(over).isSatisfied)
    }

    @Test
    fun cleanDriveBonusAwardsOnlyWhenNoInfractionsOccur() {
        val hardBrake = DriveEvaluationData(distanceKm = 10f, hardBrakeCount = 1)
        assertFalse(evaluator.evaluateCleanDriveBonus(hardBrake).isSatisfied)

        val hardAccel = DriveEvaluationData(distanceKm = 10f, hardAccelCount = 1)
        assertFalse(evaluator.evaluateCleanDriveBonus(hardAccel).isSatisfied)

        val overspeed = DriveEvaluationData(distanceKm = 10f, overspeedCount = 1)
        assertFalse(evaluator.evaluateCleanDriveBonus(overspeed).isSatisfied)

        val short = DriveEvaluationData(distanceKm = 4.0f)
        assertFalse(evaluator.evaluateCleanDriveBonus(short).isSatisfied)

        val clean = DriveEvaluationData(distanceKm = 5.0f, hardBrakeCount = 0, hardAccelCount = 0, overspeedCount = 0)
        val cleanResult = evaluator.evaluateCleanDriveBonus(clean)
        assertTrue(cleanResult.isSatisfied)
        assertEquals(15L, cleanResult.basePoints)
        assertEquals(15L, cleanResult.earnedPoints)
    }

    @Test
    fun firstDriveOfDayPassesWithAnyPositiveDistance() {
        val noDrive = DriveEvaluationData(distanceKm = 0f)
        assertFalse(evaluator.evaluateFirstDriveOfDay(noDrive).isSatisfied)

        val drove = DriveEvaluationData(distanceKm = 0.5f)
        val droveResult = evaluator.evaluateFirstDriveOfDay(drove)
        assertTrue(droveResult.isSatisfied)
        assertEquals(10L, droveResult.basePoints)
        assertEquals(10L, droveResult.earnedPoints)
    }

    @Test
    fun focusedDriveRequires30KmContinuousWithoutDistraction() {
        val distracted = DriveEvaluationData(continuousDistanceKm = 35f, isDistracted = true)
        assertFalse(evaluator.evaluateFocusedDrive(distracted).isSatisfied)

        val shortContinuous = DriveEvaluationData(continuousDistanceKm = 29.9f, isDistracted = false)
        assertFalse(evaluator.evaluateFocusedDrive(shortContinuous).isSatisfied)

        val focused = DriveEvaluationData(continuousDistanceKm = 30f, isDistracted = false)
        val focusedResult = evaluator.evaluateFocusedDrive(focused)
        assertTrue(focusedResult.isSatisfied)
        assertEquals(10L, focusedResult.basePoints)
        assertEquals(10L, focusedResult.earnedPoints)
    }

    @Test
    fun laneKeepingRequires30KmContinuousAndLessThan10Departures() {
        val tooManyDepartures = DriveEvaluationData(continuousDistanceKm = 30f, laneDepartureCount = 10)
        assertFalse(evaluator.evaluateLaneKeeping(tooManyDepartures).isSatisfied)

        val shortDistance = DriveEvaluationData(continuousDistanceKm = 25f, laneDepartureCount = 2)
        assertFalse(evaluator.evaluateLaneKeeping(shortDistance).isSatisfied)

        val keptLane = DriveEvaluationData(continuousDistanceKm = 30f, laneDepartureCount = 9)
        val keptLaneResult = evaluator.evaluateLaneKeeping(keptLane)
        assertTrue(keptLaneResult.isSatisfied)
        assertEquals(10L, keptLaneResult.basePoints)
        assertEquals(10L, keptLaneResult.earnedPoints)
    }

    @Test
    fun maintenanceVisitRequiresMaintenanceDestinationReached() {
        val otherDestination = DriveEvaluationData(isDestinationMaintenanceCenter = false, isDestinationReached = true)
        assertFalse(evaluator.evaluateMaintenanceVisit(otherDestination).isSatisfied)

        val notReached = DriveEvaluationData(isDestinationMaintenanceCenter = true, isDestinationReached = false)
        assertFalse(evaluator.evaluateMaintenanceVisit(notReached).isSatisfied)

        val reached = DriveEvaluationData(isDestinationMaintenanceCenter = true, isDestinationReached = true)
        val reachedResult = evaluator.evaluateMaintenanceVisit(reached)
        assertTrue(reachedResult.isSatisfied)
        assertEquals(10L, reachedResult.basePoints)
        assertEquals(10L, reachedResult.earnedPoints)
    }

    @Test
    fun turnSignalMannerGrantsOnePointPerSignalCappedAtTen() {
        val zero = DriveEvaluationData(turnSignalOnCount = 0)
        val zeroResult = evaluator.evaluateTurnSignalManner(zero)
        assertFalse(zeroResult.isSatisfied)
        assertEquals(0L, zeroResult.earnedPoints)

        val five = DriveEvaluationData(turnSignalOnCount = 5)
        val fiveResult = evaluator.evaluateTurnSignalManner(five)
        assertTrue(fiveResult.isSatisfied)
        assertEquals(5L, fiveResult.basePoints)
        assertEquals(5L, fiveResult.earnedPoints)

        val fifteen = DriveEvaluationData(turnSignalOnCount = 15)
        val fifteenResult = evaluator.evaluateTurnSignalManner(fifteen)
        assertTrue(fifteenResult.isSatisfied)
        assertEquals(10L, fifteenResult.basePoints)
        assertEquals(10L, fifteenResult.earnedPoints)
    }

    @Test
    fun fiveDaysSafeDriveRequiresFiveOrMoreConsecutiveDays() {
        val fourDays = DriveEvaluationData(safeDriveDaysCount = 4)
        assertFalse(evaluator.evaluate5DaysSafeDrive(fourDays).isSatisfied)

        val fiveDays = DriveEvaluationData(safeDriveDaysCount = 5)
        val fiveDaysResult = evaluator.evaluate5DaysSafeDrive(fiveDays)
        assertTrue(fiveDaysResult.isSatisfied)
        assertEquals(50L, fiveDaysResult.basePoints)
        assertEquals(50L, fiveDaysResult.earnedPoints)

        val sevenDays = DriveEvaluationData(safeDriveDaysCount = 7)
        assertTrue(evaluator.evaluate5DaysSafeDrive(sevenDays).isSatisfied)
    }

    @Test
    fun weatherMultipliersApplyCorrectly() {
        assertEquals(50L, evaluator.calculatePoints(50L, WeatherCondition.CLEAR))
        assertEquals(60L, evaluator.calculatePoints(50L, WeatherCondition.CLOUDY_OR_NIGHT))
        assertEquals(75L, evaluator.calculatePoints(50L, WeatherCondition.RAIN_OR_SNOW))

        assertEquals(20L, evaluator.calculatePoints(20L, WeatherCondition.CLEAR))
        assertEquals(24L, evaluator.calculatePoints(20L, WeatherCondition.CLOUDY_OR_NIGHT))
        assertEquals(30L, evaluator.calculatePoints(20L, WeatherCondition.RAIN_OR_SNOW))

        assertEquals(15L, evaluator.calculatePoints(15L, WeatherCondition.CLEAR))
        assertEquals(18L, evaluator.calculatePoints(15L, WeatherCondition.CLOUDY_OR_NIGHT))
        assertEquals(23L, evaluator.calculatePoints(15L, WeatherCondition.RAIN_OR_SNOW))

        assertEquals(10L, evaluator.calculatePoints(10L, WeatherCondition.CLEAR))
        assertEquals(12L, evaluator.calculatePoints(10L, WeatherCondition.CLOUDY_OR_NIGHT))
        assertEquals(15L, evaluator.calculatePoints(10L, WeatherCondition.RAIN_OR_SNOW))

        assertEquals(5L, evaluator.calculatePoints(5L, WeatherCondition.CLEAR))
        assertEquals(6L, evaluator.calculatePoints(5L, WeatherCondition.CLOUDY_OR_NIGHT))
        assertEquals(8L, evaluator.calculatePoints(5L, WeatherCondition.RAIN_OR_SNOW))

        val rainyDrive =
            DriveEvaluationData(
                distanceKm = 10f,
                safeDriveScore = 90,
                weather = WeatherCondition.RAIN_OR_SNOW,
            )
        val result = evaluator.evaluateSafeDriveCompletion(rainyDrive)
        assertTrue(result.isSatisfied)
        assertEquals(20L, result.basePoints)
        assertEquals(30L, result.earnedPoints)
        assertEquals(WeatherCondition.RAIN_OR_SNOW, result.weatherCondition)
    }

    @Test
    fun evaluateAllProducesAllTenQuestResults() {
        val perfectDrive =
            DriveEvaluationData(
                date = "2026-09-17",
                distanceKm = 35.0f,
                safeBeltMinutes = 40,
                hardBrakeCount = 0,
                hardAccelCount = 0,
                overspeedCount = 0,
                safeDriveScore = 95,
                turnSignalOnCount = 12,
                continuousDistanceKm = 35.0f,
                isDistracted = false,
                laneDepartureCount = 2,
                isDestinationMaintenanceCenter = true,
                isDestinationReached = true,
                totalDistanceKm = 120.0f,
                safeDriveDaysCount = 5,
                weather = WeatherCondition.CLEAR,
            )
        val results = evaluator.evaluateAll(perfectDrive)
        assertEquals(10, results.size)
        assertTrue(results.all { it.isSatisfied })
    }

    @Test
    fun defaultCatalogDefinesAllTenQuests() {
        val catalog = DefaultPointQuestCatalog("Asia/Seoul")
        val ids =
            listOf(
                DrivingQuestIds.SEATBELT,
                DrivingQuestIds.SAFE_DRIVE,
                DrivingQuestIds.DISTANCE_100KM,
                DrivingQuestIds.CLEAN_DRIVE,
                DrivingQuestIds.FIRST_DRIVE,
                DrivingQuestIds.FOCUS_DRIVE,
                DrivingQuestIds.LANE_KEEP,
                DrivingQuestIds.MAINTENANCE,
                DrivingQuestIds.TURN_SIGNAL,
                DrivingQuestIds.SAFE_5DAYS,
            )
        for (id in ids) {
            val def = catalog.find(id)
            assertNotNull("Quest $id should be defined in DefaultPointQuestCatalog", def)
            assertTrue("Quest $id reward points should be positive", def!!.rewardPoints > 0)
        }
    }
}
