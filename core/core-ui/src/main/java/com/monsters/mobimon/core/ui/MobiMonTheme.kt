package com.monsters.mobimon.core.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val CompanionColors =
    lightColorScheme(
        primary = Color(0xFF536B4F),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFE0E8D7),
        onPrimaryContainer = Color(0xFF30422D),
        secondary = Color(0xFF78624A),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFF0E4CE),
        onSecondaryContainer = Color(0xFF51402C),
        background = Color(0xFFFAF2E6),
        onBackground = Color(0xFF514737),
        surface = Color(0xFFFFFBF3),
        onSurface = Color(0xFF514737),
        surfaceVariant = Color(0xFFF0E8DA),
        onSurfaceVariant = Color(0xFF665D50),
        outline = Color(0xFF817665),
        outlineVariant = Color(0xFFE5DAC7),
    )

private val VehicleColors =
    darkColorScheme(
        primary = Color(0xFFBBCFB0),
        onPrimary = Color(0xFF263A24),
        primaryContainer = Color(0xFF455441),
        onPrimaryContainer = Color(0xFFE0ECD6),
        secondary = Color(0xFFDCC6A4),
        onSecondary = Color(0xFF3E3324),
        secondaryContainer = Color(0xFF574D3D),
        onSecondaryContainer = Color(0xFFF1E4CB),
        background = Color(0xFF152830),
        onBackground = Color(0xFFE6E9DF),
        surface = Color(0xFF263941),
        onSurface = Color(0xFFE6E9DF),
        surfaceVariant = Color(0xFF34484C),
        onSurfaceVariant = Color(0xFFD1D6CA),
        outline = Color(0xFFA3AD9C),
        outlineVariant = Color(0xFF5C675B),
    )

private val CarTypography =
    Typography(
        headlineMedium = TextStyle(fontSize = 32.sp, lineHeight = 40.sp, fontWeight = FontWeight.SemiBold),
        titleLarge = TextStyle(fontSize = 28.sp, lineHeight = 36.sp, fontWeight = FontWeight.SemiBold),
        titleMedium = TextStyle(fontSize = 24.sp, lineHeight = 32.sp, fontWeight = FontWeight.Medium),
        bodyLarge = TextStyle(fontSize = 24.sp, lineHeight = 34.sp),
        bodyMedium = TextStyle(fontSize = 24.sp, lineHeight = 34.sp),
        labelLarge = TextStyle(fontSize = 22.sp, lineHeight = 30.sp, fontWeight = FontWeight.Medium),
    )

/** Applies paired companion or in-app vehicle-preview colors to [content]. */
@Composable
fun MobiMonTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) VehicleColors else CompanionColors,
        typography = CarTypography,
        content = content,
    )
}
