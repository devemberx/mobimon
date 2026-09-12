package com.monsters.mobimon.feature.pet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.monsters.mobimon.core.domain.CompanionSettings
import com.monsters.mobimon.core.ui.MobiMonContentColumn
import com.monsters.mobimon.core.ui.MobiMonMessage

/** Displays committed display preferences; actual overlay support remains explicitly unavailable. */
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
) {
    MobiMonContentColumn(modifier = modifier) {
        if (!settingsAvailable) {
            MobiMonMessage(
                stringResource(
                    if (settingsLoadFailed) R.string.pet_settings_unavailable else R.string.pet_settings_loading,
                ),
                isError = settingsLoadFailed,
            )
            if (settingsLoadFailed) {
                Button(onClick = onRetry) { Text(stringResource(R.string.pet_settings_retry)) }
            }
            return@MobiMonContentColumn
        }
        SettingToggle(
            title = stringResource(R.string.pet_setting_visibility),
            description = stringResource(R.string.pet_setting_visibility_description),
            checked = settings.showOnVehicleHome,
            enabled = !visibilitySaving,
            onCheckedChange = onShowOnVehicleHomeChange,
        )
        if (visibilitySaving) MobiMonMessage(stringResource(R.string.pet_saving))
        visibilityError?.let { MobiMonMessage(it, isError = true) }
        SettingToggle(
            title = stringResource(R.string.pet_setting_motion),
            description = stringResource(R.string.pet_setting_motion_description),
            checked = settings.reducedMotion,
            enabled = !motionSaving,
            onCheckedChange = onReducedMotionChange,
        )
        if (motionSaving) MobiMonMessage(stringResource(R.string.pet_saving))
        motionError?.let { MobiMonMessage(it, isError = true) }
        MobiMonMessage(stringResource(R.string.pet_overlay_unavailable))
    }
}

@Composable
private fun SettingToggle(
    title: String,
    description: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 76.dp)
                    .toggleable(value = checked, enabled = enabled, role = Role.Switch, onValueChange = onCheckedChange)
                    .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(description, style = MaterialTheme.typography.bodyMedium)
            }
            Switch(checked = checked, onCheckedChange = null, enabled = enabled)
        }
    }
}
