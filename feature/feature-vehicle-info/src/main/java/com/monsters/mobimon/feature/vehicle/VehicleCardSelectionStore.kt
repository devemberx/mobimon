package com.monsters.mobimon.feature.vehicle

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface VehicleCardSelectionStore {
    val selectedCards: StateFlow<List<String>>

    fun save(cards: List<String>)

    companion object {
        fun defaults(): List<String> = VehicleCardCatalog.defaultSlots.map { it.id }

        fun validOrDefaults(cards: List<String>): List<String> =
            cards.takeIf { it.size == 6 && it.all { id -> VehicleCardCatalog.find(id) != null } } ?: defaults()
    }
}

internal class InMemoryVehicleCardSelectionStore : VehicleCardSelectionStore {
    private val state = MutableStateFlow(VehicleCardSelectionStore.defaults())
    override val selectedCards: StateFlow<List<String>> = state

    override fun save(cards: List<String>) {
        state.value = VehicleCardSelectionStore.validOrDefaults(cards)
    }
}
