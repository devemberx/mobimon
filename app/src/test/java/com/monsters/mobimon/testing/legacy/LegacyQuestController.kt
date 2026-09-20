package com.monsters.mobimon.testing.legacy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monsters.mobimon.core.domain.Clock
import com.monsters.mobimon.core.domain.ProgressionIdentity
import com.monsters.mobimon.core.domain.QuestCommandResult
import com.monsters.mobimon.core.domain.QuestEvaluator
import com.monsters.mobimon.core.domain.QuestProgress
import com.monsters.mobimon.core.domain.QuestRejection
import com.monsters.mobimon.core.domain.QuestRepository
import com.monsters.mobimon.core.domain.QuestType
import com.monsters.mobimon.core.domain.RewardRepository
import com.monsters.mobimon.core.domain.RewardResult
import com.monsters.mobimon.core.domain.SignalQuality
import com.monsters.mobimon.core.domain.VehicleRepository
import com.monsters.mobimon.core.domain.VehicleSnapshot
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

enum class LegacyQuestMessage {
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
    APP_USE_RESTRICTED,
}

data class LegacyQuestState(
    val snapshot: VehicleSnapshot,
    val progress: QuestProgress = QuestProgress(),
    val canManageQuest: Boolean = false,
    val canAcknowledge: Boolean = false,
    val isBusy: Boolean = false,
    val observationFailed: Boolean = false,
    val message: LegacyQuestMessage? = null,
)

private data class ObservationData(
    val progress: QuestProgress,
    val snapshot: VehicleSnapshot,
    val now: Long,
)

class LegacyQuestController(
    private val quests: QuestRepository,
    private val rewards: RewardRepository,
    private val vehicle: VehicleRepository,
    private val identity: ProgressionIdentity,
    private val clock: Clock,
    private val evaluator: QuestEvaluator,
) : ViewModel() {
    private val mutableState = MutableStateFlow(LegacyQuestState(vehicle.snapshots.value))
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
                    emit(Unit)
                    delay(1_000)
                }
            }
        observation =
            viewModelScope.launch {
                combine(
                    quests.progress,
                    vehicle.snapshots,
                    clockTicks,
                ) { progress, snapshot, _ ->
                    ObservationData(progress, snapshot, clock.nowMillis())
                }.catch { cause ->
                    if (cause is CancellationException) throw cause
                    mutableState.update {
                        it.copy(
                            canManageQuest = false,
                            canAcknowledge = false,
                            observationFailed = true,
                            message = LegacyQuestMessage.STORAGE_FAILURE,
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
                    show(LegacyQuestMessage.REFRESH_REQUIRED)
                } else {
                    handle(quests.cancel(run.id, run.revision, snapshot))
                }
            }
        }

    fun acknowledge(displayedSnapshotId: String) =
        command {
            val snapshot = vehicle.snapshots.value
            if (snapshot.id != displayedSnapshotId) {
                show(LegacyQuestMessage.REFRESH_REQUIRED)
                return@command
            }
            if (!validate(snapshot)) return@command
            val run = state.value.progress.activeRun
            if (run == null) {
                show(LegacyQuestMessage.REFRESH_REQUIRED)
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
                RewardResult.StorageFailure -> show(LegacyQuestMessage.STORAGE_FAILURE)
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
                show(LegacyQuestMessage.STORAGE_FAILURE)
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
                QuestCommandResult.AlreadyActive -> LegacyQuestMessage.ALREADY_ACTIVE
                QuestCommandResult.AlreadyCompleted -> LegacyQuestMessage.ALREADY_COMPLETED
                is QuestCommandResult.Rejected -> result.reason.message()
                QuestCommandResult.StorageFailure -> LegacyQuestMessage.STORAGE_FAILURE
            },
        )
    }

    private fun show(message: LegacyQuestMessage?) {
        mutableState.update { it.copy(message = message) }
    }
}

private fun LegacyQuestMessage?.isResolved(
    invalid: QuestRejection?,
    previous: VehicleSnapshot,
    current: VehicleSnapshot,
): Boolean =
    invalid == null &&
        when (this) {
            LegacyQuestMessage.NOT_PARKED,
            LegacyQuestMessage.NO_DATA,
            LegacyQuestMessage.STALE,
            LegacyQuestMessage.WRONG_SOURCE,
            -> true
            LegacyQuestMessage.REFRESH_REQUIRED -> previous.id != current.id
            else -> false
        }

private fun QuestRejection.message(): LegacyQuestMessage =
    when (this) {
        QuestRejection.NOT_PARKED -> LegacyQuestMessage.NOT_PARKED
        QuestRejection.UNAVAILABLE, QuestRejection.INVALID_SIGNAL -> LegacyQuestMessage.NO_DATA
        QuestRejection.STALE -> LegacyQuestMessage.STALE
        QuestRejection.WRONG_SOURCE -> LegacyQuestMessage.WRONG_SOURCE
        QuestRejection.WRONG_EPOCH -> LegacyQuestMessage.OBSERVATION_CHANGED
        QuestRejection.BEFORE_START, QuestRejection.RUN_CHANGED -> LegacyQuestMessage.REFRESH_REQUIRED
        QuestRejection.UNSUPPORTED_QUEST -> LegacyQuestMessage.UNSUPPORTED
        QuestRejection.APP_USE_RESTRICTED -> LegacyQuestMessage.APP_USE_RESTRICTED
    }
