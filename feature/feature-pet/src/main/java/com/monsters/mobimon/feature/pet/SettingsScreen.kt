package com.monsters.mobimon.feature.pet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.monsters.mobimon.core.domain.CompanionSettings
import com.monsters.mobimon.core.ui.MobiMonButton
import com.monsters.mobimon.core.ui.MobiMonButtonStyle
import com.monsters.mobimon.core.ui.MobiMonContentColumn
import com.monsters.mobimon.core.ui.MobiMonDimensions
import com.monsters.mobimon.core.ui.MobiMonListItem
import com.monsters.mobimon.core.ui.MobiMonMessage
import com.monsters.mobimon.core.ui.MobiMonStatusBadge
import com.monsters.mobimon.core.ui.MobiMonStatusTone
import com.monsters.mobimon.core.ui.MobiMonTheme

/** Current preference behavior on v4 primitives; final composition remains feature-owned. */
@Composable
fun SettingsScreen(
    settings: CompanionSettings,
    onReducedMotionChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    motionSaving: Boolean = false,
    motionError: String? = null,
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
    Surface(modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier.fillMaxWidth().padding(MobiMonDimensions.contentPadding),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(MobiMonDimensions.contentGap),
            ) {
                MobiMonButton(
                    onBack,
                    Modifier.testTag("settings-back").semantics { contentDescription = backDescription },
                    style = MobiMonButtonStyle.SECONDARY,
                ) { Text(stringResource(com.monsters.mobimon.core.ui.R.string.mobimon_back)) }
                Text(
                    stringResource(R.string.pet_settings_title),
                    Modifier.weight(1f).semantics { heading() },
                    style = MaterialTheme.typography.headlineMedium,
                )
            }
            MobiMonContentColumn(Modifier.weight(1f).fillMaxWidth()) {
                val parking =
                    stringResource(
                        if (parkedVerified) R.string.pet_settings_parked else R.string.pet_settings_parking_unknown,
                    )
                MobiMonStatusBadge(
                    tone = if (parkedVerified) MobiMonStatusTone.SUCCESS else MobiMonStatusTone.INFORMATION,
                ) {
                    Text(
                        if (simulatedVehicle) {
                            stringResource(
                                R.string.pet_settings_simulated_parking,
                                parking,
                            )
                        } else {
                            parking
                        },
                    )
                }
                Text(stringResource(R.string.pet_settings_subtitle), color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                    if (settingsLoadFailed) MobiMonButton(onRetry) { Text(stringResource(R.string.pet_settings_retry)) }
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

@Preview(name = "Settings · v4 primitives", widthDp = 1792, heightDp = 888, locale = "ko")
@Preview(name = "Settings · enlarged text", widthDp = 800, heightDp = 900, fontScale = 1.5f, locale = "ko")
@Composable
private fun SettingsPreview() {
    MobiMonTheme { SettingsScreen(CompanionSettings(), {}, parkedVerified = true) }
}
