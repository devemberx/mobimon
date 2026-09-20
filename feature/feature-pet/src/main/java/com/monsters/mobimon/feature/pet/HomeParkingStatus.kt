package com.monsters.mobimon.feature.pet

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.monsters.mobimon.core.domain.DrivingState
import com.monsters.mobimon.core.domain.SignalQuality
import com.monsters.mobimon.core.domain.VehicleSnapshot
import com.monsters.mobimon.core.ui.MobiMonParkingBadge
import com.monsters.mobimon.core.ui.R as CoreUiR

@Composable
internal fun HomeParkingStatus(
    snapshot: VehicleSnapshot,
    modifier: Modifier = Modifier,
    scale: Float = 1f,
) {
    val isCharging = snapshot.quality == SignalQuality.VALID && snapshot.isCharging == true
    val badgeTextRes =
        when {
            snapshot.quality != SignalQuality.VALID || snapshot.drivingState == DrivingState.UNKNOWN ->
                CoreUiR.string.mobimon_parking_unconfirmed
            isCharging -> R.string.pet_charging_compact
            snapshot.drivingState == DrivingState.MOVING -> R.string.pet_driving_moving
            snapshot.gear?.uppercase() == "R" -> R.string.pet_gear_r_compact
            snapshot.gear?.uppercase() == "N" -> R.string.pet_gear_n_compact
            snapshot.gear?.uppercase() == "D" -> R.string.pet_gear_d_compact
            else -> CoreUiR.string.mobimon_parking_confirmed
        }
    val status = stringResource(badgeTextRes)
    MobiMonParkingBadge(
        status = status,
        modifier = modifier,
        scale = scale,
        showParkingIcon =
            badgeTextRes == CoreUiR.string.mobimon_parking_confirmed ||
                badgeTextRes == CoreUiR.string.mobimon_parking_unconfirmed,
    )
}
