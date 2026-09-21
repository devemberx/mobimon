package com.monsters.mobimon.feature.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import com.monsters.mobimon.core.domain.AuthenticationProblem
import com.monsters.mobimon.core.domain.GitHubSession

/** Activity memory only, including selection and unfinished IME composition. Never saved to a Bundle. */
internal class ConversationDraftViewModel : ViewModel() {
    var draft by mutableStateOf(TextFieldValue())
        private set

    private var owner: Pair<String, Long?>? = null

    fun bind(
        profileId: String,
        session: GitHubSession,
    ) {
        val accountId =
            when (session) {
                is GitHubSession.Authenticated -> session.account.id
                GitHubSession.SignedOut -> null
                GitHubSession.Restoring -> owner?.second
                is GitHubSession.Failure ->
                    when (session.problem) {
                        AuthenticationProblem.NETWORK, AuthenticationProblem.PROVIDER -> owner?.second
                        else -> null
                    }
            }
        val next = profileId to accountId
        if (owner != next) draft = TextFieldValue()
        owner = next
    }

    fun edit(value: TextFieldValue) {
        draft = value
    }
}
