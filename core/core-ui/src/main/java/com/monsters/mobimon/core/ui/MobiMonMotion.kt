package com.monsters.mobimon.core.ui

import androidx.compose.runtime.staticCompositionLocalOf

/** Host-owned preference for decorative motion; renderers keep their approved static artwork when disabled. */
val LocalMobiMonMotionEnabled = staticCompositionLocalOf { true }
