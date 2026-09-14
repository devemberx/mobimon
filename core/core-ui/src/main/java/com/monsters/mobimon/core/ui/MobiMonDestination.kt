package com.monsters.mobimon.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

/** A full-content destination with accessible Back and Home actions.
 * @param content The feature body; receives the available area below the header.
 * @sample com.monsters.mobimon.core.ui.ComponentGallery
 */
@Composable
fun MobiMonDestination(
    title: String,
    onBack: () -> Unit,
    onHome: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Column(modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = MobiMonDimensions.contentPadding, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MobiMonDimensions.contentGap),
        ) {
            TextButton(
                onClick = onBack,
                modifier =
                    Modifier.sizeIn(
                        minWidth = MobiMonDimensions.touchTarget,
                        minHeight = MobiMonDimensions.touchTarget,
                    ),
            ) {
                Text(stringResource(R.string.mobimon_back))
            }
            Text(title, Modifier.weight(1f), style = MaterialTheme.typography.headlineMedium)
            TextButton(
                onClick = onHome,
                modifier =
                    Modifier.sizeIn(
                        minWidth = MobiMonDimensions.touchTarget,
                        minHeight = MobiMonDimensions.touchTarget,
                    ),
            ) {
                Text(stringResource(R.string.mobimon_home))
            }
        }
        Box(Modifier.weight(1f).fillMaxWidth(), content = content)
    }
}
