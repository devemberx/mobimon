package com.monsters.mobimon.feature.quest

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.monsters.mobimon.core.domain.Clock
import com.monsters.mobimon.core.domain.ProgressionIdentity
import com.monsters.mobimon.core.domain.QuestEvaluator
import com.monsters.mobimon.core.domain.QuestRepository
import com.monsters.mobimon.core.domain.QuestType
import com.monsters.mobimon.core.domain.RewardRepository
import com.monsters.mobimon.core.domain.VehicleRepository
import com.monsters.mobimon.core.domain.VehicleSnapshot
import com.monsters.mobimon.core.navigation.AppRoute
import com.monsters.mobimon.core.navigation.FeatureEntry
import com.monsters.mobimon.core.navigation.FeatureNavigator
import com.monsters.mobimon.core.navigation.QuestRoute
import com.monsters.mobimon.core.navigation.VehicleRoute
import com.monsters.mobimon.core.presentation.PointBalanceState
import com.monsters.mobimon.core.presentation.PointPresentation
import com.monsters.mobimon.core.presentation.VehicleDetailContribution
import com.monsters.mobimon.core.presentation.VehiclePresentation
import com.monsters.mobimon.core.presentation.parkedVerified
import com.monsters.mobimon.core.ui.MobiMonDestination

class QuestFeature(
    private val quests: QuestRepository,
    private val rewards: RewardRepository,
    private val source: VehicleRepository,
    private val identity: ProgressionIdentity,
    private val clock: Clock,
    private val evaluator: QuestEvaluator,
    private val vehicle: VehiclePresentation,
    private val wallet: PointPresentation,
) : FeatureEntry,
    VehicleDetailContribution {
    override val routes = setOf(QuestRoute.QUESTS)
    override val key = "legacy-q01"

    @Composable
    private fun model(): QuestViewModel {
        val factory =
            remember(this) {
                viewModelFactory { initializer { QuestViewModel(quests, rewards, source, identity, clock, evaluator) } }
            }
        return viewModel(factory = factory)
    }

    @Composable
    override fun Content(
        route: AppRoute,
        navigator: FeatureNavigator,
        modifier: Modifier,
    ) {
        val model = model()
        val state by model.state.collectAsStateWithLifecycle()
        val pointBalance by wallet.model().state.collectAsStateWithLifecycle()
        val snapshot = vehicle.snapshot()
        val interactionAllowed = snapshot.parkedVerified
        val balance = (pointBalance as? PointBalanceState.Ready)?.balance
        val balanceFailed = pointBalance == PointBalanceState.Failed
        val questError = error(state)
        MobiMonDestination(
            stringResource(R.string.quest_destination_title),
            navigator.back,
            navigator.returnHome,
            modifier,
        ) {
            QuestScreen(
                state.progress,
                state.canManageQuest && interactionAllowed,
                model::start,
                model::cancel,
                onOpenVehicleInfo = { navigator.navigate(VehicleRoute.VEHICLE_INFO) },
                isBusy = state.isBusy,
                errorMessage = questError,
                pointBalance = balance,
                pointLoadFailed = balanceFailed,
            )
        }
    }

    @Composable
    override fun Content(
        snapshot: VehicleSnapshot,
        modifier: Modifier,
    ) {
        val model = model()
        val state by model.state.collectAsStateWithLifecycle()
        QuestVehicleCard(
            snapshot = snapshot,
            questActive = state.progress.activeRun != null,
            questCompleted = state.progress.completions.any { it.type == QuestType.Q01 },
            canAcknowledge = state.canAcknowledge && snapshot.parkedVerified,
            onAcknowledge = model::acknowledge,
            modifier = modifier,
            isBusy = state.isBusy,
            errorMessage = error(state),
        )
    }

    @Composable
    private fun error(state: QuestUiState): String? {
        val observationChanged = state.progress.activeRun?.let { it.startEpoch != state.snapshot.epoch } == true
        return questErrorText(state.message ?: if (observationChanged) QuestMessage.OBSERVATION_CHANGED else null)
    }
}
