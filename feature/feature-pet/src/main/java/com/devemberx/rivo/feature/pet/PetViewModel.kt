package com.devemberx.rivo.feature.pet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devemberx.rivo.core.domain.CompanionSettings
import com.devemberx.rivo.core.domain.PetAppearance
import com.devemberx.rivo.core.domain.PetProfile
import com.devemberx.rivo.core.domain.PetRepository
import com.devemberx.rivo.core.domain.SettingsRepository
import com.devemberx.rivo.core.domain.WriteResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PetUiState(
    val profile: PetProfile? = null,
    val settings: CompanionSettings = CompanionSettings(),
    val isLoading: Boolean = true,
    val loadFailed: Boolean = false,
    val isSaving: Boolean = false,
    val saveFailed: Boolean = false,
)

class PetViewModel(
    private val pets: PetRepository,
    private val preferences: SettingsRepository,
) : ViewModel() {
    private val mutableState = MutableStateFlow(PetUiState())
    val state = mutableState.asStateFlow()

    private var observer: Job? = null

    init {
        retry()
    }

    fun retry() {
        if (observer?.isActive == true) return
        mutableState.update { it.copy(isLoading = true, loadFailed = false) }
        observer =
            viewModelScope.launch {
                try {
                    pets.initialize()
                    combine(pets.profile, preferences.settings) { profile, settings -> profile to settings }
                        .collect { (profile, settings) ->
                            mutableState.update {
                                it.copy(
                                    profile = profile,
                                    settings = settings,
                                    isLoading = false,
                                    loadFailed = false,
                                )
                            }
                        }
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    mutableState.update { it.copy(isLoading = false, loadFailed = true) }
                }
            }
    }

    fun setAppearance(appearance: PetAppearance) = save { pets.setAppearance(appearance) }

    fun setShowOnVehicleHome(enabled: Boolean) = save { preferences.setShowOnVehicleHome(enabled) }

    fun setReducedMotion(enabled: Boolean) = save { preferences.setReducedMotion(enabled) }

    private fun save(write: suspend () -> WriteResult) {
        if (state.value.isSaving || state.value.profile == null) return
        mutableState.update { it.copy(isSaving = true, saveFailed = false) }
        viewModelScope.launch {
            try {
                val result = write()
                mutableState.update { it.copy(saveFailed = result == WriteResult.Failure) }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                mutableState.update { it.copy(saveFailed = true) }
            } finally {
                mutableState.update { it.copy(isSaving = false) }
            }
        }
    }
}
