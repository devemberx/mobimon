package com.monsters.mobimon.feature.quest

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

@Composable
internal fun questErrorText(message: QuestMessage?): String? =
    message?.let {
        stringResource(
            when (it) {
                QuestMessage.NOT_PARKED -> R.string.quest_not_parked
                QuestMessage.NO_DATA -> R.string.quest_no_data
                QuestMessage.STALE -> R.string.quest_stale
                QuestMessage.WRONG_SOURCE -> R.string.quest_wrong_source
                QuestMessage.OBSERVATION_CHANGED -> R.string.quest_observation_changed
                QuestMessage.REFRESH_REQUIRED -> R.string.quest_refresh_required
                QuestMessage.UNSUPPORTED -> R.string.quest_unsupported
                QuestMessage.ALREADY_ACTIVE -> R.string.quest_already_active
                QuestMessage.ALREADY_COMPLETED -> R.string.quest_already_completed
                QuestMessage.STORAGE_FAILURE -> R.string.quest_route_save_failed
                QuestMessage.APP_USE_RESTRICTED -> R.string.quest_app_use_restricted
            },
        )
    }
