package com.monsters.mobimon.feature.auth

/** Presentation only. Authentication does not establish [ConversationConnection.READY]. */
enum class ConversationConnection { SIGNED_OUT, UNAVAILABLE, READY }

data class ConversationMessage(
    val id: String,
    val text: String,
    val fromUser: Boolean,
)

data class ConversationUiState(
    val connection: ConversationConnection = ConversationConnection.UNAVAILABLE,
    val messages: List<ConversationMessage> = emptyList(),
    val replyPending: Boolean = false,
    val failed: Boolean = false,
)
