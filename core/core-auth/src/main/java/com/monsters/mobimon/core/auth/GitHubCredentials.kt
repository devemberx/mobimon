package com.monsters.mobimon.core.auth

import com.monsters.mobimon.core.domain.AuthenticationProblem
import com.monsters.mobimon.core.domain.GitHubAccount

/** Deliberately not data classes: generated toString must never reveal credentials. */
internal class GitHubTokens(
    val accessToken: String,
    val expiresAtMillis: Long? = null,
    val refreshToken: String? = null,
    val refreshExpiresAtMillis: Long? = null,
) {
    override fun toString() = "GitHubTokens(REDACTED)"
}

internal class StoredCredential(
    val clientId: String,
    val tokens: GitHubTokens,
) {
    override fun toString() = "StoredCredential(REDACTED)"
}

internal class DeviceAuthorization(
    val deviceCode: String,
    val userCode: String,
    val verificationUri: String,
    val expiresInSeconds: Int,
    val intervalSeconds: Int,
) {
    override fun toString() = "DeviceAuthorization(REDACTED)"
}

internal sealed interface TokenPoll {
    data object Pending : TokenPoll

    data class SlowDown(
        val intervalSeconds: Int?,
    ) : TokenPoll

    data object Expired : TokenPoll

    class Approved(
        val tokens: GitHubTokens,
    ) : TokenPoll
}

internal class AuthenticationException(
    val problem: AuthenticationProblem,
) : Exception(problem.name)

internal interface GitHubApi {
    suspend fun requestCode(): DeviceAuthorization

    suspend fun poll(deviceCode: String): TokenPoll

    suspend fun refresh(refreshToken: String): GitHubTokens

    suspend fun account(accessToken: String): GitHubAccount
}

internal interface CredentialStore {
    suspend fun read(): StoredCredential?

    suspend fun write(credential: StoredCredential)

    suspend fun clear()
}
