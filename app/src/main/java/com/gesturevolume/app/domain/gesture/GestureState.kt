package com.gesturevolume.app.domain.gesture

sealed interface GestureState {
    data object Idle : GestureState
    data class TrackingCircle(val pointsCount: Int) : GestureState
    data class CircleDetected(val centroidX: Float, val centroidY: Float, val confidence: Float) : GestureState
    data class VolumeGestureActive(val startY: Float, val currentY: Float, val activeSinceMs: Long) : GestureState
    data class AdjustingVolume(val deltaY: Float, val currentVolume: Int, val percentage: Int) : GestureState
    data class HudVisible(val currentVolume: Int, val percentage: Int) : GestureState
}

sealed interface GestureEvent {
    data class TouchDown(val point: TouchPoint) : GestureEvent
    data class TouchMove(val point: TouchPoint) : GestureEvent
    data class TouchUp(val point: TouchPoint) : GestureEvent
    data class CircleRecognized(val result: CircleRecognitionResult) : GestureEvent
    data object TimeoutOccurred : GestureEvent
    data object CancelRequested : GestureEvent
    data object ServiceDisconnected : GestureEvent
}
