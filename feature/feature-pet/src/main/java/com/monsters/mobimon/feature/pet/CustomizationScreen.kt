package com.monsters.mobimon.feature.pet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.monsters.mobimon.core.domain.CosmeticInventory
import com.monsters.mobimon.core.domain.CosmeticItem
import com.monsters.mobimon.core.domain.CosmeticSlot
import com.monsters.mobimon.core.ui.CharacterArtwork
import com.monsters.mobimon.core.ui.CharacterAssetImage
import com.monsters.mobimon.core.ui.MobiMonButton
import com.monsters.mobimon.core.ui.MobiMonMessage
import com.monsters.mobimon.core.ui.MobiMonSelectionCard
import com.monsters.mobimon.core.ui.MobiMonTab
import com.monsters.mobimon.core.ui.MobiMonTabs
import com.monsters.mobimon.core.ui.PetAvatar

private fun isObsoleteItem(itemId: String): Boolean =
    itemId == "accessory:necklace" ||
        itemId == "accessory:mint_scarf" ||
        itemId.contains("necklace") ||
        itemId.contains("mint_scarf")

internal val NONE_ACCESSORY_ITEM =
    CosmeticItem(
        id = "none:accessory",
        slot = CosmeticSlot.ACCESSORY,
        price = 0,
        compatibleFriendId = null,
    )

@Composable
internal fun CompactCustomizationScreen(
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
    activeTab: CosmeticSlot,
    onTabChange: (CosmeticSlot) -> Unit,
) {
    var subTab by rememberSaveable { mutableIntStateOf(0) } // 0: 전체, 1: 보유 중

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
                MobiMonButton(onClick = onRetry, modifier = Modifier.fillMaxWidth()) {
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
            CosmeticSlot.ACCESSORY -> currentEquippedAccessoryId ?: "none:accessory"
            CosmeticSlot.BACKGROUND -> inventory.equippedItemIds[CosmeticSlot.BACKGROUND]
            else -> null
        }

    val previewFriendId =
        if (activeTab == CosmeticSlot.FRIEND) {
            effectiveSelectedId ?: "friend:mobi"
        } else {
            currentEquippedFriendId
        }
    val previewAccessoryId =
        when {
            activeTab == CosmeticSlot.ACCESSORY ->
                if ((effectiveSelectedId == "none:accessory") || (effectiveSelectedId?.startsWith("none:") == true)) {
                    null
                } else {
                    effectiveSelectedId?.takeIf { id ->
                        catalog.any { it.id == id && it.slot == CosmeticSlot.ACCESSORY }
                    }
                        ?: currentEquippedAccessoryId
                }
            previewFriendId == currentEquippedFriendId -> currentEquippedAccessoryId
            else -> inventory.equippedByFriend[previewFriendId]?.get(CosmeticSlot.ACCESSORY)
        }
    val previewBackgroundId =
        if (activeTab == CosmeticSlot.BACKGROUND) {
            effectiveSelectedId
        } else {
            inventory.equippedItemIds[CosmeticSlot.BACKGROUND]
        }

    // Filter items based on active categories
    val tabItems =
        when (activeTab) {
            CosmeticSlot.FRIEND -> catalog.filter { it.slot == CosmeticSlot.FRIEND && !isObsoleteItem(it.id) }
            CosmeticSlot.ACCESSORY -> {
                val accessories =
                    catalog.filter {
                        !isObsoleteItem(it.id) &&
                            (it.slot == CosmeticSlot.ACCESSORY || it.slot == CosmeticSlot.OUTFIT) &&
                            (
                                it.compatibleFriendId == currentEquippedFriendId ||
                                    (currentEquippedFriendId == "friend:mobi" && it.compatibleFriendId == null)
                            )
                    }
                listOf(NONE_ACCESSORY_ITEM) + accessories
            }
            CosmeticSlot.BACKGROUND -> catalog.filter { it.slot == CosmeticSlot.BACKGROUND && !isObsoleteItem(it.id) }
            else -> emptyList()
        }

    val filteredItems =
        if (subTab == 1) {
            tabItems.filter { it.id.startsWith("none:") || inventory.ownedItemIds.contains(it.id) }
        } else {
            tabItems
        }

    BoxWithConstraints(modifier.fillMaxSize()) {
        val scrollCatalog =
            maxWidth / LocalDensity.current.fontScale < 1000.dp || maxHeight / LocalDensity.current.fontScale < 650.dp
        val catalogScroll = rememberScrollState()
        Row(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            // Left Panel: Preview
            Column(
                modifier = Modifier.weight(0.4f).fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Card(
                    modifier = Modifier.widthIn(max = 540.dp).fillMaxWidth().aspectRatio(1.35f),
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Box(
                            Modifier
                                .fillMaxSize()
                                .testTag("preview-background")
                                .background(Brush.verticalGradient(listOf(Color(0xFF1B3856), Color(0xFF0A192D)))),
                        ) {
                            previewBackgroundId?.let { CharacterArtwork.backgrounds[it] }?.let { background ->
                                CharacterAssetImage(background, Modifier.fillMaxSize())
                            }
                            if (previewBackgroundId != null) {
                                val particleType =
                                    when {
                                        previewBackgroundId.contains(
                                            "snow",
                                        ) -> com.monsters.mobimon.core.ui.ParticleType.SNOW
                                        previewBackgroundId.contains(
                                            "petal",
                                        ) ||
                                            previewBackgroundId.contains(
                                                "flower",
                                            ) -> com.monsters.mobimon.core.ui.ParticleType.PETAL
                                        else -> com.monsters.mobimon.core.ui.ParticleType.STAR
                                    }
                                com.monsters.mobimon.core.ui.FallingParticlesEffect(
                                    particleType = particleType,
                                    modifier = Modifier.fillMaxSize().testTag("preview-background-particles"),
                                )
                            }
                        }
                        PetAvatar(
                            modifier = Modifier.fillMaxSize(0.88f).testTag("preview-character"),
                            friendId = previewFriendId,
                            accessoryId = previewAccessoryId,
                            outfitId =
                                catalog
                                    .firstOrNull {
                                        it.id == effectiveSelectedId && it.slot == CosmeticSlot.OUTFIT
                                    }?.id
                                    ?: inventory.equippedByFriend[previewFriendId]?.get(CosmeticSlot.OUTFIT),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                val previewText =
                    when {
                        effectiveSelectedId == null -> ""
                        effectiveSelectedId == currentEquippedFriendId ||
                            effectiveSelectedId == currentEquippedAccessoryId ||
                            (effectiveSelectedId == "none:accessory" && currentEquippedAccessoryId == null) -> {
                            val name = cosmeticName(effectiveSelectedId)
                            "$name · 현재 착용"
                        }
                        else -> {
                            val name = cosmeticName(effectiveSelectedId)
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
            Column(
                modifier =
                    Modifier
                        .weight(
                            0.6f,
                        ).fillMaxHeight()
                        .then(if (scrollCatalog) Modifier.verticalScroll(catalogScroll) else Modifier),
            ) {
                MobiMonTabs(Modifier.fillMaxWidth()) {
                    listOf(
                        CosmeticSlot.FRIEND to R.string.pet_customization_tab_friend,
                        CosmeticSlot.ACCESSORY to R.string.pet_customization_tab_accessory,
                        CosmeticSlot.BACKGROUND to R.string.pet_customization_tab_background,
                    ).forEach { (slot, label) ->
                        MobiMonTab(activeTab == slot, {
                            onTabChange(slot)
                            onSelectItem(null)
                        }) {
                            Text(stringResource(label))
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                MobiMonTabs(Modifier.fillMaxWidth()) {
                    MobiMonTab(
                        subTab == 0,
                        { subTab = 0 },
                    ) { Text(stringResource(R.string.pet_customization_subtab_all)) }
                    MobiMonTab(
                        subTab == 1,
                        { subTab = 1 },
                    ) { Text(stringResource(R.string.pet_customization_subtab_owned)) }
                }
                Text(
                    stringResource(R.string.pet_customization_count_format, filteredItems.size),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Items Grid
                Box(
                    modifier =
                        Modifier.fillMaxWidth().then(
                            if (scrollCatalog) Modifier.height(400.dp) else Modifier.weight(1f),
                        ),
                ) {
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
                            modifier = Modifier.selectableGroup().testTag("shop-items"),
                            columns = GridCells.Adaptive(240.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(filteredItems, key = { it.id }) { item ->
                                val isNoneItem = item.id.startsWith("none:")
                                val isSelected = item.id == effectiveSelectedId
                                val isOwned = isNoneItem || inventory.ownedItemIds.contains(item.id)
                                val isEquipped =
                                    if (isNoneItem) {
                                        currentEquippedAccessoryId == null
                                    } else {
                                        inventory.equippedItemIds[item.slot] == item.id ||
                                            (item.slot == CosmeticSlot.FRIEND && currentEquippedFriendId == item.id)
                                    }

                                MobiMonSelectionCard(
                                    selected = isSelected,
                                    onClick = { onSelectItem(item.id) },
                                    modifier = Modifier.fillMaxWidth().heightIn(min = 180.dp),
                                ) {
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        Column(
                                            modifier = Modifier.fillMaxWidth().padding(12.dp),
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
                                                val iconAsset =
                                                    CharacterArtwork.characters[item.id]
                                                        ?: CharacterArtwork.itemIcons[item.id]
                                                if (iconAsset != null) {
                                                    CharacterAssetImage(iconAsset, Modifier.size(64.dp))
                                                } else {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(32.dp),
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(12.dp))

                                            val title = cosmeticName(item.id)

                                            Text(
                                                text = title,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Center,
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
                val selectedItem =
                    tabItems.firstOrNull { it.id == effectiveSelectedId }
                        ?: NONE_ACCESSORY_ITEM.takeIf { activeTab == CosmeticSlot.ACCESSORY }
                if (selectedItem != null) {
                    val isNoneItem = selectedItem.id.startsWith("none:")
                    val isOwned = isNoneItem || inventory.ownedItemIds.contains(selectedItem.id)
                    val isEquipped =
                        if (isNoneItem) {
                            currentEquippedAccessoryId == null
                        } else {
                            inventory.equippedItemIds[selectedItem.slot] == selectedItem.id ||
                                (selectedItem.slot == CosmeticSlot.FRIEND && currentEquippedFriendId == selectedItem.id)
                        }

                    val buttonText =
                        when {
                            purchasing -> stringResource(R.string.pet_action_purchasing)
                            saving -> stringResource(R.string.pet_action_equipping)
                            isEquipped -> stringResource(R.string.pet_action_equipped)
                            isOwned -> stringResource(R.string.pet_appearance_apply)
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
                            (
                                isOwned ||
                                    (!pointLoadFailed && pointBalance != null && pointBalance >= selectedItem.price)
                            )

                    MobiMonButton(
                        onClick = {
                            if (isNoneItem) {
                                onEquipItem(selectedItem.id)
                            } else if (isOwned) {
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
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(text = buttonText, style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
        }
    }
}

@Composable
internal fun cosmeticName(itemId: String): String =
    when {
        itemId.startsWith("none") -> stringResource(R.string.pet_item_none)
        itemId == "friend:mobi" -> stringResource(R.string.pet_friend_mobi)
        itemId == "friend:luna" -> stringResource(R.string.pet_friend_luna)
        itemId == "accessory:mobi_headphones" -> stringResource(R.string.pet_item_mobi_headphones)
        itemId == "accessory:mobi_goggles" -> stringResource(R.string.pet_item_mobi_goggles)
        itemId == "accessory:luna_cap" -> stringResource(R.string.pet_item_luna_cap)
        itemId == "accessory:luna_sunglasses" -> stringResource(R.string.pet_item_luna_sunglasses)
        else -> itemId
    }
