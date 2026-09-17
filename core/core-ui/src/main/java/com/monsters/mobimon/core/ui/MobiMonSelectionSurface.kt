package com.monsters.mobimon.core.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

@Composable
internal fun MobiMonSelectionSurface(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    role: Role,
    pill: Boolean,
    content: @Composable BoxScope.() -> Unit,
    customShape: androidx.compose.ui.graphics.Shape? = null,
) {
    var focused by remember { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme
    val shape = customShape ?: if (pill) RoundedCornerShape(50) else RoundedCornerShape(MobiMonDimensions.panelCorner)
    Surface(
        modifier
            .sizeIn(minWidth = MobiMonDimensions.touchTarget, minHeight = MobiMonDimensions.touchTarget)
            .onFocusChanged { focused = it.isFocused }
            .then(if (focused) Modifier.border(3.dp, colors.secondary, shape) else Modifier)
            .selectable(selected, enabled = enabled, role = role, onClick = onClick),
        shape = shape,
        border = BorderStroke(if (selected) 3.dp else 1.dp, if (selected) colors.secondary else colors.outline),
        color = if (selected && pill) colors.secondary else colors.surface,
        contentColor =
            when {
                !enabled -> colors.onSurfaceVariant
                selected && pill -> colors.onSecondary
                else -> colors.onSurface
            },
    ) {
        Box(
            if (pill) Modifier.padding(horizontal = 24.dp, vertical = 12.dp) else Modifier,
            contentAlignment = Alignment.Center,
            content = content,
        )
    }
}
