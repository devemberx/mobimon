package com.monsters.mobimon.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monsters.mobimon.core.domain.SettingsRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.stateIn

/** Pause decoration until preferences are known, and while their storage is unavailable. */
internal class MotionPreferencesViewModel(
    settings: SettingsRepository,
) : ViewModel() {
    val reducedMotion =
        settings.settings
            .map { it.reducedMotion }
            .retryWhen { cause, _ ->
                if (cause is CancellationException) throw cause
                emit(true)
                delay(5_000)
                true
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), true)
}
