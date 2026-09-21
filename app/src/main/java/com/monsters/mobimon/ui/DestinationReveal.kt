package com.monsters.mobimon.ui

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath

/** Expand the trigger's rounded bounds to the destination without moving the Home scene. */
internal fun Modifier.revealFrom(
    origin: Rect,
    progress: () -> Float,
): Modifier =
    drawWithCache {
        val source =
            Rect(
                origin.left * size.width,
                origin.top * size.height,
                origin.right * size.width,
                origin.bottom * size.height,
            )
        val destination = Rect(0f, 0f, size.width, size.height)
        val path = Path()
        onDrawWithContent {
            val fraction = progress().coerceIn(0f, 1f)
            if (fraction == 1f) {
                drawContent()
            } else {
                val bounds = lerp(source, destination, fraction)
                val radius = source.height / 2 * (1 - fraction)
                path.reset()
                path.addRoundRect(RoundRect(bounds, CornerRadius(radius, radius)))
                clipPath(path) { this@onDrawWithContent.drawContent() }
            }
        }
    }
