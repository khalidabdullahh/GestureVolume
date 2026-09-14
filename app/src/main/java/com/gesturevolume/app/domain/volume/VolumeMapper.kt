package com.gesturevolume.app.domain.volume

import kotlin.math.abs

class VolumeMapper(
    private val pixelThresholdPerStep: Float = 60f // Distance in pixels to trigger 1 step change
) {

    /**
     * Translates a vertical delta in pixels (positive = swipe up, negative = swipe down)
     * into a volume level adjustment delta.
     */
    fun calculateDeltaLevel(
        deltaY: Float,
        sensitivityMultiplier: Float = 1.0f
    ): Int {
        val effectiveThreshold = (pixelThresholdPerStep / sensitivityMultiplier.coerceAtLeast(0.1f))
        if (abs(deltaY) < effectiveThreshold) {
            return 0
        }
        val steps = (deltaY / effectiveThreshold).toInt()
        return steps
    }

    /**
     * Translates accumulated vertical distance into percentage change (0-100).
     */
    fun calculatePercentageDelta(
        deltaY: Float,
        screenHeightPx: Float,
        sensitivityMultiplier: Float = 1.0f
    ): Int {
        val totalRange = screenHeightPx * 0.40f // Full volume span across ~40% of screen height
        val ratio = (deltaY / totalRange) * sensitivityMultiplier
        return (ratio * 100).toInt()
    }
}
