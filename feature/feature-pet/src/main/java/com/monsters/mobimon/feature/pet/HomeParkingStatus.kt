package com.monsters.mobimon.feature.pet

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.monsters.mobimon.core.domain.DrivingState
import com.monsters.mobimon.core.domain.SignalQuality
import com.monsters.mobimon.core.domain.VehicleSnapshot
import com.monsters.mobimon.core.ui.MobiMonStatusBadge
import com.monsters.mobimon.core.ui.MobiMonStatusTone

@Composable
internal fun HomeParkingStatus(
    snapshot: VehicleSnapshot,
    modifier: Modifier = Modifier,
) {
    val isCharging = snapshot.quality == SignalQuality.VALID && snapshot.isCharging == true
    val parked = !isCharging && snapshot.quality == SignalQuality.VALID && snapshot.drivingState == DrivingState.PARKED
    val status =
        stringResource(
            when {
                snapshot.quality != SignalQuality.VALID || snapshot.drivingState == DrivingState.UNKNOWN ->
                    R.string.pet_driving_unknown
                isCharging -> R.string.pet_charging_compact
                snapshot.drivingState == DrivingState.MOVING -> R.string.pet_driving_moving
                else -> R.string.pet_driving_parked
            },
        )
    MobiMonStatusBadge(
        modifier.semantics(mergeDescendants = true) { contentDescription = status },
        tone = if (isCharging || parked) MobiMonStatusTone.SUCCESS else MobiMonStatusTone.INFORMATION,
    ) {
        Text(
            when {
                isCharging -> stringResource(R.string.pet_charging_compact)
                parked -> stringResource(R.string.pet_parking_compact)
                else -> status
            },
            style = MaterialTheme.typography.labelLarge,
        )
    }
}
