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
        stringResource(
            if (parked) CoreUiR.string.mobimon_parking_confirmed else CoreUiR.string.mobimon_parking_unconfirmed,
        )
    Box(
        modifier
            .size(258.dp * scale, 60.dp * scale)
            .background(Colors.raised, RoundedCornerShape(30.dp * scale))
            .testTag("chat-parking-badge")
            .semantics(mergeDescendants = true) { contentDescription = status },
    ) {
        if (parked) {
            Icon(
                painterResource(CoreUiR.drawable.mobimon_parking),
                null,
                Modifier.offset(62.dp * scale, 5.dp * scale).size(40.dp * scale),
                tint = Colors.accent,
            )
        }
        Text(
            status,
            Modifier.align(Alignment.CenterEnd).offset(x = -20.dp * scale),
            style = mobiMonReferenceTextStyle(24f, scale),
            color = Colors.accent,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
internal fun ConversationAuthBadge(
    authenticated: Boolean,
    scale: Float,
    modifier: Modifier = Modifier,
) {
    val status = stringResource(if (authenticated) R.string.chat_authenticated else R.string.chat_signed_out)
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
            style = mobiMonReferenceTextStyle(24f, scale),
            color = if (authenticated) Colors.success else Colors.muted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
