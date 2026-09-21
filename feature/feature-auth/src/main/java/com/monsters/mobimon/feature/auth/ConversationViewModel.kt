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
internal class ConversationViewModel(
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
    private var generation = 0L
    private var conversationId = UUID.randomUUID().toString()
    private var messageId = 0L
    private var work: Job? = null
    private var history = emptyList<ConversationMessage>()

    init {
        viewModelScope.launch {
            authentication.session.collect { session ->
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
                if (session !is GitHubSession.Authenticated) {
                    cancel()
                    mutableState.value = state.value.copy(connection = ConversationConnection.SIGNED_OUT)
                } else if (state.value.connection == ConversationConnection.SIGNED_OUT) {
                    mutableState.value = state.value.copy(connection = ConversationConnection.UNAVAILABLE)
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
    }

    fun activate(interactionAllowed: Boolean) {
        active = true
        allowed = interactionAllowed
        if (!allowed) {
            cancel()
        }
    }

    fun deactivate() {
        active = false
        allowed = false
        cancel()
    }

    fun edit(value: TextFieldValue) {
        if (!active || !allowed || state.value.replyPending) return
        draft = value
    }

    fun send(text: String = draft.text) {
        if (!canInteract() ||
            work?.isActive == true ||
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
        send()
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
        history = emptyList()
        conversationId = UUID.randomUUID().toString()
        draft = TextFieldValue()
        mutableState.value = ConversationUiState()
    }

    private fun canInteract() =
        active &&
            allowed &&
            profileId != null &&
            (authentication.session.value as? GitHubSession.Authenticated)?.account?.id == accountId &&
            accountId != null

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
