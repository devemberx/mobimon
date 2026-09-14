package com.monsters.mobimon.core.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.monsters.mobimon.core.domain.PointEconomy

/** Shared wallet observation; purchase and reward commands remain feature owned. */
class PointPresentation(
    private val points: PointEconomy,
) {
    @Composable
    fun model(): PointBalanceViewModel {
        val factory = remember(this) { viewModelFactory { initializer { PointBalanceViewModel(points) } } }
        return viewModel(factory = factory)
    }
}
