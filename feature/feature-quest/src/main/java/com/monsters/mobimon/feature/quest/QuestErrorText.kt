package com.monsters.mobimon.feature.quest

internal fun QuestMessage.textResource(): Int =
    when (this) {
        QuestMessage.INTERACTION_RESTRICTED -> R.string.quest_interaction_restricted
        QuestMessage.REFRESH_REQUIRED -> R.string.quest_refresh_required
        QuestMessage.CONDITION_NOT_MET -> R.string.quest_condition_not_met
        QuestMessage.UNSUPPORTED -> R.string.quest_unsupported
        QuestMessage.STORAGE_FAILURE -> R.string.quest_route_save_failed
    }
