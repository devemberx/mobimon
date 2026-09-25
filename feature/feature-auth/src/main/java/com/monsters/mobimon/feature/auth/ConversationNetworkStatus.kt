package com.monsters.mobimon.feature.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** Internet availability used before and during a Copilot request. */
interface ConversationNetworkStatus {
    val online: StateFlow<Boolean>

    fun isOnline(): Boolean
}

object AssumedOnlineConversationNetworkStatus : ConversationNetworkStatus {
    override val online = MutableStateFlow(true)

    override fun isOnline() = true
}
