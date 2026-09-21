package com.monsters.mobimon.ui

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

/** Expand the trigger's rounded bounds to the destination without moving the Home scene. */
internal fun Modifier.revealFrom(
    origin: Rect,
    progress: () -> Float,
): Modifier =
    graphicsLayer {
        val fraction = progress().coerceIn(0f, 1f)
        // Layer clipping applies the same rounded bounds to drawing and pointer hit testing.
        clip = fraction < 1f
        shape = RevealShape(origin, fraction)
    }

private data class RevealShape(
    val origin: Rect,
    val fraction: Float,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val source =
            Rect(
                origin.left * size.width,
                origin.top * size.height,
                origin.right * size.width,
                origin.bottom * size.height,
            )
        val destination = Rect(0f, 0f, size.width, size.height)
        val bounds = lerp(source, destination, fraction)
        val radius = source.height / 2 * (1 - fraction)
        return Outline.Rounded(RoundRect(bounds, CornerRadius(radius, radius)))
    }
}
