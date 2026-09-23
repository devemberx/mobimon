package com.monsters.mobimon.feature.pet

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.monsters.mobimon.core.domain.CompanionSettings
import com.monsters.mobimon.core.ui.MobiMonTheme

@Preview(name = "Settings · wide", widthDp = 1792, heightDp = 829, locale = "ko")
@Preview(name = "Settings · enlarged text", widthDp = 1792, heightDp = 829, fontScale = 1.5f, locale = "ko")
@Composable
private fun SettingsPreview() {
    MobiMonTheme { SettingsScreen(CompanionSettings(), {}, parkedVerified = true) }
}
