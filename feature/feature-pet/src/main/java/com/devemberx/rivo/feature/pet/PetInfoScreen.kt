package com.devemberx.rivo.feature.pet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.devemberx.rivo.core.domain.DrivingState
import com.devemberx.rivo.core.domain.PetProfile
import com.devemberx.rivo.core.domain.QuestProgress
import com.devemberx.rivo.core.domain.QuestType
import com.devemberx.rivo.core.domain.SignalQuality
import com.devemberx.rivo.core.domain.VehicleSnapshot
import com.devemberx.rivo.core.ui.PetAvatar
import com.devemberx.rivo.core.ui.RivoContentColumn
import com.devemberx.rivo.core.ui.RivoGrowthSummary
import com.devemberx.rivo.core.ui.RivoSection

/** Displays saved growth and experiences independently of current vehicle availability. */
@Composable
fun PetInfoScreen(
    profile: PetProfile,
    stage: Int,
    xpUntilNextStage: Int?,
    snapshot: VehicleSnapshot,
    progress: QuestProgress,
    modifier: Modifier = Modifier,
) {
    RivoContentColumn(modifier = modifier) {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            PetAvatar(appearanceKey = profile.appearance.name, stage = stage)
        }
        RivoSection(title = stringResource(R.string.pet_growth_title)) {
            RivoGrowthSummary(profile.totalXp, stage, xpUntilNextStage)
            Text(stringResource(R.string.pet_growth_independent), style = MaterialTheme.typography.bodyMedium)
        }
        RivoSection(title = stringResource(R.string.pet_vehicle_status_title)) {
            PetVehicleSummary(snapshot)
        }
        RivoSection(title = stringResource(R.string.pet_experiences_title)) {
            if (progress.completions.isEmpty()) {
                Text(stringResource(R.string.pet_experiences_empty))
            } else {
                progress.completions.forEach { completion ->
                    val title =
                        stringResource(
                            when (completion.type) {
                                QuestType.Q01 -> R.string.pet_experience_q01
                                QuestType.Q02 -> R.string.pet_experience_q02
                                QuestType.Q03 -> R.string.pet_experience_q03
                            },
                        )
                    Text(stringResource(R.string.pet_experience_reward, title, completion.awardedXp))
                }
            }
        }
    }
}

@Composable
internal fun PetVehicleSummary(
    snapshot: VehicleSnapshot,
    modifier: Modifier = Modifier,
) {
    val valid = snapshot.quality == SignalQuality.VALID
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            stringResource(
                when {
                    !valid || snapshot.drivingState == DrivingState.UNKNOWN -> R.string.pet_driving_unknown
                    snapshot.drivingState == DrivingState.MOVING -> R.string.pet_driving_moving
                    else -> R.string.pet_driving_parked
                },
            ),
            style = MaterialTheme.typography.titleMedium,
        )
        val battery = snapshot.batteryPercent?.takeIf { valid && it in 0..100 }
        Text(
            if (battery == null) {
                stringResource(R.string.pet_vehicle_unavailable)
            } else {
                stringResource(R.string.pet_battery, battery)
            },
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
