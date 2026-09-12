package com.monsters.mobimon.feature.quest

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.monsters.mobimon.core.domain.QuestProgress
import com.monsters.mobimon.core.domain.QuestType
import com.monsters.mobimon.core.ui.MobiMonContentColumn
import com.monsters.mobimon.core.ui.MobiMonMessage
import com.monsters.mobimon.core.ui.MobiMonSection

/** Renders committed Q01 progression without issuing rewards itself. */
@Composable
fun QuestScreen(
    progress: QuestProgress,
    canManageQuest: Boolean,
    onStartQuest: (QuestType) -> Unit,
    onCancelQuest: () -> Unit,
    onOpenVehicleInfo: () -> Unit,
    modifier: Modifier = Modifier,
    isBusy: Boolean = false,
    errorMessage: String? = null,
) {
    val completion = progress.completions.firstOrNull { it.type == QuestType.Q01 }
    val active = progress.activeRun
    MobiMonContentColumn(modifier = modifier) {
        Text(stringResource(R.string.quest_intro), style = MaterialTheme.typography.bodyLarge)
        errorMessage?.let { MobiMonMessage(it, isError = true) }
        if (isBusy) MobiMonMessage(stringResource(R.string.quest_saving))
        if (!canManageQuest && completion == null) MobiMonMessage(stringResource(R.string.quest_parked_required))
        MobiMonSection(title = stringResource(R.string.quest_q01_title)) {
            Text(stringResource(R.string.quest_q01_description))
            when {
                completion != null -> {
                    Text(stringResource(R.string.quest_completed), style = MaterialTheme.typography.labelLarge)
                    Text(
                        stringResource(R.string.quest_reward_received, completion.awardedXp),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Text(stringResource(R.string.quest_reward_once))
                }
                active?.type == QuestType.Q01 -> {
                    Text(stringResource(R.string.quest_active), style = MaterialTheme.typography.labelLarge)
                    Button(
                        onClick = onOpenVehicleInfo,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    ) {
                        Text(stringResource(R.string.quest_open_vehicle))
                    }
                    OutlinedButton(
                        onClick = onCancelQuest,
                        enabled = canManageQuest && !isBusy,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    ) {
                        Text(stringResource(R.string.quest_cancel))
                    }
                }
                else -> {
                    Text(stringResource(R.string.quest_reward), style = MaterialTheme.typography.labelLarge)
                    if (active != null) Text(stringResource(R.string.quest_other_active))
                    Button(
                        onClick = { onStartQuest(QuestType.Q01) },
                        enabled = canManageQuest && !isBusy && active == null,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    ) {
                        Text(stringResource(R.string.quest_start_q01))
                    }
                }
            }
        }
    }
}
