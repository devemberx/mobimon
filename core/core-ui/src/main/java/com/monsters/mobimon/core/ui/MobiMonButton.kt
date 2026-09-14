package com.monsters.mobimon.core.ui

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** A primary action with the project's vehicle touch target. State is owned by the caller.
 * @param content Label and optional leading icon in the button's row.
 * @sample com.monsters.mobimon.core.ui.ComponentGallery
 */
@Composable
fun MobiMonButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier.sizeIn(minWidth = MobiMonDimensions.touchTarget, minHeight = MobiMonDimensions.touchTarget),
        enabled = enabled,
        content = content,
    )
}
