package com.gesturevolume.app.system.overlay

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
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

    private val pillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }
    private val notchPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val pillRect = RectF()

    private var currentConfig = GestureConfig()
    private var startY = 0f
    private var accumulatedDeltaY = 0f
    private var isInteracting = false

    // 0f = Idle 1/3 compact size, 1f = Active expanded full size
    private var expandProgress = 0f
    private var expandAnimator: ValueAnimator? = null

    init {
        notchPaint.strokeWidth = dpToPx(2f)
        borderPaint.strokeWidth = dpToPx(1f)
    }

    fun updateConfig(config: GestureConfig) {
        this.currentConfig = config
        alpha = if (isInteracting) 1.0f else config.edgeOpacity
        invalidate()
    }

    private fun animateExpansion(target: Float, durationMs: Long = 180) {
        expandAnimator?.cancel()
        expandAnimator = ValueAnimator.ofFloat(expandProgress, target).apply {
            duration = durationMs
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                expandProgress = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val fullWidth = width.toFloat()
        val fullHeight = height.toFloat()

        // 1/3 size in idle state: ~7dp width, ~40dp height (1/3 of 120dp)
        // Full size in active state: ~24dp width, ~114dp height
        val idleWidth = dpToPx(7f)
        val activeWidth = dpToPx(24f).coerceAtMost(fullWidth - dpToPx(2f))
        val currentWidth = idleWidth + (activeWidth - idleWidth) * expandProgress

        val idleHeight = dpToPx(40f)
        val activeHeight = dpToPx(configSafeHeightDp()).coerceAtMost(fullHeight - dpToPx(4f))
        val currentHeight = idleHeight + (activeHeight - idleHeight) * expandProgress

        val top = (fullHeight - currentHeight) / 2f
        val bottom = (fullHeight + currentHeight) / 2f

        val isRight = currentConfig.edgeSide == EdgeSide.RIGHT
        val inset = dpToPx(1f)

        if (isRight) {
            pillRect.set(fullWidth - currentWidth - inset, top, fullWidth - inset, bottom)
        } else {
            pillRect.set(inset, top, currentWidth + inset, bottom)
        }

        val cornerRadius = currentWidth / 2f

        // Translucent frosted white pill
        val baseAlpha = (160 + (85 * expandProgress)).toInt().coerceIn(0, 255)
        pillPaint.color = Color.argb(baseAlpha, 255, 255, 255)
        canvas.drawRoundRect(pillRect, cornerRadius, cornerRadius, pillPaint)

        // Subtle glowing border
        val borderAlpha = (60 + (90 * expandProgress)).toInt().coerceIn(0, 255)
        borderPaint.color = Color.argb(borderAlpha, 255, 255, 255)
        canvas.drawRoundRect(pillRect, cornerRadius, cornerRadius, borderPaint)

        // Draw tactile center notches (fades in as it expands)
        if (expandProgress > 0.25f) {
            val notchAlpha = ((expandProgress - 0.25f) / 0.75f * 200).toInt().coerceIn(0, 255)
            notchPaint.color = Color.argb(notchAlpha, 30, 41, 59) // Dark slate for clean contrast on white pill

            val cy = fullHeight / 2f
            val notchLen = dpToPx(7f)
            val notchSpacing = dpToPx(5.5f)
            val notchX = pillRect.centerX()

            // Center notch
            canvas.drawLine(notchX - notchLen / 2, cy, notchX + notchLen / 2, cy, notchPaint)
            // Top notch
            canvas.drawLine(notchX - notchLen / 2, cy - notchSpacing, notchX + notchLen / 2, cy - notchSpacing, notchPaint)
            // Bottom notch
            canvas.drawLine(notchX - notchLen / 2, cy + notchSpacing, notchX + notchLen / 2, cy + notchSpacing, notchPaint)
        }
    }

    private fun configSafeHeightDp(): Float {
        return (currentConfig.edgeLengthDp.toFloat() - 6f).coerceAtLeast(80f)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                isInteracting = true
                startY = event.rawY
                accumulatedDeltaY = 0f
                alpha = 1.0f
                animateExpansion(1.0f, durationMs = 160)
                onShowHudRequested()
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                if (isInteracting) {
                    val currentY = event.rawY
                    val deltaY = startY - currentY // Upward = positive (volume up)
                    accumulatedDeltaY += deltaY
                    startY = currentY

                    // Responsive tactile step calculation
                    val sens = currentConfig.swipeSensitivity.coerceIn(0.4f, 3.0f)
                    val thresholdPx = dpToPx(11f) / sens
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
                    animateExpansion(0.0f, durationMs = 240)
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
