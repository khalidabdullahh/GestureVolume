package com.gesturevolume.app.system.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import com.gesturevolume.app.data.model.EdgeSide
import com.gesturevolume.app.data.model.GestureConfig
import com.gesturevolume.app.data.model.HudPlacement
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
            hudView?.setInitial(volumeState)

            if (!isHudAttached && hudView != null) {
                val isRight = config.edgeSide == EdgeSide.RIGHT
                hudView?.setTextSizeSp(config.hudSizeSp)

                val dx = config.hudOffsetXDp
                val dy = config.hudOffsetYDp
                val (hudGravity, hudX, hudY) = when (config.hudPlacement) {
                    HudPlacement.BESIDE_HANDLE -> Triple(
                        (if (isRight) Gravity.END else Gravity.START) or Gravity.CENTER_VERTICAL,
                        dpToPx((44 + dx).coerceAtLeast(0).toFloat()).toInt(),
                        dpToPx((config.edgeYOffsetDp + dy).toFloat()).toInt()
                    )
                    HudPlacement.CENTER -> Triple(
                        Gravity.CENTER,
                        dpToPx(dx.toFloat()).toInt(),
                        dpToPx(dy.toFloat()).toInt()
                    )
                    HudPlacement.TOP -> Triple(
                        Gravity.TOP or Gravity.CENTER_HORIZONTAL,
                        dpToPx(dx.toFloat()).toInt(),
                        dpToPx((60 + dy).coerceAtLeast(0).toFloat()).toInt()
                    )
                }

                val params = WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                    PixelFormat.TRANSLUCENT
                ).apply {
                    this.gravity = hudGravity
                    this.x = hudX
                    this.y = hudY
                }

                hudView?.alpha = 0f
                hudView?.translationX = if (isRight) dpToPx(14f) else -dpToPx(14f)
                try {
                    windowManager.addView(hudView, params)
                    isHudAttached = true
                    hudView?.animate()?.alpha(1f)?.translationX(0f)?.setDuration(140)?.start()
                } catch (_: Exception) {}
            } else {
                hudView?.animate()?.alpha(1f)?.translationX(0f)?.setDuration(80)?.start()
            }
        }
    }

    fun updateHud(volumeState: VolumeState, config: GestureConfig) {
        mainHandler.post {
            if (isHudAttached) {
                hudView?.updateVolume(volumeState)
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
                    ?.setDuration(160)
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
        onVolumeSwipe: (deltaFraction: Float) -> Unit,
        onShowHudRequested: () -> Unit,
        onGestureFinished: () -> Unit,
        onTap: () -> Unit,
        onDoubleTap: () -> Unit
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
                onGestureFinished = onGestureFinished,
                onTap = onTap,
                onDoubleTap = onDoubleTap
            )
            view.updateConfig(config)

            val widthPx = dpToPx(32f).toInt()
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
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
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

                val widthPx = dpToPx(32f).toInt()
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
                            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
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

    fun setEdgeHandleVisible(visible: Boolean) {
        mainHandler.post {
            edgeHandleView?.visibility = if (visible) View.VISIBLE else View.INVISIBLE
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
