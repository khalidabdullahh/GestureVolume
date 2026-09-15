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

data class GestureConfig(
    val circleSensitivity: Float = 0.70f, // Confidence threshold (0.50 to 0.90)
    val timeoutSeconds: Int = 3,          // Activation timeout in seconds (2, 3, 5, 10)
    val swipeSensitivity: Float = 1.0f,   // Vertical swipe sensitivity multiplier
    val showPercentage: Boolean = true,
    val hapticFeedback: Boolean = true,
    val hudPosition: HudPosition = HudPosition.CENTER,
    val edgeSide: EdgeSide = EdgeSide.RIGHT,
    val edgeOpacity: Float = 0.55f,       // Opacity of the edge trigger handle
    val edgeLengthDp: Int = 120,          // Length of the edge trigger handle
    val edgeYOffsetDp: Int = 0            // Vertical offset from center
)

