package com.monsters.mobimon.core.ui

import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextMotion
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Native text positioned by the export's baseline, inside a reference-sized parent. */
@Composable
fun MobiMonReferenceText(
    text: String,
    x: Float,
    baseline: Float,
    size: Float,
    modifier: Modifier = Modifier,
    scale: Float = 1f,
    bold: Boolean = false,
    color: Color = MobiMonColors.text,
) {
    Text(
        text,
        Modifier
            .offset((x * scale).dp, ((baseline - size * 1.4f) * scale).dp)
            .then(modifier)
            .paddingFromBaseline(top = (size * 1.4f * scale).dp),
        style = mobiMonReferenceTextStyle(size, scale, bold),
        color = color,
    )
}

fun mobiMonReferenceTextStyle(
    size: Float,
    scale: Float = 1f,
    bold: Boolean = false,
) = TextStyle(
    fontFamily = MobiMonFontFamily,
    fontSize = (size * scale).sp,
    lineHeight = (size * 1.4f * scale).sp,
    letterSpacing = 0.sp,
    fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
    textMotion = TextMotion.Animated,
    platformStyle = PlatformTextStyle(includeFontPadding = false),
)
