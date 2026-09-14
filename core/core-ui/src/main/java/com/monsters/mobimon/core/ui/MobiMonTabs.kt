package com.monsters.mobimon.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

/** Wrapping, caller-owned tab group. [content] supplies stable, labelled [MobiMonTab] children.
 * @sample com.monsters.mobimon.core.ui.ComponentGallery
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MobiMonTabs(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    FlowRow(
        modifier.selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(MobiMonDimensions.contentGap),
        verticalArrangement = Arrangement.spacedBy(MobiMonDimensions.contentGap),
        content = { content() },
    )
}
