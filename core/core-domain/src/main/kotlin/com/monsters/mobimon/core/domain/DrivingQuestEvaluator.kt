package com.monsters.mobimon.core.domain

import kotlin.math.roundToLong

class DrivingQuestEvaluator(
    private val minSafeDriveScore: Int = SAFE_DRIVE_DEFAULT_PASS_SCORE,
) {
    fun calculatePoints(
        basePoints: Long,
        weather: WeatherCondition,
    ): Long {
        require(basePoints >= 0) { "Base points cannot be negative" }
        return (basePoints * weather.multiplier).roundToLong()
    }

    fun evaluateSeatbelt(data: DriveEvaluationData): DrivingQuestResult {
        val basePoints = 5L
        val satisfied = data.distanceKm >= MIN_DRIVE_DISTANCE_KM && data.safeBeltMinutes > 0
        val earned = if (satisfied) calculatePoints(basePoints, data.weather) else 0L
        val reason =
            when {
                data.distanceKm < MIN_DRIVE_DISTANCE_KM -> "최소 주행 거리(${MIN_DRIVE_DISTANCE_KM}km) 미달"
                data.safeBeltMinutes <= 0 -> "안전벨트 착용 기록 없음"
                else -> "안전벨트 착용 주행 완료"
            }
        return DrivingQuestResult(
            questId = DrivingQuestIds.SEATBELT,
            isSatisfied = satisfied,
            basePoints = basePoints,
            earnedPoints = earned,
            weatherCondition = data.weather,
            reason = reason,
        )
    }

    fun evaluateSafeDriveCompletion(data: DriveEvaluationData): DrivingQuestResult {
        val basePoints = 20L
        val satisfied = data.distanceKm >= MIN_DRIVE_DISTANCE_KM && data.safeDriveScore >= minSafeDriveScore
        val earned = if (satisfied) calculatePoints(basePoints, data.weather) else 0L
        val reason =
            when {
                data.distanceKm < MIN_DRIVE_DISTANCE_KM -> "최소 주행 거리(${MIN_DRIVE_DISTANCE_KM}km) 미달"
                data.safeDriveScore < minSafeDriveScore -> "안전점수($minSafeDriveScore 점 이상) 미달"
                else -> "안전 주행 종료 기준 충족"
            }
        return DrivingQuestResult(
            questId = DrivingQuestIds.SAFE_DRIVE,
            isSatisfied = satisfied,
            basePoints = basePoints,
            earnedPoints = earned,
            weatherCondition = data.weather,
            reason = reason,
        )
    }

    fun evaluate100KmDrive(data: DriveEvaluationData): DrivingQuestResult {
        val basePoints = 25L
        val satisfied = data.totalDistanceKm >= TARGET_TOTAL_DISTANCE_KM
        val earned = if (satisfied) calculatePoints(basePoints, data.weather) else 0L
        val reason = if (satisfied) "누적 ${TARGET_TOTAL_DISTANCE_KM.toInt()}km 주행 완료" else "누적 주행 거리 부족"
        return DrivingQuestResult(
            questId = DrivingQuestIds.DISTANCE_100KM,
            isSatisfied = satisfied,
            basePoints = basePoints,
            earnedPoints = earned,
            weatherCondition = data.weather,
            reason = reason,
        )
    }

    fun evaluateCleanDriveBonus(data: DriveEvaluationData): DrivingQuestResult {
        val basePoints = 15L
        val hasViolations = data.hardBrakeCount > 0 || data.hardAccelCount > 0 || data.overspeedCount > 0
        val satisfied = data.distanceKm >= MIN_DRIVE_DISTANCE_KM && !hasViolations
        val earned = if (satisfied) calculatePoints(basePoints, data.weather) else 0L
        val reason =
            when {
                data.distanceKm < MIN_DRIVE_DISTANCE_KM -> "최소 주행 거리(${MIN_DRIVE_DISTANCE_KM}km) 미달"
                hasViolations -> "급가속/급제동/과속 이력 있음 (보너스 미지급)"
                else -> "클린 드라이브 보너스 조건 충족"
            }
        return DrivingQuestResult(
            questId = DrivingQuestIds.CLEAN_DRIVE,
            isSatisfied = satisfied,
            basePoints = basePoints,
            earnedPoints = earned,
            weatherCondition = data.weather,
            reason = reason,
        )
    }

    fun evaluateFirstDriveOfDay(data: DriveEvaluationData): DrivingQuestResult {
        val basePoints = 10L
        val satisfied = data.distanceKm > 0f
        val earned = if (satisfied) calculatePoints(basePoints, data.weather) else 0L
        val reason = if (satisfied) "당일 첫 주행 완료" else "주행 거리 없음"
        return DrivingQuestResult(
            questId = DrivingQuestIds.FIRST_DRIVE,
            isSatisfied = satisfied,
            basePoints = basePoints,
            earnedPoints = earned,
            weatherCondition = data.weather,
            reason = reason,
        )
    }

    fun evaluateFocusedDrive(data: DriveEvaluationData): DrivingQuestResult {
        val basePoints = 10L
        val satisfied = data.continuousDistanceKm >= CONTINUOUS_DISTANCE_KM && !data.isDistracted
        val earned = if (satisfied) calculatePoints(basePoints, data.weather) else 0L
        val reason =
            when {
                data.continuousDistanceKm < CONTINUOUS_DISTANCE_KM -> "연속 주행 거리(${CONTINUOUS_DISTANCE_KM.toInt()}km) 미달"
                data.isDistracted -> "주행 중 부주의 감지됨"
                else -> "30km 연속 집중 운전 완료"
            }
        return DrivingQuestResult(
            questId = DrivingQuestIds.FOCUS_DRIVE,
            isSatisfied = satisfied,
            basePoints = basePoints,
            earnedPoints = earned,
            weatherCondition = data.weather,
            reason = reason,
        )
    }

    fun evaluateLaneKeeping(data: DriveEvaluationData): DrivingQuestResult {
        val basePoints = 10L
        val satisfied =
            data.continuousDistanceKm >= CONTINUOUS_DISTANCE_KM && data.laneDepartureCount < MAX_LANE_DEPARTURES
        val earned = if (satisfied) calculatePoints(basePoints, data.weather) else 0L
        val reason =
            when {
                data.continuousDistanceKm < CONTINUOUS_DISTANCE_KM -> "연속 주행 거리(${CONTINUOUS_DISTANCE_KM.toInt()}km) 미달"
                data.laneDepartureCount >= MAX_LANE_DEPARTURES -> "차선 이탈 횟수($MAX_LANE_DEPARTURES 회 이상) 초과"
                else -> "30km 연속 차선 지키기 완료"
            }
        return DrivingQuestResult(
            questId = DrivingQuestIds.LANE_KEEP,
            isSatisfied = satisfied,
            basePoints = basePoints,
            earnedPoints = earned,
            weatherCondition = data.weather,
            reason = reason,
        )
    }

    fun evaluateMaintenanceVisit(data: DriveEvaluationData): DrivingQuestResult {
        val basePoints = 10L
        val satisfied = data.isDestinationMaintenanceCenter && data.isDestinationReached
        val earned = if (satisfied) calculatePoints(basePoints, data.weather) else 0L
        val reason = if (satisfied) "정비소 목적지 도착 확인" else "정비소 도착 미확인"
        return DrivingQuestResult(
            questId = DrivingQuestIds.MAINTENANCE,
            isSatisfied = satisfied,
            basePoints = basePoints,
            earnedPoints = earned,
            weatherCondition = data.weather,
            reason = reason,
        )
    }

    fun evaluateTurnSignalManner(data: DriveEvaluationData): DrivingQuestResult {
        val effectiveCount = data.turnSignalOnCount.coerceIn(0, MAX_DAILY_TURN_SIGNAL_POINTS)
        val satisfied = effectiveCount > 0
        val earned =
            if (satisfied) {
                calculatePoints(effectiveCount.toLong(), data.weather)
            } else {
                0L
            }
        val reason =
            if (satisfied) {
                "방향지시등 ${effectiveCount}회 점등 점수 적립"
            } else {
                "방향지시등 점등 기록 없음"
            }
        return DrivingQuestResult(
            questId = DrivingQuestIds.TURN_SIGNAL,
            isSatisfied = satisfied,
            basePoints = effectiveCount.toLong(),
            earnedPoints = earned,
            weatherCondition = data.weather,
            reason = reason,
        )
    }

    fun evaluate5DaysSafeDrive(data: DriveEvaluationData): DrivingQuestResult {
        val basePoints = 50L
        val satisfied = data.safeDriveDaysCount >= REQUIRED_CONSECUTIVE_SAFE_DAYS
        val earned = if (satisfied) calculatePoints(basePoints, data.weather) else 0L
        val reason =
            if (satisfied) {
                "${REQUIRED_CONSECUTIVE_SAFE_DAYS}일 연속 안전 주행 달성"
            } else {
                "연속 안전 주행 일수(${data.safeDriveDaysCount}/$REQUIRED_CONSECUTIVE_SAFE_DAYS) 부족"
            }
        return DrivingQuestResult(
            questId = DrivingQuestIds.SAFE_5DAYS,
            isSatisfied = satisfied,
            basePoints = basePoints,
            earnedPoints = earned,
            weatherCondition = data.weather,
            reason = reason,
        )
    }

    fun evaluateBatteryCare(data: DriveEvaluationData): DrivingQuestResult {
        val basePoints = 20L
        val satisfied = data.isBatteryChargedProperly
        val earned = if (satisfied) calculatePoints(basePoints, data.weather) else 0L
        val reason = if (satisfied) "배터리 헬스케어 충전 완료" else "배터리 적정 충전 조건 미충족"
        return DrivingQuestResult(
            questId = DrivingQuestIds.BATTERY_CARE,
            isSatisfied = satisfied,
            basePoints = basePoints,
            earnedPoints = earned,
            weatherCondition = data.weather,
            reason = reason,
        )
    }

    fun evaluateLongTripRest(data: DriveEvaluationData): DrivingQuestResult {
        val basePoints = 25L
        val satisfied = data.hasRestedDuringLongDrive
        val earned = if (satisfied) calculatePoints(basePoints, data.weather) else 0L
        val reason = if (satisfied) "장거리 주행 중 권장 휴식 완료" else "장거리 주행 중 휴식 조건 미충족"
        return DrivingQuestResult(
            questId = DrivingQuestIds.LONG_TRIP_REST,
            isSatisfied = satisfied,
            basePoints = basePoints,
            earnedPoints = earned,
            weatherCondition = data.weather,
            reason = reason,
        )
    }

    fun evaluateWasherFluid(data: DriveEvaluationData): DrivingQuestResult {
        val basePoints = 15L
        val satisfied = data.isWasherFluidRefilled
        val earned = if (satisfied) calculatePoints(basePoints, data.weather) else 0L
        val reason = if (satisfied) "워셔액 보충 완료" else "워셔액 보충 미확인"
        return DrivingQuestResult(
            questId = DrivingQuestIds.WASHER_FLUID,
            isSatisfied = satisfied,
            basePoints = basePoints,
            earnedPoints = earned,
            weatherCondition = data.weather,
            reason = reason,
        )
    }

    fun evaluateTireCheck(data: DriveEvaluationData): DrivingQuestResult {
        val basePoints = 15L
        val satisfied = data.isTirePressureNormalWeekly
        val earned = if (satisfied) calculatePoints(basePoints, data.weather) else 0L
        val reason = if (satisfied) "1주일 타이어 공기압 정상 유지 달성" else "타이어 공기압 경고 발생 또는 미달성"
        return DrivingQuestResult(
            questId = DrivingQuestIds.TIRE_CHECK,
            isSatisfied = satisfied,
            basePoints = basePoints,
            earnedPoints = earned,
            weatherCondition = data.weather,
            reason = reason,
        )
    }

    fun evaluateAll(data: DriveEvaluationData): List<DrivingQuestResult> =
        listOf(
            evaluateSeatbelt(data),
            evaluateSafeDriveCompletion(data),
            evaluate100KmDrive(data),
            evaluateCleanDriveBonus(data),
            evaluateFirstDriveOfDay(data),
            evaluateFocusedDrive(data),
            evaluateLaneKeeping(data),
            evaluateMaintenanceVisit(data),
            evaluateTurnSignalManner(data),
            evaluate5DaysSafeDrive(data),
            evaluateBatteryCare(data),
            evaluateLongTripRest(data),
            evaluateWasherFluid(data),
            evaluateTireCheck(data),
        )

    companion object {
        const val MIN_DRIVE_DISTANCE_KM = 5.0f
        const val CONTINUOUS_DISTANCE_KM = 30.0f
        const val TARGET_TOTAL_DISTANCE_KM = 100.0f
        const val MAX_LANE_DEPARTURES = 10
        const val MAX_DAILY_TURN_SIGNAL_POINTS = 10
        const val REQUIRED_CONSECUTIVE_SAFE_DAYS = 5
        const val SAFE_DRIVE_DEFAULT_PASS_SCORE = 80
    }
}
