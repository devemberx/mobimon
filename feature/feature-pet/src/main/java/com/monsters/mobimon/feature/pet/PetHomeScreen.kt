package com.monsters.mobimon.feature.pet

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.monsters.mobimon.core.domain.CompanionSettings
import com.monsters.mobimon.core.domain.PetProfile
import com.monsters.mobimon.core.domain.QuestProgress
import com.monsters.mobimon.core.domain.QuestType
import com.monsters.mobimon.core.domain.SignalSource
import com.monsters.mobimon.core.domain.VehicleSnapshot
import com.monsters.mobimon.core.ui.MobiMonMessage
import com.monsters.mobimon.core.ui.MobiMonPointSummary
import com.monsters.mobimon.core.ui.MobiMonSourceBadge
import com.monsters.mobimon.core.ui.PetAvatar

/** Displays either in-app home from committed state; navigation belongs to the shell. */
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
    interactionAllowed: Boolean = false,
    profileObservationFailed: Boolean = false,
    onRetryProfile: () -> Unit = {},
    inventoryLoaded: Boolean = true,
    inventoryLoadFailed: Boolean = false,
) {
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box {
            HomeScenery(Modifier.matchParentSize())
            BoxWithConstraints(Modifier.fillMaxSize().safeDrawingPadding()) {
                val fontScale = LocalDensity.current.fontScale
                val composed =
                    maxWidth / fontScale >= 1400.dp &&
                        maxHeight / fontScale >= 800.dp &&
                        fontScale <= 1.2f &&
                        !vehiclePreview &&
                        !profileObservationFailed &&
                        !inventoryLoadFailed &&
                        inventoryLoaded &&
                        pointBalance != null &&
                        !pointLoadFailed &&
                        friendId != null &&
                        snapshot.warnings.isEmpty()
                if (composed) {
                    HomeComposition(
                        maxWidth,
                        maxHeight,
                        profile,
                        snapshot,
                        requireNotNull(friendId),
                        pointBalance,
                        pointLoadFailed,
                        onOpenMenu,
                        onOpenAppearance,
                        onOpenVehicleInfo,
                        onPetClick,
                    ) {
                        HomeSignalNotes(snapshot)
                        Text(stringResource(R.string.pet_ai_unavailable), textAlign = TextAlign.Center)
                        if (!interactionAllowed) {
                            Text(
                                stringResource(R.string.pet_interaction_restricted),
                                textAlign = TextAlign.Center,
                            )
                        }
                        HomeSecondaryActions(vehiclePreview, legacyQuestVisible, progress, onSwitchHome, onOpenQuests)
                    }
                    return@BoxWithConstraints
                }
                val edge = (maxWidth * 0.025f).coerceIn(24.dp, 64.dp)
                val sceneHeight = (maxHeight * 0.4f).coerceIn(300.dp, 800.dp)
                val avatarSize = (maxHeight * 0.32f).coerceIn(200.dp, 620.dp)
                Column(
                    Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .heightIn(min = maxHeight)
                        .padding(edge),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
                ) {
                    HomeHeader(snapshot, pointBalance, pointLoadFailed, onOpenMenu, onOpenAppearance)
                    if (vehiclePreview) MobiMonMessage(stringResource(R.string.pet_vehicle_preview_notice))
                    if (profileObservationFailed) {
                        HomeFailure(stringResource(R.string.pet_profile_observation_failed), onRetryProfile)
                    }
                    if (inventoryLoadFailed) {
                        HomeFailure(stringResource(R.string.pet_inventory_failed), onRetryProfile)
                    }
                    Column(
                        Modifier.fillMaxWidth().heightIn(min = sceneHeight),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Surface(
                            shape = RoundedCornerShape(48.dp),
                            color = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.widthIn(max = 840.dp),
                        ) {
                            Text(
                                stringResource(R.string.pet_home_greeting),
                                modifier = Modifier.padding(horizontal = 40.dp, vertical = 20.dp),
                                style = MaterialTheme.typography.headlineMedium,
                                textAlign = TextAlign.Center,
                            )
                        }
                        val visible = !vehiclePreview || settings.showOnVehicleHome
                        when {
                            !visible ->
                                Text(
                                    stringResource(R.string.pet_hidden),
                                    modifier = Modifier.padding(48.dp),
                                    textAlign = TextAlign.Center,
                                )
                            friendId != null -> {
                                PetAvatar(
                                    modifier = Modifier.size(avatarSize),
                                    appearanceKey = profile.appearance.name,
                                    friendId = friendId,
                                )
                            }
                            inventoryLoadFailed -> Unit
                            !inventoryLoaded -> {
                                CircularProgressIndicator(Modifier.padding(24.dp))
                                Text(stringResource(R.string.pet_inventory_loading))
                            }
                            else ->
                                Text(
                                    stringResource(R.string.pet_no_friend),
                                    modifier = Modifier.padding(24.dp),
                                    textAlign = TextAlign.Center,
                                )
                        }
                    }
                    HomeVehicleCard(snapshot, onOpenVehicleInfo, Modifier.widthIn(max = 900.dp).fillMaxWidth())
                    Column(
                        Modifier.widthIn(max = 1000.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Button(
                            onClick = onPetClick,
                            enabled = false,
                            modifier =
                                Modifier
                                    .widthIn(
                                        min = 280.dp,
                                        max = 540.dp,
                                    ).fillMaxWidth()
                                    .heightIn(min = 76.dp),
                            colors =
                                ButtonDefaults.buttonColors(
                                    disabledContainerColor = MaterialTheme.colorScheme.primary,
                                    disabledContentColor = MaterialTheme.colorScheme.onPrimary,
                                ),
                        ) { Text(stringResource(R.string.pet_talk_unavailable)) }
                        Text(
                            stringResource(R.string.pet_ai_unavailable),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                        )
                        if (!interactionAllowed) {
                            Text(
                                stringResource(R.string.pet_interaction_restricted),
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                    HomeSecondaryActions(vehiclePreview, legacyQuestVisible, progress, onSwitchHome, onOpenQuests)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HomeHeader(
    snapshot: VehicleSnapshot,
    pointBalance: Long?,
    pointLoadFailed: Boolean,
    onOpenMenu: () -> Unit,
    onOpenAppearance: () -> Unit,
) {
    val menuDescription = stringResource(R.string.pet_open_menu)
    val fontScale = LocalDensity.current.fontScale
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val wide = maxWidth / fontScale >= 1180.dp
        FlowRow(
            Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.spacedBy(
                    24.dp,
                    if (wide) Alignment.Start else Alignment.CenterHorizontally,
                ),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Row(
                modifier = if (wide) Modifier.weight(1f) else Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                OutlinedButton(
                    onClick = onOpenMenu,
                    modifier =
                        Modifier
                            .sizeIn(minWidth = 76.dp, minHeight = 76.dp)
                            .semantics { contentDescription = menuDescription },
                    shape = RoundedCornerShape(24.dp),
                ) { Text(stringResource(R.string.pet_menu)) }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        stringResource(R.string.pet_brand),
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.semantics { heading() },
                    )
                    if (snapshot.source == SignalSource.SIMULATED) MobiMonSourceBadge(simulated = true)
                }
            }
            HomeParkingStatus(snapshot)
            FlowRow(
                modifier = if (wide) Modifier.weight(1f) else Modifier,
                horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.End),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(Modifier.heightIn(min = 76.dp), contentAlignment = Alignment.Center) {
                    MobiMonPointSummary(pointBalance, failed = pointLoadFailed)
                }
                Button(
                    onClick = onOpenAppearance,
                    modifier = Modifier.heightIn(min = 76.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                ) { Text(stringResource(R.string.pet_customize)) }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HomeSecondaryActions(
    vehiclePreview: Boolean,
    legacyQuestVisible: Boolean,
    progress: QuestProgress,
    onSwitchHome: () -> Unit,
    onOpenQuests: () -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TextButton(onClick = onSwitchHome, modifier = Modifier.heightIn(min = 76.dp)) {
            Text(stringResource(if (vehiclePreview) R.string.pet_companion_home else R.string.pet_vehicle_home))
        }
        if (legacyQuestVisible) {
            TextButton(onClick = onOpenQuests, modifier = Modifier.heightIn(min = 76.dp)) {
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

@Composable
private fun HomeFailure(
    message: String,
    onRetry: () -> Unit,
) {
    Column(Modifier.widthIn(max = 1320.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        MobiMonMessage(message, isError = true)
        Button(onClick = onRetry, modifier = Modifier.heightIn(min = 76.dp)) {
            Text(stringResource(R.string.pet_retry))
        }
    }
}

/** Initial profile loading and failure use the same fixed Home setting. */
@Composable
fun PetHomeLoadingScreen(
    failed: Boolean,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box {
            HomeScenery(Modifier.matchParentSize())
            Column(
                Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(stringResource(R.string.pet_brand), style = MaterialTheme.typography.headlineMedium)
                if (failed) {
                    HomeFailure(stringResource(R.string.pet_home_load_failed), onRetry)
                } else {
                    CircularProgressIndicator()
                    Text(stringResource(R.string.pet_home_loading))
                }
            }
        }
    }
}
