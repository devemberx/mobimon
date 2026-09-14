package com.monsters.mobimon.feature.auth.preview

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.monsters.mobimon.core.ui.MobiMonTheme
import com.monsters.mobimon.feature.auth.CopilotAccessIssue
import com.monsters.mobimon.feature.auth.CopilotAction
import com.monsters.mobimon.feature.auth.CopilotConnectionScreen
import com.monsters.mobimon.feature.auth.CopilotUiState
import com.monsters.mobimon.feature.auth.R
import kotlinx.coroutines.delay
import com.monsters.mobimon.feature.auth.R as AuthR

/** Isolated UI rehearsal; no credentials, network calls or persistent account state. */
class CopilotPreviewActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MobiMonTheme { CopilotPreview(onExit = ::finish) } }
    }
}

private enum class PreviewStep { INTRO, QR, ADDRESS, EXPIRED, CONNECTED, RECONNECT, ACCESS, DISCONNECT, SETTINGS, CHAT }

@Composable
private fun CopilotPreview(onExit: () -> Unit) {
    var step by rememberSaveable { mutableStateOf(PreviewStep.INTRO) }
    var pending by rememberSaveable { mutableStateOf(false) }
    var reducedMotion by rememberSaveable { mutableStateOf(false) }
    var showAccessNotice by rememberSaveable { mutableStateOf(false) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    fun navigate(next: PreviewStep) {
        pending = false
        step = next
    }

    fun back() {
        when (step) {
            PreviewStep.INTRO -> onExit()
            PreviewStep.QR, PreviewStep.EXPIRED -> navigate(PreviewStep.INTRO)
            PreviewStep.ADDRESS -> navigate(PreviewStep.QR)
            PreviewStep.CHAT, PreviewStep.SETTINGS -> navigate(PreviewStep.CONNECTED)
            else -> navigate(PreviewStep.SETTINGS)
        }
    }
    BackHandler(onBack = ::back)
    LaunchedEffect(pending, step, lifecycle) {
        if (pending) {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                delay(400)
                navigate(if (step == PreviewStep.DISCONNECT) PreviewStep.INTRO else PreviewStep.CONNECTED)
            }
        }
    }
    Surface(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().safeDrawingPadding()) {
            Text(
                stringResource(R.string.copilot_preview_disclosure),
                Modifier.padding(horizontal = 16.dp),
                fontSize = 14.sp,
                lineHeight = 18.sp,
            )
            LazyRow(verticalAlignment = Alignment.CenterVertically) {
                item {
                    TextButton(
                        onClick = { navigate(PreviewStep.INTRO) },
                    ) { Text(stringResource(R.string.copilot_preview_reset)) }
                }
                item {
                    TextButton(
                        onClick = { navigate(PreviewStep.EXPIRED) },
                    ) { Text(stringResource(R.string.copilot_preview_expire)) }
                }
                item {
                    TextButton(
                        onClick = { navigate(PreviewStep.RECONNECT) },
                    ) { Text(stringResource(R.string.copilot_preview_reconnect)) }
                }
                item {
                    TextButton(
                        onClick = { navigate(PreviewStep.ACCESS) },
                    ) { Text(stringResource(R.string.copilot_preview_access)) }
                }
                item {
                    Row(
                        Modifier.toggleable(
                            reducedMotion,
                            role = Role.Checkbox,
                            onValueChange = { reducedMotion = it },
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(checked = reducedMotion, onCheckedChange = null)
                        Text(stringResource(R.string.copilot_preview_reduce_motion))
                    }
                }
            }
            val state = previewState(step, pending)
            if (state != null) {
                CopilotConnectionScreen(
                    state = state,
                    onAction = { action ->
                        when (action) {
                            CopilotAction.BACK -> back()
                            CopilotAction.CANCEL -> navigate(PreviewStep.INTRO)
                            CopilotAction.REQUEST_CODE -> navigate(PreviewStep.QR)
                            CopilotAction.SHOW_ADDRESS -> navigate(PreviewStep.ADDRESS)
                            CopilotAction.SHOW_QR -> navigate(PreviewStep.QR)
                            CopilotAction.RECHECK, CopilotAction.DISCONNECT -> if (!pending) pending = true
                            CopilotAction.START_CONVERSATION -> navigate(PreviewStep.CHAT)
                            CopilotAction.OPEN_SETTINGS, CopilotAction.KEEP_CONNECTION -> navigate(PreviewStep.SETTINGS)
                            CopilotAction.REVIEW_ACCESS -> showAccessNotice = true
                        }
                    },
                    modifier = Modifier.weight(1f),
                    interactionAllowed = true,
                    simulatedVehicle = true,
                    qrCode = painterResource(AuthR.drawable.copilot_preview_qr),
                    reducedMotion = reducedMotion,
                )
            } else {
                Column(
                    Modifier.weight(1f).fillMaxWidth().padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        stringResource(
                            if (step ==
                                PreviewStep.SETTINGS
                            ) {
                                R.string.copilot_preview_settings
                            } else {
                                R.string.copilot_preview_chat
                            },
                        ),
                    )
                    if (step == PreviewStep.SETTINGS) {
                        Button(
                            onClick = { navigate(PreviewStep.DISCONNECT) },
                        ) { Text(stringResource(AuthR.string.copilot_disconnect)) }
                    }
                    Button(
                        onClick = { navigate(PreviewStep.CONNECTED) },
                    ) { Text(stringResource(R.string.copilot_preview_return)) }
                }
            }
        }
    }
    if (showAccessNotice) {
        AlertDialog(
            onDismissRequest = { showAccessNotice = false },
            title = { Text(stringResource(AuthR.string.copilot_review_access)) },
            text = { Text(stringResource(R.string.copilot_preview_access_notice)) },
            confirmButton = {
                TextButton(
                    onClick = { showAccessNotice = false },
                ) { Text(stringResource(R.string.copilot_preview_close)) }
            },
        )
    }
}

@Composable
private fun previewState(
    step: PreviewStep,
    pending: Boolean,
): CopilotUiState? =
    when (step) {
        PreviewStep.INTRO -> CopilotUiState.Introduction()
        PreviewStep.QR, PreviewStep.ADDRESS ->
            CopilotUiState.Waiting(
                "ABCD · 1234",
                272,
                step == PreviewStep.ADDRESS,
                pending,
            )
        PreviewStep.EXPIRED -> CopilotUiState.Expired
        PreviewStep.CONNECTED ->
            CopilotUiState.Connected(
                "@mobimon-driver",
                stringResource(R.string.copilot_preview_account),
            )
        PreviewStep.RECONNECT ->
            CopilotUiState.Reconnect(
                "@mobimon-driver",
                stringResource(R.string.copilot_preview_reconnect_reason),
            )
        PreviewStep.ACCESS ->
            CopilotUiState.AccessCheck(
                "@mobimon-driver",
                CopilotAccessIssue.SUBSCRIPTION,
                checking = pending,
            )
        PreviewStep.DISCONNECT -> CopilotUiState.Disconnect("@mobimon-driver", disconnecting = pending)
        PreviewStep.SETTINGS, PreviewStep.CHAT -> null
    }
