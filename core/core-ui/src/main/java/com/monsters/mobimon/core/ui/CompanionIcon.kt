package com.monsters.mobimon.core.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.scale

enum class CompanionIcon { BACK, CLOSE }

@Composable
fun CompanionIcon(
    icon: CompanionIcon,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier) {
        scale(size.width / 48f, size.height / 48f, pivot = Offset.Zero) {
            fun line(
                x: Float,
                y: Float,
                endX: Float,
                endY: Float,
            ) = drawLine(tint, Offset(x, y), Offset(endX, endY), 3f, StrokeCap.Round)
            when (icon) {
                CompanionIcon.BACK -> {
                    line(29f, 10f, 15f, 24f)
                    line(15f, 24f, 29f, 38f)
                }
                CompanionIcon.CLOSE -> {
                    line(12f, 12f, 36f, 36f)
                    line(36f, 12f, 12f, 36f)
                }
            }
        }
    }
}
