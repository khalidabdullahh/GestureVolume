package com.gesturevolume.app.system.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.WindowManager
import com.gesturevolume.app.data.model.EdgeSide
import com.gesturevolume.app.data.model.GestureConfig
import com.gesturevolume.app.data.model.HudPosition
import com.gesturevolume.app.data.model.VolumeState

class OverlayManager(private val context: Context) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val mainHandler = Handler(Looper.getMainLooper())

    private var hudView: VolumeHudView? = null
    private var isHudAttached = false

    private var edgeHandleView: EdgeHandleView? = null
    private var isEdgeHandleAttached = false

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
                    HudPosition.TOP -> dpToPx(50f).toInt()
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

    fun attachEdgeHandle(
        config: GestureConfig,
        onVolumeSwipe: (stepDelta: Int) -> Unit,
        onShowHudRequested: () -> Unit,
        onGestureFinished: () -> Unit
    ) {
        mainHandler.post {
            if (isEdgeHandleAttached) {
                updateEdgeHandle(config)
                return@post
            }

            val view = EdgeHandleView(
                context = context,
                onVolumeSwipe = onVolumeSwipe,
                onShowHudRequested = onShowHudRequested,
                onGestureFinished = onGestureFinished
            )
            view.updateConfig(config)

            val widthPx = dpToPx(28f).toInt()
            val heightPx = dpToPx(config.edgeLengthDp.toFloat()).toInt()

            val gravity = if (config.edgeSide == EdgeSide.RIGHT) {
                Gravity.END or Gravity.CENTER_VERTICAL
            } else {
                Gravity.START or Gravity.CENTER_VERTICAL
            }

            val params = WindowManager.LayoutParams(
                widthPx,
                heightPx,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
            ).apply {
                this.gravity = gravity
                this.y = dpToPx(config.edgeYOffsetDp.toFloat()).toInt()
            }

            try {
                windowManager.addView(view, params)
                edgeHandleView = view
                isEdgeHandleAttached = true
            } catch (_: Exception) {}
        }
    }

    fun updateEdgeHandle(config: GestureConfig) {
        mainHandler.post {
            if (isEdgeHandleAttached && edgeHandleView != null) {
                edgeHandleView?.updateConfig(config)

                val widthPx = dpToPx(28f).toInt()
                val heightPx = dpToPx(config.edgeLengthDp.toFloat()).toInt()

                val gravity = if (config.edgeSide == EdgeSide.RIGHT) {
                    Gravity.END or Gravity.CENTER_VERTICAL
                } else {
                    Gravity.START or Gravity.CENTER_VERTICAL
                }

                val params = WindowManager.LayoutParams(
                    widthPx,
                    heightPx,
                    WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                    PixelFormat.TRANSLUCENT
                ).apply {
                    this.gravity = gravity
                    this.y = dpToPx(config.edgeYOffsetDp.toFloat()).toInt()
                }

                try {
                    windowManager.updateViewLayout(edgeHandleView, params)
                } catch (_: Exception) {}
            }
        }
    }

    fun detachEdgeHandle() {
        mainHandler.post {
            if (isEdgeHandleAttached && edgeHandleView != null) {
                try {
                    windowManager.removeViewImmediate(edgeHandleView)
                } catch (_: Exception) {}
                edgeHandleView = null
                isEdgeHandleAttached = false
            }
        }
    }

    fun cleanup() {
        hideHud()
        detachEdgeHandle()
    }

    private fun dpToPx(dp: Float): Float {
        return dp * context.resources.displayMetrics.density
    }
}
