package com.monsters.mobimon.feature.vehicle

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monsters.mobimon.core.domain.DrivingState
import com.monsters.mobimon.core.domain.SignalQuality
import com.monsters.mobimon.core.domain.SignalSource
import com.monsters.mobimon.core.domain.SignalUnavailableReason
import com.monsters.mobimon.core.domain.VehicleSnapshot
import com.monsters.mobimon.core.domain.VehicleWarning
import com.monsters.mobimon.core.domain.WarningSeverity
import com.monsters.mobimon.core.ui.MobiMonColors
import com.monsters.mobimon.core.ui.MobiMonDimensions
import com.monsters.mobimon.core.ui.MobiMonParkingBadge
import com.monsters.mobimon.core.ui.PetAvatar
import com.monsters.mobimon.core.ui.R as CoreUiR

/** Displays vehicle readings without owning quest or interaction commands. */
@Composable
fun VehicleInfoScreen(
    snapshot: VehicleSnapshot,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onHome: (() -> Unit)? = null,
    friendId: String = "friend:mobi",
    accessoryId: String? = null,
    outfitId: String? = null,
    backgroundId: String? = null,
) {
    val readings = snapshot.toVehicleInfoUiState()
    val mood = readings.condition.mood()
    val title = stringResource(R.string.vehicle_destination_title)

    BoxWithConstraints(
        modifier =
            modifier
                .fillMaxSize()
                .background(VehicleScreenBackground)
                .semantics { paneTitle = title },
    ) {
        val fontScale = LocalDensity.current.fontScale
        val reference = maxWidth >= 1400.dp && maxHeight >= 760.dp && fontScale <= 1.2f
        val scale = if (reference) maxWidth.value / 2560f else 0.75f
        val contentHeight = maxHeight

        if (reference) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Box(Modifier.fillMaxSize().testTag("vehicle-reference")) {
                    VehicleHeader(
                        snapshot = snapshot,
                        onBack = onBack,
                        onHome = onHome,
                        scale = scale,
                        modifier =
                            Modifier
                                .offset(72.dp * scale, 36.dp * scale)
                                .size(2416.dp * scale, 104.dp * scale),
                    )
                    Column(
                        modifier =
                            Modifier
                                .offset(72.dp * scale, 196.dp * scale)
                                .size(2416.dp * scale, contentHeight - 220.dp * scale)
                                .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(28.dp * scale),
                    ) {
                        VehicleStatusBanner(snapshot, mood, friendId)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(28.dp * scale),
                            verticalAlignment = Alignment.Top,
                        ) {
                            CompanionStatusPanel(
                                mood = mood,
                                friendId = friendId,
                                accessoryId = accessoryId,
                                outfitId = outfitId,
                                backgroundId = backgroundId,
                                modifier = Modifier.weight(0.34f),
                            )
                            VehicleCardGrid(
                                snapshot = snapshot,
                                readings = readings,
                                parkingScale = scale,
                                modifier = Modifier.weight(1f),
                            )
                        }
                        if (snapshot.warnings.isNotEmpty()) WarningList(snapshot.warnings)
                    }
                }
            }
        } else {
            val compactScale = (maxWidth.value / 1400f).coerceIn(0.55f, 0.9f)
            val isWide = maxWidth >= 980.dp
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                VehicleHeader(
                    snapshot = snapshot,
                    onBack = onBack,
                    onHome = onHome,
                    scale = compactScale,
                    modifier = Modifier.fillMaxWidth(),
                )
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    VehicleStatusBanner(snapshot, mood, friendId)
                    if (isWide) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(28.dp),
                            verticalAlignment = Alignment.Top,
                        ) {
                            CompanionStatusPanel(
                                mood = mood,
                                friendId = friendId,
                                accessoryId = accessoryId,
                                outfitId = outfitId,
                                backgroundId = backgroundId,
                                modifier = Modifier.weight(0.34f),
                            )
                            VehicleCardGrid(
                                snapshot = snapshot,
                                readings = readings,
                                parkingScale = scale,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    } else {
                        CompanionStatusPanel(
                            mood = mood,
                            friendId = friendId,
                            accessoryId = accessoryId,
                            outfitId = outfitId,
                            backgroundId = backgroundId,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        VehicleCardGrid(
                            snapshot = snapshot,
                            readings = readings,
                            parkingScale = scale,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    if (snapshot.warnings.isNotEmpty()) WarningList(snapshot.warnings)
                }
            }
        }
    }
}

@Composable
internal fun VehicleHeader(
    snapshot: VehicleSnapshot,
    onBack: () -> Unit,
    onHome: (() -> Unit)?,
    scale: Float,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val buttonSize = if (scale >= 0.7f) 104.dp * scale else MobiMonDimensions.touchTarget
        val iconSize = if (scale >= 0.7f) 40.dp * scale else 24.dp
        IconButton(
            onClick = onBack,
            modifier =
                Modifier
                    .size(buttonSize)
                    .background(VehiclePanelBackground, CircleShape)
                    .border(1.dp, MobiMonColors.border, CircleShape)
                    .testTag("vehicle-header-back-button"),
        ) {
            Icon(
                painter = painterResource(CoreUiR.drawable.mobimon_icon_back),
                contentDescription = stringResource(CoreUiR.string.mobimon_back),
                tint = MobiMonColors.text,
                modifier = Modifier.size(iconSize),
            )
        }
        Spacer(Modifier.width(if (scale >= 0.7f) 32.dp * scale else 16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.vehicle_destination_title),
                color = MobiMonColors.text,
                fontSize = if (scale >= 0.7f) (46f * scale).sp else 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.semantics { heading() },
            )
            Text(
                text = stringResource(R.string.vehicle_header_subtitle),
                color = MobiMonColors.muted,
                fontSize = if (scale >= 0.7f) (28f * scale).sp else 14.sp,
            )
        }
    }
}

@Composable
private fun VehicleStatusBanner(
    snapshot: VehicleSnapshot,
    mood: VehicleMood,
    friendId: String,
    modifier: Modifier = Modifier,
) {
    StatusSurface(
        background = mood.bannerBackground,
        border = mood.accent,
        modifier = modifier.fillMaxWidth().testTag("vehicle-status-banner"),
        contentPadding = PaddingValues(horizontal = 28.dp, vertical = 18.dp),
        corner = 20.dp,
    ) {
        if (mood == VehicleMood.ATTENTION && snapshot.batteryPercent != null) {
            BannerText(
                title =
                    stringResource(R.string.vehicle_banner_low_battery_title).replace(
                        "모비",
                        if (friendId ==
                            "friend:luna"
                        ) {
                            "루나"
                        } else {
                            "모비"
                        },
                    ),
                description = stringResource(R.string.vehicle_banner_low_battery_desc),
                accent = mood.accent,
            )
        } else {
            BannerText(
                title =
                    stringResource(mood.bannerTitleRes).replace(
                        "모비",
                        if (friendId ==
                            "friend:luna"
                        ) {
                            "루나"
                        } else {
                            "모비"
                        },
                    ),
                description = stringResource(mood.bannerDescriptionRes),
                accent = mood.accent,
            )
        }
    }
}

@Composable
private fun BannerText(
    title: String,
    description: String,
    accent: Color,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(22.dp),
    ) {
        Text(
            text = title,
            color = accent,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "|",
            color = accent.copy(alpha = 0.55f),
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = description,
            color = MobiMonColors.text,
            style = MaterialTheme.typography.titleLarge,
        )
    }
}

@Composable
private fun CompanionStatusPanel(
    mood: VehicleMood,
    friendId: String,
    accessoryId: String?,
    outfitId: String?,
    backgroundId: String?,
    modifier: Modifier = Modifier,
) {
    StatusSurface(
        background = VehiclePanelBackground,
        border = VehicleBorder,
        modifier = modifier.height(VehiclePanelHeight),
        contentPadding = PaddingValues(horizontal = 28.dp, vertical = 32.dp),
        corner = 24.dp,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(26.dp),
        ) {
            PetAvatar(
                modifier = Modifier.size(272.dp),
                friendId = friendId,
                accessoryId = accessoryId,
                outfitId = outfitId,
                backgroundId = backgroundId,
            )
            StatusPill(
                text = stringResource(mood.badgeRes),
                foreground = mood.accent,
                background = mood.badgeBackground,
                border = mood.accent,
            )
            Text(
                text = stringResource(mood.companionTextRes),
                color = MobiMonColors.text,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun VehicleCardGrid(
    snapshot: VehicleSnapshot,
    readings: VehicleInfoUiState,
    modifier: Modifier = Modifier,
    parkingScale: Float = 1f,
) {
    val minimumCardWidth = 360.dp * LocalDensity.current.fontScale.coerceAtLeast(1f)
    val cards =
        listOf<@Composable (Modifier) -> Unit>(
            { BatteryCard(snapshot, readings.batteryPercent, it) },
            { DrivingCard(snapshot, it, parkingScale) },
            { TireCard(readings, it) },
            { EnvironmentCard(readings, it) },
            { DriverAssistCard(readings, it) },
            { ConnectionCard(snapshot, it) },
        )
    BoxWithConstraints(modifier) {
        val columns =
            when {
                maxWidth >= minimumCardWidth * 3 + 48.dp -> 3
                maxWidth >= minimumCardWidth * 2 + 24.dp -> 2
                else -> 1
            }
        Column(
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            cards.chunked(columns).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    row.forEach { card -> card(Modifier.weight(1f).fillMaxHeight()) }
                }
            }
        }
    }
}

@Composable
private fun BatteryCard(
    snapshot: VehicleSnapshot,
    battery: Int?,
    modifier: Modifier = Modifier,
) {
    val quality = snapshot.batteryQuality ?: snapshot.quality
    val statusText = batteryStatusText(snapshot)
    val warning = battery != null && battery < 20 || quality != SignalQuality.VALID
    MetricCard(
        title = stringResource(R.string.vehicle_battery_card_title),
        value = battery?.let { "$it%" } ?: stringResource(R.string.vehicle_unknown_short),
        supporting = battery?.let { stringResource(R.string.vehicle_battery, it) } ?: statusText,
        modifier = modifier,
        badge = if (battery != null) batteryBadgeText(battery) else null,
        badgeTone = if (warning) VehicleTone.WARNING else VehicleTone.SUCCESS,
    ) {
        BatteryBar(percent = battery ?: 0, tone = if (warning) VehicleTone.WARNING else VehicleTone.SUCCESS)
        if (quality == SignalQuality.STALE) {
            snapshot.batteryAgeMillis?.let {
                Text(
                    text = lastCheckedText(it),
                    color = MobiMonColors.muted,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun DrivingCard(
    snapshot: VehicleSnapshot,
    modifier: Modifier = Modifier,
    parkingScale: Float = 1f,
) {
    val drivingText =
        stringResource(
            when {
                snapshot.quality != SignalQuality.VALID || snapshot.drivingState == DrivingState.UNKNOWN ->
                    CoreUiR.string.mobimon_parking_unconfirmed
                snapshot.drivingState == DrivingState.MOVING -> R.string.vehicle_driving_moving
                else -> CoreUiR.string.mobimon_parking_confirmed
            },
        )
    MetricCard(
        title = stringResource(R.string.vehicle_driving_card_title),
        value = drivingText,
        valueContent = {
            MobiMonParkingBadge(
                status = drivingText,
                scale = parkingScale,
                showParkingIcon =
                    snapshot.quality != SignalQuality.VALID || snapshot.drivingState != DrivingState.MOVING,
            )
        },
        supporting = parkingSupportingText(snapshot),
        modifier = modifier,
        badge =
            stringResource(
                when (snapshot.quality) {
                    SignalQuality.VALID -> R.string.vehicle_quality_valid_short
                    SignalQuality.STALE -> R.string.vehicle_quality_stale_short
                    SignalQuality.UNAVAILABLE -> R.string.vehicle_quality_unavailable_short
                },
            ),
        badgeTone = if (snapshot.quality == SignalQuality.VALID) VehicleTone.SUCCESS else VehicleTone.WARNING,
    ) {
        if (snapshot.quality == SignalQuality.STALE) {
            snapshot.parkingAgeMillis?.let {
                Text(
                    text = lastCheckedText(it),
                    color = MobiMonColors.muted,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun TireCard(
    readings: VehicleInfoUiState,
    modifier: Modifier = Modifier,
) {
    val warning = readings.tireWarning
    MetricCard(
        title = stringResource(R.string.vehicle_tire_card_title),
        value = readings.tireStatus ?: stringResource(R.string.vehicle_unknown_short),
        supporting =
            warning?.description ?: stringResource(
                if (readings.tireStatus == null) R.string.vehicle_tire_unavailable else R.string.vehicle_tire_checked,
            ),
        modifier = modifier,
        badge =
            when {
                warning != null -> stringResource(R.string.vehicle_warning_caution)
                readings.tireStatus != null -> stringResource(R.string.vehicle_checked_badge)
                else -> null
            },
        badgeTone = if (warning == null) VehicleTone.NEUTRAL else VehicleTone.WARNING,
    )
}

@Composable
private fun EnvironmentCard(
    readings: VehicleInfoUiState,
    modifier: Modifier = Modifier,
) {
    val temperature = readings.outsideTemperature?.let { stringResource(R.string.vehicle_temperature_value, it) }
    val rainText =
        when (readings.isRaining) {
            true -> stringResource(R.string.vehicle_raining)
            false -> stringResource(R.string.vehicle_not_raining)
            null -> stringResource(R.string.vehicle_weather_unknown)
        }
    MetricCard(
        title = stringResource(R.string.vehicle_environment_card_title),
        value = temperature ?: stringResource(R.string.vehicle_unknown_short),
        supporting = rainText,
        modifier = modifier,
    )
}

@Composable
private fun DriverAssistCard(
    readings: VehicleInfoUiState,
    modifier: Modifier = Modifier,
) {
    val issue =
        when (readings.assistWarning) {
            DriverAssistWarning.EMERGENCY_BRAKING -> stringResource(R.string.vehicle_emergency_braking)
            DriverAssistWarning.DROWSY -> stringResource(R.string.vehicle_drowsy)
            DriverAssistWarning.DISTRACTED -> stringResource(R.string.vehicle_distracted)
            null -> {
                when {
                    readings.frontDistance != null ->
                        stringResource(
                            R.string.vehicle_front_distance,
                            readings.frontDistance,
                        )
                    readings.assistChecked -> stringResource(R.string.vehicle_assist_no_issue)
                    else -> stringResource(R.string.vehicle_assist_unavailable)
                }
            }
        }
    MetricCard(
        title = stringResource(R.string.vehicle_assist_card_title),
        value = readings.attentionLevel?.let { "$it" } ?: stringResource(R.string.vehicle_unknown_short),
        supporting = issue,
        modifier = modifier,
        badge =
            when {
                readings.assistWarning != null -> stringResource(R.string.vehicle_attention_needed)
                readings.assistChecked -> stringResource(R.string.vehicle_no_warning_badge)
                else -> null
            },
        badgeTone =
            when {
                readings.assistWarning != null -> VehicleTone.WARNING
                readings.assistChecked -> VehicleTone.SUCCESS
                else -> VehicleTone.NEUTRAL
            },
    )
}

@Composable
private fun ConnectionCard(
    snapshot: VehicleSnapshot,
    modifier: Modifier = Modifier,
) {
    MetricCard(
        title = stringResource(R.string.vehicle_connection_card_title),
        value =
            stringResource(
                when (snapshot.quality) {
                    SignalQuality.VALID -> R.string.vehicle_connection_live
                    SignalQuality.STALE -> R.string.vehicle_connection_stale
                    SignalQuality.UNAVAILABLE -> R.string.vehicle_connection_unavailable
                },
            ),
        supporting =
            if (snapshot.source == SignalSource.SIMULATED) {
                stringResource(R.string.vehicle_source_simulated)
            } else {
                stringResource(R.string.vehicle_source_real)
            },
        modifier = modifier,
        badge =
            if (snapshot.source == SignalSource.SIMULATED) {
                stringResource(R.string.vehicle_source_simulated_badge)
            } else {
                stringResource(R.string.vehicle_source_real_badge)
            },
        badgeTone = if (snapshot.quality == SignalQuality.VALID) VehicleTone.SUCCESS else VehicleTone.WARNING,
    )
}

@Composable
private fun WarningList(warnings: List<VehicleWarning>) {
    StatusSurface(
        background = VehiclePanelBackground,
        border = VehicleBorder,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(28.dp),
        corner = 24.dp,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Text(
                text = stringResource(R.string.vehicle_warnings_title),
                color = MobiMonColors.muted,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            warnings.forEach { warning ->
                WarningRow(warning)
            }
        }
    }
}

@Composable
private fun WarningRow(warning: VehicleWarning) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text =
                stringResource(
                    if (warning.quality == SignalQuality.VALID) {
                        R.string.vehicle_warning_current
                    } else {
                        R.string.vehicle_warning_previous
                    },
                    stringResource(
                        when (warning.severity) {
                            WarningSeverity.NOTICE -> R.string.vehicle_warning_notice
                            WarningSeverity.CAUTION -> R.string.vehicle_warning_caution
                            WarningSeverity.CRITICAL -> R.string.vehicle_warning_critical
                        },
                    ),
                    warning.item,
                ),
            color = if (warning.quality == SignalQuality.VALID) WarningAccent else MobiMonColors.muted,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        warning.location?.let {
            Text(text = it, color = MobiMonColors.muted, style = MaterialTheme.typography.bodySmall)
        }
        Text(text = warning.description, color = MobiMonColors.text, style = MaterialTheme.typography.bodyLarge)
        if (warning.quality == SignalQuality.VALID) {
            Text(text = warning.nextAction, color = MobiMonColors.warning, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    supporting: String,
    modifier: Modifier = Modifier,
    badge: String? = null,
    badgeTone: VehicleTone = VehicleTone.NEUTRAL,
    valueContent: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit = {},
) {
    StatusSurface(
        background = VehiclePanelBackground,
        border = VehicleBorder,
        modifier = modifier.heightIn(min = VehicleCardHeight),
        contentPadding = PaddingValues(24.dp),
        corner = 24.dp,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = title,
                    modifier = Modifier.weight(1f),
                    color = MobiMonColors.muted,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                badge?.let {
                    StatusPill(
                        text = it,
                        foreground = badgeTone.foreground,
                        background = badgeTone.background,
                        border = badgeTone.border,
                    )
                }
            }
            if (valueContent != null) {
                valueContent()
            } else {
                Text(
                    text = value,
                    color = MobiMonColors.text,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                )
            }
            Text(
                text = supporting,
                color = MobiMonColors.muted,
                style = MaterialTheme.typography.bodySmall,
            )
            content()
        }
    }
}

@Composable
private fun BatteryBar(
    percent: Int,
    tone: VehicleTone,
) {
    val clamped = percent.coerceIn(0, 100)
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(16.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MobiMonColors.raised),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth(clamped / 100f)
                    .height(16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(tone.foreground),
        )
    }
}

@Composable
private fun StatusPill(
    text: String,
    foreground: Color,
    background: Color,
    border: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = background,
        contentColor = foreground,
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, border),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = foreground,
        )
    }
}

@Composable
private fun StatusSurface(
    background: Color,
    border: Color,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(24.dp),
    corner: androidx.compose.ui.unit.Dp = 24.dp,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        color = background,
        contentColor = MobiMonColors.text,
        shape = RoundedCornerShape(corner),
        border =
            if (border == Color.Transparent) {
                null
            } else {
                BorderStroke(VehicleBorderWidth, border)
            },
    ) {
        Box(Modifier.padding(contentPadding)) {
            content()
        }
    }
}

@Composable
private fun batteryStatusText(snapshot: VehicleSnapshot): String =
    when {
        (snapshot.batteryQuality ?: snapshot.quality) == SignalQuality.STALE -> {
            stringResource(R.string.vehicle_battery_stale)
        }
        snapshot.batteryUnavailableReason == SignalUnavailableReason.UNSUPPORTED -> {
            stringResource(R.string.vehicle_battery_unsupported)
        }
        snapshot.batteryUnavailableReason == SignalUnavailableReason.PERMISSION_DENIED -> {
            stringResource(R.string.vehicle_battery_permission)
        }
        snapshot.batteryUnavailableReason == SignalUnavailableReason.DISCONNECTED -> {
            stringResource(R.string.vehicle_battery_disconnected)
        }
        else -> stringResource(R.string.vehicle_battery_unavailable)
    }

@Composable
private fun batteryBadgeText(battery: Int): String =
    stringResource(
        when {
            battery < 20 -> R.string.vehicle_battery_badge_low
            battery < 50 -> R.string.vehicle_battery_badge_watch
            else -> R.string.vehicle_battery_badge_ok
        },
    )

@Composable
private fun parkingSupportingText(snapshot: VehicleSnapshot): String =
    when {
        snapshot.quality == SignalQuality.VALID -> stringResource(R.string.vehicle_quality_valid)
        snapshot.quality == SignalQuality.STALE -> stringResource(R.string.vehicle_quality_stale)
        snapshot.parkingUnavailableReason != null -> {
            val reason = snapshot.parkingUnavailableReason ?: SignalUnavailableReason.NOT_REPORTED
            stringResource(reason.description())
        }
        else -> stringResource(R.string.vehicle_quality_unavailable)
    }

private fun VehicleCondition.mood(): VehicleMood =
    when (this) {
        VehicleCondition.CHECKED -> VehicleMood.GOOD
        VehicleCondition.PARTIAL -> VehicleMood.PARTIAL
        VehicleCondition.LOW_BATTERY -> VehicleMood.ATTENTION
        VehicleCondition.WARNING -> VehicleMood.WARNING
        VehicleCondition.STALE -> VehicleMood.STALE
        VehicleCondition.UNAVAILABLE -> VehicleMood.UNKNOWN
    }

private enum class VehicleMood(
    val badgeRes: Int,
    val companionTextRes: Int,
    val bannerTitleRes: Int,
    val bannerDescriptionRes: Int,
    val accent: Color,
    val bannerBackground: Color,
    val badgeBackground: Color,
) {
    GOOD(
        R.string.vehicle_mood_good_badge,
        R.string.vehicle_mood_good_companion,
        R.string.vehicle_banner_good_title,
        R.string.vehicle_banner_good_desc,
        SuccessAccent,
        SuccessBackground,
        SuccessBadgeBackground,
    ),
    PARTIAL(
        R.string.vehicle_mood_partial_badge,
        R.string.vehicle_mood_partial_companion,
        R.string.vehicle_banner_partial_title,
        R.string.vehicle_banner_partial_desc,
        MobiMonColors.accent,
        NeutralBackground,
        NeutralBadgeBackground,
    ),
    ATTENTION(
        R.string.vehicle_mood_hungry_badge,
        R.string.vehicle_mood_hungry_companion,
        R.string.vehicle_banner_low_battery_title,
        R.string.vehicle_banner_low_battery_desc,
        WarningAccent,
        WarningBackground,
        WarningBadgeBackground,
    ),
    WARNING(
        R.string.vehicle_mood_warning_badge,
        R.string.vehicle_mood_warning_companion,
        R.string.vehicle_banner_warning_title,
        R.string.vehicle_banner_warning_desc,
        WarningAccent,
        WarningBackground,
        WarningBadgeBackground,
    ),
    STALE(
        R.string.vehicle_mood_stale_badge,
        R.string.vehicle_mood_stale_companion,
        R.string.vehicle_banner_stale_title,
        R.string.vehicle_banner_stale_desc,
        WarningAccent,
        WarningBackground,
        WarningBadgeBackground,
    ),
    UNKNOWN(
        R.string.vehicle_mood_unknown_badge,
        R.string.vehicle_mood_unknown_companion,
        R.string.vehicle_banner_unknown_title,
        R.string.vehicle_banner_unknown_desc,
        MobiMonColors.accent,
        NeutralBackground,
        NeutralBadgeBackground,
    ),
}

private enum class VehicleTone(
    val foreground: Color,
    val background: Color,
    val border: Color,
) {
    SUCCESS(SuccessAccent, SuccessBadgeBackground, SuccessAccent),
    WARNING(WarningAccent, WarningBadgeBackground, WarningAccent),
    NEUTRAL(MobiMonColors.accent, NeutralBadgeBackground, MobiMonColors.border),
}

private val SuccessAccent = Color(0xFF71E5C5)
private val SuccessBackground = Color(0xFF102B28)
private val SuccessBadgeBackground = Color(0xFF123733)
private val WarningAccent = Color(0xFFFBBF24)
private val WarningBackground = Color(0xFF251E14)
private val WarningBadgeBackground = Color(0xFF2E2213)
private val NeutralBackground = Color(0xFF10243A)
private val NeutralBadgeBackground = Color(0xFF142A42)
internal val VehicleScreenBackground = MobiMonColors.background
private val VehiclePanelBackground = Color(0xFF142A42)
private val VehicleBorder = Color(0xFF2A4968)
private val VehicleBorderWidth = 2.dp
private val VehicleCardHeight = 260.dp
private val VehiclePanelHeight = 544.dp

@Composable
private fun lastCheckedText(ageMillis: Long): String {
    val seconds = ageMillis / 1_000
    return when {
        seconds < 60 -> stringResource(R.string.vehicle_last_checked_seconds, seconds)
        seconds < 3_600 -> stringResource(R.string.vehicle_last_checked_minutes, seconds / 60)
        seconds < 86_400 -> stringResource(R.string.vehicle_last_checked_hours, seconds / 3_600)
        else -> stringResource(R.string.vehicle_last_checked_days, seconds / 86_400)
    }
}

private fun SignalUnavailableReason.description(): Int =
    when (this) {
        SignalUnavailableReason.UNSUPPORTED -> R.string.vehicle_parking_unsupported
        SignalUnavailableReason.PERMISSION_DENIED -> R.string.vehicle_parking_permission
        SignalUnavailableReason.DISCONNECTED -> R.string.vehicle_parking_disconnected
        SignalUnavailableReason.NOT_REPORTED, SignalUnavailableReason.INVALID -> R.string.vehicle_parking_unavailable
    }
