package com.monsters.mobimon.runtime

import android.car.Car
import android.car.drivingstate.CarUxRestrictionsManager
import android.content.Context
import android.content.pm.PackageManager
import com.monsters.mobimon.core.domain.AppUseState
import com.monsters.mobimon.core.domain.CurrentAppUse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

interface AppUseLifecycle {
    fun start()

    fun stop()
}

/** Reads the current display's AAOS UX restrictions. Unknown AAOS state fails closed. */
class CarAppUseMonitor(
    private val context: Context,
) : AppUseLifecycle,
    CurrentAppUse {
    private val mutableState =
        MutableStateFlow(
            if (context.packageManager.hasSystemFeature(PackageManager.FEATURE_AUTOMOTIVE)) {
                AppUseState.UNAVAILABLE
            } else {
                AppUseState.ALLOWED
            },
        )
    val states = mutableState.asStateFlow()
    private var car: Car? = null
    private var manager: CarUxRestrictionsManager? = null
    private var running = false
    private var generation = 0L

    override fun state(): AppUseState = states.value

    @Synchronized
    override fun start() {
        if (!context.packageManager.hasSystemFeature(PackageManager.FEATURE_AUTOMOTIVE)) {
            mutableState.value = AppUseState.ALLOWED
            return
        }
        if (running) return
        running = true
        val currentGeneration = ++generation
        mutableState.value = AppUseState.UNAVAILABLE
        try {
            car =
                Car.createCar(context, null, Car.CAR_WAIT_TIMEOUT_DO_NOT_WAIT) { connected, ready ->
                    onCarLifecycle(connected, ready, currentGeneration)
                }
            if (car == null) running = false
        } catch (_: RuntimeException) {
            stop()
        } catch (_: LinkageError) {
            stop()
        }
    }

    @Synchronized
    private fun onCarLifecycle(
        connected: Car,
        ready: Boolean,
        callbackGeneration: Long,
    ) {
        if (!running || callbackGeneration != generation) return
        mutableState.value = AppUseState.UNAVAILABLE
        clearManager()
        if (!ready) return
        try {
            val restrictions =
                connected.getCarManager(Car.CAR_UX_RESTRICTION_SERVICE) as? CarUxRestrictionsManager
                    ?: return
            manager = restrictions
            restrictions.registerListener { current ->
                if (running && callbackGeneration == generation && manager === restrictions) {
                    mutableState.value = current.toAppUseState()
                }
            }
            mutableState.value = restrictions.currentCarUxRestrictions.toAppUseState()
        } catch (_: RuntimeException) {
            clearManager()
        } catch (_: LinkageError) {
            clearManager()
        }
    }

    private fun clearManager() {
        try {
            manager?.unregisterListener()
        } catch (_: RuntimeException) {
            // A disconnected car service can reject listener teardown.
        } catch (_: LinkageError) {
            // The optional car library may disappear with the platform service.
        } finally {
            manager = null
        }
    }

    @Synchronized
    override fun stop() {
        running = false
        generation++
        mutableState.value = AppUseState.UNAVAILABLE
        clearManager()
        try {
            car?.disconnect()
        } catch (_: RuntimeException) {
            // State already fails closed; process teardown must continue.
        } catch (_: LinkageError) {
            // State already fails closed; process teardown must continue.
        } finally {
            car = null
        }
    }
}

private fun android.car.drivingstate.CarUxRestrictions.toAppUseState(): AppUseState =
    if (isRequiresDistractionOptimization) AppUseState.RESTRICTED else AppUseState.ALLOWED
