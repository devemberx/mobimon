package com.monsters.mobimon.core.auth

import com.monsters.mobimon.core.domain.AuthenticationProblem
import com.monsters.mobimon.core.domain.ConversationProblem
import com.monsters.mobimon.core.domain.ConversationResult
import com.monsters.mobimon.core.domain.ConversationTurn
import com.monsters.mobimon.core.domain.GitHubAccount
import com.monsters.mobimon.core.domain.GitHubSession
import com.monsters.mobimon.core.domain.GitHubSignIn
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PersistentGitHubAuthenticationTest {
    private val api = FakeApi()
    private val store = MemoryStore()
    private var allowed = true

    @Test fun pendingAndSlowDownHonorIntervalsBeforePersistedCompletion() =
        runTest {
            val calls = mutableListOf<Long>()
            val outcomes =
                ArrayDeque(
                    listOf(TokenPoll.Pending, TokenPoll.SlowDown(12), TokenPoll.Approved(GitHubTokens("approved"))),
                )
            api.pollResult = {
                calls += testScheduler.currentTime
                outcomes.removeFirst()
            }
            val repository = repository()
            val progress = repository.signIn().toList()
            assertEquals(listOf(5000L, 10000L, 22000L), calls)
            assertEquals(GitHubSignIn.Complete, progress.last())
            assertEquals("approved", store.credential?.tokens?.accessToken)
            assertEquals(GitHubSession.Authenticated(GitHubAccount(1, "driver")), repository.session.value)
        }

    @Test fun expiryHidesCodeAndStopsPolling() =
        runTest {
            api.expires = 12
            var polls = 0
            api.pollResult = {
                polls++
                TokenPoll.Pending
            }
            val progress = repository().signIn().toList()
            assertEquals(2, polls)
            assertEquals(GitHubSignIn.Expired, progress.last())
            assertNull(store.credential)
        }

    @Test fun cancellationDuringExchangeCannotPersistLateApproval() =
        runTest {
            val approval = CompletableDeferred<TokenPoll>()
            api.pollResult = { approval.await() }
            val repository = repository()
            val job = launch { repository.signIn().toList() }
            advanceTimeBy(5000)
            runCurrent()
            job.cancelAndJoin()
            approval.complete(TokenPoll.Approved(GitHubTokens("late")))
            runCurrent()
            assertNull(store.credential)
            assertFalse(repository.session.value is GitHubSession.Authenticated)
        }

    @Test fun restrictionDuringExchangeCannotPersistApproval() =
        runTest {
            api.pollResult = {
                allowed = false
                TokenPoll.Approved(GitHubTokens("late"))
            }
            val progress = repository().signIn().toList()
            assertEquals(GitHubSignIn.Failed(AuthenticationProblem.RESTRICTED), progress.last())
            assertNull(store.credential)
        }

    @Test fun storageFailureNeverPublishesAuthentication() =
        runTest {
            store.failWrites = true
            val repository = repository()
            assertEquals(GitHubSignIn.Failed(AuthenticationProblem.STORAGE), repository.signIn().toList().last())
            assertFalse(repository.session.value is GitHubSession.Authenticated)
        }

    @Test fun newRepositoryRestoresPersistedTokenWithoutDeviceFlow() =
        runTest {
            repository().signIn().toList()
            val restarted = repository()
            restarted.restore()
            assertTrue(restarted.session.value is GitHubSession.Authenticated)
            assertEquals(1, api.codeRequests)
        }

    @Test fun revokedTokenIsClearedAndRequiresApproval() =
        runTest {
            store.credential = StoredCredential("client", GitHubTokens("revoked"))
            api.accountResult = { throw AuthenticationException(AuthenticationProblem.REAUTHENTICATION) }
            val repository = repository()
            repository.restore()
            assertEquals(GitHubSession.Failure(AuthenticationProblem.REAUTHENTICATION), repository.session.value)
            assertNull(store.credential)
        }

    @Test fun offlineRestorePreservesCredentialAndRetryValidatesIt() =
        runTest {
            store.credential = StoredCredential("client", GitHubTokens("saved"))
            api.accountResult = { throw AuthenticationException(AuthenticationProblem.NETWORK) }
            val repository = repository()
            repository.restore()
            assertEquals(GitHubSession.Failure(AuthenticationProblem.NETWORK), repository.session.value)
            assertEquals("saved", store.credential?.tokens?.accessToken)
            api.accountResult = { GitHubAccount(1, "driver") }
            repository.restore()
            assertTrue(repository.session.value is GitHubSession.Authenticated)
        }

    @Test fun refreshIsPersistedBeforeIdentityFailureAndReusedAfterRestart() =
        runTest {
            store.credential = StoredCredential("client", GitHubTokens("old", 1, "refresh", 999999))
            api.accountResult = { throw AuthenticationException(AuthenticationProblem.NETWORK) }
            repository().restore()
            assertEquals("rotated", store.credential?.tokens?.accessToken)
            assertEquals("new-refresh", store.credential?.tokens?.refreshToken)
            api.accountResult = { token ->
                assertEquals("rotated", token)
                GitHubAccount(1, "driver")
            }
            val restarted = repository()
            restarted.restore()
            assertTrue(restarted.session.value is GitHubSession.Authenticated)
            assertEquals(1, api.refreshRequests)
        }

    @Test fun expiredRefreshRequiresSignInWithoutNetwork() =
        runTest {
            store.credential = StoredCredential("client", GitHubTokens("old", 1, "refresh", 1))
            val repository = repository()
            repository.restore()
            assertEquals(GitHubSession.Failure(AuthenticationProblem.REAUTHENTICATION), repository.session.value)
            assertEquals(0, api.refreshRequests)
        }

    @Test fun approvedTokenSurvivesTemporaryIdentityFailure() =
        runTest {
            api.accountResult = { throw AuthenticationException(AuthenticationProblem.NETWORK) }
            val repository = repository()
            assertEquals(GitHubSignIn.Failed(AuthenticationProblem.NETWORK), repository.signIn().toList().last())
            assertNotNull(store.credential)
            api.accountResult = { GitHubAccount(1, "driver") }
            repository.restore()
            assertTrue(repository.session.value is GitHubSession.Authenticated)
        }

    @Test fun deniedApprovalNeverPersistsToken() =
        runTest {
            api.pollResult = { throw AuthenticationException(AuthenticationProblem.DENIED) }
            assertEquals(GitHubSignIn.Failed(AuthenticationProblem.DENIED), repository().signIn().toList().last())
            assertNull(store.credential)
        }

    @Test fun networkPollingBacksOffWithoutExceedingDeadline() =
        runTest {
            val calls = mutableListOf<Long>()
            api.expires = 32
            api.pollResult =
                {
                    calls += testScheduler.currentTime
                    throw AuthenticationException(AuthenticationProblem.NETWORK)
                }
            val progress = repository().signIn().toList()
            assertEquals(listOf(5000L, 15000L), calls)
            assertTrue(progress.filterIsInstance<GitHubSignIn.Waiting>().any { it.retrying })
            assertEquals(GitHubSignIn.Expired, progress.last())
        }

    @Test fun disconnectRemovesDurableSessionAndStartupStaysSignedOut() =
        runTest {
            val repository = repository()
            repository.signIn().toList()
            repository.disconnect()
            val restarted = repository()
            restarted.restore()
            assertEquals(GitHubSession.SignedOut, restarted.session.value)
            assertNull(store.credential)
        }

    @Test fun changedClientIdCannotReuseAnotherAppsToken() =
        runTest {
            store.credential = StoredCredential("other", GitHubTokens("saved"))
            val repository = repository()
            repository.restore()
            assertEquals(GitHubSession.SignedOut, repository.session.value)
            assertNull(store.credential)
        }

    @Test fun unreadableStorageFailsClosedWithoutProviderRequests() =
        runTest {
            store.failReads = true
            val repository = repository()
            repository.restore()
            assertEquals(GitHubSession.Failure(AuthenticationProblem.STORAGE), repository.session.value)
            assertEquals(0, api.codeRequests)
        }

    @Test fun conversationReusesStoredCredentialAndRefreshesBeforeExpiry() =
        runTest {
            val repository = repository()
            repository.signIn().toList()
            assertEquals("approved", repository.conversationCredential(1).token)
            store.credential = StoredCredential("client", GitHubTokens("old", 120_000, "refresh", 999_999))
            val credential = repository.conversationCredential(1)
            assertEquals("rotated", credential.token)
            assertEquals("rotated", store.credential?.tokens?.accessToken)
            assertTrue(repository.isCurrent(credential))
            assertFalse(credential.toString().contains("rotated"))
            repository.disconnect()
            repository.signIn().toList()
            assertFalse(repository.isCurrent(credential))
        }

    @Test fun conversationCredentialRejectsWrongAccountAndParkingLoss() =
        runTest {
            val repository = repository()
            repository.signIn().toList()
            try {
                repository.conversationCredential(2)
                throw AssertionError("Wrong account must fail")
            } catch (error: AuthenticationException) {
                assertEquals(AuthenticationProblem.REAUTHENTICATION, error.problem)
            }
            allowed = false
            try {
                repository.conversationCredential(1)
                throw AssertionError("Unparked request must fail")
            } catch (error: AuthenticationException) {
                assertEquals(AuthenticationProblem.RESTRICTED, error.problem)
            }
        }

    @Test fun changedIdentityDuringConversationRefreshClearsCredentials() =
        runTest {
            val repository = repository()
            repository.signIn().toList()
            store.credential = StoredCredential("client", GitHubTokens("old", 120_000, "refresh", 999_999))
            api.accountResult = { GitHubAccount(2, "different") }
            try {
                repository.conversationCredential(1)
                throw AssertionError("Changed identity must fail")
            } catch (error: AuthenticationException) {
                assertEquals(AuthenticationProblem.REAUTHENTICATION, error.problem)
            }
            assertNull(store.credential)
            assertEquals(GitHubSession.Failure(AuthenticationProblem.REAUTHENTICATION), repository.session.value)
        }

    @Test fun conversationRetriesIdentityValidationAfterRefreshNetworkFailure() =
        runTest {
            val repository = repository()
            repository.signIn().toList()
            store.credential = StoredCredential("client", GitHubTokens("old", 120_000, "refresh", 999_999))
            var identityChecks = 0
            api.accountResult = {
                identityChecks++
                throw AuthenticationException(AuthenticationProblem.NETWORK)
            }
            repeat(2) {
                assertAuthenticationProblem(AuthenticationProblem.NETWORK) { repository.conversationCredential(1) }
                assertEquals("rotated", store.credential?.tokens?.accessToken)
            }
            assertEquals(2, identityChecks)
            assertEquals(1, api.refreshRequests)
            api.accountResult = { token ->
                assertEquals("rotated", token)
                GitHubAccount(1, "driver")
            }
            val credential = repository.conversationCredential(1)
            assertEquals("rotated", credential.token)
            assertTrue(repository.isCurrent(credential))
        }

    @Test fun cancelledRefreshIdentityCheckCannotAuthorizeOldOrUnverifiedCredentials() =
        runTest {
            val repository = repository()
            repository.signIn().toList()
            val oldCredential = repository.conversationCredential(1)
            store.credential = StoredCredential("client", GitHubTokens("old", 120_000, "refresh", 999_999))
            val identity = CompletableDeferred<GitHubAccount>()
            api.accountResult = { identity.await() }
            val pending = launch { repository.conversationCredential(1) }
            runCurrent()
            pending.cancelAndJoin()
            assertEquals("rotated", store.credential?.tokens?.accessToken)
            assertFalse(repository.isCurrent(oldCredential))
            api.accountResult = { GitHubAccount(2, "different") }
            assertAuthenticationProblem(AuthenticationProblem.REAUTHENTICATION) { repository.conversationCredential(1) }
            assertNull(store.credential)
            assertEquals(GitHubSession.Failure(AuthenticationProblem.REAUTHENTICATION), repository.session.value)
        }

    @Test fun copilot401AtEveryStageRequiresRecheckBeforeReusingCredentials() =
        runTest {
            for (stage in CopilotRequestStage.entries) {
                val repository = repository()
                repository.signIn().toList()
                val credential = repository.conversationCredential(1)
                val copilot = FakeCopilotApi(stage)
                val provider = conversationProvider(repository, copilot)
                assertEquals(ConversationResult.Failure(ConversationProblem.ACCOUNT), reply(provider))
                assertEquals(GitHubSession.Failure(AuthenticationProblem.PROVIDER), repository.session.value)
                assertEquals("approved", store.credential?.tokens?.accessToken)
                assertFalse(repository.isCurrent(credential))
                val requests = copilot.requests
                assertEquals(ConversationResult.Failure(ConversationProblem.ACCOUNT), reply(provider))
                assertEquals(requests, copilot.requests)
            }
        }

    @Test fun copilot401RecoveryConfirmsRevocationBeforeClearingCredentials() =
        runTest {
            val repository = repository()
            repository.signIn().toList()
            val provider = conversationProvider(repository, FakeCopilotApi())
            reply(provider)
            assertEquals(GitHubSession.Failure(AuthenticationProblem.PROVIDER), repository.session.value)
            api.accountResult = { throw AuthenticationException(AuthenticationProblem.REAUTHENTICATION) }
            repository.restore()
            assertEquals(GitHubSession.Failure(AuthenticationProblem.REAUTHENTICATION), repository.session.value)
            assertNull(store.credential)
        }

    @Test fun copilot401RecoveryRefreshesCredentialWithoutReplayingCompletion() =
        runTest {
            store.credential = StoredCredential("client", GitHubTokens("old", 999_999, "refresh", 9_999_999))
            val repository = repository()
            repository.restore()
            val copilot = FakeCopilotApi(CopilotRequestStage.COMPLETION)
            val provider = conversationProvider(repository, copilot)
            assertEquals(ConversationResult.Failure(ConversationProblem.ACCOUNT), reply(provider))
            assertEquals(GitHubSession.Failure(AuthenticationProblem.PROVIDER), repository.session.value)
            assertEquals(1, copilot.completions)
            api.accountResult = { token ->
                if (token == "old") throw AuthenticationException(AuthenticationProblem.REAUTHENTICATION)
                GitHubAccount(1, "driver")
            }
            repository.restore()
            assertEquals(GitHubSession.Authenticated(GitHubAccount(1, "driver")), repository.session.value)
            assertEquals("rotated", store.credential?.tokens?.accessToken)
            assertEquals(1, copilot.completions)
            copilot.failureStage = null
            assertEquals(ConversationResult.Success("answer"), reply(provider))
            assertEquals("rotated", copilot.lastToken)
            assertEquals(2, copilot.completions)
        }

    @Test fun copilot401OfflineRecoveryRetainsCredentialsUntilSuccessfulRecheck() =
        runTest {
            val repository = repository()
            repository.signIn().toList()
            reply(conversationProvider(repository, FakeCopilotApi()))
            assertEquals(GitHubSession.Failure(AuthenticationProblem.PROVIDER), repository.session.value)
            api.accountResult = { throw AuthenticationException(AuthenticationProblem.NETWORK) }
            repository.restore()
            assertEquals(GitHubSession.Failure(AuthenticationProblem.NETWORK), repository.session.value)
            assertEquals("approved", store.credential?.tokens?.accessToken)
            api.accountResult = { GitHubAccount(1, "driver") }
            repository.restore()
            assertEquals("approved", repository.conversationCredential(1).token)
        }

    @Test fun lateCopilot401CannotInvalidateAReplacementSession() =
        runTest {
            val repository = repository()
            repository.signIn().toList()
            val copilot = FakeCopilotApi(CopilotRequestStage.COMPLETION)
            val response = CompletableDeferred<Unit>()
            copilot.beforeComplete = { response.await() }
            val pending = async { reply(conversationProvider(repository, copilot)) }
            runCurrent()
            repository.disconnect()
            repository.signIn().toList()
            val replacement = repository.conversationCredential(1)
            response.complete(Unit)
            assertEquals(ConversationResult.Failure(ConversationProblem.ACCOUNT), pending.await())
            assertEquals(GitHubSession.Authenticated(GitHubAccount(1, "driver")), repository.session.value)
            assertTrue(repository.isCurrent(replacement))
        }

    private fun conversationProvider(
        repository: PersistentGitHubAuthentication,
        copilot: CopilotApi,
    ) = CopilotConversationProvider(
        repository::conversationCredential,
        repository::isCurrent,
        { allowed },
        copilot,
        { 100_000L },
        repository::rejectConversationCredential,
    )

    private suspend fun reply(provider: CopilotConversationProvider) =
        provider.reply(1, "conversation", "friend:mobi", listOf(ConversationTurn("hello", true)))

    private class FakeCopilotApi(
        var failureStage: CopilotRequestStage? = CopilotRequestStage.AUTHORIZATION,
    ) : CopilotApi {
        var requests = 0
        var completions = 0
        var lastToken: String? = null
        var beforeComplete: suspend () -> Unit = {}

        override suspend fun authorize(githubToken: String): CopilotAccess {
            lastToken = githubToken
            checkStage(CopilotRequestStage.AUTHORIZATION)
            return CopilotAccess(githubToken, 999_999L, "https://api.githubcopilot.com/".toHttpUrl())
        }

        override suspend fun models(access: CopilotAccess): List<CopilotModel> {
            checkStage(CopilotRequestStage.MODELS)
            return listOf(CopilotModel("gpt-4o", CopilotChatApi.CHAT_COMPLETIONS))
        }

        override suspend fun complete(
            access: CopilotAccess,
            model: CopilotModel,
            friendId: String,
            messages: List<ConversationTurn>,
        ): String {
            completions++
            beforeComplete()
            checkStage(CopilotRequestStage.COMPLETION)
            return "answer"
        }

        private fun checkStage(stage: CopilotRequestStage) {
            requests++
            if (stage == failureStage) throw ConversationException(ConversationProblem.ACCOUNT)
        }
    }

    private suspend fun assertAuthenticationProblem(
        expected: AuthenticationProblem,
        block: suspend () -> Unit,
    ) {
        try {
            block()
            throw AssertionError("Expected $expected")
        } catch (error: AuthenticationException) {
            assertEquals(expected, error.problem)
        }
    }

    private fun TestScope.repository() =
        PersistentGitHubAuthentication(
            "client",
            api,
            store,
            { testScheduler.currentTime },
            { 100_000L + testScheduler.currentTime },
            { allowed },
        )

    private class MemoryStore : CredentialStore {
        var credential: StoredCredential? = null
        var failReads = false
        var failWrites = false

        override suspend fun read(): StoredCredential? {
            check(!failReads)
            return credential
        }

        override suspend fun write(credential: StoredCredential) {
            check(!failWrites)
            this.credential = credential
        }

        override suspend fun clear() {
            credential = null
        }
    }

    private class FakeApi : GitHubApi {
        var expires = 60
        var codeRequests = 0
        var refreshRequests = 0
        var pollResult: suspend () -> TokenPoll = { TokenPoll.Approved(GitHubTokens("approved")) }
        var accountResult: suspend (String) -> GitHubAccount = { GitHubAccount(1, "driver") }

        override suspend fun requestCode(): DeviceAuthorization {
            codeRequests++
            return DeviceAuthorization("private-device", "ABCD-EFGH", "https://github.com/login/device", expires, 5)
        }

        override suspend fun poll(deviceCode: String): TokenPoll = pollResult()

        override suspend fun account(accessToken: String): GitHubAccount = accountResult(accessToken)

        override suspend fun refresh(refreshToken: String): GitHubTokens {
            refreshRequests++
            return GitHubTokens("rotated", 999999, "new-refresh", 9999999)
        }
    }
}
