package com.monsters.mobimon.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** A scrollable content body that reflows within the available window. */
@Composable
fun MobiMonContentColumn(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .verticalScroll(
                    rememberScrollState(),
                ).padding(MobiMonDimensions.contentPadding),
        verticalArrangement = Arrangement.spacedBy(MobiMonDimensions.contentGap),
        content = content,
    )
}
