package com.monsters.mobimon.feature.vehicle

import com.monsters.mobimon.core.domain.SignalQuality
import com.monsters.mobimon.core.domain.VehicleSnapshot
import com.monsters.mobimon.core.domain.VehicleWarning
import com.monsters.mobimon.core.domain.WarningSeverity

internal enum class VehicleCondition { CHECKED, PARTIAL, LOW_BATTERY, WARNING, STALE, UNAVAILABLE }

internal enum class DriverAssistWarning { EMERGENCY_BRAKING, DROWSY, DISTRACTED }

internal data class VehicleInfoUiState(
    val condition: VehicleCondition,
    val batteryPercent: Int?,
    val tireStatus: String?,
    val tireWarning: VehicleWarning?,
    val outsideTemperature: Int?,
    val isRaining: Boolean?,
    val attentionLevel: Int?,
    val frontDistance: Int?,
    val assistWarning: DriverAssistWarning?,
    val assistChecked: Boolean,
)

internal fun VehicleSnapshot.toVehicleInfoUiState(): VehicleInfoUiState {
    val battery = batteryPercent?.takeIf { (batteryQuality ?: quality) == SignalQuality.VALID && it in 0..100 }
    // These legacy optional readings share the snapshot timestamp until the adapter supplies per-signal metadata.
    val current = takeIf { quality == SignalQuality.VALID }
    val tire = current?.tirePressureStatus?.takeIf(String::isNotBlank)
    val tireWarning =
        warnings.firstOrNull {
            it.quality == SignalQuality.VALID && (it.item.contains("타이어") || it.item.contains("바퀴"))
        }
    val assistWarning =
        when {
            current?.isEmergencyBraking == true -> DriverAssistWarning.EMERGENCY_BRAKING
            current?.isDrowsy == true -> DriverAssistWarning.DROWSY
            current?.isDistracted == true -> DriverAssistWarning.DISTRACTED
            else -> null
        }
    val assistChecked =
        current?.isEmergencyBraking != null && current.isDrowsy != null && current.isDistracted != null
    val condition =
        when {
            quality == SignalQuality.UNAVAILABLE -> VehicleCondition.UNAVAILABLE
            quality == SignalQuality.STALE -> VehicleCondition.STALE
            assistWarning != null ||
                warnings.any {
                    it.quality == SignalQuality.VALID && it.severity != WarningSeverity.NOTICE
                } -> VehicleCondition.WARNING
            battery != null && battery < 20 -> VehicleCondition.LOW_BATTERY
            battery == null || tire == null || !assistChecked -> VehicleCondition.PARTIAL
            else -> VehicleCondition.CHECKED
        }
    return VehicleInfoUiState(
        condition = condition,
        batteryPercent = battery,
        tireStatus = tire,
        tireWarning = tireWarning,
        outsideTemperature = current?.outsideTemperature,
        isRaining = current?.isRaining,
        attentionLevel = current?.attentionLevel,
        frontDistance = current?.distanceToFrontVehicle,
        assistWarning = assistWarning,
        assistChecked = assistChecked,
    )
}
