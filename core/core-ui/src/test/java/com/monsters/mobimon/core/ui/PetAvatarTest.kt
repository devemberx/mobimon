package com.monsters.mobimon.core.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w600dp-h300dp-mdpi")
class PetAvatarTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun equippedLooksUseIsolatedAssetsAndLunaIsNormalized() {
        CharacterArtwork.equippedLooks.values.forEach { assertNull(it.crop) }
        assertTrue(CharacterArtwork.characters.getValue("friend:luna").visualScale < 1f)
    }

    @Test
    fun supportedMobiVariantsHaveDistinctPixelSignatures() {
        lateinit var view: View
        compose.setContent {
            val currentView = LocalView.current
            SideEffect { view = currentView }
            MobiMonTheme {
                Row {
                    PetAvatar(modifier = Modifier.testTag("default"))
                    PetAvatar(
                        modifier = Modifier.testTag("cream"),
                        appearanceKey = "CREAM",
                    )
                    PetAvatar(
                        modifier = Modifier.testTag("headphones"),
                        accessoryId = "accessory:mobi_headphones",
                    )
                    PetAvatar(
                        modifier = Modifier.testTag("goggles"),
                        accessoryId = "accessory:mobi_goggles",
                    )
                }
            }
        }

        val tags = listOf("default", "cream", "headphones", "goggles")
        val bounds = tags.associateWith { compose.onNodeWithTag(it).fetchSemanticsNode().boundsInRoot }
        lateinit var signatures: Map<String, List<Int>>
        compose.runOnIdle {
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            view.draw(Canvas(bitmap))
            signatures =
                bounds.mapValues { (_, area) ->
                    val left = area.left.toInt()
                    val top = area.top.toInt()
                    val width = area.width.toInt()
                    val height = area.height.toInt()
                    IntArray(width * height)
                        .also { pixels ->
                            bitmap.getPixels(pixels, 0, width, left, top, width, height)
                        }.toList()
                }
            bitmap.recycle()
        }

        signatures.values.toList().forEachIndexed { index, signature ->
            signatures.values.drop(index + 1).forEach { other -> assertNotEquals(signature, other) }
        }
        assertTrue(signatures.getValue("default").toSet().size > 1_000)
        assertTrue(0xFFF2E4C8.toInt() in signatures.getValue("cream"))
    }

    @Test
    fun mobiAnimationCacheLoadsTwelveFramesFromAssets() {
        val context =
            androidx.test.core.app.ApplicationProvider
                .getApplicationContext<android.content.Context>()
        val frames = MobiAnimationCache.getOrLoadFrames(context)
        org.junit.Assert.assertEquals(12, frames.size)
    }
}
