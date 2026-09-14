package com.gesturevolume.app.domain.gesture

import kotlin.math.hypot

data class TouchPoint(
    val x: Float,
    val y: Float,
    val timestampMs: Long = System.currentTimeMillis()
) {
    fun distanceTo(other: TouchPoint): Float {
        return hypot(x - other.x, y - other.y)
    }
}
