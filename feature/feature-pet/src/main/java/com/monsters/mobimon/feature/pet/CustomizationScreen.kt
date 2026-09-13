package com.monsters.mobimon.feature.pet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monsters.mobimon.core.domain.CosmeticInventory
import com.monsters.mobimon.core.domain.CosmeticItem
import com.monsters.mobimon.core.domain.CosmeticSlot
import com.monsters.mobimon.core.ui.MobiMonMessage
import com.monsters.mobimon.core.ui.MobiMonPointSummary
import com.monsters.mobimon.core.ui.PetAvatar

@Composable
fun CustomizationScreen(
    inventory: CosmeticInventory?,
    catalog: List<CosmeticItem>,
    selectedItemId: String?,
    purchasing: Boolean,
    purchaseFailed: Boolean,
    onSelectItem: (String?) -> Unit,
    onPurchaseItem: (String, Long) -> Unit,
    onEquipItem: (String) -> Unit,
    onEquipFriend: (String) -> Unit,
    pointBalance: Long?,
    pointLoadFailed: Boolean,
    modifier: Modifier = Modifier,
    saving: Boolean = false,
    loadFailed: Boolean = false,
    saveFailed: Boolean = false,
    onRetry: () -> Unit = {},
) {
    var activeTab by rememberSaveable { mutableStateOf(CosmeticSlot.FRIEND) }
    var subTab by rememberSaveable { mutableStateOf(0) } // 0: 전체, 1: 보유 중

    if (inventory == null) {
        Column(
            modifier = modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            MobiMonMessage(
                stringResource(if (loadFailed) R.string.pet_inventory_failed else R.string.pet_inventory_loading),
                isError = loadFailed,
            )
            if (loadFailed) {
                Spacer(Modifier.height(16.dp))
                Button(onClick = onRetry, modifier = Modifier.fillMaxWidth().heightIn(min = 76.dp)) {
                    Text(stringResource(R.string.pet_retry))
                }
            }
        }
        return
    }

    val currentEquippedFriendId = inventory.equippedItemIds[CosmeticSlot.FRIEND] ?: "friend:mobi"
    val currentEquippedAccessoryId = inventory.equippedItemIds[CosmeticSlot.ACCESSORY]

    val effectiveSelectedId =
        selectedItemId ?: when (activeTab) {
            CosmeticSlot.FRIEND -> currentEquippedFriendId
            CosmeticSlot.ACCESSORY -> currentEquippedAccessoryId ?: "accessory:necklace"
            CosmeticSlot.BACKGROUND -> inventory.equippedItemIds[CosmeticSlot.BACKGROUND]
            else -> null
        }

    val previewFriendId =
        if (activeTab ==
            CosmeticSlot.FRIEND
        ) {
            effectiveSelectedId ?: "friend:mobi"
        } else {
            currentEquippedFriendId
        }
    val previewAccessoryId =
        if (activeTab ==
            CosmeticSlot.ACCESSORY
        ) {
            effectiveSelectedId
        } else {
            currentEquippedAccessoryId
        }

    // Filter items based on active categories
    val tabItems =
        when (activeTab) {
            CosmeticSlot.FRIEND -> catalog.filter { it.slot == CosmeticSlot.FRIEND }
            CosmeticSlot.ACCESSORY ->
                catalog.filter {
                    it.slot == CosmeticSlot.ACCESSORY ||
                        it.slot == CosmeticSlot.OUTFIT
                }
            CosmeticSlot.BACKGROUND -> catalog.filter { it.slot == CosmeticSlot.BACKGROUND }
            else -> emptyList()
        }

    val filteredItems =
        if (subTab == 1) {
            tabItems.filter { inventory.ownedItemIds.contains(it.id) }
        } else {
            tabItems
        }

    Row(modifier = modifier.fillMaxSize().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        // Left Panel: Preview
        Column(
            modifier = Modifier.weight(0.4f).fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Card(
                modifier = Modifier.size(260.dp),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    PetAvatar(
                        modifier = Modifier.size(200.dp),
                        friendId = previewFriendId,
                        accessoryId = previewAccessoryId,
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            val previewText =
                when {
                    effectiveSelectedId == null -> ""
                    effectiveSelectedId == currentEquippedFriendId ||
                        effectiveSelectedId == currentEquippedAccessoryId -> {
                        val name =
                            if (effectiveSelectedId.startsWith(
                                    "friend",
                                )
                            ) {
                                stringResource(R.string.pet_friend_mobi)
                            } else {
                                stringResource(R.string.pet_item_necklace)
                            }
                        "$name · 현재 착용"
                    }
                    else -> {
                        val name =
                            when (effectiveSelectedId) {
                                "friend:mobi" -> stringResource(R.string.pet_friend_mobi)
                                "friend:luna" -> stringResource(R.string.pet_friend_luna)
                                "accessory:necklace" -> stringResource(R.string.pet_item_necklace)
                                "accessory:mint_scarf" -> stringResource(R.string.pet_item_mint_scarf)
                                else -> effectiveSelectedId
                            }
                        "$name · 착용 미리보기"
                    }
                }
            Text(
                text = previewText,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        // Right Panel: Catalog and Options
        Column(modifier = Modifier.weight(0.6f).fillMaxHeight()) {
            // Point balance bar
            MobiMonPointSummary(pointBalance, failed = pointLoadFailed)
            Spacer(modifier = Modifier.height(8.dp))

            // Main Category Tabs
            TabRow(
                selectedTabIndex =
                    when (activeTab) {
                        CosmeticSlot.FRIEND -> 0
                        CosmeticSlot.ACCESSORY -> 1
                        CosmeticSlot.BACKGROUND -> 2
                        else -> 1
                    },
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onBackground,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(
                            tabPositions[
                                when (activeTab) {
                                    CosmeticSlot.FRIEND -> 0
                                    CosmeticSlot.ACCESSORY -> 1
                                    CosmeticSlot.BACKGROUND -> 2
                                    else -> 1
                                },
                            ],
                        ),
                        color = MaterialTheme.colorScheme.primary,
                    )
                },
            ) {
                Tab(
                    selected = activeTab == CosmeticSlot.FRIEND,
                    onClick = {
                        activeTab = CosmeticSlot.FRIEND
                        onSelectItem(null)
                    },
                    text = { Text(stringResource(R.string.pet_customization_tab_friend), fontSize = 20.sp) },
                )
                Tab(
                    selected = activeTab == CosmeticSlot.ACCESSORY,
                    onClick = {
                        activeTab = CosmeticSlot.ACCESSORY
                        onSelectItem(null)
                    },
                    text = { Text(stringResource(R.string.pet_customization_tab_accessory), fontSize = 20.sp) },
                )
                Tab(
                    selected = activeTab == CosmeticSlot.BACKGROUND,
                    onClick = {
                        activeTab = CosmeticSlot.BACKGROUND
                        onSelectItem(null)
                    },
                    text = { Text(stringResource(R.string.pet_customization_tab_background), fontSize = 20.sp) },
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sub tabs (전체 / 보유 중)
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = CircleShape,
                        color =
                            if (subTab ==
                                0
                            ) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                        modifier = Modifier.clickable { subTab = 0 },
                    ) {
                        Text(
                            text = stringResource(R.string.pet_customization_subtab_all),
                            modifier = Modifier.padding(horizontal = 16.mutValue(), vertical = 6.mutValue()),
                            style = MaterialTheme.typography.labelLarge,
                            color =
                                if (subTab ==
                                    0
                                ) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                        )
                    }
                    Surface(
                        shape = CircleShape,
                        color =
                            if (subTab ==
                                1
                            ) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                        modifier = Modifier.clickable { subTab = 1 },
                    ) {
                        Text(
                            text = stringResource(R.string.pet_customization_subtab_owned),
                            modifier = Modifier.padding(horizontal = 16.mutValue(), vertical = 6.mutValue()),
                            style = MaterialTheme.typography.labelLarge,
                            color =
                                if (subTab ==
                                    1
                                ) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                        )
                    }
                }
                Text(
                    text = stringResource(R.string.pet_customization_count_format, filteredItems.size),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Items Grid
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (filteredItems.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            stringResource(R.string.pet_catalog_pending),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.outline,
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(filteredItems, key = { it.id }) { item ->
                            val isSelected = item.id == effectiveSelectedId
                            val isOwned = inventory.ownedItemIds.contains(item.id)
                            val isEquipped =
                                inventory.equippedItemIds[item.slot] == item.id ||
                                    (item.slot == CosmeticSlot.FRIEND && currentEquippedFriendId == item.id)

                            Card(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .border(
                                            width = if (isSelected) 3.dp else 0.dp,
                                            color =
                                                if (isSelected) {
                                                    MaterialTheme.colorScheme.primary
                                                } else {
                                                    Color.Transparent
                                                },
                                            shape = MaterialTheme.shapes.large,
                                        ).clickable { onSelectItem(item.id) },
                                shape = MaterialTheme.shapes.large,
                                colors =
                                    CardDefaults.cardColors(
                                        containerColor =
                                            if (isSelected) {
                                                MaterialTheme.colorScheme.surfaceVariant
                                            } else {
                                                MaterialTheme.colorScheme.surface
                                            },
                                    ),
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    Column(
                                        modifier = Modifier.fillMaxSize().padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                    ) {
                                        // Item Icon visual placeholder
                                        Box(
                                            modifier =
                                                Modifier
                                                    .size(70.dp)
                                                    .background(
                                                        color = MaterialTheme.colorScheme.primaryContainer,
                                                        shape = CircleShape,
                                                    ),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            if (item.id.startsWith("friend")) {
                                                PetAvatar(modifier = Modifier.size(50.dp), friendId = item.id)
                                            } else {
                                                // accessory placeholder
                                                Box(
                                                    modifier =
                                                        Modifier
                                                            .size(36.dp)
                                                            .background(
                                                                color =
                                                                    if (item.id ==
                                                                        "accessory:mint_scarf"
                                                                    ) {
                                                                        Color(0xFF7FC1A5)
                                                                    } else {
                                                                        Color(0xFF8B5A2B)
                                                                    },
                                                                shape = MaterialTheme.shapes.small,
                                                            ),
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.weight(1f))

                                        val title =
                                            when (item.id) {
                                                "friend:mobi" -> stringResource(R.string.pet_friend_mobi)
                                                "friend:luna" -> stringResource(R.string.pet_friend_luna)
                                                "accessory:necklace" -> stringResource(R.string.pet_item_necklace)
                                                "accessory:mint_scarf" -> stringResource(R.string.pet_item_mint_scarf)
                                                else -> item.id
                                            }

                                        Text(
                                            text = title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center,
                                            maxLines = 1,
                                        )

                                        val subtitle =
                                            when {
                                                isEquipped -> stringResource(R.string.pet_item_status_equipped)
                                                isOwned -> stringResource(R.string.pet_item_status_owned)
                                                else -> stringResource(R.string.pet_item_status_unowned, item.price)
                                            }

                                        Text(
                                            text = subtitle,
                                            style = MaterialTheme.typography.labelLarge,
                                            color =
                                                if (isEquipped) {
                                                    MaterialTheme.colorScheme.primary
                                                } else {
                                                    MaterialTheme.colorScheme.outline
                                                },
                                            textAlign = TextAlign.Center,
                                            maxLines = 1,
                                        )
                                    }

                                    if (isSelected || isEquipped) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier =
                                                Modifier
                                                    .align(Alignment.TopEnd)
                                                    .padding(8.dp)
                                                    .size(24.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Status message info overlays if any
            if (purchaseFailed) MobiMonMessage(stringResource(R.string.pet_inventory_failed), isError = true)
            if (saveFailed) MobiMonMessage(stringResource(R.string.pet_inventory_save_failed), isError = true)

            // Bottom action button matching contextual selection states
            val selectedItem = catalog.firstOrNull { it.id == effectiveSelectedId }
            if (selectedItem != null) {
                val isOwned = inventory.ownedItemIds.contains(selectedItem.id)
                val isEquipped =
                    inventory.equippedItemIds[selectedItem.slot] == selectedItem.id ||
                        (selectedItem.slot == CosmeticSlot.FRIEND && currentEquippedFriendId == selectedItem.id)

                val buttonText =
                    when {
                        purchasing -> stringResource(R.string.pet_action_purchasing)
                        saving -> stringResource(R.string.pet_action_equipping)
                        isEquipped -> stringResource(R.string.pet_action_equipped)
                        isOwned -> {
                            if (selectedItem.id == "accessory:mint_scarf") {
                                stringResource(R.string.pet_action_equip_scarf)
                            } else if (selectedItem.id ==
                                "accessory:necklace"
                            ) {
                                stringResource(R.string.pet_action_equip_necklace)
                            } else {
                                stringResource(R.string.pet_appearance_apply)
                            }
                        }
                        else -> {
                            if (pointBalance != null && pointBalance < selectedItem.price) {
                                stringResource(R.string.pet_action_insufficient_points, selectedItem.price)
                            } else {
                                stringResource(R.string.pet_action_purchase, selectedItem.price)
                            }
                        }
                    }

                val buttonEnabled =
                    !purchasing &&
                        !saving &&
                        !isEquipped &&
                        (isOwned || (pointBalance != null && pointBalance >= selectedItem.price))

                Button(
                    onClick = {
                        if (isOwned) {
                            if (selectedItem.slot == CosmeticSlot.FRIEND) {
                                onEquipFriend(selectedItem.id)
                            } else {
                                onEquipItem(selectedItem.id)
                            }
                        } else {
                            onPurchaseItem(selectedItem.id, selectedItem.price)
                        }
                    },
                    enabled = buttonEnabled,
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor =
                                if (isEquipped) {
                                    MaterialTheme.colorScheme.outlineVariant
                                } else {
                                    MaterialTheme.colorScheme.primary
                                },
                        ),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 72.dp),
                ) {
                    Text(text = buttonText, style = MaterialTheme.typography.titleLarge)
                }
            }
        }
    }
}

private fun Int.mutValue(): androidx.compose.ui.unit.Dp = this.dp
