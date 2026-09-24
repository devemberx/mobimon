package com.monsters.mobimon.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.monsters.mobimon.core.domain.ConversationProblem
import com.monsters.mobimon.core.ui.mobiMonReferenceTextStyle
import com.monsters.mobimon.core.ui.MobiMonColors as Colors
import com.monsters.mobimon.core.ui.R as CoreUiR

/** Conversation-specific status geometry from the v5 chat frame. */
@Composable
internal fun ConversationParkingBadge(
    parked: Boolean,
    scale: Float,
    modifier: Modifier = Modifier,
) {
    val status =
        stringResource(if (parked) CoreUiR.string.mobimon_parking_confirmed else R.string.chat_parking_required)
    Box(
        modifier
            .size((if (parked) 258.dp else 272.dp) * scale, 60.dp * scale)
            .background(Colors.raised, RoundedCornerShape(30.dp * scale))
            .testTag("chat-parking-badge")
            .semantics(mergeDescendants = true) { contentDescription = status },
    ) {
        if (parked) {
            Icon(
                painterResource(CoreUiR.drawable.mobimon_parking),
                null,
                Modifier.offset(37.dp * scale, 10.dp * scale).size(40.dp * scale),
                tint = Colors.accent,
            )
        }
        Text(
            status,
            if (parked) {
                Modifier
                    .align(
                        Alignment.CenterEnd,
                    ).offset(x = -40.dp * scale, y = -1.dp * scale)
            } else {
                Modifier.align(Alignment.Center)
            },
            style = mobiMonReferenceTextStyle(if (parked) 26f else 24f, scale),
            color = if (parked) Colors.accent else Colors.destructive,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

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
