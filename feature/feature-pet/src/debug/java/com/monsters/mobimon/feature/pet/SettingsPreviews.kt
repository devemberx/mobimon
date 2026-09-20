package com.monsters.mobimon.feature.pet

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.monsters.mobimon.core.domain.CompanionSettings
import com.monsters.mobimon.core.ui.MobiMonTheme

@Preview(name = "Settings · wide", widthDp = 1792, heightDp = 888, locale = "ko")
@Preview(name = "Settings · enlarged text", widthDp = 800, heightDp = 900, fontScale = 1.5f, locale = "ko")
@Composable
private fun SettingsPreview() {
    MobiMonTheme { SettingsScreen(CompanionSettings(), {}, parkedVerified = true) }
}
