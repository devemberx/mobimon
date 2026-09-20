package com.monsters.mobimon.core.ui

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.animation.core.withInfiniteAnimationFrameNanos
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.isActive
import java.util.Locale

private val IDLE_BREATH_FRAME_DURATIONS_MS =
    IntArray(24) { if (it == 23) 130 else 90 }

internal object MobiAnimationCache {
    @Volatile
    private var cachedFrames: List<ImageBitmap>? = null

    fun getOrLoadFrames(context: Context): List<ImageBitmap> {
        cachedFrames?.let { return it }
        return synchronized(this) {
            cachedFrames?.let { return it }
            try {
                val assetManager = context.applicationContext?.assets ?: context.assets
                val decodeOptions = BitmapFactory.Options().apply { inSampleSize = 2 }
                val frames =
                    (1..24).map { i ->
                        val path =
                            String.format(
                                Locale.US,
                                "characters/mobi/idle_breath/mobi_idle_breath_%02d.png",
                                i,
                            )
                        assetManager.open(path).use { stream ->
                            BitmapFactory.decodeStream(stream, null, decodeOptions)!!.asImageBitmap()
                        }
                    }
                cachedFrames = frames
                frames
            } catch (_: Exception) {
                emptyList()
            }
        }
    }
}

internal object LunaAnimationCache {
    @Volatile
    private var cachedFrames: List<ImageBitmap>? = null

    fun getOrLoadFrames(context: Context): List<ImageBitmap> {
        cachedFrames?.let { return it }
        return synchronized(this) {
            cachedFrames?.let { return it }
            try {
                val assetManager = context.applicationContext?.assets ?: context.assets
                val decodeOptions = BitmapFactory.Options().apply { inSampleSize = 2 }
                val frames =
                    (1..24).map { i ->
                        val path =
                            String.format(
                                Locale.US,
                                "characters/luna/idle_breath/luna_idle_%02d.png",
                                i,
                            )
                        assetManager.open(path).use { stream ->
                            BitmapFactory.decodeStream(stream, null, decodeOptions)!!.asImageBitmap()
                        }
                    }
                cachedFrames = frames
                frames
            } catch (_: Exception) {
                emptyList()
            }
        }
    }
}

enum class PetEmotion {
    IDLE,
    HAPPY,
}

/**
 * Renders the selected character from external artwork and retains the historical Cream fallback.
 * [appearanceKey] accepts GOLDEN or CREAM without importing a domain model.
 */
@Composable
fun PetAvatar(
    modifier: Modifier = Modifier,
    appearanceKey: String = "GOLDEN",
    friendId: String = "friend:mobi",
    accessoryId: String? = null,
    outfitId: String? = null,
    backgroundId: String? = null,
    isAnimated: Boolean = true,
    emotion: PetEmotion = PetEmotion.IDLE,
) {
    val cat = friendId == "friend:luna"
    val cream = appearanceKey == "CREAM"
    val motionEnabled = isAnimated && LocalMobiMonMotionEnabled.current
    val description = stringResource(if (cat) R.string.mobimon_luna_description else R.string.mobimon_mobi_description)
    if (emotion == PetEmotion.HAPPY) {
        val happyAsset = CharacterArtwork.happy(friendId, accessoryId ?: outfitId)
        Box(modifier = modifier.size(120.dp).semantics { contentDescription = description }) {
            backgroundId?.let { CharacterArtwork.backgrounds[it] }?.let {
                CharacterAssetImage(it, Modifier.fillMaxSize())
            }
            CharacterAssetImage(happyAsset, Modifier.fillMaxSize())
        }
        return
    }
    if (!cream || cat) {
        Box(modifier = modifier.size(120.dp).semantics { contentDescription = description }) {
            backgroundId?.let { CharacterArtwork.backgrounds[it] }?.let {
                CharacterAssetImage(it, Modifier.fillMaxSize())
            }
            val equippedLook = CharacterArtwork.equippedLooks[accessoryId ?: outfitId]
            if (motionEnabled && (friendId == "friend:mobi") && (equippedLook == null)) {
                MobiIdleBreathAnimation(
                    modifier = Modifier.fillMaxSize(),
                    fallbackAsset = CharacterArtwork.preview(friendId, accessoryId ?: outfitId),
                )
            } else if (motionEnabled && (friendId == "friend:luna") && (equippedLook == null)) {
                LunaIdleBreathAnimation(
                    modifier = Modifier.fillMaxSize(),
                    fallbackAsset = CharacterArtwork.preview(friendId, accessoryId ?: outfitId),
                )
            } else {
                CharacterAssetImage(CharacterArtwork.preview(friendId, accessoryId ?: outfitId), Modifier.fillMaxSize())
            }
        }
        return
    }
    val fur = Color(0xFFF2E4C8)
    val ear = Color(0xFFD4C29D)
    Canvas(modifier = modifier.size(120.dp).semantics { contentDescription = description }) {
        val radius = size.minDimension * 0.34f
        val center = Offset(size.width / 2, size.height / 2)
        drawCircle(ear, radius * 0.48f, center + Offset(-radius * 0.85f, -radius * 0.4f))
        drawCircle(ear, radius * 0.48f, center + Offset(radius * 0.85f, -radius * 0.4f))
        drawCircle(fur, radius, center)
        drawCircle(Color(0xFF51402C), radius * 0.1f, center + Offset(0f, radius * 0.25f))
    }
}

@Composable
fun MobiIdleBreathAnimation(
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    fallbackAsset: CharacterAsset = CharacterArtwork.characters.getValue("friend:mobi"),
) {
    IdleBreathAnimation(MobiAnimationCache::getOrLoadFrames, false, modifier, contentDescription, fallbackAsset)
}

@Composable
fun LunaIdleBreathAnimation(
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    fallbackAsset: CharacterAsset = CharacterArtwork.characters.getValue("friend:luna"),
) {
    IdleBreathAnimation(LunaAnimationCache::getOrLoadFrames, true, modifier, contentDescription, fallbackAsset)
}

@Composable
private fun IdleBreathAnimation(
    loadFrames: (Context) -> List<ImageBitmap>,
    applyAssetTransform: Boolean,
    modifier: Modifier,
    contentDescription: String?,
    fallbackAsset: CharacterAsset,
) {
    if (!LocalMobiMonMotionEnabled.current) {
        CharacterAssetImage(fallbackAsset, modifier, contentDescription)
        return
    }
    val context = LocalContext.current
    val frames = remember(context, loadFrames) { loadFrames(context) }

    if (frames.isEmpty()) {
        CharacterAssetImage(
            asset = fallbackAsset,
            modifier = modifier,
            contentDescription = contentDescription,
        )
    } else {
        var currentFrameIndex by remember(frames) { mutableIntStateOf(0) }
        LaunchedEffect(frames) {
            var previousTime = withInfiniteAnimationFrameNanos { it }
            var elapsedNanos = 0L
            while (isActive) {
                val time = withInfiniteAnimationFrameNanos { it }
                // Resume from a stopped window without jumping through the whole breathing cycle.
                elapsedNanos += (time - previousTime).coerceAtMost(130_000_000L)
                previousTime = time
                var nextFrame = currentFrameIndex
                while (elapsedNanos >= IDLE_BREATH_FRAME_DURATIONS_MS[nextFrame] * 1_000_000L) {
                    elapsedNanos -= IDLE_BREATH_FRAME_DURATIONS_MS[nextFrame] * 1_000_000L
                    nextFrame = (nextFrame + 1) % frames.size
                }
                currentFrameIndex = nextFrame
            }
        }
        Box(
            modifier =
                if (applyAssetTransform) {
                    modifier.graphicsLayer {
                        scaleX = fallbackAsset.visualScale
                        scaleY = fallbackAsset.visualScale
                        translationX = size.width * fallbackAsset.translationXFraction
                        translationY = size.height * fallbackAsset.translationYFraction
                    }
                } else {
                    modifier
                },
            contentAlignment = Alignment.Center,
        ) {
            Image(
                bitmap = frames[currentFrameIndex],
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
            )
        }
    }
}
