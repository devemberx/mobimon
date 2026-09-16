package com.monsters.mobimon.feature.pet

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToIndex
import com.monsters.mobimon.core.domain.CosmeticInventory
import com.monsters.mobimon.core.domain.CosmeticItem
import com.monsters.mobimon.core.domain.CosmeticSlot
import com.monsters.mobimon.core.ui.MobiMonTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "ko-rKR")
class CustomizationScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test fun selectingOwnedFriendPreviewsWithoutEquippingUntilApply() {
        var applied: String? = null
        val catalog =
            listOf(
                CosmeticItem("friend:mobi", CosmeticSlot.FRIEND, 0),
                CosmeticItem("friend:luna", CosmeticSlot.FRIEND, 0),
            )
        val inventory =
            CosmeticInventory(
                ownedItemIds = setOf("friend:mobi", "friend:luna"),
                equippedItemIds = mapOf(CosmeticSlot.FRIEND to "friend:mobi"),
            )

        compose.setContent {
            var selectedId by androidx.compose.runtime.remember { mutableStateOf<String?>(null) }
            MobiMonTheme {
                CustomizationScreen(
                    inventory = inventory,
                    catalog = catalog,
                    selectedItemId = selectedId,
                    purchasing = false,
                    purchaseFailed = false,
                    onSelectItem = { selectedId = it },
                    onPurchaseItem = { _, _ -> },
                    onEquipItem = {},
                    onEquipFriend = { applied = it },
                    pointBalance = 0,
                    pointLoadFailed = false,
                )
            }
        }

        compose.onNodeWithText("친구 바꾸기").performClick()

        compose.onNodeWithTag("shop-items").performScrollTo().performScrollToIndex(1)
        compose
            .onNodeWithText("Luna · 고양이")
            .performScrollTo()
            .assertIsDisplayed()
            .performClick()
        compose.onNodeWithText("Luna · 고양이 · 착용 미리보기").assertExists()
        org.junit.Assert.assertNull(applied)
        compose
            .onNodeWithText("이 모습 적용")
            .performScrollTo()
            .assertIsDisplayed()
            .performClick()
        org.junit.Assert.assertEquals("friend:luna", applied)
    }

    @Test fun failedInventoryOffersRetry() {
        var retries = 0
        compose.setContent {
            MobiMonTheme {
                CustomizationScreen(
                    inventory = null,
                    catalog = emptyList(),
                    selectedItemId = null,
                    purchasing = false,
                    purchaseFailed = false,
                    onSelectItem = {},
                    onPurchaseItem = { _, _ -> },
                    onEquipItem = {},
                    onEquipFriend = {},
                    pointBalance = null,
                    pointLoadFailed = false,
                    loadFailed = true,
                    onRetry = { retries++ },
                )
            }
        }

        compose.onNodeWithText("다시 시도").assertIsDisplayed().performClick()
        org.junit.Assert.assertEquals(1, retries)
    }

    @Test fun accessoryCatalogAndPreviewFollowEquippedFriend() {
        val catalog =
            listOf(
                CosmeticItem("friend:mobi", CosmeticSlot.FRIEND, 0),
                CosmeticItem("friend:luna", CosmeticSlot.FRIEND, 0),
                CosmeticItem("accessory:mobi_headphones", CosmeticSlot.ACCESSORY, 300, "friend:mobi"),
                CosmeticItem("accessory:luna_cap", CosmeticSlot.ACCESSORY, 300, "friend:luna"),
            )
        val inventory =
            mutableStateOf(
                CosmeticInventory(
                    ownedItemIds = catalog.mapTo(mutableSetOf()) { it.id },
                    equippedItemIds = mapOf(CosmeticSlot.FRIEND to "friend:mobi"),
                ),
            )
        compose.setContent {
            var selectedId by androidx.compose.runtime.remember { mutableStateOf<String?>(null) }
            MobiMonTheme {
                CustomizationScreen(
                    inventory = inventory.value,
                    catalog = catalog,
                    selectedItemId = selectedId,
                    purchasing = false,
                    purchaseFailed = false,
                    onSelectItem = { selectedId = it },
                    onPurchaseItem = { _, _ -> },
                    onEquipItem = {},
                    onEquipFriend = { id ->
                        inventory.value = inventory.value.copy(equippedItemIds = mapOf(CosmeticSlot.FRIEND to id))
                    },
                    pointBalance = 300,
                    pointLoadFailed = false,
                )
            }
        }

        compose.onNodeWithText("옷·소품").performClick()
        compose.onNodeWithText("모비 헤드폰").assertExists()
        compose.onNodeWithText("루나 모자").assertDoesNotExist()
        compose.runOnIdle {
            inventory.value = inventory.value.copy(equippedItemIds = mapOf(CosmeticSlot.FRIEND to "friend:luna"))
        }
        compose.onNodeWithText("루나 모자").assertExists()
        compose.onNodeWithText("모비 헤드폰").assertDoesNotExist()
        compose.onNodeWithTag("preview-background").assertExists()
        compose.onNodeWithTag("preview-character").assertExists()
    }
}
