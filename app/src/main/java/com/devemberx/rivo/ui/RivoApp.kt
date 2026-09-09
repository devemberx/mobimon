package com.devemberx.rivo.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.devemberx.rivo.R
import com.devemberx.rivo.core.domain.PetAppearance
import com.devemberx.rivo.core.domain.QuestType
import com.devemberx.rivo.core.domain.RewardCalculator
import com.devemberx.rivo.core.domain.SignalSource
import com.devemberx.rivo.core.ui.RivoTheme
import com.devemberx.rivo.di.AppDependencies
import com.devemberx.rivo.feature.pet.AppearanceScreen
import com.devemberx.rivo.feature.pet.PetHomeScreen
import com.devemberx.rivo.feature.pet.PetInfoScreen
import com.devemberx.rivo.feature.pet.PetUiState
import com.devemberx.rivo.feature.pet.PetViewModel
import com.devemberx.rivo.feature.pet.SettingsScreen
import com.devemberx.rivo.feature.quest.QuestMessage
import com.devemberx.rivo.feature.quest.QuestScreen
import com.devemberx.rivo.feature.quest.QuestUiState
import com.devemberx.rivo.feature.quest.QuestViewModel
import com.devemberx.rivo.feature.vehicle.VehicleInfoScreen

@Composable
fun RivoApp(dependencies: AppDependencies) {
    val factory =
        remember(dependencies) {
            viewModelFactory {
                initializer { PetViewModel(dependencies.pets, dependencies.settings) }
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
    val petState by petViewModel.state.collectAsStateWithLifecycle()
    val questState by questViewModel.state.collectAsStateWithLifecycle()
    RivoContent(
        petState = petState,
        questState = questState,
        actions =
            RivoActions(
                onRetry = {
                    petViewModel.retry()
                    questViewModel.retry()
                },
                onAppearanceChange = petViewModel::setAppearance,
                onVehicleVisibilityChange = petViewModel::setShowOnVehicleHome,
                onReducedMotionChange = petViewModel::setReducedMotion,
                onStartQuest = questViewModel::start,
                onCancelQuest = questViewModel::cancel,
                onAcknowledge = questViewModel::acknowledge,
            ),
    )
}

data class RivoActions(
    val onRetry: () -> Unit,
    val onAppearanceChange: (PetAppearance) -> Unit,
    val onVehicleVisibilityChange: (Boolean) -> Unit,
    val onReducedMotionChange: (Boolean) -> Unit,
    val onStartQuest: (QuestType) -> Unit,
    val onCancelQuest: () -> Unit,
    val onAcknowledge: (String) -> Unit,
)

private val ShellSaver =
    listSaver<ShellState, String>(
        save = { listOf(it.home.name, it.drawer.name) },
        restore = { ShellState(HomeSurface.valueOf(it[0]), DrawerDestination.valueOf(it[1])) },
    )

@Composable
fun RivoContent(
    petState: PetUiState,
    questState: QuestUiState,
    actions: RivoActions,
    modifier: Modifier = Modifier,
) {
    var shell by rememberSaveable(stateSaver = ShellSaver) { mutableStateOf(ShellState()) }
    val calculator = remember { RewardCalculator() }
    RivoTheme {
        Surface(modifier = modifier.fillMaxSize()) {
            val profile = petState.profile
            if (petState.loadFailed || questState.observationFailed) {
                LoadingOrError(failed = true, onRetry = actions.onRetry)
            } else if (profile == null || petState.isLoading) {
                LoadingOrError(failed = false, onRetry = actions.onRetry)
            } else {
                BackHandler(enabled = shell.drawer == DrawerDestination.CLOSED && shell.conversationUnavailable) {
                    shell = shell.back()
                }
                val stage = calculator.stage(profile.totalXp)
                val remaining = calculator.xpUntilNextStage(profile.totalXp)
                PetHomeScreen(
                    profile,
                    stage,
                    remaining,
                    questState.snapshot,
                    questState.progress,
                    petState.settings,
                    onOpenMenu = { shell = shell.openDrawer(DrawerDestination.MENU) },
                    onOpenVehicleInfo = { shell = shell.openDrawer(DrawerDestination.VEHICLE_INFO) },
                    onOpenQuests = { shell = shell.openDrawer(DrawerDestination.QUESTS) },
                    onSwitchHome = { shell = shell.switchHome() },
                    onPetClick = { shell = shell.copy(conversationUnavailable = !shell.conversationUnavailable) },
                    vehiclePreview = shell.home == HomeSurface.VEHICLE,
                    showConversationUnavailable = shell.conversationUnavailable,
                )
                if (shell.drawer != DrawerDestination.CLOSED) {
                    CompanionDrawer(
                        destination = shell.drawer,
                        onBack = { shell = shell.back() },
                        onClose = { shell = shell.closeDrawer() },
                        onNavigate = { shell = shell.openDrawer(it) },
                        simulated = questState.snapshot.source == SignalSource.SIMULATED,
                    ) {
                        val saveError = if (petState.saveFailed) stringResource(R.string.save_failed) else null
                        val observationChanged =
                            questState.progress.activeRun?.let { it.startEpoch != questState.snapshot.epoch } == true
                        val questError =
                            questErrorText(
                                questState.message ?: if (observationChanged) {
                                    QuestMessage.OBSERVATION_CHANGED
                                } else {
                                    null
                                },
                            )
                        when (shell.drawer) {
                            DrawerDestination.PET_INFO ->
                                PetInfoScreen(
                                    profile,
                                    stage,
                                    remaining,
                                    questState.snapshot,
                                    questState.progress,
                                )
                            DrawerDestination.APPEARANCE ->
                                AppearanceScreen(
                                    profile.appearance,
                                    actions.onAppearanceChange,
                                    isSaving = petState.isSaving,
                                    errorMessage = saveError,
                                )
                            DrawerDestination.SETTINGS ->
                                SettingsScreen(
                                    petState.settings,
                                    actions.onVehicleVisibilityChange,
                                    actions.onReducedMotionChange,
                                    isSaving = petState.isSaving,
                                    errorMessage = saveError,
                                )
                            DrawerDestination.QUESTS ->
                                QuestScreen(
                                    questState.progress,
                                    questState.canManageQuest,
                                    actions.onStartQuest,
                                    actions.onCancelQuest,
                                    onOpenVehicleInfo = { shell = shell.openDrawer(DrawerDestination.VEHICLE_INFO) },
                                    isBusy = questState.isBusy,
                                    errorMessage = questError,
                                )
                            DrawerDestination.VEHICLE_INFO ->
                                VehicleInfoScreen(
                                    snapshot = questState.snapshot,
                                    questActive = questState.progress.activeRun != null,
                                    questCompleted = questState.progress.completions.any { it.type == QuestType.Q01 },
                                    canAcknowledge = questState.canAcknowledge,
                                    onAcknowledge = actions.onAcknowledge,
                                    isBusy = questState.isBusy,
                                    errorMessage = questError,
                                )
                            DrawerDestination.MENU, DrawerDestination.CLOSED -> Unit
                        }
                    }
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
            },
        )
    }
