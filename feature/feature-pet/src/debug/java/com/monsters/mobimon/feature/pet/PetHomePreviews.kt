package com.monsters.mobimon.feature.pet

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.monsters.mobimon.core.domain.DrivingState
import com.monsters.mobimon.core.domain.PetProfile
import com.monsters.mobimon.core.domain.SignalQuality
import com.monsters.mobimon.core.domain.SignalSource
import com.monsters.mobimon.core.domain.VehicleSnapshot
import com.monsters.mobimon.core.ui.MobiMonTheme

// Include the head unit's smaller content window after system bars and AAOS app scaling.
@Preview(name = "Head unit content - simulated ready", widthDp = 1792, heightDp = 888, locale = "ko")
@Preview(name = "AAOS content excluding OS bars", widthDp = 2560, heightDp = 1268, locale = "ko")
@Preview(name = "Head unit - enlarged text", widthDp = 1792, heightDp = 888, fontScale = 1.5f, locale = "ko")
@Composable
private fun HomeReadyPreview() {
    HomePreview()
}

@Preview(name = "Profile loading", widthDp = 2560, heightDp = 1268, locale = "ko")
@Composable
private fun HomeLoadingPreview() {
    MobiMonTheme { PetHomeLoadingScreen(failed = false, onRetry = {}) }
}

@Preview(name = "Profile failure", widthDp = 2560, heightDp = 1268, locale = "ko")
@Composable
private fun HomeFailurePreview() {
    MobiMonTheme { PetHomeLoadingScreen(failed = true, onRetry = {}) }
}

@Preview(name = "Empty selection - unavailable vehicle", widthDp = 1792, heightDp = 888, locale = "ko")
@Composable
private fun HomeEmptyPreview() {
    HomePreview(empty = true)
}

@Preview(name = "Stale parking - fresh battery", widthDp = 1792, heightDp = 888, locale = "ko")
@Composable
private fun HomeStalePreview() {
    HomePreview(stale = true)
}

@Preview(name = "Head unit - unavailable vehicle", widthDp = 1792, heightDp = 888, locale = "ko")
@Composable
private fun HomeUnavailablePreview() {
    HomePreview(unavailable = true)
}

@Composable
private fun HomePreview(
    empty: Boolean = false,
    stale: Boolean = false,
    unavailable: Boolean = false,
) {
    MobiMonTheme {
        PetHomeScreen(
            profile = PetProfile("preview"),
            snapshot =
                VehicleSnapshot(
                    "preview",
                    "preview",
                    1,
                    0,
                    source = if (empty || unavailable) SignalSource.REAL else SignalSource.SIMULATED,
                    drivingState = if (empty || unavailable) DrivingState.UNKNOWN else DrivingState.PARKED,
                    quality =
                        when {
                            empty || unavailable -> SignalQuality.UNAVAILABLE
                            stale -> SignalQuality.STALE
                            else -> SignalQuality.VALID
                        },
                    batteryPercent = if (empty || unavailable) null else 72,
                    batteryQuality = if (empty || unavailable) SignalQuality.UNAVAILABLE else SignalQuality.VALID,
                    parkingAgeMillis = if (stale) 60_000 else null,
                ),
            onOpenMenu = {},
            onPetClick = {},
            friendId = if (empty) null else "friend:mobi",
            inventoryLoaded = true,
            pointBalance = if (empty) null else 0,
            pointLoadFailed = empty,
            interactionAllowed = !empty && !stale && !unavailable,
        )
    }
}
