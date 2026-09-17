package com.monsters.mobimon.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.monsters.mobimon.core.domain.CosmeticSlot
import com.monsters.mobimon.core.domain.PetRepository
import com.monsters.mobimon.core.domain.PointEconomy
import com.monsters.mobimon.core.domain.SettingsRepository
import com.monsters.mobimon.core.domain.SignalSource
import com.monsters.mobimon.core.navigation.AiRoute
import com.monsters.mobimon.core.navigation.AppRoute
import com.monsters.mobimon.core.navigation.FeatureEntry
import com.monsters.mobimon.core.navigation.FeatureNavigator
import com.monsters.mobimon.core.presentation.VehiclePresentation
import com.monsters.mobimon.core.presentation.parkedVerified
import com.monsters.mobimon.core.ui.MobiMonButton
import com.monsters.mobimon.core.ui.MobiMonContentColumn
import com.monsters.mobimon.core.ui.MobiMonDestination
import com.monsters.mobimon.core.ui.MobiMonDimensions
import com.monsters.mobimon.core.ui.MobiMonMessage

/** AI-owned routes. A real connection adapter is deliberately not inferred from UI state. */
class AiFeature(
    private val pets: PetRepository,
    private val settings: SettingsRepository,
    private val points: PointEconomy,
    private val vehicle: VehiclePresentation,
) : FeatureEntry {
    override val routes = setOf(AiRoute.COPILOT, AiRoute.CONVERSATION)

    @Composable
    override fun Content(
        route: AppRoute,
        navigator: FeatureNavigator,
        modifier: Modifier,
    ) {
        val snapshot = vehicle.snapshot()
        if (route == AiRoute.CONVERSATION) {
            MobiMonDestination(
                stringResource(R.string.conversation_title),
                navigator.back,
                navigator.returnHome,
                modifier,
            ) {
                MobiMonContentColumn {
                    MobiMonMessage(stringResource(R.string.conversation_not_connected))
                    MobiMonButton(
                        onClick = { navigator.navigate(AiRoute.COPILOT) },
                        enabled = snapshot.parkedVerified,
                    ) { Text(stringResource(R.string.conversation_connect)) }
                }
            }
            return
        }
        val factory =
            remember(this) { viewModelFactory { initializer { AiCompanionViewModel(pets, settings, points) } } }
        val model: AiCompanionViewModel = viewModel(factory = factory)
        val companion by model.state.collectAsStateWithLifecycle()
        val profile = companion.profile
        if (profile == null) {
            MobiMonDestination(stringResource(R.string.copilot_title), navigator.back, navigator.returnHome, modifier) {
                MobiMonContentColumn {
                    MobiMonMessage(
                        stringResource(
                            if (companion.failed) R.string.ai_context_failed else R.string.ai_context_loading,
                        ),
                        isError = companion.failed,
                    )
                    if (companion.failed) {
                        MobiMonButton(
                            onClick = model::retry,
                        ) { Text(stringResource(R.string.ai_retry)) }
                    }
                }
            }
            return
        }
        Column(modifier.fillMaxSize()) {
            if (companion.failed) {
                Row(
                    Modifier.fillMaxWidth().padding(MobiMonDimensions.contentPadding),
                    horizontalArrangement = Arrangement.spacedBy(MobiMonDimensions.contentGap),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    MobiMonMessage(
                        stringResource(R.string.ai_context_update_failed),
                        Modifier.weight(1f),
                        isError = true,
                    )
                    MobiMonButton(onClick = model::retry) { Text(stringResource(R.string.ai_retry)) }
                }
            }
            CopilotConnectionScreen(
                state = CopilotUiState.Introduction(connectionUnavailable = true),
                onAction = { action ->
                    when (action) {
                        CopilotAction.BACK, CopilotAction.CANCEL -> navigator.back()
                        else -> Unit
                    }
                },
                modifier = Modifier.weight(1f),
                interactionAllowed = snapshot.parkedVerified,
                simulatedVehicle = snapshot.source == SignalSource.SIMULATED,
                friendId = companion.inventory?.equippedItemIds?.get(CosmeticSlot.FRIEND) ?: "friend:mobi",
                appearanceKey = profile.appearance.name,
                accessoryId = companion.inventory?.equippedItemIds?.get(CosmeticSlot.ACCESSORY),
                outfitId = companion.inventory?.equippedItemIds?.get(CosmeticSlot.OUTFIT),
                backgroundId = companion.inventory?.equippedItemIds?.get(CosmeticSlot.BACKGROUND),
            )
        }
    }
}
