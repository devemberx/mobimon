package com.monsters.mobimon.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monsters.mobimon.core.domain.GitHubAuthentication
import com.monsters.mobimon.core.domain.GitHubSession
import com.monsters.mobimon.core.domain.GitHubSignIn
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

internal class GitHubAuthenticationViewModel(
    private val authentication: GitHubAuthentication,
) : ViewModel() {
    private val progress = MutableStateFlow<GitHubSignIn?>(null)
    private val address = MutableStateFlow(false)
    private val confirming = MutableStateFlow(false)
    private val busy = MutableStateFlow(false)
    private val mutableState = MutableStateFlow<CopilotUiState>(CopilotUiState.Introduction(!authentication.configured))
    val state = mutableState.asStateFlow()
    private var command: Job? = null
    private var active = false
    private var allowed = false

    init {
        viewModelScope.launch {
            combine(
                authentication.session.onEach { session ->
                    if (session is GitHubSession.Authenticated && progress.value is GitHubSignIn.Failed) {
                        progress.value = null
                    }
                },
                progress,
                address,
                confirming,
                busy,
            ) { session, pending, showAddress, confirm, working ->
                when {
                    confirm && session is GitHubSession.Authenticated ->
                        CopilotUiState.Disconnect(
                            "@${session.account.login}",
                            working,
                        )
                    pending is GitHubSignIn.Waiting ->
                        CopilotUiState.Waiting(
                            pending.userCode,
                            pending.remainingSeconds,
                            showAddress,
                            verificationUri = pending.verificationUri,
                            retrying = pending.retrying,
                        )
                    pending == GitHubSignIn.Expired -> CopilotUiState.Expired
                    pending is GitHubSignIn.Failed -> CopilotUiState.AuthenticationStatus(problem = pending.problem)
                    pending == GitHubSignIn.Requesting || working -> CopilotUiState.AuthenticationStatus(pending = true)
                    session is GitHubSession.Authenticated ->
                        CopilotUiState.AuthenticationStatus(
                            account = "@${session.account.login}",
                        )
                    session is GitHubSession.Failure -> CopilotUiState.AuthenticationStatus(problem = session.problem)
                    session == GitHubSession.Restoring -> CopilotUiState.AuthenticationStatus(pending = true)
                    else -> CopilotUiState.Introduction(!authentication.configured)
                }
            }.collect { mutableState.value = it }
        }
    }

    fun activate(interactionAllowed: Boolean) {
        active = true
        allowed = interactionAllowed
        if (!allowed) stopPolling()
        if (authentication.session.value == GitHubSession.Restoring && command?.isActive != true) restore()
    }

    fun deactivate() {
        active = false
        allowed = false
        stopPolling()
    }

    fun action(action: CopilotAction) {
        when (action) {
            CopilotAction.SHOW_ADDRESS -> address.value = true
            CopilotAction.SHOW_QR -> address.value = false
            CopilotAction.KEEP_CONNECTION -> confirming.value = false
            CopilotAction.CONFIRM_DISCONNECT -> if (active && allowed) confirming.value = true
            CopilotAction.CANCEL, CopilotAction.BACK -> stopPolling()
            CopilotAction.REQUEST_CODE -> {
                if (!active || !allowed || command?.isActive == true) return
                address.value = false
                progress.value = GitHubSignIn.Requesting
                command =
                    viewModelScope.launch {
                        authentication.signIn().collect {
                            progress.value =
                                if (it ==
                                    GitHubSignIn.Complete
                                ) {
                                    null
                                } else {
                                    it
                                }
                        }
                    }
            }
            CopilotAction.RECHECK -> {
                // Polling already uses GitHub's minimum interval. Manual checks never bypass that schedule.
                if (progress.value is GitHubSignIn.Waiting || command?.isActive == true || !active || !allowed) return
                restore()
            }
            CopilotAction.DISCONNECT -> {
                if (!active || !allowed || command?.isActive == true) return
                command =
                    viewModelScope.launch {
                        busy.value = true
                        try {
                            progress.value = null
                            authentication.disconnect()
                            confirming.value = false
                        } finally {
                            busy.value = false
                        }
                    }
            }
            else -> Unit
        }
    }

    private fun restore() {
        command =
            viewModelScope.launch {
                busy.value = true
                progress.value = null
                try {
                    authentication.restore()
                } finally {
                    busy.value = false
                }
            }
    }

    private fun stopPolling() {
        if (progress.value is GitHubSignIn.Waiting || progress.value == GitHubSignIn.Requesting) {
            command?.cancel()
            progress.value = null
        }
    }
}
