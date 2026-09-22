package com.monsters.mobimon.feature.quest

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.monsters.mobimon.core.ui.MobiMonMessage
import com.monsters.mobimon.core.ui.PetAvatar
import com.monsters.mobimon.core.ui.PetEmotion
import com.monsters.mobimon.core.ui.MobiMonColors as Colors

@Composable
internal fun QuestRewardSuccessModal(
    points: Long,
    bonusPoints: Long = 0L,
    weatherMultiplier: Float = 1.0f,
    friendId: String,
    accessoryId: String?,
    outfitId: String?,
    backgroundId: String?,
    scale: Float,
    onConfirm: () -> Unit,
) {
    Dialog(
        onDismissRequest = onConfirm,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(Color(0xE6050C16))
                    .clickable(onClick = onConfirm),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier =
                    Modifier
                        .width(1040.dp * scale)
                        .height(880.dp * scale)
                        .clip(RoundedCornerShape(32.dp * scale))
                        .background(Colors.panel)
                        .border(2.dp * scale, Colors.border, RoundedCornerShape(32.dp * scale))
                        .clickable(enabled = false) {}
                        .padding(48.dp * scale)
                        .testTag("quest-reward-success-modal"),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                // Badge
                Box(
                    modifier =
                        Modifier
                            .widthIn(min = 160.dp * scale)
                            .height(44.dp * scale)
                            .clip(RoundedCornerShape(12.dp * scale))
                            .background(Colors.raised)
                            .padding(horizontal = 20.dp * scale),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text =
                            if (bonusPoints > 0) {
                                stringResource(R.string.quest_modal_badge_weather_bonus)
                            } else {
                                stringResource(R.string.quest_modal_badge)
                            },
                        style = questTextStyle(24f, scale, bold = false, color = Colors.accent),
                    )
                }

                Spacer(Modifier.height(24.dp * scale))

                // Joyful character avatar
                PetAvatar(
                    modifier = Modifier.size(280.dp * scale),
                    appearanceKey = "GOLDEN",
                    friendId = friendId,
                    accessoryId = accessoryId,
                    outfitId = outfitId,
                    backgroundId = backgroundId,
                    emotion = PetEmotion.HAPPY,
                )

                Spacer(Modifier.height(24.dp * scale))

                Text(
                    text = stringResource(R.string.quest_modal_title, points),
                    style = questTextStyle(46f, scale, bold = true, color = Colors.text),
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(12.dp * scale))

                Text(
                    text =
                        stringResource(R.string.quest_modal_subtitle).replace(
                            "모비",
                            if (friendId ==
                                "friend:luna"
                            ) {
                                "루나"
                            } else {
                                "모비"
                            },
                        ),
                    style = questTextStyle(30f, scale, bold = false, color = Colors.muted),
                    textAlign = TextAlign.Center,
                )

                if (bonusPoints > 0) {
                    Spacer(Modifier.height(10.dp * scale))
                    Text(
                        text = stringResource(R.string.quest_modal_weather_bonus, bonusPoints),
                        style = questTextStyle(26f, scale, bold = true, color = Colors.accent),
                        textAlign = TextAlign.Center,
                    )
                }

                Spacer(Modifier.height(if (bonusPoints > 0) 18.dp * scale else 28.dp * scale))

                // Reward chip
                Box(
                    modifier =
                        Modifier
                            .width(if (bonusPoints > 0) 640.dp * scale else 520.dp * scale)
                            .height(64.dp * scale)
                            .clip(RoundedCornerShape(16.dp * scale))
                            .background(Color(0xFF0E2034))
                            .border(1.dp * scale, Color(0xFF2A4968), RoundedCornerShape(16.dp * scale)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text =
                            if (bonusPoints > 0) {
                                stringResource(R.string.quest_modal_chip_weather_bonus, points, bonusPoints)
                            } else {
                                stringResource(R.string.quest_modal_chip, points)
                            },
                        style =
                            questTextStyle(
                                baseSp = if (bonusPoints > 0) 28f else 32f,
                                scale = scale,
                                bold = true,
                                color = Colors.success,
                            ),
                    )
                }

                Spacer(Modifier.height(if (bonusPoints > 0) 24.dp * scale else 36.dp * scale))

                // Confirm button (440x96)
                Box(
                    modifier =
                        Modifier
                            .width(440.dp * scale)
                            .height(96.dp * scale)
                            .clip(RoundedCornerShape(20.dp * scale))
                            .background(Colors.button)
                            .clickable(onClick = onConfirm)
                            .testTag("quest-modal-btn-confirm"),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.quest_action_confirm),
                        style = questTextStyle(38f, scale, bold = true, color = Colors.onButton),
                    )
                }
            }
        }
    }
}

@Composable
internal fun QuestHiddenClaimModal(
    quest: HiddenQuestUiModel,
    friendId: String,
    accessoryId: String?,
    outfitId: String?,
    backgroundId: String?,
    scale: Float,
    canClaim: Boolean,
    isBusy: Boolean,
    errorMessage: String?,
    onClaim: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = { if (!isBusy) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(Color(0xE6050C16))
                    .clickable(enabled = !isBusy, onClick = onDismiss),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier =
                    Modifier
                        .width(1040.dp * scale)
                        .heightIn(max = 880.dp * scale)
                        .clip(RoundedCornerShape(32.dp * scale))
                        .background(Colors.panel)
                        .border(2.dp * scale, Color(0xFFF1C40F), RoundedCornerShape(32.dp * scale))
                        .clickable(enabled = false) {}
                        .padding(32.dp * scale)
                        .verticalScroll(rememberScrollState())
                        .testTag("quest-hidden-claim-modal"),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                // Badge
                Box(
                    modifier =
                        Modifier
                            .width(220.dp * scale)
                            .height(44.dp * scale)
                            .clip(RoundedCornerShape(12.dp * scale))
                            .background(Color(0xFF2E2611)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.quest_hidden_badge),
                        style = questTextStyle(24f, scale, bold = true, color = Color(0xFFF1C40F)),
                    )
                }

                Spacer(Modifier.height(20.dp * scale))

                // Character avatar
                PetAvatar(
                    modifier = Modifier.size(260.dp * scale),
                    appearanceKey = "GOLDEN",
                    friendId = friendId,
                    accessoryId = accessoryId,
                    outfitId = outfitId,
                    backgroundId = backgroundId,
                )

                Spacer(Modifier.height(20.dp * scale))

                Text(
                    text = quest.title,
                    style = questTextStyle(42f, scale, bold = true, color = Colors.text),
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(10.dp * scale))

                Text(
                    text = quest.description,
                    style = questTextStyle(28f, scale, bold = false, color = Colors.muted),
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(24.dp * scale))

                if (isBusy) {
                    Text(
                        stringResource(R.string.quest_saving),
                        style = questTextStyle(28f, scale, color = Colors.muted),
                    )
                }
                errorMessage?.let { MobiMonMessage(it, isError = true) }

                // Reward chip
                Box(
                    modifier =
                        Modifier
                            .width(520.dp * scale)
                            .height(64.dp * scale)
                            .clip(RoundedCornerShape(16.dp * scale))
                            .background(Color(0xFF0E2034))
                            .border(1.dp * scale, Color(0xFF2A4968), RoundedCornerShape(16.dp * scale)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.quest_modal_chip, quest.rewardPoints),
                        style = questTextStyle(32f, scale, bold = true, color = Colors.success),
                    )
                }

                Spacer(Modifier.height(30.dp * scale))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp * scale),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Dismiss button
                    Box(
                        modifier =
                            Modifier
                                .width(200.dp * scale)
                                .height(88.dp * scale)
                                .clip(RoundedCornerShape(20.dp * scale))
                                .background(Colors.raised)
                                .clickable(enabled = !isBusy, onClick = onDismiss)
                                .testTag("quest-hidden-btn-dismiss"),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(R.string.quest_hidden_dismiss),
                            style = questTextStyle(32f, scale, bold = false, color = Colors.muted),
                        )
                    }

                    // Claim button
                    Box(
                        modifier =
                            Modifier
                                .width(340.dp * scale)
                                .height(88.dp * scale)
                                .clip(RoundedCornerShape(20.dp * scale))
                                .background(Colors.button)
                                .clickable(enabled = canClaim, onClick = onClaim)
                                .testTag("quest-hidden-btn-claim"),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(R.string.quest_action_claim),
                            style = questTextStyle(36f, scale, bold = true, color = Colors.onButton),
                        )
                    }
                }
            }
        }
    }
}
