package com.monsters.mobimon.feature.pet

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monsters.mobimon.core.ui.LocalMobiMonMotionEnabled
import com.monsters.mobimon.core.ui.MobiMonColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.random.Random

/** Pop in on entry or pet tap, then reappear periodically without moving the scene. */
@Composable
internal fun HomeSpeechBubble(
    modifier: Modifier = Modifier,
    scale: Float = 1f,
    triggerKey: Int = 0,
) {
    val motionEnabled = LocalMobiMonMotionEnabled.current
    val textScale = LocalDensity.current.fontScale.coerceAtLeast(1f)
    val fontSize = (32.4f * scale).coerceAtLeast(24f)
    val lineHeight = (43.2f * scale).coerceAtLeast(34f)
    // The artwork must stop shrinking when either minimum text dimension is reached.
    val bubbleScale = maxOf(fontSize / 32.4f, lineHeight / 43.2f) * textScale
    var visible by remember { mutableStateOf(!motionEnabled) }
    val progress by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = if (motionEnabled) spring(dampingRatio = 0.75f, stiffness = 400f) else snap(),
        label = "home_speech_bubble_entrance",
    )
    LaunchedEffect(triggerKey, motionEnabled) {
        if (!motionEnabled) {
            visible = true
            return@LaunchedEffect
        }
        while (isActive) {
            if (triggerKey > 0) {
                visible = false
                delay(16L)
            }
            visible = true
            delay(4500L)
            visible = false
            delay(Random.nextLong(30_000L, 60_000L))
        }
    }
    Box(
        modifier
            .width((324 * bubbleScale).dp)
            .heightIn(min = (174.6f * bubbleScale).dp)
            .testTag("home-companion-message")
            .graphicsLayer {
                val fraction = if (motionEnabled) progress else 1f
                alpha = (fraction * 2).coerceIn(0f, 1f)
                scaleX = 0.82f + 0.18f * fraction
                scaleY = scaleX
                transformOrigin = TransformOrigin(0.08f, 1f)
            },
    ) {
        Image(
            painterResource(R.drawable.pet_speech_bubble),
            null,
            Modifier.matchParentSize(),
            contentScale = ContentScale.FillBounds,
        )
        Text(
            stringResource(R.string.pet_home_message),
            modifier =
                Modifier.padding(
                    start = (58.5f * bubbleScale).dp,
                    end = (36 * bubbleScale).dp,
                    top = (26 * bubbleScale).dp,
                    bottom = (46 * bubbleScale).dp,
                ),
            style =
                MaterialTheme.typography.bodyLarge.copy(
                    fontSize = fontSize.sp,
                    lineHeight = lineHeight.sp,
                    letterSpacing = (0.2f * scale).sp,
                    fontWeight = FontWeight.Normal,
                ),
            color = MobiMonColors.onButton,
        )
    }
}
