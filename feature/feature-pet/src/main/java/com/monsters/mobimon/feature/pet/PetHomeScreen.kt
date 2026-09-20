package com.monsters.mobimon.feature.pet

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monsters.mobimon.core.domain.PetProfile
import com.monsters.mobimon.core.domain.VehicleSnapshot
import com.monsters.mobimon.core.ui.FallingParticlesEffect
import com.monsters.mobimon.core.ui.LocalMobiMonMotionEnabled
import com.monsters.mobimon.core.ui.MobiMonButton
import com.monsters.mobimon.core.ui.MobiMonColors
import com.monsters.mobimon.core.ui.MobiMonMessage
import com.monsters.mobimon.core.ui.MobiMonNavigationButton
import com.monsters.mobimon.core.ui.MobiMonPointSummary
import com.monsters.mobimon.core.ui.ParticleType
import com.monsters.mobimon.core.ui.PetAvatar
import com.monsters.mobimon.core.ui.companionBackgroundRes
import com.monsters.mobimon.core.ui.R as CoreUiR

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
    backgroundTimeOfDay: String? = snapshot.timeOfDay,
) {
    var bubbleTrigger by remember { mutableIntStateOf(0) }
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val windowHeight = maxHeight
            val fontScale = LocalDensity.current.fontScale
            val scale = minOf(maxWidth.value / 2560f, maxHeight.value / 1268f)
            val textShadow =
                if (companionBackgroundRes(backgroundTimeOfDay) == CoreUiR.drawable.pet_home_background_night) {
                    null
                } else {
                    with(LocalDensity.current) {
                        Shadow(MobiMonColors.background, Offset(0f, 2.dp.toPx()), 4.dp.toPx())
                    }
                }
            val bubbleLeft = (maxWidth - (2560 * scale).dp) / 2 + (1518 * scale).dp
            val referenceLayout = maxWidth / fontScale >= 1200.dp && maxHeight / fontScale >= 700.dp
            HomeBackground(backgroundTimeOfDay, backgroundId)
            val companion: @Composable (Modifier) -> Unit = { companionModifier ->
                HomeCompanion(
                    profile,
                    friendId,
                    accessoryId,
                    outfitId,
                    backgroundId,
                    inventoryLoaded,
                    inventoryLoadFailed,
                    companionModifier,
                    onClick = { bubbleTrigger++ },
                )
            }
            val header: @Composable (Modifier, Float) -> Unit = { headerModifier, headerScale ->
                HomeHeader(snapshot, pointBalance, pointLoadFailed, onOpenMenu, headerModifier, headerScale, textShadow)
            }
            val notices: @Composable () -> Unit = {
                if (profileObservationFailed) {
                    HomeFailure(
                        stringResource(R.string.pet_profile_observation_failed),
                        onRetryProfile,
                    )
                }
                if (inventoryLoadFailed) HomeFailure(stringResource(R.string.pet_inventory_failed), onRetryProfile)
            }
            val action: @Composable (Modifier, Float) -> Unit = { actionModifier, actionScale ->
                HomeConversationAction(connectionAvailable, interactionAllowed, onPetClick, actionModifier, actionScale)
            }
            if (referenceLayout) {
                // Coordinates exclude the SVG's 76-unit top and 96-unit bottom system bars.
                // Artwork scales uniformly; native text and controls retain their minimum sizes.
                Column(
                    Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).heightIn(min = windowHeight),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(Modifier.fillMaxWidth().height((1068 * scale).dp)) {
                        header(Modifier.padding(horizontal = (72 * scale).dp).offset(y = (56 * scale).dp), scale)
                        HomeGreeting(
                            Modifier.align(Alignment.TopCenter).offset(y = (194 * scale).dp),
                            scale,
                            textShadow,
                        )
                        AmbientTextHeader(
                            backgroundTimeOfDay,
                            Modifier.align(Alignment.TopCenter).offset(y = (385 * scale).dp),
                        )
                        companion(
                            Modifier.align(Alignment.TopCenter).offset(y = (466 * scale).dp).size((520 * scale).dp),
                        )
                        if (friendId != null) {
                            HomeSpeechBubble(
                                Modifier.align(Alignment.TopStart).offset(
                                    x = bubbleLeft,
                                    y =
                                        (
                                            543.7f *
                                                scale
                                        ).dp,
                                ),
                                scale,
                                triggerKey = bubbleTrigger,
                            )
                        }
                    }
                    action(Modifier, scale)
                    Column(
                        Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        notices()
                    }
                }
            } else {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .verticalScroll(
                            rememberScrollState(),
                        ).heightIn(min = maxHeight)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    header(Modifier, 0.75f)
                    notices()
                    HomeGreeting(Modifier, 0.65f, textShadow)
                    AmbientTextHeader(backgroundTimeOfDay)
                    companion(Modifier.size(240.dp))
                    if (friendId != null) HomeSpeechBubble(scale = 0.75f, triggerKey = bubbleTrigger)
                    action(Modifier, 0.75f)
                }
            }
        }
    }
}

@Composable
private fun HomeBackground(
    timeOfDay: String?,
    backgroundId: String?,
) {
    Crossfade(
        targetState = companionBackgroundRes(timeOfDay),
        animationSpec = tween(durationMillis = if (LocalMobiMonMotionEnabled.current) 1000 else 0),
        modifier = Modifier.fillMaxSize(),
        label = "pet_home_background_crossfade",
    ) { targetRes ->
        val tintOpacity =
            when (targetRes) {
                CoreUiR.drawable.pet_home_background_morning -> 0.08f
                CoreUiR.drawable.pet_home_background_day -> 0.12f
                CoreUiR.drawable.pet_home_background_afternoon -> 0.10f
                CoreUiR.drawable.pet_home_background_sunset -> 0.06f
                else -> 0.04f
            }
        Image(
            painterResource(targetRes),
            null,
            Modifier.fillMaxSize().drawWithCache {
                // Keep the horizon clear and crossfade the glass tint with its artwork.
                val glass =
                    Brush.verticalGradient(
                        0f to MobiMonColors.background.copy(alpha = tintOpacity),
                        0.5f to MobiMonColors.background.copy(alpha = tintOpacity * 0.25f),
                        1f to MobiMonColors.background.copy(alpha = tintOpacity * 0.6f),
                    )
                onDrawWithContent {
                    drawContent()
                    drawRect(glass)
                }
            },
            contentScale = ContentScale.Crop,
        )
    }
    if (backgroundId != null) {
        val particleType =
            when {
                backgroundId.contains("snow") -> ParticleType.SNOW
                backgroundId.contains("petal") || backgroundId.contains("flower") -> ParticleType.PETAL
                else -> ParticleType.STAR
            }
        FallingParticlesEffect(
            particleType = particleType,
            modifier = Modifier.fillMaxSize().testTag("home-background-particles"),
        )
    }
}

@Composable
private fun HomeGreeting(
    modifier: Modifier = Modifier,
    scale: Float = 1f,
    textShadow: Shadow? = null,
) {
    Column(
        modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement =
            Arrangement.spacedBy(
                (
                    3 *
                        scale
                ).dp,
            ),
    ) {
        Text(
            stringResource(R.string.pet_home_greeting),
            style =
                MaterialTheme.typography.displayLarge.copy(
                    fontSize = (64.8f * scale).sp,
                    shadow = textShadow,
                    lineHeight =
                        (
                            86.4f *
                                scale
                        ).sp,
                ),
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            stringResource(R.string.pet_home_subtitle),
            style =
                MaterialTheme.typography.bodyLarge.copy(
                    fontSize = (32.4f * scale).coerceAtLeast(24f).sp,
                    shadow = textShadow,
                    letterSpacing = (0.5f * scale).sp,
                    lineHeight =
                        (
                            46 *
                                scale
                        ).coerceAtLeast(34f).sp,
                ),
            color = MobiMonColors.muted,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun HomeCompanion(
    profile: PetProfile,
    friendId: String?,
    accessoryId: String?,
    outfitId: String?,
    backgroundId: String?,
    inventoryLoaded: Boolean,
    inventoryLoadFailed: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(modifier, contentAlignment = Alignment.Center) {
        when {
            friendId != null ->
                PetAvatar(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onClick,
                            ),
                    appearanceKey = profile.appearance.name,
                    friendId = friendId,
                    accessoryId = accessoryId,
                    outfitId = outfitId,
                    backgroundId = backgroundId,
                )
            inventoryLoadFailed -> Unit
            !inventoryLoaded ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(Modifier.padding(24.dp))
                    Text(stringResource(R.string.pet_inventory_loading))
                }
            else -> Text(stringResource(R.string.pet_no_friend), textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun HomeConversationAction(
    connectionAvailable: Boolean,
    interactionAllowed: Boolean,
    onPetClick: () -> Unit,
    modifier: Modifier = Modifier,
    scale: Float = 1f,
) {
    Column(
        modifier.widthIn(max = 1320.dp).fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        MobiMonButton(
            onClick = onPetClick,
            enabled = connectionAvailable && interactionAllowed,
            modifier =
                Modifier
                    .widthIn(
                        min = (532.8f * scale).dp,
                    ).heightIn(min = (100.8f * scale).dp)
                    .testTag("home-conversation-action"),
        ) {
            Icon(painterResource(R.drawable.pet_chat_icon), null, Modifier.size((33.3f * scale).dp))
            Spacer(Modifier.width((24 * scale).dp))
            Text(
                stringResource(if (connectionAvailable) R.string.pet_talk_action else R.string.pet_talk_unavailable),
                modifier = Modifier.offset(y = (-3 * scale).dp),
                style =
                    MaterialTheme.typography.titleLarge.copy(
                        fontSize = (34.2f * scale).coerceAtLeast(24f).sp,
                        fontWeight = FontWeight.Bold,
                    ),
            )
        }
        // Reserve notice space so a restriction never moves the companion or action.
        Column(Modifier.fillMaxWidth().heightIn(min = 80.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            if (!connectionAvailable) Text(stringResource(R.string.pet_ai_unavailable), textAlign = TextAlign.Center)
            if (!interactionAllowed) {
                Text(
                    stringResource(R.string.pet_interaction_restricted),
                    textAlign = TextAlign.Center,
                )
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
    scale: Float = 1f,
    textShadow: Shadow? = null,
) {
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val wide = maxWidth / LocalDensity.current.fontScale >= 1100.dp
        val brand: @Composable () -> Unit = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy((34 * scale).dp),
            ) {
                MobiMonNavigationButton(
                    painterResource(R.drawable.pet_menu_icon),
                    stringResource(R.string.pet_open_menu),
                    onOpenMenu,
                    visualSize = (104 * scale).dp,
                    iconSize = (32 * scale).dp,
                    borderWidth = (2 * scale).dp,
                )
                Column(Modifier.offset(x = (-2 * scale).dp, y = (-10 * scale).dp)) {
                    Text(
                        stringResource(R.string.pet_brand),
                        style =
                            MaterialTheme.typography.headlineMedium.copy(
                                fontSize = (46 * scale).sp,
                                shadow = textShadow,
                                lineHeight =
                                    (
                                        66 *
                                            scale
                                    ).sp,
                                fontWeight = FontWeight.Bold,
                            ),
                        modifier = Modifier.semantics { heading() },
                    )
                    Text(
                        stringResource(R.string.pet_brand_tagline),
                        style =
                            MaterialTheme.typography.bodyMedium.copy(
                                shadow = textShadow,
                                fontSize =
                                    (
                                        28 *
                                            scale
                                    ).coerceAtLeast(24f).sp,
                            ),
                        color = MobiMonColors.muted,
                    )
                }
            }
        }
        val status: @Composable () -> Unit = {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy((48 * scale).dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Box(
                    Modifier.heightIn(min = (76 * scale).dp).offset(y = (7 * scale).dp),
                    contentAlignment = Alignment.Center,
                ) {
                    MobiMonPointSummary(
                        pointBalance,
                        failed = pointLoadFailed,
                        textStyle =
                            MaterialTheme.typography.titleLarge.copy(
                                shadow = textShadow,
                                fontSize =
                                    (
                                        36 *
                                            scale
                                    ).coerceAtLeast(24f).sp,
                                fontWeight = FontWeight.Bold,
                            ),
                    )
                }
                HomeParkingStatus(snapshot, scale = scale)
            }
        }
        if (wide) {
            Box(Modifier.fillMaxWidth()) {
                Box(Modifier.align(Alignment.TopStart)) { brand() }
                Box(Modifier.align(Alignment.TopEnd)) { status() }
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
        MobiMonButton(
            onClick = onRetry,
            modifier = Modifier.heightIn(min = 76.dp),
        ) { Text(stringResource(R.string.pet_retry)) }
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
