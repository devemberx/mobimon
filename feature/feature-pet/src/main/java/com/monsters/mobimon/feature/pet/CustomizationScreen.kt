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
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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
import com.monsters.mobimon.core.domain.CosmeticInventory
import com.monsters.mobimon.core.domain.CosmeticSlot
import com.monsters.mobimon.core.ui.MobiMonContentColumn
import com.monsters.mobimon.core.ui.MobiMonMessage
import com.monsters.mobimon.core.ui.MobiMonPointSummary
import com.monsters.mobimon.core.ui.MobiMonSection
import com.monsters.mobimon.core.ui.PetAvatar

/** Free friends are selectable now; priced catalog entries remain absent until defined. */
@Composable
fun CustomizationScreen(
    inventory: CosmeticInventory?,
    onEquipFriend: (String) -> Unit,
    pointBalance: Long?,
    pointLoadFailed: Boolean,
    modifier: Modifier = Modifier,
    saving: Boolean = false,
    loadFailed: Boolean = false,
    saveFailed: Boolean = false,
    onRetry: () -> Unit = {},
) {
    val equipped = inventory?.equippedItemIds?.get(CosmeticSlot.FRIEND)
    var preview by rememberSaveable(equipped) { mutableStateOf(equipped ?: "friend:mobi") }
    MobiMonContentColumn(modifier = modifier) {
        MobiMonPointSummary(pointBalance, failed = pointLoadFailed)
        if (inventory == null) {
            MobiMonMessage(
                stringResource(if (loadFailed) R.string.pet_inventory_failed else R.string.pet_inventory_loading),
                isError = loadFailed,
            )
            if (loadFailed) {
                Button(onClick = onRetry, modifier = Modifier.fillMaxWidth().heightIn(min = 76.dp)) {
                    Text(stringResource(R.string.pet_retry))
                }
            }
            return@MobiMonContentColumn
        }
        if (loadFailed) {
            MobiMonMessage(stringResource(R.string.pet_inventory_failed), isError = true)
            Button(onClick = onRetry, modifier = Modifier.fillMaxWidth().heightIn(min = 76.dp)) {
                Text(stringResource(R.string.pet_retry))
            }
        }
        MobiMonSection(title = stringResource(R.string.pet_customization_friends)) {
            PetAvatar(modifier = Modifier.size(200.dp), friendId = preview)
            Text(
                stringResource(
                    if (preview ==
                        equipped
                    ) {
                        R.string.pet_appearance_applied
                    } else {
                        R.string.pet_appearance_preview
                    },
                ),
            )
            Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                listOf("friend:mobi", "friend:luna").forEach { friendId ->
                    Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.large) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .heightIn(min = 76.dp)
                                .selectable(selected = preview == friendId, role = Role.RadioButton) {
                                    preview =
                                        friendId
                                }.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                stringResource(
                                    if (friendId ==
                                        "friend:mobi"
                                    ) {
                                        R.string.pet_friend_mobi
                                    } else {
                                        R.string.pet_friend_luna
                                    },
                                ),
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                    }
                }
            }
            if (saving) MobiMonMessage(stringResource(R.string.pet_saving))
            if (saveFailed) MobiMonMessage(stringResource(R.string.pet_inventory_save_failed), isError = true)
            Button(
                onClick = { onEquipFriend(preview) },
                enabled = !saving && preview != equipped && preview in inventory.ownedItemIds,
                modifier = Modifier.fillMaxWidth().heightIn(min = 76.dp),
            ) { Text(stringResource(R.string.pet_appearance_apply)) }
        }
        MobiMonSection(title = stringResource(R.string.pet_customization_outfits)) {
            Text(stringResource(R.string.pet_catalog_pending))
        }
        MobiMonSection(title = stringResource(R.string.pet_customization_backgrounds)) {
            Text(stringResource(R.string.pet_catalog_pending))
        }
    }
}
