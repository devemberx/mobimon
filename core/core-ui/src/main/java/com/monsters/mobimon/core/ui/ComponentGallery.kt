package com.monsters.mobimon.core.ui

import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

/** Design review samples use no repositories, accounts or vehicle providers. */
@Preview(name = "Components · head unit", widthDp = 1280, heightDp = 720, locale = "ko")
@Preview(name = "Components · enlarged text", widthDp = 800, heightDp = 900, fontScale = 1.5f, locale = "ko")
@Composable
internal fun ComponentGallery() {
    MobiMonTheme(colorScheme = MobiMonTwilightColors) {
        Surface {
            MobiMonDestination(title = "MobiMon UI", onBack = {}, onHome = {}) {
                MobiMonContentColumn {
                    MobiMonSection("Actions and status") {
                        MobiMonSourceBadge(simulated = true)
                        MobiMonPointSummary(balance = null)
                        MobiMonPointSummary(balance = 0)
                        MobiMonPointSummary(balance = null, failed = true)
                        MobiMonButton(onClick = {}) { Text("Continue") }
                        MobiMonButton(onClick = {}, enabled = false) { Text("Unavailable") }
                    }
                    MobiMonMessage("Availability message")
                    MobiMonMessage("Retryable failure", isError = true)
                }
            }
        }
    }
}
