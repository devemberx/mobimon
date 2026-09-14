package com.monsters.mobimon.core.domain

enum class SignalSource { REAL, SIMULATED }

enum class DrivingState { UNKNOWN, PARKED, MOVING }

enum class SignalQuality { UNAVAILABLE, VALID, STALE }

enum class SignalUnavailableReason { UNSUPPORTED, PERMISSION_DENIED, DISCONNECTED, NOT_REPORTED, INVALID }

enum class WarningSeverity { NOTICE, CAUTION, CRITICAL }

data class VehicleWarning(
    val item: String,
    val location: String? = null,
    val severity: WarningSeverity,
    val description: String,
    val nextAction: String,
    val observedAtMillis: Long,
    val quality: SignalQuality = SignalQuality.VALID,
)

data class VehicleSnapshot(
    val id: String,
    val epoch: String,
    val sequence: Long,
    val receivedAtMillis: Long,
    val source: SignalSource,
    val drivingState: DrivingState,
    val quality: SignalQuality,
    val batteryPercent: Int? = null,
    val batteryReceivedAtMillis: Long? = null,
    val batteryQuality: SignalQuality? = null,
    val parkingUnavailableReason: SignalUnavailableReason? = null,
    val batteryUnavailableReason: SignalUnavailableReason? = null,
    val warnings: List<VehicleWarning> = emptyList(),
    /** Derived UI metadata; never used as quest evidence. */
    val parkingAgeMillis: Long? = null,
    val batteryAgeMillis: Long? = null,
)
