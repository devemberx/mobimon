package com.monsters.mobimon.core.ui

import android.graphics.BitmapFactory
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize

data class CharacterAsset(
    @DrawableRes val resourceId: Int,
    val crop: AssetCrop? = null,
    val visualScale: Float = 1f,
)

data class AssetCrop(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
)

/** Asset IDs stay independent of composition and can be extended when artwork arrives. */
object CharacterArtwork {
    val characters =
        mapOf(
            "friend:mobi" to CharacterAsset(R.drawable.mobimon_mobi_v4),
            "friend:luna" to CharacterAsset(R.drawable.mobimon_luna_v4, visualScale = 0.87f),
        )

    val equippedLooks =
        mapOf(
            "accessory:mobi_headphones" to CharacterAsset(R.drawable.mobimon_mobi_headphones_v4, visualScale = 0.93f),
            "accessory:mobi_goggles" to CharacterAsset(R.drawable.mobimon_mobi_goggles_v4),
            "accessory:luna_cap" to CharacterAsset(R.drawable.mobimon_luna_cap_v4),
            "accessory:luna_sunglasses" to CharacterAsset(R.drawable.mobimon_luna_sunglasses_v4, visualScale = 0.96f),
        )

    val itemIcons =
        mapOf(
            "accessory:mobi_headphones" to CharacterAsset(R.drawable.mobimon_mobi_items_v4, AssetCrop(0, 0, 450, 724)),
            "accessory:mobi_goggles" to CharacterAsset(R.drawable.mobimon_mobi_items_v4, AssetCrop(450, 0, 450, 724)),
            "accessory:luna_cap" to CharacterAsset(R.drawable.mobimon_luna_items_v4, AssetCrop(0, 0, 455, 724)),
            "accessory:luna_sunglasses" to
                CharacterAsset(
                    R.drawable.mobimon_luna_items_v4,
                    AssetCrop(470, 0, 455, 724),
                ),
        )

    // Add drawable resource mappings here when selectable background art is delivered.
    val backgrounds: Map<String, CharacterAsset> =
        mapOf(
            "background:star" to CharacterAsset(R.drawable.ic_star_particle),
            "background:snow" to CharacterAsset(R.drawable.ic_star_particle),
            "background:petal" to CharacterAsset(R.drawable.ic_star_particle),
            "background:night" to CharacterAsset(R.drawable.ic_star_particle),
        )

    fun preview(
        friendId: String,
        accessoryId: String?,
    ): CharacterAsset = equippedLooks[accessoryId] ?: characters[friendId] ?: characters.getValue("friend:mobi")
}

@Composable
fun CharacterAssetImage(
    asset: CharacterAsset,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    val context = LocalContext.current
    val crop = asset.crop
    val painter =
        if (crop == null) {
            painterResource(asset.resourceId)
        } else {
            val bitmap =
                remember(
                    asset.resourceId,
                ) { BitmapFactory.decodeResource(context.resources, asset.resourceId).asImageBitmap() }
            remember(bitmap, crop) {
                BitmapPainter(bitmap, srcOffset = IntOffset(crop.x, crop.y), srcSize = IntSize(crop.width, crop.height))
            }
        }
    Box(modifier, contentAlignment = Alignment.Center) {
        Image(
            painter,
            contentDescription,
            modifier = Modifier.fillMaxSize(asset.visualScale),
            contentScale = ContentScale.Fit,
        )
    }
}
