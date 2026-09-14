package com.monsters.mobimon.core.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

enum class MobiMonStatusTone { INFORMATION, SUCCESS, WARNING, ERROR }

/** Noninteractive status; [content] must name the state instead of relying on color.
 * No tone implies provider verification or permission to dispatch an action.
 * @sample com.monsters.mobimon.core.ui.ComponentGallery
 */
@Composable
fun MobiMonStatusBadge(
    modifier: Modifier = Modifier,
    tone: MobiMonStatusTone = MobiMonStatusTone.INFORMATION,
    content: @Composable RowScope.() -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val foreground =
        when (tone) {
            MobiMonStatusTone.INFORMATION -> colors.secondary
            MobiMonStatusTone.SUCCESS -> colors.tertiary
            MobiMonStatusTone.WARNING -> MobiMonColors.warning
            MobiMonStatusTone.ERROR -> colors.error
        }
    Surface(
        modifier,
        color = colors.surface,
        contentColor = foreground,
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, colors.outline),
    ) {
        Row(
            Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            content = content,
        )
    }
}
