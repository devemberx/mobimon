package com.monsters.mobimon.feature.quest

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.shape.CircleShape
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
import com.monsters.mobimon.core.domain.DrivingQuestIds
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
    onAcknowledgeVehicle: (String) -> Unit,
    vehicleSnapshot: VehicleSnapshot,
    canAcknowledgeVehicle: Boolean,
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
    satisfiedQuestIds: Set<String> = emptySet(),
    friendId: String = "friend:mobi",
    appearanceKey: String = "GOLDEN",
    accessoryId: String? = null,
    outfitId: String? = null,
    backgroundId: String? = null,
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
    var internalCompletions by remember(customCompletions) { mutableStateOf(customCompletions) }

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

    BackHandler(enabled = currentSelectedQuestId != null) {
        handleSelectQuest(null)
    }

    val q01Completed = progress.completions.any { it.type == QuestType.Q01 } || internalCompletions.contains("q01")
    val q01RunActive = progress.activeRun != null
    val displaySnapshot = snapshot ?: vehicleSnapshot
    val q01Claimable =
        !q01Completed && !q01RunActive && canManageQuest && internalCompletions.contains("q01_acknowledged")

    val q01Status =
        when {
            q01Completed -> QuestItemStatus.COMPLETED
            q01Claimable -> QuestItemStatus.CLAIMABLE
            else -> QuestItemStatus.IN_PROGRESS
        }

    val drivingQuestStatus: (String) -> QuestItemStatus = { questId ->
        when {
            internalCompletions.contains(questId) -> QuestItemStatus.COMPLETED
            satisfiedQuestIds.contains(questId) && canManageQuest -> QuestItemStatus.CLAIMABLE
            else -> QuestItemStatus.IN_PROGRESS
        }
    }

    val drivingQuestAction: (QuestItemStatus) -> QuestActionType = { status ->
        when (status) {
            QuestItemStatus.CLAIMABLE -> QuestActionType.CLAIM_REWARD
            QuestItemStatus.COMPLETED -> QuestActionType.ALREADY_CLAIMED
            QuestItemStatus.IN_PROGRESS -> QuestActionType.VIEW_DETAIL
        }
    }

    val seatbeltStatus = drivingQuestStatus(DrivingQuestIds.SEATBELT)
    val safeDriveStatus = drivingQuestStatus(DrivingQuestIds.SAFE_DRIVE)
    val distance100KmStatus = drivingQuestStatus(DrivingQuestIds.DISTANCE_100KM)
    val cleanDriveStatus = drivingQuestStatus(DrivingQuestIds.CLEAN_DRIVE)
    val firstDriveStatus = drivingQuestStatus(DrivingQuestIds.FIRST_DRIVE)
    val focusDriveStatus = drivingQuestStatus(DrivingQuestIds.FOCUS_DRIVE)
    val laneKeepStatus = drivingQuestStatus(DrivingQuestIds.LANE_KEEP)
    val maintenanceStatus = drivingQuestStatus(DrivingQuestIds.MAINTENANCE)
    val turnSignalStatus = drivingQuestStatus(DrivingQuestIds.TURN_SIGNAL)
    val safe5DaysStatus = drivingQuestStatus(DrivingQuestIds.SAFE_5DAYS)
    val batteryCareStatus = drivingQuestStatus(DrivingQuestIds.BATTERY_CARE)
    val longTripRestStatus = drivingQuestStatus(DrivingQuestIds.LONG_TRIP_REST)
    val washerFluidStatus = drivingQuestStatus(DrivingQuestIds.WASHER_FLUID)
    val tireCheckStatus = drivingQuestStatus(DrivingQuestIds.TIRE_CHECK)

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
                        QuestItemStatus.IN_PROGRESS ->
                            if (q01RunActive) QuestActionType.VIEW_DETAIL else QuestActionType.START
                    },
                completedDate = "2026.09.14",
            ),
            QuestItemUiModel(
                id = DrivingQuestIds.SEATBELT,
                type = null,
                title = stringResource(R.string.quest_seatbelt_name),
                description = stringResource(R.string.quest_seatbelt_short_desc),
                detailLine1 = stringResource(R.string.quest_seatbelt_detail_line1),
                detailLine2 = stringResource(R.string.quest_seatbelt_detail_line2),
                scheduleText = stringResource(R.string.quest_schedule_per_drive_short),
                scheduleFullText = stringResource(R.string.quest_schedule_per_drive_short),
                rewardPoints = 5,
                status = seatbeltStatus,
                actionType = drivingQuestAction(seatbeltStatus),
                targetRoute = VehicleRoute.VEHICLE_INFO,
            ),
            QuestItemUiModel(
                id = DrivingQuestIds.SAFE_DRIVE,
                type = null,
                title = stringResource(R.string.quest_safe_drive_name),
                description = stringResource(R.string.quest_safe_drive_short_desc),
                detailLine1 = stringResource(R.string.quest_safe_drive_detail_line1),
                detailLine2 = stringResource(R.string.quest_safe_drive_detail_line2),
                scheduleText = stringResource(R.string.quest_schedule_per_drive_short),
                scheduleFullText = stringResource(R.string.quest_schedule_per_drive_short),
                rewardPoints = 20,
                status = safeDriveStatus,
                actionType = drivingQuestAction(safeDriveStatus),
                targetRoute = VehicleRoute.VEHICLE_INFO,
            ),
            QuestItemUiModel(
                id = DrivingQuestIds.DISTANCE_100KM,
                type = null,
                title = stringResource(R.string.quest_100km_name),
                description = stringResource(R.string.quest_100km_short_desc),
                detailLine1 = stringResource(R.string.quest_100km_detail_line1),
                detailLine2 = stringResource(R.string.quest_100km_detail_line2),
                scheduleText = stringResource(R.string.quest_schedule_once_short),
                scheduleFullText = stringResource(R.string.quest_schedule_once),
                rewardPoints = 25,
                status = distance100KmStatus,
                actionType = drivingQuestAction(distance100KmStatus),
                targetRoute = VehicleRoute.VEHICLE_INFO,
            ),
            QuestItemUiModel(
                id = DrivingQuestIds.CLEAN_DRIVE,
                type = null,
                title = stringResource(R.string.quest_clean_drive_name),
                description = stringResource(R.string.quest_clean_drive_short_desc),
                detailLine1 = stringResource(R.string.quest_clean_drive_detail_line1),
                detailLine2 = stringResource(R.string.quest_clean_drive_detail_line2),
                scheduleText = stringResource(R.string.quest_schedule_per_drive_short),
                scheduleFullText = stringResource(R.string.quest_schedule_per_drive_short),
                rewardPoints = 15,
                status = cleanDriveStatus,
                actionType = drivingQuestAction(cleanDriveStatus),
                targetRoute = VehicleRoute.VEHICLE_INFO,
            ),
            QuestItemUiModel(
                id = DrivingQuestIds.FIRST_DRIVE,
                type = null,
                title = stringResource(R.string.quest_first_drive_name),
                description = stringResource(R.string.quest_first_drive_short_desc),
                detailLine1 = stringResource(R.string.quest_first_drive_detail_line1),
                detailLine2 = stringResource(R.string.quest_first_drive_detail_line2),
                scheduleText = stringResource(R.string.quest_schedule_daily_short),
                scheduleFullText = stringResource(R.string.quest_schedule_daily_short),
                rewardPoints = 10,
                status = firstDriveStatus,
                actionType = drivingQuestAction(firstDriveStatus),
                targetRoute = VehicleRoute.VEHICLE_INFO,
            ),
            QuestItemUiModel(
                id = DrivingQuestIds.FOCUS_DRIVE,
                type = null,
                title = stringResource(R.string.quest_focus_drive_name),
                description = stringResource(R.string.quest_focus_drive_short_desc),
                detailLine1 = stringResource(R.string.quest_focus_drive_detail_line1),
                detailLine2 = stringResource(R.string.quest_focus_drive_detail_line2),
                scheduleText = stringResource(R.string.quest_schedule_per_drive_short),
                scheduleFullText = stringResource(R.string.quest_schedule_per_drive_short),
                rewardPoints = 10,
                status = focusDriveStatus,
                actionType = drivingQuestAction(focusDriveStatus),
                targetRoute = VehicleRoute.VEHICLE_INFO,
            ),
            QuestItemUiModel(
                id = DrivingQuestIds.LANE_KEEP,
                type = null,
                title = stringResource(R.string.quest_lane_keep_name),
                description = stringResource(R.string.quest_lane_keep_short_desc),
                detailLine1 = stringResource(R.string.quest_lane_keep_detail_line1),
                detailLine2 = stringResource(R.string.quest_lane_keep_detail_line2),
                scheduleText = stringResource(R.string.quest_schedule_per_drive_short),
                scheduleFullText = stringResource(R.string.quest_schedule_per_drive_short),
                rewardPoints = 10,
                status = laneKeepStatus,
                actionType = drivingQuestAction(laneKeepStatus),
                targetRoute = VehicleRoute.VEHICLE_INFO,
            ),
            QuestItemUiModel(
                id = DrivingQuestIds.MAINTENANCE,
                type = null,
                title = stringResource(R.string.quest_maintenance_name),
                description = stringResource(R.string.quest_maintenance_short_desc),
                detailLine1 = stringResource(R.string.quest_maintenance_detail_line1),
                detailLine2 = stringResource(R.string.quest_maintenance_detail_line2),
                scheduleText = stringResource(R.string.quest_schedule_once_short),
                scheduleFullText = stringResource(R.string.quest_schedule_once),
                rewardPoints = 10,
                status = maintenanceStatus,
                actionType = drivingQuestAction(maintenanceStatus),
                targetRoute = VehicleRoute.VEHICLE_INFO,
            ),
            QuestItemUiModel(
                id = DrivingQuestIds.TURN_SIGNAL,
                type = null,
                title = stringResource(R.string.quest_turn_signal_name),
                description = stringResource(R.string.quest_turn_signal_short_desc),
                detailLine1 = stringResource(R.string.quest_turn_signal_detail_line1),
                detailLine2 = stringResource(R.string.quest_turn_signal_detail_line2),
                scheduleText = stringResource(R.string.quest_schedule_per_count_short),
                scheduleFullText = stringResource(R.string.quest_schedule_per_count_short),
                rewardPoints = 1,
                status = turnSignalStatus,
                actionType = drivingQuestAction(turnSignalStatus),
                targetRoute = VehicleRoute.VEHICLE_INFO,
            ),
            QuestItemUiModel(
                id = DrivingQuestIds.SAFE_5DAYS,
                type = null,
                title = stringResource(R.string.quest_5days_safe_name),
                description = stringResource(R.string.quest_5days_safe_short_desc),
                detailLine1 = stringResource(R.string.quest_5days_safe_detail_line1),
                detailLine2 = stringResource(R.string.quest_5days_safe_detail_line2),
                scheduleText = stringResource(R.string.quest_schedule_weekly_short),
                scheduleFullText = stringResource(R.string.quest_schedule_weekly_short),
                rewardPoints = 50,
                status = safe5DaysStatus,
                actionType = drivingQuestAction(safe5DaysStatus),
                targetRoute = VehicleRoute.VEHICLE_INFO,
            ),
            QuestItemUiModel(
                id = DrivingQuestIds.BATTERY_CARE,
                type = null,
                title = stringResource(R.string.quest_battery_care_name),
                description = stringResource(R.string.quest_battery_care_short_desc),
                detailLine1 = stringResource(R.string.quest_battery_care_detail_line1),
                detailLine2 = stringResource(R.string.quest_battery_care_detail_line2),
                scheduleText = stringResource(R.string.quest_schedule_weekly_short),
                scheduleFullText = stringResource(R.string.quest_schedule_weekly_short),
                rewardPoints = 20,
                status = batteryCareStatus,
                actionType = drivingQuestAction(batteryCareStatus),
                targetRoute = VehicleRoute.VEHICLE_INFO,
            ),
            QuestItemUiModel(
                id = DrivingQuestIds.LONG_TRIP_REST,
                type = null,
                title = stringResource(R.string.quest_long_trip_rest_name),
                description = stringResource(R.string.quest_long_trip_rest_short_desc),
                detailLine1 = stringResource(R.string.quest_long_trip_rest_detail_line1),
                detailLine2 = stringResource(R.string.quest_long_trip_rest_detail_line2),
                scheduleText = stringResource(R.string.quest_schedule_per_drive_short),
                scheduleFullText = stringResource(R.string.quest_schedule_per_drive_short),
                rewardPoints = 25,
                status = longTripRestStatus,
                actionType = drivingQuestAction(longTripRestStatus),
                targetRoute = VehicleRoute.VEHICLE_INFO,
            ),
            QuestItemUiModel(
                id = DrivingQuestIds.WASHER_FLUID,
                type = null,
                title = stringResource(R.string.quest_washer_fluid_name),
                description = stringResource(R.string.quest_washer_fluid_short_desc),
                detailLine1 = stringResource(R.string.quest_washer_fluid_detail_line1),
                detailLine2 = stringResource(R.string.quest_washer_fluid_detail_line2),
                scheduleText = stringResource(R.string.quest_schedule_once_short),
                scheduleFullText = stringResource(R.string.quest_schedule_once),
                rewardPoints = 15,
                status = washerFluidStatus,
                actionType = drivingQuestAction(washerFluidStatus),
                targetRoute = VehicleRoute.VEHICLE_INFO,
            ),
            QuestItemUiModel(
                id = DrivingQuestIds.TIRE_CHECK,
                type = null,
                title = stringResource(R.string.quest_tire_check_name),
                description = stringResource(R.string.quest_tire_check_short_desc),
                detailLine1 = stringResource(R.string.quest_tire_check_detail_line1),
                detailLine2 = stringResource(R.string.quest_tire_check_detail_line2),
                scheduleText = stringResource(R.string.quest_schedule_weekly_short),
                scheduleFullText = stringResource(R.string.quest_schedule_weekly_short),
                rewardPoints = 15,
                status = tireCheckStatus,
                actionType = drivingQuestAction(tireCheckStatus),
                targetRoute = VehicleRoute.VEHICLE_INFO,
            ),
        )

    var dismissedHiddenQuestIds by remember { mutableStateOf(emptySet<String>()) }

    val isCostumeEquipped = !accessoryId.isNullOrBlank() || !outfitId.isNullOrBlank()
    val isBackgroundEquipped =
        !backgroundId.isNullOrBlank() && backgroundId != "none" && backgroundId != "background:default"
    val isNewFriendEquipped = !friendId.isNullOrBlank() && friendId != "friend:mobi"

    val hiddenQuests =
        listOf(
            HiddenQuestUiModel(
                id = DrivingQuestIds.HIDDEN_COSTUME,
                title = stringResource(R.string.quest_hidden_costume_name),
                description = stringResource(R.string.quest_hidden_costume_desc),
                rewardPoints = 30L,
                isSatisfied = isCostumeEquipped || satisfiedQuestIds.contains(DrivingQuestIds.HIDDEN_COSTUME),
            ),
            HiddenQuestUiModel(
                id = DrivingQuestIds.HIDDEN_BACKGROUND,
                title = stringResource(R.string.quest_hidden_background_name),
                description = stringResource(R.string.quest_hidden_background_desc),
                rewardPoints = 30L,
                isSatisfied = isBackgroundEquipped || satisfiedQuestIds.contains(DrivingQuestIds.HIDDEN_BACKGROUND),
            ),
            HiddenQuestUiModel(
                id = DrivingQuestIds.HIDDEN_NEW_FRIEND,
                title = stringResource(R.string.quest_hidden_new_friend_name),
                description = stringResource(R.string.quest_hidden_new_friend_desc),
                rewardPoints = 30L,
                isSatisfied = isNewFriendEquipped || satisfiedQuestIds.contains(DrivingQuestIds.HIDDEN_NEW_FRIEND),
            ),
        )

    val activeHiddenQuest =
        hiddenQuests.firstOrNull {
            it.isSatisfied && !internalCompletions.contains(it.id) && !dismissedHiddenQuestIds.contains(it.id)
        }

    val handleClaimReward: (String) -> Unit = { questId ->
        internalCompletions = internalCompletions + questId
        val quest = quests.firstOrNull { it.id == questId }
        val hiddenQuest = hiddenQuests.firstOrNull { it.id == questId }
        val points = quest?.rewardPoints?.toLong() ?: hiddenQuest?.rewardPoints ?: 0L
        val questTitle = quest?.title ?: hiddenQuest?.title ?: ""
        if (points > 0L) {
            internalRewardModal =
                RewardSuccessModalState(
                    points = points,
                    questTitle = questTitle,
                )
        }
        onClaimReward(questId)
    }

    val selectedQuest = currentSelectedQuestId?.let { id -> quests.firstOrNull { it.id == id } }
    val title = stringResource(R.string.quest_header_title)
    val isParked = displaySnapshot.drivingState == DrivingState.PARKED

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
                            isParked = isParked,
                            friendId = friendId,
                            scale = scale,
                            onBackToList =
                                if (selectedQuest != null) {
                                    { handleSelectQuest(null) }
                                } else {
                                    null
                                },
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
                                outfitId = outfitId,
                                backgroundId = backgroundId,
                                scale = scale,
                                onBackToList = { handleSelectQuest(null) },
                                onExecute = {
                                    if (selectedQuest.id == "q01" && !q01RunActive && !q01Completed) {
                                        onStartQuest(QuestType.Q01)
                                        handleSelectQuest(null)
                                    } else {
                                        selectedQuest.targetRoute?.let(onNavigateRoute)
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
                                outfitId = outfitId,
                                backgroundId = backgroundId,
                                scale = scale,
                                onSelectQuest = handleSelectQuest,
                                onClaimReward = handleClaimReward,
                                onChat = {},
                                progress = progress,
                                canManageQuest = canManageQuest,
                                onStartQuest = onStartQuest,
                                onCancelQuest = onCancelQuest,
                                onAcknowledgeVehicle = onAcknowledgeVehicle,
                                vehicleSnapshot = displaySnapshot,
                                canAcknowledgeVehicle = canAcknowledgeVehicle,
                                isBusy = isBusy,
                                errorMessage = errorMessage,
                                pointBalance = pointBalance,
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
                        isParked = isParked,
                        friendId = friendId,
                        scale = compactScale,
                        onBackToList =
                            if (selectedQuest != null) {
                                { handleSelectQuest(null) }
                            } else {
                                null
                            },
                        modifier = Modifier.fillMaxWidth(),
                    )

                    if (selectedQuest != null) {
                        QuestDetailContent(
                            quest = selectedQuest,
                            friendId = friendId,
                            appearanceKey = appearanceKey,
                            accessoryId = accessoryId,
                            outfitId = outfitId,
                            backgroundId = backgroundId,
                            scale = compactScale,
                            onBackToList = { handleSelectQuest(null) },
                            onExecute = {
                                if (selectedQuest.id == "q01" && !q01RunActive && !q01Completed) {
                                    onStartQuest(QuestType.Q01)
                                    handleSelectQuest(null)
                                } else {
                                    selectedQuest.targetRoute?.let(onNavigateRoute)
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
                            outfitId = outfitId,
                            backgroundId = backgroundId,
                            scale = compactScale,
                            onSelectQuest = handleSelectQuest,
                            onClaimReward = handleClaimReward,
                            onChat = {},
                            progress = progress,
                            canManageQuest = canManageQuest,
                            onStartQuest = onStartQuest,
                            onCancelQuest = onCancelQuest,
                            onAcknowledgeVehicle = onAcknowledgeVehicle,
                            vehicleSnapshot = displaySnapshot,
                            canAcknowledgeVehicle = canAcknowledgeVehicle,
                            isBusy = isBusy,
                            errorMessage = errorMessage,
                            pointBalance = pointBalance,
                            modifier = Modifier.fillMaxWidth(),
                            isCompact = true,
                        )
                    }
                }
            }
            if (activeHiddenQuest != null && currentRewardModal == null) {
                QuestHiddenClaimModal(
                    quest = activeHiddenQuest,
                    friendId = friendId,
                    accessoryId = accessoryId,
                    outfitId = outfitId,
                    backgroundId = backgroundId,
                    scale = scale,
                    onClaim = { handleClaimReward(activeHiddenQuest.id) },
                    onDismiss = { dismissedHiddenQuestIds = dismissedHiddenQuestIds + activeHiddenQuest.id },
                )
            }

            if (currentRewardModal != null) {
                QuestRewardSuccessModal(
                    points = currentRewardModal.points,
                    friendId = friendId,
                    accessoryId = accessoryId,
                    outfitId = outfitId,
                    backgroundId = backgroundId,
                    scale = scale,
                    onConfirm = handleDismissModal,
                )
            }
        }
    }
}

@Composable
private fun QuestHeader(
    isParked: Boolean,
    friendId: String,
    scale: Float,
    modifier: Modifier = Modifier,
    onBackToList: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBackToList != null) {
            Box(
                modifier =
                    Modifier
                        .size(52.dp * scale)
                        .clip(CircleShape)
                        .background(Colors.panel)
                        .border(1.5.dp * scale, Colors.border, CircleShape)
                        .clickable(onClick = onBackToList)
                        .testTag("quest-header-back-button"),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.quest_icon_back),
                    contentDescription = stringResource(R.string.quest_back_to_list),
                    modifier = Modifier.size(24.dp * scale),
                    colorFilter = ColorFilter.tint(Colors.text),
                )
            }
            Spacer(Modifier.width(16.dp * scale))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.quest_header_title),
                style = questTextStyle(46f, scale, bold = true, color = Colors.text),
                modifier = Modifier.semantics { heading() },
            )
            Text(
                text =
                    stringResource(R.string.quest_header_subtitle).replace(
                        "모비",
                        if (friendId ==
                            "friend:luna"
                        ) {
                            "루나"
                        } else {
                            "모비"
                        },
                    ),
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
    outfitId: String?,
    backgroundId: String?,
    scale: Float,
    onSelectQuest: (String) -> Unit,
    onClaimReward: (String) -> Unit,
    onChat: () -> Unit,
    modifier: Modifier = Modifier,
    progress: QuestProgress = QuestProgress(),
    canManageQuest: Boolean = false,
    onStartQuest: (QuestType) -> Unit = {},
    onCancelQuest: () -> Unit = {},
    onAcknowledgeVehicle: (String) -> Unit,
    vehicleSnapshot: VehicleSnapshot,
    canAcknowledgeVehicle: Boolean,
    isBusy: Boolean = false,
    errorMessage: String? = null,
    isCompact: Boolean = false,
    pointBalance: Long? = null,
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
                isCompact = true,
                onSelectQuest = onSelectQuest,
                onClaimReward = onClaimReward,
                onChat = onChat,
                progress = progress,
                canManageQuest = canManageQuest,
                onStartQuest = onStartQuest,
                onCancelQuest = onCancelQuest,
                onAcknowledgeVehicle = onAcknowledgeVehicle,
                vehicleSnapshot = vehicleSnapshot,
                canAcknowledgeVehicle = canAcknowledgeVehicle,
                isBusy = isBusy,
                errorMessage = errorMessage,
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
                isCompact = false,
                onSelectQuest = onSelectQuest,
                onClaimReward = onClaimReward,
                onChat = onChat,
                progress = progress,
                canManageQuest = canManageQuest,
                onStartQuest = onStartQuest,
                onCancelQuest = onCancelQuest,
                onAcknowledgeVehicle = onAcknowledgeVehicle,
                vehicleSnapshot = vehicleSnapshot,
                canAcknowledgeVehicle = canAcknowledgeVehicle,
                isBusy = isBusy,
                errorMessage = errorMessage,
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
private fun LegacyQuestControls(
    progress: QuestProgress,
    canManageQuest: Boolean,
    isBusy: Boolean,
    onCancelQuest: () -> Unit,
    pointBalance: Long? = null,
    onAcknowledgeVehicle: (String) -> Unit,
    vehicleSnapshot: VehicleSnapshot,
    canAcknowledgeVehicle: Boolean,
    errorMessage: String?,
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
                val rewardText =
                    if (pointBalance != null) {
                        stringResource(R.string.quest_point_reward_received, pointBalance)
                    } else {
                        stringResource(R.string.quest_reward_received, completion.awardedXp)
                    }
                Text(rewardText)
            }

            active?.type == QuestType.Q01 -> {
                QuestVehicleCard(
                    snapshot = vehicleSnapshot,
                    questActive = true,
                    questCompleted = false,
                    canAcknowledge = canAcknowledgeVehicle,
                    onAcknowledge = onAcknowledgeVehicle,
                    isBusy = isBusy,
                    errorMessage = errorMessage,
                )
                LegacyQuestAction(
                    text = stringResource(R.string.quest_cancel),
                    enabled = canManageQuest && !isBusy,
                    onClick = onCancelQuest,
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
    onAcknowledgeVehicle: (String) -> Unit,
    vehicleSnapshot: VehicleSnapshot,
    canAcknowledgeVehicle: Boolean,
    isBusy: Boolean = false,
    errorMessage: String? = null,
    pointBalance: Long? = null,
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
            onCancelQuest = onCancelQuest,
            pointBalance = pointBalance,
            onAcknowledgeVehicle = onAcknowledgeVehicle,
            vehicleSnapshot = vehicleSnapshot,
            canAcknowledgeVehicle = canAcknowledgeVehicle,
            errorMessage = errorMessage,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(24.dp * scale))

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
                    QuestCardItem(
                        quest = quest,
                        scale = scale,
                        isCompact = isCompact,
                        onClick = { onSelectQuest(quest.id) },
                        onClaimReward = { onClaimReward(quest.id) },
                        onChat = onChat,
                        onStart = { quest.type?.let { onStartQuest(it) } },
                        startEnabled = canManageQuest && !isBusy,
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
                .then(if (isCompact) Modifier else Modifier.width(300.dp * scale))
                .height(72.dp * scale)
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

@Composable
private fun QuestCardItem(
    quest: QuestItemUiModel,
    scale: Float,
    onClick: () -> Unit,
    onClaimReward: () -> Unit,
    onChat: () -> Unit,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false,
    onStart: () -> Unit = {},
    startEnabled: Boolean = true,
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
            QuestActionType.START -> {
                Box(
                    modifier =
                        Modifier
                            .width(buttonWidth)
                            .height(108.dp * scale)
                            .clip(RoundedCornerShape(20.dp * scale))
                            .background(if (startEnabled) Colors.button else Colors.raised)
                            .clickable(enabled = startEnabled, onClick = onStart)
                            .testTag("quest-btn-start-${quest.id}"),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.quest_action_start),
                        style =
                            questTextStyle(
                                38f,
                                scale,
                                bold = true,
                                color = if (startEnabled) Colors.onButton else Colors.muted,
                            ),
                    )
                }
            }

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
    outfitId: String?,
    backgroundId: String?,
    scale: Float,
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
                    appearanceKey = appearanceKey,
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
                scale = scale,
                onBackToList = onBackToList,
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
                    friendId = friendId,
                    accessoryId = accessoryId,
                    outfitId = outfitId,
                    backgroundId = backgroundId,
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

@Composable
private fun QuestHiddenClaimModal(
    quest: HiddenQuestUiModel,
    friendId: String,
    accessoryId: String?,
    outfitId: String?,
    backgroundId: String?,
    scale: Float,
    onClaim: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(Color(0xE6050C16))
                    .clickable(onClick = onDismiss),
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
                                .clickable(onClick = onDismiss)
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
                                .clickable(onClick = onClaim)
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
