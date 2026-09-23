package com.monsters.mobimon.feature.auth

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.monsters.mobimon.core.ui.MobiMonTheme

internal val copilotPreviewStates =
    linkedMapOf(
        "P51-introduction" to CopilotUiState.Introduction(),
        "P52-waiting" to CopilotUiState.Waiting("ABCD · 1234", 272),
        "P52C-address" to CopilotUiState.Waiting("ABCD · 1234", 272, showAddress = true),
        "P52B-expired" to CopilotUiState.Expired,
        "P53-connected" to CopilotUiState.Connected("@mobimon-driver", accountLabel = "예시 계정"),
        "P54-reconnect" to CopilotUiState.Reconnect("@mobimon-driver", "계정 인증이 만료되어 대화를 잠시 멈췄어요."),
        "P56-access" to CopilotUiState.AccessCheck("@mobimon-driver", CopilotAccessIssue.PERMISSION),
        "P55-disconnect" to CopilotUiState.Disconnect("@mobimon-driver"),
    )

@Composable
private fun Sample(state: CopilotUiState) {
    MobiMonTheme {
        CopilotConnectionScreen(
            state,
            {},
            interactionAllowed = true,
            qrCode = painterResource(R.drawable.copilot_preview_qr),
        )
    }
}

@Preview(name = "P51 · Introduction", group = "Copilot UI samples", widthDp = 1792, heightDp = 829)
@Composable
private fun IntroductionPreview() = Sample(copilotPreviewStates.getValue("P51-introduction"))

@Preview(name = "P52 · Approval", group = "Copilot UI samples", widthDp = 1792, heightDp = 829)
@Composable
private fun WaitingPreview() = Sample(copilotPreviewStates.getValue("P52-waiting"))

@Preview(name = "P52C · Address help", group = "Copilot UI samples", widthDp = 1792, heightDp = 829)
@Composable
private fun AddressPreview() = Sample(copilotPreviewStates.getValue("P52C-address"))

@Preview(name = "P52B · Expired", group = "Copilot UI samples", widthDp = 1792, heightDp = 829)
@Composable
private fun ExpiredPreview() = Sample(CopilotUiState.Expired)

@Preview(name = "P53 · Connected", group = "Copilot UI samples", widthDp = 1792, heightDp = 829)
@Composable
private fun ConnectedPreview() = Sample(copilotPreviewStates.getValue("P53-connected"))

@Preview(name = "P54 · Reconnect", group = "Copilot UI samples", widthDp = 1792, heightDp = 829)
@Composable
private fun ReconnectPreview() = Sample(copilotPreviewStates.getValue("P54-reconnect"))

@Preview(name = "P56 · Check access", group = "Copilot UI samples", widthDp = 1792, heightDp = 829)
@Composable
private fun AccessPreview() = Sample(copilotPreviewStates.getValue("P56-access"))

@Preview(name = "P55 · Disconnect", group = "Copilot UI samples", widthDp = 1792, heightDp = 829)
@Composable
private fun DisconnectPreview() = Sample(copilotPreviewStates.getValue("P55-disconnect"))

@Preview(
    name = "Head unit · Large text",
    group = "Copilot UI samples",
    widthDp = 1792,
    heightDp = 829,
    fontScale = 1.5f,
)
@Composable
private fun EnlargedTextPreview() = Sample(CopilotUiState.Introduction())
