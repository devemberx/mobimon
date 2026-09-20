package com.monsters.mobimon.feature.quest

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.monsters.mobimon.core.ui.MobiMonFontFamily

internal fun questTextStyle(
    baseSp: Float,
    scale: Float,
    bold: Boolean = false,
    color: Color,
): TextStyle =
    TextStyle(
        fontFamily = MobiMonFontFamily,
        fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
        fontSize = (baseSp * scale).sp,
        color = color,
    )
