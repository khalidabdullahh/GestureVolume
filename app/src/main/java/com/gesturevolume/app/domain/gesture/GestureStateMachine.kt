package com.gesturevolume.app.domain.gesture

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GestureStateMachine {

    private val _state = MutableStateFlow<GestureState>(GestureState.Idle)
    val state: StateFlow<GestureState> = _state.asStateFlow()

    fun transition(event: GestureEvent): GestureState {
        val current = _state.value
        val next = when (event) {
            is GestureEvent.ServiceDisconnected, is GestureEvent.CancelRequested -> {
                GestureState.Idle
            }
            is GestureEvent.TimeoutOccurred -> {
                GestureState.Idle
            }
            is GestureEvent.TouchDown -> {
                when (current) {
                    is GestureState.Idle, is GestureState.HudVisible -> {
                        GestureState.TrackingCircle(1)
                    }
                    is GestureState.VolumeGestureActive, is GestureState.AdjustingVolume -> {
                        // User touched again while volume gesture active: continue gesture from new position
                        GestureState.VolumeGestureActive(
                            startY = event.point.y,
                            currentY = event.point.y,
                            activeSinceMs = System.currentTimeMillis()
                        )
                    }
                    else -> GestureState.TrackingCircle(1)
                }
            }
            is GestureEvent.TouchMove -> {
                when (current) {
                    is GestureState.TrackingCircle -> {
                        GestureState.TrackingCircle(current.pointsCount + 1)
                    }
                    is GestureState.VolumeGestureActive -> {
                        val deltaY = current.startY - event.point.y // positive = upward = increase
                        GestureState.VolumeGestureActive(
                            startY = current.startY,
                            currentY = event.point.y,
                            activeSinceMs = System.currentTimeMillis()
                        )
                    }
                    is GestureState.AdjustingVolume -> {
                        GestureState.VolumeGestureActive(
                            startY = event.point.y,
                            currentY = event.point.y,
                            activeSinceMs = System.currentTimeMillis()
                        )
                    }
                    else -> current
                }
            }
            is GestureEvent.CircleRecognized -> {
                if (event.result.isCircle) {
                    GestureState.CircleDetected(
                        centroidX = event.result.centroidX,
                        centroidY = event.result.centroidY,
                        confidence = event.result.confidence
                    )
                } else {
                    GestureState.Idle
                }
            }
            is GestureEvent.TouchUp -> {
                when (current) {
                    is GestureState.CircleDetected, is GestureState.VolumeGestureActive, is GestureState.AdjustingVolume -> {
                        GestureState.HudVisible(currentVolume = 0, percentage = 0)
                    }
                    is GestureState.TrackingCircle -> {
                        GestureState.Idle
                    }
                    else -> current
                }
            }
        }
        _state.value = next
        return next
    }

    fun setState(newState: GestureState) {
        _state.value = newState
    }

    fun reset() {
        _state.value = GestureState.Idle
    }
}
