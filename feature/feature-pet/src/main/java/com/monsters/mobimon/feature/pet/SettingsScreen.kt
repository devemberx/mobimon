package com.monsters.mobimon.feature.pet

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monsters.mobimon.core.domain.CompanionSettings
import com.monsters.mobimon.core.ui.CompanionIcon
import com.monsters.mobimon.core.ui.MobiMonFontFamily
import com.monsters.mobimon.core.ui.MobiMonMessage
import com.monsters.mobimon.core.ui.MobiMonSettingsColors
import com.monsters.mobimon.core.ui.MobiMonTheme

/** Figma P05 settings; unsupported service controls remain visibly unavailable. */
@Composable
fun SettingsScreen(
    settings: CompanionSettings,
    onShowOnVehicleHomeChange: (Boolean) -> Unit,
    onReducedMotionChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    visibilitySaving: Boolean = false,
    visibilityError: String? = null,
    motionSaving: Boolean = false,
    motionError: String? = null,
    settingsAvailable: Boolean = true,
    settingsLoadFailed: Boolean = false,
    onRetry: () -> Unit = {},
    onBack: () -> Unit = {},
    onDone: () -> Unit = {},
    parkedVerified: Boolean = false,
    simulatedVehicle: Boolean = false,
) {
    Box(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        SettingsScenery(Modifier.matchParentSize())
        BoxWithConstraints(Modifier.fillMaxSize().safeDrawingPadding()) {
            val fontScale = LocalDensity.current.fontScale
            val reference =
                maxWidth / fontScale >= 1400.dp &&
                    maxHeight / fontScale >= 800.dp &&
                    fontScale <= 1.2f &&
                    !settingsLoadFailed &&
                    visibilityError == null &&
                    motionError == null
            if (reference) {
                val scale = minOf(maxWidth.value / 2560f, maxHeight.value / 1268f)
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Box(Modifier.size(2560.dp * scale, 1268.dp * scale).testTag("settings-reference")) {
                        SettingsHeader(
                            onBack,
                            parkedVerified,
                            scale,
                            simulatedVehicle,
                            Modifier.offset(64.dp * scale, 64.dp * scale).width(
                                2432.dp * scale,
                            ),
                        )
                        if (settingsAvailable) {
                            SettingsRows(
                                settings,
                                onShowOnVehicleHomeChange,
                                onReducedMotionChange,
                                visibilitySaving,
                                motionSaving,
                                parkedVerified,
                                scale,
                                visibilityError,
                                motionError,
                                Modifier.offset(352.dp * scale, 224.dp * scale).width(1856.dp * scale),
                                reference = true,
                            )
                            Text(
                                stringResource(R.string.pet_settings_parked_notice),
                                Modifier.offset(800.dp * scale, 1064.dp * scale).width(960.dp * scale),
                                style = settingsStyle(28f, scale),
                                color = MobiMonSettingsColors.muted,
                                textAlign = TextAlign.Center,
                            )
                            SettingsDone(
                                onDone,
                                scale,
                                Modifier
                                    .offset(800.dp * scale, 1116.dp * scale)
                                    .width(960.dp * scale)
                                    .heightIn(min = (104.dp * scale).coerceAtLeast(76.dp)),
                            )
                        } else {
                            SettingsUnavailable(
                                settingsLoadFailed,
                                onRetry,
                                Modifier.offset(352.dp * scale, 224.dp * scale).width(1856.dp * scale),
                            )
                        }
                    }
                }
            } else {
                Column(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    SettingsHeader(onBack, parkedVerified, 0.75f, simulatedVehicle, Modifier.fillMaxWidth())
                    if (settingsAvailable) {
                        SettingsRows(
                            settings,
                            onShowOnVehicleHomeChange,
                            onReducedMotionChange,
                            visibilitySaving,
                            motionSaving,
                            parkedVerified,
                            0.75f,
                            visibilityError,
                            motionError,
                        )
                        if (settingsLoadFailed) SettingsUnavailable(true, onRetry)
                        Text(stringResource(R.string.pet_settings_parked_notice), color = MobiMonSettingsColors.muted)
                        SettingsDone(onDone, 0.75f, Modifier.fillMaxWidth().heightIn(min = 76.dp))
                    } else {
                        SettingsUnavailable(settingsLoadFailed, onRetry)
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsHeader(
    onBack: () -> Unit,
    parkedVerified: Boolean,
    scale: Float,
    simulatedVehicle: Boolean,
    modifier: Modifier = Modifier,
) {
    val backDescription = stringResource(R.string.pet_settings_back)
    BoxWithConstraints(modifier) {
        val compact = maxWidth / LocalDensity.current.fontScale < 900.dp
        Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    Modifier
                        .size((112.dp * scale).coerceAtLeast(76.dp), (104.dp * scale).coerceAtLeast(76.dp))
                        .clickable(role = Role.Button, onClick = onBack)
                        .semantics { contentDescription = backDescription }
                        .testTag("settings-back"),
                    shape = RoundedCornerShape(28.dp * scale),
                    color = MobiMonSettingsColors.row,
                    border = BorderStroke(1.dp, MobiMonSettingsColors.border),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        CompanionIcon(CompanionIcon.BACK, MobiMonSettingsColors.text, Modifier.size(48.dp * scale))
                    }
                }
                Spacer(Modifier.width(if (compact) 24.dp else 48.dp * scale))
                Column(Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.pet_settings_title),
                        Modifier.semantics { heading() },
                        style = settingsStyle(48f, scale, FontWeight.Bold),
                        color = MobiMonSettingsColors.text,
                    )
                    Text(
                        stringResource(R.string.pet_settings_subtitle),
                        style = settingsStyle(28f, scale),
                        color = MobiMonSettingsColors.muted,
                    )
                }
                if (!compact) SettingsParkingStatus(parkedVerified, simulatedVehicle, scale)
            }
            if (compact) SettingsParkingStatus(parkedVerified, simulatedVehicle, scale)
        }
    }
}

@Composable
private fun SettingsParkingStatus(
    parkedVerified: Boolean,
    simulatedVehicle: Boolean,
    scale: Float,
) {
    Surface(
        shape = RoundedCornerShape(40.dp * scale),
        color = MobiMonSettingsColors.row,
        border = BorderStroke(1.dp, MobiMonSettingsColors.border),
    ) {
        val parking =
            stringResource(if (parkedVerified) R.string.pet_settings_parked else R.string.pet_settings_parking_unknown)
        Text(
            if (simulatedVehicle) stringResource(R.string.pet_settings_simulated_parking, parking) else parking,
            Modifier.padding(horizontal = 24.dp * scale, vertical = 16.dp * scale),
            style = settingsStyle(28f, scale),
            color = MobiMonSettingsColors.muted,
        )
    }
}

@Composable
private fun SettingsRows(
    settings: CompanionSettings,
    onVisibilityChange: (Boolean) -> Unit,
    onMotionChange: (Boolean) -> Unit,
    visibilitySaving: Boolean,
    motionSaving: Boolean,
    parkedVerified: Boolean,
    scale: Float,
    visibilityError: String?,
    motionError: String?,
    modifier: Modifier = Modifier,
    reference: Boolean = false,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(24.dp * scale)) {
        SettingsRow(
            R.string.pet_settings_ai_title,
            R.string.pet_settings_ai_unavailable,
            R.string.pet_settings_unavailable_label,
            scale,
            reference = reference,
        )
        SettingsRow(
            R.string.pet_setting_visibility,
            R.string.pet_setting_visibility_description,
            if (settings.showOnVehicleHome) R.string.pet_settings_on else R.string.pet_settings_off,
            scale,
            settings.showOnVehicleHome,
            parkedVerified && !visibilitySaving,
            onVisibilityChange,
            reference = reference,
            feedback = visibilityError ?: if (visibilitySaving) stringResource(R.string.pet_saving) else null,
            isError =
                visibilityError != null,
        )
        SettingsRow(
            R.string.pet_settings_voice_title,
            R.string.pet_settings_voice_unavailable,
            R.string.pet_settings_unavailable_label,
            scale,
            reference = reference,
        )
        SettingsRow(
            R.string.pet_setting_motion,
            R.string.pet_setting_motion_description,
            if (settings.reducedMotion) R.string.pet_settings_on else R.string.pet_settings_off,
            scale,
            settings.reducedMotion,
            parkedVerified && !motionSaving,
            onMotionChange,
            reference = reference,
            feedback = motionError ?: if (motionSaving) stringResource(R.string.pet_saving) else null,
            isError =
                motionError != null,
        )
        SettingsRow(
            R.string.pet_settings_dnd_title,
            R.string.pet_settings_dnd_unavailable,
            R.string.pet_settings_unavailable_label,
            scale,
            reference = reference,
        )
    }
}

@Composable
private fun SettingsRow(
    title: Int,
    description: Int,
    status: Int,
    scale: Float,
    checked: Boolean? = null,
    enabled: Boolean = false,
    onCheckedChange: (Boolean) -> Unit = {},
    feedback: String? = null,
    isError: Boolean = false,
    reference: Boolean = false,
) {
    val control =
        if (checked != null) {
            Modifier.toggleable(checked, enabled = enabled, role = Role.Switch, onValueChange = onCheckedChange)
        } else {
            Modifier.semantics(mergeDescendants = true) { disabled() }
        }
    Surface(
        Modifier.fillMaxWidth().heightIn(min = 144.dp * scale).then(control),
        shape = RoundedCornerShape(32.dp * scale),
        color = MobiMonSettingsColors.row,
    ) {
        BoxWithConstraints(
            Modifier.padding(
                start = 64.dp * scale,
                end = 48.dp * scale,
                top = 20.dp * scale,
                bottom =
                    20.dp * scale,
            ),
        ) {
            if (maxWidth / LocalDensity.current.fontScale < 600.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    SettingsRowText(title, description, scale, feedback, isError)
                    SettingsRowStatus(checked, status, scale)
                }
            } else {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp * scale)) {
                    SettingsRowText(title, description, scale, feedback, isError, Modifier.weight(1f), reference)
                    Box(Modifier.align(Alignment.CenterVertically)) { SettingsRowStatus(checked, status, scale) }
                }
            }
        }
    }
}

@Composable
private fun SettingsRowText(
    title: Int,
    description: Int,
    scale: Float,
    feedback: String?,
    isError: Boolean,
    modifier: Modifier = Modifier,
    reference: Boolean = false,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp * scale)) {
        Text(
            stringResource(title),
            if (reference) Modifier.height(52.dp * scale).wrapContentHeight(unbounded = true) else Modifier,
            style = settingsStyle(40f, scale, lineHeight = 52f),
            color = MobiMonSettingsColors.text,
        )
        Text(
            feedback ?: stringResource(description),
            (if (reference) Modifier.height(44.dp * scale).wrapContentHeight(unbounded = true) else Modifier)
                .semantics { if (feedback != null) liveRegion = LiveRegionMode.Polite },
            style = settingsStyle(32f, scale, lineHeight = 44f),
            color = if (isError) MaterialTheme.colorScheme.error else MobiMonSettingsColors.muted,
        )
    }
}

@Composable
private fun SettingsRowStatus(
    checked: Boolean?,
    status: Int,
    scale: Float,
) {
    if (checked != null) {
        SettingsSwitch(checked, scale)
    } else {
        Text(stringResource(status), style = settingsStyle(30f, scale), color = MobiMonSettingsColors.muted)
    }
}

@Composable
private fun SettingsSwitch(
    checked: Boolean,
    scale: Float,
) {
    Surface(
        Modifier.size(280.dp * scale, 96.dp * scale),
        shape = RoundedCornerShape(48.dp * scale),
        color = if (checked) MobiMonSettingsColors.selected else MobiMonSettingsColors.row,
        border = BorderStroke(2.dp, MobiMonSettingsColors.border),
    ) {
        Box(Modifier.padding(12.dp * scale)) {
            Box(
                Modifier
                    .align(if (checked) Alignment.CenterEnd else Alignment.CenterStart)
                    .size(68.dp * scale)
                    .background(
                        if (checked) MobiMonSettingsColors.selectedThumb else MobiMonSettingsColors.muted,
                        RoundedCornerShape(50),
                    ),
            )
            Text(
                stringResource(if (checked) R.string.pet_settings_on else R.string.pet_settings_off),
                Modifier.align(Alignment.Center).offset(if (checked) (-38).dp * scale else 0.dp),
                style = settingsStyle(30f, scale),
                color = MobiMonSettingsColors.text,
            )
        }
    }
}

@Composable
private fun SettingsUnavailable(
    failed: Boolean,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(24.dp)) {
        MobiMonMessage(
            stringResource(if (failed) R.string.pet_settings_unavailable else R.string.pet_settings_loading),
            isError = failed,
        )
        if (failed) {
            Button(onClick = onRetry, modifier = Modifier.heightIn(min = 76.dp)) {
                Text(stringResource(R.string.pet_settings_retry))
            }
        }
    }
}

@Composable
private fun SettingsDone(
    onDone: () -> Unit,
    scale: Float,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onDone,
        modifier = modifier.testTag("settings-done"),
        shape = RoundedCornerShape(32.dp * scale),
        colors =
            ButtonDefaults.buttonColors(
                containerColor = MobiMonSettingsColors.text,
                contentColor = MaterialTheme.colorScheme.primaryContainer,
            ),
    ) {
        Text(stringResource(R.string.pet_settings_done), style = settingsStyle(36f, scale))
    }
}

private fun settingsStyle(
    size: Float,
    scale: Float,
    weight: FontWeight = FontWeight.Normal,
    lineHeight: Float = size * 1.2f,
) = TextStyle(
    fontFamily = MobiMonFontFamily,
    fontWeight = weight,
    fontSize = (size * scale).sp,
    lineHeight = (lineHeight * scale).sp,
    letterSpacing = 0.sp,
    platformStyle = PlatformTextStyle(includeFontPadding = false),
    lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None),
)

@Preview(name = "Settings ready · AAOS app content", widthDp = 1792, heightDp = 888, locale = "ko")
@Composable
private fun SettingsReadyPreview() {
    MobiMonTheme { SettingsScreen(CompanionSettings(), {}, {}, parkedVerified = true) }
}

@Preview(name = "Settings unavailable", widthDp = 1200, heightDp = 720)
@Composable
private fun SettingsUnavailablePreview() {
    MobiMonTheme { SettingsScreen(CompanionSettings(), {}, {}, settingsAvailable = false, settingsLoadFailed = true) }
}

@Preview(name = "Settings saving · AAOS app content", widthDp = 1792, heightDp = 888, locale = "ko")
@Composable
private fun SettingsSavingPreview() {
    MobiMonTheme { SettingsScreen(CompanionSettings(), {}, {}, visibilitySaving = true, parkedVerified = true) }
}

@Preview(name = "Settings restricted · enlarged text", widthDp = 1200, heightDp = 720, fontScale = 1.5f)
@Composable
private fun SettingsRestrictedPreview() {
    MobiMonTheme { SettingsScreen(CompanionSettings(), {}, {}) }
}
