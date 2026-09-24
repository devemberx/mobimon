package com.monsters.mobimon.feature.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.monsters.mobimon.core.domain.ConversationProblem
import com.monsters.mobimon.core.ui.MobiMonReferenceText
import com.monsters.mobimon.core.ui.mobiMonReferenceTextStyle
import com.monsters.mobimon.core.ui.MobiMonColors as Colors

@Composable
internal fun ConversationFailure(
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    allowed: Boolean,
    scale: Float,
    modifier: Modifier = Modifier,
    problem: ConversationProblem? = null,
) {
    val note =
        when (problem) {
            ConversationProblem.NETWORK -> R.string.chat_network_error
            ConversationProblem.SERVICE -> R.string.chat_service_error
            ConversationProblem.AUTO_UNAVAILABLE -> R.string.chat_auto_error
            ConversationProblem.TIMEOUT -> R.string.chat_timeout_error
            ConversationProblem.ACCESS -> R.string.chat_access_error
            ConversationProblem.ACCOUNT -> R.string.chat_account_error
            ConversationProblem.USAGE -> R.string.chat_usage_error
            ConversationProblem.PROVIDER -> R.string.chat_provider_error
            ConversationProblem.RESTRICTED -> R.string.chat_restricted_error
            ConversationProblem.LIMIT -> R.string.chat_limit_error
            null -> R.string.chat_failure_note
        }
    val retryLabel =
        when (problem) {
            ConversationProblem.ACCOUNT -> R.string.conversation_connect
            ConversationProblem.LIMIT -> R.string.chat_new
            else -> R.string.chat_retry
        }
    BoxWithConstraints(modifier.fillMaxSize().semantics { liveRegion = LiveRegionMode.Polite }) {
        if (maxWidth >= 1200.dp && maxHeight >= 900.dp * scale && LocalDensity.current.fontScale <= 1.1f) {
            Box(
                Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = 48.dp * scale)
                    .size(1856.dp * scale, 900.dp * scale)
                    .background(Colors.panel, RoundedCornerShape(40.dp * scale)),
            ) {
                Icon(
                    painterResource(R.drawable.conversation_warning),
                    null,
                    Modifier.offset(96.dp * scale, 100.dp * scale).size(136.dp * scale),
                    tint = Color.Unspecified,
                )
                MobiMonReferenceText(
                    stringResource(R.string.chat_failure_title),
                    96f,
                    348f,
                    72f,
                    scale = scale,
                    bold = true,
                    modifier = Modifier.semantics { heading() },
                )
                MobiMonReferenceText(
                    stringResource(note),
                    96f,
                    426f,
                    36f,
                    scale = scale,
                    color = Colors.muted,
                )
                Box(
                    Modifier
                        .offset(96.dp * scale, 616.dp * scale)
                        .size(1664.dp * scale, 92.dp * scale)
                        .background(Colors.raised, RoundedCornerShape(24.dp * scale)),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Text(
                        stringResource(R.string.chat_failure_preserved),
                        Modifier.padding(horizontal = 36.dp * scale),
                        style = mobiMonReferenceTextStyle(32f, scale),
                        color = Colors.muted,
                    )
                }
                CopilotButton(
                    stringResource(retryLabel),
                    onRetry,
                    scale,
                    Modifier.offset(96.dp * scale, 756.dp * scale).width(960.dp * scale),
                    enabled = allowed,
                )
                var focused by remember { mutableStateOf(false) }
                Surface(
                    onClick = onDismiss,
                    modifier =
                        Modifier
                            .offset(1088.dp * scale, 756.dp * scale)
                            .size(672.dp * scale, (112.dp * scale).coerceAtLeast(76.dp))
                            .onFocusChanged { focused = it.isFocused },
                    shape = RoundedCornerShape(50),
                    color = Colors.raised,
                    border = if (focused) BorderStroke(3.dp, Colors.accent) else null,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            stringResource(R.string.chat_return),
                            style = mobiMonReferenceTextStyle(40f, scale, true),
                            color = Colors.text,
                        )
                    }
                }
            }
        } else {
            Column(
                Modifier
                    .fillMaxSize()
                    .background(Colors.panel, RoundedCornerShape(40.dp * scale))
                    .verticalScroll(rememberScrollState())
                    .padding(32.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                Icon(
                    painterResource(R.drawable.conversation_warning),
                    null,
                    Modifier.size(88.dp),
                    tint = Color.Unspecified,
                )
                Text(
                    stringResource(R.string.chat_failure_title),
                    Modifier.semantics { heading() },
                    style = mobiMonReferenceTextStyle(52f, scale, true),
                    color = Colors.text,
                )
                Text(
                    stringResource(note),
                    style = mobiMonReferenceTextStyle(34f, scale),
                    color = Colors.muted,
                )
                Text(
                    stringResource(R.string.chat_failure_preserved),
                    style = mobiMonReferenceTextStyle(30f, scale),
                    color = Colors.muted,
                )
                ConversationAction(
                    stringResource(retryLabel),
                    onRetry,
                    allowed,
                    scale,
                    Modifier.fillMaxWidth(),
                )
                ConversationAction(
                    stringResource(R.string.chat_return),
                    onDismiss,
                    true,
                    scale,
                    Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
