package com.monsters.mobimon.core.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp

enum class MobiMonButtonStyle { PRIMARY, SECONDARY, DESTRUCTIVE }

/** A primary action with the project's vehicle touch target. State is owned by the caller.
 * @param content Label and optional leading icon in the button's row.
 * @sample com.monsters.mobimon.core.ui.ComponentGallery
 */
@Composable
fun MobiMonButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    style: MobiMonButtonStyle = MobiMonButtonStyle.PRIMARY,
    content: @Composable RowScope.() -> Unit,
) {
    var focused by remember { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme
    val primary = style == MobiMonButtonStyle.PRIMARY
    val foreground =
        when (style) {
            MobiMonButtonStyle.PRIMARY -> colors.onPrimary
            MobiMonButtonStyle.SECONDARY -> colors.onSurface
            MobiMonButtonStyle.DESTRUCTIVE -> colors.error
        }
    Button(
        onClick = onClick,
        modifier =
            modifier
                .sizeIn(minWidth = MobiMonDimensions.touchTarget, minHeight = MobiMonDimensions.touchTarget)
                .onFocusChanged { focused = it.isFocused }
                .then(if (focused) Modifier.border(3.dp, colors.secondary, RoundedCornerShape(50)) else Modifier),
        enabled = enabled,
        shape = RoundedCornerShape(50),
        border =
            if (primary) {
                null
            } else {
                BorderStroke(
                    1.dp,
                    if (style ==
                        MobiMonButtonStyle.DESTRUCTIVE
                    ) {
                        colors.error
                    } else {
                        colors.outline
                    },
                )
            },
        colors =
            ButtonDefaults.buttonColors(
                containerColor = if (primary) colors.primary else colors.surface,
                contentColor = foreground,
                disabledContainerColor = colors.surfaceVariant,
                disabledContentColor = colors.onSurfaceVariant,
            ),
        content = content,
    )
}
