package com.monsters.mobimon.core.ui

import androidx.compose.material3.darkColorScheme

/** V4 shared UI roles. Screens adopt this palette when their Figma migration is reviewed. */
val MobiMonTwilightColors =
    darkColorScheme(
        primary = MobiMonConnectionColors.button,
        onPrimary = MobiMonConnectionColors.onButton,
        primaryContainer = MobiMonConnectionColors.raised,
        onPrimaryContainer = MobiMonConnectionColors.text,
        secondary = MobiMonConnectionColors.accent,
        onSecondary = MobiMonConnectionColors.onButton,
        secondaryContainer = MobiMonConnectionColors.raised,
        onSecondaryContainer = MobiMonConnectionColors.text,
        tertiary = MobiMonConnectionColors.success,
        onTertiary = MobiMonConnectionColors.onButton,
        tertiaryContainer = MobiMonConnectionColors.raised,
        onTertiaryContainer = MobiMonConnectionColors.success,
        background = MobiMonConnectionColors.background,
        onBackground = MobiMonConnectionColors.text,
        surface = MobiMonConnectionColors.panel,
        onSurface = MobiMonConnectionColors.text,
        surfaceVariant = MobiMonConnectionColors.raised,
        onSurfaceVariant = MobiMonConnectionColors.muted,
        outline = MobiMonConnectionColors.border,
        outlineVariant = MobiMonConnectionColors.border,
        error = MobiMonConnectionColors.destructive,
        onError = MobiMonConnectionColors.onButton,
        errorContainer = MobiMonConnectionColors.raised,
        onErrorContainer = MobiMonConnectionColors.destructive,
        inverseSurface = MobiMonConnectionColors.button,
        inverseOnSurface = MobiMonConnectionColors.onButton,
        inversePrimary = MobiMonConnectionColors.onButton,
        surfaceTint = MobiMonConnectionColors.accent,
    )
