package com.monsters.mobimon.feature.pet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monsters.mobimon.core.domain.QuestProgress
import com.monsters.mobimon.core.domain.QuestRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn

internal class HomeQuestSummaryViewModel(
    quests: QuestRepository,
) : ViewModel() {
    val state =
        quests.progress
            .catch { cause ->
                if (cause is CancellationException) throw cause
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), QuestProgress())
}
