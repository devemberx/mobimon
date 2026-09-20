package com.monsters.mobimon.feature.pet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monsters.mobimon.core.domain.PetProfile
import com.monsters.mobimon.core.domain.PetRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PetUiState(
    val profile: PetProfile? = null,
    val isLoading: Boolean = true,
    val loadFailed: Boolean = false,
)

/** Home profile observation is independent of settings and customization commands. */
class PetViewModel(
    private val pets: PetRepository,
) : ViewModel() {
    private val mutableState = MutableStateFlow(PetUiState())
    val state = mutableState.asStateFlow()
    private var observer: Job? = null

    init {
        retry()
    }

    fun retry() {
        if (observer?.isActive == true) return
        mutableState.update { it.copy(isLoading = it.profile == null, loadFailed = false) }
        observer =
            viewModelScope.launch {
                try {
                    pets.initialize()
                    pets.profile.collect { profile -> mutableState.value = PetUiState(profile, isLoading = false) }
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    mutableState.update { it.copy(isLoading = false, loadFailed = true) }
                }
            }
    }
}
