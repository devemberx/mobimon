package com.monsters.mobimon.feature.auth

import com.monsters.mobimon.core.domain.AuthenticationProblem

/** Display data only. Credentials and provider work belong outside the UI. */
sealed interface CopilotUiState {
    data class Introduction(
        val connectionUnavailable: Boolean = false,
    ) : CopilotUiState

    data class Waiting(
        val userCode: String,
        val remainingSeconds: Int,
        val showAddress: Boolean = false,
        val checking: Boolean = false,
        val error: String? = null,
        val verificationUri: String = "https://github.com/login/device",
        val retrying: Boolean = false,
    ) : CopilotUiState

    /** GitHub authentication alone does not verify Copilot readiness. */
    data class AuthenticationStatus(
        val account: String? = null,
        val pending: Boolean = false,
        val problem: AuthenticationProblem? = null,
    ) : CopilotUiState

    data object Expired : CopilotUiState

    /** Render only after account approval and Copilot readiness are both verified. */
    data class Connected(
        val account: String,
        val accountLabel: String? = null,
    ) : CopilotUiState

    data class Reconnect(
        val account: String,
        val reason: String,
    ) : CopilotUiState

    data class AccessCheck(
        val account: String,
        val reason: CopilotAccessIssue,
        val checking: Boolean = false,
        val detail: String? = null,
    ) : CopilotUiState

    data class Disconnect(
        val account: String,
        val disconnecting: Boolean = false,
        val error: String? = null,
    ) : CopilotUiState
}

enum class CopilotAccessIssue { CHECKING, SUBSCRIPTION, PERMISSION, USAGE_LIMIT, SERVICE_UNAVAILABLE }

enum class CopilotAction {
    BACK,
    REQUEST_CODE,
    CANCEL,
    SHOW_ADDRESS,
    SHOW_QR,
    RECHECK,
    START_CONVERSATION,
    OPEN_SETTINGS,
    REVIEW_ACCESS,
    KEEP_CONNECTION,
    CONFIRM_DISCONNECT,
    DISCONNECT,
}
