package com.monsters.mobimon.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monsters.mobimon.core.domain.CosmeticInventory
import com.monsters.mobimon.core.domain.CosmeticSlot
import com.monsters.mobimon.core.domain.EquipResult
import com.monsters.mobimon.core.domain.PointEconomy
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CosmeticInventoryUiState(
    val inventory: CosmeticInventory? = null,
    val loadFailed: Boolean = false,
    val saving: Boolean = false,
    val saveFailed: Boolean = false,
) {
    val equippedFriendId: String? get() = inventory?.equippedItemIds?.get(CosmeticSlot.FRIEND)
}

class CosmeticInventoryViewModel(
    private val points: PointEconomy,
) : ViewModel() {
    private val mutableState = MutableStateFlow(CosmeticInventoryUiState())
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
                    points.inventory.collect { inventory ->
                        mutableState.update { it.copy(inventory = inventory, loadFailed = false) }
                    }
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    mutableState.update { it.copy(loadFailed = true) }
                }
            }
    }

    fun equipFriend(itemId: String) {
        if (state.value.saving ||
            state.value.inventory
                ?.ownedItemIds
                ?.contains(itemId) != true
        ) {
            return
        }
        mutableState.update { it.copy(saving = true, saveFailed = false) }
        viewModelScope.launch {
            try {
                val result = points.equip(itemId)
                mutableState.update {
                    it.copy(saveFailed = result != EquipResult.Applied && result != EquipResult.AlreadyApplied)
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                mutableState.update { it.copy(saveFailed = true) }
            } finally {
                mutableState.update { it.copy(saving = false) }
            }
        }
    }
}
