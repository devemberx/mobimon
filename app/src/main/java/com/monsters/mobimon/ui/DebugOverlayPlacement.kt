package com.monsters.mobimon.ui

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
internal fun DebugOverlayPlacement(content: @Composable () -> Unit) {
    var requested by remember { mutableStateOf(Offset(50f, 100f)) }
    val placement = remember { DebugPlacement() }
    Layout(
        modifier = Modifier.fillMaxSize().safeDrawingPadding(),
        content = {
            Box(
                Modifier.pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = {
                            requested =
                                Offset(
                                    placement.position.x.toFloat(),
                                    placement.position.y.toFloat(),
                                )
                        },
                    ) { change, amount ->
                        change.consume()
                        requested = placement.clamp(placement.clamp(requested) + amount)
                    }
                },
            ) { content() }
        },
    ) { measurables, constraints ->
        val panel =
            measurables.single().measure(
                constraints.copy(
                    minWidth = 0,
                    minHeight = 0,
                    maxWidth = minOf(720.dp.roundToPx(), constraints.maxWidth),
                ),
            )
        layout(constraints.maxWidth, constraints.maxHeight) {
            // Clamp using this measurement, including minimize and live inset changes.
            placement.maximum =
                Offset(
                    (constraints.maxWidth - panel.width).coerceAtLeast(0).toFloat(),
                    (constraints.maxHeight - panel.height).coerceAtLeast(0).toFloat(),
                )
            val clamped = placement.clamp(requested)
            placement.position = IntOffset(clamped.x.roundToInt(), clamped.y.roundToInt())
            panel.place(placement.position)
        }
    }
}

// Geometry shared with drag events never feeds composition or measurement.
private class DebugPlacement {
    var position = IntOffset.Zero
    var maximum = Offset.Zero

    fun clamp(position: Offset): Offset = Offset(position.x.coerceIn(0f, maximum.x), position.y.coerceIn(0f, maximum.y))
}
