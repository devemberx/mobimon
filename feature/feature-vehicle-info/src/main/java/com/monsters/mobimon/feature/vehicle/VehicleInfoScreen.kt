package com.monsters.mobimon.feature.vehicle

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.monsters.mobimon.core.domain.DrivingState
import com.monsters.mobimon.core.domain.SignalQuality
import com.monsters.mobimon.core.domain.SignalSource
import com.monsters.mobimon.core.domain.SignalUnavailableReason
import com.monsters.mobimon.core.domain.VehicleSnapshot
import com.monsters.mobimon.core.domain.VehicleWarning
import com.monsters.mobimon.core.domain.WarningSeverity
import com.monsters.mobimon.core.ui.MobiMonColors
import com.monsters.mobimon.core.ui.MobiMonContentColumn
import com.monsters.mobimon.core.ui.MobiMonSourceBadge
import com.monsters.mobimon.core.ui.PetAvatar

/** Displays vehicle readings without owning quest or interaction commands. */
@Composable
fun VehicleInfoScreen(
    snapshot: VehicleSnapshot,
    modifier: Modifier = Modifier,
) {
    val mood = snapshot.mood()
    MobiMonContentColumn(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            MobiMonSourceBadge(simulated = snapshot.source == SignalSource.SIMULATED)
        }
        VehicleStatusBanner(snapshot, mood)
        BoxWithConstraints {
            if (maxWidth >= 980.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(28.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    CompanionStatusPanel(
                        mood = mood,
                        modifier = Modifier.weight(0.34f),
                    )
                    VehicleCardGrid(
                        snapshot = snapshot,
                        modifier = Modifier.weight(1f),
                    )
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    CompanionStatusPanel(mood = mood, modifier = Modifier.fillMaxWidth())
                    VehicleCardGrid(snapshot = snapshot, modifier = Modifier.fillMaxWidth())
                }
            }
        }
        if (snapshot.warnings.isNotEmpty()) WarningList(snapshot.warnings)
    }
}

@Composable
private fun VehicleStatusBanner(
    snapshot: VehicleSnapshot,
    mood: VehicleMood,
    modifier: Modifier = Modifier,
) {
    StatusSurface(
        background = mood.bannerBackground,
        border = mood.accent,
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 28.dp, vertical = 18.dp),
        corner = 20.dp,
    ) {
        if (mood == VehicleMood.ATTENTION && snapshot.batteryPercent != null) {
            BannerText(
                title = stringResource(R.string.vehicle_banner_low_battery_title),
                description = stringResource(R.string.vehicle_banner_low_battery_desc),
                accent = mood.accent,
            )
        } else {
            BannerText(
                title = stringResource(mood.bannerTitleRes),
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
    modifier: Modifier = Modifier,
) {
    StatusSurface(
        background = MobiMonColors.panel,
        border = Color.Transparent,
        modifier = modifier.heightIn(min = 420.dp),
        contentPadding = PaddingValues(horizontal = 28.dp, vertical = 32.dp),
        corner = 24.dp,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(26.dp),
        ) {
            PetAvatar(modifier = Modifier.size(272.dp))
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
    modifier: Modifier = Modifier,
) {
    val battery = snapshot.validBattery()
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            BatteryCard(snapshot, battery, Modifier.weight(1f))
            DrivingCard(snapshot, Modifier.weight(1f))
            TireCard(snapshot, Modifier.weight(1f))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            EnvironmentCard(snapshot, Modifier.weight(1f))
            DriverAssistCard(snapshot, Modifier.weight(1f))
            ConnectionCard(snapshot, Modifier.weight(1f))
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
) {
    val drivingText =
        stringResource(
            when {
                snapshot.quality != SignalQuality.VALID || snapshot.drivingState == DrivingState.UNKNOWN ->
                    R.string.vehicle_driving_unknown
                snapshot.drivingState == DrivingState.MOVING -> R.string.vehicle_driving_moving
                else -> R.string.vehicle_driving_parked
            },
        )
    MetricCard(
        title = stringResource(R.string.vehicle_driving_card_title),
        value = drivingText,
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
    snapshot: VehicleSnapshot,
    modifier: Modifier = Modifier,
) {
    val tireStatus = snapshot.tirePressureStatus
    val warning = snapshot.warnings.firstOrNull { it.item.contains("타이어") || it.item.contains("바퀴") }
    MetricCard(
        title = stringResource(R.string.vehicle_tire_card_title),
        value = tireStatus ?: stringResource(R.string.vehicle_unknown_short),
        supporting = warning?.description ?: stringResource(R.string.vehicle_tire_no_warning),
        modifier = modifier,
        badge =
            warning?.let {
                stringResource(R.string.vehicle_warning_caution)
            } ?: stringResource(R.string.vehicle_no_warning_badge),
        badgeTone = if (warning == null) VehicleTone.SUCCESS else VehicleTone.WARNING,
    )
}

@Composable
private fun EnvironmentCard(
    snapshot: VehicleSnapshot,
    modifier: Modifier = Modifier,
) {
    val temperature = snapshot.outsideTemperature?.let { stringResource(R.string.vehicle_temperature_value, it) }
    val rainText =
        when (snapshot.isRaining) {
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
    snapshot: VehicleSnapshot,
    modifier: Modifier = Modifier,
) {
    val issue =
        when {
            snapshot.isEmergencyBraking == true -> stringResource(R.string.vehicle_emergency_braking)
            snapshot.isDrowsy == true -> stringResource(R.string.vehicle_drowsy)
            snapshot.isDistracted == true -> stringResource(R.string.vehicle_distracted)
            snapshot.distanceToFrontVehicle != null -> {
                val distance = snapshot.distanceToFrontVehicle ?: 0
                stringResource(R.string.vehicle_front_distance, distance)
            }
            else -> stringResource(R.string.vehicle_assist_no_issue)
        }
    MetricCard(
        title = stringResource(R.string.vehicle_assist_card_title),
        value = snapshot.attentionLevel?.let { "$it" } ?: stringResource(R.string.vehicle_unknown_short),
        supporting = issue,
        modifier = modifier,
        badge =
            if (snapshot.isEmergencyBraking == true || snapshot.isDrowsy == true || snapshot.isDistracted == true) {
                stringResource(R.string.vehicle_attention_needed)
            } else {
                stringResource(R.string.vehicle_no_warning_badge)
            },
        badgeTone =
            if (snapshot.isEmergencyBraking == true || snapshot.isDrowsy == true || snapshot.isDistracted == true) {
                VehicleTone.WARNING
            } else {
                VehicleTone.SUCCESS
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
        background = MobiMonColors.panel,
        border = Color.Transparent,
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
    content: @Composable () -> Unit = {},
) {
    StatusSurface(
        background = MobiMonColors.panel,
        border = Color.Transparent,
        modifier = modifier.heightIn(min = 236.dp),
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
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = title,
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
            Text(
                text = value,
                color = MobiMonColors.text,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
            )
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
        border = androidx.compose.foundation.BorderStroke(1.dp, border),
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
                androidx.compose.foundation.BorderStroke(1.dp, border)
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

private fun VehicleSnapshot.validBattery(): Int? =
    batteryPercent?.takeIf {
        (batteryQuality ?: quality) == SignalQuality.VALID && it in 0..100
    }

private fun VehicleSnapshot.mood(): VehicleMood =
    when {
        quality == SignalQuality.UNAVAILABLE -> VehicleMood.UNKNOWN
        quality == SignalQuality.STALE -> VehicleMood.STALE
        warnings.any {
            it.quality == SignalQuality.VALID && it.severity != WarningSeverity.NOTICE
        } -> VehicleMood.WARNING
        validBattery()?.let { it < 20 } == true -> VehicleMood.ATTENTION
        else -> VehicleMood.GOOD
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
