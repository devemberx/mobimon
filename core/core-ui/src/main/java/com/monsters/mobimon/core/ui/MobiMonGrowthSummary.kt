package com.monsters.mobimon.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

/** Displays committed XP and caller-derived growth; it never issues reward commands. */
@Composable
fun MobiMonGrowthSummary(
    totalXp: Int,
    stage: Int,
    xpUntilNextStage: Int?,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(stringResource(R.string.mobimon_growth_stage, stage), style = MaterialTheme.typography.labelLarge)
        Text(stringResource(R.string.mobimon_total_xp, totalXp), style = MaterialTheme.typography.headlineMedium)
        Text(
            text =
                if (xpUntilNextStage == null) {
                    stringResource(R.string.mobimon_growth_final)
                } else {
                    stringResource(R.string.mobimon_growth_remaining, xpUntilNextStage)
                },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
