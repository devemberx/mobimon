package com.monsters.mobimon.feature.quest

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.monsters.mobimon.core.presentation.PointBalanceState
import com.monsters.mobimon.core.ui.MobiMonPointSummary
import com.monsters.mobimon.core.ui.PetAvatar
import com.monsters.mobimon.core.ui.MobiMonColors as Colors

@Composable
internal fun QuestListContent(
    quests: List<QuestItemUiModel>,
    friendId: String,
    accessoryId: String?,
    outfitId: String?,
    backgroundId: String?,
    scale: Float,
    selectedTab: QuestFilterTab,
    onSelectTab: (QuestFilterTab) -> Unit,
    canClaim: Boolean,
    onSelectQuest: (String) -> Unit,
    onClaimReward: (String) -> Unit,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false,
    pointBalance: PointBalanceState = PointBalanceState.Loading,
) {
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
                        text = stringResource(R.string.quest_companion_heading),
                        style = questTextStyle(40f, scale, bold = true, color = Colors.text),
                    )
                    Spacer(Modifier.height(8.dp * scale))
                    Text(
                        text = stringResource(R.string.quest_companion_quote),
                        style = questTextStyle(34f, scale, bold = true, color = Colors.text),
                    )
                    Spacer(Modifier.height(6.dp * scale))
                    Text(
                        text = stringResource(R.string.quest_companion_sub_ready),
                        style = questTextStyle(26f, scale, bold = false, color = Colors.muted),
                    )
                }
            }

            QuestRightPanel(
                quests = quests,
                scale = scale,
                selectedTab = selectedTab,
                onSelectTab = onSelectTab,
                canClaim = canClaim,
                isCompact = true,
                onSelectQuest = onSelectQuest,
                onClaimReward = onClaimReward,
                pointBalance = pointBalance,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    } else {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(56.dp * scale),
        ) {
            Column(
                modifier =
                    Modifier
                        .width(680.dp * scale)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(32.dp * scale))
                        .background(Colors.panel)
                        .padding(32.dp * scale),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.quest_companion_heading),
                    style = questTextStyle(48f, scale, bold = true, color = Colors.text),
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(16.dp * scale))
                PetAvatar(
                    modifier = Modifier.size(460.dp * scale),
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
                Spacer(Modifier.height(12.dp * scale))
                Text(
                    text = stringResource(R.string.quest_companion_sub_ready),
                    style = questTextStyle(30f, scale, bold = false, color = Colors.muted),
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(16.dp * scale))
            }

            QuestRightPanel(
                quests = quests,
                scale = scale,
                selectedTab = selectedTab,
                onSelectTab = onSelectTab,
                canClaim = canClaim,
                isCompact = false,
                onSelectQuest = onSelectQuest,
                onClaimReward = onClaimReward,
                pointBalance = pointBalance,
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxHeight(),
            )
        }
    }
}

@Composable
internal fun QuestRightPanel(
    quests: List<QuestItemUiModel>,
    scale: Float,
    selectedTab: QuestFilterTab,
    onSelectTab: (QuestFilterTab) -> Unit,
    canClaim: Boolean,
    isCompact: Boolean,
    onSelectQuest: (String) -> Unit,
    onClaimReward: (String) -> Unit,
    modifier: Modifier = Modifier,
    pointBalance: PointBalanceState = PointBalanceState.Loading,
) {
    val displayedQuests =
        when (selectedTab) {
            QuestFilterTab.ALL -> quests
            QuestFilterTab.IN_PROGRESS -> quests.filter { it.status != QuestItemStatus.COMPLETED }
            QuestFilterTab.COMPLETED -> quests.filter { it.status == QuestItemStatus.COMPLETED }
        }

    Column(modifier = modifier) {
        MobiMonPointSummary(
            balance = (pointBalance as? PointBalanceState.Ready)?.balance,
            failed = pointBalance == PointBalanceState.Failed,
            textStyle = questTextStyle(34f, scale, bold = false, color = Colors.muted),
        )
        Spacer(Modifier.height(20.dp * scale))

        Text(
            text = stringResource(R.string.quest_section_title),
            style = questTextStyle(48f, scale, bold = true, color = Colors.text),
        )

        Spacer(Modifier.height(24.dp * scale))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(if (isCompact) 12.dp * scale else 24.dp * scale),
        ) {
            QuestFilterTabButton(
                title = stringResource(R.string.quest_tab_all),
                iconRes = R.drawable.quest_icon_grid,
                selected = selectedTab == QuestFilterTab.ALL,
                scale = scale,
                isCompact = isCompact,
                onClick = { onSelectTab(QuestFilterTab.ALL) },
                modifier =
                    if (isCompact) {
                        Modifier.weight(1f).testTag("quest-tab-all")
                    } else {
                        Modifier.testTag("quest-tab-all")
                    },
            )
            QuestFilterTabButton(
                title = stringResource(R.string.quest_tab_ongoing),
                iconRes = R.drawable.quest_icon_clock,
                selected = selectedTab == QuestFilterTab.IN_PROGRESS,
                scale = scale,
                isCompact = isCompact,
                onClick = { onSelectTab(QuestFilterTab.IN_PROGRESS) },
                modifier =
                    if (isCompact) {
                        Modifier.weight(1f).testTag("quest-tab-ongoing")
                    } else {
                        Modifier.testTag("quest-tab-ongoing")
                    },
            )
            QuestFilterTabButton(
                title = stringResource(R.string.quest_tab_completed),
                iconRes = R.drawable.quest_icon_check,
                selected = selectedTab == QuestFilterTab.COMPLETED,
                scale = scale,
                isCompact = isCompact,
                onClick = { onSelectTab(QuestFilterTab.COMPLETED) },
                modifier =
                    if (isCompact) {
                        Modifier.weight(1f).testTag("quest-tab-completed")
                    } else {
                        Modifier.testTag("quest-tab-completed")
                    },
            )
        }

        Spacer(Modifier.height(24.dp * scale))

        if (displayedQuests.isEmpty()) {
            val emptyModifier =
                if (isCompact) {
                    Modifier.fillMaxWidth().heightIn(min = 400.dp * scale)
                } else {
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                }
            QuestEmptyStateCard(
                selectedTab = selectedTab,
                scale = scale,
                modifier = emptyModifier,
            )
        } else {
            val listModifier =
                if (isCompact) {
                    Modifier.fillMaxWidth()
                } else {
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                }
            Column(
                modifier = listModifier,
                verticalArrangement = Arrangement.spacedBy(26.dp * scale),
            ) {
                displayedQuests.forEach { quest ->
                    key(quest.id) {
                        QuestCardItem(
                            quest = quest,
                            canClaim = canClaim,
                            scale = scale,
                            isCompact = isCompact,
                            onClick = { onSelectQuest(quest.id) },
                            onClaimReward = { onClaimReward(quest.id) },
                        )
                    }
                }
                Spacer(Modifier.height(80.dp * scale))
            }
        }
    }
}

@Composable
internal fun QuestFilterTabButton(
    title: String,
    iconRes: Int,
    selected: Boolean,
    scale: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false,
) {
    val bg = if (selected) Colors.accent else Colors.panel
    val fg = if (selected) Colors.onButton else Colors.text
    val borderMod =
        if (selected) {
            Modifier
        } else {
            Modifier.border(
                2.dp * scale,
                Colors.border,
                RoundedCornerShape(
                    24.dp * scale,
                ),
            )
        }

    Row(
        modifier =
            modifier
                .then(if (isCompact) Modifier else Modifier.width(300.dp * scale))
                .height(72.dp * scale)
                .clip(RoundedCornerShape(24.dp * scale))
                .background(bg)
                .then(borderMod)
                .selectable(selected = selected, role = Role.Tab, onClick = onClick)
                .padding(horizontal = if (isCompact) 12.dp * scale else 24.dp * scale),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(28.dp * scale),
            colorFilter = ColorFilter.tint(fg),
        )
        Spacer(Modifier.width(14.dp * scale))
        Text(
            text = title,
            style = questTextStyle(34f, scale, bold = true, color = fg),
        )
    }
}
