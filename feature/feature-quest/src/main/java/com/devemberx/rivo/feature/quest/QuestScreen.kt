package com.devemberx.rivo.feature.quest

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
import com.devemberx.rivo.core.domain.QuestProgress
import com.devemberx.rivo.core.domain.QuestType
import com.devemberx.rivo.core.ui.RivoContentColumn
import com.devemberx.rivo.core.ui.RivoMessage
import com.devemberx.rivo.core.ui.RivoSection

/** Renders committed Q01 progression and forthcoming quests without issuing rewards itself. */
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
    RivoContentColumn(modifier = modifier) {
        Text(stringResource(R.string.quest_intro), style = MaterialTheme.typography.bodyLarge)
        errorMessage?.let { RivoMessage(it, isError = true) }
        if (isBusy) RivoMessage(stringResource(R.string.quest_saving))
        if (!canManageQuest && completion == null) RivoMessage(stringResource(R.string.quest_parked_required))
        RivoSection(title = stringResource(R.string.quest_q01_title)) {
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
        ForthcomingQuest(
            title = stringResource(R.string.quest_q02_title),
            description = stringResource(R.string.quest_q02_description),
            actionLabel = stringResource(R.string.quest_q02_forthcoming),
        )
        ForthcomingQuest(
            title = stringResource(R.string.quest_q03_title),
            description = stringResource(R.string.quest_q03_description),
            actionLabel = stringResource(R.string.quest_q03_forthcoming),
        )
    }
}

@Composable
private fun ForthcomingQuest(
    title: String,
    description: String,
    actionLabel: String,
) {
    RivoSection(title = title) {
        Text(description)
        OutlinedButton(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
            Text(actionLabel)
        }
    }
}
