package com.monsters.mobimon.core.ui

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val CarTypography =
    Typography(
        displayLarge =
            TextStyle(
                fontFamily = MobiMonFontFamily,
                fontSize = 72.sp,
                lineHeight = 96.sp,
                fontWeight = FontWeight.Bold,
            ),
        displayMedium =
            TextStyle(
                fontFamily = MobiMonFontFamily,
                fontSize = 56.sp,
                lineHeight = 72.sp,
                fontWeight = FontWeight.Bold,
            ),
        displaySmall =
            TextStyle(
                fontFamily = MobiMonFontFamily,
                fontSize = 48.sp,
                lineHeight = 64.sp,
                fontWeight = FontWeight.Bold,
            ),
        headlineLarge =
            TextStyle(
                fontFamily = MobiMonFontFamily,
                fontSize = 40.sp,
                lineHeight = 56.sp,
                fontWeight = FontWeight.Bold,
            ),
        headlineSmall =
            TextStyle(
                fontFamily = MobiMonFontFamily,
                fontSize = 28.sp,
                lineHeight = 40.sp,
                fontWeight = FontWeight.Bold,
            ),
        titleSmall =
            TextStyle(
                fontFamily = MobiMonFontFamily,
                fontSize = 24.sp,
                lineHeight = 36.sp,
                fontWeight = FontWeight.Bold,
            ),
        bodySmall =
            TextStyle(
                fontFamily = MobiMonFontFamily,
                fontSize = 24.sp,
                lineHeight = 36.sp,
            ),
        labelMedium = TextStyle(fontFamily = MobiMonFontFamily, fontSize = 24.sp, lineHeight = 36.sp),
        labelSmall = TextStyle(fontFamily = MobiMonFontFamily, fontSize = 24.sp, lineHeight = 36.sp),
        headlineMedium =
            TextStyle(
                fontFamily = MobiMonFontFamily,
                fontSize = 32.sp,
                lineHeight = 40.sp,
                fontWeight = FontWeight.SemiBold,
            ),
        titleLarge =
            TextStyle(
                fontFamily = MobiMonFontFamily,
                fontSize = 28.sp,
                lineHeight = 36.sp,
                fontWeight = FontWeight.SemiBold,
            ),
        titleMedium =
            TextStyle(
                fontFamily = MobiMonFontFamily,
                fontSize = 24.sp,
                lineHeight = 32.sp,
                fontWeight = FontWeight.Medium,
            ),
        bodyLarge = TextStyle(fontFamily = MobiMonFontFamily, fontSize = 24.sp, lineHeight = 34.sp),
        bodyMedium = TextStyle(fontFamily = MobiMonFontFamily, fontSize = 24.sp, lineHeight = 34.sp),
        labelLarge =
            TextStyle(
                fontFamily = MobiMonFontFamily,
                fontSize = 22.sp,
                lineHeight = 30.sp,
                fontWeight = FontWeight.Medium,
            ),
    )

/** Applies the fixed vehicle-display palette, independent of the system theme. */
@Composable
fun MobiMonTheme(
    colorScheme: ColorScheme = MobiMonTwilightColors,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = colorScheme,
        typography = CarTypography,
        content = content,
    )
}
