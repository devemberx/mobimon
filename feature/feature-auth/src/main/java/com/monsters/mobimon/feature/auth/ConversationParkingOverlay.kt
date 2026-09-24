package com.monsters.mobimon.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.monsters.mobimon.core.ui.MobiMonButton
import com.monsters.mobimon.core.ui.MobiMonReferenceText
import com.monsters.mobimon.core.ui.mobiMonReferenceTextStyle
import com.monsters.mobimon.core.ui.MobiMonColors as Colors

/** App-content overlay matching the parking-required Figma frame. */
@Composable
internal fun ConversationParkingOverlay(onHome: () -> Unit) {
    val title = stringResource(R.string.chat_parking_title)
    BoxWithConstraints(Modifier.fillMaxSize().semantics { paneTitle = title }) {
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
                    .testTag("chat-parking-dialog"),
            ) {
                ParkingPauseIcon(Modifier.offset(64.dp * scale, 64.dp * scale), scale)
                MobiMonReferenceText(
                    title,
                    64f,
                    258f,
                    48f,
                    scale = scale,
                    bold = true,
                    modifier = Modifier.semantics { heading() },
                )
                MobiMonReferenceText(
                    stringResource(R.string.chat_parking_body),
                    64f,
                    336f,
                    36f,
                    scale = scale,
                    color = Colors.muted,
                )
                MobiMonReferenceText(
                    stringResource(R.string.chat_parking_instruction),
                    64f,
                    390f,
                    36f,
                    scale = scale,
                    color = Colors.muted,
                )
                MobiMonReferenceText(
                    stringResource(R.string.chat_parking_preserved),
                    64f,
                    466f,
                    28f,
                    scale = scale,
                    color = Colors.muted,
                )
                MobiMonButton(
                    onClick = onHome,
                    modifier =
                        Modifier
                            .offset(64.dp * scale, 560.dp * scale)
                            .size(1232.dp * scale, 116.dp * scale)
                            .testTag("chat-parking-home"),
                ) {
                    Text(
                        stringResource(R.string.chat_parking_home),
                        style = mobiMonReferenceTextStyle(40f, scale, true),
                    )
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
                    .testTag("chat-parking-dialog"),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                ParkingPauseIcon(Modifier, 0.65f)
                Text(
                    title,
                    style =
                        mobiMonReferenceTextStyle(
                            48f,
                            0.65f,
                            true,
                        ),
                    color = Colors.text,
                    modifier =
                        Modifier.semantics {
                            heading()
                        },
                )
                Text(
                    stringResource(R.string.chat_parking_body) + "\n" +
                        stringResource(R.string.chat_parking_instruction),
                    style = mobiMonReferenceTextStyle(36f, 0.65f),
                    color = Colors.muted,
                )
                Text(
                    stringResource(R.string.chat_parking_preserved),
                    style = mobiMonReferenceTextStyle(28f, 0.65f),
                    color = Colors.muted,
                )
                Spacer(Modifier.height(8.dp))
                MobiMonButton(
                    onClick = onHome,
                    modifier = Modifier.fillMaxWidth().height(76.dp).testTag("chat-parking-home"),
                ) {
                    Text(
                        stringResource(R.string.chat_parking_home),
                        style = mobiMonReferenceTextStyle(40f, 0.65f, true),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun ParkingPauseIcon(
    modifier: Modifier,
    scale: Float,
) {
    Box(
        modifier
            .size(112.dp * scale)
            .background(Colors.raised, RoundedCornerShape(32.dp * scale)),
    ) {
        Box(
            Modifier
                .offset(36.dp * scale, 30.dp * scale)
                .size(12.dp * scale, 52.dp * scale)
                .background(Colors.destructive, RoundedCornerShape(6.dp * scale)),
        )
        Box(
            Modifier
                .offset(64.dp * scale, 30.dp * scale)
                .size(12.dp * scale, 52.dp * scale)
                .background(Colors.destructive, RoundedCornerShape(6.dp * scale)),
        )
    }
}
