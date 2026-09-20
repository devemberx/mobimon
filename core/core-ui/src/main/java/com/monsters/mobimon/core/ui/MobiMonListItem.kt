package com.monsters.mobimon.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

/** Aligned information row shared by account, preference and reading presentations.
 * [leading] is decorative; [headline] and [supporting] describe the item.
 * [trailing] moves below the text in compact/enlarged-text windows.
 * Apply interaction semantics to [modifier] or supply a separate trailing control, never both.
 * @sample com.monsters.mobimon.core.ui.ComponentGallery
 */
@Composable
fun MobiMonListItem(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(MobiMonDimensions.contentPadding),
    shape: Shape = RoundedCornerShape(MobiMonDimensions.messageCorner),
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    leading: (@Composable () -> Unit)? = null,
    supporting: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    headline: @Composable () -> Unit,
) {
    Surface(
        modifier,
        shape = shape,
        color = containerColor,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        BoxWithConstraints(Modifier.fillMaxWidth().padding(contentPadding), contentAlignment = Alignment.Center) {
            val stacked = maxWidth / LocalDensity.current.fontScale < 560.dp
            Column(
                verticalArrangement = Arrangement.spacedBy(MobiMonDimensions.contentGap, Alignment.CenterVertically),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(MobiMonDimensions.contentGap),
                ) {
                    if (leading != null) Box(contentAlignment = Alignment.Center) { leading() }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        headline()
                        supporting?.invoke()
                    }
                    if (!stacked) trailing?.invoke()
                }
                if (stacked) trailing?.invoke()
            }
        }
    }
}
