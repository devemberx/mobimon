package com.monsters.mobimon.feature.pet

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.monsters.mobimon.core.domain.VehicleSnapshot
import com.monsters.mobimon.core.presentation.parkingBadgeConfirmed
import com.monsters.mobimon.core.ui.MobiMonParkingStatusBadge

@Composable
internal fun HomeParkingStatus(
    snapshot: VehicleSnapshot,
    modifier: Modifier = Modifier,
    scale: Float = 1f,
) {
    MobiMonParkingStatusBadge(
        confirmed = snapshot.parkingBadgeConfirmed,
        modifier = modifier,
        scale = scale,
    )
}
