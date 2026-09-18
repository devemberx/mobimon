package com.monsters.mobimon.feature.pet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monsters.mobimon.core.domain.CosmeticInventory
import com.monsters.mobimon.core.domain.CosmeticItem
import com.monsters.mobimon.core.domain.CosmeticSlot
import com.monsters.mobimon.core.ui.CharacterArtwork
import com.monsters.mobimon.core.ui.CharacterAssetImage
import com.monsters.mobimon.core.ui.MobiMonButton
import com.monsters.mobimon.core.ui.MobiMonMessage
import com.monsters.mobimon.core.ui.MobiMonPointSummary
import com.monsters.mobimon.core.ui.MobiMonSelectionCard
import com.monsters.mobimon.core.ui.MobiMonTab
import com.monsters.mobimon.core.ui.PetAvatar

private fun isObsoleteItem(itemId: String): Boolean =
    itemId == "accessory:necklace" ||
        itemId == "accessory:mint_scarf" ||
        itemId.contains("necklace") ||
        itemId.contains("mint_scarf")

private val StoreNight = Color(0xFF091525)
private val StorePanel = Color(0xFF142A42)
private val StoreRaised = Color(0xFF203C58)
private val StoreSky = Color(0xFF87DAF5)
private val StoreText = Color(0xFFF4F7FC)
private val StoreMuted = Color(0xFFB9CADD)

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
    onBack: () -> Unit = {},
) {
    var tab by rememberSaveable { mutableStateOf(CosmeticSlot.FRIEND) }
    BoxWithConstraints(modifier.fillMaxSize().background(StoreNight)) {
        val scale = minOf(maxWidth.value / 2560f, maxHeight.value / 1268f)
        val reference = maxWidth >= 1000.dp && maxHeight >= 540.dp && LocalDensity.current.fontScale <= 1.2f
        if (!reference || inventory == null) {
            Column(Modifier.fillMaxSize()) {
                StoreHeader(pointBalance, pointLoadFailed, onBack, 0.6f, Modifier.fillMaxWidth().padding(16.dp))
                CompactCustomizationScreen(
                    inventory,
                    catalog,
                    selectedItemId,
                    purchasing,
                    purchaseFailed,
                    onSelectItem,
                    onPurchaseItem,
                    onEquipItem,
                    onEquipFriend,
                    pointBalance,
                    pointLoadFailed,
                    Modifier.weight(1f),
                    saving,
                    loadFailed,
                    saveFailed,
                    onRetry,
                    activeTab = tab,
                    onTabChange = { tab = it },
                )
            }
        } else {
            Box(Modifier.size(2560.dp * scale, 1268.dp * scale).align(Alignment.Center).testTag("store-reference")) {
                StoreHeader(
                    pointBalance,
                    pointLoadFailed,
                    onBack,
                    scale,
                    Modifier.offset(72.dp * scale, 56.dp * scale).size(2416.dp * scale, 104.dp * scale),
                )
                val friend = inventory.equippedItemIds[CosmeticSlot.FRIEND] ?: "friend:mobi"
                val items =
                    when (tab) {
                        CosmeticSlot.FRIEND ->
                            catalog.filter {
                                it.slot == CosmeticSlot.FRIEND &&
                                    !isObsoleteItem(
                                        it.id,
                                    )
                            }
                        CosmeticSlot.ACCESSORY -> {
                            val accessories =
                                catalog.filter {
                                    !isObsoleteItem(it.id) &&
                                        (it.slot == CosmeticSlot.ACCESSORY || it.slot == CosmeticSlot.OUTFIT) &&
                                        (
                                            it.compatibleFriendId == friend ||
                                                (friend == "friend:mobi" && it.compatibleFriendId == null)
                                        )
                                }
                            listOf(NONE_ACCESSORY_ITEM) + accessories
                        }
                        else -> catalog.filter { it.slot == tab && !isObsoleteItem(it.id) }
                    }
                val selected =
                    items.firstOrNull { it.id == selectedItemId }
                        ?: items.firstOrNull {
                            if (it.id.startsWith("none")) {
                                inventory.equippedItemIds[CosmeticSlot.ACCESSORY] == null
                            } else {
                                it.id == inventory.equippedItemIds[it.slot]
                            }
                        }
                        ?: items.firstOrNull()
                val previewFriend = if (tab == CosmeticSlot.FRIEND) selected?.id ?: friend else friend
                val equipment =
                    if (previewFriend ==
                        friend
                    ) {
                        inventory.equippedItemIds
                    } else {
                        inventory.equippedByFriend[previewFriend].orEmpty()
                    }
                val outfit = selected?.takeIf { it.slot == CosmeticSlot.OUTFIT }?.id ?: equipment[CosmeticSlot.OUTFIT]
                val accessory =
                    when {
                        tab == CosmeticSlot.ACCESSORY -> selected?.takeIf { !it.id.startsWith("none") }?.id
                        else -> equipment[CosmeticSlot.ACCESSORY]
                    }
                val background =
                    selected?.takeIf { it.slot == CosmeticSlot.BACKGROUND }?.id
                        ?: inventory.equippedItemIds[CosmeticSlot.BACKGROUND]
                val equipped =
                    selected != null &&
                        (
                            if (selected.id.startsWith("none")) {
                                inventory.equippedItemIds[selected.slot] == null
                            } else {
                                inventory.equippedItemIds[selected.slot] == selected.id
                            }
                        )
                val owned =
                    selected != null && (selected.id.startsWith("none") || selected.id in inventory.ownedItemIds)
                Column(
                    Modifier
                        .offset(72.dp * scale, 216.dp * scale)
                        .size(916.dp * scale, 994.dp * scale)
                        .background(StorePanel, RoundedCornerShape(48.dp * scale))
                        .padding(32.dp * scale),
                ) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .weight(
                                1f,
                            ).clip(RoundedCornerShape(36.dp * scale))
                            .background(StoreRaised),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                            background?.let { CharacterArtwork.backgrounds[it] }?.let {
                                CharacterAssetImage(it, Modifier.fillMaxSize().testTag("preview-background"))
                            }
                            PetAvatar(
                                Modifier
                                    .fillMaxSize()
                                    .graphicsLayer {
                                        scaleX = if (previewFriend == "friend:luna") 1.10f else 0.92f
                                        scaleY = scaleX
                                    }.testTag("preview-character"),
                                friendId = previewFriend,
                                accessoryId = accessory,
                                outfitId = outfit,
                            )
                        }
                        Text(
                            storeFriendName(previewFriend),
                            fontSize = (48f * scale).sp,
                            color = StoreText,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.height(40.dp * scale))
                    }
                    Row(
                        Modifier.fillMaxWidth().height(154.dp * scale).padding(start = 28.dp * scale),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start,
                    ) {
                        Text(
                            if (equipped) "사용 중" else "미리보기",
                            color = StoreSky,
                            fontSize = (28f * scale).sp,
                            modifier =
                                Modifier.background(StoreRaised, CircleShape).padding(
                                    24.dp * scale,
                                    14.dp * scale,
                                ),
                        )
                        Spacer(Modifier.width(44.dp * scale))
                        Text(
                            if (equipped) "내 친구에게 작은 선물을" else "아직 적용되지 않았어요",
                            color = StoreMuted,
                            fontSize =
                                (
                                    28f *
                                        scale
                                ).sp,
                        )
                    }
                }
                Row(
                    Modifier
                        .offset(
                            1040.dp * scale,
                            216.dp * scale,
                        ).size(1448.dp * scale, 100.dp * scale)
                        .selectableGroup(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp * scale),
                ) {
                    listOf(
                        CosmeticSlot.FRIEND to "친구",
                        CosmeticSlot.ACCESSORY to "옷과 소품",
                        CosmeticSlot.BACKGROUND to "배경",
                    ).forEach { (slot, label) ->
                        MobiMonTab(
                            tab == slot,
                            {
                                tab = slot
                                onSelectItem(null)
                            },
                            Modifier
                                .weight(
                                    when (slot) {
                                        CosmeticSlot.FRIEND -> 432f
                                        CosmeticSlot.ACCESSORY -> 472f
                                        else -> 496f
                                    },
                                ).fillMaxHeight()
                                .testTag("store-tab-${slot.name}"),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement =
                                    Arrangement.spacedBy(
                                        24.dp * scale,
                                    ),
                            ) {
                                Icon(
                                    painterResource(
                                        when (slot) {
                                            CosmeticSlot.FRIEND -> R.drawable.store_friends
                                            CosmeticSlot.ACCESSORY -> R.drawable.store_clothes
                                            else -> R.drawable.store_background
                                        },
                                    ),
                                    null,
                                    Modifier.size(
                                        40.dp * scale,
                                    ),
                                )
                                Text(label, fontSize = (40f * scale).sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                Column(Modifier.offset(1040.dp * scale, 358.dp * scale).width(1448.dp * scale)) {
                    Text(
                        if (tab == CosmeticSlot.FRIEND) "함께할 친구" else "작은 소품으로, 새로운 기분",
                        color = StoreText,
                        fontSize = (44f * scale).sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(12.dp * scale))
                    Text(
                        if (tab == CosmeticSlot.FRIEND) "친구를 바꿔도 보유 아이템은 그대로예요." else "보유한 아이템은 언제든 다시 사용할 수 있어요.",
                        color = StoreMuted,
                        fontSize = (28f * scale).sp,
                    )
                }
                LazyVerticalGrid(
                    columns = GridCells.Fixed(if (tab == CosmeticSlot.FRIEND) 2 else 3),
                    modifier =
                        Modifier
                            .offset(
                                1040.dp * scale,
                                514.dp * scale,
                            ).size(1448.dp * scale, 492.dp * scale)
                            .selectableGroup()
                            .testTag("shop-items"),
                    horizontalArrangement = Arrangement.spacedBy(24.dp * scale),
                    verticalArrangement = Arrangement.spacedBy(24.dp * scale),
                ) {
                    items(items, key = { it.id }) { item ->
                        val isNone = item.id.startsWith("none")
                        val active =
                            if (isNone) {
                                inventory.equippedItemIds[CosmeticSlot.ACCESSORY] == null
                            } else {
                                inventory.equippedItemIds[item.slot] == item.id
                            }
                        MobiMonSelectionCard(
                            item.id == selected?.id,
                            { onSelectItem(item.id) },
                            Modifier.fillMaxWidth().height((if (tab == CosmeticSlot.FRIEND) 492 else 432).dp * scale),
                            shape = RoundedCornerShape(36.dp * scale),
                        ) {
                            Column(Modifier.fillMaxSize().padding(36.dp * scale)) {
                                Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                                    if (item.slot == CosmeticSlot.FRIEND) {
                                        PetAvatar(Modifier.fillMaxSize(), friendId = item.id)
                                    } else {
                                        (CharacterArtwork.itemIcons[item.id] ?: CharacterArtwork.backgrounds[item.id])
                                            ?.let {
                                                CharacterAssetImage(it, Modifier.fillMaxSize())
                                            } ?: Icon(
                                            painterResource(R.drawable.store_check),
                                            null,
                                            Modifier.size(80.dp * scale),
                                            tint = StoreSky,
                                        )
                                    }
                                }
                                Spacer(Modifier.height(20.dp * scale))
                                if (item.slot == CosmeticSlot.FRIEND) {
                                    Row(
                                        Modifier.fillMaxWidth().height(76.dp * scale),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            storeFriendName(item.id),
                                            Modifier.weight(1f),
                                            fontSize = (38f * scale).sp,
                                            color = StoreText,
                                            fontWeight = FontWeight.Bold,
                                        )
                                        val selectedCard = item.id == selected?.id
                                        Text(
                                            if (active) {
                                                "사용 중"
                                            } else if (selectedCard) {
                                                "✓  선택됨"
                                            } else {
                                                "보유 중"
                                            },
                                            Modifier
                                                .background(
                                                    if (selectedCard &&
                                                        !active
                                                    ) {
                                                        StoreSky
                                                    } else {
                                                        StoreRaised
                                                    },
                                                    CircleShape,
                                                ).padding(horizontal = 36.dp * scale, vertical = 18.dp * scale),
                                            color = if (selectedCard && !active) StoreNight else StoreSky,
                                            fontSize = (28f * scale).sp,
                                            fontWeight = FontWeight.Bold,
                                        )
                                    }
                                    Spacer(Modifier.height(16.dp * scale))
                                } else {
                                    Text(
                                        cosmeticName(item.id),
                                        fontSize = (36f * scale).sp,
                                        color = StoreText,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    Spacer(Modifier.height(12.dp * scale))
                                    Text(
                                        when {
                                            active -> "사용 중"
                                            item.id == selected?.id -> "✓ 선택됨"
                                            isNone || item.id in inventory.ownedItemIds -> "보유 중"
                                            else -> "${item.price} P"
                                        },
                                        fontSize = (28f * scale).sp,
                                        color = StoreSky,
                                    )
                                }
                            }
                        }
                    }
                }
                if (items.isEmpty()) {
                    Text(
                        stringResource(R.string.pet_catalog_pending),
                        color = StoreMuted,
                        fontSize = (32f * scale).sp,
                        modifier = Modifier.offset(1080.dp * scale, 620.dp * scale).width(1300.dp * scale),
                    )
                }
                if (tab == CosmeticSlot.ACCESSORY && !purchaseFailed && !saveFailed) {
                    Box(
                        Modifier
                            .offset(1040.dp * scale, 996.dp * scale)
                            .size(1448.dp * scale, 64.dp * scale)
                            .background(StoreRaised, RoundedCornerShape(24.dp * scale))
                            .padding(horizontal = 40.dp * scale),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        Text("아이템을 선택하면 친구에게 먼저 입혀 볼 수 있어요.", color = StoreMuted, fontSize = (28f * scale).sp)
                    }
                }
                if (purchaseFailed || saveFailed) {
                    MobiMonMessage(
                        stringResource(
                            if (saveFailed) R.string.pet_inventory_save_failed else R.string.pet_inventory_failed,
                        ),
                        isError = true,
                        modifier =
                            Modifier.offset(1040.dp * scale, 1016.dp * scale).width(
                                1448.dp * scale,
                            ),
                    )
                }
                val action =
                    when {
                        saving || purchasing -> "적용 중…"
                        selected == null -> "아이템을 골라 주세요"
                        equipped -> "사용 중"
                        owned && tab == CosmeticSlot.FRIEND -> "${storeFriendName(selected.id)}와 함께하기"
                        owned -> "이 모습 적용"
                        pointBalance != null && pointBalance < selected.price -> "포인트 부족 · ${selected.price} P"
                        else -> "${selected.price} P 구매"
                    }
                MobiMonButton(
                    onClick = {
                        selected?.let {
                            if (!owned) {
                                onPurchaseItem(it.id, it.price)
                            } else if (it.slot == CosmeticSlot.FRIEND) {
                                onEquipFriend(it.id)
                            } else {
                                onEquipItem(it.id)
                            }
                        }
                    },
                    enabled =
                        selected != null &&
                            !equipped &&
                            !saving &&
                            !purchasing &&
                            (owned || (!pointLoadFailed && pointBalance != null && pointBalance >= selected.price)),
                    modifier = Modifier.offset(1040.dp * scale, 1098.dp * scale).size(1448.dp * scale, 112.dp * scale),
                ) {
                    if (owned && !equipped && !saving) {
                        Icon(painterResource(R.drawable.store_check), null, Modifier.size(40.dp * scale))
                        Spacer(Modifier.width(32.dp * scale))
                    }
                    Text(action, fontSize = (40f * scale).sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun StoreHeader(
    balance: Long?,
    failed: Boolean,
    onBack: () -> Unit,
    scale: Float,
    modifier: Modifier,
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        IconButton(
            onBack,
            Modifier
                .size(
                    104.dp * scale,
                ).background(StorePanel, CircleShape)
                .border(1.dp, Color(0xFF64839F), CircleShape),
        ) {
            Icon(
                painterResource(R.drawable.store_back),
                stringResource(com.monsters.mobimon.core.ui.R.string.mobimon_back),
                tint = StoreText,
                modifier = Modifier.size(40.dp * scale),
            )
        }
        Spacer(Modifier.width(32.dp * scale))
        Text(
            "꾸미기",
            Modifier.weight(1f).semantics {
                heading()
            },
            color = StoreText,
            fontSize = (64f * scale).sp,
            fontWeight = FontWeight.Bold,
        )
        MobiMonPointSummary(
            balance,
            failed = failed,
            textStyle =
                MaterialTheme.typography.titleLarge.copy(
                    fontSize =
                        (
                            36f *
                                scale
                        ).sp,
                    color = StoreText,
                ),
        )
    }
}

private fun storeFriendName(id: String): String = if (id == "friend:luna") "루나" else "모비"
