package com.gesturevolume.app.system.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import com.gesturevolume.app.data.model.GestureConfig
import com.gesturevolume.app.data.model.HudPosition
import com.gesturevolume.app.data.model.VolumeState

class OverlayManager(private val context: Context) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val mainHandler = Handler(Looper.getMainLooper())

    private var hudView: VolumeHudView? = null
    private var isHudAttached = false

    private var touchDetectorView: View? = null
    private var isTouchDetectorAttached = false

    fun showHud(volumeState: VolumeState, config: GestureConfig) {
        mainHandler.post {
            if (hudView == null) {
                hudView = VolumeHudView(context)
            }

            hudView?.updateVolume(volumeState, config.showPercentage)

            if (!isHudAttached && hudView != null) {
                val gravity = when (config.hudPosition) {
                    HudPosition.TOP -> Gravity.TOP or Gravity.CENTER_HORIZONTAL
                    HudPosition.CENTER -> Gravity.CENTER
                    HudPosition.RIGHT -> Gravity.END or Gravity.CENTER_VERTICAL
                    HudPosition.LEFT -> Gravity.START or Gravity.CENTER_VERTICAL
                }

                val yOffset = when (config.hudPosition) {
                    HudPosition.TOP -> 120
                    else -> 0
                }

                val params = WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                    PixelFormat.TRANSLUCENT
                ).apply {
                    this.gravity = gravity
                    this.y = yOffset
                }

                hudView?.alpha = 0f
                try {
                    windowManager.addView(hudView, params)
                    isHudAttached = true
                    hudView?.animate()?.alpha(1f)?.setDuration(160)?.start()
                } catch (_: Exception) {}
            } else {
                hudView?.animate()?.alpha(1f)?.setDuration(80)?.start()
            }
        }
    }

    fun updateHud(volumeState: VolumeState, config: GestureConfig) {
        mainHandler.post {
            if (isHudAttached) {
                hudView?.updateVolume(volumeState, config.showPercentage)
            } else {
                showHud(volumeState, config)
            }
        }
    }

    fun hideHud() {
        mainHandler.post {
            if (isHudAttached && hudView != null) {
                hudView?.animate()
                    ?.alpha(0f)
                    ?.setDuration(220)
                    ?.withEndAction {
                        try {
                            if (isHudAttached && hudView != null) {
                                windowManager.removeViewImmediate(hudView)
                                isHudAttached = false
                            }
                        } catch (_: Exception) {}
                    }
                    ?.start()
            }
        }
    }

    fun attachTouchOverlay(onTouch: (MotionEvent) -> Boolean) {
        mainHandler.post {
            if (isTouchDetectorAttached) return@post

            val view = object : View(context) {
                override fun onTouchEvent(event: MotionEvent): Boolean {
                    return onTouch(event)
                }
            }

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT
            )

            try {
                windowManager.addView(view, params)
                touchDetectorView = view
                isTouchDetectorAttached = true
            } catch (_: Exception) {}
        }
    }

    fun detachTouchOverlay() {
        mainHandler.post {
            if (isTouchDetectorAttached && touchDetectorView != null) {
                try {
                    windowManager.removeViewImmediate(touchDetectorView)
                } catch (_: Exception) {}
                touchDetectorView = null
                isTouchDetectorAttached = false
            }
        }
    }

    fun cleanup() {
        hideHud()
        detachTouchOverlay()
    }
}
