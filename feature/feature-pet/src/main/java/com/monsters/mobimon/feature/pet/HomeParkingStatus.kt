package com.monsters.mobimon.feature.pet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monsters.mobimon.core.domain.DrivingState
import com.monsters.mobimon.core.domain.SignalQuality
import com.monsters.mobimon.core.domain.VehicleSnapshot
import com.monsters.mobimon.core.ui.MobiMonStatusBadge
import com.monsters.mobimon.core.ui.MobiMonStatusTone

@Composable
internal fun HomeParkingStatus(
    snapshot: VehicleSnapshot,
    modifier: Modifier = Modifier,
    scale: Float = 1f,
) {
    val isCharging = snapshot.quality == SignalQuality.VALID && snapshot.isCharging == true
    val parked = snapshot.quality == SignalQuality.VALID && snapshot.drivingState == DrivingState.PARKED
    val badgeTextRes =
        when {
            snapshot.quality != SignalQuality.VALID || snapshot.drivingState == DrivingState.UNKNOWN ->
                R.string.pet_driving_unknown
            isCharging -> R.string.pet_charging_compact
            snapshot.drivingState == DrivingState.MOVING -> R.string.pet_driving_moving
            snapshot.gear?.uppercase() == "R" -> R.string.pet_gear_r_compact
            snapshot.gear?.uppercase() == "N" -> R.string.pet_gear_n_compact
            snapshot.gear?.uppercase() == "D" -> R.string.pet_gear_d_compact
            else -> R.string.pet_driving_parked
        }
    val status = stringResource(badgeTextRes)
    MobiMonStatusBadge(
        modifier
            .widthIn(min = (344 * scale).dp)
            .heightIn(min = (76 * scale).dp)
            .semantics(mergeDescendants = true) { contentDescription = status },
        tone = MobiMonStatusTone.INFORMATION,
        contentPadding = PaddingValues(horizontal = (24 * scale).dp, vertical = (12 * scale).dp),
        horizontalArrangement = Arrangement.spacedBy((32 * scale).dp, Alignment.CenterHorizontally),
        borderWidth = (2 * scale).dp,
    ) {
        if (parked && !isCharging && badgeTextRes == R.string.pet_driving_parked) {
            Icon(painterResource(R.drawable.pet_parking_icon), null, Modifier.size((38 * scale).dp))
        }
        Text(
            status,
            style =
                MaterialTheme.typography.bodyMedium.copy(
                    fontSize = (30 * scale).coerceAtLeast(24f).sp,
                    fontWeight = FontWeight.Normal,
                ),
        )
    }
}
