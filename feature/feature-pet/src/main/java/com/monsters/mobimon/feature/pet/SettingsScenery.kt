package com.monsters.mobimon.feature.pet

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.scale
import com.monsters.mobimon.core.ui.MobiMonSettingsColors

@Composable
internal fun SettingsScenery(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        drawRect(Brush.verticalGradient(listOf(MobiMonSettingsColors.sky, MobiMonSettingsColors.ground)))
        scale(size.width / 2560f, size.height / 1440f, pivot = Offset.Zero) {
            drawPath(
                Path().apply {
                    moveTo(0f, 1080f)
                    cubicTo(420f, 920f, 750f, 1100f, 1100f, 1180f)
                    cubicTo(1500f, 1400f, 1730f, 800f, 2160f, 940f)
                    cubicTo(2340f, 970f, 2480f, 1030f, 2560f, 1080f)
                    lineTo(2560f, 1440f)
                    lineTo(0f, 1440f)
                    close()
                },
                MobiMonSettingsColors.hill,
            )
        }
    }
}
