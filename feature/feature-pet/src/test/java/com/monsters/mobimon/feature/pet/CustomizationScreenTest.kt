package com.monsters.mobimon.feature.pet

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.monsters.mobimon.core.domain.CosmeticInventory
import com.monsters.mobimon.core.domain.CosmeticSlot
import org.junit.Assert.assertEquals
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
        compose.setContent {
            MaterialTheme {
                CustomizationScreen(
                    inventory =
                        CosmeticInventory(
                            ownedItemIds = setOf("friend:mobi", "friend:luna"),
                            equippedItemIds = mapOf(CosmeticSlot.FRIEND to "friend:mobi"),
                        ),
                    onEquipFriend = { applied = it },
                    pointBalance = 0,
                    pointLoadFailed = false,
                )
            }
        }
        compose.onNodeWithText("Luna · 고양이").performScrollTo().performClick()
        assertEquals(null, applied)
        compose.onNodeWithText("미리 보는 중").performScrollTo().assertExists()
        compose.onNodeWithText("이 모습 적용").performScrollTo().performClick()
        assertEquals("friend:luna", applied)
    }

    @Test fun failedInventoryOffersRetry() {
        var retries = 0
        compose.setContent {
            MaterialTheme {
                CustomizationScreen(
                    inventory = null,
                    onEquipFriend = {},
                    pointBalance = null,
                    pointLoadFailed = false,
                    loadFailed = true,
                    onRetry = { retries++ },
                )
            }
        }

        compose.onNodeWithText("다시 시도").assertIsDisplayed().performClick()
        assertEquals(1, retries)
    }
}
