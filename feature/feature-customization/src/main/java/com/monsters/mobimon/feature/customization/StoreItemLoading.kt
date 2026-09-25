package com.monsters.mobimon.feature.customization

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
internal fun storeCardRevealModifier(itemId: String): Modifier {
    var visible by remember(itemId) { mutableStateOf(false) }
    LaunchedEffect(itemId) { visible = true }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 180),
        label = "storeCardAlpha",
    )
    return Modifier.graphicsLayer {
        this.alpha = alpha
        translationY = (1f - alpha) * 8.dp.toPx()
    }
}

@Composable
internal fun StorePendingCard(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.extraLarge,
) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier
            .testTag("store-item-placeholder")
            .clip(shape)
            .background(colors.surface)
            .border(1.dp, colors.outline, shape)
            .padding(24.dp),
    ) {
        Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
            Box(Modifier.size(88.dp).background(colors.surfaceVariant, CircleShape))
        }
        Spacer(Modifier.height(16.dp))
        Box(Modifier.fillMaxWidth(0.45f).height(20.dp).background(colors.surfaceVariant, CircleShape))
        Spacer(Modifier.height(12.dp))
        Box(Modifier.fillMaxWidth(0.28f).height(16.dp).background(colors.surfaceVariant, CircleShape))
    }
}
