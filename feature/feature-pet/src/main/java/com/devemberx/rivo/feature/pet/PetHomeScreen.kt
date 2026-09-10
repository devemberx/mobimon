package com.devemberx.rivo.feature.pet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.devemberx.rivo.core.domain.CompanionSettings
import com.devemberx.rivo.core.domain.PetProfile
import com.devemberx.rivo.core.domain.QuestProgress
import com.devemberx.rivo.core.domain.QuestType
import com.devemberx.rivo.core.domain.SignalSource
import com.devemberx.rivo.core.domain.VehicleSnapshot
import com.devemberx.rivo.core.ui.PetAvatar
import com.devemberx.rivo.core.ui.RivoContentColumn
import com.devemberx.rivo.core.ui.RivoGrowthSummary
import com.devemberx.rivo.core.ui.RivoMessage
import com.devemberx.rivo.core.ui.RivoSection
import com.devemberx.rivo.core.ui.RivoSourceBadge
import com.devemberx.rivo.core.ui.RivoTheme

/** Displays either in-app home from shared committed state; callbacks are owned by the shell. */
@Composable
fun PetHomeScreen(
    profile: PetProfile,
    stage: Int,
    xpUntilNextStage: Int?,
    snapshot: VehicleSnapshot,
    progress: QuestProgress,
    settings: CompanionSettings,
    onOpenMenu: () -> Unit,
    onOpenVehicleInfo: () -> Unit,
    onOpenQuests: () -> Unit,
    onSwitchHome: () -> Unit,
    onPetClick: () -> Unit,
    modifier: Modifier = Modifier,
    vehiclePreview: Boolean = false,
    showConversationUnavailable: Boolean = false,
) {
    RivoTheme(darkTheme = vehiclePreview) {
        Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            RivoContentColumn(modifier = Modifier.fillMaxSize()) {
                HomeActions(vehiclePreview, onOpenMenu, onSwitchHome)
                RivoSourceBadge(simulated = snapshot.source == SignalSource.SIMULATED)
                Text(
                    stringResource(if (vehiclePreview) R.string.pet_vehicle_heading else R.string.pet_home_heading),
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text(stringResource(R.string.pet_home_subtitle), color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (vehiclePreview) RivoMessage(stringResource(R.string.pet_vehicle_preview_notice))
                PetScene(
                    profile = profile,
                    stage = stage,
                    visible = !vehiclePreview || settings.showOnVehicleHome,
                    onPetClick = onPetClick,
                )
                if (showConversationUnavailable) RivoMessage(stringResource(R.string.pet_conversation_unavailable))
                RivoSection(title = stringResource(R.string.pet_growth_title)) {
                    RivoGrowthSummary(profile.totalXp, stage, xpUntilNextStage)
                }
                RivoSection(title = stringResource(R.string.pet_vehicle_status_title)) {
                    PetVehicleSummary(snapshot)
                    OutlinedButton(
                        onClick = onOpenVehicleInfo,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    ) {
                        Text(stringResource(R.string.pet_vehicle_details))
                    }
                }
                Button(onClick = onOpenQuests, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                    Text(
                        stringResource(
                            when {
                                progress.completions.any { it.type == QuestType.Q01 } -> R.string.pet_quest_history
                                progress.activeRun != null -> R.string.pet_quest_continue
                                else -> R.string.pet_quest_start
                            },
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeActions(
    vehiclePreview: Boolean,
    onOpenMenu: () -> Unit,
    onSwitchHome: () -> Unit,
) {
    val menuDescription = stringResource(R.string.pet_open_menu)
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        TextButton(
            onClick = onOpenMenu,
            modifier = Modifier.heightIn(min = 48.dp).semantics { contentDescription = menuDescription },
        ) {
            Text(stringResource(R.string.pet_menu))
        }
        TextButton(onClick = onSwitchHome, modifier = Modifier.heightIn(min = 48.dp)) {
            Text(stringResource(if (vehiclePreview) R.string.pet_companion_home else R.string.pet_vehicle_home))
        }
    }
}

@Composable
private fun PetScene(
    profile: PetProfile,
    stage: Int,
    visible: Boolean,
    onPetClick: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (visible) {
            val talkDescription = stringResource(R.string.pet_talk)
            Box(
                modifier =
                    Modifier
                        .size(144.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .clickable(role = Role.Button, onClick = onPetClick)
                        .semantics { contentDescription = talkDescription },
                contentAlignment = Alignment.Center,
            ) {
                PetAvatar(appearanceKey = profile.appearance.name, stage = stage)
            }
            Text(stringResource(R.string.pet_tap_hint), color = MaterialTheme.colorScheme.onPrimaryContainer)
        } else {
            Text(
                stringResource(R.string.pet_hidden),
                modifier = Modifier.padding(vertical = 36.dp),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}
