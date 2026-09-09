package com.devemberx.rivo.feature.quest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devemberx.rivo.core.domain.Clock
import com.devemberx.rivo.core.domain.ProgressionIdentity
import com.devemberx.rivo.core.domain.QuestCommandResult
import com.devemberx.rivo.core.domain.QuestEvaluator
import com.devemberx.rivo.core.domain.QuestProgress
import com.devemberx.rivo.core.domain.QuestRejection
import com.devemberx.rivo.core.domain.QuestRepository
import com.devemberx.rivo.core.domain.QuestType
import com.devemberx.rivo.core.domain.RewardRepository
import com.devemberx.rivo.core.domain.RewardResult
import com.devemberx.rivo.core.domain.SignalQuality
import com.devemberx.rivo.core.domain.VehicleRepository
import com.devemberx.rivo.core.domain.VehicleSnapshot
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class QuestMessage {
    NOT_PARKED,
    NO_DATA,
    STALE,
    WRONG_SOURCE,
    OBSERVATION_CHANGED,
    REFRESH_REQUIRED,
    UNSUPPORTED,
    ALREADY_ACTIVE,
    ALREADY_COMPLETED,
    STORAGE_FAILURE,
}

data class QuestUiState(
    val snapshot: VehicleSnapshot,
    val progress: QuestProgress = QuestProgress(),
    val canManageQuest: Boolean = false,
    val canAcknowledge: Boolean = false,
    val isBusy: Boolean = false,
    val observationFailed: Boolean = false,
    val message: QuestMessage? = null,
)

class QuestViewModel(
    private val quests: QuestRepository,
    private val rewards: RewardRepository,
    private val vehicle: VehicleRepository,
    private val identity: ProgressionIdentity,
    private val clock: Clock,
    private val evaluator: QuestEvaluator,
) : ViewModel() {
    private val mutableState = MutableStateFlow(QuestUiState(vehicle.snapshots.value))
    val state = mutableState.asStateFlow()
    private var observation: Job? = null

    init {
        retry()
    }

    fun retry() {
        if (observation?.isActive == true) return
        val clockTicks =
            flow {
                while (true) {
                    emit(clock.nowMillis())
                    delay(1_000)
                }
            }
        observation =
            viewModelScope.launch {
                combine(quests.progress, vehicle.snapshots, clockTicks) { progress, snapshot, now ->
                    Triple(progress, snapshot, now)
                }.catch { cause ->
                    if (cause is CancellationException) throw cause
                    mutableState.update {
                        it.copy(
                            canManageQuest = false,
                            canAcknowledge = false,
                            observationFailed = true,
                            message = QuestMessage.STORAGE_FAILURE,
                        )
                    }
                }.collect { (progress, snapshot, now) ->
                    val invalid = evaluator.validateSnapshot(snapshot, identity.source, now)
                    val displaySnapshot =
                        when (invalid) {
                            QuestRejection.STALE -> snapshot.copy(quality = SignalQuality.STALE)
                            QuestRejection.INVALID_SIGNAL -> snapshot.copy(quality = SignalQuality.UNAVAILABLE)
                            else -> snapshot
                        }
                    val run = progress.activeRun
                    mutableState.update {
                        it.copy(
                            progress = progress,
                            snapshot = displaySnapshot,
                            canManageQuest = invalid == null,
                            canAcknowledge =
                                run != null &&
                                    evaluator.evaluate(
                                        run,
                                        run.revision,
                                        snapshot,
                                        identity.source,
                                        now,
                                    ) == null,
                            observationFailed = false,
                            message =
                                if (it.observationFailed ||
                                    it.message.isResolved(invalid, it.snapshot, snapshot)
                                ) {
                                    null
                                } else {
                                    it.message
                                },
                        )
                    }
                }
            }
    }

    fun start(type: QuestType) =
        command {
            val snapshot = vehicle.snapshots.value
            if (validate(snapshot)) handle(quests.start(type, snapshot))
        }

    fun cancel() =
        command {
            val snapshot = vehicle.snapshots.value
            if (validate(snapshot)) {
                val run = state.value.progress.activeRun
                if (run == null) {
                    show(QuestMessage.REFRESH_REQUIRED)
                } else {
                    handle(quests.cancel(run.id, run.revision, snapshot))
                }
            }
        }

    fun acknowledge(displayedSnapshotId: String) =
        command {
            val snapshot = vehicle.snapshots.value
            if (snapshot.id != displayedSnapshotId) {
                show(QuestMessage.REFRESH_REQUIRED)
                return@command
            }
            if (!validate(snapshot)) return@command
            val run = state.value.progress.activeRun
            if (run == null) {
                show(QuestMessage.REFRESH_REQUIRED)
                return@command
            }
            val rejection = evaluator.evaluate(run, run.revision, snapshot, identity.source, clock.nowMillis())
            if (rejection != null) {
                show(rejection.message())
                return@command
            }
            when (val result = rewards.complete(run.id, run.revision, snapshot)) {
                is RewardResult.Applied, RewardResult.AlreadyAwarded -> show(null)
                is RewardResult.Rejected -> show(result.reason.message())
                RewardResult.StorageFailure -> show(QuestMessage.STORAGE_FAILURE)
            }
        }

    private fun command(action: suspend () -> Unit) {
        if (state.value.isBusy || state.value.observationFailed) return
        mutableState.update { it.copy(isBusy = true, message = null) }
        viewModelScope.launch {
            try {
                action()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                show(QuestMessage.STORAGE_FAILURE)
            } finally {
                mutableState.update { it.copy(isBusy = false) }
            }
        }
    }

    private fun validate(snapshot: VehicleSnapshot): Boolean {
        val rejection = evaluator.validateSnapshot(snapshot, identity.source, clock.nowMillis())
        if (rejection != null) show(rejection.message())
        return rejection == null
    }

    private fun handle(result: QuestCommandResult) {
        show(
            when (result) {
                is QuestCommandResult.Started, QuestCommandResult.Cancelled -> null
                QuestCommandResult.AlreadyActive -> QuestMessage.ALREADY_ACTIVE
                QuestCommandResult.AlreadyCompleted -> QuestMessage.ALREADY_COMPLETED
                is QuestCommandResult.Rejected -> result.reason.message()
                QuestCommandResult.StorageFailure -> QuestMessage.STORAGE_FAILURE
            },
        )
    }

    private fun show(message: QuestMessage?) {
        mutableState.update { it.copy(message = message) }
    }
}

private fun QuestMessage?.isResolved(
    invalid: QuestRejection?,
    previous: VehicleSnapshot,
    current: VehicleSnapshot,
): Boolean =
    invalid == null &&
        when (this) {
            QuestMessage.NOT_PARKED, QuestMessage.NO_DATA, QuestMessage.STALE, QuestMessage.WRONG_SOURCE -> true
            QuestMessage.REFRESH_REQUIRED -> previous.id != current.id
            else -> false
        }

private fun QuestRejection.message(): QuestMessage =
    when (this) {
        QuestRejection.NOT_PARKED -> QuestMessage.NOT_PARKED
        QuestRejection.UNAVAILABLE, QuestRejection.INVALID_SIGNAL -> QuestMessage.NO_DATA
        QuestRejection.STALE -> QuestMessage.STALE
        QuestRejection.WRONG_SOURCE -> QuestMessage.WRONG_SOURCE
        QuestRejection.WRONG_EPOCH -> QuestMessage.OBSERVATION_CHANGED
        QuestRejection.BEFORE_START, QuestRejection.RUN_CHANGED -> QuestMessage.REFRESH_REQUIRED
        QuestRejection.UNSUPPORTED_QUEST -> QuestMessage.UNSUPPORTED
    }
