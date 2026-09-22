package com.monsters.mobimon.core.ui

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.animation.core.withInfiniteAnimationFrameNanos
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

internal object MobiIdleTimeline {
    const val COLUMNS = 6
    const val ROWS = 4
    const val FRAME_COUNT = COLUMNS * ROWS
    const val TILT_PERIOD_MS = 6_200L
    const val BOB_PERIOD_MS = 6_600L

    // Sources already contain inhale/exhale and repeated extreme poses; do not ping-pong.
    private val durationsMs =
        intArrayOf(
            180,
            190,
            190,
            190,
            190,
            190,
            180,
            180,
            210,
            200,
            180,
            180,
            180,
            210,
            205,
            200,
            195,
            195,
            190,
            190,
            190,
            180,
            180,
            180,
        )
    val cycleMs: Long = 4_050L
    private val sourceDurationMs = durationsMs.sum().toLong()
    private val endsMs = durationsMs.runningFold(0L) { sum, duration -> sum + duration }.drop(1).toLongArray()

    fun frameAt(elapsedNanos: Long): Int {
        val position = sourcePosition(elapsedNanos)
        for (index in endsMs.indices) if (position < endsMs[index]) return index
        return 0
    }

    private fun sourcePosition(elapsedNanos: Long): Double =
        (elapsedNanos.coerceAtLeast(0L) % (cycleMs * 1_000_000L)).toDouble() *
            sourceDurationMs / (cycleMs * 1_000_000L)

    fun blendAt(
        elapsedNanos: Long,
        frame: Int = frameAt(elapsedNanos),
    ): Float {
        val start = if (frame == 0) 0L else endsMs[frame - 1]
        return ((sourcePosition(elapsedNanos) - start) / durationsMs[frame]).toFloat().coerceIn(0f, 1f)
    }

    fun breathAt(elapsedNanos: Long): Float {
        val phase = (elapsedNanos.coerceAtLeast(0L) % (cycleMs * 1_000_000L)).toDouble()
        return ((1 - cos(2 * PI * phase / (cycleMs * 1_000_000L))) / 2).toFloat()
    }

    fun bobAt(elapsedNanos: Long): Float {
        val phase = (elapsedNanos.coerceAtLeast(0L) % (BOB_PERIOD_MS * 1_000_000L)).toDouble()
        val wave = sin(2 * PI * phase / (BOB_PERIOD_MS * 1_000_000L))
        return (wave * wave).toFloat()
    }

    fun scaleXAt(elapsedNanos: Long): Float = 1f + 0.012f * breathAt(elapsedNanos) + 0.005f * (1f - bobAt(elapsedNanos))

    fun scaleYAt(elapsedNanos: Long): Float = 1f + 0.024f * breathAt(elapsedNanos) - 0.004f * (1f - bobAt(elapsedNanos))

    // Relative to the fixed sprite box: about 4dp breath + 2.4dp bob at the 600dp Home reference size.
    fun liftFractionAt(elapsedNanos: Long): Float = -0.0067f * breathAt(elapsedNanos) - 0.004f * bobAt(elapsedNanos)

    fun tiltAt(elapsedNanos: Long): Float {
        val phase = (elapsedNanos.coerceAtLeast(0L) % (TILT_PERIOD_MS * 1_000_000L)).toDouble()
        return (2.35 * sin(2 * PI * phase / (TILT_PERIOD_MS * 1_000_000L))).toFloat()
    }
}

internal object MobiSpriteCache {
    const val ASSET_PATH = "characters/mobi/idle_breath/mobi_idle_breath_sprite.png"

    @Volatile private var cached: ImageBitmap? = null

    fun peek(): ImageBitmap? = cached

    fun getOrLoad(context: Context): ImageBitmap? {
        cached?.let { return it }
        return synchronized(this) {
            cached?.let { return it }
            val assets = context.applicationContext.assets
            // Retain the previous renderer's 627px decoded cells. The shipped atlas is lossless/full resolution.
            // Full software decode would exceed Android's 100MiB Canvas bitmap limit (144MiB).
            val options =
                BitmapFactory.Options().apply {
                    inSampleSize = 2
                    inScaled = false
                }
            try {
                assets.open(ASSET_PATH).use { stream ->
                    val bitmap = requireNotNull(BitmapFactory.decodeStream(stream, null, options))
                    require(
                        bitmap.width == 627 * MobiIdleTimeline.COLUMNS && bitmap.height == 627 * MobiIdleTimeline.ROWS,
                    )
                    bitmap.asImageBitmap().also { cached = it }
                }
            } catch (_: java.io.IOException) {
                null
            }
        }
    }
}

/** One atlas, a fixed destination, and draw/layer-only clock reads. No per-frame composition or bitmap crops. */
@Composable
fun MobiIdleBreathAnimation(
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    fallbackAsset: CharacterAsset = CharacterArtwork.characters.getValue("friend:mobi"),
) {
    if (!LocalMobiMonMotionEnabled.current) {
        CharacterAssetImage(fallbackAsset, modifier, contentDescription)
        return
    }
    val context = LocalContext.current.applicationContext
    val sprite by produceState<ImageBitmap?>(initialValue = MobiSpriteCache.peek(), context) {
        value = withContext(Dispatchers.IO) { MobiSpriteCache.getOrLoad(context) }
    }
    val sheet = sprite
    if (sheet == null) {
        CharacterAssetImage(fallbackAsset, modifier, contentDescription)
        return
    }
    val elapsed = remember { mutableLongStateOf(0L) }
    LaunchedEffect(sheet) {
        val origin = withInfiniteAnimationFrameNanos { it }
        while (isActive) {
            elapsed.longValue = withInfiniteAnimationFrameNanos { it } - origin
        }
    }
    Box(
        modifier
            .semantics { if (contentDescription != null) this.contentDescription = contentDescription }
            .graphicsLayer {
                val time = elapsed.longValue
                rotationZ = MobiIdleTimeline.tiltAt(time)
                scaleX = MobiIdleTimeline.scaleXAt(time)
                scaleY = MobiIdleTimeline.scaleYAt(time)
                translationY = size.minDimension * MobiIdleTimeline.liftFractionAt(time)
                // Isolate premultiplied interpolation from the Home background; keep body/hands/wheel together.
                compositingStrategy = CompositingStrategy.Offscreen
                transformOrigin = TransformOrigin(0.5f, 0.9f)
                clip = false
            }.drawWithCache {
                val cell = IntSize(sheet.width / MobiIdleTimeline.COLUMNS, sheet.height / MobiIdleTimeline.ROWS)
                val sources =
                    Array(MobiIdleTimeline.FRAME_COUNT) { index ->
                        IntOffset(
                            index % MobiIdleTimeline.COLUMNS * cell.width,
                            index / MobiIdleTimeline.COLUMNS * cell.height,
                        )
                    }
                val side = size.minDimension.roundToInt()
                val destination = IntSize(side, side)
                val destinationOffset =
                    IntOffset(((size.width - side) / 2).roundToInt(), ((size.height - side) / 2).roundToInt())
                onDrawBehind {
                    val time = elapsed.longValue
                    val frame = MobiIdleTimeline.frameAt(time)
                    val blend = MobiIdleTimeline.blendAt(time, frame)
                    drawImage(
                        image = sheet,
                        srcOffset = sources[frame],
                        srcSize = cell,
                        dstOffset = destinationOffset,
                        dstSize = destination,
                        filterQuality = FilterQuality.Low,
                        alpha = 1f - blend,
                    )
                    if (blend > 0f) {
                        drawImage(
                            image = sheet,
                            srcOffset = sources[(frame + 1) % MobiIdleTimeline.FRAME_COUNT],
                            srcSize = cell,
                            dstOffset = destinationOffset,
                            dstSize = destination,
                            filterQuality = FilterQuality.Low,
                            alpha = blend,
                            blendMode = BlendMode.Plus,
                        )
                    }
                }
            },
    )
}
