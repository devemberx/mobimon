package com.monsters.mobimon.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monsters.mobimon.core.domain.Clock
import com.monsters.mobimon.core.domain.ProgressionIdentity
import com.monsters.mobimon.core.domain.VehicleFreshnessPolicy
import com.monsters.mobimon.core.domain.VehicleRepository
import com.monsters.mobimon.core.domain.VehicleSnapshot
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

/** Owns vehicle display freshness independently of quest storage observations. */
class VehicleStateViewModel(
    private val vehicle: VehicleRepository,
    private val identity: ProgressionIdentity,
    private val clock: Clock,
    private val freshness: VehicleFreshnessPolicy,
) : ViewModel() {
    private val mutableState = MutableStateFlow(vehicle.snapshots.value)
    val state = mutableState.asStateFlow()

    init {
        viewModelScope.launch {
            val ticks =
                flow {
                    while (true) {
                        emit(clock.nowMillis())
                        delay(1_000)
                    }
                }
            combine(vehicle.snapshots, ticks) { snapshot, now -> displaySnapshot(snapshot, now) }
                .collect { mutableState.value = it }
        }
    }

    private fun displaySnapshot(
        snapshot: VehicleSnapshot,
        nowMillis: Long,
    ): VehicleSnapshot = freshness.displaySnapshot(snapshot, identity.source, nowMillis)
}
