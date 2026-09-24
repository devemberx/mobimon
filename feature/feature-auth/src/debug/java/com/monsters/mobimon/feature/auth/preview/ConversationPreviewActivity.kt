package com.monsters.mobimon.feature.auth.preview

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.sp
import com.monsters.mobimon.core.ui.LocalMobiMonMotionEnabled
import com.monsters.mobimon.core.ui.MobiMonColors
import com.monsters.mobimon.core.ui.MobiMonTheme
import com.monsters.mobimon.feature.auth.ConversationConnection
import com.monsters.mobimon.feature.auth.ConversationMessage
import com.monsters.mobimon.feature.auth.ConversationScreen
import com.monsters.mobimon.feature.auth.ConversationUiState

/** Debug-only visual rehearsal. No repositories, credentials, provider or simulated approval. */
class ConversationPreviewActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sample = intent.getStringExtra("state")
        val messages =
            listOf(
                ConversationMessage("sample-user", "오늘은 조금 피곤한 하루였어.", true),
                ConversationMessage("sample-reply", "오늘 하루도 수고했어요.\n지금은 잠깐 쉬어 가도 괜찮아요.\n어떤 일이 있었는지 들려줄래요?", false),
            )
        setContent {
            var state by remember {
                mutableStateOf(
                    ConversationUiState(
                        connection = ConversationConnection.READY,
                        messages =
                            when (sample) {
                                "messages" -> messages
                                "keyboard" ->
                                    listOf(
                                        ConversationMessage(
                                            "sample-keyboard",
                                            "오늘 하루도 수고했어요.\n어떤 일이 있었는지 들려줄래요?",
                                            false,
                                        ),
                                    )
                                "pending" -> messages.take(1)
                                "failed" ->
                                    listOf(
                                        messages.first(),
                                        ConversationMessage(
                                            "sample-reply",
                                            "오늘 하루도 수고했어요.\n잠깐 쉬면서 편하게 이야기해 볼까요?",
                                            false,
                                        ),
                                        ConversationMessage("sample-next", "응, 기분 좋아지는 얘기 해줘.", true),
                                        ConversationMessage(
                                            "sample-next-reply",
                                            "좋아요. 오늘 발견한 작은 행복부터 나눠 볼까요?",
                                            false,
                                        ),
                                        ConversationMessage("sample-failed", "모비는 뭐가 좋아?", true),
                                    )
                                else -> emptyList()
                            },
                        replyPending = sample == "pending",
                        failed = sample == "failed",
                    ),
                )
            }
            var draft by remember {
                mutableStateOf(
                    TextFieldValue(
                        when (sample) {
                            "keyboard" -> "오늘 하루가 조금 힘들었어"
                            "failed" -> "모비는 뭐가 좋아?"
                            else -> ""
                        },
                    ),
                )
            }
            CompositionLocalProvider(LocalMobiMonMotionEnabled provides (sample == "pending")) {
                MobiMonTheme {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(MobiMonColors.background)
                            .safeDrawingPadding()
                            .imePadding(),
                    ) {
                        ConversationScreen(
                            state,
                            draft,
                            { draft = it },
                            { text ->
                                state =
                                    state.copy(
                                        messages =
                                            state.messages +
                                                ConversationMessage("sample-${state.messages.size}", text, true),
                                        replyPending = true,
                                    )
                                draft = TextFieldValue()
                            },
                            { state = state.copy(replyPending = false) },
                            ::finish,
                            {},
                            Modifier.fillMaxSize(),
                            interactionAllowed = true,
                            onRetry = { state = state.copy(failed = false) },
                            onDismissFailure = {
                                state = state.copy(messages = state.messages.dropLast(1), failed = false)
                            },
                            onNewConversation = {
                                state = state.copy(messages = emptyList(), failed = false, replyPending = false)
                                draft = TextFieldValue()
                            },
                        )
                        Text(
                            "DEBUG UI preview · Sample conversation · No provider connection",
                            Modifier.align(Alignment.BottomStart),
                            fontSize = 12.sp,
                            color = MobiMonColors.warning,
                        )
                    }
                }
            }
        }
    }
}
