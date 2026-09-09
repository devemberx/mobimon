package com.devemberx.rivo.feature.vehicle

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.devemberx.rivo.core.domain.DrivingState
import com.devemberx.rivo.core.domain.SignalQuality
import com.devemberx.rivo.core.domain.SignalSource
import com.devemberx.rivo.core.domain.VehicleSnapshot
import com.devemberx.rivo.core.ui.RivoContentColumn
import com.devemberx.rivo.core.ui.RivoMessage
import com.devemberx.rivo.core.ui.RivoSection
import com.devemberx.rivo.core.ui.RivoSourceBadge

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
) {
    val valid = snapshot.quality == SignalQuality.VALID
    val parked = valid && snapshot.drivingState == DrivingState.PARKED
    val enabled = questActive && !questCompleted && canAcknowledge && parked && !isBusy
    RivoContentColumn(modifier = modifier) {
        RivoSourceBadge(simulated = snapshot.source == SignalSource.SIMULATED)
        RivoSection(title = stringResource(R.string.vehicle_current_status)) {
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
            val battery = snapshot.batteryPercent?.takeIf { valid && it in 0..100 }
            Text(
                text =
                    if (battery == null) {
                        stringResource(R.string.vehicle_battery_unavailable)
                    } else {
                        stringResource(R.string.vehicle_battery, battery)
                    },
                style = MaterialTheme.typography.titleLarge,
            )
        }
        if (!parked) {
            RivoMessage(stringResource(R.string.vehicle_parked_required))
        }
        errorMessage?.let { RivoMessage(it, isError = true) }
        RivoSection(title = stringResource(R.string.vehicle_q01_title)) {
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
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            ) {
                Text(stringResource(R.string.vehicle_acknowledge))
            }
        }
    }
}
