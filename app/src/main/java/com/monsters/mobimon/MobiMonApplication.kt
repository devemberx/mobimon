package com.monsters.mobimon

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.monsters.mobimon.runtime.CompanionRuntime
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MobiMonApplication : Application() {
    @Inject lateinit var runtime: CompanionRuntime

    override fun onCreate() {
        super.onCreate()
        // Process lifecycle survives Activity recreation; all routes share one connection.
        ProcessLifecycleOwner.get().lifecycle.addObserver(
            object : DefaultLifecycleObserver {
                override fun onStart(owner: LifecycleOwner) {
                    runtime.start()
                }

                override fun onStop(owner: LifecycleOwner) {
                    runtime.stop()
                }
            },
        )
    }
}
