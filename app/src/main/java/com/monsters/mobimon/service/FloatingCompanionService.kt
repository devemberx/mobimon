package com.monsters.mobimon.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.monsters.mobimon.core.domain.SettingsRepository
import com.monsters.mobimon.core.presentation.CompanionAppearancePresentation
import com.monsters.mobimon.core.ui.MobiMonTheme
import com.monsters.mobimon.core.ui.PetAvatar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs

/**
 * Floating companion overlay service rendering the companion avatar on top of all screens.
 * Fails closed if overlay permission is lost or explicit opt-in preference is disabled.
 */
@AndroidEntryPoint
class FloatingCompanionService : Service() {
    @Inject lateinit var settingsRepository: SettingsRepository

    @Inject lateinit var companionAppearance: CompanionAppearancePresentation

    private var windowManager: WindowManager? = null
    private var composeView: ComposeView? = null
    private var lifecycleOwner: OverlayLifecycleOwner? = null
    private var isViewAttached = false

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return
        }

        initOverlayView()
        observeSettings()
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initOverlayView() {
        val wm =
            getSystemService(Context.WINDOW_SERVICE) as? WindowManager ?: run {
                stopSelf()
                return
            }
        windowManager = wm

        val layoutFlag =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            }

        val params =
            WindowManager
                .LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    layoutFlag,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    PixelFormat.TRANSLUCENT,
                ).apply {
                    gravity = Gravity.TOP or Gravity.START
                    x = 80
                    y = 120
                }

        val owner = OverlayLifecycleOwner()
        lifecycleOwner = owner

        val view =
            ComposeView(this).apply {
                setViewTreeLifecycleOwner(owner)
                setViewTreeSavedStateRegistryOwner(owner)
                setViewTreeViewModelStoreOwner(owner)
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
                setContent {
                    CompositionLocalProvider(
                        LocalViewModelStoreOwner provides owner,
                    ) {
                        MobiMonTheme {
                            val appearanceState = companionAppearance.state()
                            Box(
                                modifier =
                                    Modifier
                                        .size(140.dp)
                                        .padding(8.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                PetAvatar(
                                    modifier = Modifier.fillMaxSize(),
                                    appearanceKey = "GOLDEN",
                                    friendId = appearanceState.friendId,
                                    accessoryId = appearanceState.accessoryId,
                                    outfitId = appearanceState.outfitId,
                                    backgroundId = null,
                                    isAnimated = true,
                                )
                            }
                        }
                    }
                }
            }

        setupDragAndTap(view, params, wm)

        try {
            wm.addView(view, params)
            isViewAttached = true
            composeView = view
        } catch (_: Exception) {
            stopSelf()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupDragAndTap(
        view: ComposeView,
        params: WindowManager.LayoutParams,
        wm: WindowManager,
    ) {
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var isDragging = false

        view.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isDragging = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - initialTouchX).toInt()
                    val dy = (event.rawY - initialTouchY).toInt()
                    if (abs(dx) > 10 || abs(dy) > 10) {
                        isDragging = true
                    }
                    params.x = initialX + dx
                    params.y = initialY + dy
                    try {
                        wm.updateViewLayout(view, params)
                    } catch (_: Exception) {
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!isDragging) {
                        val launchIntent =
                            packageManager.getLaunchIntentForPackage(packageName)?.apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                            }
                        if (launchIntent != null) {
                            startActivity(launchIntent)
                        }
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun observeSettings() {
        serviceScope.launch {
            settingsRepository.settings.collect { settings ->
                if (!settings.launcherCharacterEnabled || !Settings.canDrawOverlays(this@FloatingCompanionService)) {
                    stopSelf()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        if (isViewAttached) {
            try {
                composeView?.let { windowManager?.removeView(it) }
            } catch (_: Exception) {
            }
            isViewAttached = false
        }
        composeView = null
        lifecycleOwner?.destroy()
        lifecycleOwner = null
        windowManager = null
    }
}
