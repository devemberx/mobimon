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
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performScrollTo
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
@Config(sdk = [34], qualifiers = "ko-rKR-w2560dp-h1248dp-mdpi")
@OptIn(ExperimentalTestApi::class)
class ConversationScreenTest {
    @get:Rule val compose = createComposeRule()
    private var state by mutableStateOf(ConversationUiState(ConversationConnection.READY))
    private var draft by mutableStateOf(TextFieldValue())
    private var allowed by mutableStateOf(true)
    private var height by mutableStateOf(1184.dp)
    private var sends = 0
    private var cancellations = 0
    private var homeReturns = 0
    private var checks = 0
    private var retries = 0
    private var connectionOpens = 0
    private lateinit var view: View

    @Test
    fun restrictedConversationShowsParkingNoticeInHeader() {
        allowed = false
        show()
        val badge =
            compose
                .onNodeWithContentDescription(
                    "주차 후 이용 가능",
                ).assertIsDisplayed()
                .fetchSemanticsNode()
                .boundsInRoot
        assertEquals(36f, badge.top, 1f)
        assertEquals(2488f, badge.right, 1f)
    }

    @Test
    fun suggestionsOnlyFillDraftAndExplicitSendCommitsCompositionWithoutDuplicates() {
        show()
        compose.onNodeWithTag("chat-send").assertIsNotEnabled()
        compose.onNodeWithText("오늘 하루 이야기할래").performClick()
        compose.onNodeWithTag("chat-input").assertIsFocused()
        compose.runOnIdle {
            assertEquals(0, sends)
            assertEquals("오늘 하루 이야기할래", draft.text)
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
    fun unverifiedConnectionBlocksSendAndSignedOutAndRestrictedStatesBlockIt() {
        state = ConversationUiState(ConversationConnection.UNAVAILABLE)
        show()
        compose.onNodeWithTag("chat-input").performTextInput("작성 중")
        compose.onNodeWithTag("chat-send").assertIsNotEnabled()
        compose.onNodeWithText("Copilot 연결됨").assertDoesNotExist()
        compose.runOnIdle { state = state.copy(connection = ConversationConnection.SIGNED_OUT) }
        compose.onNodeWithTag("chat-auth-badge").assertContentDescriptionEquals("계정 연결 필요")
        compose.onNodeWithTag("chat-send").assertIsNotEnabled()
        compose.runOnIdle { allowed = false }
        compose.onNodeWithTag("chat-parking-dialog").assertIsDisplayed()
        compose.onNodeWithTag("chat-input").assertDoesNotExist()
        compose.runOnIdle {
            assertEquals("작성 중", draft.text)
            assertEquals(0, sends)
        }
    }

    @Test
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    fun networkFailureShowsModalAndRecheckNeverSendsDraft() {
        state = ConversationUiState(ConversationConnection.UNAVAILABLE, connectionProblem = ConversationProblem.NETWORK)
        draft = TextFieldValue("작성 중")
        show()
        capture("network-error")
        compose.onNodeWithTag("chat-network-dialog").assertIsDisplayed()
        compose.onNodeWithText("네트워크 연결을 확인해 주세요").assertIsDisplayed()
        compose.onNodeWithTag("chat-send").assertDoesNotExist()
        compose.onNodeWithTag("chat-network-retry").performClick()
        compose.runOnIdle {
            assertEquals(1, checks)
            assertEquals(0, sends)
            assertEquals("작성 중", draft.text)
        }
        compose.onNodeWithText("Copilot 연결을 다시 확인하는 중").assertIsDisplayed()
        compose.onNodeWithTag("chat-network-retry").assertIsNotEnabled()
        compose.onNodeWithTag("chat-network-home").assertIsEnabled()
        capture("network-checking")
        compose.runOnIdle { state = state.copy(connection = ConversationConnection.READY, connectionRetrying = false) }
        compose.onNodeWithTag("chat-network-dialog").assertDoesNotExist()
        compose.onNodeWithTag("chat-send").assertIsEnabled()
    }

    @Test
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    fun failedMessageNetworkErrorKeepsRetryAndEditReachable() {
        state =
            ConversationUiState(
                ConversationConnection.READY,
                messages = listOf(ConversationMessage("attempt", "다시 보낼 내용", true)),
                failed = true,
                problem = ConversationProblem.NETWORK,
            )
        draft = TextFieldValue("다시 보낼 내용")
        show()
        capture("message-network-failed")
        compose.onNodeWithTag("chat-network-dialog").assertDoesNotExist()
        compose.onNodeWithTag("chat-user-bubble").assertIsDisplayed()
        compose.onNodeWithText("다시 보내기").performClick()
        compose.runOnIdle {
            assertEquals(1, retries)
            assertEquals(0, checks)
        }
        compose.onNodeWithText("내용 수정").performClick()
        compose.runOnIdle { assertEquals("다시 보낼 내용", draft.text) }
    }

    @Test
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    fun accessCheckFailureExplainsHowToRecheckAfterChangingPermissions() {
        state = ConversationUiState(ConversationConnection.UNAVAILABLE, connectionProblem = ConversationProblem.ACCESS)
        show()
        capture("connection-access")
        compose.onNodeWithText("GitHub가 이 앱의 대화 요청을 허용하지 않았어요.", substring = true).assertIsDisplayed()
        compose.onNodeWithTag("chat-send").assertDoesNotExist()
        val icon =
            compose
                .onNodeWithTag(
                    "chat-connection-icon",
                    useUnmergedTree = true,
                ).fetchSemanticsNode()
                .boundsInRoot
        assertEquals(112f, icon.width, 1f)
        val body = compose.onNodeWithTag("chat-connection-body").fetchSemanticsNode().boundsInRoot
        val instruction = compose.onNodeWithTag("chat-connection-instruction").fetchSemanticsNode().boundsInRoot
        val preserved = compose.onNodeWithTag("chat-connection-preserved").fetchSemanticsNode().boundsInRoot
        assertEquals(104f, instruction.top - body.top, 1f)
        assertEquals(87f, preserved.top - instruction.top, 1f)
        compose.onNodeWithText("다시 확인").performClick()
        compose.runOnIdle { assertEquals(1, checks) }
    }

    @Test
    fun accountCheckFailureOpensConnectionManagement() {
        state = ConversationUiState(ConversationConnection.UNAVAILABLE, connectionProblem = ConversationProblem.ACCOUNT)
        show()
        compose.onNodeWithText("연결 안내 열기").performClick()
        compose.runOnIdle { assertEquals(1, connectionOpens) }
    }

    @Test
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    fun providerCheckFailureExplainsErrorAndOffersRecheck() {
        state =
            ConversationUiState(ConversationConnection.UNAVAILABLE, connectionProblem = ConversationProblem.PROVIDER)
        show()
        capture("connection-provider")
        compose.onNodeWithText("Copilot이 지원하지 않는 응답을 보냈어요.", substring = true).assertIsDisplayed()
        compose.onNodeWithText("다시 확인").performClick()
        compose.runOnIdle { assertEquals(1, checks) }
    }

    @Test
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    fun parkingLossShowsBlockingDialogAndHomeKeepsDraft() {
        draft = TextFieldValue("주차 후 보낼 메시지")
        allowed = false
        show()

        capture("parking-required")
        compose.onNodeWithText("주차 후 대화를 이어가요").assertIsDisplayed()
        compose.onNodeWithText("대화와 전송은 주차 상태에서만 사용할 수 있어요.").assertIsDisplayed()
        val dialog = compose.onNodeWithTag("chat-parking-dialog").fetchSemanticsNode().boundsInRoot
        assertEquals(600f, dialog.left, 1f)
        assertEquals(254f, dialog.top, 1f)
        assertEquals(1360f, dialog.width, 1f)
        assertEquals(740f, dialog.height, 1f)
        val badge =
            compose
                .onNodeWithContentDescription("주차 후 이용 가능")
                .fetchSemanticsNode()
                .boundsInRoot
        assertEquals(2048f, badge.left, 1f)
        assertEquals(36f, badge.top, 1f)
        assertEquals(440f, badge.width, 1f)
        compose.onNodeWithTag("chat-send").assertDoesNotExist()
        compose.onNodeWithTag("chat-parking-home").assertIsDisplayed().performClick()
        compose.runOnIdle {
            assertEquals(1, homeReturns)
            assertEquals("주차 후 보낼 메시지", draft.text)
            allowed = true
        }
        compose.onNodeWithTag("chat-parking-dialog").assertDoesNotExist()
    }

    @Test
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    fun parkingDialogKeepsHomeReachableWithEnlargedTextAtAaosDensity() {
        allowed = false
        show(fontScale = 1.6f, density = 10f / 7f)

        compose.onNodeWithText("주차 후 대화를 이어가요").assertIsDisplayed()
        compose
            .onNodeWithTag("chat-parking-home")
            .performScrollTo()
            .assertIsDisplayed()
            .assertHeightIsAtLeast(76.dp)
        capture("parking-required-enlarged-aaos")
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
        compose.onNodeWithTag("chat-send").assertIsNotEnabled()
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
        compose.onNodeWithText("다시 확인").assertIsDisplayed()
        compose.runOnIdle { state = state.copy(problem = ConversationProblem.SERVICE) }
        compose.onNodeWithText("Copilot 서비스가 응답하지 못했어요.", substring = true).assertIsDisplayed()
        capture("service-error-aaos")
    }

    @Test
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    fun initialComposerAndLengthFeedbackWorkWithEnlargedTextAndKeyboard() {
        state = ConversationUiState(ConversationConnection.UNAVAILABLE)
        height = 940.dp
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
        compose.onNodeWithText("모비와의 대화").assertIsDisplayed()
        compose.onNodeWithText("오늘도 함께 쉬어 가요.").assertIsDisplayed()
        compose.onNodeWithText("오늘 하루 이야기할래").assertIsDisplayed()
        compose.onNodeWithText("기분 좋아지는 얘기 해줘").assertIsDisplayed()
        compose.onNodeWithText("모비는 오늘 어땠어?").assertIsDisplayed()
        val companion = compose.onNodeWithTag("chat-companion").fetchSemanticsNode().boundsInRoot
        val panel = compose.onNodeWithTag("chat-panel").fetchSemanticsNode().boundsInRoot
        assertEquals(72f, companion.left, 1f)
        assertEquals(196f, companion.top, 1f)
        assertEquals(680f, companion.width, 1f)
        assertEquals(964f, companion.height, 1f)
        assertEquals(796f, panel.left, 1f)
        assertEquals(34f, panel.top, 1f)
        assertEquals(1692f, panel.width, 1f)
        assertEquals(1126f, panel.height, 1f)
        val avatar = compose.onNodeWithTag("chat-avatar").fetchSemanticsNode().boundsInRoot
        assertEquals(100f, avatar.left, 1f)
        assertEquals(336f, avatar.top, 1f)
        assertEquals(624f, avatar.width, 1f)
        val parking = compose.onNodeWithContentDescription("주차 확인됨").fetchSemanticsNode().boundsInRoot
        assertEquals(2048f, parking.left, 1f)
        assertEquals(36f, parking.top, 1f)
        assertEquals(440f, parking.width, 1f)
        assertEquals(76f, parking.height, 1f)
        val auth = compose.onNodeWithTag("chat-auth-badge").fetchSemanticsNode().boundsInRoot
        compose.onNodeWithTag("chat-auth-badge").assertContentDescriptionEquals("Copilot 연결됨")
        assertEquals(1776f, auth.left, 1f)
        assertEquals(51f, auth.top, 1f)
        assertEquals(244f, auth.width, 1f)
        assertEquals(60f, auth.height, 1f)
        compose.runOnIdle { state = state.copy(messages = referenceMessages) }
        capture("messages")
        val userBubble =
            compose
                .onAllNodesWithTag("chat-user-bubble")
                .onFirst()
                .fetchSemanticsNode()
                .boundsInRoot
        val friendBubble =
            compose
                .onAllNodesWithTag("chat-friend-bubble")
                .onFirst()
                .fetchSemanticsNode()
                .boundsInRoot
        assertTrue(userBubble.width < 500f)
        assertTrue(friendBubble.width < 900f)
        compose.runOnIdle {
            state =
                state.copy(
                    messages = referenceMessages + ConversationMessage("last-user", "모비는 뭐가 좋아?", true),
                    replyPending = true,
                )
        }
        capture("reply-pending")
        compose.onNodeWithText("오늘은 조금 피곤한 하루였어.").assertIsDisplayed()
        val firstPendingBubble =
            compose
                .onAllNodesWithTag(
                    "chat-user-bubble",
                ).onFirst()
                .fetchSemanticsNode()
                .boundsInRoot
        assertTrue(firstPendingBubble.top > 180f)
        val pendingBubble = compose.onNodeWithTag("chat-pending-bubble").fetchSemanticsNode().boundsInRoot
        assertTrue(pendingBubble.width < 200f)
        compose.runOnIdle {
            height = 940.dp
            state =
                state.copy(
                    messages = referenceMessages,
                    replyPending = false,
                )
            draft = TextFieldValue("모비는 뭐가 좋아?")
        }
        capture("keyboard-input")
        compose.onNodeWithText("오늘은 조금 피곤한 하루였어.").assertIsDisplayed()
        compose.onNodeWithText("대화는 GitHub Copilot으로 전송돼요. AI 답변은 부정확할 수 있어요.").assertIsDisplayed()
        val resized = compose.onNodeWithTag("chat-panel").fetchSemanticsNode().boundsInRoot
        val composer = compose.onNodeWithTag("chat-composer").fetchSemanticsNode().boundsInRoot
        assertEquals(panel.left, resized.left, 1f)
        assertEquals(panel.top, resized.top, 1f)
        assertTrue(composer.bottom <= resized.bottom)
        assertEquals(882f, resized.height, 1f)
        assertEquals(796f, resized.left, 1f)
        assertEquals(34f, resized.top, 1f)
        assertEquals(916f, resized.bottom, 1f)
        compose.runOnIdle {
            height = 1184.dp
            state =
                state.copy(
                    messages = referenceMessages + ConversationMessage("last-user", "모비는 뭐가 좋아?", true),
                    failed = true,
                )
        }
        capture("connection-failed")
        compose.onNodeWithText("오늘은 조금 피곤한 하루였어.").assertIsDisplayed()
        compose.onNodeWithTag("chat-companion").assertIsDisplayed()
        compose.onNodeWithTag("chat-panel").assertIsDisplayed()
        val failure = compose.onNodeWithTag("chat-inline-failure").fetchSemanticsNode().boundsInRoot
        assertEquals(852f, failure.left, 1f)
        assertEquals(929f, failure.top, 1f)
        val retry =
            compose
                .onNodeWithTag(
                    "chat-inline-retry-visual",
                    useUnmergedTree = true,
                ).fetchSemanticsNode()
                .boundsInRoot
        val edit =
            compose
                .onNodeWithTag(
                    "chat-inline-edit-visual",
                    useUnmergedTree = true,
                ).fetchSemanticsNode()
                .boundsInRoot
        assertEquals(1974f, retry.left, 1f)
        assertEquals(230f, retry.width, 1f)
        assertEquals(2228f, edit.left, 1f)
        assertEquals(206f, edit.width, 1f)
        compose.onNodeWithText("내용 수정").performClick()
        compose.onNodeWithTag("chat-input").assertIsDisplayed()
        compose.runOnIdle { assertEquals("모비는 뭐가 좋아?", draft.text) }
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
    @Config(qualifiers = "ko-rKR-w800dp-h1000dp-mdpi")
    fun enlargedTextKeepsFailedTurnAndRecoveryActionsVisible() {
        state =
            ConversationUiState(
                ConversationConnection.READY,
                messages = listOf(ConversationMessage("attempt", "남겨 둔 질문", true)),
                failed = true,
                problem = ConversationProblem.TIMEOUT,
            )
        draft = TextFieldValue("남겨 둔 질문")
        height = 900.dp
        show(fontScale = 1.6f)

        compose.onNodeWithTag("chat-user-bubble").assertIsDisplayed()
        compose.onNodeWithTag("chat-inline-failure").assertIsDisplayed()
        compose.onNodeWithTag("chat-composer").assertIsDisplayed()
        capture("failed-enlarged-text")
        compose.onNodeWithText("다시 보내기").performClick()
        compose.onNodeWithText("내용 수정").performClick()
        compose.runOnIdle {
            assertEquals(1, retries)
            assertEquals("남겨 둔 질문", draft.text)
        }
    }

    @Test
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    fun aaosDensityPreservesReferenceGeometryAndLargeTouchTargets() {
        state = state.copy(messages = messages)
        show(density = 10f / 7f)
        capture("messages-aaos")
        compose.onNodeWithTag("chat-send").assertHeightIsAtLeast(76.dp)
        val panel = compose.onNodeWithTag("chat-panel").fetchSemanticsNode().boundsInRoot
        assertEquals(796f, panel.left, 1f)
        assertEquals(34f, panel.top, 1f)
        assertEquals(1126f, panel.height, 1f)
        val send = compose.onNodeWithTag("chat-send-visual", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
        assertEquals(72f, send.width, 1f)
        compose.runOnIdle {
            height = 940.dp
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
        assertEquals(882f, resized.height, 1f)
        assertEquals(854f, composer.left, 1f)
        assertEquals(805f, composer.top, 1f)
        assertEquals(87f, composer.height, 1f)
        assertEquals(192f, avatar.left, 1f)
        assertEquals(304f, avatar.top, 1f)
        assertEquals(440f, avatar.width, 1f)
    }

    @Test
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    fun tallerSystemNavigationShrinksCompanionBeforeTextOverlaps() {
        height = 1120.dp
        show()
        capture("navigation-resized")
        val companion = compose.onNodeWithTag("chat-companion").fetchSemanticsNode().boundsInRoot
        val panel = compose.onNodeWithTag("chat-panel").fetchSemanticsNode().boundsInRoot
        val avatar = compose.onNodeWithTag("chat-avatar").fetchSemanticsNode().boundsInRoot
        assertEquals(900f, companion.height, 1f)
        assertEquals(1096f, panel.bottom, 1f)
        assertEquals(440f, avatar.width, 1f)
        compose.onNodeWithTag("chat-composer").assertIsDisplayed()
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
                        { connectionOpens++ },
                        Modifier.height(height / density),
                        interactionAllowed = allowed,
                        onReturnHome = { homeReturns++ },
                        onRetry = { retries++ },
                        onRecheckConnection = {
                            checks++
                            state =
                                state.copy(
                                    connection = ConversationConnection.CHECKING,
                                    connectionProblem = null,
                                    connectionRetrying = true,
                                )
                        },
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

    private val referenceMessages =
        listOf(
            ConversationMessage("first-user", "오늘은 조금 피곤한 하루였어.", true),
            ConversationMessage("first-reply", "오늘 하루도 수고했어요.\n잠깐 쉬면서 편하게 이야기해 볼까요?", false),
            ConversationMessage("second-user", "응, 기분 좋아지는 얘기 해줘.", true),
            ConversationMessage("second-reply", "좋아요. 오늘 발견한 작은 행복부터 나눠 볼까요?", false),
        )
}
