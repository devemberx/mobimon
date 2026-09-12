package com.monsters.mobimon.feature.vehicle

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.monsters.mobimon.core.domain.DrivingState
import com.monsters.mobimon.core.domain.SignalQuality
import com.monsters.mobimon.core.domain.SignalSource
import com.monsters.mobimon.core.domain.SignalUnavailableReason
import com.monsters.mobimon.core.domain.VehicleSnapshot
import com.monsters.mobimon.core.domain.WarningSeverity
import com.monsters.mobimon.core.ui.MobiMonContentColumn
import com.monsters.mobimon.core.ui.MobiMonMessage
import com.monsters.mobimon.core.ui.MobiMonSection
import com.monsters.mobimon.core.ui.MobiMonSourceBadge

/** Displays a snapshot and forwards its displayed ID when Q01 acknowledgment is allowed. */
@Composable
fun VehicleInfoScreen(
    snapshot: VehicleSnapshot,
    questActive: Boolean,
    questCompleted: Boolean,
    canAcknowledge: Boolean,
    onAcknowledge: (String) -> Unit,
    modifier: Modifier = Modifier,
    isBusy: Boolean = false,
    errorMessage: String? = null,
    legacyQuestVisible: Boolean = true,
) {
    val valid = snapshot.quality == SignalQuality.VALID
    val parked = valid && snapshot.drivingState == DrivingState.PARKED
    val enabled = questActive && !questCompleted && canAcknowledge && parked && !isBusy
    MobiMonContentColumn(modifier = modifier) {
        MobiMonSourceBadge(simulated = snapshot.source == SignalSource.SIMULATED)
        MobiMonSection(title = stringResource(R.string.vehicle_current_status)) {
            Text(
                text =
                    stringResource(
                        when (snapshot.quality) {
                            SignalQuality.VALID -> R.string.vehicle_quality_valid
                            SignalQuality.STALE -> R.string.vehicle_quality_stale
                            SignalQuality.UNAVAILABLE -> R.string.vehicle_quality_unavailable
                        },
                    ),
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                stringResource(
                    when {
                        !valid || snapshot.drivingState == DrivingState.UNKNOWN -> R.string.vehicle_driving_unknown
                        snapshot.drivingState == DrivingState.MOVING -> R.string.vehicle_driving_moving
                        else -> R.string.vehicle_driving_parked
                    },
                ),
            )
            if (!valid) {
                snapshot.parkingUnavailableReason?.let { Text(stringResource(it.description())) }
                if (snapshot.quality == SignalQuality.STALE) {
                    snapshot.parkingAgeMillis?.let { Text(lastCheckedText(it)) }
                }
            }
            val battery =
                snapshot.batteryPercent?.takeIf {
                    (snapshot.batteryQuality ?: snapshot.quality) == SignalQuality.VALID && it in 0..100
                }
            Text(
                text =
                    if (battery == null) {
                        stringResource(
                            if ((snapshot.batteryQuality ?: snapshot.quality) == SignalQuality.STALE) {
                                R.string.vehicle_battery_stale
                            } else if (snapshot.batteryUnavailableReason == SignalUnavailableReason.UNSUPPORTED) {
                                R.string.vehicle_battery_unsupported
                            } else if (snapshot.batteryUnavailableReason == SignalUnavailableReason.PERMISSION_DENIED) {
                                R.string.vehicle_battery_permission
                            } else if (snapshot.batteryUnavailableReason == SignalUnavailableReason.DISCONNECTED) {
                                R.string.vehicle_battery_disconnected
                            } else {
                                R.string.vehicle_battery_unavailable
                            },
                        )
                    } else {
                        stringResource(R.string.vehicle_battery, battery)
                    },
                style = MaterialTheme.typography.titleLarge,
            )
            if ((snapshot.batteryQuality ?: snapshot.quality) == SignalQuality.STALE) {
                snapshot.batteryAgeMillis?.let { Text(lastCheckedText(it)) }
            }
        }
        if (snapshot.warnings.isNotEmpty()) {
            MobiMonSection(title = stringResource(R.string.vehicle_warnings_title)) {
                snapshot.warnings.forEach { warning ->
                    Text(
                        stringResource(
                            if (warning.quality == SignalQuality.VALID) {
                                R.string.vehicle_warning_current
                            } else {
                                R.string.vehicle_warning_previous
                            },
                            stringResource(
                                when (warning.severity) {
                                    WarningSeverity.NOTICE -> R.string.vehicle_warning_notice
                                    WarningSeverity.CAUTION -> R.string.vehicle_warning_caution
                                    WarningSeverity.CRITICAL -> R.string.vehicle_warning_critical
                                },
                            ),
                            warning.item,
                        ),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    warning.location?.let { Text(it) }
                    Text(warning.description)
                    if (warning.quality == SignalQuality.VALID) Text(warning.nextAction)
                }
            }
        }
        if (!parked) {
            MobiMonMessage(stringResource(R.string.vehicle_parked_required))
        }
        errorMessage?.let { MobiMonMessage(it, isError = true) }
        if (legacyQuestVisible) {
            MobiMonSection(title = stringResource(R.string.vehicle_q01_title)) {
                Text(
                    stringResource(
                        when {
                            questCompleted -> R.string.vehicle_q01_completed
                            !questActive -> R.string.vehicle_q01_start_required
                            isBusy -> R.string.vehicle_q01_saving
                            !canAcknowledge && parked -> R.string.vehicle_q01_waiting
                            else -> R.string.vehicle_q01_instruction
                        },
                    ),
                )
                Button(
                    onClick = { onAcknowledge(snapshot.id) },
                    enabled = enabled,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 76.dp),
                ) {
                    Text(stringResource(R.string.vehicle_acknowledge))
                }
            }
        }
    }
}

@Composable
private fun lastCheckedText(ageMillis: Long): String {
    val seconds = ageMillis / 1_000
    return when {
        seconds < 60 -> stringResource(R.string.vehicle_last_checked_seconds, seconds)
        seconds < 3_600 -> stringResource(R.string.vehicle_last_checked_minutes, seconds / 60)
        seconds < 86_400 -> stringResource(R.string.vehicle_last_checked_hours, seconds / 3_600)
        else -> stringResource(R.string.vehicle_last_checked_days, seconds / 86_400)
    }
}

private fun SignalUnavailableReason.description(): Int =
    when (this) {
        SignalUnavailableReason.UNSUPPORTED -> R.string.vehicle_parking_unsupported
        SignalUnavailableReason.PERMISSION_DENIED -> R.string.vehicle_parking_permission
        SignalUnavailableReason.DISCONNECTED -> R.string.vehicle_parking_disconnected
        SignalUnavailableReason.NOT_REPORTED, SignalUnavailableReason.INVALID -> R.string.vehicle_parking_unavailable
    }
