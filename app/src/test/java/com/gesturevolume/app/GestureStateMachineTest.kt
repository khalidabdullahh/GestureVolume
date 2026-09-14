package com.gesturevolume.app

import com.gesturevolume.app.domain.gesture.CircleRecognitionResult
import com.gesturevolume.app.domain.gesture.GestureEvent
import com.gesturevolume.app.domain.gesture.GestureState
import com.gesturevolume.app.domain.gesture.GestureStateMachine
import com.gesturevolume.app.domain.gesture.TouchPoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GestureStateMachineTest {

    private lateinit var stateMachine: GestureStateMachine

    @Before
    fun setUp() {
        stateMachine = GestureStateMachine()
    }

    @Test
    fun testInitialState_isIdle() {
        assertEquals(GestureState.Idle, stateMachine.state.value)
    }

    @Test
    fun testTouchDown_transitionsToTracking() {
        val next = stateMachine.transition(GestureEvent.TouchDown(TouchPoint(100f, 100f)))
        assertTrue(next is GestureState.TrackingCircle)
    }

    @Test
    fun testCircleRecognized_transitionsToCircleDetected() {
        stateMachine.transition(GestureEvent.TouchDown(TouchPoint(100f, 100f)))
        val recognized = stateMachine.transition(
            GestureEvent.CircleRecognized(
                CircleRecognitionResult(
                    isCircle = true,
                    confidence = 0.85f,
                    centroidX = 150f,
                    centroidY = 150f,
                    meanRadius = 80f
                )
            )
        )
        assertTrue(recognized is GestureState.CircleDetected)
    }

    @Test
    fun testTimeout_transitionsToIdle() {
        stateMachine.transition(GestureEvent.TouchDown(TouchPoint(100f, 100f)))
        val next = stateMachine.transition(GestureEvent.TimeoutOccurred)
        assertEquals(GestureState.Idle, next)
    }

    @Test
    fun testServiceDisconnected_resetsToIdle() {
        stateMachine.setState(GestureState.VolumeGestureActive(200f, 250f, 1000L))
        val next = stateMachine.transition(GestureEvent.ServiceDisconnected)
        assertEquals(GestureState.Idle, next)
    }

    @Test
    fun testCancelRequested_resetsToIdle() {
        stateMachine.setState(GestureState.VolumeGestureActive(200f, 250f, 1000L))
        val next = stateMachine.transition(GestureEvent.CancelRequested)
        assertEquals(GestureState.Idle, next)
    }
}
