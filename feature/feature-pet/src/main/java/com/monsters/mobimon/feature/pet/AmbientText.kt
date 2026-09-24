package com.monsters.mobimon.feature.pet

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monsters.mobimon.core.ui.LocalMobiMonMotionEnabled
import com.monsters.mobimon.core.ui.MobiMonColors
import com.monsters.mobimon.core.ui.companionTimePeriod
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

internal val morningPhrases =
    listOf(
        "오늘의 첫 길, 함께할게요.",
        "천천히, 오늘을 시작해봐요.",
        "아침빛 따라, 같이 가볼까요?",
        "오늘도 같이 시작해볼까요?",
        "좋은 하루의 시작, 함께할게요.",
    )

internal val dayPhrases =
    listOf(
        "오늘도 나란히 달려볼까요?",
        "가는 길, 내가 함께할게요.",
        "햇살 좋은 길, 함께 가요.",
        "조금 천천히 가도 좋아요.",
        "오늘의 길도, 함께할게요.",
    )

internal val afternoonPhrases =
    listOf(
        "잠깐, 창밖을 봐도 좋아요.",
        "느긋한 오후, 같이 가요.",
        "서두르지 않아도 괜찮아요.",
        "남은 길도, 같이 가볼까요?",
        "조금 천천히 가도 좋아요.",
    )

internal val sunsetPhrases =
    listOf(
        "오늘도 수고 많았어요.",
        "이제 조금, 쉬어가도 좋아요.",
        "집으로 가는 길, 함께할게요.",
        "오늘 하루도 거의 다 왔어요.",
        "돌아가는 길, 천천히 가요.",
    )

internal val nightPhrases =
    listOf(
        "이 밤도, 천천히 가요.",
        "고요한 길도, 함께할게요.",
        "오늘의 마지막 길까지 함께할게요.",
        "좋은 길엔, 늘 네가 있어요.",
        "오늘도 함께해서 좋았어요.",
    )

internal fun ambientPhrasesForPeriod(period: String): List<String> =
    when (period) {
        "morning" -> morningPhrases
        "day" -> dayPhrases
        "afternoon" -> afternoonPhrases
        "sunset" -> sunsetPhrases
        else -> nightPhrases
    }

internal fun getNextAmbientPhrase(
    period: String,
    lastPhrase: String?,
): String {
    val phrases = ambientPhrasesForPeriod(period)
    val candidates =
        if (lastPhrase != null && phrases.size > 1) {
            phrases.filter { it != lastPhrase }
        } else {
            phrases
        }
    return candidates.random()
}

internal data class AmbientTiming(
    val fadeInMs: Int,
    val visibleMs: Int,
    val fadeOutMs: Int,
    val emptyMs: Int,
)

internal fun getAmbientTiming(period: String): AmbientTiming =
    when (period) {
        "morning" -> AmbientTiming(fadeInMs = 1600, visibleMs = 7500, fadeOutMs = 1800, emptyMs = 840)
        "day" -> AmbientTiming(fadeInMs = 1500, visibleMs = 8000, fadeOutMs = 1800, emptyMs = 900)
        "afternoon" -> AmbientTiming(fadeInMs = 1600, visibleMs = 8000, fadeOutMs = 1800, emptyMs = 900)
        "sunset" -> AmbientTiming(fadeInMs = 1800, visibleMs = 8000, fadeOutMs = 2000, emptyMs = 960)
        else -> AmbientTiming(fadeInMs = 1800, visibleMs = 8500, fadeOutMs = 2000, emptyMs = 1050)
    }

/**
 * Displays an ambient time-of-day phrase that gently fades in and out.
 */
@Composable
fun AmbientTextHeader(
    backgroundTimeOfDay: String?,
    modifier: Modifier = Modifier,
    scale: Float = 1f,
) {
    val period = companionTimePeriod(backgroundTimeOfDay)
    var currentPhrase by remember { mutableStateOf<String?>(null) }
    var lastPhrase by remember { mutableStateOf<String?>(null) }

    val alphaAnim = remember { Animatable(0f) }
    val offsetYAnim = remember { Animatable(0f) }

    val isMotionEnabled = LocalMobiMonMotionEnabled.current

    LaunchedEffect(period, isMotionEnabled) {
        alphaAnim.snapTo(0f)
        offsetYAnim.snapTo(0f)
        currentPhrase = null

        if (!isMotionEnabled) {
            val phrase = getNextAmbientPhrase(period, lastPhrase)
            currentPhrase = phrase
            lastPhrase = phrase
            alphaAnim.snapTo(1f)
            offsetYAnim.snapTo(0f)
            return@LaunchedEffect
        }

        while (isActive) {
            val phrase = getNextAmbientPhrase(period, lastPhrase)
            currentPhrase = phrase
            lastPhrase = phrase

            val timing = getAmbientTiming(period)

            // 1. Fade In with subtle vertical motion (+5dp -> 0dp)
            offsetYAnim.snapTo(5f)
            val fadeInAlphaJob =
                launch {
                    alphaAnim.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(durationMillis = timing.fadeInMs, easing = LinearOutSlowInEasing),
                    )
                }
            val fadeInOffsetJob =
                launch {
                    offsetYAnim.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(durationMillis = timing.fadeInMs, easing = LinearOutSlowInEasing),
                    )
                }
            fadeInAlphaJob.join()
            fadeInOffsetJob.join()

            // 2. Visible Interval
            delay(timing.visibleMs.toLong())

            // 3. Fade Out with subtle vertical motion (0dp -> -3dp)
            val fadeOutAlphaJob =
                launch {
                    alphaAnim.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(durationMillis = timing.fadeOutMs, easing = FastOutLinearInEasing),
                    )
                }
            val fadeOutOffsetJob =
                launch {
                    offsetYAnim.animateTo(
                        targetValue = -3f,
                        animationSpec = tween(durationMillis = timing.fadeOutMs, easing = FastOutLinearInEasing),
                    )
                }
            fadeOutAlphaJob.join()
            fadeOutOffsetJob.join()

            // 4. Empty Interval (no text displayed)
            currentPhrase = null
            delay(timing.emptyMs.toLong())
        }
    }

    Box(
        modifier =
            modifier
                .heightIn(min = (48f * scale).coerceAtLeast(48f).dp)
                .testTag("home-ambient-text-container"),
        contentAlignment = Alignment.Center,
    ) {
        val phrase = currentPhrase
        if (phrase != null) {
            Text(
                text = phrase,
                style =
                    MaterialTheme.typography.titleLarge.copy(
                        fontSize = (32.4f * scale).coerceAtLeast(24f).sp,
                        lineHeight = (43.2f * scale).coerceAtLeast(34f).sp,
                        fontWeight = FontWeight.Medium,
                        shadow =
                            Shadow(
                                color = Color(0x88000000),
                                offset = Offset(0f, 2f),
                                blurRadius = 4f,
                            ),
                    ),
                color = MobiMonColors.muted,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier =
                    Modifier
                        .offset(y = offsetYAnim.value.dp)
                        .alpha(alphaAnim.value)
                        .testTag("home-ambient-text"),
            )
        }
    }
}
