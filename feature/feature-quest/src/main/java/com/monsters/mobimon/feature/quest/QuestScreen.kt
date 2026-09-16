package com.monsters.mobimon.feature.quest

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.monsters.mobimon.core.domain.DrivingState
import com.monsters.mobimon.core.domain.QuestProgress
import com.monsters.mobimon.core.domain.QuestType
import com.monsters.mobimon.core.domain.VehicleSnapshot
import com.monsters.mobimon.core.navigation.AppRoute
import com.monsters.mobimon.core.navigation.VehicleRoute
import com.monsters.mobimon.core.ui.MobiMonFontFamily
import com.monsters.mobimon.core.ui.PetAvatar
import com.monsters.mobimon.core.ui.MobiMonColors as Colors

@Composable
fun QuestScreen(
    progress: QuestProgress,
    canManageQuest: Boolean,
    onStartQuest: (QuestType) -> Unit = {},
    onCancelQuest: () -> Unit = {},
    onOpenVehicleInfo: () -> Unit = {},
    modifier: Modifier = Modifier,
    isBusy: Boolean = false,
    errorMessage: String? = null,
    pointBalance: Long? = null,
    pointLoadFailed: Boolean = false,
    snapshot: VehicleSnapshot? = null,
    selectedTab: QuestFilterTab = QuestFilterTab.ALL,
    selectedQuestId: String? = null,
    rewardSuccessModal: RewardSuccessModalState? = null,
    customCompletions: Set<String> = setOf("q03"),
    friendId: String = "friend:mobi",
    appearanceKey: String = "GOLDEN",
    accessoryId: String? = null,
    onSelectTab: (QuestFilterTab) -> Unit = {},
    onSelectQuest: (String?) -> Unit = {},
    onClaimReward: (String) -> Unit = {},
    onDismissRewardModal: () -> Unit = {},
    onNavigateRoute: (AppRoute) -> Unit = {},
    onBack: () -> Unit = {},
    onHome: () -> Unit = {},
) {
    var internalSelectedQuestId by remember { mutableStateOf(selectedQuestId) }
    var internalRewardModal by remember { mutableStateOf(rewardSuccessModal) }
    var internalCompletions by remember { mutableStateOf(customCompletions) }

    val currentSelectedQuestId = internalSelectedQuestId
    val currentRewardModal = internalRewardModal ?: rewardSuccessModal

    val handleSelectQuest: (String?) -> Unit = { questId ->
        internalSelectedQuestId = questId
        onSelectQuest(questId)
    }

    val handleDismissModal: () -> Unit = {
        internalRewardModal = null
        onDismissRewardModal()
    }

    val q01Completed = progress.completions.any { it.type == QuestType.Q01 } || internalCompletions.contains("q01")
    val q01RunActive = progress.activeRun != null
    val q01Claimable =
        (q01RunActive && canManageQuest) ||
            (!q01Completed && !q01RunActive && canManageQuest && internalCompletions.contains("q01_acknowledged"))

    val q01Status =
        when {
            q01Completed -> QuestItemStatus.COMPLETED
            q01Claimable -> QuestItemStatus.CLAIMABLE
            else -> QuestItemStatus.IN_PROGRESS
        }

    val quests =
        listOf(
            QuestItemUiModel(
                id = "q01",
                type = QuestType.Q01,
                title = stringResource(R.string.quest_q01_name),
                description = stringResource(R.string.quest_q01_short_desc),
                detailLine1 = stringResource(R.string.quest_q01_detail_line1),
                detailLine2 = stringResource(R.string.quest_q01_detail_line2),
                rewardPoints = 50,
                status = q01Status,
                actionType =
                    when (q01Status) {
                        QuestItemStatus.CLAIMABLE -> QuestActionType.CLAIM_REWARD
                        QuestItemStatus.COMPLETED -> QuestActionType.ALREADY_CLAIMED
                        QuestItemStatus.IN_PROGRESS -> QuestActionType.VIEW_DETAIL
                    },
                completedDate = "2026.09.14",
                targetRoute = VehicleRoute.VEHICLE_INFO,
            ),
        )

    val handleClaimReward: (String) -> Unit = { questId ->
        internalCompletions = internalCompletions + questId
        val quest = quests.firstOrNull { it.id == questId }
        if (quest != null) {
            internalRewardModal =
                RewardSuccessModalState(
                    points = quest.rewardPoints,
                    questTitle = quest.title,
                )
        }
        onClaimReward(questId)
    }

    val selectedQuest = currentSelectedQuestId?.let { id -> quests.firstOrNull { it.id == id } }
    val title = stringResource(R.string.quest_header_title)
    val isParked = snapshot?.drivingState == DrivingState.PARKED

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(Colors.background)
                .semantics { paneTitle = title },
    ) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val fontScale = LocalDensity.current.fontScale
            val reference = maxWidth >= 1400.dp && maxHeight >= 760.dp && fontScale <= 1f
            val scale = if (reference) minOf(maxWidth.value / 2560f, maxHeight.value / 1268f) else 0.75f

            if (reference) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Box(
                        Modifier
                            .size(2560.dp * scale, 1268.dp * scale)
                            .testTag("quest-reference"),
                    ) {
                        QuestHeader(
                            onBack = {
                                if (selectedQuest != null) handleSelectQuest(null) else onBack()
                            },
                            isParked = isParked,
                            scale = scale,
                            modifier =
                                Modifier
                                    .offset(72.dp * scale, 54.dp * scale)
                                    .width(2416.dp * scale)
                                    .height(106.dp * scale),
                        )

                        if (selectedQuest != null) {
                            QuestDetailContent(
                                quest = selectedQuest,
                                friendId = friendId,
                                appearanceKey = appearanceKey,
                                accessoryId = accessoryId,
                                scale = scale,
                                onExecute = {
                                    if (selectedQuest.id == "q01") {
                                        if (!q01RunActive && !q01Completed) onStartQuest(QuestType.Q01)
                                        onOpenVehicleInfo()
                                    }
                                },
                                onClaimReward = { handleClaimReward(selectedQuest.id) },
                                modifier =
                                    Modifier
                                        .offset(72.dp * scale, 216.dp * scale)
                                        .size(2416.dp * scale, 994.dp * scale),
                            )
                        } else {
                            QuestListContent(
                                quests = quests,
                                friendId = friendId,
                                appearanceKey = appearanceKey,
                                accessoryId = accessoryId,
                                scale = scale,
                                onSelectQuest = handleSelectQuest,
                                onClaimReward = handleClaimReward,
                                onChat = { onNavigateRoute(VehicleRoute.VEHICLE_INFO) },
                                progress = progress,
                                canManageQuest = canManageQuest,
                                onStartQuest = onStartQuest,
                                onCancelQuest = onCancelQuest,
                                onOpenVehicleInfo = onOpenVehicleInfo,
                                isBusy = isBusy,
                                errorMessage = errorMessage,
                                modifier =
                                    Modifier
                                        .offset(72.dp * scale, 216.dp * scale)
                                        .size(2416.dp * scale, 994.dp * scale),
                            )
                        }
                    }
                }
            } else {
                val compactScale = (maxWidth.value / 1400f).coerceIn(0.55f, 0.9f)
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    QuestHeader(
                        onBack = {
                            if (selectedQuest != null) handleSelectQuest(null) else onBack()
                        },
                        isParked = isParked,
                        scale = compactScale,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    if (selectedQuest != null) {
                        QuestDetailContent(
                            quest = selectedQuest,
                            friendId = friendId,
                            appearanceKey = appearanceKey,
                            accessoryId = accessoryId,
                            scale = compactScale,
                            onExecute = {
                                if (selectedQuest.id == "q01") {
                                    if (!q01RunActive && !q01Completed) onStartQuest(QuestType.Q01)
                                    onOpenVehicleInfo()
                                }
                            },
                            onClaimReward = { handleClaimReward(selectedQuest.id) },
                            modifier = Modifier.fillMaxWidth(),
                            isCompact = true,
                        )
                    } else {
                        QuestListContent(
                            quests = quests,
                            friendId = friendId,
                            appearanceKey = appearanceKey,
                            accessoryId = accessoryId,
                            scale = compactScale,
                            onSelectQuest = handleSelectQuest,
                            onClaimReward = handleClaimReward,
                            onChat = { onNavigateRoute(VehicleRoute.VEHICLE_INFO) },
                            progress = progress,
                            canManageQuest = canManageQuest,
                            onStartQuest = onStartQuest,
                            onCancelQuest = onCancelQuest,
                            onOpenVehicleInfo = onOpenVehicleInfo,
                            isBusy = isBusy,
                            errorMessage = errorMessage,
                            modifier = Modifier.fillMaxWidth(),
                            isCompact = true,
                        )
                    }
                }
            }

            if (currentRewardModal != null) {
                QuestRewardSuccessModal(
                    points = currentRewardModal.points,
                    scale = scale,
                    onConfirm = handleDismissModal,
                )
            }
        }
    }
}

@Composable
private fun QuestHeader(
    onBack: () -> Unit,
    isParked: Boolean,
    scale: Float,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier =
                Modifier
                    .size(104.dp * scale)
                    .clip(RoundedCornerShape(24.dp * scale))
                    .background(Colors.panel)
                    .border(2.dp * scale, Colors.border, RoundedCornerShape(24.dp * scale))
                    .clickable(onClick = onBack)
                    .testTag("quest-back-button"),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.quest_icon_back),
                contentDescription = stringResource(R.string.quest_header_title),
                modifier = Modifier.size(36.dp * scale),
                colorFilter = ColorFilter.tint(Colors.text),
            )
        }

        Spacer(Modifier.width(32.dp * scale))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.quest_header_title),
                style = questTextStyle(46f, scale, bold = true, color = Colors.text),
                modifier = Modifier.semantics { heading() },
            )
            Text(
                text = stringResource(R.string.quest_header_subtitle),
                style = questTextStyle(28f, scale, bold = false, color = Colors.muted),
            )
        }

        Row(
            modifier =
                Modifier
                    .widthIn(min = 344.dp * scale)
                    .height(76.dp * scale)
                    .clip(RoundedCornerShape(38.dp * scale))
                    .background(Colors.panel)
                    .border(2.dp * scale, Colors.border, RoundedCornerShape(38.dp * scale))
                    .padding(horizontal = 24.dp * scale),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp * scale),
        ) {
            Image(
                painter = painterResource(R.drawable.quest_parking),
                contentDescription = null,
                modifier = Modifier.size(36.dp * scale),
                colorFilter = ColorFilter.tint(if (isParked) Colors.accent else Colors.warning),
            )
            Text(
                text =
                    stringResource(
                        if (isParked) R.string.quest_parking_confirmed else R.string.quest_parking_unconfirmed,
                    ),
                style =
                    questTextStyle(
                        30f,
                        scale,
                        bold = false,
                        color = if (isParked) Colors.accent else Colors.warning,
                    ),
            )
        }
    }
}

@Composable
private fun QuestListContent(
    quests: List<QuestItemUiModel>,
    friendId: String,
    appearanceKey: String,
    accessoryId: String?,
    scale: Float,
    onSelectQuest: (String) -> Unit,
    onClaimReward: (String) -> Unit,
    onChat: () -> Unit,
    modifier: Modifier = Modifier,
    progress: QuestProgress = QuestProgress(),
    canManageQuest: Boolean = false,
    onStartQuest: (QuestType) -> Unit = {},
    onCancelQuest: () -> Unit = {},
    onOpenVehicleInfo: () -> Unit = {},
    isBusy: Boolean = false,
    errorMessage: String? = null,
    isCompact: Boolean = false,
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
                    appearanceKey = appearanceKey,
                    friendId = friendId,
                    accessoryId = accessoryId,
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
                isCompact = true,
                onSelectQuest = onSelectQuest,
                onClaimReward = onClaimReward,
                onChat = onChat,
                progress = progress,
                canManageQuest = canManageQuest,
                onStartQuest = onStartQuest,
                onCancelQuest = onCancelQuest,
                onOpenVehicleInfo = onOpenVehicleInfo,
                isBusy = isBusy,
                errorMessage = errorMessage,
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
                        .heightIn(min = 994.dp * scale)
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
                    appearanceKey = appearanceKey,
                    friendId = friendId,
                    accessoryId = accessoryId,
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
                isCompact = false,
                onSelectQuest = onSelectQuest,
                onClaimReward = onClaimReward,
                onChat = onChat,
                progress = progress,
                canManageQuest = canManageQuest,
                onStartQuest = onStartQuest,
                onCancelQuest = onCancelQuest,
                onOpenVehicleInfo = onOpenVehicleInfo,
                isBusy = isBusy,
                errorMessage = errorMessage,
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxHeight(),
            )
        }
    }
}

@Composable
private fun LegacyQuestControls(
    progress: QuestProgress,
    canManageQuest: Boolean,
    isBusy: Boolean,
    onStartQuest: (QuestType) -> Unit,
    onCancelQuest: () -> Unit,
    onOpenVehicleInfo: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val completion = progress.completions.firstOrNull { it.type == QuestType.Q01 }
    val active = progress.activeRun

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        when {
            completion != null -> {
                Text(stringResource(R.string.quest_reward_received, completion.awardedXp))
            }

            active?.type == QuestType.Q01 -> {
                LegacyQuestAction(
                    text = stringResource(R.string.quest_open_vehicle),
                    enabled = true,
                    onClick = onOpenVehicleInfo,
                )
                LegacyQuestAction(
                    text = stringResource(R.string.quest_cancel),
                    enabled = canManageQuest && !isBusy,
                    onClick = onCancelQuest,
                )
            }

            else -> {
                LegacyQuestAction(
                    text = stringResource(R.string.quest_start_q01),
                    enabled = canManageQuest && !isBusy && active == null,
                    onClick = { onStartQuest(QuestType.Q01) },
                )
            }
        }
    }
}

@Composable
private fun LegacyQuestAction(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = 76.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(if (enabled) Colors.button else Colors.raised)
                .clickable(enabled = enabled, onClick = onClick)
                .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = questTextStyle(28f, 1f, bold = true, color = Colors.onButton),
        )
    }
}

@Composable
private fun QuestRightPanel(
    quests: List<QuestItemUiModel>,
    scale: Float,
    isCompact: Boolean,
    onSelectQuest: (String) -> Unit,
    onClaimReward: (String) -> Unit,
    onChat: () -> Unit,
    modifier: Modifier = Modifier,
    progress: QuestProgress = QuestProgress(),
    canManageQuest: Boolean = false,
    onStartQuest: (QuestType) -> Unit = {},
    onCancelQuest: () -> Unit = {},
    onOpenVehicleInfo: () -> Unit = {},
    isBusy: Boolean = false,
    errorMessage: String? = null,
) {
    var selectedTab by remember { mutableStateOf(QuestFilterTab.ALL) }

    val displayedQuests =
        when (selectedTab) {
            QuestFilterTab.ALL -> quests
            QuestFilterTab.IN_PROGRESS -> quests.filter { it.status != QuestItemStatus.COMPLETED }
            QuestFilterTab.COMPLETED -> quests.filter { it.status == QuestItemStatus.COMPLETED }
        }

    Column(modifier = modifier) {
        LegacyQuestControls(
            progress = progress,
            canManageQuest = canManageQuest,
            isBusy = isBusy,
            onStartQuest = onStartQuest,
            onCancelQuest = onCancelQuest,
            onOpenVehicleInfo = onOpenVehicleInfo,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(36.dp * scale))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(R.string.quest_section_title),
                style = questTextStyle(48f, scale, bold = true, color = Colors.text),
            )
            Box(
                modifier =
                    Modifier
                        .width(292.dp * scale)
                        .height(60.dp * scale)
                        .clip(RoundedCornerShape(16.dp * scale))
                        .background(Colors.raised),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.quest_badge_example),
                    style = questTextStyle(26f, scale, bold = false, color = Colors.accent),
                )
            }
        }

        Spacer(Modifier.height(44.dp * scale))

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
                onClick = { selectedTab = QuestFilterTab.ALL },
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
                onClick = { selectedTab = QuestFilterTab.IN_PROGRESS },
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
                onClick = { selectedTab = QuestFilterTab.COMPLETED },
                modifier =
                    if (isCompact) {
                        Modifier.weight(1f).testTag("quest-tab-completed")
                    } else {
                        Modifier.testTag("quest-tab-completed")
                    },
            )
        }

        Spacer(Modifier.height(36.dp * scale))

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
                    QuestCardItem(
                        quest = quest,
                        scale = scale,
                        isCompact = isCompact,
                        onClick = { onSelectQuest(quest.id) },
                        onClaimReward = { onClaimReward(quest.id) },
                        onChat = onChat,
                    )
                }
                Spacer(Modifier.height(80.dp * scale))
            }
        }
    }
}

@Composable
private fun QuestFilterTabButton(
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
                .then(if (isCompact) Modifier else Modifier.width(336.dp * scale))
                .height(88.dp * scale)
                .clip(RoundedCornerShape(24.dp * scale))
                .background(bg)
                .then(borderMod)
                .clickable(onClick = onClick)
                .padding(horizontal = if (isCompact) 12.dp * scale else 24.dp * scale),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(32.dp * scale),
            colorFilter = ColorFilter.tint(fg),
        )
        Spacer(Modifier.width(16.dp * scale))
        Text(
            text = title,
            style = questTextStyle(38f, scale, bold = true, color = fg),
        )
    }
}

@Composable
private fun QuestCardItem(
    quest: QuestItemUiModel,
    scale: Float,
    onClick: () -> Unit,
    onClaimReward: () -> Unit,
    onChat: () -> Unit,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false,
) {
    val buttonWidth = if (isCompact) 280.dp * scale else 368.dp * scale
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(220.dp * scale)
                .clip(RoundedCornerShape(24.dp * scale))
                .background(Colors.panel)
                .border(2.dp * scale, Colors.border, RoundedCornerShape(24.dp * scale))
                .clickable(onClick = onClick)
                .padding(horizontal = if (isCompact) 24.dp * scale else 48.dp * scale, vertical = 26.dp * scale)
                .testTag("quest-card-${quest.id}"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = quest.title,
                style = questTextStyle(40f, scale, bold = true, color = Colors.text),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(8.dp * scale))
            Text(
                text = quest.description,
                style = questTextStyle(30f, scale, bold = false, color = Colors.muted),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(16.dp * scale))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(if (isCompact) 12.dp * scale else 24.dp * scale),
            ) {
                // Status pill
                val (statusText, statusColor) =
                    when (quest.status) {
                        QuestItemStatus.CLAIMABLE -> stringResource(R.string.quest_status_claimable) to Colors.success
                        QuestItemStatus.IN_PROGRESS -> stringResource(R.string.quest_status_ongoing) to Colors.accent
                        QuestItemStatus.COMPLETED -> stringResource(R.string.quest_status_completed) to Colors.muted
                    }
                Box(
                    modifier =
                        Modifier
                            .width(if (isCompact) 160.dp * scale else 210.dp * scale)
                            .height(60.dp * scale)
                            .clip(RoundedCornerShape(16.dp * scale))
                            .background(Colors.raised),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = statusText,
                        style = questTextStyle(26f, scale, bold = false, color = statusColor),
                    )
                }

                val rewardFormat =
                    if (quest.status == QuestItemStatus.COMPLETED) {
                        stringResource(
                            R.string.quest_reward_points_received_format,
                            quest.scheduleText,
                            quest.rewardPoints,
                        )
                    } else {
                        stringResource(R.string.quest_reward_points_format, quest.scheduleText, quest.rewardPoints)
                    }
                Text(
                    text = rewardFormat,
                    style = questTextStyle(30f, scale, bold = false, color = statusColor),
                )
            }
        }

        Spacer(Modifier.width(16.dp * scale))

        // Action button on right
        when (quest.actionType) {
            QuestActionType.CLAIM_REWARD -> {
                Row(
                    modifier =
                        Modifier
                            .width(buttonWidth)
                            .height(108.dp * scale)
                            .clip(RoundedCornerShape(20.dp * scale))
                            .background(Colors.button)
                            .clickable(onClick = onClaimReward)
                            .testTag("quest-btn-claim-${quest.id}"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Image(
                        painter = painterResource(R.drawable.quest_icon_gift),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp * scale),
                        colorFilter = ColorFilter.tint(Colors.onButton),
                    )
                    Spacer(Modifier.width(16.dp * scale))
                    Text(
                        text = stringResource(R.string.quest_action_claim),
                        style = questTextStyle(38f, scale, bold = true, color = Colors.onButton),
                    )
                }
            }

            QuestActionType.CHAT -> {
                Row(
                    modifier =
                        Modifier
                            .width(buttonWidth)
                            .height(108.dp * scale)
                            .clip(RoundedCornerShape(20.dp * scale))
                            .background(Colors.panel)
                            .border(2.dp * scale, Colors.border, RoundedCornerShape(20.dp * scale))
                            .clickable(onClick = onChat)
                            .testTag("quest-btn-chat-${quest.id}"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Image(
                        painter = painterResource(R.drawable.quest_icon_chat),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp * scale),
                        colorFilter = ColorFilter.tint(Colors.text),
                    )
                    Spacer(Modifier.width(16.dp * scale))
                    Text(
                        text = stringResource(R.string.quest_action_chat),
                        style = questTextStyle(38f, scale, bold = true, color = Colors.text),
                    )
                }
            }

            QuestActionType.VIEW_DETAIL -> {
                Row(
                    modifier =
                        Modifier
                            .width(buttonWidth)
                            .height(108.dp * scale)
                            .clip(RoundedCornerShape(20.dp * scale))
                            .background(Colors.panel)
                            .border(2.dp * scale, Colors.border, RoundedCornerShape(20.dp * scale))
                            .clickable(onClick = onClick)
                            .testTag("quest-btn-detail-${quest.id}"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Image(
                        painter = painterResource(R.drawable.quest_icon_search),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp * scale),
                        colorFilter = ColorFilter.tint(Colors.text),
                    )
                    Spacer(Modifier.width(16.dp * scale))
                    Text(
                        text = stringResource(R.string.quest_action_view_detail),
                        style = questTextStyle(38f, scale, bold = true, color = Colors.text),
                    )
                }
            }

            QuestActionType.ALREADY_CLAIMED -> {
                Row(
                    modifier =
                        Modifier
                            .width(buttonWidth)
                            .height(108.dp * scale)
                            .clip(RoundedCornerShape(20.dp * scale))
                            .background(Color(0xFF33465B))
                            .clickable(enabled = false) {}
                            .testTag("quest-btn-claimed-${quest.id}"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Image(
                        painter = painterResource(R.drawable.quest_icon_check),
                        contentDescription = null,
                        modifier = Modifier.size(26.dp * scale),
                        colorFilter = ColorFilter.tint(Colors.muted),
                    )
                    Spacer(Modifier.width(16.dp * scale))
                    Text(
                        text = stringResource(R.string.quest_action_received),
                        style = questTextStyle(38f, scale, bold = true, color = Colors.muted),
                    )
                }
            }
        }
    }
}

@Composable
private fun QuestEmptyStateCard(
    selectedTab: QuestFilterTab,
    scale: Float,
    modifier: Modifier = Modifier,
) {
    val isCompletedTab = selectedTab == QuestFilterTab.COMPLETED
    val title =
        if (isCompletedTab) {
            stringResource(R.string.quest_empty_completed_title)
        } else {
            stringResource(R.string.quest_empty_ongoing_title)
        }
    val subtitle =
        if (isCompletedTab) {
            stringResource(R.string.quest_empty_completed_subtitle)
        } else {
            stringResource(R.string.quest_empty_ongoing_subtitle)
        }

    Column(
        modifier =
            modifier
                .clip(RoundedCornerShape(32.dp * scale))
                .background(Colors.panel)
                .padding(48.dp * scale)
                .testTag("quest-empty-state"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.quest_empty_teong),
            contentDescription = null,
            modifier = Modifier.size(360.dp * scale),
        )
        Spacer(Modifier.height(32.dp * scale))
        Text(
            text = title,
            style = questTextStyle(48f, scale, bold = true, color = Colors.text),
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(14.dp * scale))
        Text(
            text = subtitle,
            style = questTextStyle(34f, scale, bold = false, color = Color(0xFF8496AC)),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun QuestDetailContent(
    quest: QuestItemUiModel,
    friendId: String,
    appearanceKey: String,
    accessoryId: String?,
    scale: Float,
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
                    appearanceKey = appearanceKey,
                    friendId = friendId,
                    accessoryId = accessoryId,
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
                scale = scale,
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
                        .heightIn(min = 994.dp * scale)
                        .clip(RoundedCornerShape(32.dp * scale))
                        .background(Colors.panel)
                        .padding(32.dp * scale),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(40.dp * scale))
                PetAvatar(
                    modifier = Modifier.size(520.dp * scale),
                    appearanceKey = appearanceKey,
                    friendId = friendId,
                    accessoryId = accessoryId,
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
                scale = scale,
                onExecute = onExecute,
                onClaimReward = onClaimReward,
                modifier =
                    Modifier
                        .weight(1f)
                        .heightIn(min = 994.dp * scale),
            )
        }
    }
}

@Composable
private fun QuestDetailCard(
    quest: QuestItemUiModel,
    scale: Float,
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

        Spacer(Modifier.height(44.dp * scale))

        if (isCompleted) {
            Text(
                text = stringResource(R.string.quest_detail_completed_date, quest.completedDate ?: "2026.09.14"),
                style = questTextStyle(38f, scale, bold = true, color = Colors.muted),
            )
            Spacer(Modifier.height(16.dp * scale))
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
                            .clickable(onClick = onClaimReward)
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

            else -> {
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
private fun QuestRewardSuccessModal(
    points: Long,
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
                            .width(160.dp * scale)
                            .height(44.dp * scale)
                            .clip(RoundedCornerShape(12.dp * scale))
                            .background(Colors.raised),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.quest_modal_badge),
                        style = questTextStyle(24f, scale, bold = false, color = Colors.accent),
                    )
                }

                Spacer(Modifier.height(24.dp * scale))

                // Joyful character avatar
                PetAvatar(
                    modifier = Modifier.size(280.dp * scale),
                    appearanceKey = "GOLDEN",
                    friendId = "friend:mobi",
                )

                Spacer(Modifier.height(24.dp * scale))

                Text(
                    text = stringResource(R.string.quest_modal_title, points),
                    style = questTextStyle(46f, scale, bold = true, color = Colors.text),
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(12.dp * scale))

                Text(
                    text = stringResource(R.string.quest_modal_subtitle),
                    style = questTextStyle(30f, scale, bold = false, color = Colors.muted),
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(28.dp * scale))

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
                        text = stringResource(R.string.quest_modal_chip, points),
                        style = questTextStyle(32f, scale, bold = true, color = Colors.success),
                    )
                }

                Spacer(Modifier.height(36.dp * scale))

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

private fun questTextStyle(
    baseSp: Float,
    scale: Float,
    bold: Boolean = false,
    color: Color,
): TextStyle =
    TextStyle(
        fontFamily = MobiMonFontFamily,
        fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
        fontSize = (baseSp * scale).sp,
        color = color,
    )
