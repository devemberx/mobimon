package com.monsters.mobimon.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monsters.mobimon.core.domain.CompanionSettings
import com.monsters.mobimon.core.domain.CosmeticInventory
import com.monsters.mobimon.core.domain.PetProfile
import com.monsters.mobimon.core.domain.PetRepository
import com.monsters.mobimon.core.domain.PointEconomy
import com.monsters.mobimon.core.domain.SettingsRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

internal data class AiCompanionState(
    val profile: PetProfile? = null,
    val settings: CompanionSettings = CompanionSettings(),
    val inventory: CosmeticInventory? = null,
    val failed: Boolean = false,
)

/** Reads committed companion context without depending on Home or Shop presentation state. */
internal class AiCompanionViewModel(
    private val pets: PetRepository,
    private val settings: SettingsRepository,
    private val points: PointEconomy,
) : ViewModel() {
    private val mutableState = MutableStateFlow(AiCompanionState())
    val state = mutableState.asStateFlow()
    private var observer: Job? = null

    init {
        retry()
    }

    fun retry() {
        if (observer?.isActive == true) return
        mutableState.value = mutableState.value.copy(failed = false)
        observer =
            viewModelScope.launch {
                try {
                    pets.initialize()
                    combine(pets.profile, settings.settings, points.inventory) { profile, preferences, inventory ->
                        AiCompanionState(profile, preferences, inventory)
                    }.collect { mutableState.value = it }
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    mutableState.value = mutableState.value.copy(failed = true)
                }
            }
    }
}
