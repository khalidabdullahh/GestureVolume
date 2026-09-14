package com.gesturevolume.app

import com.gesturevolume.app.domain.gesture.CircleGestureRecognizer
import com.gesturevolume.app.domain.gesture.TouchPoint
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class CircleGestureRecognizerTest {

    private lateinit var recognizer: CircleGestureRecognizer

    @Before
    fun setUp() {
        recognizer = CircleGestureRecognizer()
    }

    private fun generateCirclePoints(
        centerX: Float = 500f,
        centerY: Float = 500f,
        radius: Float = 120f,
        clockwise: Boolean = true,
        numPoints: Int = 40,
        noiseScale: Float = 0f,
        durationMs: Long = 600L
    ): List<TouchPoint> {
        val points = mutableListOf<TouchPoint>()
        val startMs = 1000L
        for (i in 0..numPoints) {
            val progress = i.toFloat() / numPoints.toFloat()
            val angle = (if (clockwise) progress else -progress) * 2.0 * PI
            val noiseX = if (noiseScale > 0) (Math.random().toFloat() - 0.5f) * noiseScale else 0f
            val noiseY = if (noiseScale > 0) (Math.random().toFloat() - 0.5f) * noiseScale else 0f
            val x = (centerX + radius * cos(angle)).toFloat() + noiseX
            val y = (centerY + radius * sin(angle)).toFloat() + noiseY
            val time = startMs + (progress * durationMs).toLong()
            points.add(TouchPoint(x, y, time))
        }
        return points
    }

    @Test
    fun testValidClockwiseCircle_isRecognized() {
        val points = generateCirclePoints(clockwise = true, radius = 150f)
        val result = recognizer.evaluate(points, sensitivityThreshold = 0.70f)
        assertTrue("Expected valid clockwise circle to be recognized, but got: ${result.reason}", result.isCircle)
        assertTrue(result.confidence >= 0.70f)
    }

    @Test
    fun testValidCounterClockwiseCircle_isRecognized() {
        val points = generateCirclePoints(clockwise = false, radius = 150f)
        val result = recognizer.evaluate(points, sensitivityThreshold = 0.70f)
        assertTrue("Expected valid counter-clockwise circle to be recognized, but got: ${result.reason}", result.isCircle)
        assertTrue(result.confidence >= 0.70f)
    }

    @Test
    fun testNoisyCircle_isRecognized() {
        val points = generateCirclePoints(clockwise = true, radius = 140f, noiseScale = 12f)
        val result = recognizer.evaluate(points, sensitivityThreshold = 0.65f)
        assertTrue("Expected noisy human circle to be recognized, got: ${result.reason}", result.isCircle)
    }

    @Test
    fun testStraightHorizontalLine_isRejected() {
        val points = mutableListOf<TouchPoint>()
        for (i in 0..30) {
            points.add(TouchPoint(100f + i * 15f, 300f, 1000L + i * 20L))
        }
        val result = recognizer.evaluate(points, sensitivityThreshold = 0.70f)
        assertFalse("Straight horizontal line must NOT be recognized as circle", result.isCircle)
    }

    @Test
    fun testStraightVerticalLine_isRejected() {
        val points = mutableListOf<TouchPoint>()
        for (i in 0..30) {
            points.add(TouchPoint(300f, 100f + i * 15f, 1000L + i * 20L))
        }
        val result = recognizer.evaluate(points, sensitivityThreshold = 0.70f)
        assertFalse("Straight vertical line must NOT be recognized as circle", result.isCircle)
    }

    @Test
    fun testIncompleteArc_isRejected() {
        val points = mutableListOf<TouchPoint>()
        // Only 120 degrees arc
        for (i in 0..20) {
            val angle = (i.toFloat() / 20f) * (2.0 * PI / 3.0)
            val x = (400f + 120f * cos(angle)).toFloat()
            val y = (400f + 120f * sin(angle)).toFloat()
            points.add(TouchPoint(x, y, 1000L + i * 25L))
        }
        val result = recognizer.evaluate(points, sensitivityThreshold = 0.70f)
        assertFalse("Incomplete 120-degree arc must NOT be recognized as circle", result.isCircle)
    }

    @Test
    fun testZigZagScribble_isRejected() {
        val points = mutableListOf<TouchPoint>()
        var y = 200f
        for (i in 0..30) {
            val x = if (i % 2 == 0) 100f else 400f
            y += 15f
            points.add(TouchPoint(x, y, 1000L + i * 20L))
        }
        val result = recognizer.evaluate(points, sensitivityThreshold = 0.70f)
        assertFalse("Zig-zag scribble with frequent reversals must NOT be recognized as circle", result.isCircle)
    }

    @Test
    fun testMicroTap_isRejected() {
        val points = listOf(
            TouchPoint(200f, 200f, 1000L),
            TouchPoint(201f, 201f, 1020L),
            TouchPoint(200f, 202f, 1040L)
        )
        val result = recognizer.evaluate(points, sensitivityThreshold = 0.70f)
        assertFalse("Micro-tap with few points must be rejected", result.isCircle)
    }
}
