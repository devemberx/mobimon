package com.monsters.mobimon.feature.quest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monsters.mobimon.core.domain.DrivingQuestEvaluator
import com.monsters.mobimon.core.domain.PointAwardResult
import com.monsters.mobimon.core.domain.PointEconomy
import com.monsters.mobimon.core.domain.VehicleSnapshot
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class QuestMessage {
    INTERACTION_RESTRICTED,
    REFRESH_REQUIRED,
    UNSUPPORTED,
    STORAGE_FAILURE,
}

data class QuestRewardSuccess(
    val questId: String,
    val points: Long,
)

data class QuestUiState(
    val completedPointQuestIds: Set<String> = emptySet(),
    val satisfiedDrivingQuestIds: Set<String> = emptySet(),
    val dismissedHiddenQuestIds: Set<String> = emptySet(),
    val pendingQuestId: String? = null,
    val isLoading: Boolean = true,
    val observationFailed: Boolean = false,
    val message: QuestMessage? = null,
    val rewardSuccess: QuestRewardSuccess? = null,
) {
    val isBusy: Boolean get() = pendingQuestId != null
}

class QuestViewModel(
    private val economy: PointEconomy,
    private val drivingEvaluator: DrivingQuestEvaluator = DrivingQuestEvaluator(),
) : ViewModel() {
    private val mutableState = MutableStateFlow(QuestUiState())
    val state = mutableState.asStateFlow()
    private var observation: Job? = null
    private val confirmedQuestIds = mutableSetOf<String>()

    init {
        retry()
    }

    fun retry() {
        if (observation?.isActive == true) return
        mutableState.update { it.copy(isLoading = true, observationFailed = false) }
        observation =
            viewModelScope.launch {
                try {
                    combine(economy.completedQuestIds, economy.driveEvaluation) { completedIds, evaluation ->
                        completedIds to
                            drivingEvaluator
                                .evaluateAll(
                                    evaluation,
                                ).filter { it.isSatisfied }
                                .map { it.questId }
                                .toSet()
                    }.collect { (completedIds, satisfiedIds) ->
                        mutableState.update {
                            it.copy(
                                completedPointQuestIds = completedIds + confirmedQuestIds,
                                satisfiedDrivingQuestIds = satisfiedIds,
                                isLoading = false,
                                observationFailed = false,
                            )
                        }
                    }
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    mutableState.update { it.copy(isLoading = false, observationFailed = true) }
                }
            }
    }

    fun dismissHiddenQuest(questId: String) {
        if (state.value.pendingQuestId == questId) return
        mutableState.update { it.copy(dismissedHiddenQuestIds = it.dismissedHiddenQuestIds + questId) }
    }

    fun dismissRewardSuccess() {
        mutableState.update { it.copy(rewardSuccess = null) }
    }

    fun claimPointQuest(
        questId: String,
        displayedSnapshot: VehicleSnapshot,
    ) {
        if (state.value.isBusy || state.value.isLoading || state.value.observationFailed) return
        mutableState.update { it.copy(pendingQuestId = questId, message = null, rewardSuccess = null) }
        viewModelScope.launch {
            try {
                when (val result = economy.awardQuest(questId, displayedSnapshot)) {
                    is PointAwardResult.Awarded -> confirm(questId, QuestRewardSuccess(questId, result.points))
                    PointAwardResult.AlreadyAwarded -> confirm(questId, null)
                    PointAwardResult.EvidenceChanged -> show(QuestMessage.REFRESH_REQUIRED)
                    PointAwardResult.InteractionRestricted -> show(QuestMessage.INTERACTION_RESTRICTED)
                    PointAwardResult.QuestUnavailable -> show(QuestMessage.UNSUPPORTED)
                    PointAwardResult.StorageFailure -> show(QuestMessage.STORAGE_FAILURE)
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                show(QuestMessage.STORAGE_FAILURE)
            } finally {
                mutableState.update { it.copy(pendingQuestId = null) }
            }
        }
    }

    private fun confirm(
        questId: String,
        rewardSuccess: QuestRewardSuccess?,
    ) {
        // A repository result establishes the commit even before its observation reaches this collector.
        confirmedQuestIds += questId
        mutableState.update {
            it.copy(
                completedPointQuestIds = it.completedPointQuestIds + questId,
                rewardSuccess = rewardSuccess,
                message = null,
            )
        }
    }

    private fun show(message: QuestMessage) {
        mutableState.update { it.copy(message = message) }
    }
}
