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
                        requested += amount
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
            val x = requested.x.roundToInt().coerceIn(0, (constraints.maxWidth - panel.width).coerceAtLeast(0))
            val y = requested.y.roundToInt().coerceIn(0, (constraints.maxHeight - panel.height).coerceAtLeast(0))
            placement.position = IntOffset(x, y)
            panel.place(x, y)
        }
    }
}

// Last placement is only read by drag events; it never feeds composition or measurement.
private class DebugPlacement {
    var position = IntOffset.Zero
}
