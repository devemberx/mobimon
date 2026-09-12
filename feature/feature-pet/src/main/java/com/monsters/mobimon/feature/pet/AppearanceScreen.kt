package com.monsters.mobimon.feature.pet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.monsters.mobimon.core.domain.PetAppearance
import com.monsters.mobimon.core.ui.MobiMonContentColumn
import com.monsters.mobimon.core.ui.MobiMonMessage
import com.monsters.mobimon.core.ui.MobiMonPointSummary
import com.monsters.mobimon.core.ui.PetAvatar

/** Requests appearance changes while continuing to display the caller's committed choice. */
@Composable
fun AppearanceScreen(
    appearance: PetAppearance,
    onAppearanceChange: (PetAppearance) -> Unit,
    modifier: Modifier = Modifier,
    isSaving: Boolean = false,
    errorMessage: String? = null,
    pointBalance: Long? = null,
    pointLoadFailed: Boolean = false,
) {
    var preview by rememberSaveable(appearance) { mutableStateOf(appearance) }
    MobiMonContentColumn(modifier = modifier) {
        Text(stringResource(R.string.pet_appearance_intro))
        MobiMonPointSummary(pointBalance, failed = pointLoadFailed)
        PetAvatar(modifier = Modifier.size(180.dp), appearanceKey = preview.name)
        if (isSaving) MobiMonMessage(stringResource(R.string.pet_saving))
        errorMessage?.let { MobiMonMessage(it, isError = true) }
        Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            PetAppearance.entries.forEach { option ->
                Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface) {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .heightIn(min = 76.dp)
                                .selectable(
                                    selected = preview == option,
                                    enabled = !isSaving,
                                    role = Role.RadioButton,
                                    onClick = { preview = option },
                                ).padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        PetAvatar(modifier = Modifier.size(64.dp), appearanceKey = option.name)
                        Text(
                            stringResource(
                                if (option == PetAppearance.GOLDEN) {
                                    R.string.pet_appearance_golden
                                } else {
                                    R.string.pet_appearance_cream
                                },
                            ),
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        RadioButton(selected = preview == option, onClick = null, enabled = !isSaving)
                    }
                }
            }
        }
        Text(
            stringResource(
                if (preview ==
                    appearance
                ) {
                    R.string.pet_appearance_applied
                } else {
                    R.string.pet_appearance_preview
                },
            ),
        )
        Button(
            onClick = { onAppearanceChange(preview) },
            enabled = preview != appearance && !isSaving,
            modifier = Modifier.fillMaxWidth().heightIn(min = 76.dp),
        ) { Text(stringResource(R.string.pet_appearance_apply)) }
    }
}
