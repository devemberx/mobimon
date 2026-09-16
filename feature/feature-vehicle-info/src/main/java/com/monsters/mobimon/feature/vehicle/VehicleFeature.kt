package com.monsters.mobimon.feature.vehicle

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.monsters.mobimon.core.navigation.AppRoute
import com.monsters.mobimon.core.navigation.FeatureEntry
import com.monsters.mobimon.core.navigation.FeatureNavigator
import com.monsters.mobimon.core.navigation.VehicleRoute
import com.monsters.mobimon.core.presentation.VehiclePresentation
import com.monsters.mobimon.core.ui.MobiMonDestination

class VehicleFeature(
    private val vehicle: VehiclePresentation,
) : FeatureEntry {
    override val routes = setOf(VehicleRoute.VEHICLE_INFO)

    @Composable
    override fun Content(
        route: AppRoute,
        navigator: FeatureNavigator,
        modifier: Modifier,
    ) {
        val snapshot = vehicle.snapshot()
        MobiMonDestination(
            stringResource(R.string.vehicle_destination_title),
            navigator.back,
            navigator.returnHome,
            modifier,
        ) {
            VehicleInfoScreen(snapshot = snapshot)
        }
    }
}
