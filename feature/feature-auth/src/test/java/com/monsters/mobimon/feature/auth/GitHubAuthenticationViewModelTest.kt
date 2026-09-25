package com.monsters.mobimon.feature.auth

import androidx.lifecycle.ViewModelStore
import com.monsters.mobimon.core.domain.AuthenticationProblem
import com.monsters.mobimon.core.domain.GitHubAccount
import com.monsters.mobimon.core.domain.GitHubAuthentication
import com.monsters.mobimon.core.domain.GitHubSession
import com.monsters.mobimon.core.domain.GitHubSignIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GitHubAuthenticationViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val authentication = FakeAuthentication()

    @Before fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After fun cleanup() {
        Dispatchers.resetMain()
    }

    @Test fun firstFrameMatchesRestoringSession() =
        runTest(dispatcher) {
            authentication.session.value = GitHubSession.Restoring
            val model = GitHubAuthenticationViewModel(authentication)
            assertEquals(CopilotUiState.AuthenticationStatus(pending = true), model.state.value)
        }

    @Test fun repeatedClicksAndManualChecksKeepOnePollingJob() =
        runTest(dispatcher) {
            val model = GitHubAuthenticationViewModel(authentication)
            model.activate(true)
            model.action(CopilotAction.REQUEST_CODE)
            model.action(CopilotAction.REQUEST_CODE)
            runCurrent()
            repeat(5) { model.action(CopilotAction.RECHECK) }
            runCurrent()
            assertEquals(1, authentication.requests)
            assertEquals(0, authentication.restores)
            assertTrue(model.state.value is CopilotUiState.Waiting)
            model.deactivate()
            runCurrent()
            assertEquals(1, authentication.cancellations)
            assertTrue(model.state.value is CopilotUiState.Introduction)
        }

    @Test fun hiddenOrUnparkedScreenCannotStartAuthentication() =
        runTest(dispatcher) {
            val model = GitHubAuthenticationViewModel(authentication)
            model.action(CopilotAction.REQUEST_CODE)
            model.activate(false)
            model.action(CopilotAction.REQUEST_CODE)
            runCurrent()
            assertEquals(0, authentication.requests)
        }

    @Test fun stopBeforeLaunchAlsoCancelsCodeRequest() =
        runTest(dispatcher) {
            val model = GitHubAuthenticationViewModel(authentication)
            model.activate(true)
            model.action(CopilotAction.REQUEST_CODE)
            model.deactivate()
            runCurrent()
            assertEquals(0, authentication.requests)
        }

    @Test fun restoredAuthenticationNeverClaimsCopilotReadiness() =
        runTest(dispatcher) {
            authentication.session.value = GitHubSession.Authenticated(GitHubAccount(1, "driver"))
            val model = GitHubAuthenticationViewModel(authentication)
            model.activate(true)
            runCurrent()
            assertEquals(CopilotUiState.AuthenticationStatus(account = "@driver"), model.state.value)
            model.action(CopilotAction.CONFIRM_DISCONNECT)
            runCurrent()
            assertTrue(model.state.value is CopilotUiState.Disconnect)
            model.action(CopilotAction.KEEP_CONNECTION)
            runCurrent()
            assertTrue(model.state.value is CopilotUiState.AuthenticationStatus)
            model.action(CopilotAction.CONFIRM_DISCONNECT)
            model.action(CopilotAction.DISCONNECT)
            runCurrent()
            assertEquals(1, authentication.disconnects)
            assertTrue(model.state.value is CopilotUiState.Introduction)
        }

    @Test fun failedRestorationExposesRetryWithoutRequestingAnotherCode() =
        runTest(dispatcher) {
            authentication.session.value = GitHubSession.Failure(AuthenticationProblem.NETWORK)
            val model = GitHubAuthenticationViewModel(authentication)
            model.activate(true)
            runCurrent()
            assertEquals(
                CopilotUiState.AuthenticationStatus(problem = AuthenticationProblem.NETWORK),
                model.state.value,
            )
            model.action(CopilotAction.RECHECK)
            runCurrent()
            assertEquals(1, authentication.restores)
            assertEquals(0, authentication.requests)
        }

    @Test fun foregroundRestoreClearsSignInFailureWithoutResurrectingIt() =
        runTest(dispatcher) {
            authentication.signInFailure = AuthenticationProblem.NETWORK
            val model = GitHubAuthenticationViewModel(authentication)
            val models = ViewModelStore().apply { put("authentication", model) }
            try {
                model.activate(true)
                model.action(CopilotAction.REQUEST_CODE)
                runCurrent()
                val failure = CopilotUiState.AuthenticationStatus(problem = AuthenticationProblem.NETWORK)
                assertEquals(failure, model.state.value)

                model.deactivate()
                model.activate(true)
                runCurrent()
                assertEquals(failure, model.state.value)

                // Application foreground restoration runs independently of the screen's commands.
                authentication.restoredSession = GitHubSession.Authenticated(GitHubAccount(1, "driver"))
                authentication.restore()
                runCurrent()
                assertEquals(CopilotUiState.AuthenticationStatus(account = "@driver"), model.state.value)
                assertEquals(1, authentication.requests)

                authentication.session.value = GitHubSession.Failure(AuthenticationProblem.REAUTHENTICATION)
                runCurrent()
                assertEquals(
                    CopilotUiState.AuthenticationStatus(problem = AuthenticationProblem.REAUTHENTICATION),
                    model.state.value,
                )
            } finally {
                models.clear()
            }
        }

    @Test fun restoredSessionPreservesAnActiveApproval() =
        runTest(dispatcher) {
            val model = GitHubAuthenticationViewModel(authentication)
            val models = ViewModelStore().apply { put("authentication", model) }
            try {
                model.activate(true)
                model.action(CopilotAction.REQUEST_CODE)
                runCurrent()
                authentication.restoredSession = GitHubSession.Authenticated(GitHubAccount(1, "driver"))
                authentication.restore()
                runCurrent()
                assertTrue(model.state.value is CopilotUiState.Waiting)
                assertEquals(0, authentication.cancellations)
            } finally {
                models.clear()
            }
        }

    private class FakeAuthentication : GitHubAuthentication {
        override val session = MutableStateFlow<GitHubSession>(GitHubSession.SignedOut)
        override val configured = true
        var requests = 0
        var cancellations = 0
        var restores = 0
        var disconnects = 0
        var signInFailure: AuthenticationProblem? = null
        var restoredSession: GitHubSession? = null

        override suspend fun restore() {
            restores++
            restoredSession?.let { session.value = it }
        }

        override suspend fun disconnect() {
            disconnects++
            session.value = GitHubSession.SignedOut
        }

        override fun signIn() =
            flow {
                requests++
                signInFailure?.let {
                    emit(GitHubSignIn.Failed(it))
                    return@flow
                }
                try {
                    emit(GitHubSignIn.Waiting("ABCD-EFGH", "https://github.com/login/device", 900))
                    awaitCancellation()
                } finally {
                    cancellations++
                }
            }
    }
}
