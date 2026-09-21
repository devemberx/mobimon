package com.monsters.mobimon.feature.auth

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.monsters.mobimon.core.ui.MobiMonNavigationButton
import com.monsters.mobimon.core.ui.MobiMonParkingBadge
import com.monsters.mobimon.core.ui.MobiMonReferenceText
import com.monsters.mobimon.core.ui.PetAvatar
import com.monsters.mobimon.core.ui.mobiMonReferenceTextStyle
import com.monsters.mobimon.core.ui.MobiMonColors as Colors
import com.monsters.mobimon.core.ui.R as CoreUiR

/** Keyboard chat presentation. The caller owns readiness, messages and request lifecycle. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ConversationScreen(
    state: ConversationUiState,
    draft: TextFieldValue,
    onDraftChange: (TextFieldValue) -> Unit,
    onSend: (String) -> Unit,
    onCancelReply: () -> Unit,
    onBack: () -> Unit,
    onOpenConnection: () -> Unit,
    modifier: Modifier = Modifier,
    interactionAllowed: Boolean = false,
    simulatedVehicle: Boolean = false,
    friendId: String = "friend:mobi",
    appearanceKey: String = "GOLDEN",
    accessoryId: String? = null,
    outfitId: String? = null,
    onRetry: () -> Unit = onOpenConnection,
    onDismissFailure: () -> Unit = {},
    onNewConversation: (() -> Unit)? = null,
) {
    val friend = stringResource(if (friendId == "friend:luna") R.string.copilot_luna else R.string.copilot_mobi)
    val title = stringResource(R.string.chat_title)
    val keyboard = LocalSoftwareKeyboardController.current
    val focus = LocalFocusManager.current
    val view = LocalView.current
    val imeVisible = WindowInsets.isImeVisible
    val back = {
        // adjustResize can consume Compose's IME bounds; check the window at the time of the action.
        if (ViewCompat.getRootWindowInsets(view)?.isVisible(WindowInsetsCompat.Type.ime()) == true) {
            keyboard?.hide()
            focus.clearFocus()
        } else {
            onBack()
        }
    }
    BackHandler(enabled = imeVisible) { back() }
    LaunchedEffect(interactionAllowed) {
        if (!interactionAllowed) {
            keyboard?.hide()
            focus.clearFocus()
        }
    }
    BoxWithConstraints(
        modifier
            .fillMaxSize()
            .background(Colors.background)
            .imePadding()
            .semantics { paneTitle = title },
    ) {
        // Width determines reference geometry; the IME changes available height, never the whole screen scale.
        val wide = maxWidth >= 1200.dp && LocalDensity.current.fontScale <= 1.1f
        val scale = if (wide) maxWidth.value / 2560f else 0.65f
        val shortened = maxHeight < 1050.dp * scale
        Column(
            Modifier.fillMaxSize().padding(
                start = if (wide) 72.dp * scale else 24.dp,
                end = if (wide) 72.dp * scale else 24.dp,
                top = if (wide) 56.dp * scale else 16.dp,
                bottom = if (wide) (if (shortened) 32.dp else 58.dp) * scale else 16.dp,
            ),
        ) {
            ConversationHeader(friend, back, interactionAllowed, simulatedVehicle, scale, wide, shortened, state.failed)
            Spacer(Modifier.height(if (wide) 56.dp * scale else 16.dp))
            if (state.failed) {
                ConversationFailure(
                    onRetry,
                    onDismissFailure,
                    interactionAllowed,
                    scale,
                    Modifier.weight(1f),
                    state.problem,
                )
            } else if (wide) {
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(44.dp * scale)) {
                    CompanionConversationPanel(
                        friend,
                        friendId,
                        appearanceKey,
                        accessoryId,
                        outfitId,
                        shortened,
                        scale,
                        Modifier.width(884.dp * scale).fillMaxHeight(),
                    )
                    ConversationPanel(
                        state,
                        draft,
                        onDraftChange,
                        onSend,
                        onCancelReply,
                        onOpenConnection,
                        friend,
                        interactionAllowed,
                        shortened,
                        scale,
                        true,
                        Modifier.weight(1f).fillMaxHeight(),
                        onNewConversation,
                    )
                }
            } else {
                ConversationPanel(
                    state,
                    draft,
                    onDraftChange,
                    onSend,
                    onCancelReply,
                    onOpenConnection,
                    friend,
                    interactionAllowed,
                    shortened,
                    scale,
                    false,
                    Modifier.weight(1f),
                    onNewConversation,
                )
            }
        }
    }
}

@Composable
private fun ConversationHeader(
    friend: String,
    onBack: () -> Unit,
    allowed: Boolean,
    simulated: Boolean,
    scale: Float,
    wide: Boolean,
    shortened: Boolean,
    failed: Boolean,
) {
    Box {
        Row(
            Modifier.fillMaxWidth().heightIn(min = if (wide) 104.dp * scale else 76.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(if (wide) 32.dp * scale else 16.dp),
        ) {
            val backSize = if (wide) 104.dp * scale else 76.dp
            Box(Modifier.size(backSize), contentAlignment = Alignment.Center) {
                MobiMonNavigationButton(
                    painterResource(R.drawable.copilot_back),
                    stringResource(R.string.copilot_back),
                    onBack,
                    modifier = Modifier.requiredSize(backSize.coerceAtLeast(76.dp)),
                    visualSize = backSize,
                    iconSize = 28.dp * scale,
                    borderWidth = 2.dp * scale,
                )
            }
            if (wide) {
                Box(Modifier.weight(1f).height(104.dp * scale)) {
                    MobiMonReferenceText(
                        stringResource(R.string.chat_title),
                        0f,
                        48.2f,
                        46f,
                        scale = scale,
                        bold = true,
                        modifier = Modifier.semantics { heading() },
                    )
                    MobiMonReferenceText(
                        stringResource(if (failed) R.string.chat_failure_subtitle else R.string.chat_subtitle, friend),
                        0f,
                        88.2f,
                        28f,
                        scale = scale,
                        color = Colors.muted,
                    )
                }
            } else {
                Column(Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.chat_title),
                        Modifier.semantics { heading() },
                        style = mobiMonReferenceTextStyle(46f, scale, true),
                        color = Colors.text,
                    )
                    if (!shortened) {
                        Text(
                            stringResource(
                                if (failed) R.string.chat_failure_subtitle else R.string.chat_subtitle,
                                friend,
                            ),
                            style = mobiMonReferenceTextStyle(28f, scale),
                            color = Colors.muted,
                        )
                    }
                }
            }
            if (wide || !shortened) {
                MobiMonParkingBadge(
                    stringResource(
                        if (allowed) {
                            CoreUiR.string.mobimon_parking_confirmed
                        } else {
                            CoreUiR.string.mobimon_parking_unconfirmed
                        },
                    ),
                    scale = scale,
                    showParkingIcon = allowed,
                )
            }
        }
        if (simulated) {
            Text(
                stringResource(R.string.copilot_simulated),
                modifier = Modifier.align(Alignment.BottomStart).offset(y = 32.dp * scale),
                style = mobiMonReferenceTextStyle(20f, scale),
                color = Colors.warning,
            )
        }
    }
}

@Composable
private fun CompanionConversationPanel(
    friend: String,
    friendId: String,
    appearanceKey: String,
    accessoryId: String?,
    outfitId: String?,
    shortened: Boolean,
    scale: Float,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier.background(Colors.panel, RoundedCornerShape(48.dp * scale)).testTag("chat-companion")) {
        val avatarSize =
            minOf(656.dp * scale, (maxHeight - (if (shortened) 164.dp else 144.dp) * scale).coerceAtLeast(0.dp))
        Text(
            stringResource(R.string.chat_companion_title, friend),
            Modifier.fillMaxWidth().padding(top = (if (shortened) 40.dp else 58.dp) * scale),
            style = mobiMonReferenceTextStyle(if (shortened) 44f else 48f, scale, true),
            color = Colors.text,
            textAlign = TextAlign.Center,
        )
        if (!shortened) {
            MobiMonReferenceText(
                stringResource(R.string.chat_companion_note),
                0f,
                174.2f,
                30f,
                modifier = Modifier.align(Alignment.TopCenter),
                scale = scale,
                color = Colors.muted,
            )
        }
        PetAvatar(
            Modifier
                .align(
                    Alignment.TopCenter,
                ).offset(y = (if (shortened) 120.dp else 188.dp) * scale)
                .size(avatarSize)
                .testTag("chat-avatar"),
            appearanceKey,
            friendId,
            accessoryId,
            outfitId,
        )
        if (!shortened) {
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 46.dp * scale)
                    .size(320.dp * scale, 60.dp * scale)
                    .background(Colors.raised, RoundedCornerShape(50)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    stringResource(R.string.chat_companion_tagline),
                    style = mobiMonReferenceTextStyle(26f, scale),
                    color = Colors.accent,
                )
            }
        }
    }
}
