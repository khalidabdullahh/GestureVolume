package com.gesturevolume.app.system.accessibility

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.os.Build
import android.os.PowerManager
import android.os.SystemClock
import android.view.Display
import androidx.annotation.RequiresApi
import com.gesturevolume.app.data.model.EdgeSide
import com.gesturevolume.app.data.model.HandleColorMode
import kotlinx.coroutines.withContext
import android.widget.Toast
import android.view.accessibility.AccessibilityEvent
import com.gesturevolume.app.data.model.GestureConfig
import com.gesturevolume.app.data.preferences.GesturePreferences
import com.gesturevolume.app.system.overlay.OverlayManager
import com.gesturevolume.app.system.volume.AndroidVolumeController
import com.gesturevolume.app.util.HapticFeedbackHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GestureAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var gesturePreferences: GesturePreferences
    private lateinit var volumeController: AndroidVolumeController
    private lateinit var overlayManager: OverlayManager
    private lateinit var hapticHelper: HapticFeedbackHelper

    private var currentConfig = GestureConfig()
    private var timeoutJob: Job? = null
    private var swipeFraction = 0f
    private var lastSwipeLevel = 0
    private var screenReceiver: BroadcastReceiver? = null

    override fun onCreate() {
        super.onCreate()
        gesturePreferences = GesturePreferences(this)
        volumeController = AndroidVolumeController(this)
        overlayManager = OverlayManager(this)
        hapticHelper = HapticFeedbackHelper(this)

        serviceScope.launch {
            gesturePreferences.configFlow.collect { config ->
                currentConfig = config
                overlayManager.updateEdgeHandle(config)
                requestBackgroundSample()
            }
        }

        registerScreenStateReceiver()
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        ServiceStateHolder.setConnected(true)
        volumeController.refreshCurrentVolume()
        attachHandle()
        requestBackgroundSample()
    }

    private fun attachHandle() {
        overlayManager.attachEdgeHandle(
            config = currentConfig,
            onVolumeSwipe = { deltaFraction ->
                handleVolumeSwipe(deltaFraction)
            },
            onShowHudRequested = {
                val volState = volumeController.refreshCurrentVolume()
                swipeFraction = volState.progress
                lastSwipeLevel = volState.currentVolume
                if (currentConfig.hapticFeedback) {
                    hapticHelper.triggerGestureActivated()
                }
                overlayManager.showHud(volState, currentConfig)
                restartTimeout()
            },
            onGestureFinished = {
                restartTimeout()
            },
            onTap = {
                // Tap on the white handle -> open the native system volume bar
                if (currentConfig.hapticFeedback) {
                    hapticHelper.triggerGestureActivated()
                }
                overlayManager.hideHud()
                volumeController.showSystemVolumePanel()
            },
            onDoubleTap = {
                takeScreenshotFromHandle()
            },
            onLongPress = {
                turnScreenOffFromHandle()
            }
        )
    }

    // Make sure the handle exists, is visible and the volume cache is fresh (after idle / screen on)
    private fun healHandle() {
        volumeController.refreshCurrentVolume()
        if (!overlayManager.isHandleAttached()) {
            attachHandle()
        }
        overlayManager.setEdgeHandleVisible(true)
        requestBackgroundSample()
    }

    // Press & hold on the handle: lock the screen / turn it off (Android 9+)
    private fun turnScreenOffFromHandle() {
        timeoutJob?.cancel()
        overlayManager.hideHud()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            Toast.makeText(this, "Screen off needs Android 9 or newer", Toast.LENGTH_SHORT).show()
            overlayManager.setEdgeHandleVisible(true)
            return
        }
        // The handle has already faded out smoothly, so the screen turns off without a flash
        if (!performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)) {
            overlayManager.setEdgeHandleVisible(true)
        }
    }

    // ---- Adaptive handle colour: measure the brightness next to the handle (Android 11+) ----
    private var lastSampleAt = 0L
    private var sampleScheduled = false
    private var backgroundLight: Boolean? = null
    private var sampleFailures = 0

    private fun requestBackgroundSample() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return
        if (currentConfig.handleColorMode != HandleColorMode.ADAPTIVE) return
        if (sampleScheduled) return
        val wait = (1200L - (SystemClock.uptimeMillis() - lastSampleAt)).coerceAtLeast(200L)
        sampleScheduled = true
        serviceScope.launch {
            delay(wait)
            sampleScheduled = false
            sampleBackground()
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun sampleBackground() {
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        if (!pm.isInteractive) return
        lastSampleAt = SystemClock.uptimeMillis()
        val config = currentConfig
        try {
            takeScreenshot(Display.DEFAULT_DISPLAY, mainExecutor, object : TakeScreenshotCallback {
                override fun onSuccess(screenshot: ScreenshotResult) {
                    serviceScope.launch {
                        val lum = withContext(Dispatchers.Default) { measureBackground(screenshot, config) }
                        if (lum == null) onSampleFailed() else onSampleMeasured(lum)
                    }
                }

                override fun onFailure(errorCode: Int) {
                    onSampleFailed()
                }
            })
        } catch (_: Exception) {
            onSampleFailed()
        }
    }

    // Average brightness (0..1) of a few pixels right next to the handle. Nothing is stored or sent.
    @RequiresApi(Build.VERSION_CODES.R)
    private fun measureBackground(screenshot: ScreenshotResult, config: GestureConfig): Float? {
        val buffer = screenshot.hardwareBuffer
        try {
            val hw = Bitmap.wrapHardwareBuffer(buffer, screenshot.colorSpace) ?: return null
            val sw = hw.copy(Bitmap.Config.ARGB_8888, false)
            hw.recycle()
            if (sw == null) return null

            val density = resources.displayMetrics.density
            val w = sw.width
            val h = sw.height
            val windowPx = 32f * density
            val x0: Int
            val x1: Int
            if (config.edgeSide == EdgeSide.RIGHT) {
                x1 = (w - windowPx - 2f * density).toInt()
                x0 = (x1 - 14f * density).toInt()
            } else {
                x0 = (windowPx + 2f * density).toInt()
                x1 = (x0 + 14f * density).toInt()
            }
            val cy = h / 2f + config.edgeYOffsetDp * density
            val half = config.edgeLengthDp * density / 2f
            val y0 = (cy - half).toInt().coerceIn(0, h - 1)
            val y1 = (cy + half).toInt().coerceIn(0, h - 1)
            val cx0 = x0.coerceIn(0, w - 1)
            val cx1 = x1.coerceIn(0, w - 1)

            var sum = 0.0
            var n = 0
            val stepsX = 4
            val stepsY = 12
            for (i in 0..stepsY) {
                val y = y0 + (y1 - y0) * i / stepsY
                for (j in 0..stepsX) {
                    val x = cx0 + (cx1 - cx0) * j / stepsX
                    val px = sw.getPixel(x, y)
                    sum += 0.299 * android.graphics.Color.red(px) +
                        0.587 * android.graphics.Color.green(px) +
                        0.114 * android.graphics.Color.blue(px)
                    n++
                }
            }
            sw.recycle()
            return if (n == 0) null else (sum / n / 255.0).toFloat()
        } catch (_: Exception) {
            return null
        } finally {
            try { buffer.close() } catch (_: Exception) {}
        }
    }

    private fun onSampleMeasured(lum: Float) {
        sampleFailures = 0
        // Hysteresis so the handle does not flicker on mid-grey backgrounds
        val light = when {
            lum > 0.58f -> true
            lum < 0.42f -> false
            else -> backgroundLight ?: (lum > 0.5f)
        }
        backgroundLight = light
        overlayManager.setAdaptiveSampling(true)
        overlayManager.setHandleBackgroundLight(light)
    }

    private fun onSampleFailed() {
        sampleFailures++
        // If measuring keeps failing, fall back to the light handle with a dark outline
        if (sampleFailures >= 3) overlayManager.setAdaptiveSampling(false)
    }

    // Double-tap on the handle: hide it for a moment so it is not in the shot, then take a screenshot
    private fun takeScreenshotFromHandle() {
        if (currentConfig.hapticFeedback) {
            hapticHelper.triggerGestureActivated()
        }
        overlayManager.hideHud()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            Toast.makeText(this, "Screenshot needs Android 9 or newer", Toast.LENGTH_SHORT).show()
            return
        }
        overlayManager.setEdgeHandleVisible(false)
        // Pause colour measuring so it never competes with the screenshot
        lastSampleAt = SystemClock.uptimeMillis() + 2000L
        serviceScope.launch {
            delay(90) // just enough frames for the handle to disappear from the capture
            performGlobalAction(GLOBAL_ACTION_TAKE_SCREENSHOT)
            delay(700)
            overlayManager.setEdgeHandleVisible(true)
        }
    }

    // Continuous swipe: the finger moves a fraction of the full range; the volume only changes
    // (one system call, one haptic tick) when the whole level actually changes.
    private fun handleVolumeSwipe(deltaFraction: Float) {
        swipeFraction = (swipeFraction + deltaFraction).coerceIn(0f, 1f)
        val state = volumeController.volumeState.value
        val range = state.maxVolume - state.minVolume
        if (range <= 0) return

        val target = state.minVolume + Math.round(swipeFraction * range)
        if (target == lastSwipeLevel) return

        val newState = volumeController.setVolume(target)
        lastSwipeLevel = newState.currentVolume
        if (currentConfig.hapticFeedback) {
            if (newState.currentVolume == newState.maxVolume || newState.currentVolume == newState.minVolume) {
                hapticHelper.triggerVolumeBoundary()
            } else {
                hapticHelper.triggerVolumeStep()
            }
        }

        overlayManager.updateHud(newState, currentConfig)
        restartTimeout()
    }

    private fun restartTimeout() {
        timeoutJob?.cancel()
        timeoutJob = serviceScope.launch {
            delay(currentConfig.timeoutSeconds * 1000L)
            overlayManager.hideHud()
        }
    }

    private fun registerScreenStateReceiver() {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_USER_PRESENT)
            addAction(Intent.ACTION_SCREEN_ON)
        }
        screenReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    Intent.ACTION_SCREEN_OFF -> {
                        timeoutJob?.cancel()
                        overlayManager.hideHud()
                    }
                    Intent.ACTION_SCREEN_ON, Intent.ACTION_USER_PRESENT -> healHandle()
                }
            }
        }
        try {
            registerReceiver(screenReceiver, filter)
        } catch (_: Exception) {}
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // No scraping or inspecting accessibility content: events are only used as a signal that
        // the screen changed, so the handle colour can be re-measured (throttled to ~1 per second).
        if (event == null || event.packageName == packageName) return
        requestBackgroundSample()
    }

    override fun onInterrupt() {
        // The system may interrupt feedback at any time; the handle must stay alive.
        timeoutJob?.cancel()
        overlayManager.hideHud()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        ServiceStateHolder.setConnected(false)
        timeoutJob?.cancel()
        overlayManager.cleanup()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        ServiceStateHolder.setConnected(false)
        timeoutJob?.cancel()
        overlayManager.cleanup()
        try {
            screenReceiver?.let { unregisterReceiver(it) }
        } catch (_: Exception) {}
        serviceScope.cancel()
    }
}
