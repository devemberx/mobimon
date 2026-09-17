package com.monsters.mobimon.feature.pet

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.monsters.mobimon.core.domain.CosmeticInventory
import com.monsters.mobimon.core.domain.CosmeticItem
import com.monsters.mobimon.core.domain.CosmeticSlot
import com.monsters.mobimon.core.ui.MobiMonTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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
