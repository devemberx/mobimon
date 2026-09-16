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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.monsters.mobimon.core.ui.MobiMonButton
import com.monsters.mobimon.core.ui.MobiMonButtonStyle
import com.monsters.mobimon.core.ui.MobiMonFontFamily
import com.monsters.mobimon.core.ui.MobiMonMessage
import com.monsters.mobimon.core.ui.MobiMonSection
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
    legacyVisible: Boolean = false,
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
    val q01Completed = progress.completions.any { it.type == QuestType.Q01 } || customCompletions.contains("q01")
    val q01RunActive = progress.activeRun != null
    val q01Claimable =
        (q01RunActive && canManageQuest) ||
            (!q01Completed && !q01RunActive && canManageQuest && customCompletions.contains("q01_acknowledged"))

    val q01Status =
        when {
            q01Completed -> QuestItemStatus.COMPLETED
            q01Claimable -> QuestItemStatus.CLAIMABLE
            else -> QuestItemStatus.IN_PROGRESS
        }

    val q02Completed = customCompletions.contains("q02")
    val q02Status = if (q02Completed) QuestItemStatus.COMPLETED else QuestItemStatus.IN_PROGRESS

    val q03Completed = customCompletions.contains("q03")
    val q03Status = if (q03Completed) QuestItemStatus.COMPLETED else QuestItemStatus.IN_PROGRESS

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
            QuestItemUiModel(
                id = "q02",
                type = QuestType.Q02,
                title = stringResource(R.string.quest_q02_name),
                description = stringResource(R.string.quest_q02_short_desc),
                detailLine1 = stringResource(R.string.quest_q02_detail_line1),
                detailLine2 = stringResource(R.string.quest_q02_detail_line2),
                rewardPoints = 30,
                status = q02Status,
                actionType =
                    when (q02Status) {
                        QuestItemStatus.COMPLETED -> QuestActionType.ALREADY_CLAIMED
                        else -> QuestActionType.CHAT
                    },
                completedDate = "2026.09.14",
            ),
            QuestItemUiModel(
                id = "q03",
                type = QuestType.Q03,
                title = stringResource(R.string.quest_q03_name),
                description = stringResource(R.string.quest_q03_short_desc),
                detailLine1 = stringResource(R.string.quest_q03_detail_line1),
                detailLine2 = stringResource(R.string.quest_q03_detail_line2),
                rewardPoints = 20,
                status = q03Status,
                actionType =
                    when (q03Status) {
                        QuestItemStatus.COMPLETED -> QuestActionType.ALREADY_CLAIMED
                        else -> QuestActionType.VIEW_DETAIL
                    },
                completedDate = "2026.09.14",
            ),
        )

    val displayedQuests =
        when (selectedTab) {
            QuestFilterTab.ALL -> quests
            QuestFilterTab.IN_PROGRESS -> quests.filter { it.status != QuestItemStatus.COMPLETED }
            QuestFilterTab.COMPLETED -> quests.filter { it.status == QuestItemStatus.COMPLETED }
        }

    val selectedQuest = selectedQuestId?.let { id -> quests.firstOrNull { it.id == id } }
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
                                if (selectedQuest != null) onSelectQuest(null) else onBack()
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
                                onClaimReward = { onClaimReward(selectedQuest.id) },
                                modifier =
                                    Modifier
                                        .offset(72.dp * scale, 216.dp * scale)
                                        .size(2416.dp * scale, 994.dp * scale),
                            )
                        } else {
                            QuestListContent(
                                quests = displayedQuests,
                                selectedTab = selectedTab,
                                friendId = friendId,
                                appearanceKey = appearanceKey,
                                accessoryId = accessoryId,
                                scale = scale,
                                onSelectTab = onSelectTab,
                                onSelectQuest = onSelectQuest,
                                onClaimReward = onClaimReward,
                                onChat = { onNavigateRoute(VehicleRoute.VEHICLE_INFO) },
                                legacyVisible = legacyVisible,
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
                            if (selectedQuest != null) onSelectQuest(null) else onBack()
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
                            onClaimReward = { onClaimReward(selectedQuest.id) },
                            modifier = Modifier.fillMaxWidth(),
                            isCompact = true,
                        )
                    } else {
                        QuestListContent(
                            quests = displayedQuests,
                            selectedTab = selectedTab,
                            friendId = friendId,
                            appearanceKey = appearanceKey,
                            accessoryId = accessoryId,
                            scale = compactScale,
                            onSelectTab = onSelectTab,
                            onSelectQuest = onSelectQuest,
                            onClaimReward = onClaimReward,
                            onChat = { onNavigateRoute(VehicleRoute.VEHICLE_INFO) },
                            modifier = Modifier.fillMaxWidth(),
                            isCompact = true,
                        )
                    }

                    if (legacyVisible && selectedQuest == null) {
                        LegacyQuestSection(
                            progress = progress,
                            canManageQuest = canManageQuest,
                            onStartQuest = onStartQuest,
                            onCancelQuest = onCancelQuest,
                            onOpenVehicleInfo = onOpenVehicleInfo,
                            isBusy = isBusy,
                            errorMessage = errorMessage,
                        )
                    }
                }
            }

            if (rewardSuccessModal != null) {
                QuestRewardSuccessModal(
                    points = rewardSuccessModal.points,
                    scale = scale,
                    onConfirm = onDismissRewardModal,
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
    selectedTab: QuestFilterTab,
    friendId: String,
    appearanceKey: String,
    accessoryId: String?,
    scale: Float,
    onSelectTab: (QuestFilterTab) -> Unit,
    onSelectQuest: (String) -> Unit,
    onClaimReward: (String) -> Unit,
    onChat: () -> Unit,
    modifier: Modifier = Modifier,
    legacyVisible: Boolean = false,
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
                selectedTab = selectedTab,
                scale = scale,
                isCompact = true,
                onSelectTab = onSelectTab,
                onSelectQuest = onSelectQuest,
                onClaimReward = onClaimReward,
                onChat = onChat,
                legacyVisible = legacyVisible,
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
                selectedTab = selectedTab,
                scale = scale,
                isCompact = false,
                onSelectTab = onSelectTab,
                onSelectQuest = onSelectQuest,
                onClaimReward = onClaimReward,
                onChat = onChat,
                legacyVisible = legacyVisible,
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
                        .heightIn(min = 994.dp * scale),
            )
        }
    }
}

@Composable
private fun QuestRightPanel(
    quests: List<QuestItemUiModel>,
    selectedTab: QuestFilterTab,
    scale: Float,
    isCompact: Boolean,
    onSelectTab: (QuestFilterTab) -> Unit,
    onSelectQuest: (String) -> Unit,
    onClaimReward: (String) -> Unit,
    onChat: () -> Unit,
    modifier: Modifier = Modifier,
    legacyVisible: Boolean = false,
    progress: QuestProgress = QuestProgress(),
    canManageQuest: Boolean = false,
    onStartQuest: (QuestType) -> Unit = {},
    onCancelQuest: () -> Unit = {},
    onOpenVehicleInfo: () -> Unit = {},
    isBusy: Boolean = false,
    errorMessage: String? = null,
) {
    Column(modifier = modifier) {
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

        Spacer(Modifier.height(36.dp * scale))

        if (quests.isEmpty()) {
            if (legacyVisible) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(26.dp * scale),
                ) {
                    QuestEmptyStateCard(
                        selectedTab = selectedTab,
                        scale = scale,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    LegacyQuestSection(
                        progress = progress,
                        canManageQuest = canManageQuest,
                        onStartQuest = onStartQuest,
                        onCancelQuest = onCancelQuest,
                        onOpenVehicleInfo = onOpenVehicleInfo,
                        isBusy = isBusy,
                        errorMessage = errorMessage,
                    )
                }
            } else {
                QuestEmptyStateCard(
                    selectedTab = selectedTab,
                    scale = scale,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(26.dp * scale),
            ) {
                quests.forEach { quest ->
                    QuestCardItem(
                        quest = quest,
                        scale = scale,
                        isCompact = isCompact,
                        onClick = { onSelectQuest(quest.id) },
                        onClaimReward = { onClaimReward(quest.id) },
                        onChat = onChat,
                    )
                }
                if (legacyVisible) {
                    LegacyQuestSection(
                        progress = progress,
                        canManageQuest = canManageQuest,
                        onStartQuest = onStartQuest,
                        onCancelQuest = onCancelQuest,
                        onOpenVehicleInfo = onOpenVehicleInfo,
                        isBusy = isBusy,
                        errorMessage = errorMessage,
                    )
                }
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
),
                    ) {
                        Text(stringResource(R.string.quest_open_vehicle))
                    }
                    MobiMonButton(
                        style = MobiMonButtonStyle.SECONDARY,
                        onClick = onCancelQuest,
                        enabled = canManageQuest && !isBusy,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 76.dp),
                    ) {
                        Text(stringResource(R.string.quest_cancel))
                    }
                }
                else -> {
                    Text(stringResource(R.string.quest_reward), style = MaterialTheme.typography.labelLarge)
                    if (active != null) Text(stringResource(R.string.quest_other_active))
                    MobiMonButton(
                        onClick = { onStartQuest(QuestType.Q01) },
                        enabled = canManageQuest && !isBusy && active == null,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 76.dp),
                    ) {
                        Text(stringResource(R.string.quest_start_q01))
                    }
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
