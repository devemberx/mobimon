package com.monsters.mobimon.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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

/** Conversation-specific parking geometry from the v5 chat frame. */
@Composable
internal fun ConversationParkingBadge(
    confirmed: Boolean,
    scale: Float,
    modifier: Modifier = Modifier,
) {
    val status =
        stringResource(
            if (confirmed) CoreUiR.string.mobimon_parking_confirmed else CoreUiR.string.mobimon_parking_restricted,
        )
    Box(
        modifier
            .size((if (confirmed) 258.dp else 272.dp) * scale, 60.dp * scale)
            .background(Colors.raised, RoundedCornerShape(30.dp * scale))
            .testTag("chat-parking-badge")
            .semantics(mergeDescendants = true) { contentDescription = status },
    ) {
        if (confirmed) {
            Icon(
                painterResource(CoreUiR.drawable.mobimon_parking),
                contentDescription = null,
                modifier = Modifier.offset(37.dp * scale, 10.dp * scale).size(40.dp * scale),
                tint = Colors.accent,
            )
            Text(
                status,
                Modifier.align(Alignment.CenterEnd).offset(x = -40.dp * scale, y = -1.dp * scale),
                style = mobiMonReferenceTextStyle(26f, scale),
                color = Colors.accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        } else {
            Row(
                Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(16.dp * scale, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painterResource(CoreUiR.drawable.mobimon_parking_unconfirmed),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp * scale).testTag("chat-parking-icon"),
                    tint = Colors.destructive,
                )
                Text(
                    status,
                    style = mobiMonReferenceTextStyle(24f, scale),
                    color = Colors.destructive,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/** Conversation connection status shown alongside the parking badge. */

@Composable
internal fun ConversationAuthBadge(
    connection: ConversationConnection,
    problem: ConversationProblem?,
    scale: Float,
    modifier: Modifier = Modifier,
) {
    val ready = connection == ConversationConnection.READY && problem == null
    val status = stringResource(if (ready) R.string.chat_ready else R.string.chat_checking)
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
            color = if (ready) Colors.success else Colors.muted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
