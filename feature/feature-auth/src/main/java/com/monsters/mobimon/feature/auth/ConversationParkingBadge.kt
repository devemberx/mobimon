package com.monsters.mobimon.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.monsters.mobimon.core.domain.ConversationProblem
import com.monsters.mobimon.core.ui.mobiMonReferenceTextStyle
import com.monsters.mobimon.core.ui.MobiMonColors as Colors

/** Conversation connection status shown alongside the shared parking badge. */

@Composable
internal fun ConversationAuthBadge(
    connection: ConversationConnection,
    problem: ConversationProblem?,
    scale: Float,
    modifier: Modifier = Modifier,
) {
    val status =
        stringResource(
            when {
                problem == ConversationProblem.NETWORK -> R.string.chat_network_badge
                connection == ConversationConnection.READY -> R.string.chat_ready
                connection == ConversationConnection.CHECKING -> R.string.chat_checking
                connection == ConversationConnection.SIGNED_OUT -> R.string.chat_signed_out
                else -> R.string.chat_unavailable
            },
        )
    Box(
        modifier
            .size(244.dp * scale, 60.dp * scale)
            .background(Colors.raised, RoundedCornerShape(30.dp * scale))
            .testTag("chat-auth-badge")
            .semantics(mergeDescendants = true) { contentDescription = status },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            status,
            modifier = Modifier.offset(y = -2.dp * scale),
            style = mobiMonReferenceTextStyle(26f, scale),
            color = if (connection == ConversationConnection.READY && problem == null) Colors.success else Colors.muted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
