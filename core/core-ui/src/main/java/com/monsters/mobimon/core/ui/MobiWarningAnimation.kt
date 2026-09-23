package com.monsters.mobimon.core.ui

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.withInfiniteAnimationFrameNanos
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

internal object MobiCollapsedTimeline {
    const val PERIOD_NANOS = 4_000_000_000L
    const val BLINK_PERIOD_NANOS = 6_500_000_000L

    fun breathAt(elapsedNanos: Long): Float {
        val phase = elapsedNanos.coerceAtLeast(0L) % PERIOD_NANOS
        return ((1.0 - kotlin.math.cos(2.0 * kotlin.math.PI * phase / PERIOD_NANOS)) * 0.5).toFloat()
    }

    fun eyeOpenAt(elapsedNanos: Long): Float {
        val phase = (elapsedNanos.coerceAtLeast(0L) % BLINK_PERIOD_NANOS) / 1_000_000f
        val value =
            when {
                phase < 5500f -> 0f
                phase < 5900f -> (phase - 5500f) / 400f
                phase < 6100f -> 1f
                else -> (6500f - phase) / 400f
            }
        return value * value * (3f - 2f * value)
    }
}

/** Opacity only: toggling warning during a fade continues from the current opacity. */
internal class MobiWarningBlend {
    val opacity = Animatable(0f)

    suspend fun target(
        warning: Boolean,
        motionEnabled: Boolean,
    ) {
        val end = if (warning) 1f else 0f
        if (motionEnabled) opacity.animateTo(end, tween(200, easing = LinearEasing)) else opacity.snapTo(end)
    }
}

internal data class MobiWarningSheets(
    val closed: android.graphics.Bitmap,
    val tiredEyes: android.graphics.Bitmap,
)

internal object MobiWarningCache {
    const val CELL = 408
    const val LOGICAL_CELL = 256
    private const val DIRECTORY = "characters/mobi/unhealthy/"

    @Volatile private var cached: MobiWarningSheets? = null

    fun peek(): MobiWarningSheets? = cached

    fun load(context: Context): MobiWarningSheets? =
        cached ?: synchronized(this) {
            cached ?: try {
                fun decode(name: String): android.graphics.Bitmap =
                    context.applicationContext.assets
                        .open("${DIRECTORY}mobi_collapsed_$name.png")
                        .use {
                            val options = BitmapFactory.Options().apply { inScaled = false }
                            val bitmap = requireNotNull(BitmapFactory.decodeStream(it, null, options))
                            require(bitmap.width == CELL && bitmap.height == CELL)
                            bitmap
                        }
                MobiWarningSheets(decode("closed"), decode("tired_eyes")).also { cached = it }
            } catch (_: java.io.IOException) {
                null
            }
        }
}

/** Normal and collapsed idle share one fixed layout slot and crossfade only. */
@Composable
fun MobiIdleBreathAnimation(
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    fallbackAsset: CharacterAsset = CharacterArtwork.characters.getValue("friend:mobi"),
    vehicleWarning: Boolean = false,
    animateNormal: Boolean = true,
    motionEnabled: Boolean = LocalMobiMonMotionEnabled.current,
) {
    val context = LocalContext.current.applicationContext
    val blend = remember { MobiWarningBlend() }
    val enabled = motionEnabled && LocalMobiMonMotionEnabled.current
    val sheets by produceState(initialValue = MobiWarningCache.peek(), context, vehicleWarning) {
        if (vehicleWarning && value == null) value = withContext(Dispatchers.IO) { MobiWarningCache.load(context) }
    }
    LaunchedEffect(vehicleWarning, enabled, sheets) {
        if (sheets != null) blend.target(vehicleWarning, enabled)
    }
    val showNormal by remember { derivedStateOf { blend.opacity.value < 1f } }
    val showCollapsed by remember { derivedStateOf { blend.opacity.value > 0f } }
    BoxWithConstraints(
        modifier.semantics { if (contentDescription != null) this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        if (showNormal) {
            Box(Modifier.matchParentSize().graphicsLayer { alpha = 1f - blend.opacity.value }) {
                if (animateNormal && enabled) {
                    NormalMobiIdleAnimation(Modifier.matchParentSize(), null, fallbackAsset)
                } else {
                    CharacterAssetImage(fallbackAsset, Modifier.matchParentSize(), null)
                }
            }
        }
        val loaded = sheets
        if (showCollapsed && loaded != null) {
            val elapsed = remember { mutableLongStateOf(0L) }
            LaunchedEffect(enabled) {
                elapsed.longValue = 0L
                if (enabled) {
                    val origin = withInfiniteAnimationFrameNanos { it }
                    while (isActive) elapsed.longValue = withInfiniteAnimationFrameNanos { it } - origin
                }
            }
            // Same source coordinate system and destination for every breathing pose.
            val extent = minOf(maxWidth, maxHeight) * MobiWarningCache.CELL / MobiWarningCache.LOGICAL_CELL
            Box(
                Modifier
                    .requiredSize(extent)
                    .graphicsLayer {
                        alpha = blend.opacity.value
                        translationY = size.minDimension * MobiWarningCache.LOGICAL_CELL / MobiWarningCache.CELL *
                            fallbackAsset.translationYFraction
                        compositingStrategy = CompositingStrategy.Offscreen
                        clip = false
                    }.mobiCollapsedDrawing(loaded) { elapsed.longValue },
            )
        }
    }
}
