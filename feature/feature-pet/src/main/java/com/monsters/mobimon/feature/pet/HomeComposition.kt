package com.monsters.mobimon.feature.pet

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monsters.mobimon.core.domain.PetProfile
import com.monsters.mobimon.core.domain.SignalSource
import com.monsters.mobimon.core.domain.VehicleSnapshot
import com.monsters.mobimon.core.ui.MobiMonPointSummary
import com.monsters.mobimon.core.ui.MobiMonSourceBadge
import com.monsters.mobimon.core.ui.PetAvatar

/** Proportional anchors follow P01; controls retain vehicle touch sizes in the available window. */
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
    val scale = minOf(width.value / 2560f, height.value / 1440f)
    val edge = (width * 0.025f).coerceAtLeast(24.dp)
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
        Box(Modifier.fillMaxWidth().height(height)) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = edge).offset(y = height * (64f / 1440f)),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                Row(
                    Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy((36.dp * scale).coerceAtLeast(24.dp)),
                ) {
                    OutlinedIconButton(
                        onClick = onOpenMenu,
                        modifier = Modifier.size((104.dp * scale).coerceAtLeast(76.dp)),
                        shape = RoundedCornerShape(32.dp * scale),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.24f)),
                        colors =
                            androidx.compose.material3.IconButtonDefaults.outlinedIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f),
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                    ) {
                        Icon(
                            painterResource(R.drawable.pet_menu),
                            stringResource(R.string.pet_open_menu),
                            Modifier.size(
                                40.dp * scale,
                            ),
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            stringResource(R.string.pet_brand),
                            modifier = Modifier.semantics { heading() },
                            style =
                                MaterialTheme.typography.headlineMedium.copy(
                                    fontSize = (48f * scale).coerceAtLeast(32f).sp,
                                ),
                        )
                        if (snapshot.source == SignalSource.SIMULATED) MobiMonSourceBadge(simulated = true)
                    }
                }
                HomeParkingStatus(
                    snapshot,
                    Modifier.width((344.dp * scale).coerceAtLeast(208.dp)).heightIn(min = 76.dp * scale),
                )
                Row(
                    Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                        MobiMonPointSummary(pointBalance, failed = pointLoadFailed)
                    }
                    Button(
                        onClick = onOpenAppearance,
                        modifier =
                            Modifier
                                .width(
                                    (296.dp * scale).coerceAtLeast(180.dp),
                                ).heightIn(min = (104.dp * scale).coerceAtLeast(76.dp)),
                        shape = RoundedCornerShape(36.dp * scale),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.24f)),
                    ) {
                        Icon(painterResource(R.drawable.pet_customize), null, Modifier.size(40.dp * scale))
                        Text(stringResource(R.string.pet_customize), Modifier.padding(start = 16.dp))
                    }
                }
            }
            HomeGreeting(
                Modifier.align(Alignment.TopCenter).offset(y = height * (256f / 1440f)).width(width * 0.325f).height(
                    137.dp * scale,
                ),
                scale,
            )
            PetAvatar(
                modifier =
                    Modifier.align(Alignment.TopCenter).offset(y = height * (400f / 1440f)).size(
                        height * (660f / 1440f),
                    ),
                appearanceKey = profile.appearance.name,
                friendId = friendId,
            )
            HomeVehicleCard(
                snapshot,
                onOpenVehicleInfo,
                Modifier
                    .align(
                        Alignment.TopCenter,
                    ).offset(y = height * (1104f / 1440f))
                    .width(width * 0.51875f)
                    .testTag("home-vehicle-summary"),
            )
            val unavailable = stringResource(R.string.pet_ai_unavailable)
            Button(
                onClick = onPetClick,
                enabled = false,
                modifier =
                    Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = height * (1272f / 1440f))
                        .width(
                            (
                                width *
                                    0.2125f
                            ).coerceAtLeast(340.dp),
                        ).heightIn(min = (104.dp * scale).coerceAtLeast(76.dp))
                        .semantics {
                            contentDescription =
                                unavailable
                        },
                colors =
                    ButtonDefaults.buttonColors(
                        disabledContainerColor = MaterialTheme.colorScheme.primary,
                        disabledContentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
            ) {
                Icon(painterResource(R.drawable.pet_chat), null, Modifier.size(32.dp))
                Text(
                    stringResource(R.string.pet_talk_unavailable),
                    Modifier.padding(start = 12.dp),
                    textAlign = TextAlign.Center,
                )
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
private fun HomeGreeting(
    modifier: Modifier,
    scale: Float,
) {
    val color = MaterialTheme.colorScheme.primary
    Box(modifier) {
        Canvas(Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(28.dp * scale)) {
            drawPath(
                Path().apply {
                    moveTo(size.width / 2 - 32.dp.toPx() * scale, 0f)
                    lineTo(size.width / 2, size.height)
                    lineTo(size.width / 2 + 32.dp.toPx() * scale, 0f)
                    close()
                },
                color,
            )
        }
        Surface(
            Modifier.fillMaxWidth().padding(bottom = 25.dp * scale),
            shape = RoundedCornerShape(56.dp * scale),
            color = color,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ) {
            Box(Modifier.height(112.dp * scale), contentAlignment = Alignment.Center) {
                Text(
                    stringResource(R.string.pet_home_greeting),
                    style =
                        MaterialTheme.typography.headlineMedium.copy(
                            fontSize =
                                (
                                    42f *
                                        scale
                                ).coerceAtLeast(28f).sp,
                        ),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
