package com.monsters.mobimon.feature.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.monsters.mobimon.core.ui.MobiMonListItem
import com.monsters.mobimon.core.ui.MobiMonColors as Colors

internal fun CopilotUiState.hasReferenceLayout(
    interactionAllowed: Boolean,
    hasQr: Boolean,
): Boolean =
    (this is CopilotUiState.Introduction || interactionAllowed) &&
        when (this) {
            is CopilotUiState.AuthenticationStatus -> account != null && !pending && problem == null
            is CopilotUiState.Introduction -> true
            is CopilotUiState.Waiting -> error == null && !retrying && (showAddress || hasQr)
            is CopilotUiState.Disconnect -> error == null
            is CopilotUiState.AccessCheck -> detail == null && !checking && reason != CopilotAccessIssue.CHECKING
            else -> true
        }

@Composable
internal fun CopilotReferencePanel(
    state: CopilotUiState,
    onAction: (CopilotAction) -> Unit,
    friend: String,
    qrCode: Painter?,
    scale: Float,
    interactionAllowed: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(modifier.testTag("copilot-panel").background(Colors.panel, RoundedCornerShape(48.dp * scale))) {
        val panel = ReferencePanel(scale, onAction)
        with(panel) {
            when (state) {
                is CopilotUiState.Introduction -> {
                    steps(0)
                    tile(R.drawable.copilot_chat, 440f)
                    text(
                        stringResource(R.string.copilot_intro_heading),
                        1220f,
                        482.8f,
                        52f,
                        bold = true,
                        isHeading = true,
                    )
                    text(stringResource(R.string.copilot_intro_subtitle), 1220f, 545.8f, 32f, color = Colors.muted)
                    lines(R.string.copilot_intro_disclosure, 1064f, 663.1f, 34f, 66f)
                    rect(1064f, 846f, 1352f, 2f, 0f)
                    lines(
                        if (state.connectionUnavailable) R.string.copilot_unavailable else R.string.copilot_intro_note,
                        1064f,
                        908f,
                        30f,
                        if (state.connectionUnavailable) 44f else 54f,
                        if (state.connectionUnavailable) Colors.warning else Colors.muted,
                    )
                    action(
                        R.string.copilot_connect,
                        CopilotAction.REQUEST_CODE,
                        1064f,
                        1088f,
                        816f,
                        enabled = interactionAllowed && !state.connectionUnavailable,
                        icon = R.drawable.copilot_qr,
                        iconX = 1294.08f,
                    )
                    action(
                        R.string.copilot_later,
                        CopilotAction.CANCEL,
                        1908f,
                        1088f,
                        516f,
                        primary = false,
                        icon = R.drawable.copilot_chevron_back,
                        iconX = 2078.45f,
                    )
                    text(stringResource(R.string.copilot_policy_note), 1064f, 1249.9f, 26f, color = Colors.muted)
                }
                is CopilotUiState.Waiting -> waiting(state, qrCode)
                CopilotUiState.Expired -> {
                    close()
                    text(stringResource(R.string.copilot_step_account), 1064f, 430f, 40f, color = Colors.muted)
                    rect(1648f, 528f, 192f, 192f, 96f)
                    icon(R.drawable.copilot_clock, 1700f, 580f, 88f)
                    text(
                        stringResource(R.string.copilot_expired_heading),
                        1000f,
                        826f,
                        56f,
                        bold = true,
                        centeredWidth = 1488f,
                        isHeading = true,
                    )
                    text(
                        stringResource(R.string.copilot_expired_note),
                        1000f,
                        904f,
                        34f,
                        color = Colors.muted,
                        centeredWidth = 1488f,
                    )
                    action(
                        R.string.copilot_new_code,
                        CopilotAction.REQUEST_CODE,
                        1240f,
                        1044f,
                        1008f,
                        icon = R.drawable.copilot_qr,
                        iconX = 1584.32f,
                    )
                }
                is CopilotUiState.Connected, is CopilotUiState.AuthenticationStatus -> {
                    val connected = state as? CopilotUiState.Connected
                    val authenticated = state as? CopilotUiState.AuthenticationStatus
                    val account = connected?.account ?: requireNotNull(authenticated?.account)
                    steps(if (connected != null) 2 else 1)
                    tile(R.drawable.copilot_connected, 446f)
                    text(
                        stringResource(
                            if (connected !=
                                null
                            ) {
                                R.string.copilot_connected_heading
                            } else {
                                R.string.github_authenticated
                            },
                        ),
                        1220f,
                        492f,
                        52f,
                        bold = true,
                        isHeading = true,
                    )
                    text(
                        stringResource(R.string.copilot_connected_subtitle, friend),
                        1220f,
                        554f,
                        32f,
                        color = Colors.muted,
                    )
                    MobiMonListItem(
                        modifier = position(1064f, 628f).size(1352.dp * scale, 132.dp * scale),
                        leading = {
                            Icon(
                                painterResource(R.drawable.copilot_account),
                                null,
                                Modifier.size(46.dp * scale),
                                tint = Colors.accent,
                            )
                        },
                        trailing = {
                            if (authenticated != null) {
                                AccountDisconnectAction({ onAction(CopilotAction.CONFIRM_DISCONNECT) }, true, scale)
                            } else {
                                connected?.accountLabel?.let { label ->
                                    Text(label, style = copilotStyle(26f, scale), color = Colors.accent)
                                }
                            }
                        },
                    ) { Text(account, style = copilotStyle(38f, scale, true)) }
                    lines(
                        if (connected != null) R.string.copilot_connected_note else R.string.github_authenticated_note,
                        1088f,
                        843f,
                        34f,
                        66f,
                    )
                    action(
                        R.string.copilot_chat,
                        CopilotAction.START_CONVERSATION,
                        1064f,
                        1088f,
                        816f,
                        icon = R.drawable.copilot_chat,
                        iconX = 1294.08f,
                        label = stringResource(R.string.copilot_chat, friend),
                    )
                    action(
                        R.string.copilot_settings,
                        CopilotAction.OPEN_SETTINGS,
                        1908f,
                        1088f,
                        508f,
                        primary = false,
                        icon = R.drawable.copilot_settings,
                        iconX = 2057.04f,
                    )
                }
                is CopilotUiState.Reconnect -> {
                    tile(R.drawable.copilot_lock, 364f)
                    text(
                        stringResource(R.string.copilot_reconnect_heading),
                        1064f,
                        578f,
                        54f,
                        bold = true,
                        isHeading = true,
                    )
                    text(state.reason, 1064f, 658f, 34f, color = Colors.muted)
                    text(stringResource(R.string.copilot_reconnect_note), 1064f, 712f, 34f, color = Colors.muted)
                    rect(1064f, 810f, 1352f, 168f, 32f)
                    text(
                        stringResource(R.string.copilot_connected_account, state.account),
                        1112f,
                        874f,
                        34f,
                        bold = true,
                    )
                    text(stringResource(R.string.copilot_draft_preserved), 1112f, 930f, 30f, color = Colors.muted)
                    action(
                        R.string.copilot_reconnect,
                        CopilotAction.REQUEST_CODE,
                        1064f,
                        1088f,
                        816f,
                        icon = R.drawable.copilot_qr,
                        iconX = 1275.84f,
                    )
                    action(
                        R.string.copilot_later,
                        CopilotAction.CANCEL,
                        1908f,
                        1088f,
                        508f,
                        primary = false,
                        icon = R.drawable.copilot_chevron_back,
                        iconX = 2074.45f,
                    )
                }
                is CopilotUiState.AccessCheck -> {
                    steps(1)
                    tile(R.drawable.copilot_info, 444f)
                    text(
                        stringResource(R.string.copilot_access_heading),
                        1220f,
                        492f,
                        47f,
                        bold = true,
                        isHeading = true,
                    )
                    text(stringResource(R.string.copilot_access_subtitle), 1220f, 554f, 30f, color = Colors.muted)
                    rect(1064f, 628f, 1352f, 132f, 32f)
                    text(state.account, 1112f, 681f, 36f, bold = true)
                    text(stringResource(state.reason.message()), 1112f, 729f, 29f, color = Colors.warning)
                    lines(R.string.copilot_access_note, 1064f, 842f, 32f, 60f, Colors.muted)
                    action(
                        R.string.copilot_review_access,
                        CopilotAction.REVIEW_ACCESS,
                        1064f,
                        1088f,
                        816f,
                        icon = R.drawable.copilot_chevron_next,
                        iconX = 1240.2f,
                    )
                    action(
                        R.string.copilot_recheck,
                        CopilotAction.RECHECK,
                        1908f,
                        1088f,
                        508f,
                        primary = false,
                        icon = R.drawable.copilot_check,
                        iconX = 2038.8f,
                    )
                }
                is CopilotUiState.Disconnect -> {
                    tile(R.drawable.copilot_user, 364f)
                    text(
                        stringResource(R.string.copilot_disconnect_heading),
                        1064f,
                        577.8f,
                        52f,
                        bold = true,
                        isHeading = true,
                    )
                    rect(1064f, 630f, 1352f, 104f, 28f)
                    text(state.account, 1112f, 695.9f, 36f, bold = true)
                    lines(R.string.copilot_disconnect_note, 1064f, 820.1f, 34f, 66f)
                    if (state.disconnecting) {
                        text(
                            stringResource(R.string.copilot_disconnecting),
                            1064f,
                            1042f,
                            28f,
                            color = Colors.warning,
                        )
                    }
                    action(
                        R.string.copilot_keep,
                        CopilotAction.KEEP_CONNECTION,
                        1064f,
                        1088f,
                        640f,
                        enabled = !state.disconnecting,
                        icon = R.drawable.copilot_chevron_back,
                        iconX = 1259.97f,
                    )
                    action(
                        R.string.copilot_disconnect,
                        CopilotAction.DISCONNECT,
                        1732f,
                        1088f,
                        684f,
                        enabled = !state.disconnecting,
                        primary = false,
                        destructive = true,
                    )
                }
            }
        }
    }
}

// Coordinates retain the exported artboard origin; the app omits the OS bars.
private class ReferencePanel(
    val scale: Float,
    val onAction: (CopilotAction) -> Unit,
) {
    fun position(
        x: Float,
        y: Float,
    ) = Modifier.offset((x - 1000).dp * scale, (y - 292).dp * scale)

    @Composable
    fun text(
        value: String,
        x: Float,
        baseline: Float,
        size: Float,
        bold: Boolean = false,
        color: Color = Colors.text,
        centeredWidth: Float? = null,
        isHeading: Boolean = false,
        modifier: Modifier = Modifier,
    ) {
        CopilotPositionedText(
            value,
            x - 1000f,
            baseline - 292f,
            size,
            scale,
            modifier
                .then(if (centeredWidth != null) Modifier.width(centeredWidth.dp * scale) else Modifier)
                .then(if (isHeading) Modifier.semantics { heading() } else Modifier),
            bold = bold,
            color = color,
            textAlign = if (centeredWidth != null) TextAlign.Center else TextAlign.Start,
        )
    }

    @Composable
    fun lines(
        resource: Int,
        x: Float,
        baseline: Float,
        size: Float,
        gap: Float,
        color: Color = Colors.text,
    ) {
        stringResource(resource).lines().forEachIndexed { index, line ->
            text(line, x, baseline + index * gap, size, color = color)
        }
    }

    @Composable
    fun rect(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        radius: Float,
        color: Color = Colors.raised,
    ) {
        Box(
            position(x, y).size(width.dp * scale, height.dp * scale).background(
                color,
                RoundedCornerShape(
                    radius.dp * scale,
                ),
            ),
        )
    }

    @Composable
    fun icon(
        resource: Int,
        x: Float,
        y: Float,
        size: Float,
    ) {
        Image(painterResource(resource), null, position(x, y).size(size.dp * scale))
    }

    @Composable
    fun tile(
        resource: Int,
        y: Float,
    ) {
        rect(1064f, y, 112f, 112f, 36f)
        icon(resource, 1094f, y + 30f, 52f)
    }

    @Composable
    fun action(
        resource: Int,
        action: CopilotAction,
        x: Float,
        y: Float,
        width: Float,
        height: Float = 112f,
        primary: Boolean = true,
        enabled: Boolean = true,
        destructive: Boolean = false,
        icon: Int? = null,
        iconX: Float = x,
        label: String = stringResource(resource),
    ) {
        CopilotButton(
            label,
            { onAction(action) },
            scale,
            position(x, y).width(width.dp * scale),
            primary = primary,
            enabled = enabled,
            destructive = destructive,
            leadingIcon = icon,
            heightUnits = height,
            referenceLabelOffset = if (icon == null) 0f else 32f,
            referenceIconOffset = iconX - x,
        )
    }

    @Composable
    fun close() {
        val visual = 112.dp * scale
        val target = visual.coerceAtLeast(76.dp)
        IconButton(
            { onAction(CopilotAction.CANCEL) },
            position(2304f, 340f).offset(-(target - visual) / 2, -(target - visual) / 2).size(target),
        ) {
            Box(
                Modifier.size(visual).background(Colors.raised, RoundedCornerShape(50)),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painterResource(R.drawable.copilot_close),
                    stringResource(R.string.copilot_close),
                    Modifier.size(
                        44.dp * scale,
                    ),
                )
            }
        }
    }

    @Composable
    fun steps(current: Int) {
        val labels = listOf(R.string.copilot_step_account, R.string.copilot_step_access, R.string.copilot_step_chat)
        labels.forEachIndexed { index, resource ->
            val x = 1064f + 448f * index
            rect(
                x,
                340f,
                44f,
                44f,
                22f,
                if (index < current) {
                    Colors.success
                } else if (index == current) {
                    Colors.accent
                } else {
                    Colors.raised
                },
            )
            if (index < current) {
                icon(R.drawable.copilot_step_check, x, 340f, 44f)
            } else {
                text(
                    (index + 1).toString(),
                    x,
                    371f,
                    25f,
                    bold = true,
                    color = if (index == current) Colors.onButton else Colors.muted,
                    centeredWidth = 44f,
                )
            }
            val status =
                stringResource(
                    if (index < current) {
                        R.string.copilot_step_done
                    } else if (index == current) {
                        R.string.copilot_step_current
                    } else {
                        R.string.copilot_step_pending
                    },
                )
            text(
                stringResource(resource),
                x + 62f,
                372.1f,
                29f,
                bold = index == current,
                color = if (index == current) Colors.text else Colors.muted,
                modifier = Modifier.semantics { stateDescription = status },
            )
            if (index < 2) rect(x + 260f, 360f, 128f, 2f, 0f, Colors.border)
        }
    }

    @Composable
    fun waiting(
        state: CopilotUiState.Waiting,
        qrCode: Painter?,
    ) {
        close()
        text(
            stringResource(
                if (state.showAddress) R.string.copilot_address_heading else R.string.copilot_waiting_heading,
            ),
            1064f,
            430f,
            52f,
            bold = true,
            isHeading = true,
        )
        text(
            stringResource(
                if (state.showAddress) R.string.copilot_address_instruction else R.string.copilot_waiting_instruction,
            ),
            1064f,
            504f,
            32f,
            color = Colors.muted,
        )
        if (state.showAddress) {
            text(stringResource(R.string.copilot_address_label), 1064f, 630f, 30f, color = Colors.muted)
            SelectionContainer {
                text(stringResource(R.string.copilot_verification_address), 1064f, 708f, 54f, color = Colors.accent)
            }
            rect(1064f, 778f, 1352f, 2f, 0f)
            code(state.userCode, 1064f, 850f, 940f, R.string.copilot_code_label)
            timer(state.remainingSeconds, 2064f, 850f, 940f, 72f)
            status(state.checking, 1064f)
            action(
                R.string.copilot_back_to_qr,
                CopilotAction.SHOW_QR,
                1064f,
                1136f,
                640f,
                height = 100f,
                primary = false,
                icon = R.drawable.copilot_chevron_back,
                iconX = 1205.25f,
            )
            action(
                R.string.copilot_approved,
                CopilotAction.RECHECK,
                1732f,
                1136f,
                684f,
                height = 100f,
                primary = false,
                enabled = !state.checking,
                icon = R.drawable.copilot_check,
                iconX = 1950.8f,
            )
        } else {
            rect(1064f, 568f, 536f, 536f, 32f, Color.White)
            Image(
                requireNotNull(qrCode),
                stringResource(R.string.copilot_qr_description),
                position(1088f, 592f).size(488.dp * scale),
            )
            timer(state.remainingSeconds, 1664f, 616f, 742f, 112f)
            rect(1664f, 790f, 752f, 2f, 0f)
            code(state.userCode, 1664f, 862f, 952f, R.string.copilot_phone_code_label)
            status(state.checking, 1664f)
            QrHelp(onAction, scale, position(1064f, 1136f))
            action(
                R.string.copilot_approved,
                CopilotAction.RECHECK,
                1664f,
                1136f,
                752f,
                height = 100f,
                primary = false,
                enabled = !state.checking,
                icon = R.drawable.copilot_check,
                iconX = 1916.8f,
            )
        }
    }

    @Composable
    fun code(
        value: String,
        x: Float,
        labelBaseline: Float,
        baseline: Float,
        label: Int,
    ) {
        text(stringResource(label), x, labelBaseline, 32f, color = Colors.muted)
        SelectionContainer { text(value, x, baseline, 64f, bold = true) }
    }

    @Composable
    fun timer(
        seconds: Int,
        x: Float,
        labelBaseline: Float,
        baseline: Float,
        size: Float,
    ) {
        text(stringResource(R.string.copilot_time_label), x, labelBaseline, 32f, color = Colors.muted)
        text(
            stringResource(R.string.copilot_time_value, seconds / 60, seconds % 60),
            x,
            baseline,
            size,
            bold = true,
            color = Colors.warning,
            modifier = Modifier.testTag("copilot-countdown"),
        )
    }

    @Composable
    fun status(
        checking: Boolean,
        x: Float,
    ) {
        rect(x, 1026f, 14f, 14f, 7f, Colors.success)
        text(
            stringResource(if (checking) R.string.copilot_checking_status else R.string.copilot_waiting_status),
            x + 32,
            1046f,
            32f,
            color = Colors.muted,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
        )
    }
}

@Composable
private fun QrHelp(
    onAction: (CopilotAction) -> Unit,
    scale: Float,
    modifier: Modifier = Modifier,
) {
    val visualHeight = 100.dp * scale
    val targetHeight = visualHeight.coerceAtLeast(76.dp)
    var focused by remember { mutableStateOf(false) }
    Box(
        modifier
            .offset(y = -(targetHeight - visualHeight) / 2)
            .size(536.dp * scale, targetHeight)
            .onFocusChanged { focused = it.isFocused }
            .then(if (focused) Modifier.border(3.dp, Colors.accent, RoundedCornerShape(50)) else Modifier)
            .clickable(role = Role.Button) { onAction(CopilotAction.SHOW_ADDRESS) },
        contentAlignment = Alignment.Center,
    ) {
        Box(Modifier.size(536.dp * scale, visualHeight)) {
            CopilotPositionedText(
                stringResource(R.string.copilot_qr_help),
                124f,
                63f,
                30f,
                scale,
                color = Colors.accent,
            )
            Image(
                painterResource(R.drawable.copilot_chevron_next_small),
                null,
                Modifier.offset(456.59.dp * scale, 36.dp * scale).size(28.dp * scale),
            )
        }
    }
}
