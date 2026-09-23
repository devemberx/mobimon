package com.monsters.mobimon.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.monsters.mobimon.R
import com.monsters.mobimon.core.domain.SettingsRepository
import com.monsters.mobimon.core.presentation.CompanionAppearancePresentation
import com.monsters.mobimon.core.ui.MobiMonTheme
import com.monsters.mobimon.core.ui.PetAvatar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.random.Random

private const val NOTIFICATION_CHANNEL_ID = "mobimon_floating_companion"
private const val NOTIFICATION_ID = 1001
private const val DEFAULT_OVERLAY_X = 1900
private const val DEFAULT_OVERLAY_Y = 700

/**
 * Floating companion overlay service rendering the companion avatar on top of all screens.
 * Runs as a specialUse foreground service to persist across the launcher and other apps.
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

    private var isMoving by mutableStateOf(false)
    private var movingLeft by mutableStateOf(true)
    private var wanderJob: Job? = null

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return
        }
        startInForeground()
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
        startInForeground()
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    getString(R.string.floating_service_notification_channel_name),
                    NotificationManager.IMPORTANCE_LOW,
                ).apply {
                    setShowBadge(false)
                }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannel(channel)
        }
    }

    private fun startInForeground() {
        createNotificationChannel()
        val notification =
            NotificationCompat
                .Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.floating_service_notification_title))
                .setContentText(getString(R.string.floating_service_notification_text))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
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
                    x = DEFAULT_OVERLAY_X
                    y = DEFAULT_OVERLAY_Y
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
                                    isMoving = isMoving,
                                    movingLeft = movingLeft,
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
            startWandering(view, params, wm)
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
                    wanderJob?.cancel()
                    isMoving = false
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
                    val displayMetrics = resources.displayMetrics
                    params.x = (initialX + dx).coerceIn(50, (displayMetrics.widthPixels - 200).coerceAtLeast(50))
                    params.y = (initialY + dy).coerceIn(50, (displayMetrics.heightPixels - 200).coerceAtLeast(50))
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
                    startWandering(view, params, wm, initialDelayMs = 4000L)
                    true
                }
                MotionEvent.ACTION_CANCEL -> {
                    startWandering(view, params, wm, initialDelayMs = 4000L)
                    false
                }
                else -> false
            }
        }
    }

    private fun startWandering(
        view: ComposeView,
        params: WindowManager.LayoutParams,
        wm: WindowManager,
        initialDelayMs: Long = 4000L,
    ) {
        wanderJob?.cancel()
        wanderJob =
            serviceScope.launch {
                delay(initialDelayMs)
                while (isActive) {
                    isMoving = false
                    val idleDuration = Random.nextLong(3500L, 7000L)
                    delay(idleDuration)
                    if (!isActive) break

                    val displayMetrics = resources.displayMetrics
                    val minX = 100
                    val maxX = (displayMetrics.widthPixels - 300).coerceAtLeast(minX + 200)
                    val minY = 150
                    val maxY = (displayMetrics.heightPixels - 300).coerceAtLeast(minY + 200)

                    val currentX = params.x
                    val currentY = params.y

                    val preferRight = currentX <= minX + 250
                    val preferLeft = currentX >= maxX - 250
                    val distance = Random.nextInt(150, 450)
                    val dy = Random.nextInt(-100, 101)

                    val (targetX, targetY) =
                        FloatingCompanionWanderMath.calculateTargetPosition(
                            currentX = currentX,
                            currentY = currentY,
                            minX = minX,
                            maxX = maxX,
                            minY = minY,
                            maxY = maxY,
                            distance = distance,
                            preferRight = preferRight,
                            preferLeft = preferLeft,
                            goLeft = Random.nextBoolean(),
                            deltaY = dy,
                        )

                    val actualDx = targetX - currentX
                    if (abs(actualDx) < 30) {
                        continue
                    }

                    movingLeft = actualDx < 0
                    isMoving = true

                    val moveDistance = hypot(actualDx.toDouble(), (targetY - currentY).toDouble()).toFloat()
                    val moveDurationMs = ((moveDistance / 250f) * 1000).toLong().coerceIn(800L, 3000L)
                    val startTime = SystemClock.uptimeMillis()
                    val startX = currentX
                    val startY = currentY

                    while (isActive) {
                        val elapsed = SystemClock.uptimeMillis() - startTime
                        val fraction = (elapsed.toFloat() / moveDurationMs).coerceIn(0f, 1f)
                        params.x = (startX + (targetX - startX) * fraction).toInt()
                        params.y = (startY + (targetY - startY) * fraction).toInt()
                        try {
                            wm.updateViewLayout(view, params)
                        } catch (_: Exception) {
                            break
                        }
                        if (fraction >= 1f) break
                        delay(16L)
                    }

                    isMoving = false
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
        wanderJob?.cancel()
        wanderJob = null
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
        try {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } catch (_: Exception) {
        }
    }
}

internal object FloatingCompanionWanderMath {
    fun calculateTargetPosition(
        currentX: Int,
        currentY: Int,
        minX: Int,
        maxX: Int,
        minY: Int,
        maxY: Int,
        distance: Int,
        preferRight: Boolean,
        preferLeft: Boolean,
        goLeft: Boolean,
        deltaY: Int,
    ): Pair<Int, Int> {
        val chooseLeft =
            when {
                preferRight -> false
                preferLeft -> true
                else -> goLeft
            }
        val targetX =
            if (chooseLeft) {
                (currentX - distance).coerceIn(minX, maxX)
            } else {
                (currentX + distance).coerceIn(minX, maxX)
            }
        val targetY = (currentY + deltaY).coerceIn(minY, maxY)
        return Pair(targetX, targetY)
    }
}
