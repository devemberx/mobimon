package com.monsters.mobimon.feature.pet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.monsters.mobimon.core.domain.CompanionSettings
import com.monsters.mobimon.core.ui.MobiMonBackButton
import com.monsters.mobimon.core.ui.MobiMonButton
import com.monsters.mobimon.core.ui.MobiMonContentColumn
import com.monsters.mobimon.core.ui.MobiMonDimensions
import com.monsters.mobimon.core.ui.MobiMonListItem
import com.monsters.mobimon.core.ui.MobiMonMessage
import com.monsters.mobimon.core.ui.MobiMonParkingBadge
import com.monsters.mobimon.core.ui.MobiMonSourceBadge
import com.monsters.mobimon.core.ui.R as CoreUiR

/** Independent preference controls; final composition remains feature-owned. */
@Composable
fun SettingsScreen(
    settings: CompanionSettings,
    onReducedMotionChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    onDebugModeChange: (Boolean) -> Unit = {},
    debugModeAvailable: Boolean = false,
    motionSaving: Boolean = false,
    motionError: String? = null,
    debugSaving: Boolean = false,
    debugError: String? = null,
    settingsAvailable: Boolean = true,
    settingsLoadFailed: Boolean = false,
    onRetry: () -> Unit = {},
    onBack: () -> Unit = {},
    onDone: () -> Unit = {},
    parkedVerified: Boolean = false,
    simulatedVehicle: Boolean = false,
    onOpenCopilot: (() -> Unit)? = null,
) {
    val backDescription = stringResource(R.string.pet_settings_back)
    BoxWithConstraints(modifier.fillMaxSize()) {
        val reference = maxWidth >= 1400.dp && maxHeight >= 800.dp && LocalDensity.current.fontScale <= 1f
        val parkingScale = if (reference) minOf(maxWidth.value / 2560f, maxHeight.value / 1268f) else 0.75f
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(Modifier.fillMaxSize()) {
                Row(
                    Modifier.fillMaxWidth().padding(MobiMonDimensions.contentPadding),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(MobiMonDimensions.contentGap),
                ) {
                    MobiMonBackButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("settings-back"),
                        contentDescription = backDescription,
                    )
                    Text(
                        stringResource(R.string.pet_settings_title),
                        Modifier.weight(1f).semantics { heading() },
                        style = MaterialTheme.typography.headlineMedium,
                    )
                }
                MobiMonContentColumn(Modifier.weight(1f).fillMaxWidth()) {
                    MobiMonParkingBadge(
                        scale = parkingScale,
                        status =
                            stringResource(
                                if (parkedVerified) {
                                    CoreUiR.string.mobimon_parking_confirmed
                                } else {
                                    CoreUiR.string.mobimon_parking_unconfirmed
                                },
                            ),
                    )
                    if (simulatedVehicle) {
                        MobiMonSourceBadge(simulated = true)
                    }
                    Text(
                        stringResource(R.string.pet_settings_subtitle),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (settingsAvailable) {
                        SettingsItem(
                            R.string.pet_settings_ai_title,
                            if (onOpenCopilot ==
                                null
                            ) {
                                R.string.pet_settings_ai_unavailable
                            } else {
                                R.string.pet_settings_ai_connect_description
                            },
                            if (onOpenCopilot ==
                                null
                            ) {
                                R.string.pet_settings_unavailable_label
                            } else {
                                R.string.pet_settings_ai_connect
                            },
                            enabled = parkedVerified,
                            onClick = onOpenCopilot,
                        )
                        SettingsItem(
                            R.string.pet_settings_voice_title,
                            R.string.pet_settings_voice_unavailable,
                            R.string.pet_settings_unavailable_label,
                        )
                        SettingsItem(
                            R.string.pet_setting_motion,
                            R.string.pet_setting_motion_description,
                            if (settings.reducedMotion) R.string.pet_settings_on else R.string.pet_settings_off,
                            checked = settings.reducedMotion,
                            enabled = parkedVerified && !motionSaving,
                            onCheckedChange = onReducedMotionChange,
                            feedback = motionError ?: if (motionSaving) stringResource(R.string.pet_saving) else null,
                            isError = motionError != null,
                        )
                        SettingsItem(
                            R.string.pet_settings_dnd_title,
                            R.string.pet_settings_dnd_unavailable,
                            R.string.pet_settings_unavailable_label,
                        )
                        if (debugModeAvailable) {
                            SettingsItem(
                                R.string.pet_settings_debug_title,
                                R.string.pet_settings_debug_description,
                                if (settings.debugModeEnabled) R.string.pet_settings_on else R.string.pet_settings_off,
                                checked = settings.debugModeEnabled,
                                enabled = parkedVerified && !debugSaving,
                                onCheckedChange = onDebugModeChange,
                                feedback = debugError ?: if (debugSaving) stringResource(R.string.pet_saving) else null,
                                isError = debugError != null,
                            )
                        }
                    }
                    if (!settingsAvailable || settingsLoadFailed) {
                        MobiMonMessage(
                            stringResource(
                                if (settingsLoadFailed) {
                                    R.string.pet_settings_unavailable
                                } else {
                                    R.string.pet_settings_loading
                                },
                            ),
                            isError = settingsLoadFailed,
                        )
                        if (settingsLoadFailed) {
                            MobiMonButton(
                                onRetry,
                            ) { Text(stringResource(R.string.pet_settings_retry)) }
                        }
                    }
                    Text(
                        stringResource(R.string.pet_settings_parked_notice),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                MobiMonButton(
                    onDone,
                    Modifier.fillMaxWidth().padding(MobiMonDimensions.contentPadding).testTag("settings-done"),
                ) {
                    Text(stringResource(R.string.pet_settings_done))
                }
            }
        }
    }
}

@Composable
private fun SettingsItem(
    title: Int,
    description: Int,
    status: Int,
    checked: Boolean? = null,
    enabled: Boolean = false,
    onCheckedChange: (Boolean) -> Unit = {},
    onClick: (() -> Unit)? = null,
    feedback: String? = null,
    isError: Boolean = false,
) {
    val interaction =
        when {
            onClick != null -> Modifier.clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            checked != null ->
                Modifier.toggleable(
                    checked,
                    enabled = enabled,
                    role = Role.Switch,
                    onValueChange = onCheckedChange,
                )
            else -> Modifier.semantics(mergeDescendants = true) { disabled() }
        }
    MobiMonListItem(
        Modifier.fillMaxWidth().heightIn(min = 76.dp).then(interaction),
        supporting = {
            Text(
                feedback ?: stringResource(description),
                Modifier.semantics { if (feedback != null) liveRegion = LiveRegionMode.Polite },
                style = MaterialTheme.typography.bodyMedium,
                color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        trailing = {
            if (checked != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(stringResource(status))
                    Switch(
                        checked,
                        onCheckedChange = null,
                        enabled = enabled,
                        modifier = Modifier.clearAndSetSemantics {},
                    )
                }
            } else {
                Text(stringResource(status), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
    ) { Text(stringResource(title), style = MaterialTheme.typography.titleLarge) }
}
