package com.monsters.mobimon.core.ui

import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import kotlin.math.roundToInt

/** Local torso bulge only. Feet, wheel, face and sprout are outside the deformation region. */
internal object MobiCollapsedGeometry {
    fun torsoOffset(
        x: Float,
        y: Float,
        breath: Float,
    ): Float {
        val horizontal = (x - 114f) / 24f
        val vertical = (y - 258f) / 28f
        val weight = (1f - horizontal * horizontal - vertical * vertical).coerceAtLeast(0f)
        return -1.4f * breath * weight * weight
    }
}

/** Deforms pixels within one fixed sprite canvas; no whole-sprite transform or pose registration. */
internal fun Modifier.mobiCollapsedDrawing(
    sheets: MobiWarningSheets,
    elapsed: () -> Long,
): Modifier =
    drawWithCache {
        val divisions = 32
        val vertices = FloatArray((divisions + 1) * (divisions + 1) * 2)
        val closedPaint = Paint(Paint.FILTER_BITMAP_FLAG)
        val eyePaint = Paint(Paint.FILTER_BITMAP_FLAG).apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.ADD) }
        val scale = size.minDimension / MobiWarningCache.CELL
        val offsetX = (size.width - size.minDimension) / 2f
        val offsetY = (size.height - size.minDimension) / 2f
        onDrawBehind {
            val time = elapsed()
            val breath = MobiCollapsedTimeline.breathAt(time)
            var index = 0
            for (row in 0..divisions) {
                for (column in 0..divisions) {
                    val x = column * MobiWarningCache.CELL.toFloat() / divisions
                    val y = row * MobiWarningCache.CELL.toFloat() / divisions
                    vertices[index++] = offsetX + (x + MobiCollapsedGeometry.torsoOffset(x, y, breath)) * scale
                    vertices[index++] = offsetY + y * scale
                }
            }
            eyePaint.alpha = (MobiCollapsedTimeline.eyeOpenAt(time) * 255).roundToInt()
            closedPaint.alpha = 255 - eyePaint.alpha
            drawIntoCanvas { canvas ->
                canvas.nativeCanvas.drawBitmapMesh(
                    sheets.closed,
                    divisions,
                    divisions,
                    vertices,
                    0,
                    null,
                    0,
                    closedPaint,
                )
                if (eyePaint.alpha > 0) {
                    canvas.nativeCanvas.drawBitmapMesh(
                        sheets.tiredEyes,
                        divisions,
                        divisions,
                        vertices,
                        0,
                        null,
                        0,
                        eyePaint,
                    )
                }
            }
        }
    }
