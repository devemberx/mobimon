package com.monsters.mobimon.feature.pet

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.monsters.mobimon.core.domain.CosmeticInventory
import com.monsters.mobimon.core.domain.CosmeticItem
import com.monsters.mobimon.core.domain.CosmeticSlot
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
            MaterialTheme {
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

        // Switch tab to friend tab if needed, wait, our default tab is ACCESSORY. Let's click the Friend tab first!
        compose.onNodeWithText("친구 바꾸기").performClick()

        compose.onNodeWithText("Luna · 고양이").performClick()
        compose.onNodeWithText("Luna · 고양이 · 착용 미리보기").assertExists()
        compose.onNodeWithText("이 모습 적용").performClick()
        org.junit.Assert.assertEquals("friend:luna", applied)
    }

    @Test fun failedInventoryOffersRetry() {
        var retries = 0
        compose.setContent {
            MaterialTheme {
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
}
