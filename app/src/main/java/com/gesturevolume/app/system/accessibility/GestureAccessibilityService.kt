package com.gesturevolume.app.system.accessibility

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
            }
        }

        registerScreenStateReceiver()
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        ServiceStateHolder.setConnected(true)
        volumeController.refreshCurrentVolume()

        overlayManager.attachEdgeHandle(
            config = currentConfig,
            onVolumeSwipe = { stepDelta ->
                handleVolumeStep(stepDelta)
            },
            onShowHudRequested = {
                val volState = volumeController.refreshCurrentVolume()
                if (currentConfig.hapticFeedback) {
                    hapticHelper.triggerGestureActivated()
                }
                overlayManager.showHud(volState, currentConfig)
                restartTimeout()
            },
            onGestureFinished = {
                restartTimeout()
            }
        )
    }

    private fun handleVolumeStep(stepDelta: Int) {
        val previousVolume = volumeController.volumeState.value.currentVolume
        val newState = volumeController.adjustVolume(stepDelta)

        if (newState.currentVolume != previousVolume && currentConfig.hapticFeedback) {
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
        }
        screenReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == Intent.ACTION_SCREEN_OFF) {
                    timeoutJob?.cancel()
                    overlayManager.hideHud()
                }
            }
        }
        try {
            registerReceiver(screenReceiver, filter)
        } catch (_: Exception) {}
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // No scraping or inspecting accessibility content.
        // Pure event pass-through to strictly comply with Android privacy and Play Protect policies.
    }

    override fun onInterrupt() {
        timeoutJob?.cancel()
        overlayManager.cleanup()
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
