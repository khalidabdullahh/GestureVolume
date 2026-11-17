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
import android.view.ViewConfiguration
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import com.gesturevolume.app.data.model.EdgeSide
import com.gesturevolume.app.data.model.GestureConfig
import com.gesturevolume.app.data.model.HandleColorMode
import kotlin.math.abs

private const val DOUBLE_TAP_WINDOW_MS = 200L

@SuppressLint("ViewConstructor")
class EdgeHandleView(
    context: Context,
    private val onVolumeSwipe: (deltaFraction: Float) -> Unit,
    private val onShowHudRequested: () -> Unit,
    private val onGestureFinished: () -> Unit,
    private val onTap: () -> Unit,
    private val onDoubleTap: () -> Unit,
    private val onLongPress: () -> Unit
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
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val pillRect = RectF()
    private val glowRect = RectF()

    private var currentConfig = GestureConfig()
    private var startY = 0f
    private var isInteracting = false
    private var downRawY = 0f
    private var isDragging = false
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private var pendingTap: Runnable? = null
    private var longPressFired = false

    // Smart adaptive colour: true once the service can measure the background brightness (Android 11+)
    private var smartAdaptive = false
    private var toneProgress = 1f // 0 = black handle (light background), 1 = white handle (dark background)
    private var toneAnimator: ValueAnimator? = null

    fun setAdaptiveSampling(enabled: Boolean) {
        if (smartAdaptive == enabled) return
        smartAdaptive = enabled
        invalidate()
    }

    fun setBackgroundIsLight(isLight: Boolean) {
        val target = if (isLight) 0f else 1f
        toneAnimator?.cancel()
        toneAnimator = ValueAnimator.ofFloat(toneProgress, target).apply {
            duration = 260
            addUpdateListener {
                toneProgress = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    // Smoothly fades the handle away, then runs the action (used right before the screen is locked)
    private fun fadeOutThen(action: () -> Unit) {
        animateExpansion(0f, durationMs = 160)
        animate().cancel()
        animate().alpha(0f).setDuration(160).withEndAction { action() }.start()
    }

    // Brings the handle back after a fade-out (screen on again, or the lock did not happen)
    fun restoreAfterFade() {
        animate().cancel()
        visibility = VISIBLE
        isInteracting = false
        animateExpansion(0f, durationMs = 200)
        alpha = currentConfig.edgeOpacity
    }

    // Press & hold (without moving) -> turn the screen off
    private val longPressRunnable = Runnable {
        if (!isFingerDown || isDragging || !currentConfig.longPressScreenOff) return@Runnable
        longPressFired = true
        performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
        // Fade the handle out first so there is no bright flash right before the screen turns off
        fadeOutThen { onLongPress() }
    }

    // A single tap waits briefly so a second tap can turn it into a double tap (screenshot)
    private fun handleTap() {
        if (!currentConfig.doubleTapScreenshot) {
            onTap()
            return
        }
        val pending = pendingTap
        if (pending != null) {
            removeCallbacks(pending)
            pendingTap = null
            onDoubleTap()
        } else {
            val task = Runnable {
                pendingTap = null
                onTap()
            }
            pendingTap = task
            postDelayed(task, DOUBLE_TAP_WINDOW_MS)
        }
    }

    // Returns the handle to its compact inactive state a few seconds after the last use
    private var isFingerDown = false

    private val collapseRunnable = Runnable {
        if (isFingerDown) return@Runnable // never collapse in the middle of a gesture
        isInteracting = false
        animateExpansion(0.0f, durationMs = 260)
        alpha = currentConfig.edgeOpacity
    }

    private fun scheduleCollapse() {
        removeCallbacks(collapseRunnable)
        postDelayed(collapseRunnable, currentConfig.timeoutSeconds.coerceAtLeast(1) * 1000L)
    }

    override fun onDetachedFromWindow() {
        removeCallbacks(collapseRunnable)
        removeCallbacks(longPressRunnable)
        pendingTap?.let { removeCallbacks(it) }
        pendingTap = null
        expandAnimator?.cancel()
        toneAnimator?.cancel()
        super.onDetachedFromWindow()
    }

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

    private fun animateExpansion(target: Float, durationMs: Long = 220) {
        expandAnimator?.cancel()
        expandAnimator = ValueAnimator.ofFloat(expandProgress, target).apply {
            duration = durationMs
            // Springy overshoot when waking up, smooth ease when going back to idle
            interpolator = if (target > expandProgress) OvershootInterpolator(1.6f) else DecelerateInterpolator(1.4f)
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
        val currentWidth = (idleWidth + (activeWidth - idleWidth) * expandProgress)
            .coerceIn(idleWidth * 0.8f, fullWidth - dpToPx(2f))

        val idleHeight = dpToPx(40f)
        val activeHeight = dpToPx(configSafeHeightDp()).coerceAtMost(fullHeight - dpToPx(4f))
        val currentHeight = (idleHeight + (activeHeight - idleHeight) * expandProgress)
            .coerceIn(idleHeight * 0.8f, fullHeight - dpToPx(2f))

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

        // ---- Colours: CUSTOM = one fixed colour, ADAPTIVE = light fill + dark outline (visible on any background)
        val adaptive = currentConfig.handleColorMode == HandleColorMode.ADAPTIVE
        // smart = real adaptive: black handle on light backgrounds, white handle on dark backgrounds
        val smart = adaptive && smartAdaptive
        val baseColor = when {
            smart -> {
                val v = (18 + (255 - 18) * toneProgress).toInt().coerceIn(0, 255)
                Color.rgb(v, v, v)
            }
            adaptive -> Color.WHITE
            else -> currentConfig.handleColor
        }
        val r = Color.red(baseColor)
        val g = Color.green(baseColor)
        val b = Color.blue(baseColor)
        val lum = (0.299f * r + 0.587f * g + 0.114f * b) / 255f
        val twoTone = adaptive && !smart // fallback for Android 10 and older: light fill + dark outline
        val outlineR: Int; val outlineG: Int; val outlineB: Int
        if (twoTone || lum > 0.5f) { outlineR = 15; outlineG = 23; outlineB = 42 } else { outlineR = 255; outlineG = 255; outlineB = 255 }

        // Soft glow behind the pill while it is awake
        val glowAlpha = (50 * expandProgress).toInt().coerceIn(0, 255)
        if (glowAlpha > 0) {
            if (twoTone) glowPaint.color = Color.argb(glowAlpha, outlineR, outlineG, outlineB)
            else glowPaint.color = Color.argb(glowAlpha, r, g, b)
            val gl = dpToPx(2.5f)
            glowRect.set(pillRect.left - gl, pillRect.top - gl, pillRect.right + gl, pillRect.bottom + gl)
            canvas.drawRoundRect(glowRect, cornerRadius + gl, cornerRadius + gl, glowPaint)
        }

        // Pill fill
        val baseAlpha = (160 + (85 * expandProgress)).toInt().coerceIn(0, 255)
        pillPaint.color = Color.argb(baseAlpha, r, g, b)
        canvas.drawRoundRect(pillRect, cornerRadius, cornerRadius, pillPaint)

        // Outline: strong only in the two-tone fallback, none in smart adaptive mode
        if (!smart) {
            val borderAlpha = if (twoTone) (190 + (65 * expandProgress)).toInt().coerceIn(0, 255)
                              else (60 + (90 * expandProgress)).toInt().coerceIn(0, 255)
            borderPaint.strokeWidth = dpToPx(if (twoTone) 1.5f else 1f)
            borderPaint.color = Color.argb(borderAlpha, outlineR, outlineG, outlineB)
            canvas.drawRoundRect(pillRect, cornerRadius, cornerRadius, borderPaint)
        }

        // Draw tactile center notches (fades in as it expands)
        if (expandProgress > 0.25f) {
            val notchAlpha = ((expandProgress - 0.25f) / 0.75f * 200).toInt().coerceIn(0, 255)
            notchPaint.color = Color.argb(notchAlpha, outlineR, outlineG, outlineB) // Always contrasts with the fill

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
                removeCallbacks(collapseRunnable)
                isFingerDown = true
                isInteracting = true
                isDragging = false
                longPressFired = false
                downRawY = event.rawY
                startY = event.rawY
                alpha = 1.0f
                // Instant, springy feedback the moment the finger lands
                animateExpansion(1.0f, durationMs = 220)
                if (currentConfig.longPressScreenOff) {
                    postDelayed(longPressRunnable, ViewConfiguration.getLongPressTimeout().toLong())
                }
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                if (!isInteracting) return super.onTouchEvent(event)

                val currentY = event.rawY
                if (!isDragging) {
                    if (abs(currentY - downRawY) < touchSlop) return true
                    // Finger moved far enough: this is a swipe, not a tap
                    isDragging = true
                    removeCallbacks(longPressRunnable)
                    startY = currentY
                    onShowHudRequested()
                    return true
                }

                // Continuous mapping: a swipe over ~42% of the screen height covers the full volume range
                val deltaY = startY - currentY // upward = positive
                startY = currentY
                val sens = currentConfig.swipeSensitivity.coerceIn(0.4f, 3.0f)
                val fullRangePx = resources.displayMetrics.heightPixels * 0.42f / sens
                if (deltaY != 0f) {
                    onVolumeSwipe(deltaY / fullRangePx)
                }
                return true
            }

            MotionEvent.ACTION_UP -> {
                isFingerDown = false
                removeCallbacks(longPressRunnable)
                if (isInteracting) {
                    if (longPressFired) {
                        longPressFired = false // long press already handled, no tap
                    } else if (isDragging) {
                        onGestureFinished()
                    } else {
                        performClick()
                        handleTap()
                    }
                    isDragging = false
                    scheduleCollapse()
                    return true
                }
            }

            MotionEvent.ACTION_CANCEL -> {
                isFingerDown = false
                removeCallbacks(longPressRunnable)
                longPressFired = false
                if (isInteracting) {
                    isDragging = false
                    removeCallbacks(collapseRunnable)
                    collapseRunnable.run()
                    onGestureFinished()
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    private fun dpToPx(dp: Float): Float {
        return dp * resources.displayMetrics.density
    }
}
