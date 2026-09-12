package com.monsters.mobimon.feature.pet

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
import com.monsters.mobimon.core.domain.CompanionSettings
import com.monsters.mobimon.core.domain.PetProfile
import com.monsters.mobimon.core.domain.QuestProgress
import com.monsters.mobimon.core.domain.QuestType
import com.monsters.mobimon.core.domain.SignalSource
import com.monsters.mobimon.core.domain.VehicleSnapshot
import com.monsters.mobimon.core.ui.MobiMonContentColumn
import com.monsters.mobimon.core.ui.MobiMonMessage
import com.monsters.mobimon.core.ui.MobiMonPointSummary
import com.monsters.mobimon.core.ui.MobiMonSection
import com.monsters.mobimon.core.ui.MobiMonSourceBadge
import com.monsters.mobimon.core.ui.MobiMonTheme
import com.monsters.mobimon.core.ui.PetAvatar

/** Displays either in-app home from shared committed state; callbacks are owned by the shell. */
@Composable
fun PetHomeScreen(
    profile: PetProfile,
    snapshot: VehicleSnapshot,
    progress: QuestProgress,
    settings: CompanionSettings,
    onOpenMenu: () -> Unit,
    onOpenVehicleInfo: () -> Unit,
    onOpenQuests: () -> Unit,
    onSwitchHome: () -> Unit,
    onPetClick: () -> Unit,
    onOpenAppearance: () -> Unit,
    modifier: Modifier = Modifier,
    vehiclePreview: Boolean = false,
    pointBalance: Long? = null,
    pointLoadFailed: Boolean = false,
    legacyQuestVisible: Boolean = true,
    friendId: String? = "friend:mobi",
    interactionAllowed: Boolean = true,
    profileObservationFailed: Boolean = false,
    onRetryProfile: () -> Unit = {},
) {
    MobiMonTheme(darkTheme = true) {
        Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            MobiMonContentColumn(modifier = Modifier.fillMaxSize()) {
                HomeActions(vehiclePreview, onOpenMenu, onSwitchHome)
                MobiMonSourceBadge(simulated = snapshot.source == SignalSource.SIMULATED)
                Text(
                    stringResource(if (vehiclePreview) R.string.pet_vehicle_heading else R.string.pet_home_heading),
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text(stringResource(R.string.pet_home_subtitle), color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (vehiclePreview) MobiMonMessage(stringResource(R.string.pet_vehicle_preview_notice))
                if (profileObservationFailed) {
                    MobiMonMessage(stringResource(R.string.pet_profile_observation_failed), isError = true)
                    Button(onClick = onRetryProfile, modifier = Modifier.fillMaxWidth().heightIn(min = 76.dp)) {
                        Text(stringResource(R.string.pet_settings_retry))
                    }
                }
                PetScene(
                    profile = profile,
                    friendId = friendId,
                    visible = !vehiclePreview || settings.showOnVehicleHome,
                    onPetClick = onPetClick,
                    interactionAllowed = interactionAllowed,
                )
                MobiMonSection(title = stringResource(R.string.pet_customization_title)) {
                    MobiMonPointSummary(pointBalance, failed = pointLoadFailed)
                    OutlinedButton(
                        onClick = onOpenAppearance,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 76.dp),
                    ) { Text(stringResource(R.string.pet_customize)) }
                }
                MobiMonSection(title = stringResource(R.string.pet_vehicle_status_title)) {
                    PetVehicleSummary(snapshot)
                    OutlinedButton(
                        onClick = onOpenVehicleInfo,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 76.dp),
                    ) {
                        Text(stringResource(R.string.pet_vehicle_details))
                    }
                }
                Button(onClick = onOpenQuests, modifier = Modifier.fillMaxWidth().heightIn(min = 76.dp)) {
                    Text(
                        stringResource(
                            when {
                                !legacyQuestVisible -> R.string.pet_quests
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
            modifier = Modifier.heightIn(min = 76.dp).semantics { contentDescription = menuDescription },
        ) {
            Text(stringResource(R.string.pet_menu))
        }
        TextButton(onClick = onSwitchHome, modifier = Modifier.heightIn(min = 76.dp)) {
            Text(stringResource(if (vehiclePreview) R.string.pet_companion_home else R.string.pet_vehicle_home))
        }
    }
}

@Composable
private fun PetScene(
    profile: PetProfile,
    friendId: String?,
    visible: Boolean,
    onPetClick: () -> Unit,
    interactionAllowed: Boolean,
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
        if (visible && friendId != null) {
            Text(stringResource(if (friendId == "friend:luna") R.string.pet_friend_luna else R.string.pet_friend_mobi))
            val talkDescription = stringResource(R.string.pet_talk)
            Box(
                modifier =
                    Modifier
                        .size(144.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .then(
                            if (interactionAllowed) {
                                Modifier
                                    .clickable(role = Role.Button, onClick = onPetClick)
                                    .semantics { contentDescription = talkDescription }
                            } else {
                                Modifier
                            },
                        ),
                contentAlignment = Alignment.Center,
            ) {
                PetAvatar(appearanceKey = profile.appearance.name, friendId = friendId)
            }
            Text(stringResource(R.string.pet_tap_hint), color = MaterialTheme.colorScheme.onPrimaryContainer)
            if (!interactionAllowed) MobiMonMessage(stringResource(R.string.pet_interaction_restricted))
        } else if (!visible) {
            Text(
                stringResource(R.string.pet_hidden),
                modifier = Modifier.padding(vertical = 36.dp),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        } else {
            Text(stringResource(R.string.pet_inventory_loading))
        }
    }
}
