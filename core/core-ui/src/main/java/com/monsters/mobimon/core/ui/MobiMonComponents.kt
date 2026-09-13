package com.monsters.mobimon.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

/** A scrollable content body that reflows within the available window. */
@Composable
fun MobiMonContentColumn(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        content = content,
    )
}

/** Groups related [content] beneath [title] without owning its state. */
@Composable
fun MobiMonSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}

/** Marks simulated data explicitly without claiming that a real source is connected. */
@Composable
fun MobiMonSourceBadge(
    simulated: Boolean,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = MaterialTheme.typography.labelLarge,
    contentPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        shape = RoundedCornerShape(12.dp),
    ) {
        Text(
            text = stringResource(if (simulated) R.string.mobimon_source_simulated else R.string.mobimon_source_real),
            modifier = Modifier.padding(contentPadding),
            style = textStyle,
        )
    }
}

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

/** Shows loading/failure distinctly from a committed zero-point balance. */
@Composable
fun MobiMonPointSummary(
    balance: Long?,
    failed: Boolean = false,
    modifier: Modifier = Modifier,
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

/** Announces a caller-provided failure or a local availability explanation. */
@Composable
fun MobiMonMessage(
    message: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
) {
    Surface(
        modifier = modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite },
        color = if (isError) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant,
        contentColor =
            if (isError) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        shape = RoundedCornerShape(16.dp),
    ) {
        Text(message, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium)
    }
}
