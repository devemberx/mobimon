package com.monsters.mobimon.feature.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monsters.mobimon.core.ui.mobiMonReferenceTextStyle
import com.monsters.mobimon.core.ui.MobiMonColors as Colors

@Composable
internal fun ConversationPanel(
    state: ConversationUiState,
    draft: TextFieldValue,
    onDraftChange: (TextFieldValue) -> Unit,
    onSend: (String) -> Unit,
    onCancelReply: () -> Unit,
    onOpenConnection: () -> Unit,
    friend: String,
    allowed: Boolean,
    shortened: Boolean,
    scale: Float,
    wide: Boolean,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    val chooseSuggestion: (String) -> Unit = { text ->
        if (allowed && !state.replyPending) {
            onDraftChange(TextFieldValue(text, TextRange(text.length)))
            focusRequester.requestFocus()
            keyboard?.show()
        }
    }
    Column(
        modifier
            .fillMaxWidth()
            .background(Colors.panel, RoundedCornerShape(48.dp * scale))
            .testTag("chat-panel")
            .padding(
                start = (if (wide) 56.dp else 24.dp) * scale,
                end = (if (wide) 72.dp else 24.dp) * scale,
                top = (if (wide) 32.dp else 24.dp) * scale,
                bottom = (if (shortened) 32.dp else 20.dp) * scale,
            ),
    ) {
        Row(Modifier.fillMaxWidth().heightIn(min = 60.dp * scale), verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.chat_today),
                Modifier.weight(1f).semantics { heading() },
                style = mobiMonReferenceTextStyle(36f, scale, true),
                color = Colors.text,
            )
            Box(
                Modifier
                    .then(if (wide) Modifier.size(244.dp * scale, 60.dp * scale) else Modifier)
                    .background(Colors.raised, RoundedCornerShape(50))
                    .padding(
                        horizontal = (if (wide) 0.dp else 24.dp) * scale,
                        vertical =
                            10.dp * scale,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    stringResource(
                        when (state.connection) {
                            ConversationConnection.READY -> R.string.chat_ready
                            ConversationConnection.SIGNED_OUT -> R.string.chat_signed_out
                            ConversationConnection.UNAVAILABLE -> R.string.chat_unavailable
                        },
                    ),
                    style = mobiMonReferenceTextStyle(28f, scale),
                    color = if (state.connection == ConversationConnection.READY) Colors.success else Colors.muted,
                )
            }
        }
        Box(Modifier.weight(1f).fillMaxWidth().clipToBounds()) {
            if (state.messages.isEmpty() && !state.replyPending) {
                ConversationEmpty(scale, shortened, Modifier.fillMaxSize())
            } else {
                ConversationMessages(state, friend, scale, shortened, Modifier.fillMaxSize())
            }
        }
        if (!shortened && !state.replyPending) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp * scale)) {
                val suggestions =
                    if (state.messages.isEmpty()) {
                        listOf(R.string.chat_suggestion_mood, R.string.chat_suggestion_story)
                    } else {
                        listOf(R.string.chat_suggestion_followup)
                    }
                if (state.messages.isNotEmpty() && wide) Spacer(Modifier.weight(1f))
                suggestions.forEach { resource ->
                    val text = stringResource(resource)
                    ConversationAction(
                        text,
                        { chooseSuggestion(text) },
                        allowed,
                        scale,
                        Modifier.weight(1f),
                        referenceGeometry = wide,
                    )
                }
            }
            Spacer(Modifier.height(24.dp * scale))
        }
        ConversationComposer(
            state,
            draft,
            onDraftChange,
            onSend,
            onCancelReply,
            friend,
            allowed,
            scale,
            wide,
            focusRequester,
        )
        if (!shortened) {
            if (state.connection == ConversationConnection.READY) {
                Text(
                    stringResource(R.string.chat_disclaimer),
                    Modifier.padding(top = 12.dp * scale),
                    style = mobiMonReferenceTextStyle(24f, scale).copy(lineHeight = (32f * scale).sp),
                    color = Colors.muted,
                )
            } else {
                Text(
                    stringResource(
                        if (state.connection ==
                            ConversationConnection.SIGNED_OUT
                        ) {
                            R.string.chat_sign_in_note
                        } else {
                            R.string.chat_provider_note
                        },
                    ),
                    Modifier.padding(top = 12.dp * scale),
                    style = mobiMonReferenceTextStyle(24f, scale).copy(lineHeight = (32f * scale).sp),
                    color = Colors.muted,
                )
            }
        }
        if (state.connection == ConversationConnection.SIGNED_OUT && !shortened) {
            ConversationAction(
                stringResource(R.string.conversation_connect),
                onOpenConnection,
                allowed,
                scale,
                Modifier.fillMaxWidth().padding(top = 12.dp * scale),
            )
        }
    }
}

@Composable
private fun ConversationEmpty(
    scale: Float,
    shortened: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier.verticalScroll(rememberScrollState()).offset(x = 16.dp * scale).padding(
            top =
                (if (shortened) 20.dp else 100.dp) * scale,
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (!shortened) {
            Box(
                Modifier.size(112.dp * scale).background(Colors.raised, RoundedCornerShape(36.dp * scale)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(painterResource(R.drawable.copilot_chat), null, Modifier.size(56.dp * scale), tint = Colors.accent)
            }
            Spacer(Modifier.height(56.dp * scale))
        }
        Text(
            stringResource(R.string.chat_empty_title),
            style = mobiMonReferenceTextStyle(if (shortened) 36f else 52f, scale, true),
            color = Colors.text,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(16.dp * scale))
        Text(
            stringResource(R.string.chat_empty_note),
            style = mobiMonReferenceTextStyle(32f, scale),
            color = Colors.muted,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ConversationMessages(
    state: ConversationUiState,
    friend: String,
    scale: Float,
    shortened: Boolean,
    modifier: Modifier = Modifier,
) {
    val scroll = rememberLazyListState()
    val count = state.messages.size + if (state.replyPending) 1 else 0
    LaunchedEffect(count, shortened) {
        // BoxWithConstraints can launch this during measurement; scrolling forces a remeasure.
        withFrameNanos { }
        if (count > 0) scroll.scrollToItem(count - 1)
    }
    LazyColumn(
        modifier.testTag("chat-messages"),
        state = scroll,
        contentPadding = PaddingValues(top = (if (shortened) 4.dp else 16.dp) * scale, bottom = 24.dp * scale),
        verticalArrangement = Arrangement.spacedBy(54.dp * scale),
    ) {
        items(state.messages, key = { it.id }) { message ->
            MessageBubble(message.text, message.fromUser, friend, scale, shortened = shortened)
        }
        if (state.replyPending) {
            item(key = "reply-pending") {
                MessageBubble(stringResource(R.string.chat_preparing, friend), false, friend, scale, pending = true)
            }
        }
    }
}

@Composable
private fun MessageBubble(
    text: String,
    fromUser: Boolean,
    friend: String,
    scale: Float,
    pending: Boolean = false,
    shortened: Boolean = false,
) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = if (fromUser) Alignment.End else Alignment.Start) {
        Text(
            if (fromUser) stringResource(R.string.chat_user) else friend,
            Modifier.padding(horizontal = 16.dp * scale),
            style = mobiMonReferenceTextStyle(28f, scale, true),
            color = if (fromUser) Colors.muted else Colors.accent,
        )
        Spacer(Modifier.height(16.dp * scale))
        Column(
            Modifier
                .fillMaxWidth(if (fromUser) 960f / 1360 else 1192f / 1360)
                .heightIn(
                    min =
                        (
                            if (pending) {
                                208.dp
                            } else if (shortened && !fromUser) {
                                156.dp
                            } else {
                                0.dp
                            }
                        ) * scale,
                ).background(if (fromUser) Colors.button else Colors.raised, RoundedCornerShape(36.dp * scale))
                .padding(
                    horizontal = 44.dp * scale,
                    vertical =
                        (
                            if (fromUser) {
                                38.dp
                            } else if (shortened) {
                                22.dp
                            } else {
                                28.dp
                            }
                        ) * scale,
                ).semantics { if (pending) liveRegion = LiveRegionMode.Polite },
        ) {
            if (pending) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(32.dp * scale),
                    modifier = Modifier.padding(top = 26.dp * scale, bottom = 44.dp * scale),
                ) {
                    repeat(3) { Box(Modifier.size(16.dp * scale).background(Colors.accent, CircleShape)) }
                }
            }
            SelectionContainer {
                Text(
                    text,
                    style =
                        mobiMonReferenceTextStyle(
                            if (shortened) 34f else 36f,
                            scale,
                        ).copy(lineHeight = ((if (shortened) 56f else 60f) * scale).sp),
                    color = if (fromUser) Colors.onButton else Colors.text,
                )
            }
        }
    }
}

@Composable
private fun ConversationComposer(
    state: ConversationUiState,
    draft: TextFieldValue,
    onDraftChange: (TextFieldValue) -> Unit,
    onSend: (String) -> Unit,
    onCancelReply: () -> Unit,
    friend: String,
    allowed: Boolean,
    scale: Float,
    wide: Boolean,
    focusRequester: FocusRequester,
) {
    val canSend =
        allowed &&
            state.connection == ConversationConnection.READY &&
            !state.replyPending &&
            draft.text.isNotBlank()
    val submit = {
        if (canSend) {
            // Explicit Send commits the visible syllable/word without discarding IME input.
            onDraftChange(draft.copy(composition = null))
            onSend(draft.text)
        }
    }
    val label = stringResource(R.string.chat_input)
    val actionLabel = stringResource(if (state.replyPending) R.string.chat_stop else R.string.chat_send)
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 116.dp * scale)
            .testTag("chat-composer")
            .background(Colors.background, RoundedCornerShape(58.dp * scale))
            .border(
                2.dp * scale,
                Colors.border,
                RoundedCornerShape(
                    58.dp * scale,
                ),
            ).padding(start = 44.dp * scale, end = 28.dp * scale, top = 12.dp * scale, bottom = 12.dp * scale),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp * scale),
    ) {
        BasicTextField(
            value = draft,
            onValueChange = { if (allowed && !state.replyPending) onDraftChange(it) },
            modifier =
                Modifier
                    .weight(1f)
                    .heightIn(min = 64.dp * scale)
                    .focusRequester(focusRequester)
                    .testTag(
                        "chat-input",
                    ).semantics { contentDescription = label },
            enabled = allowed,
            readOnly = state.replyPending,
            textStyle = mobiMonReferenceTextStyle(32f, scale).copy(color = Colors.text),
            cursorBrush = SolidColor(Colors.accent),
            maxLines = 3,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { submit() }),
            decorationBox = { input ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (draft.text.isEmpty()) {
                        Text(
                            stringResource(
                                if (state.replyPending) R.string.chat_waiting else R.string.chat_placeholder,
                                friend,
                            ),
                            style = mobiMonReferenceTextStyle(32f, scale),
                            color = Colors.muted,
                        )
                    }
                    input()
                }
            },
        )
        var sendFocused by remember { mutableStateOf(false) }
        val enabled = if (state.replyPending) allowed else canSend
        val visualSize = 92.dp * scale
        val targetSize = visualSize.coerceAtLeast(76.dp)
        Box(Modifier.size(if (wide) visualSize else targetSize), contentAlignment = Alignment.Center) {
            Surface(
                onClick = {
                    if (state.replyPending) {
                        onCancelReply()
                    } else {
                        submit()
                    }
                },
                enabled = enabled,
                modifier =
                    Modifier
                        .requiredSize(targetSize)
                        .onFocusChanged { sendFocused = it.isFocused }
                        .testTag("chat-send")
                        .semantics { contentDescription = actionLabel },
                shape = CircleShape,
                color = Color.Transparent,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Surface(
                        Modifier.size(visualSize).testTag("chat-send-visual"),
                        shape = CircleShape,
                        color = if (enabled) Colors.button else Color(0xFF33465B),
                        border = if (sendFocused) BorderStroke(3.dp, Colors.accent) else null,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (state.replyPending) {
                                Box(
                                    Modifier.size(32.dp * scale).background(
                                        Colors.onButton,
                                        RoundedCornerShape(
                                            3.dp * scale,
                                        ),
                                    ),
                                )
                            } else {
                                Icon(
                                    painterResource(R.drawable.conversation_send),
                                    stringResource(R.string.chat_send),
                                    Modifier.size(40.dp * scale),
                                    tint = if (enabled) Colors.onButton else Colors.muted,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun ConversationAction(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean,
    scale: Float,
    modifier: Modifier = Modifier,
    referenceGeometry: Boolean = false,
) {
    var focused by remember { mutableStateOf(false) }
    val visualHeight = 80.dp * scale
    Box(
        modifier.then(if (referenceGeometry) Modifier.height(visualHeight) else Modifier.heightIn(min = 76.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .then(if (referenceGeometry) Modifier.requiredHeight(visualHeight.coerceAtLeast(76.dp)) else Modifier)
                .onFocusChanged { focused = it.isFocused }
                .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .then(if (referenceGeometry) Modifier.height(visualHeight) else Modifier.heightIn(min = 76.dp))
                    .border(
                        if (focused) 3.dp else 2.dp * scale,
                        if (focused) Colors.accent else Colors.border,
                        RoundedCornerShape(50),
                    ).padding(horizontal = 24.dp * scale, vertical = 12.dp * scale),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text,
                    style = mobiMonReferenceTextStyle(30f, scale),
                    color = if (enabled) Colors.text else Colors.muted,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
