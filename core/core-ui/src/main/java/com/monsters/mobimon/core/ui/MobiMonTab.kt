package com.monsters.mobimon.core.ui

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role

/** A v4 category pill; selection and actions remain with the caller.
 * @sample com.monsters.mobimon.core.ui.ComponentGallery
 */
@Composable
fun MobiMonTab(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable BoxScope.() -> Unit,
) {
    MobiMonSelectionSurface(selected, onClick, modifier, enabled, Role.Tab, pill = true, content)
}
