package com.gesturevolume.app.system.accessibility

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.view.MotionEvent
import android.view.accessibility.AccessibilityEvent
import com.gesturevolume.app.data.model.GestureConfig
import com.gesturevolume.app.data.preferences.GesturePreferences
import com.gesturevolume.app.domain.gesture.CircleGestureRecognizer
import com.gesturevolume.app.domain.gesture.GestureEvent
import com.gesturevolume.app.domain.gesture.GestureState
import com.gesturevolume.app.domain.gesture.GestureStateMachine
import com.gesturevolume.app.domain.gesture.TouchPoint
import com.gesturevolume.app.domain.volume.VolumeMapper
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

    private val recognizer = CircleGestureRecognizer()
    private val stateMachine = GestureStateMachine()
    private val volumeMapper = VolumeMapper()

    private var currentConfig = GestureConfig()
    private val rawTouchPoints = mutableListOf<TouchPoint>()
    private var volumeGestureStartY = 0f
    private var lastRecordedVolume = 0

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
            }
        }

        registerScreenStateReceiver()
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        ServiceStateHolder.setConnected(true)
        volumeController.refreshCurrentVolume()

        overlayManager.attachTouchOverlay { motionEvent ->
            handleTouchEvent(motionEvent)
        }
    }

    private fun handleTouchEvent(event: MotionEvent): Boolean {
        val touchPoint = TouchPoint(event.rawX, event.rawY, System.currentTimeMillis())
        val currentState = stateMachine.state.value

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                if (currentState is GestureState.VolumeGestureActive || currentState is GestureState.HudVisible) {
                    volumeGestureStartY = event.rawY
                    lastRecordedVolume = volumeController.refreshCurrentVolume().currentVolume
                    restartTimeout()
                } else {
                    rawTouchPoints.clear()
                    rawTouchPoints.add(touchPoint)
                    stateMachine.transition(GestureEvent.TouchDown(touchPoint))
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (currentState is GestureState.VolumeGestureActive || currentState is GestureState.AdjustingVolume) {
                    val deltaY = volumeGestureStartY - event.rawY // positive = upward = louder
                    val stepDelta = volumeMapper.calculateDeltaLevel(deltaY, currentConfig.swipeSensitivity)

                    if (stepDelta != 0) {
                        val previousVolume = volumeController.volumeState.value.currentVolume
                        val newState = volumeController.adjustVolume(stepDelta)
                        volumeGestureStartY = event.rawY // reset anchor after step consumed

                        if (newState.currentVolume != previousVolume) {
                            if (currentConfig.hapticFeedback) {
                                if (newState.currentVolume == newState.maxVolume || newState.currentVolume == newState.minVolume) {
                                    hapticHelper.triggerVolumeBoundary()
                                } else {
                                    hapticHelper.triggerVolumeStep()
                                }
                            }
                        }

                        overlayManager.updateHud(newState, currentConfig)
                        restartTimeout()
                    }
                    return true
                } else {
                    rawTouchPoints.add(touchPoint)
                    stateMachine.transition(GestureEvent.TouchMove(touchPoint))

                    if (rawTouchPoints.size >= 10) {
                        val result = recognizer.evaluate(rawTouchPoints, currentConfig.circleSensitivity)
                        if (result.isCircle) {
                            stateMachine.transition(GestureEvent.CircleRecognized(result))
                            volumeGestureStartY = event.rawY
                            val currentVolState = volumeController.refreshCurrentVolume()
                            lastRecordedVolume = currentVolState.currentVolume

                            if (currentConfig.hapticFeedback) {
                                hapticHelper.triggerGestureActivated()
                            }

                            stateMachine.setState(
                                GestureState.VolumeGestureActive(
                                    startY = event.rawY,
                                    currentY = event.rawY,
                                    activeSinceMs = System.currentTimeMillis()
                                )
                            )

                            overlayManager.showHud(currentVolState, currentConfig)
                            restartTimeout()
                            rawTouchPoints.clear()
                            return true
                        }
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (currentState is GestureState.VolumeGestureActive || currentState is GestureState.AdjustingVolume) {
                    stateMachine.setState(
                        GestureState.HudVisible(
                            currentVolume = volumeController.volumeState.value.currentVolume,
                            percentage = volumeController.volumeState.value.percentage
                        )
                    )
                    restartTimeout()
                } else {
                    rawTouchPoints.clear()
                    stateMachine.reset()
                }
            }
        }
        return false
    }

    private fun restartTimeout() {
        timeoutJob?.cancel()
        timeoutJob = serviceScope.launch {
            delay(currentConfig.timeoutSeconds * 1000L)
            overlayManager.hideHud()
            stateMachine.reset()
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
                    stateMachine.reset()
                    rawTouchPoints.clear()
                }
            }
        }
        try {
            registerReceiver(screenReceiver, filter)
        } catch (_: Exception) {}
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // No scraping or inspecting accessibility content.
        // Pure event pass-through to comply strictly with privacy and policy.
    }

    override fun onInterrupt() {
        timeoutJob?.cancel()
        overlayManager.cleanup()
        stateMachine.reset()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        ServiceStateHolder.setConnected(false)
        timeoutJob?.cancel()
        overlayManager.cleanup()
        stateMachine.reset()
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
