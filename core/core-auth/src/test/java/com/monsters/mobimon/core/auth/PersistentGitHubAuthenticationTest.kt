package com.monsters.mobimon.core.auth

import com.monsters.mobimon.core.domain.AuthenticationProblem
import com.monsters.mobimon.core.domain.GitHubAccount
import com.monsters.mobimon.core.domain.GitHubSession
import com.monsters.mobimon.core.domain.GitHubSignIn
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
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
