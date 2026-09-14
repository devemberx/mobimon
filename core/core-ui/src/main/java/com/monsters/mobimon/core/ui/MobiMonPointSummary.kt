package com.monsters.mobimon.core.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle

/** Shows loading/failure distinctly from a committed zero-point balance. */
@Composable
fun MobiMonPointSummary(
    balance: Long?,
    modifier: Modifier = Modifier,
    failed: Boolean = false,
    textStyle: TextStyle = MaterialTheme.typography.titleLarge,
) {
    Text(
        text =
            when {
                failed -> stringResource(R.string.mobimon_points_failed)
                balance == null -> stringResource(R.string.mobimon_points_loading)
                else -> stringResource(R.string.mobimon_points_balance, balance)
            },
        modifier = modifier,
        style = textStyle,
    )
}
