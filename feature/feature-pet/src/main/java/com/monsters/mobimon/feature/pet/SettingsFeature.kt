package com.monsters.mobimon.feature.pet

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.monsters.mobimon.core.domain.SettingsRepository
import com.monsters.mobimon.core.domain.SignalSource
import com.monsters.mobimon.core.navigation.AiRoute
import com.monsters.mobimon.core.navigation.AppRoute
import com.monsters.mobimon.core.navigation.CompanionRoute
import com.monsters.mobimon.core.navigation.FeatureEntry
import com.monsters.mobimon.core.navigation.FeatureNavigator
import com.monsters.mobimon.core.presentation.VehiclePresentation
import com.monsters.mobimon.core.presentation.parkedVerified

class SettingsFeature(
    private val settings: SettingsRepository,
    private val vehicle: VehiclePresentation,
    private val debugSettingsAvailable: Boolean = false,
) : FeatureEntry {
    override val routes = setOf(CompanionRoute.SETTINGS)

    @Composable
    override fun Content(
        route: AppRoute,
        navigator: FeatureNavigator,
        modifier: Modifier,
    ) {
        require(route in routes)
        val factory = remember(this) { viewModelFactory { initializer { SettingsViewModel(settings) } } }
        val model: SettingsViewModel = viewModel(factory = factory)
        val state by model.state.collectAsStateWithLifecycle()
        val snapshot = vehicle.snapshot()
        val context = LocalContext.current
        SettingsScreen(
            settings = state.settings,
            onReducedMotionChange = { if (snapshot.parkedVerified) model.setReducedMotion(it) },
            modifier = modifier,
            onDebugModeChange = { if (snapshot.parkedVerified) model.setDebugMode(it) },
            debugModeAvailable = debugSettingsAvailable,
            onLauncherCharacterChange = { enabled ->
                if (snapshot.parkedVerified) {
                    if (enabled && !Settings.canDrawOverlays(context)) {
                        try {
                            val intent =
                                Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:${context.packageName}"),
                                ).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            try {
                                val fallback =
                                    Intent(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        Uri.parse("package:${context.packageName}"),
                                    ).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                context.startActivity(fallback)
                            } catch (_: Exception) {
                            }
                        }
                    }
                    model.setLauncherCharacter(enabled)
                }
            },
            hasOverlayPermission = Settings.canDrawOverlays(context),
            launcherSaving = state.launcherSaving,
            launcherError = if (state.launcherSaveFailed) stringResource(R.string.pet_route_save_failed) else null,
            motionSaving = state.reducedMotionSaving,
            motionError = if (state.reducedMotionSaveFailed) stringResource(R.string.pet_route_save_failed) else null,
            debugSaving = state.debugModeSaving,
            debugError = if (state.debugModeSaveFailed) stringResource(R.string.pet_route_save_failed) else null,
            settingsAvailable = state.loaded,
            settingsLoadFailed = state.loadFailed,
            onRetry = model::retry,
            onBack = navigator.back,
            onDone = navigator.returnHome,
            parkedVerified = snapshot.parkedVerified,
            simulatedVehicle = snapshot.source == SignalSource.SIMULATED,
            onOpenCopilot = { if (snapshot.parkedVerified) navigator.navigate(AiRoute.COPILOT) },
        )
    }
}
