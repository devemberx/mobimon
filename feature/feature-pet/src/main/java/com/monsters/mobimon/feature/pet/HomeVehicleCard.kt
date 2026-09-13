package com.monsters.mobimon.feature.pet

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.monsters.mobimon.core.domain.DrivingState
import com.monsters.mobimon.core.domain.SignalQuality
import com.monsters.mobimon.core.domain.SignalUnavailableReason
import com.monsters.mobimon.core.domain.VehicleSnapshot
import com.monsters.mobimon.core.domain.WarningSeverity

@Composable
internal fun HomeParkingStatus(
    snapshot: VehicleSnapshot,
    modifier: Modifier = Modifier,
) {
    val parked = snapshot.quality == SignalQuality.VALID && snapshot.drivingState == DrivingState.PARKED
    Surface(
        modifier = modifier.semantics(mergeDescendants = true) {},
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(40.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.24f)),
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        ) {
            if (parked) Text(stringResource(R.string.pet_parking_symbol), color = MaterialTheme.colorScheme.secondary)
            Text(
                stringResource(
                    when {
                        snapshot.quality != SignalQuality.VALID || snapshot.drivingState == DrivingState.UNKNOWN ->
                            R.string.pet_driving_unknown
                        snapshot.drivingState == DrivingState.MOVING -> R.string.pet_driving_moving
                        else -> R.string.pet_driving_parked
                    },
                ),
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun HomeVehicleCard(
    snapshot: VehicleSnapshot,
    onOpenVehicleInfo: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val batteryQuality = snapshot.batteryQuality ?: snapshot.quality
    val battery = snapshot.batteryPercent?.takeIf { batteryQuality == SignalQuality.VALID && it in 0..100 }
    val currentWarning =
        snapshot.warnings
            .filter {
                it.quality == SignalQuality.VALID
            }.maxByOrNull { it.severity.ordinal }
    val historicalWarning = snapshot.warnings.any { it.quality != SignalQuality.VALID }
    val fontScale = LocalDensity.current.fontScale
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.24f)),
    ) {
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val wide = maxWidth / fontScale >= 600.dp
            FlowRow(
                Modifier.padding(
                    horizontal = 24.dp,
                    vertical =
                        if (snapshot.quality == SignalQuality.VALID &&
                            snapshot.warnings.isEmpty()
                        ) {
                            0.dp
                        } else {
                            8.dp
                        },
                ),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                maxItemsInEachRow = if (wide) 2 else 1,
            ) {
                Column(
                    Modifier.weight(1f).align(Alignment.CenterVertically),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (currentWarning != null) {
                        Text(
                            stringResource(
                                R.string.pet_warning_item,
                                stringResource(
                                    when (currentWarning.severity) {
                                        WarningSeverity.NOTICE -> R.string.pet_warning_notice
                                        WarningSeverity.CAUTION -> R.string.pet_warning_caution
                                        WarningSeverity.CRITICAL -> R.string.pet_warning_critical
                                    },
                                ),
                                currentWarning.item,
                            ),
                            style = MaterialTheme.typography.titleLarge,
                            color =
                                if (currentWarning.severity == WarningSeverity.NOTICE) {
                                    MaterialTheme.colorScheme.onSurface
                                } else {
                                    MaterialTheme.colorScheme.error
                                },
                        )
                        currentWarning.location?.let { Text(it) }
                        Text(currentWarning.description)
                        Text(currentWarning.nextAction)
                    }
                    Text(
                        if (battery != null) {
                            stringResource(R.string.pet_battery, battery)
                        } else {
                            stringResource(
                                if (batteryQuality == SignalQuality.STALE) {
                                    R.string.pet_battery_stale
                                } else {
                                    snapshot.batteryUnavailableReason.batteryDescription()
                                },
                            )
                        },
                        style = MaterialTheme.typography.titleMedium,
                    )
                    if (batteryQuality == SignalQuality.STALE) {
                        snapshot.batteryAgeMillis?.let {
                            Text(
                                lastChecked(it),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    if (snapshot.quality == SignalQuality.STALE) {
                        Text(
                            stringResource(R.string.pet_parking_stale),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        snapshot.parkingAgeMillis?.let {
                            Text(
                                lastChecked(it),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    } else if (snapshot.quality == SignalQuality.UNAVAILABLE) {
                        Text(
                            stringResource(snapshot.parkingUnavailableReason.parkingDescription()),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (historicalWarning) {
                        Text(
                            stringResource(R.string.pet_warning_historical),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                val detailsDescription = stringResource(R.string.pet_vehicle_details)
                TextButton(
                    onClick = onOpenVehicleInfo,
                    modifier =
                        (if (wide) Modifier else Modifier.fillMaxWidth())
                            .heightIn(min = 76.dp)
                            .semantics { contentDescription = detailsDescription }
                            .align(Alignment.CenterVertically),
                ) {
                    Text(stringResource(R.string.pet_vehicle_status), color = MaterialTheme.colorScheme.secondary)
                    Icon(
                        painterResource(R.drawable.pet_chevron),
                        null,
                        Modifier.padding(start = 12.dp).size(24.dp),
                        tint = MaterialTheme.colorScheme.secondary,
                    )
                }
            }
        }
    }
}

@Composable
private fun lastChecked(ageMillis: Long): String =
    stringResource(R.string.pet_last_checked_seconds, ageMillis.coerceAtLeast(0) / 1_000)

private fun SignalUnavailableReason?.batteryDescription(): Int =
    when (this) {
        SignalUnavailableReason.UNSUPPORTED -> R.string.pet_battery_unsupported
        SignalUnavailableReason.PERMISSION_DENIED -> R.string.pet_battery_permission
        SignalUnavailableReason.DISCONNECTED -> R.string.pet_battery_disconnected
        else -> R.string.pet_vehicle_unavailable
    }

private fun SignalUnavailableReason?.parkingDescription(): Int =
    when (this) {
        SignalUnavailableReason.UNSUPPORTED -> R.string.pet_parking_unsupported
        SignalUnavailableReason.PERMISSION_DENIED -> R.string.pet_parking_permission
        SignalUnavailableReason.DISCONNECTED -> R.string.pet_parking_disconnected
        else -> R.string.pet_parking_unavailable
    }
