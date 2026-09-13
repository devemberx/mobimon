package com.monsters.mobimon.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monsters.mobimon.core.domain.CosmeticInventory
import com.monsters.mobimon.core.domain.CosmeticItem
import com.monsters.mobimon.core.domain.CosmeticSlot
import com.monsters.mobimon.core.domain.EquipResult
import com.monsters.mobimon.core.domain.PointEconomy
import com.monsters.mobimon.core.domain.PurchaseResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CosmeticInventoryUiState(
    val inventory: CosmeticInventory? = null,
    val catalog: List<CosmeticItem> = emptyList(),
    val loadFailed: Boolean = false,
    val saving: Boolean = false,
    val saveFailed: Boolean = false,
    val selectedItemId: String? = null,
    val purchasing: Boolean = false,
    val purchaseFailed: Boolean = false,
) {
    val equippedFriendId: String? get() = inventory?.equippedItemIds?.get(CosmeticSlot.FRIEND)
    val equippedAccessoryId: String? get() = inventory?.equippedItemIds?.get(CosmeticSlot.ACCESSORY)
}

class CosmeticInventoryViewModel(
    private val points: PointEconomy,
) : ViewModel() {
    private val mutableState = MutableStateFlow(CosmeticInventoryUiState())
    val state = mutableState.asStateFlow()
    private var observer: Job? = null
    private var catalogObserver: Job? = null

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

        catalogObserver?.cancel()
        catalogObserver =
            viewModelScope.launch {
                try {
                    points.catalog.collect { catalog ->
                        mutableState.update { it.copy(catalog = catalog) }
                    }
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    // Fail silently for catalog
                }
            }
    }

    fun selectItem(itemId: String?) {
        mutableState.update { it.copy(selectedItemId = itemId, purchaseFailed = false, saveFailed = false) }
    }

    fun purchaseItem(
        itemId: String,
        price: Long,
    ) {
        if (state.value.purchasing) return
        mutableState.update { it.copy(purchasing = true, purchaseFailed = false) }
        viewModelScope.launch {
            try {
                val result = points.purchase(itemId, price)
                mutableState.update {
                    it.copy(purchaseFailed = result !is PurchaseResult.Purchased)
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                mutableState.update { it.copy(purchaseFailed = true) }
            } finally {
                mutableState.update { it.copy(purchasing = false) }
            }
        }
    }

    fun equipItem(itemId: String) {
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

    fun equipFriend(itemId: String) {
        equipItem(itemId)
    }
}
