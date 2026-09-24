package com.monsters.mobimon.feature.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monsters.mobimon.core.domain.AuthenticationProblem
import com.monsters.mobimon.core.domain.ConversationLimits
import com.monsters.mobimon.core.domain.ConversationProblem
import com.monsters.mobimon.core.domain.ConversationProvider
import com.monsters.mobimon.core.domain.ConversationResult
import com.monsters.mobimon.core.domain.ConversationTurn
import com.monsters.mobimon.core.domain.GitHubAuthentication
import com.monsters.mobimon.core.domain.GitHubSession
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

/** Activity memory only. Request generations also reject providers that return after cancellation. */
class ConversationViewModel(
    private val authentication: GitHubAuthentication,
    private val provider: ConversationProvider,
) : ViewModel() {
    private val mutableState = MutableStateFlow(ConversationUiState())
    val state = mutableState.asStateFlow()
    var draft by mutableStateOf(TextFieldValue())
        private set
    private var accountId: Long? = null
    private var profileId: String? = null
    private var friendId = "friend:mobi"
    private var active = false
    private var allowed = false
    private var foregroundAllowed = false
    private var foregroundManaged = false
    private var generation = 0L
    private var checkGeneration = 0L
    private var conversationId = UUID.randomUUID().toString()
    private var messageId = 0L
    private var work: Job? = null
    private var checkWork: Job? = null
    private var authenticationRetry: Job? = null
    private var history = emptyList<ConversationMessage>()

    init {
        viewModelScope.launch {
            authentication.session.collect { session ->
                val retryingAuthentication = authenticationRetry?.isActive == true
                val next =
                    when (session) {
                        is GitHubSession.Authenticated -> session.account.id
                        GitHubSession.Restoring -> accountId
                        is GitHubSession.Failure ->
                            if (session.problem in
                                listOf(
                                    AuthenticationProblem.NETWORK,
                                    AuthenticationProblem.PROVIDER,
                                )
                            ) {
                                accountId
                            } else {
                                null
                            }
                        GitHubSession.SignedOut -> null
                    }
                if (next != accountId) {
                    clear()
                    accountId = next
                }
                when (session) {
                    is GitHubSession.Authenticated -> {
                        if (state.value.connection == ConversationConnection.SIGNED_OUT) {
                            mutableState.value = state.value.copy(connection = ConversationConnection.UNAVAILABLE)
                        }
                        if (checkAllowed() &&
                            (
                                retryingAuthentication ||
                                    state.value.connection == ConversationConnection.UNAVAILABLE &&
                                    state.value.connectionProblem == null
                            )
                        ) {
                            checkConnection(retrying = retryingAuthentication)
                        }
                    }
                    GitHubSession.Restoring -> {
                        if (!retryingAuthentication) {
                            cancel()
                            cancelCheck()
                            mutableState.value = state.value.copy(connection = ConversationConnection.CHECKING)
                        }
                    }
                    is GitHubSession.Failure -> {
                        if (!retryingAuthentication ||
                            session.problem !in setOf(AuthenticationProblem.NETWORK, AuthenticationProblem.PROVIDER)
                        ) {
                            cancel()
                            cancelCheck()
                            mutableState.value =
                                state.value.copy(
                                    connection =
                                        if (session.problem in
                                            setOf(AuthenticationProblem.NETWORK, AuthenticationProblem.PROVIDER)
                                        ) {
                                            ConversationConnection.UNAVAILABLE
                                        } else {
                                            ConversationConnection.SIGNED_OUT
                                        },
                                    connectionProblem =
                                        when (session.problem) {
                                            AuthenticationProblem.NETWORK -> ConversationProblem.NETWORK
                                            AuthenticationProblem.PROVIDER -> ConversationProblem.SERVICE
                                            else -> null
                                        },
                                    connectionRetrying = false,
                                )
                        }
                    }
                    GitHubSession.SignedOut -> {
                        cancel()
                        cancelCheck()
                        mutableState.value =
                            state.value.copy(connection = ConversationConnection.SIGNED_OUT, connectionProblem = null)
                    }
                }
            }
        }
    }

    fun bind(
        profile: String,
        friend: String,
    ) {
        if (profileId != profile) {
            clear()
            profileId = profile
        }
        if (friendId != friend) {
            cancel()
            conversationId = UUID.randomUUID().toString()
            friendId = friend
        }
        if (checkAllowed() &&
            state.value.connection == ConversationConnection.UNAVAILABLE &&
            state.value.connectionProblem == null
        ) {
            checkConnection()
        }
    }

    fun activate(interactionAllowed: Boolean) {
        active = true
        allowed = interactionAllowed
        if (!allowed) {
            cancel()
            if (!foregroundAllowed) cancelCheck()
        } else if (state.value.connection == ConversationConnection.UNAVAILABLE &&
            state.value.connectionProblem == null
        ) {
            checkConnection()
        }
    }

    /** The app shell calls this on foreground entry and whenever trusted Park/AAOS evidence changes. */
    fun setForegroundAllowed(
        interactionAllowed: Boolean,
        refresh: Boolean = false,
    ) {
        foregroundManaged = true
        foregroundAllowed = interactionAllowed
        if (!checkAllowed()) {
            cancelCheck()
            cancel()
        } else if (interactionAllowed &&
            (
                refresh ||
                    (
                        state.value.connection == ConversationConnection.UNAVAILABLE &&
                            state.value.connectionProblem == null
                    )
            )
        ) {
            checkConnection(force = refresh)
        }
    }

    fun deactivate() {
        active = false
        allowed = false
        cancel()
        if (!foregroundAllowed) cancelCheck()
    }

    fun edit(value: TextFieldValue) {
        if (!active || !allowed || state.value.replyPending) return
        draft = value
        if (state.value.failed) mutableState.value = state.value.copy(failed = false)
    }

    fun send(text: String = draft.text) = sendInternal(text, retry = false)

    private fun sendInternal(
        text: String,
        retry: Boolean,
    ) {
        if (!canInteract() ||
            work?.isActive == true ||
            (state.value.failed && !retry) ||
            text.isBlank() ||
            text != draft.text
        ) {
            return
        }
        if (text.length > ConversationLimits.INPUT_CHARACTERS ||
            history.size >= ConversationLimits.EXCHANGES * 2 ||
            history.sumOf { it.text.length } + text.length + ConversationLimits.REPLY_CHARACTERS >
            ConversationLimits.HISTORY_CHARACTERS
        ) {
            fail(ConversationProblem.LIMIT)
            return
        }
        val account = accountId ?: return
        val request = ++generation
        val user = ConversationMessage((++messageId).toString(), text, true)
        draft = draft.copy(composition = null)
        mutableState.value =
            state.value.copy(messages = history + user, replyPending = true, failed = false, problem = null)
        work =
            viewModelScope.launch {
                val result =
                    safely {
                        provider.reply(
                            account,
                            conversationId,
                            friendId,
                            (history + user).map { ConversationTurn(it.text, it.fromUser) },
                        )
                    }
                if (request != generation || !canInteract() || account != accountId) return@launch
                when (result) {
                    is ConversationResult.Success -> {
                        if (result.value.isBlank() || result.value.length > ConversationLimits.REPLY_CHARACTERS) {
                            fail(ConversationProblem.PROVIDER)
                        } else {
                            history =
                                history + user + ConversationMessage((++messageId).toString(), result.value, false)
                            draft = TextFieldValue()
                            mutableState.value =
                                state.value.copy(
                                    messages = history,
                                    replyPending = false,
                                    connection = ConversationConnection.READY,
                                )
                        }
                    }
                    is ConversationResult.Failure -> fail(result.problem)
                }
            }
    }

    fun retry() {
        if (!canInteract() || work?.isActive == true) return
        sendInternal(draft.text, retry = true)
    }

    fun retryConnection() {
        if (!interactionAvailable() || checkWork?.isActive == true || authenticationRetry?.isActive == true) return
        when (val session = authentication.session.value) {
            is GitHubSession.Authenticated -> checkConnection(retrying = true)
            is GitHubSession.Failure -> {
                if (session.problem !in setOf(AuthenticationProblem.NETWORK, AuthenticationProblem.PROVIDER)) return
                val request = ++checkGeneration
                mutableState.value =
                    state.value.copy(
                        connection = ConversationConnection.CHECKING,
                        connectionProblem = null,
                        connectionRetrying = true,
                    )
                authenticationRetry =
                    viewModelScope.launch {
                        try {
                            authentication.restore()
                        } catch (cancelled: CancellationException) {
                            throw cancelled
                        } catch (_: Exception) {
                            if (request == checkGeneration && interactionAvailable()) {
                                authenticationRetry = null
                                mutableState.value =
                                    state.value.copy(
                                        connection = ConversationConnection.UNAVAILABLE,
                                        connectionProblem = ConversationProblem.SERVICE,
                                        connectionRetrying = false,
                                    )
                            }
                            return@launch
                        }
                        if (request != checkGeneration || !interactionAvailable()) return@launch
                        authenticationRetry = null
                        when (val restored = authentication.session.value) {
                            is GitHubSession.Authenticated -> {
                                if (accountId != restored.account.id) {
                                    clear()
                                    accountId = restored.account.id
                                }
                                checkConnection(retrying = true)
                            }
                            is GitHubSession.Failure -> {
                                mutableState.value =
                                    state.value.copy(
                                        connection = ConversationConnection.UNAVAILABLE,
                                        connectionProblem =
                                            if (restored.problem ==
                                                AuthenticationProblem.NETWORK
                                            ) {
                                                ConversationProblem.NETWORK
                                            } else {
                                                ConversationProblem.SERVICE
                                            },
                                        connectionRetrying = false,
                                    )
                            }
                            else -> Unit
                        }
                    }
            }
            else -> Unit
        }
    }

    fun dismissFailure() {
        mutableState.value = state.value.copy(failed = false)
    }

    fun cancel() {
        generation++
        work?.cancel()
        work = null
        mutableState.value = state.value.copy(messages = history, replyPending = false)
    }

    fun newConversation() {
        if (!active || !allowed) return
        cancel()
        history = emptyList()
        conversationId = UUID.randomUUID().toString()
        draft = TextFieldValue()
        mutableState.value = state.value.copy(messages = history, failed = false, problem = null)
    }

    private fun clear() {
        cancel()
        cancelCheck()
        history = emptyList()
        conversationId = UUID.randomUUID().toString()
        draft = TextFieldValue()
        mutableState.value = ConversationUiState()
    }

    private fun canInteract() =
        active &&
            allowed &&
            interactionAvailable() &&
            state.value.connection == ConversationConnection.READY &&
            profileId != null &&
            (authentication.session.value as? GitHubSession.Authenticated)?.account?.id == accountId &&
            accountId != null

    private fun interactionAvailable() = if (foregroundManaged) foregroundAllowed else active && allowed

    private fun checkAllowed() = interactionAvailable() && authentication.session.value is GitHubSession.Authenticated

    private fun checkConnection(
        force: Boolean = false,
        retrying: Boolean = false,
    ) {
        if (!checkAllowed()) return
        if (checkWork?.isActive == true) {
            if (!force) return
            cancelCheck()
        }
        val account = (authentication.session.value as? GitHubSession.Authenticated)?.account?.id ?: return
        if (account != accountId) return
        val request = ++checkGeneration
        mutableState.value =
            state.value.copy(
                connection = ConversationConnection.CHECKING,
                connectionProblem = null,
                connectionRetrying = retrying,
            )
        checkWork =
            viewModelScope.launch {
                val result = safely { provider.connect(account) }
                if (request != checkGeneration || !checkAllowed() || account != accountId) return@launch
                checkWork = null
                mutableState.value =
                    when (result) {
                        is ConversationResult.Success ->
                            state.value.copy(
                                connection = ConversationConnection.READY,
                                connectionProblem = null,
                                connectionRetrying = false,
                            )
                        is ConversationResult.Failure ->
                            state.value.copy(
                                connection = ConversationConnection.UNAVAILABLE,
                                connectionProblem = result.problem,
                                connectionRetrying = false,
                            )
                    }
            }
    }

    private fun cancelCheck() {
        checkGeneration++
        checkWork?.cancel()
        checkWork = null
        authenticationRetry?.cancel()
        authenticationRetry = null
        if (state.value.connection == ConversationConnection.CHECKING ||
            state.value.connection == ConversationConnection.READY
        ) {
            mutableState.value =
                state.value.copy(connection = ConversationConnection.UNAVAILABLE, connectionRetrying = false)
        }
    }

    private fun fail(problem: ConversationProblem) {
        mutableState.value =
            state.value.copy(
                messages = history,
                replyPending = false,
                failed = true,
                problem = problem,
                connection =
                    if (problem in listOf(ConversationProblem.ACCOUNT, ConversationProblem.ACCESS)) {
                        ConversationConnection.UNAVAILABLE
                    } else {
                        state.value.connection
                    },
            )
    }

    private suspend fun <T> safely(block: suspend () -> ConversationResult<T>): ConversationResult<T> =
        try {
            block()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            ConversationResult.Failure(ConversationProblem.PROVIDER)
        }
}
