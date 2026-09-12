package com.monsters.mobimon.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.monsters.mobimon.R
import com.monsters.mobimon.core.domain.AppUseState
import com.monsters.mobimon.core.domain.DrivingState
import com.monsters.mobimon.core.domain.PetAppearance
import com.monsters.mobimon.core.domain.QuestType
import com.monsters.mobimon.core.domain.SignalQuality
import com.monsters.mobimon.core.domain.SignalSource
import com.monsters.mobimon.core.domain.VehicleSnapshot
import com.monsters.mobimon.core.ui.MobiMonContentColumn
import com.monsters.mobimon.core.ui.MobiMonMessage
import com.monsters.mobimon.core.ui.MobiMonTheme
import com.monsters.mobimon.di.AppDependencies
import com.monsters.mobimon.feature.pet.CustomizationScreen
import com.monsters.mobimon.feature.pet.PetHomeScreen
import com.monsters.mobimon.feature.pet.PetUiState
import com.monsters.mobimon.feature.pet.PetViewModel
import com.monsters.mobimon.feature.pet.SettingsScreen
import com.monsters.mobimon.feature.quest.QuestMessage
import com.monsters.mobimon.feature.quest.QuestScreen
import com.monsters.mobimon.feature.quest.QuestUiState
import com.monsters.mobimon.feature.quest.QuestViewModel
import com.monsters.mobimon.feature.vehicle.VehicleInfoScreen

@Composable
fun MobiMonApp(dependencies: AppDependencies) {
    val factory =
        remember(dependencies) {
            viewModelFactory {
                initializer { PetViewModel(dependencies.pets, dependencies.settings) }
                initializer { PointBalanceViewModel(dependencies.points) }
                initializer { CosmeticInventoryViewModel(dependencies.points) }
                initializer {
                    VehicleStateViewModel(
                        dependencies.vehicle,
                        dependencies.identity,
                        dependencies.clock,
                        dependencies.freshness,
                    )
                }
                initializer {
                    QuestViewModel(
                        dependencies.quests,
                        dependencies.rewards,
                        dependencies.vehicle,
                        dependencies.identity,
                        dependencies.clock,
                        dependencies.evaluator,
                    )
                }
            }
        }
    val petViewModel: PetViewModel = viewModel(factory = factory)
    val questViewModel: QuestViewModel = viewModel(factory = factory)
    val vehicleViewModel: VehicleStateViewModel = viewModel(factory = factory)
    val pointsViewModel: PointBalanceViewModel = viewModel(factory = factory)
    val inventoryViewModel: CosmeticInventoryViewModel = viewModel(factory = factory)
    val petState by petViewModel.state.collectAsStateWithLifecycle()
    val questState by questViewModel.state.collectAsStateWithLifecycle()
    val vehicleSnapshot by vehicleViewModel.state.collectAsStateWithLifecycle()
    val pointBalance by pointsViewModel.state.collectAsStateWithLifecycle()
    val inventoryState by inventoryViewModel.state.collectAsStateWithLifecycle()
    val appUseState by dependencies.appUse.states.collectAsStateWithLifecycle()
    MobiMonContent(
        petState = petState,
        questState = questState,
        vehicleSnapshot = vehicleSnapshot,
        pointBalance = pointBalance,
        inventoryState = inventoryState,
        appUseState = appUseState,
        actions =
            MobiMonActions(
                onRetry = {
                    petViewModel.retry()
                    questViewModel.retry()
                    pointsViewModel.retry()
                    inventoryViewModel.retry()
                },
                onAppearanceChange = petViewModel::setAppearance,
                onVehicleVisibilityChange = petViewModel::setShowOnVehicleHome,
                onReducedMotionChange = petViewModel::setReducedMotion,
                onStartQuest = questViewModel::start,
                onCancelQuest = questViewModel::cancel,
                onAcknowledge = questViewModel::acknowledge,
                onEquipFriend = inventoryViewModel::equipFriend,
            ),
    )
}

data class MobiMonActions(
    val onRetry: () -> Unit,
    val onAppearanceChange: (PetAppearance) -> Unit,
    val onVehicleVisibilityChange: (Boolean) -> Unit,
    val onReducedMotionChange: (Boolean) -> Unit,
    val onStartQuest: (QuestType) -> Unit,
    val onCancelQuest: () -> Unit,
    val onAcknowledge: (String) -> Unit,
    val onEquipFriend: (String) -> Unit = {},
)

internal val ShellSaver =
    listSaver<ShellState, String>(
        save = { listOf(it.home.name, it.route.name) },
        restore = { saved ->
            ShellState(
                home = HomeSurface.entries.firstOrNull { it.name == saved.getOrNull(0) } ?: HomeSurface.PET,
                route = AppRoute.entries.firstOrNull { it.name == saved.getOrNull(1) } ?: AppRoute.HOME,
            )
        },
    )

@Composable
fun MobiMonContent(
    petState: PetUiState,
    questState: QuestUiState,
    actions: MobiMonActions,
    modifier: Modifier = Modifier,
    vehicleSnapshot: VehicleSnapshot = questState.snapshot,
    pointBalance: PointBalanceState = PointBalanceState.Loading,
    inventoryState: CosmeticInventoryUiState = CosmeticInventoryUiState(),
    appUseState: AppUseState = AppUseState.ALLOWED,
) {
    var shell by rememberSaveable(stateSaver = ShellSaver) { mutableStateOf(ShellState()) }
    MobiMonTheme {
        Surface(modifier = modifier.fillMaxSize()) {
            val profile = petState.profile
            if (profile == null && petState.loadFailed) {
                LoadingOrError(failed = true, onRetry = actions.onRetry)
            } else if (profile == null || petState.isLoading) {
                LoadingOrError(failed = false, onRetry = actions.onRetry)
            } else if (appUseState != AppUseState.ALLOWED) {
                MobiMonContentColumn {
                    Text(stringResource(R.string.app_use_paused), style = MaterialTheme.typography.headlineMedium)
                    MobiMonMessage(stringResource(R.string.app_use_restricted))
                }
            } else {
                BackHandler(enabled = shell.menuOpen || shell.route != AppRoute.HOME) { shell = shell.back() }
                val balance = (pointBalance as? PointBalanceState.Ready)?.balance
                val balanceFailed = pointBalance == PointBalanceState.Failed
                val legacyQuestVisible = vehicleSnapshot.source == SignalSource.SIMULATED
                val interactionAllowed =
                    appUseState == AppUseState.ALLOWED &&
                        vehicleSnapshot.quality == SignalQuality.VALID &&
                        vehicleSnapshot.drivingState == DrivingState.PARKED
                val visibilitySaveError =
                    if (petState.visibilitySaveFailed) {
                        stringResource(
                            R.string.save_failed,
                        )
                    } else {
                        null
                    }
                val motionSaveError =
                    if (petState.reducedMotionSaveFailed) {
                        stringResource(
                            R.string.save_failed,
                        )
                    } else {
                        null
                    }
                val observationChanged =
                    questState.progress.activeRun?.let { it.startEpoch != questState.snapshot.epoch } == true
                val questError =
                    questErrorText(
                        questState.message ?: if (observationChanged) QuestMessage.OBSERVATION_CHANGED else null,
                    )
                if (shell.route == AppRoute.HOME) {
                    PetHomeScreen(
                        profile,
                        vehicleSnapshot,
                        questState.progress,
                        petState.settings,
                        onOpenMenu = { shell = shell.openMenu() },
                        onOpenVehicleInfo = { shell = shell.navigate(AppRoute.VEHICLE_INFO) },
                        onOpenQuests = { shell = shell.navigate(AppRoute.QUESTS) },
                        onSwitchHome = { shell = shell.switchHome() },
                        onPetClick = { shell = shell.navigate(AppRoute.CONVERSATION) },
                        onOpenAppearance = { shell = shell.navigate(AppRoute.APPEARANCE) },
                        vehiclePreview = shell.home == HomeSurface.VEHICLE,
                        pointBalance = balance,
                        pointLoadFailed = balanceFailed,
                        legacyQuestVisible = legacyQuestVisible,
                        friendId = inventoryState.equippedFriendId,
                        interactionAllowed = interactionAllowed,
                        profileObservationFailed = petState.loadFailed,
                        onRetryProfile = actions.onRetry,
                    )
                } else {
                    Column(Modifier.fillMaxSize()) {
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(24.dp),
                        ) {
                            TextButton(
                                onClick = { shell = shell.back() },
                                modifier = Modifier.heightIn(min = 76.dp),
                            ) { Text(stringResource(R.string.drawer_back)) }
                            Text(
                                stringResource(shell.route.title()),
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.headlineMedium,
                            )
                            TextButton(
                                onClick = { shell = shell.returnHome() },
                                modifier = Modifier.heightIn(min = 76.dp),
                            ) { Text(stringResource(R.string.return_home)) }
                        }
                        Box(Modifier.weight(1f).fillMaxWidth()) {
                            when (shell.route) {
                                AppRoute.APPEARANCE ->
                                    CustomizationScreen(
                                        inventory = inventoryState.inventory,
                                        onEquipFriend = actions.onEquipFriend,
                                        pointBalance = balance,
                                        pointLoadFailed = balanceFailed,
                                        saving = inventoryState.saving,
                                        loadFailed = inventoryState.loadFailed,
                                        saveFailed = inventoryState.saveFailed,
                                        onRetry = actions.onRetry,
                                    )
                                AppRoute.SETTINGS ->
                                    SettingsScreen(
                                        petState.settings,
                                        actions.onVehicleVisibilityChange,
                                        actions.onReducedMotionChange,
                                        visibilitySaving = petState.visibilitySaving,
                                        visibilityError = visibilitySaveError,
                                        motionSaving = petState.reducedMotionSaving,
                                        motionError = motionSaveError,
                                        settingsAvailable = petState.settingsLoaded,
                                        settingsLoadFailed = petState.settingsLoadFailed,
                                        onRetry = actions.onRetry,
                                    )
                                AppRoute.QUESTS ->
                                    QuestScreen(
                                        questState.progress,
                                        questState.canManageQuest && interactionAllowed,
                                        actions.onStartQuest,
                                        actions.onCancelQuest,
                                        onOpenVehicleInfo = { shell = shell.navigate(AppRoute.VEHICLE_INFO) },
                                        isBusy = questState.isBusy,
                                        errorMessage = questError,
                                        pointBalance = balance,
                                        pointLoadFailed = balanceFailed,
                                        legacyVisible = legacyQuestVisible,
                                    )
                                AppRoute.VEHICLE_INFO ->
                                    VehicleInfoScreen(
                                        snapshot = vehicleSnapshot,
                                        questActive = questState.progress.activeRun != null,
                                        questCompleted =
                                            questState.progress.completions.any {
                                                it.type == QuestType.Q01
                                            },
                                        canAcknowledge = questState.canAcknowledge && interactionAllowed,
                                        onAcknowledge = actions.onAcknowledge,
                                        isBusy = questState.isBusy,
                                        errorMessage = questError,
                                        legacyQuestVisible = legacyQuestVisible,
                                    )
                                AppRoute.CONVERSATION ->
                                    MobiMonContentColumn {
                                        MobiMonMessage(stringResource(R.string.conversation_not_connected))
                                    }
                                AppRoute.HOME -> Unit
                            }
                        }
                    }
                }
                if (shell.menuOpen) {
                    CompanionMenu(
                        onClose = { shell = shell.back() },
                        onNavigate = { shell = shell.navigate(it) },
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingOrError(
    failed: Boolean,
    onRetry: () -> Unit,
) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (failed) {
            Text(stringResource(R.string.load_failed))
            Button(onClick = onRetry) { Text(stringResource(R.string.retry)) }
        } else {
            CircularProgressIndicator()
            Text(stringResource(R.string.loading_companion))
        }
    }
}

@Composable
private fun questErrorText(message: QuestMessage?): String? =
    message?.let {
        stringResource(
            when (it) {
                QuestMessage.NOT_PARKED -> R.string.quest_not_parked
                QuestMessage.NO_DATA -> R.string.quest_no_data
                QuestMessage.STALE -> R.string.quest_stale
                QuestMessage.WRONG_SOURCE -> R.string.quest_wrong_source
                QuestMessage.OBSERVATION_CHANGED -> R.string.quest_observation_changed
                QuestMessage.REFRESH_REQUIRED -> R.string.quest_refresh_required
                QuestMessage.UNSUPPORTED -> R.string.quest_unsupported
                QuestMessage.ALREADY_ACTIVE -> R.string.quest_already_active
                QuestMessage.ALREADY_COMPLETED -> R.string.quest_already_completed
                QuestMessage.STORAGE_FAILURE -> R.string.save_failed
                QuestMessage.APP_USE_RESTRICTED -> R.string.app_use_restricted
            },
        )
    }
