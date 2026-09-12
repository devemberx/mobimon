package com.monsters.mobimon.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monsters.mobimon.core.domain.PointEconomy
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface PointBalanceState {
    data object Loading : PointBalanceState

    data class Ready(
        val balance: Long,
    ) : PointBalanceState

    data object Failed : PointBalanceState
}

class PointBalanceViewModel(
    private val points: PointEconomy,
) : ViewModel() {
    private val mutableState = MutableStateFlow<PointBalanceState>(PointBalanceState.Loading)
    val state = mutableState.asStateFlow()
    private var observer: Job? = null

    init {
        retry()
    }

    fun retry() {
        if (observer?.isActive == true) return
        mutableState.value = PointBalanceState.Loading
        observer =
            viewModelScope.launch {
                try {
                    points.wallet.collect { wallet -> mutableState.value = PointBalanceState.Ready(wallet.balance) }
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    mutableState.value = PointBalanceState.Failed
                }
            }
    }
}
