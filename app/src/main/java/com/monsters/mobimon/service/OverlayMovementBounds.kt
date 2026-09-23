package com.monsters.mobimon.service

internal data class OverlayMovementBounds(
    val minX: Int,
    val minY: Int,
    val maxX: Int,
    val maxY: Int,
) {
    fun clamp(
        x: Int,
        y: Int,
    ): Pair<Int, Int> = x.coerceIn(minX, maxX) to y.coerceIn(minY, maxY)
}

// Coordinates use the full window origin; fitInsetsTypes is disabled on the overlay.
internal fun overlayMovementBounds(
    width: Int,
    height: Int,
    left: Int,
    top: Int,
    right: Int,
    bottom: Int,
    overlayWidth: Int,
    overlayHeight: Int,
): OverlayMovementBounds =
    OverlayMovementBounds(
        left,
        top,
        (width - right - overlayWidth).coerceAtLeast(left),
        (height - bottom - overlayHeight).coerceAtLeast(top),
    )
