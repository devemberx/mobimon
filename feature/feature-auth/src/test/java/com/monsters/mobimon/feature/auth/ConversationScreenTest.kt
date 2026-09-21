package com.monsters.mobimon.feature.auth

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.monsters.mobimon.core.domain.ConversationProblem
import com.monsters.mobimon.core.ui.LocalMobiMonMotionEnabled
import com.monsters.mobimon.core.ui.MobiMonTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "ko-rKR-w2560dp-h1332dp-mdpi")
@OptIn(ExperimentalTestApi::class)
class ConversationScreenTest {
    @get:Rule val compose = createComposeRule()
    private var state by mutableStateOf(ConversationUiState(ConversationConnection.READY))
    private var draft by mutableStateOf(TextFieldValue())
    private var allowed by mutableStateOf(true)
    private var height by mutableStateOf(1268.dp)
    private var sends = 0
    private var cancellations = 0
    private lateinit var view: View

    @Test
    fun suggestionsOnlyFillDraftAndExplicitSendCommitsCompositionWithoutDuplicates() {
        show()
        compose.onNodeWithTag("chat-send").assertIsNotEnabled()
        compose.onNodeWithText("오늘 기분 이야기").performClick()
        compose.onNodeWithTag("chat-input").assertIsFocused()
        compose.runOnIdle {
            assertEquals(0, sends)
            assertEquals("오늘 기분 이야기", draft.text)
        }
        compose.onNodeWithTag("chat-input").performImeAction()
        compose.onNodeWithTag("chat-input").performImeAction()
        compose.runOnIdle { assertEquals(1, sends) }
        compose.onNodeWithContentDescription("답변 기다리기 취소").performClick()
        compose.runOnIdle {
            assertEquals(1, cancellations)
            draft = TextFieldValue("한", TextRange(1), TextRange(0, 1))
        }
        compose.onNodeWithTag("chat-input").performImeAction()
        compose.runOnIdle {
            assertEquals(2, sends)
            assertEquals("한", state.messages.single().text)
        }
    }

    @Test
    fun unverifiedConnectionAllowsSendButSignedOutAndRestrictedStatesBlockIt() {
        state = ConversationUiState(ConversationConnection.UNAVAILABLE)
        show()
        compose.onNodeWithTag("chat-input").performTextInput("작성 중")
        compose.onNodeWithTag("chat-send").assertIsEnabled()
        compose.onNodeWithText("Copilot 연결됨").assertDoesNotExist()
        compose.runOnIdle { state = state.copy(connection = ConversationConnection.SIGNED_OUT) }
        compose.onNodeWithTag("chat-send").assertIsNotEnabled()
        compose.runOnIdle { allowed = false }
        compose.onNodeWithTag("chat-input").assertIsNotEnabled()
        compose.runOnIdle {
            assertEquals("작성 중", draft.text)
            assertEquals(0, sends)
        }
    }

    @Test
    fun sendIsReachableUsingKeyboard() {
        draft = TextFieldValue("안녕")
        show()
        compose
            .onNodeWithTag("chat-send")
            .assertHeightIsAtLeast(76.dp)
            .performSemanticsAction(SemanticsActions.RequestFocus)
            .assertIsFocused()
            .performKeyInput { pressKey(Key.Enter) }
        compose.runOnIdle { assertEquals(1, sends) }
    }

    @Test
    fun lostProviderAccessKeepsCompletedRepliesVisibleWithRecovery() {
        state = ConversationUiState(ConversationConnection.UNAVAILABLE, messages = messages)
        show()
        compose.onNodeWithText(messages.last().text).assertIsDisplayed()
        compose.onNodeWithText("Copilot 연결 확인").assertDoesNotExist()
        compose.onNodeWithTag("chat-send").assertIsNotEnabled()
    }

    @Test
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    fun cleanInitialScreenAndRecoveryRemainAccessibleAtAaosDensity() {
        state = ConversationUiState(ConversationConnection.UNAVAILABLE)
        draft = TextFieldValue("보내기 전 초안")
        show(density = 10f / 7f)
        capture("initial-aaos")
        compose.onNodeWithTag("chat-send").assertIsEnabled()
        compose.onNodeWithText("확인하고 Copilot 연결").assertDoesNotExist()
        compose.onNodeWithText("Copilot 연결 확인").assertDoesNotExist()
        compose.onNodeWithText("GitHub 개인정보 처리방침").assertDoesNotExist()
        state = state.copy(connection = ConversationConnection.READY, messages = messages)
        capture("session-actions-aaos")
        compose
            .onNodeWithText("새 대화")
            .assertIsDisplayed()
            .assertHeightIsAtLeast(76.dp)
            .performClick()
        compose.runOnIdle { assertTrue(state.messages.isEmpty()) }
        compose.runOnIdle { state = state.copy(failed = true, problem = ConversationProblem.ACCESS) }
        capture("access-error-aaos")
        compose.onNodeWithText("다시 보내기").assertIsDisplayed()
        compose.runOnIdle { state = state.copy(problem = ConversationProblem.SERVICE) }
        compose.onNodeWithText("Copilot 서비스가 응답하지 못했어요.", substring = true).assertIsDisplayed()
        capture("service-error-aaos")
        compose.runOnIdle { state = state.copy(problem = ConversationProblem.AUTO_UNAVAILABLE) }
        compose.onNodeWithText("이 앱에서 Copilot 자동 선택을 이용할 수 없어요.", substring = true).assertIsDisplayed()
        capture("auto-error-aaos")
    }

    @Test
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    fun initialComposerAndLengthFeedbackWorkWithEnlargedTextAndKeyboard() {
        state = ConversationUiState(ConversationConnection.UNAVAILABLE)
        height = 960.dp
        show(fontScale = 1.6f)
        compose.onNodeWithTag("chat-input").assertIsDisplayed()
        capture("initial-enlarged-keyboard")
        compose.runOnIdle {
            state = state.copy(connection = ConversationConnection.READY)
            draft = TextFieldValue("x".repeat(4001))
        }
        compose.onNodeWithTag("chat-send").assertIsNotEnabled()
        compose.onNodeWithText("메시지를 4,000자 이하로 줄여 주세요.").assertIsDisplayed()
    }

    @Test
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    fun referenceStatesAndKeyboardResizeProduceReviewImages() {
        show()
        capture("empty")
        val companion = compose.onNodeWithTag("chat-companion").fetchSemanticsNode().boundsInRoot
        val panel = compose.onNodeWithTag("chat-panel").fetchSemanticsNode().boundsInRoot
        assertEquals(884f, companion.width, 1f)
        assertEquals(1000f, panel.left, 1f)
        compose.runOnIdle { state = state.copy(messages = messages) }
        capture("messages")
        compose.runOnIdle { state = state.copy(messages = messages.take(1), replyPending = true) }
        capture("reply-pending")
        compose.runOnIdle {
            height = 960.dp
            state =
                state.copy(
                    messages = listOf(ConversationMessage("keyboard", "오늘 하루도 수고했어요.\n어떤 일이 있었는지 들려줄래요?", false)),
                    replyPending = false,
                )
            draft = TextFieldValue("오늘 하루가 조금 힘들었어")
        }
        capture("keyboard-input")
        val resized = compose.onNodeWithTag("chat-panel").fetchSemanticsNode().boundsInRoot
        val composer = compose.onNodeWithTag("chat-composer").fetchSemanticsNode().boundsInRoot
        assertEquals(panel.left, resized.left, 1f)
        assertEquals(panel.top, resized.top, 1f)
        assertTrue(composer.bottom <= resized.bottom)
        assertEquals(712f, resized.height, 1f)
        compose.runOnIdle {
            height = 1268.dp
            state = state.copy(failed = true)
        }
        capture("connection-failed")
        compose.onNodeWithText("내용 수정").performClick()
        compose.onNodeWithTag("chat-input").assertIsDisplayed()
        compose.runOnIdle { assertEquals("오늘 하루가 조금 힘들었어", draft.text) }
    }

    @Test
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    fun enlargedTextKeepsComposerAndControlsVisible() {
        draft = TextFieldValue("긴 메시지를 작성하고 있어요. 줄 바꿈과 선택도 유지해요.")
        show(fontScale = 1.6f)
        compose.onNodeWithTag("chat-input").assertIsDisplayed()
        compose.onNodeWithTag("chat-send").assertIsDisplayed().assertHeightIsAtLeast(76.dp)
        capture("target-enlarged-text")
    }

    @Test
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    fun aaosDensityPreservesReferenceGeometryAndLargeTouchTargets() {
        state = state.copy(messages = messages)
        show(density = 10f / 7f)
        capture("messages-aaos")
        compose.onNodeWithTag("chat-send").assertHeightIsAtLeast(76.dp)
        val panel = compose.onNodeWithTag("chat-panel").fetchSemanticsNode().boundsInRoot
        assertEquals(1000f, panel.left, 1f)
        assertEquals(216f, panel.top, 1f)
        assertEquals(994f, panel.height, 1f)
        val send = compose.onNodeWithTag("chat-send-visual", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
        assertEquals(92f, send.width, 1f)
        compose.runOnIdle {
            height = 960.dp
            state =
                state.copy(
                    messages =
                        listOf(
                            ConversationMessage("keyboard", "오늘 하루도 수고했어요.\n어떤 일이 있었는지 들려줄래요?", false),
                        ),
                )
            draft = TextFieldValue("오늘 하루가 조금 힘들었어")
        }
        capture("keyboard-input-aaos")
        val resized = compose.onNodeWithTag("chat-panel").fetchSemanticsNode().boundsInRoot
        val composer = compose.onNodeWithTag("chat-composer").fetchSemanticsNode().boundsInRoot
        val avatar = compose.onNodeWithTag("chat-avatar").fetchSemanticsNode().boundsInRoot
        assertEquals(712f, resized.height, 1f)
        assertEquals(1056f, composer.left, 1f)
        assertEquals(780f, composer.top, 1f)
        assertEquals(116f, composer.height, 1f)
        assertEquals(240f, avatar.left, 1f)
        assertEquals(548f, avatar.width, 1f)
    }

    private fun show(
        fontScale: Float = 1f,
        density: Float = 1f,
    ) {
        compose.setContent {
            val current = LocalView.current
            SideEffect { view = current }
            CompositionLocalProvider(
                LocalMobiMonMotionEnabled provides false,
                LocalDensity provides Density(density, fontScale),
            ) {
                MobiMonTheme {
                    ConversationScreen(
                        state,
                        draft,
                        { draft = it },
                        { text ->
                            sends++
                            state =
                                state.copy(
                                    messages = listOf(ConversationMessage("sent", text, true)),
                                    replyPending = true,
                                )
                            draft =
                                TextFieldValue()
                        },
                        {
                            cancellations++
                            state = state.copy(replyPending = false)
                        },
                        {},
                        {},
                        Modifier.height(height / density),
                        interactionAllowed = allowed,
                        onDismissFailure = { state = state.copy(failed = false) },
                        onNewConversation = { state = state.copy(messages = emptyList()) },
                    )
                }
            }
        }
    }

    private fun capture(name: String) {
        compose.waitForIdle()
        compose.runOnIdle {
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            view.draw(Canvas(bitmap))
            File("build/reports/conversation-ui").apply { mkdirs() }.resolve("$name.png").outputStream().use {
                assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it))
            }
            bitmap.recycle()
        }
    }

    private val messages =
        listOf(
            ConversationMessage("user", "오늘은 조금 피곤한 하루였어.", true),
            ConversationMessage("reply", "오늘 하루도 수고했어요.\n지금은 잠깐 쉬어 가도 괜찮아요.\n어떤 일이 있었는지 들려줄래요?", false),
        )
}
