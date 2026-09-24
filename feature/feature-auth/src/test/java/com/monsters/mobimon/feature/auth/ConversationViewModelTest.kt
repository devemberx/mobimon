package com.monsters.mobimon.feature.auth

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModelStore
import com.monsters.mobimon.core.domain.AuthenticationProblem
import com.monsters.mobimon.core.domain.ConversationLimits
import com.monsters.mobimon.core.domain.ConversationProblem
import com.monsters.mobimon.core.domain.ConversationProvider
import com.monsters.mobimon.core.domain.ConversationResult
import com.monsters.mobimon.core.domain.ConversationTurn
import com.monsters.mobimon.core.domain.GitHubAccount
import com.monsters.mobimon.core.domain.GitHubAuthentication
import com.monsters.mobimon.core.domain.GitHubSession
import com.monsters.mobimon.core.domain.GitHubSignIn
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ConversationViewModelTest {
    private val authentication = FakeAuthentication()
    private val provider = FakeProvider()
    private val store = ViewModelStore()

    @After fun close() {
        store.clear()
        Dispatchers.resetMain()
    }

    @Test fun modelCheckConnectsBeforeSendAndDuplicateSendIsBlocked() =
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            val model = model()
            runCurrent()
            model.edit(TextFieldValue("hello"))
            assertEquals(1, provider.connections)
            assertEquals(0, provider.requests.size)
            assertEquals(ConversationConnection.READY, model.state.value.connection)
            val reply = CompletableDeferred<ConversationResult<String>>()
            provider.answer = { reply.await() }
            model.send()
            model.send()
            runCurrent()
            assertEquals(1, provider.requests.size)
            assertTrue(model.state.value.replyPending)
            assertEquals(ConversationConnection.READY, model.state.value.connection)
            reply.complete(ConversationResult.Success("hi"))
            runCurrent()
            assertEquals(
                listOf("hello", "hi"),
                model.state.value.messages
                    .map { it.text },
            )
            assertEquals("", model.draft.text)
            assertEquals(ConversationConnection.READY, model.state.value.connection)
            model.edit(TextFieldValue("more"))
            model.send()
            runCurrent()
            assertEquals(listOf("hello", "hi", "more"), provider.requests.last().map { it.text })
        }

    @Test fun networkFailureRechecksModelWithoutSendingDraftAndBlocksDuplicateChecks() =
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            provider.connectionAnswer = { ConversationResult.Failure(ConversationProblem.NETWORK) }
            val model = model()
            runCurrent()
            assertEquals(ConversationConnection.UNAVAILABLE, model.state.value.connection)
            assertEquals(ConversationProblem.NETWORK, model.state.value.connectionProblem)
            model.edit(TextFieldValue("keep this draft"))
            model.send()
            runCurrent()
            assertTrue(provider.requests.isEmpty())

            val recheck = CompletableDeferred<ConversationResult<String>>()
            provider.connectionAnswer = { recheck.await() }
            model.retryConnection()
            model.retryConnection()
            runCurrent()
            assertEquals(2, provider.connections)
            assertEquals(ConversationConnection.CHECKING, model.state.value.connection)
            assertTrue(model.state.value.connectionRetrying)
            assertTrue(provider.requests.isEmpty())
            recheck.complete(ConversationResult.Success("gpt-4o"))
            runCurrent()
            assertEquals(ConversationConnection.READY, model.state.value.connection)
            assertEquals(null, model.state.value.connectionProblem)
            assertEquals("keep this draft", model.draft.text)
        }

    @Test fun parkingLossCancelsLateModelCheck() =
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            val late = CompletableDeferred<ConversationResult<String>>()
            provider.connectionAnswer = { withContext(NonCancellable) { late.await() } }
            val model = model()
            runCurrent()
            assertEquals(ConversationConnection.CHECKING, model.state.value.connection)
            model.activate(false)
            late.complete(ConversationResult.Success("gpt-4o"))
            runCurrent()
            assertFalse(model.state.value.connection == ConversationConnection.READY)
        }

    @Test fun backgroundCancelsLateModelCheckAndForegroundRechecks() =
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            val late = CompletableDeferred<ConversationResult<String>>()
            provider.connectionAnswer = { withContext(NonCancellable) { late.await() } }
            val model = model()
            model.setForegroundAllowed(true)
            runCurrent()
            assertEquals(ConversationConnection.CHECKING, model.state.value.connection)
            model.setForegroundAllowed(false)
            late.complete(ConversationResult.Success("gpt-4o"))
            runCurrent()
            assertFalse(model.state.value.connection == ConversationConnection.READY)
            model.edit(TextFieldValue("keep"))
            model.send()
            assertTrue(provider.requests.isEmpty())
            provider.connectionAnswer = { ConversationResult.Success("gpt-4o") }
            model.setForegroundAllowed(true)
            runCurrent()
            assertEquals(ConversationConnection.READY, model.state.value.connection)
        }

    @Test fun authenticationNetworkFailureCanRecheckWithoutSendingDraft() =
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            val model = model()
            runCurrent()
            model.edit(TextFieldValue("keep this draft"))
            authentication.session.value = GitHubSession.Failure(AuthenticationProblem.NETWORK)
            runCurrent()
            assertEquals(ConversationProblem.NETWORK, model.state.value.connectionProblem)
            val priorChecks = provider.connections
            authentication.restoreAction = {
                authentication.session.value = GitHubSession.Authenticated(GitHubAccount(1, "first"))
            }
            model.retryConnection()
            runCurrent()
            assertEquals(1, authentication.restores)
            assertEquals(priorChecks + 1, provider.connections)
            assertEquals(ConversationConnection.READY, model.state.value.connection)
            assertEquals("keep this draft", model.draft.text)
            assertTrue(provider.requests.isEmpty())
        }

    @Test fun provisionalDraftSurvivesFirstIdentityValidationAfterRecoverableFailure() =
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            authentication.session.value = GitHubSession.Restoring
            val model = model()
            runCurrent()
            val draft = TextFieldValue("초안", TextRange(2), TextRange(1, 2))
            model.edit(draft)
            authentication.session.value = GitHubSession.Failure(AuthenticationProblem.NETWORK)
            runCurrent()
            authentication.restoreAction = {
                authentication.session.value = GitHubSession.Authenticated(GitHubAccount(1, "first"))
            }

            model.retryConnection()
            runCurrent()

            assertEquals(draft, model.draft)
            assertEquals(ConversationConnection.READY, model.state.value.connection)
            assertTrue(
                model.state.value.messages
                    .isEmpty(),
            )
        }

    @Test fun unexpectedAuthenticationRestoreFailureNeverChecksTheModel() =
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            val model = model()
            runCurrent()
            val priorChecks = provider.connections
            authentication.session.value = GitHubSession.Failure(AuthenticationProblem.NETWORK)
            authentication.restoreAction = { throw IllegalStateException("restore failed") }
            runCurrent()
            model.retryConnection()
            runCurrent()
            assertEquals(ConversationConnection.UNAVAILABLE, model.state.value.connection)
            assertEquals(ConversationProblem.SERVICE, model.state.value.connectionProblem)
            assertEquals(priorChecks, provider.connections)
        }

    @Test fun failureRetainsHistoryAndDraftAndRetryDoesNotDuplicateUserMessage() =
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            val model = model()
            runCurrent()
            model.edit(TextFieldValue("first"))
            model.send()
            runCurrent()
            val prior = model.state.value.messages
            provider.answer = { ConversationResult.Failure(ConversationProblem.TIMEOUT) }
            model.edit(TextFieldValue("second", TextRange(6)))
            model.send()
            runCurrent()
            assertEquals(
                prior.map { it.text } + "second",
                model.state.value.messages
                    .map { it.text },
            )
            assertEquals("second", model.draft.text)
            assertEquals(ConversationProblem.TIMEOUT, model.state.value.problem)
            provider.answer = { ConversationResult.Success("answer") }
            model.retry()
            runCurrent()
            assertFalse(model.state.value.failed)
            assertEquals(
                listOf("first", "answer", "second", "answer"),
                model.state.value.messages
                    .map { it.text },
            )
        }

    @Test fun editingFailedTurnRemovesItsUnansweredBubbleButKeepsTheDraft() =
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            val model = model()
            runCurrent()
            provider.answer = { ConversationResult.Failure(ConversationProblem.TIMEOUT) }
            model.edit(TextFieldValue("before"))
            model.send()
            runCurrent()
            assertEquals(
                listOf("before"),
                model.state.value.messages
                    .map { it.text },
            )

            model.edit(TextFieldValue("after"))

            assertFalse(model.state.value.failed)
            assertTrue(
                model.state.value.messages
                    .isEmpty(),
            )
            assertEquals("after", model.draft.text)
        }

    @Test fun stalledReplyTimesOutAndRetainsEditableAttempt() =
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            val model = model()
            runCurrent()
            provider.answer = { CompletableDeferred<ConversationResult<String>>().await() }
            model.edit(TextFieldValue("waiting"))
            model.send()
            runCurrent()
            assertTrue(model.state.value.replyPending)

            advanceTimeBy(30_000)
            runCurrent()

            assertFalse(model.state.value.replyPending)
            assertEquals(ConversationProblem.TIMEOUT, model.state.value.problem)
            assertEquals("waiting", model.draft.text)
            assertEquals(
                listOf("waiting"),
                model.state.value.messages
                    .map { it.text },
            )
            model.dismissFailure()
            assertTrue(
                model.state.value.messages
                    .isEmpty(),
            )
        }

    @Test fun stalledConnectionCheckTimesOutAndAllowsExplicitRecheck() =
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            provider.connectionAnswer = { CompletableDeferred<ConversationResult<String>>().await() }
            val model = model()
            runCurrent()
            assertEquals(ConversationConnection.CHECKING, model.state.value.connection)

            advanceTimeBy(30_000)
            runCurrent()

            assertEquals(ConversationConnection.UNAVAILABLE, model.state.value.connection)
            assertEquals(ConversationProblem.TIMEOUT, model.state.value.connectionProblem)
            provider.connectionAnswer = { ConversationResult.Success("gpt-4o") }
            model.retryConnection()
            runCurrent()
            assertEquals(ConversationConnection.READY, model.state.value.connection)
        }

    @Test fun failedTurnRemainsVisibleAfterLeavingAndReturningToChat() =
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            val model = model()
            runCurrent()
            provider.answer = { ConversationResult.Failure(ConversationProblem.TIMEOUT) }
            model.edit(TextFieldValue("keep this attempt"))
            model.send()
            runCurrent()
            model.deactivate()
            model.activate(true)

            assertTrue(model.state.value.failed)
            assertEquals(
                listOf("keep this attempt"),
                model.state.value.messages
                    .map { it.text },
            )
            assertEquals("keep this attempt", model.draft.text)
        }

    @Test fun accessFailureRecheckDoesNotResendAndRestoresExplicitSend() =
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            val model = model()
            runCurrent()
            provider.answer = { ConversationResult.Failure(ConversationProblem.ACCESS) }
            model.edit(TextFieldValue("private draft"))
            model.send()
            runCurrent()
            assertEquals(ConversationConnection.UNAVAILABLE, model.state.value.connection)
            assertEquals(
                listOf("private draft"),
                model.state.value.messages
                    .map { it.text },
            )

            val sentBeforeRecheck = provider.requests.size
            model.retryConnection()
            runCurrent()
            assertEquals(sentBeforeRecheck, provider.requests.size)
            assertEquals(ConversationConnection.READY, model.state.value.connection)
            assertFalse(model.state.value.failed)
            assertEquals("private draft", model.draft.text)

            provider.answer = { ConversationResult.Success("answer") }
            model.send()
            runCurrent()
            assertEquals(
                listOf("private draft", "answer"),
                model.state.value.messages
                    .map { it.text },
            )
        }

    @Test fun editingAfterAccessFailureRechecksWithoutSendingTheDraft() =
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            val model = model()
            runCurrent()
            provider.answer = { ConversationResult.Failure(ConversationProblem.ACCESS) }
            model.edit(TextFieldValue("before"))
            model.send()
            runCurrent()
            val sentBeforeEdit = provider.requests.size
            val checksBeforeEdit = provider.connections

            model.edit(TextFieldValue("after"))
            runCurrent()

            assertEquals(checksBeforeEdit + 1, provider.connections)
            assertEquals(sentBeforeEdit, provider.requests.size)
            assertEquals(ConversationConnection.READY, model.state.value.connection)
            assertEquals("after", model.draft.text)
        }

    @Test fun cancelLeaveParkingLossAndContextChangeRejectLateReplies() =
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            val model = model()
            runCurrent()
            val changes: List<() -> Unit> =
                listOf(
                    { model.cancel() },
                    { model.deactivate() },
                    { model.activate(false) },
                    { model.bind("profile", "friend:luna") },
                )
            for (change in changes) {
                model.activate(true)
                val late = CompletableDeferred<ConversationResult<String>>()
                provider.answer = { withContext(NonCancellable) { late.await() } }
                model.edit(TextFieldValue("keep"))
                model.send()
                runCurrent()
                change()
                late.complete(ConversationResult.Success("must be ignored"))
                runCurrent()
                assertFalse(model.state.value.replyPending)
                assertTrue(
                    model.state.value.messages
                        .isEmpty(),
                )
                assertEquals("keep", model.draft.text)
            }
        }

    @Test fun ownerChangeDisconnectAndNewSessionClearPrivateData() =
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            val model = model()
            runCurrent()
            model.edit(TextFieldValue("private"))
            model.send()
            runCurrent()
            model.newConversation()
            assertTrue(
                model.state.value.messages
                    .isEmpty(),
            )
            val changes: List<() -> Unit> =
                listOf(
                    { model.bind("other", "friend:mobi") },
                    { authentication.session.value = GitHubSession.Authenticated(GitHubAccount(2, "second")) },
                    { authentication.session.value = GitHubSession.SignedOut },
                )
            for (change in changes) {
                model.edit(TextFieldValue("private"))
                change()
                runCurrent()
                assertEquals("", model.draft.text)
                assertTrue(
                    model.state.value.messages
                        .isEmpty(),
                )
            }
        }

    @Test fun compositionSurvivesNavigationAndTemporaryAuthenticationErrorsButNotRevocation() =
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            val model = model()
            runCurrent()
            val draft = TextFieldValue("오늘 하", TextRange(4), TextRange(3, 4))
            model.edit(draft)
            model.deactivate()
            model.bind("profile", "friend:mobi")
            assertEquals(draft, model.draft)
            authentication.session.value = GitHubSession.Failure(AuthenticationProblem.NETWORK)
            runCurrent()
            assertEquals(draft, model.draft)
            authentication.session.value = GitHubSession.Failure(AuthenticationProblem.REAUTHENTICATION)
            runCurrent()
            assertEquals(TextFieldValue(), model.draft)
        }

    @Test fun limitsNeverSendAndNewViewModelHasNoSessionData() =
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            val model = model()
            runCurrent()
            model.edit(TextFieldValue("x".repeat(ConversationLimits.INPUT_CHARACTERS + 1)))
            model.send()
            runCurrent()
            assertEquals(ConversationProblem.LIMIT, model.state.value.problem)
            assertEquals(0, provider.requests.size)
            model.newConversation()
            repeat(ConversationLimits.EXCHANGES) {
                model.edit(TextFieldValue("turn $it"))
                model.send()
                runCurrent()
            }
            model.edit(TextFieldValue("too many"))
            model.send()
            runCurrent()
            assertEquals(ConversationLimits.EXCHANGES, provider.requests.size)
            assertEquals(ConversationProblem.LIMIT, model.state.value.problem)
            val restarted = model()
            assertEquals(TextFieldValue(), restarted.draft)
            assertTrue(
                restarted.state.value.messages
                    .isEmpty(),
            )
        }

    @Test fun signedOutAndParkingLossNeverDispatchTheDraft() =
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            val model = model()
            runCurrent()
            model.edit(TextFieldValue("keep"))
            model.activate(false)
            model.send()
            runCurrent()
            assertEquals(0, provider.requests.size)
            assertEquals("keep", model.draft.text)
            model.activate(true)
            authentication.session.value = GitHubSession.SignedOut
            model.send()
            runCurrent()
            assertEquals(0, provider.requests.size)
            assertEquals(ConversationConnection.SIGNED_OUT, model.state.value.connection)
        }

    private fun model() =
        ConversationViewModel(authentication, provider).also {
            store.put("model-${System.identityHashCode(it)}", it)
            it.bind("profile", "friend:mobi")
            it.activate(true)
        }

    @Test fun routingIdentitySurvivesNavigationButChangesForNewConversationAndOwner() =
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            val model = model()
            runCurrent()

            fun send() {
                model.edit(TextFieldValue("same prompt"))
                model.send()
            }
            send()
            runCurrent()
            val first = provider.conversationIds.last()
            model.deactivate()
            model.activate(true)
            send()
            runCurrent()
            assertEquals(first, provider.conversationIds.last())
            model.newConversation()
            send()
            runCurrent()
            val second = provider.conversationIds.last()
            assertNotEquals(first, second)
            model.bind("other profile", "friend:mobi")
            runCurrent()
            send()
            runCurrent()
            assertNotEquals(second, provider.conversationIds.last())
        }

    private class FakeProvider : ConversationProvider {
        var connections = 0
        var connectionAnswer: suspend () -> ConversationResult<String> = { ConversationResult.Success("gpt-4o") }
        val conversationIds = mutableListOf<String>()
        val requests = mutableListOf<List<ConversationTurn>>()
        var answer: suspend () -> ConversationResult<String> = { ConversationResult.Success("answer") }

        override suspend fun connect(accountId: Long): ConversationResult<String> {
            connections++
            return connectionAnswer()
        }

        override suspend fun reply(
            accountId: Long,
            conversationId: String,
            friendId: String,
            messages: List<ConversationTurn>,
        ): ConversationResult<String> {
            conversationIds += conversationId
            requests += messages
            return answer()
        }
    }

    private class FakeAuthentication : GitHubAuthentication {
        override val session = MutableStateFlow<GitHubSession>(GitHubSession.Authenticated(GitHubAccount(1, "first")))
        override val configured = true
        var restores = 0
        var restoreAction: suspend () -> Unit = {}

        override suspend fun restore() {
            restores++
            restoreAction()
        }

        override suspend fun disconnect() {
            session.value = GitHubSession.SignedOut
        }

        override fun signIn() = emptyFlow<GitHubSignIn>()
    }
}
