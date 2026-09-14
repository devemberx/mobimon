package com.monsters.mobimon.core.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.UiComposable
import com.monsters.mobimon.core.domain.VehicleSnapshot

/** An app-assembled extension to vehicle details, without sibling feature dependencies. */
interface VehicleDetailContribution {
    val key: String

    @Composable
    @UiComposable
    fun Content(
        snapshot: VehicleSnapshot,
        modifier: Modifier,
    )
}
