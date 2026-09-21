package com.monsters.mobimon.core.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/** Public authentication state never contains provider credentials. */
interface GitHubAuthentication {
    val session: StateFlow<GitHubSession>
    val configured: Boolean

    suspend fun restore()

    fun signIn(): Flow<GitHubSignIn>

    suspend fun disconnect()
}

data class GitHubAccount(
    val id: Long,
    val login: String,
)

sealed interface GitHubSession {
    data object Restoring : GitHubSession

    data object SignedOut : GitHubSession

    data class Authenticated(
        val account: GitHubAccount,
    ) : GitHubSession

    data class Failure(
        val problem: AuthenticationProblem,
    ) : GitHubSession
}

sealed interface GitHubSignIn {
    data object Requesting : GitHubSignIn

    data class Waiting(
        val userCode: String,
        val verificationUri: String,
        val remainingSeconds: Int,
        val retrying: Boolean = false,
    ) : GitHubSignIn

    data object Complete : GitHubSignIn

    data object Expired : GitHubSignIn

    data class Failed(
        val problem: AuthenticationProblem,
    ) : GitHubSignIn
}

enum class AuthenticationProblem {
    CONFIGURATION,
    NETWORK,
    DENIED,
    REAUTHENTICATION,
    STORAGE,
    PROVIDER,
    RESTRICTED,
}
