package com.monsters.mobimon.core.vss

object VssAdapterLocator {
    fun createOrNull(context: Any): VssRawVehicleSource? =
        try {
            val adapterClass =
                Class.forName("com.monsters.mobimon.core.vss.adapter.VssManagerRawVehicleSource")
            val contextClass = Class.forName("android.content.Context")
            adapterClass.getConstructor(contextClass).newInstance(context) as? VssRawVehicleSource
        } catch (_: Throwable) {
            null
        }
}
