package com.gesturevolume.app.system.overlay

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.gesturevolume.app.R
import com.gesturevolume.app.data.model.VolumeState

class VolumeProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#2E334D")
        style = Paint.Style.FILL
    }

    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#6C5CE7")
        style = Paint.Style.FILL
    }

    private var currentProgress = 0f
    private val rect = RectF()
    private val progressRect = RectF()

    fun setProgress(progress: Float, animated: Boolean = true) {
        val target = progress.coerceIn(0f, 1f)
        if (animated) {
            val animator = ObjectAnimator.ofFloat(this, "animProgress", currentProgress, target)
            animator.duration = 120
            animator.interpolator = DecelerateInterpolator()
            animator.start()
        } else {
            currentProgress = target
            invalidate()
        }
    }

    @Suppress("unused")
    fun setAnimProgress(value: Float) {
        currentProgress = value
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cornerRadius = height / 2f
        rect.set(0f, 0f, width.toFloat(), height.toFloat())
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, bgPaint)

        val progressWidth = width * currentProgress
        if (progressWidth > 0f) {
            progressRect.set(0f, 0f, progressWidth, height.toFloat())
            canvas.drawRoundRect(progressRect, cornerRadius, cornerRadius, progressPaint)
        }
    }
}

class VolumeHudView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val iconView: ImageView
    private val percentText: TextView
    private val titleText: TextView
    private val progressBar: VolumeProgressBar
    private val cardContainer: LinearLayout

    init {
        val dp = { value: Float ->
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, resources.displayMetrics).toInt()
        }

        // Card Container
        cardContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18f), dp(14f), dp(18f), dp(16f))
            gravity = Gravity.CENTER_HORIZONTAL

            val bg = GradientDrawable().apply {
                setColor(Color.parseColor("#1C1F2E"))
                cornerRadius = dp(20f).toFloat()
                setStroke(dp(1.5f), Color.parseColor("#323854"))
            }
            background = bg
            elevation = dp(8f).toFloat()
        }

        val topRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        iconView = ImageView(context).apply {
            val size = dp(24f)
            layoutParams = LinearLayout.LayoutParams(size, size).apply {
                marginEnd = dp(10f)
            }
            setColorFilter(Color.parseColor("#00CEC9"))
            setImageResource(android.R.drawable.ic_lock_silent_mode_off)
        }

        titleText = TextView(context).apply {
            text = "Media Volume"
            setTextColor(Color.parseColor("#E2E8F0"))
            textSize = 14f
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        percentText = TextView(context).apply {
            text = "0%"
            setTextColor(Color.parseColor("#6C5CE7"))
            textSize = 15f
            paint.isFakeBoldText = true
        }

        topRow.addView(iconView)
        topRow.addView(titleText)
        topRow.addView(percentText)

        progressBar = VolumeProgressBar(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(10f)
            ).apply {
                topMargin = dp(12f)
            }
        }

        cardContainer.addView(topRow)
        cardContainer.addView(progressBar)

        val cardLayoutParams = LayoutParams(dp(260f), LayoutParams.WRAP_CONTENT).apply {
            gravity = Gravity.CENTER
        }
        addView(cardContainer, cardLayoutParams)
    }

    fun updateVolume(volumeState: VolumeState, showPercentage: Boolean) {
        percentText.visibility = if (showPercentage) View.VISIBLE else View.GONE
        percentText.text = "${volumeState.percentage}%"

        if (volumeState.isMuted || volumeState.percentage == 0) {
            iconView.setImageResource(android.R.drawable.ic_lock_silent_mode)
            iconView.setColorFilter(Color.parseColor("#A0A5BD"))
        } else {
            iconView.setImageResource(android.R.drawable.ic_lock_silent_mode_off)
            iconView.setColorFilter(Color.parseColor("#00CEC9"))
        }

        progressBar.setProgress(volumeState.progress, animated = true)
    }
}
