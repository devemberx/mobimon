package com.monsters.mobimon.feature.pet

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.monsters.mobimon.core.domain.PetProfile
import com.monsters.mobimon.core.domain.SignalQuality
import com.monsters.mobimon.core.domain.SignalSource
import com.monsters.mobimon.core.domain.VehicleSnapshot
import com.monsters.mobimon.core.ui.MobiMonHomeColors
import com.monsters.mobimon.core.ui.MobiMonPointSummary
import com.monsters.mobimon.core.ui.MobiMonSourceBadge
import com.monsters.mobimon.core.ui.PetAvatar

/** P01 128:569 reserves 76/96 units for OS bars; fit the remaining 2560:1268 content without stretching. */
@Composable
internal fun HomeComposition(
    width: Dp,
    height: Dp,
    profile: PetProfile,
    snapshot: VehicleSnapshot,
    friendId: String,
    pointBalance: Long?,
    pointLoadFailed: Boolean,
    onOpenMenu: () -> Unit,
    onOpenAppearance: () -> Unit,
    onOpenVehicleInfo: () -> Unit,
    onPetClick: () -> Unit,
    supplementaryContent: @Composable () -> Unit,
) {
    val scale = minOf(width.value / 2560f, height.value / 1268f)
    val colors = MaterialTheme.colorScheme
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
        Box(Modifier.fillMaxWidth().height(height).background(Color.Black), contentAlignment = Alignment.Center) {
            Box(Modifier.size(2560.dp * scale, 1268.dp * scale).clipToBounds().testTag("home-composition")) {
                HomeScenery(Modifier.matchParentSize(), insetReference = true)
                HomeReferenceLayout(scale) {
                    HomeAction(
                        104f,
                        104f,
                        32f,
                        scale,
                        onOpenMenu,
                        Modifier.reference(64f, 140f, 104f, 104f, touch = true),
                        description = stringResource(R.string.pet_open_menu),
                    ) {
                        Icon(
                            painterResource(R.drawable.pet_menu),
                            null,
                            Modifier.size(40.dp * scale),
                            tint = colors.onSecondaryContainer,
                        )
                    }
                    Text(
                        stringResource(R.string.pet_brand),
                        Modifier.referenceText(200f, 199.2f, 480f).semantics { heading() },
                        style = homeTextStyle(48f, scale, FontWeight.Bold),
                        color = MobiMonHomeColors.brand,
                    )
                    if (snapshot.source == SignalSource.SIMULATED) {
                        MobiMonSourceBadge(
                            true,
                            Modifier.reference(200f, 220f, 190f, 42f),
                            textStyle = homeTextStyle(20f, scale),
                            contentPadding =
                                PaddingValues(
                                    horizontal =
                                        12.dp * scale,
                                    vertical = 4.dp * scale,
                                ),
                        )
                    }
                    HomeParkingStatus(snapshot, Modifier.reference(1108f, 154f, 344f, 76f), scale)
                    MobiMonPointSummary(
                        pointBalance,
                        pointLoadFailed,
                        Modifier.referenceText(1856f, 204f, 312f),
                        textStyle =
                            homeTextStyle(
                                30f,
                                scale,
                                FontWeight.Bold,
                                TextAlign.End,
                            ).copy(color = MobiMonHomeColors.balance),
                    )
                    HomeAction(
                        296f,
                        104f,
                        36f,
                        scale,
                        onOpenAppearance,
                        Modifier.reference(2200f, 140f, 296f, 104f, touch = true),
                        containerColor = colors.surfaceVariant,
                        borderColor = colors.outlineVariant,
                        borderWidth = 1.dp * scale,
                    ) {
                        Box(Modifier.fillMaxSize()) {
                            Icon(
                                painterResource(R.drawable.pet_customize),
                                null,
                                Modifier.offset(28.dp * scale, 28.dp * scale).size(48.dp * scale),
                                tint = colors.onSurfaceVariant,
                            )
                            Text(
                                stringResource(R.string.pet_customize),
                                Modifier.offset(x = 117.08.dp * scale).paddingFromBaseline(
                                    top =
                                        67.1.dp * scale,
                                ),
                                style = homeTextStyle(34f, scale, FontWeight.Bold),
                                color = MobiMonHomeColors.customization,
                            )
                        }
                    }
                    HomeGreeting(scale, Modifier.reference(864f, 300f, 832f, 137f))
                    Text(
                        stringResource(R.string.pet_home_greeting),
                        Modifier.referenceText(864f, 372.8f, 832f),
                        color = colors.secondaryContainer,
                        style = homeTextStyle(42f, scale, align = TextAlign.Center),
                    )
                    PetAvatar(Modifier.reference(1030f, 430f, 500f, 500f), profile.appearance.name, friendId = friendId)
                    Surface(
                        Modifier.reference(616f, 1050f, 1328f, 112f).testTag("home-vehicle-summary"),
                        shape = RoundedCornerShape(32.dp * scale),
                        color = colors.surface.copy(alpha = 0.82f),
                        border = BorderStroke(2.dp * scale, colors.outline.copy(alpha = 0.24f)),
                    ) {}
                    if ((snapshot.batteryQuality ?: snapshot.quality) == SignalQuality.VALID &&
                        snapshot.batteryPercent?.let { it in 0..100 } == true
                    ) {
                        Icon(
                            painterResource(R.drawable.pet_vehicle_checked),
                            null,
                            Modifier.reference(656f, 1086f, 40f, 40f),
                            tint = colors.secondary,
                        )
                    }
                    Text(
                        homeBatteryText(snapshot),
                        Modifier.referenceText(728f, 1119.8f, 880f),
                        style = homeTextStyle(32f, scale),
                        color = MobiMonHomeColors.vehicleSummary,
                    )
                    Canvas(Modifier.reference(1656f, 1082f, 2f, 48f)) {
                        drawLine(
                            MobiMonHomeColors.divider,
                            Offset.Zero,
                            Offset(0f, size.height),
                            strokeWidth = size.width,
                        )
                    }
                    val details = stringResource(R.string.pet_vehicle_details)
                    Box(
                        Modifier
                            .reference(
                                1656f,
                                1050f,
                                288f,
                                112f,
                                touch = true,
                            ).clickable(role = Role.Button, onClick = onOpenVehicleInfo)
                            .semantics { contentDescription = details },
                    ) {
                        Text(
                            stringResource(R.string.pet_vehicle_status),
                            Modifier.offset(x = 40.dp * scale).paddingFromBaseline(
                                top =
                                    70.dp * scale,
                            ),
                            color = colors.secondary,
                            style = homeTextStyle(30f, scale, FontWeight.Bold),
                        )
                        Icon(
                            painterResource(R.drawable.pet_chevron),
                            null,
                            Modifier
                                .offset(
                                    224.dp * scale,
                                    41.dp * scale,
                                ).size(32.dp * scale),
                            tint = colors.secondary,
                        )
                    }
                    val unavailable = stringResource(R.string.pet_ai_unavailable)
                    HomeAction(
                        544f,
                        104f,
                        52f,
                        scale,
                        onPetClick,
                        Modifier.reference(1008f, 1192f, 544f, 104f, touch = true),
                        enabled = false,
                        description = unavailable,
                        containerColor = colors.onPrimaryContainer,
                        borderColor = Color.Transparent,
                    ) {
                        Box(Modifier.fillMaxSize()) {
                            Icon(
                                painterResource(R.drawable.pet_chat),
                                null,
                                Modifier
                                    .offset(
                                        166.dp * scale,
                                        34.dp * scale,
                                    ).size(40.dp * scale),
                                tint = colors.primaryContainer,
                            )
                            Text(
                                stringResource(R.string.pet_talk_action),
                                Modifier.offset(x = 232.dp * scale).paddingFromBaseline(
                                    top =
                                        67.9.dp * scale,
                                ),
                                color = colors.primaryContainer,
                                style = homeTextStyle(36f, scale, FontWeight.Bold),
                            )
                        }
                    }
                    Text(
                        stringResource(R.string.pet_ai_connection_unavailable),
                        Modifier.referenceText(1008f, 1332f, 544f),
                        style = homeTextStyle(26f, scale, align = TextAlign.Center),
                    )
                }
            }
        }
        Column(
            Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) { supplementaryContent() }
    }
}

@Composable
private fun HomeAction(
    width: Float,
    height: Float,
    radius: Float,
    scale: Float,
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean = true,
    description: String? = null,
    containerColor: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f),
    borderColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.24f),
    borderWidth: Dp = 2.dp * scale,
    content: @Composable () -> Unit,
) {
    Box(
        modifier.clickable(enabled = enabled, role = Role.Button, onClick = onClick).semantics {
            description?.let { contentDescription = it }
            if (!enabled && description != null) stateDescription = description
        },
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            Modifier.size(width.dp * scale, height.dp * scale),
            shape = RoundedCornerShape(radius.dp * scale),
            color = containerColor,
            border = BorderStroke(borderWidth, borderColor),
        ) {
            Box(contentAlignment = Alignment.Center) { content() }
        }
    }
}

@Composable
private fun HomeGreeting(
    scale: Float,
    modifier: Modifier,
) {
    val color = MaterialTheme.colorScheme.primary
    Canvas(modifier) {
        val factor = scale.dp.toPx()
        drawRoundRect(
            color,
            size =
                androidx.compose.ui.geometry
                    .Size(size.width, 112f * factor),
            cornerRadius =
                androidx.compose.ui.geometry
                    .CornerRadius(56f * factor),
        )
        drawPath(
            Path().apply {
                moveTo(384f * factor, 110f * factor)
                lineTo(416f * factor, 137f * factor)
                lineTo(448f * factor, 110f * factor)
                close()
            },
            color,
        )
    }
}
