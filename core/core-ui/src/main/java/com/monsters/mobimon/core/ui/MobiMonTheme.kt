package com.monsters.mobimon.core.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** Fixed Home foreground roles from the letterbox design. */
object MobiMonHomeColors {
    val brand = Color(0xFFF2F1E9)
    val parking = Color(0xFFC9DACC)
    val balance = Color(0xFFEEE5CC)
    val customization = Color(0xFFEDF2EA)
    val vehicleSummary = Color(0xFFE3EBE6)
    val divider = Color(0xFF688386)
}

/** Fixed settings and menu roles measured from the P05/P02 Figma frames. */
object MobiMonSettingsColors {
    val sky = Color(0xFF1C2C43)
    val ground = Color(0xFF35524F)
    val hill = Color(0xFF456260)
    val row = Color(0xFF1B2E3D)
    val menu = Color(0xFF223B4C)
    val menuControlBorder = Color(0xFF748F9A)
    val border = Color(0xFF708C99)
    val text = Color(0xFFEDF2EA)
    val muted = Color(0xFFC0CFCD)
    val selected = Color(0xFF526B5B)
    val selectedThumb = Color(0xFFC3DDC7)
}

/** Colors exported from the Copilot v4 Figma frames. */
object MobiMonConnectionColors {
    val background = Color(0xFF091525)
    val panel = Color(0xFF142A42)
    val raised = Color(0xFF203C58)
    val text = Color(0xFFF4F7FC)
    val muted = Color(0xFFB9CADD)
    val accent = Color(0xFF87DAF5)
    val success = Color(0xFF71E5C5)
    val warning = Color(0xFFFFD18A)
    val destructive = Color(0xFFFFA79F)
    val border = Color(0xFF64839F)
    val button = Color(0xFFF7F2E8)
    val onButton = Color(0xFF10243A)
}

private val VehicleColors =
    darkColorScheme(
        primary = Color(0xFFF2EFE5),
        onPrimary = Color(0xFF253443),
        primaryContainer = Color(0xFF29484B),
        onPrimaryContainer = Color(0xFFEEF2EA),
        secondary = Color(0xFFB9DDC6),
        onSecondary = Color(0xFF253443),
        secondaryContainer = Color(0xFF2B4249),
        onSecondaryContainer = Color(0xFFD7E3DC),
        tertiary = Color(0xFF89A492),
        onTertiary = Color(0xFF111F32),
        tertiaryContainer = Color(0xFF415F5E),
        onTertiaryContainer = Color(0xFFEEF2EA),
        background = Color(0xFF111F32),
        onBackground = Color(0xFFEEF2EA),
        surface = Color(0xFF173144),
        onSurface = Color(0xFFEEF2EA),
        surfaceVariant = Color(0xFF294353),
        onSurfaceVariant = Color(0xFFD3E3DF),
        outline = Color(0xFFA8C0C6),
        outlineVariant = Color(0xFF5D7885),
    )

private val CarTypography =
    Typography(
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
fun MobiMonTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = VehicleColors,
        typography = CarTypography,
        content = content,
    )
}
