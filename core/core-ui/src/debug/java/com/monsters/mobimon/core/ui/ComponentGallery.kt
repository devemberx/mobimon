package com.monsters.mobimon.core.ui

import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

/** Design review samples use no repositories, accounts or vehicle providers. */
@Preview(name = "Components · head unit", widthDp = 1792, heightDp = 888, locale = "ko")
@Preview(name = "Components · enlarged text", widthDp = 1792, heightDp = 888, fontScale = 1.5f, locale = "ko")
@Composable
internal fun ComponentGallery() {
    MobiMonTheme {
        Surface {
            MobiMonDestination(title = "MobiMon UI", onBack = {}, onHome = {}) {
                MobiMonContentColumn {
                    MobiMonSection("Actions and status") {
                        MobiMonSourceBadge(simulated = true)
                        MobiMonPointSummary(balance = null)
                        MobiMonPointSummary(balance = 0)
                        MobiMonPointSummary(balance = null, failed = true)
                        MobiMonButton(onClick = {}) { Text("Continue") }
                        MobiMonButton(onClick = {}, style = MobiMonButtonStyle.SECONDARY) { Text("Back") }
                        MobiMonButton(onClick = {}, style = MobiMonButtonStyle.DESTRUCTIVE) { Text("Disconnect") }
                        MobiMonButton(onClick = {}, enabled = false) { Text("Unavailable") }
                    }
                    MobiMonSection("Selection and information") {
                        MobiMonTabs {
                            MobiMonTab(selected = true, onClick = {}) { Text("Friends") }
                            MobiMonTab(selected = false, onClick = {}) { Text("Backgrounds") }
                        }
                        MobiMonSelectionCard(selected = true, onClick = {}) { Text("Selected preview") }
                        MobiMonListItem(
                            supporting = { Text("Committed preference") },
                            trailing = { MobiMonStatusBadge(tone = MobiMonStatusTone.SUCCESS) { Text("Available") } },
                        ) { Text("Status") }
                    }
                    MobiMonMessage("Availability message")
                    MobiMonMessage("Retryable failure", isError = true)
                }
            }
        }
    }
}
