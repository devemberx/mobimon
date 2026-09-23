package com.monsters.mobimon.feature.quest

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.monsters.mobimon.core.ui.PetAvatar
import java.util.Locale
import com.monsters.mobimon.core.ui.MobiMonColors as Colors

@Composable
internal fun QuestDetailContent(
    quest: QuestItemUiModel,
    friendId: String,
    accessoryId: String?,
    outfitId: String?,
    backgroundId: String?,
    scale: Float,
    canClaim: Boolean,
    onBackToList: () -> Unit,
    onExecute: () -> Unit,
    onClaimReward: () -> Unit,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false,
) {
    val isCompleted = quest.status == QuestItemStatus.COMPLETED

    if (isCompact) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(24.dp * scale),
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp * scale))
                        .background(Colors.panel)
                        .padding(24.dp * scale),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(24.dp * scale),
            ) {
                PetAvatar(
                    modifier = Modifier.size(160.dp * scale),
                    friendId = friendId,
                    accessoryId = accessoryId,
                    outfitId = outfitId,
                    backgroundId = backgroundId,
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.quest_companion_quote),
                        style = questTextStyle(36f, scale, bold = true, color = Colors.text),
                    )
                    Spacer(Modifier.height(8.dp * scale))
                    Text(
                        text =
                            stringResource(
                                if (isCompleted) {
                                    R.string.quest_companion_sub_completed
                                } else {
                                    R.string.quest_companion_sub_ready
                                },
                            ),
                        style = questTextStyle(26f, scale, bold = false, color = Colors.muted),
                    )
                }
            }

            QuestDetailCard(
                quest = quest,
                canClaim = canClaim,
                scale = scale,
                onBackToList = onBackToList,
                onExecute = onExecute,
                onClaimReward = onClaimReward,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    } else {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(48.dp * scale),
        ) {
            Column(
                modifier =
                    Modifier
                        .width(824.dp * scale)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(32.dp * scale))
                        .background(Colors.panel)
                        .padding(32.dp * scale),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(40.dp * scale))
                PetAvatar(
                    modifier = Modifier.size(520.dp * scale),
                    friendId = friendId,
                    accessoryId = accessoryId,
                    outfitId = outfitId,
                    backgroundId = backgroundId,
                )
                Spacer(Modifier.height(32.dp * scale))
                Text(
                    text = stringResource(R.string.quest_companion_quote),
                    style = questTextStyle(42f, scale, bold = true, color = Colors.text),
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(16.dp * scale))
                Text(
                    text =
                        stringResource(
                            if (isCompleted) {
                                R.string.quest_companion_sub_completed
                            } else {
                                R.string.quest_companion_sub_ready
                            },
                        ),
                    style = questTextStyle(30f, scale, bold = false, color = Colors.muted),
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(24.dp * scale))
            }

            QuestDetailCard(
                quest = quest,
                canClaim = canClaim,
                scale = scale,
                onBackToList = onBackToList,
                onExecute = onExecute,
                onClaimReward = onClaimReward,
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxHeight(),
            )
        }
    }
}

@Composable
internal fun QuestDetailCard(
    quest: QuestItemUiModel,
    scale: Float,
    canClaim: Boolean,
    onBackToList: () -> Unit,
    onExecute: () -> Unit,
    onClaimReward: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isCompleted = quest.status == QuestItemStatus.COMPLETED
    val isClaimable = quest.status == QuestItemStatus.CLAIMABLE

    Column(
        modifier =
            modifier
                .clip(RoundedCornerShape(32.dp * scale))
                .background(Colors.panel)
                .padding(64.dp * scale)
                .testTag("quest-detail-card"),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier
                        .width(236.dp * scale)
                        .height(60.dp * scale)
                        .clip(RoundedCornerShape(16.dp * scale))
                        .background(Colors.raised),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = quest.scheduleFullText,
                    style = questTextStyle(26f, scale, bold = false, color = Colors.accent),
                )
            }

            Row(
                modifier =
                    Modifier
                        .clip(RoundedCornerShape(20.dp * scale))
                        .background(Colors.raised)
                        .border(1.dp * scale, Colors.border, RoundedCornerShape(20.dp * scale))
                        .clickable(onClick = onBackToList)
                        .padding(horizontal = 20.dp * scale, vertical = 10.dp * scale)
                        .testTag("quest-detail-back-button"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp * scale),
            ) {
                Image(
                    painter = painterResource(R.drawable.quest_icon_back),
                    contentDescription = stringResource(R.string.quest_back_to_list),
                    modifier = Modifier.size(20.dp * scale),
                    colorFilter = ColorFilter.tint(Colors.text),
                )
                Text(
                    text = stringResource(R.string.quest_back_to_list),
                    style = questTextStyle(24f, scale, bold = false, color = Colors.text),
                )
            }
        }

        Spacer(Modifier.height(36.dp * scale))

        Text(
            text = quest.title,
            style = questTextStyle(60f, scale, bold = true, color = Colors.text),
        )

        Spacer(Modifier.height(32.dp * scale))

        Text(
            text = quest.detailLine1,
            style = questTextStyle(36f, scale, bold = false, color = Colors.muted),
        )
        Text(
            text = quest.detailLine2,
            style = questTextStyle(36f, scale, bold = false, color = Colors.muted),
        )

        Spacer(Modifier.height(44.dp * scale))

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(2.dp * scale)
                    .background(Colors.raised),
        )

        Spacer(Modifier.height(44.dp * scale))

        QuestProgressDetailSection(
            detail = quest.progressDetail,
            scale = scale,
        )

        if (quest.showVehicleStep) {
            if (quest.progressDetail != null && quest.progressDetail !is QuestProgressDetail.TireCheck) {
                Spacer(Modifier.height(24.dp * scale))
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp * scale),
            ) {
                if (isClaimable || isCompleted) {
                    Image(
                        painter = painterResource(R.drawable.quest_icon_check),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp * scale),
                        colorFilter = ColorFilter.tint(Colors.success),
                    )
                    Text(
                        text = stringResource(R.string.quest_detail_vehicle_done),
                        style = questTextStyle(38f, scale, bold = true, color = Colors.text),
                    )
                } else {
                    Text(
                        text = stringResource(R.string.quest_detail_vehicle_step),
                        style = questTextStyle(38f, scale, bold = true, color = Colors.text),
                    )
                }
            }
        }

        Spacer(Modifier.height(44.dp * scale))

        if (isCompleted) {
            Text(
                text = stringResource(R.string.quest_detail_received_note, quest.rewardPoints),
                style = questTextStyle(30f, scale, bold = false, color = Color(0xFF8496AC)),
            )
        } else {
            Text(
                text = stringResource(R.string.quest_detail_reward_label, quest.rewardPoints),
                style = questTextStyle(38f, scale, bold = true, color = Colors.accent),
            )
            Spacer(Modifier.height(16.dp * scale))
            Text(
                text = stringResource(R.string.quest_detail_notice),
                style = questTextStyle(30f, scale, bold = false, color = Colors.muted),
            )
        }

        Spacer(Modifier.height(48.dp * scale))

        when {
            isCompleted -> {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(112.dp * scale)
                            .clip(RoundedCornerShape(24.dp * scale))
                            .background(Color(0xFF33465B))
                            .clickable(enabled = false) {}
                            .testTag("quest-btn-detail-completed"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Image(
                        painter = painterResource(R.drawable.quest_icon_check),
                        contentDescription = null,
                        modifier = Modifier.size(30.dp * scale),
                        colorFilter = ColorFilter.tint(Colors.muted),
                    )
                    Spacer(Modifier.width(16.dp * scale))
                    Text(
                        text = stringResource(R.string.quest_action_already_claimed),
                        style = questTextStyle(38f, scale, bold = true, color = Colors.muted),
                    )
                }
            }

            isClaimable -> {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(112.dp * scale)
                            .clip(RoundedCornerShape(24.dp * scale))
                            .background(Colors.button)
                            .clickable(enabled = canClaim, onClick = onClaimReward)
                            .testTag("quest-btn-detail-claim"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Image(
                        painter = painterResource(R.drawable.quest_icon_gift),
                        contentDescription = null,
                        modifier = Modifier.size(34.dp * scale),
                        colorFilter = ColorFilter.tint(Colors.onButton),
                    )
                    Spacer(Modifier.width(16.dp * scale))
                    Text(
                        text = stringResource(R.string.quest_action_claim),
                        style = questTextStyle(38f, scale, bold = true, color = Colors.onButton),
                    )
                }
            }

            quest.showExecuteButton -> {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(112.dp * scale)
                            .clip(RoundedCornerShape(24.dp * scale))
                            .background(Colors.panel)
                            .border(2.dp * scale, Colors.border, RoundedCornerShape(24.dp * scale))
                            .clickable(onClick = onExecute)
                            .testTag("quest-btn-detail-execute"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Image(
                        painter = painterResource(R.drawable.quest_icon_send),
                        contentDescription = null,
                        modifier = Modifier.size(34.dp * scale),
                        colorFilter = ColorFilter.tint(Colors.text),
                    )
                    Spacer(Modifier.width(16.dp * scale))
                    Text(
                        text = stringResource(R.string.quest_action_execute),
                        style = questTextStyle(38f, scale, bold = true, color = Colors.text),
                    )
                }
            }
        }
    }
}

@Composable
private fun QuestMetricItem(
    label: String,
    value: String,
    scale: Float,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .clip(RoundedCornerShape(16.dp * scale))
                .background(Colors.raised)
                .padding(horizontal = 24.dp * scale, vertical = 14.dp * scale),
        contentAlignment = Alignment.CenterStart,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp * scale),
        ) {
            Text(
                text = label,
                style = questTextStyle(28f, scale, bold = false, color = Colors.muted),
            )
            Text(
                text = value,
                style = questTextStyle(32f, scale, bold = true, color = Colors.accent),
            )
        }
    }
}

@Composable
private fun QuestProgressDetailSection(
    detail: QuestProgressDetail?,
    scale: Float,
) {
    if (detail == null) return

    when (detail) {
        is QuestProgressDetail.Seatbelt -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp * scale),
            ) {
                QuestMetricItem(
                    label = stringResource(R.string.quest_detail_metric_current_distance),
                    value = String.format(Locale.getDefault(), "%.1f km", detail.currentDistanceKm),
                    scale = scale,
                )
                QuestMetricItem(
                    label = stringResource(R.string.quest_detail_metric_remaining_distance),
                    value = String.format(Locale.getDefault(), "%.1f km", detail.remainingDistanceKm),
                    scale = scale,
                )
            }
        }

        is QuestProgressDetail.SafeDrive -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp * scale),
            ) {
                QuestMetricItem(
                    label = stringResource(R.string.quest_detail_metric_safe_score),
                    value = stringResource(R.string.quest_detail_score_format, detail.safeScore),
                    scale = scale,
                )
                QuestMetricItem(
                    label = stringResource(R.string.quest_detail_metric_current_distance),
                    value = String.format(Locale.getDefault(), "%.1f km", detail.currentDistanceKm),
                    scale = scale,
                )
                QuestMetricItem(
                    label = stringResource(R.string.quest_detail_metric_remaining_distance),
                    value = String.format(Locale.getDefault(), "%.1f km", detail.remainingDistanceKm),
                    scale = scale,
                )
            }
        }

        is QuestProgressDetail.TotalDistance -> {
            QuestMetricItem(
                label = stringResource(R.string.quest_detail_metric_total_distance),
                value = String.format(Locale.getDefault(), "%.1f km", detail.totalDistanceKm),
                scale = scale,
            )
        }

        is QuestProgressDetail.CleanDrive -> {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp * scale)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp * scale),
                ) {
                    QuestMetricItem(
                        label = stringResource(R.string.quest_detail_metric_hard_accel),
                        value = stringResource(R.string.quest_detail_count_format, detail.hardAccelCount),
                        scale = scale,
                    )
                    QuestMetricItem(
                        label = stringResource(R.string.quest_detail_metric_hard_brake),
                        value = stringResource(R.string.quest_detail_count_format, detail.hardBrakeCount),
                        scale = scale,
                    )
                    QuestMetricItem(
                        label = stringResource(R.string.quest_detail_metric_overspeed),
                        value = stringResource(R.string.quest_detail_count_format, detail.overspeedCount),
                        scale = scale,
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp * scale),
                ) {
                    QuestMetricItem(
                        label = stringResource(R.string.quest_detail_metric_current_distance),
                        value = String.format(Locale.getDefault(), "%.1f km", detail.currentDistanceKm),
                        scale = scale,
                    )
                    QuestMetricItem(
                        label = stringResource(R.string.quest_detail_metric_remaining_distance),
                        value = String.format(Locale.getDefault(), "%.1f km", detail.remainingDistanceKm),
                        scale = scale,
                    )
                }
            }
        }

        QuestProgressDetail.FirstDrive -> {
            Text(
                text = stringResource(R.string.quest_detail_msg_first_drive),
                style = questTextStyle(34f, scale, bold = true, color = Colors.text),
            )
        }

        is QuestProgressDetail.FocusDrive -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp * scale),
            ) {
                QuestMetricItem(
                    label = stringResource(R.string.quest_detail_metric_current_distance),
                    value = String.format(Locale.getDefault(), "%.1f km", detail.currentDistanceKm),
                    scale = scale,
                )
                QuestMetricItem(
                    label = stringResource(R.string.quest_detail_metric_remaining_distance),
                    value = String.format(Locale.getDefault(), "%.1f km", detail.remainingDistanceKm),
                    scale = scale,
                )
                QuestMetricItem(
                    label = stringResource(R.string.quest_detail_metric_distraction_level),
                    value =
                        detail.distractionLevel?.let { stringResource(R.string.quest_detail_percent_format, it) }
                            ?: stringResource(R.string.quest_detail_unknown),
                    scale = scale,
                )
            }
        }

        is QuestProgressDetail.LaneKeep -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp * scale),
            ) {
                QuestMetricItem(
                    label = stringResource(R.string.quest_detail_metric_current_distance),
                    value = String.format(Locale.getDefault(), "%.1f km", detail.currentDistanceKm),
                    scale = scale,
                )
                QuestMetricItem(
                    label = stringResource(R.string.quest_detail_metric_remaining_distance),
                    value = String.format(Locale.getDefault(), "%.1f km", detail.remainingDistanceKm),
                    scale = scale,
                )
                QuestMetricItem(
                    label = stringResource(R.string.quest_detail_metric_lane_departure),
                    value = stringResource(R.string.quest_detail_count_format, detail.laneDepartureCount),
                    scale = scale,
                )
            }
        }

        QuestProgressDetail.Maintenance -> {
            Text(
                text = stringResource(R.string.quest_detail_msg_maintenance),
                style = questTextStyle(34f, scale, bold = true, color = Colors.text),
            )
        }

        is QuestProgressDetail.TurnSignal -> {
            QuestMetricItem(
                label = stringResource(R.string.quest_detail_metric_turn_signal),
                value = stringResource(R.string.quest_detail_count_format, detail.turnSignalCount),
                scale = scale,
            )
        }

        is QuestProgressDetail.SafeDriveStreak -> {
            QuestMetricItem(
                label = stringResource(R.string.quest_detail_metric_safe_drive_count),
                value = stringResource(R.string.quest_detail_count_format, detail.safeDriveCount),
                scale = scale,
            )
        }

        is QuestProgressDetail.BatteryCare -> {
            QuestMetricItem(
                label = stringResource(R.string.quest_detail_metric_battery_charge),
                value =
                    detail.batteryPercent?.let { stringResource(R.string.quest_detail_percent_format, it) }
                        ?: stringResource(R.string.quest_detail_unknown),
                scale = scale,
            )
        }

        is QuestProgressDetail.LongTripRest -> {
            val formattedTime =
                detail.drivingMinutes?.let { minutes ->
                    if (minutes >= 60) {
                        stringResource(
                            R.string.quest_detail_time_hours_minutes_format,
                            minutes / 60,
                            minutes % 60,
                        )
                    } else {
                        stringResource(R.string.quest_detail_time_minutes_format, minutes)
                    }
                } ?: stringResource(R.string.quest_detail_unknown)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp * scale),
            ) {
                QuestMetricItem(
                    label = stringResource(R.string.quest_detail_metric_current_distance),
                    value = String.format(Locale.getDefault(), "%.1f km", detail.currentDistanceKm),
                    scale = scale,
                )
                QuestMetricItem(
                    label = stringResource(R.string.quest_detail_metric_remaining_distance),
                    value = String.format(Locale.getDefault(), "%.1f km", detail.remainingDistanceKm),
                    scale = scale,
                )
                QuestMetricItem(
                    label = stringResource(R.string.quest_detail_metric_driving_time),
                    value = formattedTime,
                    scale = scale,
                )
            }
        }

        is QuestProgressDetail.WasherFluid -> {
            QuestMetricItem(
                label = stringResource(R.string.quest_detail_metric_washer_level),
                value =
                    detail.washerFluidLevel?.let { stringResource(R.string.quest_detail_percent_format, it) }
                        ?: stringResource(R.string.quest_detail_unknown),
                scale = scale,
            )
        }

        QuestProgressDetail.TireCheck -> {
            // Keep as is
        }
    }
}
