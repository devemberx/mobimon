package com.monsters.mobimon.feature.customization

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.monsters.mobimon.core.domain.CosmeticInventory
import com.monsters.mobimon.core.domain.CosmeticItem
import com.monsters.mobimon.core.domain.CosmeticSlot
import com.monsters.mobimon.core.ui.MobiMonTheme
import com.monsters.mobimon.core.ui.companionBackgroundRes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "ko-rKR-w2560dp-h1332dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class StoreReferenceScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test fun categoriesSelectIndependentlyAndPreviewOnlyAppliesOnConfirmation() {
        var applied: String? = null
        lateinit var view: View
        val catalog =
            listOf(
                CosmeticItem("friend:mobi", CosmeticSlot.FRIEND, 0),
                CosmeticItem("friend:luna", CosmeticSlot.FRIEND, 0),
                CosmeticItem("accessory:mobi_headphones", CosmeticSlot.ACCESSORY, 300, "friend:mobi"),
            )
        compose.setContent {
            val currentView = LocalView.current
            SideEffect { view = currentView }
            var selected by remember { mutableStateOf<String?>("friend:luna") }
            MobiMonTheme {
                CustomizationScreen(
                    CosmeticInventory(
                        catalog.mapTo(mutableSetOf()) { it.id },
                        mapOf(
                            CosmeticSlot.FRIEND to "friend:mobi",
                        ),
                    ),
                    catalog,
                    selected,
                    false,
                    false,
                    { selected = it },
                    { _, _ -> },
                    {},
                    { applied = it },
                    1200,
                    false,
                )
            }
        }
        compose.onNodeWithTag("store-tab-FRIEND").assertIsSelected()
        compose.onNodeWithText("주차 확인됨").assertDoesNotExist()
        compose.onNodeWithText("꾸미기").assertExists()
        assertNull(applied)
        capture(view, "P20-friend")
        compose.onNodeWithText("루나와 함께하기").performClick()
        assertEquals("friend:luna", applied)
        compose.onNodeWithTag("store-tab-ACCESSORY").performClick().assertIsSelected()
        compose.onNodeWithTag("store-tab-FRIEND").assertIsNotSelected()
        capture(view, "P22-accessories")
        compose.onNodeWithTag("store-tab-BACKGROUND").performClick().assertIsSelected()
        compose.onNodeWithTag("store-tab-ACCESSORY").assertIsNotSelected()
        capture(view, "P22-backgrounds")
    }

    @Test fun previewDescriptionMatchesItemTypes() {
        val descriptions = mutableMapOf<String, String>()
        compose.setContent {
            MobiMonTheme {
                descriptions["friend:mobi"] =
                    storePreviewDescription(CosmeticSlot.FRIEND, null, "friend:mobi", true)
                descriptions["friend:luna"] =
                    storePreviewDescription(CosmeticSlot.FRIEND, null, "friend:luna", false)
                descriptions["none:accessory"] =
                    storePreviewDescription(CosmeticSlot.ACCESSORY, "none:accessory", "friend:mobi", true)
                descriptions["accessory:luna_cap"] =
                    storePreviewDescription(CosmeticSlot.ACCESSORY, "accessory:luna_cap", "friend:luna", false)
                descriptions["accessory:luna_sunglasses"] =
                    storePreviewDescription(CosmeticSlot.ACCESSORY, "accessory:luna_sunglasses", "friend:luna", false)
                descriptions["accessory:mobi_headphones"] =
                    storePreviewDescription(CosmeticSlot.ACCESSORY, "accessory:mobi_headphones", "friend:mobi", false)
                descriptions["accessory:mobi_goggles"] =
                    storePreviewDescription(CosmeticSlot.ACCESSORY, "accessory:mobi_goggles", "friend:mobi", false)
                descriptions["none:background"] =
                    storePreviewDescription(CosmeticSlot.BACKGROUND, "none:background", "friend:mobi", true)
                descriptions["background:star"] =
                    storePreviewDescription(CosmeticSlot.BACKGROUND, "background:star", "friend:mobi", false)
                descriptions["background:snow"] =
                    storePreviewDescription(CosmeticSlot.BACKGROUND, "background:snow", "friend:mobi", false)
                descriptions["background:petal"] =
                    storePreviewDescription(CosmeticSlot.BACKGROUND, "background:petal", "friend:mobi", false)
            }
        }
        assertEquals("우리들의 작은 친구 모비에요", descriptions["friend:mobi"])
        assertEquals("귀여운 애교쟁이 루나랍니다냥", descriptions["friend:luna"])
        assertEquals("내추럴한 모습이에요", descriptions["none:accessory"])
        assertEquals("함께 항해를 떠나볼까요?", descriptions["accessory:luna_cap"])
        assertEquals("눈부실 때는 선글라스만한게 없죠~", descriptions["accessory:luna_sunglasses"])
        assertEquals("노이즈캔슬링으로 운전에 집중!", descriptions["accessory:mobi_headphones"])
        assertEquals("빈티지 느낌에는 고글만한게 없죠~", descriptions["accessory:mobi_goggles"])
        assertEquals("깔끔한 배경화면이에요", descriptions["none:background"])
        assertEquals("반짝반짝 별이 내려요~", descriptions["background:star"])
        assertEquals("소복소복 눈이 내려요~", descriptions["background:snow"])
        assertEquals("살랑살랑 꽃이 내려요~", descriptions["background:petal"])
    }

    @Test fun catalogFailureRetainsPreviewAndOffersRetryBeforeApplying() {
        var retries = 0
        lateinit var view: View
        val catalog =
            listOf(
                CosmeticItem("friend:mobi", CosmeticSlot.FRIEND, 0),
                CosmeticItem("friend:luna", CosmeticSlot.FRIEND, 0),
            )
        compose.setContent {
            val currentView = LocalView.current
            SideEffect { view = currentView }
            var failed by remember { mutableStateOf(true) }
            MobiMonTheme {
                CustomizationScreen(
                    inventory =
                        CosmeticInventory(
                            catalog.mapTo(mutableSetOf()) { it.id },
                            mapOf(
                                CosmeticSlot.FRIEND to "friend:mobi",
                            ),
                        ),
                    catalog = catalog,
                    selectedItemId = "friend:luna",
                    purchasing = false,
                    purchaseFailed = false,
                    onSelectItem = {},
                    onPurchaseItem = { _, _ -> },
                    onEquipItem = {},
                    onEquipFriend = {},
                    pointBalance = 1200,
                    pointLoadFailed = false,
                    catalogLoadFailed = failed,
                    onRetry = {
                        retries++
                        failed = false
                    },
                )
            }
        }

        compose.onNodeWithTag("store-reference").assertExists()
        compose.onNodeWithText("아이템 목록을 불러오지 못했어요.").assertIsDisplayed()
        compose.onNodeWithText("루나와 함께하기").assertIsNotEnabled()
        assertFriendCardFitsCatalogViewport()
        capture(view, "catalog-recovery")
        compose.onNodeWithText("다시 시도").assertIsDisplayed().performClick()
        assertEquals(1, retries)
        compose.onNodeWithText("다시 시도").assertDoesNotExist()
        compose.onNodeWithText("루나와 함께하기").assertIsEnabled()
    }

    @Test fun walletFailureRetainsPreviewAndOffersRetry() {
        var retries = 0
        lateinit var view: View
        val catalog =
            listOf(
                CosmeticItem("friend:mobi", CosmeticSlot.FRIEND, 0),
                CosmeticItem("friend:luna", CosmeticSlot.FRIEND, 0),
            )
        compose.setContent {
            val currentView = LocalView.current
            SideEffect { view = currentView }
            var failed by remember { mutableStateOf(true) }
            MobiMonTheme {
                CustomizationScreen(
                    inventory =
                        CosmeticInventory(
                            catalog.mapTo(mutableSetOf()) { it.id },
                            mapOf(CosmeticSlot.FRIEND to "friend:mobi"),
                        ),
                    catalog = catalog,
                    selectedItemId = "friend:luna",
                    purchasing = false,
                    purchaseFailed = false,
                    onSelectItem = {},
                    onPurchaseItem = { _, _ -> },
                    onEquipItem = {},
                    onEquipFriend = {},
                    pointBalance = if (failed) null else 1200,
                    pointLoadFailed = failed,
                    onRetry = {
                        retries++
                        failed = false
                    },
                )
            }
        }

        compose.onNodeWithTag("store-reference").assertExists()
        compose.onNodeWithTag("preview-character").assertExists()
        compose.onNodeWithText("다시 시도").assertIsDisplayed()
        assertFriendCardFitsCatalogViewport()
        capture(view, "wallet-recovery")
        compose.onNodeWithText("다시 시도").performClick()
        assertEquals(1, retries)
        compose.onNodeWithText("다시 시도").assertDoesNotExist()
        compose.onNodeWithText("루나와 함께하기").assertIsEnabled()
    }

    @Test fun unverifiedParkingDisablesApplyAndDoesNotDispatchTheCommand() {
        var applied: String? = null
        val catalog =
            listOf(
                CosmeticItem("friend:mobi", CosmeticSlot.FRIEND, 0),
                CosmeticItem("friend:luna", CosmeticSlot.FRIEND, 0),
            )
        compose.setContent {
            MobiMonTheme {
                CustomizationScreen(
                    inventory =
                        CosmeticInventory(
                            catalog.mapTo(mutableSetOf()) { it.id },
                            mapOf(
                                CosmeticSlot.FRIEND to "friend:mobi",
                            ),
                        ),
                    catalog = catalog,
                    selectedItemId = "friend:luna",
                    purchasing = false,
                    purchaseFailed = false,
                    onSelectItem = {},
                    onPurchaseItem = { _, _ -> },
                    onEquipItem = {},
                    onEquipFriend = { applied = it },
                    pointBalance = 1200,
                    pointLoadFailed = false,
                    interactionAllowed = false,
                )
            }
        }

        compose.onNodeWithText("루나와 함께하기").assertIsNotEnabled().performClick()
        assertNull(applied)
    }

    @Test fun previewBackgroundFollowsTimeOfDay() {
        assertEquals(
            com.monsters.mobimon.core.ui.R.drawable.pet_home_background_morning,
            companionBackgroundRes("Morning"),
        )
        assertEquals(com.monsters.mobimon.core.ui.R.drawable.pet_home_background_day, companionBackgroundRes("Day"))
        assertEquals(com.monsters.mobimon.core.ui.R.drawable.pet_home_background_night, companionBackgroundRes("Night"))
        assertEquals(
            com.monsters.mobimon.core.ui.R.drawable.pet_home_background_morning,
            companionBackgroundRes("morning"),
        )
        assertEquals(com.monsters.mobimon.core.ui.R.drawable.pet_home_background_day, companionBackgroundRes("day"))
        assertEquals(com.monsters.mobimon.core.ui.R.drawable.pet_home_background_night, companionBackgroundRes("night"))
        assertEquals(
            com.monsters.mobimon.core.ui.R.drawable.pet_home_background_morning,
            companionBackgroundRes("09"),
        )
        assertEquals(com.monsters.mobimon.core.ui.R.drawable.pet_home_background_day, companionBackgroundRes("14"))
        assertEquals(com.monsters.mobimon.core.ui.R.drawable.pet_home_background_night, companionBackgroundRes("20"))
        assertEquals(
            com.monsters.mobimon.core.ui.R.drawable.pet_home_background_morning,
            companionBackgroundRes("아침"),
        )
        assertEquals(com.monsters.mobimon.core.ui.R.drawable.pet_home_background_day, companionBackgroundRes("낮"))
        assertEquals(com.monsters.mobimon.core.ui.R.drawable.pet_home_background_night, companionBackgroundRes("밤"))
    }

    private fun assertFriendCardFitsCatalogViewport() {
        val viewport = compose.onNodeWithTag("shop-items").getUnclippedBoundsInRoot()
        val card = compose.onNodeWithText("모비").getUnclippedBoundsInRoot()
        assertTrue("Friend card status must fit above the recovery controls", card.bottom <= viewport.bottom)
    }

    private fun capture(
        view: View,
        name: String,
    ) {
        compose.runOnIdle {
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            view.draw(Canvas(bitmap))
            val directory = File("build/reports/store-ui").apply { mkdirs() }
            File(directory, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            bitmap.recycle()
        }
    }
}
