package com.monsters.mobimon.feature.auth

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.monsters.mobimon.core.domain.AuthenticationProblem
import com.monsters.mobimon.core.domain.GitHubAccount
import com.monsters.mobimon.core.domain.GitHubSession
import org.junit.Assert.assertEquals
import org.junit.Test

class ConversationDraftViewModelTest {
    @Test
    fun sameOwnerKeepsSelectionAndUnfinishedKoreanInputInMemory() {
        val model = ConversationDraftViewModel()
        model.bind("profile", authenticated(1))
        val draft = TextFieldValue("오늘 하", TextRange(4), TextRange(3, 4))
        model.edit(draft)
        model.bind("profile", authenticated(1))
        assertEquals(draft, model.draft)
        assertEquals(TextFieldValue(), ConversationDraftViewModel().draft)
    }

    @Test
    fun profileChangeAccountChangeAndDisconnectClearDraft() {
        val model = ConversationDraftViewModel()
        model.bind("profile", authenticated(1))
        listOf("other" to 1L, "other" to 2L, "other" to null).forEach { (profile, account) ->
            model.edit(TextFieldValue("private draft"))
            model.bind(profile, account?.let(::authenticated) ?: GitHubSession.SignedOut)
            assertEquals(TextFieldValue(), model.draft)
        }
    }

    @Test
    fun temporaryAuthenticationProblemsRetainDraftButRevocationClearsIt() {
        val model = ConversationDraftViewModel()
        model.bind("profile", authenticated(1))
        val draft = TextFieldValue("작성 중", TextRange(4), TextRange(3, 4))
        model.edit(draft)
        listOf(
            GitHubSession.Restoring,
            GitHubSession.Failure(AuthenticationProblem.NETWORK),
            GitHubSession.Failure(AuthenticationProblem.PROVIDER),
            authenticated(1),
        ).forEach { session ->
            model.bind("profile", session)
            assertEquals(draft, model.draft)
        }
        model.bind("profile", GitHubSession.Failure(AuthenticationProblem.REAUTHENTICATION))
        assertEquals(TextFieldValue(), model.draft)
    }

    private fun authenticated(id: Long) = GitHubSession.Authenticated(GitHubAccount(id, "sample-$id"))
}
