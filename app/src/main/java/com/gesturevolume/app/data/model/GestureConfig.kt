package com.gesturevolume.app.data.model

enum class HudPosition {
    TOP,
    CENTER,
    RIGHT,
    LEFT
}

enum class HudPlacement {
    BESIDE_HANDLE,
    CENTER,
    TOP
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

enum class HandleColorMode {
    CUSTOM,   // one fixed colour picked by the user
    ADAPTIVE  // two-tone handle (light fill + dark outline) that stays visible on white AND black backgrounds
}

data class GestureConfig(
    val timeoutSeconds: Int = 3,          // Activation timeout in seconds (2, 3, 5, 10)
    val swipeSensitivity: Float = 1.0f,   // Vertical swipe sensitivity multiplier
    val showPercentage: Boolean = true,
    val hapticFeedback: Boolean = true,
    val doubleTapScreenshot: Boolean = true, // Double-tap the handle to take a screenshot (Android 9+)
    val hudPosition: HudPosition = HudPosition.CENTER,
    val hudPlacement: HudPlacement = HudPlacement.BESIDE_HANDLE, // Where the swipe percentage appears
    val hudSizeSp: Int = 40,              // Size of the swipe percentage text
    val hudOffsetXDp: Int = 0,            // Extra horizontal shift of the percentage
    val hudOffsetYDp: Int = 0,            // Extra vertical shift of the percentage
    val longPressScreenOff: Boolean = true, // Press & hold the handle to turn the screen off (Android 9+)
    val handleColorMode: HandleColorMode = HandleColorMode.ADAPTIVE,
    val handleColor: Int = 0xFFFFFFFF.toInt(), // ARGB colour used in CUSTOM mode
    val edgeSide: EdgeSide = EdgeSide.RIGHT,
    val edgeOpacity: Float = 0.55f,       // Opacity of the edge trigger handle
    val edgeLengthDp: Int = 120,          // Length of the edge trigger handle
    val edgeYOffsetDp: Int = -120         // Vertical offset from center (Default: -120dp upper center to avoid bottom send buttons/keyboards)
)

