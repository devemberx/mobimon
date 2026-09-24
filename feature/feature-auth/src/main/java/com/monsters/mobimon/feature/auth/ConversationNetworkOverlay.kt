package com.monsters.mobimon.feature.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.monsters.mobimon.core.domain.ConversationProblem
import com.monsters.mobimon.core.ui.MobiMonButton
import com.monsters.mobimon.core.ui.MobiMonReferenceText
import com.monsters.mobimon.core.ui.mobiMonReferenceTextStyle
import com.monsters.mobimon.core.ui.MobiMonColors as Colors

/** The same frame serves access recheck and explicit retry of a failed message. */
@Composable
internal fun ConversationNetworkOverlay(
    checking: Boolean,
    messageFailure: Boolean,
    problem: ConversationProblem?,
    onHome: () -> Unit,
    onRetry: () -> Unit,
) {
    val title =
        stringResource(
            if (checking) R.string.chat_network_rechecking_title else R.string.chat_network_title,
        )
    val body =
        stringResource(
            when {
                checking -> R.string.chat_network_rechecking_body
                messageFailure -> conversationFailureNote(problem)
                else -> R.string.chat_network_body
            },
        )
    val instruction =
        stringResource(
            if (checking) R.string.chat_network_rechecking_instruction else R.string.chat_network_instruction,
        )
    val preserved =
        stringResource(
            if (checking) R.string.chat_network_rechecking_preserved else R.string.chat_network_preserved,
        )
    val action =
        stringResource(
            when {
                checking -> R.string.chat_network_rechecking_action
                messageFailure -> R.string.chat_retry
                else -> R.string.chat_network_recheck
            },
        )
    BoxWithConstraints(
        Modifier.fillMaxSize().semantics {
            paneTitle = title
            liveRegion = LiveRegionMode.Polite
        },
    ) {
        val source = remember { MutableInteractionSource() }
        Box(
            Modifier
                .fillMaxSize()
                .background(Colors.background.copy(alpha = 0.72f))
                .clickable(interactionSource = source, indication = null) {}
                .clearAndSetSemantics {},
        )
        val wide = maxWidth >= 1200.dp && LocalDensity.current.fontScale <= 1.1f
        if (wide) {
            val scale = minOf(maxWidth.value / 2560f, maxHeight.value / 1184f)
            Box(
                Modifier
                    .align(Alignment.Center)
                    .offset(y = 32.dp * scale)
                    .size(1360.dp * scale, 740.dp * scale)
                    .background(Colors.panel, RoundedCornerShape(32.dp * scale))
                    .border(2.dp * scale, Colors.border, RoundedCornerShape(32.dp * scale))
                    .testTag("chat-network-dialog"),
            ) {
                NetworkIcon(checking, Modifier.offset(64.dp * scale, 64.dp * scale), scale)
                MobiMonReferenceText(
                    title,
                    64f,
                    258f,
                    48f,
                    scale = scale,
                    bold = true,
                    modifier = Modifier.semantics { heading() },
                )
                MobiMonReferenceText(body, 64f, 336f, 36f, scale = scale, color = Colors.muted)
                MobiMonReferenceText(instruction, 64f, 390f, 36f, scale = scale, color = Colors.muted)
                MobiMonReferenceText(preserved, 64f, 466f, 28f, scale = scale, color = Colors.muted)
                NetworkHomeButton(
                    onHome,
                    scale,
                    Modifier.offset(64.dp * scale, 560.dp * scale).size(592.dp * scale, 116.dp * scale),
                )
                MobiMonButton(
                    onClick = onRetry,
                    enabled = !checking,
                    modifier =
                        Modifier
                            .offset(680.dp * scale, 560.dp * scale)
                            .size(616.dp * scale, 116.dp * scale)
                            .testTag("chat-network-retry"),
                ) {
                    Text(action, style = mobiMonReferenceTextStyle(40f, scale, true))
                }
            }
        } else {
            Column(
                Modifier
                    .align(Alignment.Center)
                    .width((maxWidth - 48.dp).coerceAtMost(720.dp))
                    .heightIn(max = maxHeight - 32.dp)
                    .background(Colors.panel, RoundedCornerShape(24.dp))
                    .border(2.dp, Colors.border, RoundedCornerShape(24.dp))
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
                    .testTag("chat-network-dialog"),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                NetworkIcon(checking, Modifier, 0.65f)
                Text(
                    title,
                    style = mobiMonReferenceTextStyle(48f, 0.65f, true),
                    color = Colors.text,
                    modifier = Modifier.semantics { heading() },
                )
                Text(body + "\n" + instruction, style = mobiMonReferenceTextStyle(36f, 0.65f), color = Colors.muted)
                Text(preserved, style = mobiMonReferenceTextStyle(28f, 0.65f), color = Colors.muted)
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    NetworkHomeButton(onHome, 0.65f, Modifier.weight(1f).height(76.dp))
                    MobiMonButton(
                        onClick = onRetry,
                        enabled = !checking,
                        modifier = Modifier.weight(1f).height(76.dp).testTag("chat-network-retry"),
                    ) {
                        Text(action, style = mobiMonReferenceTextStyle(36f, 0.65f, true), textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}

@Composable
private fun NetworkIcon(
    checking: Boolean,
    modifier: Modifier,
    scale: Float,
) {
    Box(
        modifier.size(112.dp * scale).background(Colors.raised, RoundedCornerShape(32.dp * scale)),
        contentAlignment = Alignment.Center,
    ) {
        if (checking) {
            Canvas(Modifier.size(52.dp * scale)) {
                drawCircle(Colors.border, style = Stroke(width = 5.dp.toPx() * scale))
            }
            CircularProgressIndicator(Modifier.size(52.dp * scale), color = Colors.accent, strokeWidth = 5.dp * scale)
        } else {
            Icon(
                painterResource(R.drawable.conversation_network_off),
                null,
                Modifier.size(64.dp * scale),
                tint = Color.Unspecified,
            )
        }
    }
}

@Composable
private fun NetworkHomeButton(
    onClick: () -> Unit,
    scale: Float,
    modifier: Modifier,
) {
    var focused by remember { mutableStateOf(false) }
    Surface(
        onClick = onClick,
        modifier = modifier.onFocusChanged { focused = it.isFocused }.testTag("chat-network-home"),
        shape = RoundedCornerShape(58.dp * scale),
        color = Colors.raised,
        border = BorderStroke(if (focused) 3.dp else 1.dp, if (focused) Colors.accent else Colors.border),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                stringResource(R.string.chat_parking_home),
                style = mobiMonReferenceTextStyle(40f, scale, true),
                color = Colors.text,
            )
        }
    }
}
