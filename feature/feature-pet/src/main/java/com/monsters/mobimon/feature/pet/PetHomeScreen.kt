package com.monsters.mobimon.feature.pet

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.monsters.mobimon.core.domain.PetProfile
import com.monsters.mobimon.core.domain.VehicleSnapshot
import com.monsters.mobimon.core.ui.MobiMonButton
import com.monsters.mobimon.core.ui.MobiMonMessage
import com.monsters.mobimon.core.ui.MobiMonNavigationButton
import com.monsters.mobimon.core.ui.MobiMonPointSummary
import com.monsters.mobimon.core.ui.PetAvatar

/** Displays the in-app Home from committed state; navigation belongs to the shell. */
@Composable
fun PetHomeScreen(
    profile: PetProfile,
    snapshot: VehicleSnapshot,
    onOpenMenu: () -> Unit,
    onPetClick: () -> Unit,
    modifier: Modifier = Modifier,
    pointBalance: Long? = null,
    pointLoadFailed: Boolean = false,
    friendId: String? = "friend:mobi",
    accessoryId: String? = null,
    outfitId: String? = null,
    backgroundId: String? = null,
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
            val backgroundRes =
                when (snapshot.timeOfDay) {
                    "Morning" -> R.drawable.pet_home_background_morning_v4
                    "Day" -> R.drawable.pet_home_background_day_v4
                    else -> R.drawable.pet_home_background_v4
                }
            Crossfade(
                targetState = backgroundRes,
                animationSpec = tween(durationMillis = 1000),
                modifier = Modifier.fillMaxSize(),
                label = "pet_home_background_crossfade",
            ) { targetRes ->
                Image(
                    painter = painterResource(targetRes),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
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
            if (backgroundId != null) {
                val particleType =
                    when {
                        backgroundId.contains("snow") -> com.monsters.mobimon.core.ui.ParticleType.SNOW
                        backgroundId.contains(
                            "petal",
                        ) ||
                            backgroundId.contains("flower") -> com.monsters.mobimon.core.ui.ParticleType.PETAL
                        else -> com.monsters.mobimon.core.ui.ParticleType.STAR
                    }
                com.monsters.mobimon.core.ui.FallingParticlesEffect(
                    particleType = particleType,
                    modifier = Modifier.fillMaxSize().testTag("home-background-particles"),
                )
            }
            val fontScale = LocalDensity.current.fontScale
            val wide = this.maxWidth / fontScale >= 1200.dp
            val horizontalEdge =
                if (wide) (this.maxWidth * 0.028125f).coerceIn(48.dp, 72.dp) else 24.dp
            val verticalEdge = (this.maxHeight * 0.04f).coerceIn(24.dp, 52.dp)
            val sceneHeight = (this.maxHeight * if (wide) 0.52f else 0.4f).coerceIn(320.dp, 680.dp)
            val avatarSize = (this.maxHeight * if (wide) 0.42f else 0.32f).coerceIn(220.dp, 620.dp)
            Column(
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .heightIn(min = maxHeight)
                    .padding(horizontal = horizontalEdge, vertical = verticalEdge),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement =
                    Arrangement.spacedBy(
                        if (wide) 12.dp else 24.dp,
                        Alignment.CenterVertically,
                    ),
            ) {
                HomeHeader(
                    snapshot = snapshot,
                    pointBalance = pointBalance,
                    pointLoadFailed = pointLoadFailed,
                    onOpenMenu = onOpenMenu,
                    modifier = Modifier.offset(y = (-48).dp),
                )
                if (profileObservationFailed) {
                    HomeFailure(stringResource(R.string.pet_profile_observation_failed), onRetryProfile)
                }
                if (inventoryLoadFailed) {
                    HomeFailure(stringResource(R.string.pet_inventory_failed), onRetryProfile)
                }
                HomeCompanionScene(
                    profile = profile,
                    friendId = friendId,
                    accessoryId = accessoryId,
                    outfitId = outfitId,
                    backgroundId = backgroundId,
                    inventoryLoaded = inventoryLoaded,
                    inventoryLoadFailed = inventoryLoadFailed,
                    avatarSize = avatarSize,
                    modifier =
                        Modifier
                            .fillMaxWidth(if (wide) 0.56f else 1f)
                            .heightIn(min = sceneHeight),
                )
                HomeConversationAction(
                    talkText = talkText,
                    connectionText = connectionText,
                    enabled = connectionAvailable && interactionAllowed,
                    interactionAllowed = interactionAllowed,
                    onPetClick = onPetClick,
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
    outfitId: String?,
    backgroundId: String?,
    inventoryLoaded: Boolean,
    inventoryLoadFailed: Boolean,
    avatarSize: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
) {
    Box(modifier.widthIn(max = 920.dp), contentAlignment = Alignment.Center) {
        Column(
            Modifier.align(Alignment.TopCenter).offset(y = (-48).dp).zIndex(1f),
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
                    outfitId = outfitId,
                    backgroundId = backgroundId,
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
        HomeConnectionStatus(
            connectionText = connectionText,
            interactionAllowed = interactionAllowed,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun HomeConnectionStatus(
    connectionText: Int,
    interactionAllowed: Boolean,
    modifier: Modifier = Modifier,
) {
    Layout(
        content = {
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
        },
        modifier = modifier,
    ) { measurables, constraints ->
        val connectionPlaceable = measurables[0].measure(constraints)
        val noticePlaceable = if (measurables.size > 1) measurables[1].measure(constraints) else null

        val width =
            if (constraints.hasBoundedWidth) {
                constraints.maxWidth
            } else {
                maxOf(connectionPlaceable.width, noticePlaceable?.width ?: 0)
            }
        val height = connectionPlaceable.height

        layout(width, height) {
            val connectionX = (width - connectionPlaceable.width) / 2
            connectionPlaceable.placeRelative(connectionX, 0)

            if (noticePlaceable != null) {
                val noticeX = (width - noticePlaceable.width) / 2
                val noticeY = connectionPlaceable.height + 12.dp.roundToPx()
                noticePlaceable.placeRelative(noticeX, noticeY)
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
    modifier: Modifier = Modifier,
) {
    val menuDescription = stringResource(R.string.pet_open_menu)
    val fontScale = LocalDensity.current.fontScale
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val wide = this.maxWidth / fontScale >= 1180.dp
        val brand: @Composable () -> Unit = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                MobiMonNavigationButton(
                    icon = painterResource(R.drawable.pet_menu_icon),
                    description = menuDescription,
                    onClick = onOpenMenu,
                    visualSize = 76.dp,
                    iconSize = 28.dp,
                )
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
                    }
                }
            }
        }
        val status: @Composable () -> Unit = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(32.dp),
            ) {
                Box(Modifier.heightIn(min = 76.dp), contentAlignment = Alignment.Center) {
                    MobiMonPointSummary(pointBalance, failed = pointLoadFailed)
                }
                HomeParkingStatus(snapshot)
            }
        }
        if (wide) {
            Box(Modifier.fillMaxWidth().heightIn(min = 112.dp)) {
                Box(Modifier.align(Alignment.CenterStart)) { brand() }
                Box(Modifier.align(Alignment.CenterEnd)) { status() }
            }
        } else {
            FlowRow(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                brand()
                status()
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
