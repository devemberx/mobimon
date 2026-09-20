package com.monsters.mobimon.feature.pet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monsters.mobimon.core.domain.CompanionSettings
import com.monsters.mobimon.core.domain.SettingsRepository
import com.monsters.mobimon.core.domain.WriteResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val settings: CompanionSettings = CompanionSettings(),
    val loaded: Boolean = false,
    val loadFailed: Boolean = false,
    val reducedMotionSaving: Boolean = false,
    val reducedMotionSaveFailed: Boolean = false,
    val debugModeSaving: Boolean = false,
    val debugModeSaveFailed: Boolean = false,
)

/** Each preference saves independently and renders only committed repository values. */
class SettingsViewModel(
    private val preferences: SettingsRepository,
) : ViewModel() {
    private val mutableState = MutableStateFlow(SettingsUiState())
    val state = mutableState.asStateFlow()
    private var observer: Job? = null

    init {
        retry()
    }

    fun retry() {
        if (observer?.isActive == true) return
        mutableState.update { it.copy(loadFailed = false) }
        observer =
            viewModelScope.launch {
                try {
                    preferences.settings.collect { settings ->
                        mutableState.update { it.copy(settings = settings, loaded = true, loadFailed = false) }
                    }
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    mutableState.update { it.copy(loadFailed = true) }
                }
            }
    }

    fun setReducedMotion(enabled: Boolean) = save(motion = true) { preferences.setReducedMotion(enabled) }

    fun setDebugMode(enabled: Boolean) = save(motion = false) { preferences.setDebugModeEnabled(enabled) }

    private fun save(
        motion: Boolean,
        write: suspend () -> WriteResult,
    ) {
        val current = state.value
        if (!current.loaded || current.loadFailed) return
        if (if (motion) current.reducedMotionSaving else current.debugModeSaving) return
        updateSave(motion, saving = true, failed = false)
        viewModelScope.launch {
            try {
                updateSave(motion, saving = true, failed = write() == WriteResult.Failure)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                updateSave(motion, saving = true, failed = true)
            } finally {
                updateSave(motion, saving = false)
            }
        }
    }

    private fun updateSave(
        motion: Boolean,
        saving: Boolean,
        failed: Boolean? = null,
    ) {
        mutableState.update {
            if (motion) {
                it.copy(reducedMotionSaving = saving, reducedMotionSaveFailed = failed ?: it.reducedMotionSaveFailed)
            } else {
                it.copy(debugModeSaving = saving, debugModeSaveFailed = failed ?: it.debugModeSaveFailed)
            }
        }
    }
}
