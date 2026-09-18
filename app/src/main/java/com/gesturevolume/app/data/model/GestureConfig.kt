package com.gesturevolume.app.data.model

enum class HudPosition {
    TOP,
    CENTER,
    RIGHT,
    LEFT
}

enum class EdgeSide {
    RIGHT,
    LEFT
}

enum class EdgeVerticalPreset {
    TOP,
    UPPER_CENTER,
    CENTER,
    LOWER_CENTER,
    BOTTOM
}

data class GestureConfig(
    val timeoutSeconds: Int = 3,          // Activation timeout in seconds (2, 3, 5, 10)
    val swipeSensitivity: Float = 1.0f,   // Vertical swipe sensitivity multiplier
    val showPercentage: Boolean = true,
    val hapticFeedback: Boolean = true,
    val hudPosition: HudPosition = HudPosition.CENTER,
    val edgeSide: EdgeSide = EdgeSide.RIGHT,
    val edgeOpacity: Float = 0.55f,       // Opacity of the edge trigger handle
    val edgeLengthDp: Int = 120,          // Length of the edge trigger handle
    val edgeYOffsetDp: Int = -120         // Vertical offset from center (Default: -120dp upper center to avoid bottom send buttons/keyboards)
)

