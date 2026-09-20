package com.monsters.mobimon.feature.vehicle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.monsters.mobimon.core.navigation.AppRoute
import com.monsters.mobimon.core.navigation.FeatureEntry
import com.monsters.mobimon.core.navigation.FeatureNavigator
import com.monsters.mobimon.core.navigation.VehicleRoute
import com.monsters.mobimon.core.presentation.CompanionAppearancePresentation
import com.monsters.mobimon.core.presentation.VehiclePresentation
import com.monsters.mobimon.core.ui.MobiMonButton
import com.monsters.mobimon.core.ui.MobiMonDestination
import com.monsters.mobimon.core.ui.MobiMonDimensions
import com.monsters.mobimon.core.ui.MobiMonMessage

class VehicleFeature(
    private val vehicle: VehiclePresentation,
    private val appearance: CompanionAppearancePresentation,
) : FeatureEntry {
    override val routes = setOf(VehicleRoute.VEHICLE_INFO)

    @Composable
    override fun Content(
        route: AppRoute,
        navigator: FeatureNavigator,
        modifier: Modifier,
    ) {
        val snapshot = vehicle.snapshot()
        val appearanceModel = appearance.model()
        val equipped by appearanceModel.state.collectAsStateWithLifecycle()
        MobiMonDestination(
            stringResource(R.string.vehicle_destination_title),
            navigator.back,
            navigator.returnHome,
            modifier,
        ) {
            Column(Modifier.fillMaxSize()) {
                if (equipped.failed) {
                    Row(
                        Modifier.fillMaxWidth().padding(MobiMonDimensions.contentPadding),
                        horizontalArrangement = Arrangement.spacedBy(MobiMonDimensions.contentGap),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        MobiMonMessage(
                            stringResource(
                                if (equipped.inventory == null) {
                                    R.string.vehicle_appearance_load_failed
                                } else {
                                    R.string.vehicle_appearance_update_failed
                                },
                            ),
                            Modifier.weight(1f),
                            isError = true,
                        )
                        MobiMonButton(onClick = appearanceModel::retry) { Text(stringResource(R.string.vehicle_retry)) }
                    }
                }
                VehicleInfoScreen(
                    snapshot = snapshot,
                    modifier = Modifier.weight(1f),
                    friendId = equipped.friendId,
                    accessoryId = equipped.accessoryId,
                    backgroundId = equipped.backgroundId,
                    outfitId = equipped.outfitId,
                )
            }
        }
    }
}
