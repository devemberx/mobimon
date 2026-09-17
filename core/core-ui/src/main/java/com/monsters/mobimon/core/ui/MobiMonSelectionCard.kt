package com.monsters.mobimon.core.ui

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role

/** Selectable preview card; selection does not purchase, equip or award anything.
 * [content] supplies artwork, labels and ownership status. Keep actions outside the card.
 * @sample com.monsters.mobimon.core.ui.ComponentGallery
 */
@Composable
fun MobiMonSelectionCard(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: androidx.compose.ui.graphics.Shape? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    MobiMonSelectionSurface(
        selected,
        onClick,
        modifier,
        enabled,
        Role.RadioButton,
        pill = false,
        content,
        customShape = shape,
    )
}
