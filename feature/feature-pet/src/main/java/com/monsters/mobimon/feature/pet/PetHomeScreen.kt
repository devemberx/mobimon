package com.monsters.mobimon.feature.pet

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.monsters.mobimon.core.domain.PetProfile
import com.monsters.mobimon.core.domain.QuestProgress
import com.monsters.mobimon.core.domain.QuestType
import com.monsters.mobimon.core.domain.SignalSource
import com.monsters.mobimon.core.domain.VehicleSnapshot
import com.monsters.mobimon.core.ui.MobiMonButton
import com.monsters.mobimon.core.ui.MobiMonButtonStyle
import com.monsters.mobimon.core.ui.MobiMonMessage
import com.monsters.mobimon.core.ui.MobiMonPointSummary
import com.monsters.mobimon.core.ui.MobiMonSourceBadge
import com.monsters.mobimon.core.ui.PetAvatar
import com.monsters.mobimon.core.ui.R as CoreUiR

/** Displays either in-app home from committed state; navigation belongs to the shell. */
@Composable
fun PetHomeScreen(
    profile: PetProfile,
    snapshot: VehicleSnapshot,
    progress: QuestProgress,
    onOpenMenu: () -> Unit,
    onOpenVehicleInfo: () -> Unit,
    onOpenQuests: () -> Unit,
    onPetClick: () -> Unit,
    onOpenAppearance: () -> Unit,
    modifier: Modifier = Modifier,
    pointBalance: Long? = null,
    pointLoadFailed: Boolean = false,
    legacyQuestVisible: Boolean = true,
    friendId: String? = "friend:mobi",
    accessoryId: String? = null,
    interactionAllowed: Boolean = false,
    profileObservationFailed: Boolean = false,
    onRetryProfile: () -> Unit = {},
    inventoryLoaded: Boolean = true,
    inventoryLoadFailed: Boolean = false,
    connectionAvailable: Boolean = false,
) {
    val connectionText = if (connectionAvailable) R.string.pet_connection_description else R.string.pet_ai_unavailable
    val talkText = if (connectionAvailable) R.string.pet_talk_action else R.string.pet_talk_unavailable
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(CoreUiR.drawable.bg_main),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0f to MaterialTheme.colorScheme.background.copy(alpha = 0.48f),
                            0.45f to MaterialTheme.colorScheme.background.copy(alpha = 0.08f),
                            1f to MaterialTheme.colorScheme.background.copy(alpha = 0.64f),
                        ),
                    ),
            )
            val fontScale = LocalDensity.current.fontScale
            val wide = maxWidth / fontScale >= 1200.dp
            val edge = (maxWidth * 0.025f).coerceIn(24.dp, 64.dp)
            val sceneHeight = (maxHeight * if (wide) 0.52f else 0.4f).coerceIn(320.dp, 680.dp)
            val avatarSize = (maxHeight * if (wide) 0.42f else 0.32f).coerceIn(220.dp, 620.dp)
            Column(
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .heightIn(min = maxHeight)
                    .padding(edge),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement =
                    Arrangement.spacedBy(
                        if (wide) 12.dp else 24.dp,
                        Alignment.CenterVertically,
                    ),
            ) {
                HomeHeader(snapshot, pointBalance, pointLoadFailed, onOpenMenu, onOpenAppearance)
                if (profileObservationFailed) {
                    HomeFailure(stringResource(R.string.pet_profile_observation_failed), onRetryProfile)
                }
                if (inventoryLoadFailed) {
                    HomeFailure(stringResource(R.string.pet_inventory_failed), onRetryProfile)
                }
                if (wide) {
                    Box(Modifier.fillMaxWidth().heightIn(min = sceneHeight)) {
                        HomeCompanionScene(
                            profile = profile,
                            friendId = friendId,
                            accessoryId = accessoryId,
                            inventoryLoaded = inventoryLoaded,
                            inventoryLoadFailed = inventoryLoadFailed,
                            avatarSize = avatarSize,
                            modifier =
                                Modifier
                                    .fillMaxWidth(0.56f)
                                    .heightIn(min = sceneHeight)
                                    .align(Alignment.Center),
                        )
                        HomeArtworkActions(
                            onOpenVehicleInfo = onOpenVehicleInfo,
                            onOpenQuests = onOpenQuests,
                            showQuest = legacyQuestVisible,
                            modifier = Modifier.align(Alignment.CenterEnd),
                        )
                    }
                } else {
                    HomeCompanionScene(
                        profile = profile,
                        friendId = friendId,
                        accessoryId = accessoryId,
                        inventoryLoaded = inventoryLoaded,
                        inventoryLoadFailed = inventoryLoadFailed,
                        avatarSize = avatarSize,
                        modifier = Modifier.fillMaxWidth().heightIn(min = sceneHeight),
                    )
                    HomeArtworkActions(
                        onOpenVehicleInfo = onOpenVehicleInfo,
                        onOpenQuests = onOpenQuests,
                        showQuest = false,
                    )
                }
                HomeConversationAction(
                    talkText = talkText,
                    connectionText = connectionText,
                    enabled = connectionAvailable && interactionAllowed,
                    interactionAllowed = interactionAllowed,
                    onPetClick = onPetClick,
                )
                HomeSecondaryActions(
                    legacyQuestVisible && !wide,
                    progress,
                    onOpenQuests,
                )
            }
        }
    }
}

@Composable
private fun HomeCompanionScene(
    profile: PetProfile,
    friendId: String?,
    accessoryId: String?,
    inventoryLoaded: Boolean,
    inventoryLoadFailed: Boolean,
    avatarSize: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
) {
    Box(modifier.widthIn(max = 920.dp), contentAlignment = Alignment.Center) {
        Column(
            Modifier.align(Alignment.TopCenter).zIndex(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                stringResource(R.string.pet_home_greeting),
                style = MaterialTheme.typography.displaySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.semantics { heading() },
            )
            Text(
                stringResource(R.string.pet_home_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f),
                textAlign = TextAlign.Center,
            )
        }
        when {
            friendId != null -> {
                PetAvatar(
                    modifier = Modifier.size(avatarSize).align(Alignment.BottomCenter),
                    appearanceKey = profile.appearance.name,
                    friendId = friendId,
                    accessoryId = accessoryId,
                )
                Surface(
                    shape = RoundedCornerShape(36.dp),
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier =
                        Modifier
                            .widthIn(max = 300.dp)
                            .align(Alignment.CenterEnd)
                            .testTag("home-companion-message"),
                ) {
                    Text(
                        stringResource(R.string.pet_home_message),
                        modifier = Modifier.padding(horizontal = 32.dp, vertical = 22.dp),
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
            }
            inventoryLoadFailed -> Unit
            !inventoryLoaded -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(Modifier.padding(24.dp))
                    Text(stringResource(R.string.pet_inventory_loading))
                }
            }
            else ->
                Text(
                    stringResource(R.string.pet_no_friend),
                    modifier = Modifier.padding(24.dp),
                    textAlign = TextAlign.Center,
                )
        }
    }
}

@Composable
private fun HomeArtworkActions(
    onOpenVehicleInfo: () -> Unit,
    onOpenQuests: () -> Unit,
    showQuest: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        HomeArtworkButton(
            image = R.drawable.pet_home_vehicle_status_v4,
            description = stringResource(R.string.pet_vehicle_details),
            onClick = onOpenVehicleInfo,
        )
        if (showQuest) {
            HomeArtworkButton(
                image = R.drawable.pet_home_quest_v4,
                description = stringResource(R.string.pet_quest_start),
                onClick = onOpenQuests,
            )
        }
    }
}

@Composable
private fun HomeArtworkButton(
    image: Int,
    description: String,
    onClick: () -> Unit,
) {
    MobiMonButton(
        style = MobiMonButtonStyle.SECONDARY,
        onClick = onClick,
        modifier =
            Modifier
                .width(176.dp)
                .heightIn(min = 128.dp)
                .semantics { contentDescription = description },
    ) {
        Image(
            painter = painterResource(image),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(116.dp),
        )
    }
}

@Composable
private fun HomeConversationAction(
    talkText: Int,
    connectionText: Int,
    enabled: Boolean,
    interactionAllowed: Boolean,
    onPetClick: () -> Unit,
) {
    Column(
        Modifier.widthIn(max = 1000.dp).fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        MobiMonButton(
            onClick = onPetClick,
            enabled = enabled,
            modifier =
                Modifier
                    .widthIn(min = 280.dp, max = 540.dp)
                    .fillMaxWidth()
                    .heightIn(min = 76.dp),
        ) {
            Text(stringResource(talkText))
        }
        Text(
            stringResource(connectionText),
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
                MobiMonButton(
                    style = MobiMonButtonStyle.SECONDARY,
                    onClick = onOpenMenu,
                    modifier =
                        Modifier
                            .sizeIn(minWidth = 76.dp, minHeight = 76.dp)
                            .semantics { contentDescription = menuDescription },
                ) { Text(stringResource(R.string.pet_menu)) }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        stringResource(R.string.pet_brand),
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.semantics { heading() },
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            stringResource(R.string.pet_brand_tagline),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f),
                        )
                        if (snapshot.source == SignalSource.SIMULATED) MobiMonSourceBadge(simulated = true)
                    }
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
                MobiMonButton(
                    style = MobiMonButtonStyle.SECONDARY,
                    onClick = onOpenAppearance,
                    modifier = Modifier.heightIn(min = 76.dp),
                ) { Text(stringResource(R.string.pet_customize)) }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HomeSecondaryActions(
    legacyQuestVisible: Boolean,
    progress: QuestProgress,
    onOpenQuests: () -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (legacyQuestVisible) {
            MobiMonButton(
                style = MobiMonButtonStyle.SECONDARY,
                onClick = onOpenQuests,
                modifier = Modifier.heightIn(min = 76.dp),
            ) {
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
        MobiMonButton(onClick = onRetry, modifier = Modifier.heightIn(min = 76.dp)) {
            Text(stringResource(R.string.pet_retry))
        }
    }
}

/** Initial profile loading and failure use the same Home theme. */
@Composable
fun PetHomeLoadingScreen(
    failed: Boolean,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box {
            Column(
                Modifier
                    .fillMaxSize()
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
