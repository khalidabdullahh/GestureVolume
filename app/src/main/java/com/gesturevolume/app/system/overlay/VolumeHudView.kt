package com.gesturevolume.app.system.overlay

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import com.gesturevolume.app.data.model.VolumeState

/**
 * Minimal swipe HUD: no card and no bar, just a transparent percentage with a small
 * up / down arrow showing whether the volume is rising or falling.
 */
class VolumeHudView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private val arrowText: TextView
    private val percentText: TextView
    private var lastPercentage = -1

    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER_HORIZONTAL

        val dp = { value: Float ->
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, resources.displayMetrics)
        }

        arrowText = TextView(context).apply {
            text = " "
            textSize = 13f
            gravity = Gravity.CENTER
            includeFontPadding = false
            setTextColor(Color.argb(230, 255, 255, 255))
            setShadowLayer(dp(5f), 0f, dp(1f), Color.argb(170, 0, 0, 0))
        }

        percentText = TextView(context).apply {
            text = "0%"
            textSize = 40f
            gravity = Gravity.CENTER
            includeFontPadding = false
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setTextColor(Color.WHITE)
            setShadowLayer(dp(9f), 0f, dp(2f), Color.argb(200, 0, 0, 0))
            minWidth = dp(96f).toInt()
        }

        addView(arrowText)
        addView(percentText)
    }

    /** Called when the HUD appears: show the current value without an arrow. */
    fun setInitial(volumeState: VolumeState) {
        lastPercentage = volumeState.percentage
        arrowText.text = " "
        percentText.text = "${volumeState.percentage}%"
    }

    fun updateVolume(volumeState: VolumeState) {
        val p = volumeState.percentage
        if (p == lastPercentage) return

        arrowText.text = if (lastPercentage >= 0 && p < lastPercentage) "\u25BC" else "\u25B2"
        percentText.text = "$p%"

        // Tiny "tick" bump so each change feels alive
        percentText.scaleX = 1.08f
        percentText.scaleY = 1.08f
        percentText.animate().scaleX(1f).scaleY(1f).setDuration(90).start()

        lastPercentage = p
    }
}
