package com.gesturevolume.app.system.overlay

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.view.MotionEvent
import android.view.View
import com.gesturevolume.app.data.model.EdgeSide
import com.gesturevolume.app.data.model.GestureConfig
import kotlin.math.abs

@SuppressLint("ViewConstructor")
class EdgeHandleView(
    context: Context,
    private val onVolumeSwipe: (stepDelta: Int) -> Unit,
    private val onShowHudRequested: () -> Unit,
    private val onGestureFinished: () -> Unit
) : View(context) {

    private val pillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val notchPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val pillRect = RectF()

    private var currentConfig = GestureConfig()
    private var startY = 0f
    private var accumulatedDeltaY = 0f
    private var isInteracting = false

    private var touchScale = 1.0f
    private val scaleAnimator = ValueAnimator.ofFloat(1.0f, 1.15f).apply {
        duration = 150
        addUpdateListener {
            touchScale = it.animatedValue as Float
            invalidate()
        }
    }

    init {
        notchPaint.color = Color.WHITE
        notchPaint.style = Paint.Style.STROKE
        notchPaint.strokeWidth = dpToPx(2f)
        notchPaint.strokeCap = Paint.Cap.ROUND
    }

    fun updateConfig(config: GestureConfig) {
        this.currentConfig = config
        alpha = config.edgeOpacity
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val inset = dpToPx(3f)
        pillRect.set(inset, inset, w.toFloat() - inset, h.toFloat() - inset)

        val gradient = LinearGradient(
            0f, 0f, 0f, h.toFloat(),
            intArrayOf(
                Color.parseColor("#8B5CF6"), // Purple
                Color.parseColor("#6366F1"), // Indigo
                Color.parseColor("#06B6D4")  // Cyan
            ),
            null,
            Shader.TileMode.CLAMP
        )
        pillPaint.shader = gradient
        pillPaint.style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cornerRadius = width / 2f

        canvas.save()
        if (touchScale > 1.0f) {
            val cx = if (currentConfig.edgeSide == EdgeSide.RIGHT) width.toFloat() else 0f
            val cy = height / 2f
            canvas.scale(touchScale, 1.05f, cx, cy)
        }

        canvas.drawRoundRect(pillRect, cornerRadius, cornerRadius, pillPaint)

        // Draw tactile center notches
        val cy = height / 2f
        val notchLen = dpToPx(6f)
        val notchSpacing = dpToPx(5f)
        val notchX = if (currentConfig.edgeSide == EdgeSide.RIGHT) width * 0.45f else width * 0.55f

        // Center notch
        canvas.drawLine(notchX - notchLen / 2, cy, notchX + notchLen / 2, cy, notchPaint)
        // Top notch
        canvas.drawLine(notchX - notchLen / 2, cy - notchSpacing, notchX + notchLen / 2, cy - notchSpacing, notchPaint)
        // Bottom notch
        canvas.drawLine(notchX - notchLen / 2, cy + notchSpacing, notchX + notchLen / 2, cy + notchSpacing, notchPaint)

        canvas.restore()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                isInteracting = true
                startY = event.rawY
                accumulatedDeltaY = 0f
                alpha = 1.0f
                scaleAnimator.start()
                onShowHudRequested()
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                if (isInteracting) {
                    val currentY = event.rawY
                    val deltaY = startY - currentY // Upward = positive (volume up)
                    accumulatedDeltaY += deltaY
                    startY = currentY

                    // Sensitivity step calculation (faster and responsive)
                    val sens = currentConfig.swipeSensitivity.coerceIn(0.4f, 3.0f)
                    val thresholdPx = dpToPx(12f) / sens
                    if (abs(accumulatedDeltaY) >= thresholdPx) {
                        val steps = (accumulatedDeltaY / thresholdPx).toInt()
                        if (steps != 0) {
                            onVolumeSwipe(steps)
                            accumulatedDeltaY -= (steps * thresholdPx)
                        }
                    }
                    return true
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (isInteracting) {
                    isInteracting = false
                    scaleAnimator.reverse()
                    alpha = currentConfig.edgeOpacity
                    onGestureFinished()
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }

    private fun dpToPx(dp: Float): Float {
        return dp * resources.displayMetrics.density
    }
}
