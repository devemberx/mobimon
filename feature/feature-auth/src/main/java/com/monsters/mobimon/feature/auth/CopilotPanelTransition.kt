package com.monsters.mobimon.feature.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics

@Composable
internal fun CopilotPanelTransition(
    state: CopilotUiState,
    onAction: (CopilotAction) -> Unit,
    modifier: Modifier = Modifier,
    interactionAllowed: Boolean = false,
    content: @Composable (CopilotUiState, (CopilotAction) -> Unit) -> Unit,
) {
    // Expired credentials and lost authorization must disappear without an outgoing frame.
    if (!interactionAllowed ||
        state is CopilotUiState.Expired ||
        state is CopilotUiState.Reconnect ||
        state is CopilotUiState.AccessCheck
    ) {
        Box(modifier) { content(state, onAction) }
        return
    }
    val latestState by rememberUpdatedState(state)
    val latestAction by rememberUpdatedState(onAction)
    AnimatedContent(
        targetState = state,
        modifier = modifier,
        contentKey = { it.panelKey() },
        contentAlignment = Alignment.TopStart,
        transitionSpec = { (fadeIn(tween(180)) togetherWith fadeOut(tween(120))).using(null) },
        label = "Copilot panel",
    ) { panelState ->
        val active = panelState == latestState
        Box(if (active) Modifier else Modifier.clearAndSetSemantics {}) {
            content(panelState) { action ->
                if (panelState == latestState) latestAction(action)
            }
        }
    }
}

private fun CopilotUiState.panelKey(): String =
    when (this) {
        is CopilotUiState.Introduction -> "introduction"
        is CopilotUiState.Waiting -> if (showAddress) "address" else "qr"
        CopilotUiState.Expired -> "expired"
        is CopilotUiState.Connected -> "connected"
        is CopilotUiState.Reconnect -> "reconnect"
        is CopilotUiState.AccessCheck -> "access"
        is CopilotUiState.Disconnect -> "disconnect"
    }
