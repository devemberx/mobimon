package com.monsters.mobimon.feature.pet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.monsters.mobimon.core.domain.PetRepository
import com.monsters.mobimon.core.domain.PointEconomy
import com.monsters.mobimon.core.domain.QuestRepository
import com.monsters.mobimon.core.domain.SettingsRepository
import com.monsters.mobimon.core.domain.SignalSource
import com.monsters.mobimon.core.navigation.AiRoute
import com.monsters.mobimon.core.navigation.AppRoute
import com.monsters.mobimon.core.navigation.CompanionRoute
import com.monsters.mobimon.core.navigation.FeatureEntry
import com.monsters.mobimon.core.navigation.FeatureNavigator
import com.monsters.mobimon.core.navigation.QuestRoute
import com.monsters.mobimon.core.navigation.VehicleRoute
import com.monsters.mobimon.core.presentation.PointBalanceState
import com.monsters.mobimon.core.presentation.PointPresentation
import com.monsters.mobimon.core.presentation.VehiclePresentation
import com.monsters.mobimon.core.presentation.parkedVerified
import com.monsters.mobimon.core.ui.MobiMonDestination

class PetFeature(
    private val pets: PetRepository,
    private val settings: SettingsRepository,
    private val quests: QuestRepository,
    private val points: PointEconomy,
    private val wallet: PointPresentation,
    private val vehicle: VehiclePresentation,
) : FeatureEntry {
    override val routes = setOf(CompanionRoute.HOME, CompanionRoute.SETTINGS, CompanionRoute.APPEARANCE)

    @Composable
    override fun Content(
        route: AppRoute,
        navigator: FeatureNavigator,
        modifier: Modifier,
    ) {
        val factory =
            remember(this) {
                viewModelFactory {
                    initializer { PetViewModel(pets, settings) }
                    initializer { CosmeticInventoryViewModel(points) }
                    initializer { HomeQuestSummaryViewModel(quests) }
                }
            }
        val petModel: PetViewModel = viewModel(factory = factory)
        val inventoryModel: CosmeticInventoryViewModel = viewModel(factory = factory)
        val summaryModel: HomeQuestSummaryViewModel = viewModel(factory = factory)
        val pointModel = wallet.model()
        val petState by petModel.state.collectAsStateWithLifecycle()
        val inventoryState by inventoryModel.state.collectAsStateWithLifecycle()
        val progress by summaryModel.state.collectAsStateWithLifecycle()
        val pointBalance by pointModel.state.collectAsStateWithLifecycle()
        val vehicleSnapshot = vehicle.snapshot()
        val profile = petState.profile
        val onRetry = {
            petModel.retry()
            inventoryModel.retry()
            pointModel.retry()
        }
        if (profile == null || petState.isLoading) {
            PetHomeLoadingScreen(failed = petState.loadFailed, onRetry = onRetry, modifier = modifier)
            return
        }
        val interactionAllowed = vehicleSnapshot.parkedVerified
        val balance = (pointBalance as? PointBalanceState.Ready)?.balance
        val balanceFailed = pointBalance == PointBalanceState.Failed
        val legacyQuestVisible = vehicleSnapshot.source == SignalSource.SIMULATED
        val motionSaveError =
            if (petState.reducedMotionSaveFailed) {
                stringResource(
                    R.string.pet_route_save_failed,
                )
            } else {
                null
            }
        when (route) {
            CompanionRoute.HOME -> {
                PetHomeScreen(
                    profile,
                    vehicleSnapshot,
                    progress,
                    modifier = modifier,
                    onOpenMenu = navigator.openMenu,
                    onOpenVehicleInfo = { navigator.navigate(VehicleRoute.VEHICLE_INFO) },
                    onOpenQuests = { navigator.navigate(QuestRoute.QUESTS) },
                    onPetClick = { if (interactionAllowed) navigator.navigate(AiRoute.COPILOT) },
                    onOpenAppearance = { navigator.navigate(CompanionRoute.APPEARANCE) },
                    pointBalance = balance,
                    pointLoadFailed = balanceFailed,
                    legacyQuestVisible = legacyQuestVisible,
                    friendId = inventoryState.equippedFriendId,
                    accessoryId = inventoryState.equippedAccessoryId,
                    interactionAllowed = interactionAllowed,
                    connectionAvailable = true,
                    profileObservationFailed = petState.loadFailed,
                    onRetryProfile = onRetry,
                    inventoryLoaded = inventoryState.inventory != null,
                    inventoryLoadFailed = inventoryState.loadFailed,
                )
            }
            CompanionRoute.SETTINGS -> {
                SettingsScreen(
                    petState.settings,
                    petModel::setReducedMotion,
                    modifier = modifier,
                    motionSaving = petState.reducedMotionSaving,
                    motionError = motionSaveError,
                    settingsAvailable = petState.settingsLoaded,
                    settingsLoadFailed = petState.settingsLoadFailed,
                    onRetry = onRetry,
                    onBack = navigator.back,
                    onDone = navigator.returnHome,
                    parkedVerified = interactionAllowed,
                    simulatedVehicle = vehicleSnapshot.source == SignalSource.SIMULATED,
                    onOpenCopilot = { if (interactionAllowed) navigator.navigate(AiRoute.COPILOT) },
                )
            }
            CompanionRoute.APPEARANCE ->
                MobiMonDestination(
                    title = stringResource(R.string.pet_destination_title),
                    onBack = navigator.back,
                    onHome = navigator.returnHome,
                    modifier = modifier,
                ) {
                    CustomizationScreen(
                        inventory = inventoryState.inventory,
                        catalog = inventoryState.catalog,
                        selectedItemId = inventoryState.selectedItemId,
                        purchasing = inventoryState.purchasing,
                        purchaseFailed = inventoryState.purchaseFailed,
                        onSelectItem = inventoryModel::selectItem,
                        onPurchaseItem = inventoryModel::purchaseItem,
                        onEquipItem = inventoryModel::equipItem,
                        onEquipFriend = inventoryModel::equipFriend,
                        pointBalance = balance,
                        pointLoadFailed = balanceFailed,
                        saving = inventoryState.saving,
                        loadFailed = inventoryState.loadFailed,
                        saveFailed = inventoryState.saveFailed,
                        onRetry = onRetry,
                    )
                }
            else -> error("Unsupported companion route: $route")
        }
    }
}
