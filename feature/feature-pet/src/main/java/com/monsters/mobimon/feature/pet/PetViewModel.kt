package com.monsters.mobimon.feature.pet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monsters.mobimon.core.domain.CompanionSettings
import com.monsters.mobimon.core.domain.PetAppearance
import com.monsters.mobimon.core.domain.PetProfile
import com.monsters.mobimon.core.domain.PetRepository
import com.monsters.mobimon.core.domain.SettingsRepository
import com.monsters.mobimon.core.domain.WriteResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PetUiState(
    val profile: PetProfile? = null,
    val settings: CompanionSettings = CompanionSettings(),
    val isLoading: Boolean = true,
    val loadFailed: Boolean = false,
    val settingsLoaded: Boolean = false,
    val settingsLoadFailed: Boolean = false,
    val appearanceSaving: Boolean = false,
    val appearanceSaveFailed: Boolean = false,
    val visibilitySaving: Boolean = false,
    val visibilitySaveFailed: Boolean = false,
    val reducedMotionSaving: Boolean = false,
    val reducedMotionSaveFailed: Boolean = false,
) {
    val isSaving: Boolean
        get() = appearanceSaving || visibilitySaving || reducedMotionSaving

    val saveFailed: Boolean
        get() = appearanceSaveFailed || visibilitySaveFailed || reducedMotionSaveFailed
}

private enum class SaveOperation { APPEARANCE, VISIBILITY, REDUCED_MOTION }

class PetViewModel(
    private val pets: PetRepository,
    private val preferences: SettingsRepository,
) : ViewModel() {
    private val mutableState = MutableStateFlow(PetUiState())
    val state = mutableState.asStateFlow()

    private var profileObserver: Job? = null
    private var settingsObserver: Job? = null

    init {
        retry()
    }

    fun retry() {
        if (profileObserver?.isActive != true) {
            mutableState.update { it.copy(isLoading = it.profile == null, loadFailed = false) }
            profileObserver =
                viewModelScope.launch {
                    try {
                        pets.initialize()
                        pets.profile.collect { profile ->
                            mutableState.update {
                                it.copy(profile = profile, isLoading = false, loadFailed = false)
                            }
                        }
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (_: Exception) {
                        mutableState.update { it.copy(isLoading = false, loadFailed = true) }
                    }
                }
        }
        if (settingsObserver?.isActive != true) {
            mutableState.update { it.copy(settingsLoadFailed = false) }
            settingsObserver =
                viewModelScope.launch {
                    try {
                        preferences.settings.collect { settings ->
                            mutableState.update {
                                it.copy(settings = settings, settingsLoaded = true, settingsLoadFailed = false)
                            }
                        }
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (_: Exception) {
                        mutableState.update { it.copy(settingsLoadFailed = true) }
                    }
                }
        }
    }

    fun setAppearance(appearance: PetAppearance) = save(SaveOperation.APPEARANCE) { pets.setAppearance(appearance) }

    fun setShowOnVehicleHome(enabled: Boolean) =
        save(SaveOperation.VISIBILITY) { preferences.setShowOnVehicleHome(enabled) }

    fun setReducedMotion(enabled: Boolean) =
        save(SaveOperation.REDUCED_MOTION) { preferences.setReducedMotion(enabled) }

    private fun save(
        operation: SaveOperation,
        write: suspend () -> WriteResult,
    ) {
        if (state.value.isSaving(operation) || state.value.profile == null) return
        mutableState.update { it.withSave(operation, saving = true, failed = false) }
        viewModelScope.launch {
            try {
                val result = write()
                mutableState.update { it.withSave(operation, saving = true, failed = result == WriteResult.Failure) }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                mutableState.update { it.withSave(operation, saving = true, failed = true) }
            } finally {
                mutableState.update { it.withSave(operation, saving = false) }
            }
        }
    }
}

private fun PetUiState.isSaving(operation: SaveOperation): Boolean =
    when (operation) {
        SaveOperation.APPEARANCE -> appearanceSaving
        SaveOperation.VISIBILITY -> visibilitySaving
        SaveOperation.REDUCED_MOTION -> reducedMotionSaving
    }

private fun PetUiState.withSave(
    operation: SaveOperation,
    saving: Boolean,
    failed: Boolean? = null,
): PetUiState =
    when (operation) {
        SaveOperation.APPEARANCE ->
            copy(appearanceSaving = saving, appearanceSaveFailed = failed ?: appearanceSaveFailed)
        SaveOperation.VISIBILITY ->
            copy(visibilitySaving = saving, visibilitySaveFailed = failed ?: visibilitySaveFailed)
        SaveOperation.REDUCED_MOTION ->
            copy(reducedMotionSaving = saving, reducedMotionSaveFailed = failed ?: reducedMotionSaveFailed)
    }
