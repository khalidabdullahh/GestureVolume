package com.gesturevolume.app.domain.gesture

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

data class CircleRecognitionResult(
    val isCircle: Boolean,
    val confidence: Float,
    val centroidX: Float = 0f,
    val centroidY: Float = 0f,
    val meanRadius: Float = 0f,
    val reason: String = ""
)

class CircleGestureRecognizer(
    private val minPoints: Int = 10,
    private val minPathLengthPx: Float = 100f,
    private val minRadiusPx: Float = 30f,
    private val maxRadiusPx: Float = 1000f,
    private val minDurationMs: Long = 120L,
    private val maxDurationMs: Long = 3500L
) {

    fun evaluate(rawPoints: List<TouchPoint>, sensitivityThreshold: Float = 0.70f): CircleRecognitionResult {
        if (rawPoints.size < minPoints) {
            return CircleRecognitionResult(false, 0f, reason = "Insufficient points (${rawPoints.size} < $minPoints)")
        }

        val duration = rawPoints.last().timestampMs - rawPoints.first().timestampMs
        if (duration < minDurationMs || duration > maxDurationMs) {
            return CircleRecognitionResult(false, 0f, reason = "Duration out of bounds ($duration ms)")
        }

        // Resample / smooth points for noise reduction
        val points = resamplePoints(rawPoints, targetSpacing = 12f)
        if (points.size < 8) {
            return CircleRecognitionResult(false, 0f, reason = "Resampled points too few (${points.size})")
        }

        // 1. Calculate total path length
        var totalPathLength = 0f
        for (i in 1 until points.size) {
            totalPathLength += points[i].distanceTo(points[i - 1])
        }
        if (totalPathLength < minPathLengthPx) {
            return CircleRecognitionResult(false, 0f, reason = "Path too short ($totalPathLength px)")
        }

        // 2. Bounding Box & Aspect Ratio
        var minX = Float.MAX_VALUE
        var maxX = Float.MIN_VALUE
        var minY = Float.MAX_VALUE
        var maxY = Float.MIN_VALUE

        var sumX = 0f
        var sumY = 0f

        for (p in points) {
            minX = min(minX, p.x)
            maxX = max(maxX, p.x)
            minY = min(minY, p.y)
            maxY = max(maxY, p.y)
            sumX += p.x
            sumY += p.y
        }

        val width = maxX - minX
        val height = maxY - minY
        if (width <= 0f || height <= 0f) {
            return CircleRecognitionResult(false, 0f, reason = "Invalid bounding box dimensions")
        }

        val aspect = min(width, height) / max(width, height)
        if (aspect < 0.50f) {
            return CircleRecognitionResult(false, 0f, reason = "Aspect ratio too low ($aspect)")
        }

        // 3. Centroid & Radius Standard Deviation
        val centroidX = sumX / points.size
        val centroidY = sumY / points.size

        val radii = FloatArray(points.size)
        var sumRadius = 0f
        for (i in points.indices) {
            radii[i] = hypot(points[i].x - centroidX, points[i].y - centroidY)
            sumRadius += radii[i]
        }
        val meanRadius = sumRadius / points.size

        if (meanRadius < minRadiusPx || meanRadius > maxRadiusPx) {
            return CircleRecognitionResult(false, 0f, centroidX, centroidY, meanRadius, "Mean radius out of range ($meanRadius px)")
        }

        var varianceSum = 0f
        for (r in radii) {
            val diff = r - meanRadius
            varianceSum += diff * diff
        }
        val stdDev = sqrt(varianceSum / points.size)
        val radiusCoeffOfVariation = stdDev / meanRadius

        // 4. Start-to-End Closure
        val closureDistance = points.first().distanceTo(points.last())
        val closureRatio = closureDistance / meanRadius // Ideal <= 0.70

        // 5. Angular Sweep (Winding Number) & Direction Consistency
        var totalAngularChange = 0.0
        var positiveSteps = 0
        var negativeSteps = 0

        var prevAngle = atan2((points[0].y - centroidY).toDouble(), (points[0].x - centroidX).toDouble())
        for (i in 1 until points.size) {
            val currAngle = atan2((points[i].y - centroidY).toDouble(), (points[i].x - centroidX).toDouble())
            var delta = currAngle - prevAngle
            // Normalize delta to [-PI, PI]
            while (delta > PI) delta -= 2 * PI
            while (delta < -PI) delta += 2 * PI

            totalAngularChange += delta
            if (delta > 0.02) positiveSteps++
            else if (delta < -0.02) negativeSteps++
            prevAngle = currAngle
        }

        val absAngleChange = abs(totalAngularChange)
        val twoPi = 2 * PI
        // Angular sweep should be close to 2*PI (between ~4.5 rad and ~8.5 rad)
        if (absAngleChange < 4.2 || absAngleChange > 9.0) {
            return CircleRecognitionResult(false, 0f, centroidX, centroidY, meanRadius, "Incomplete loop ($absAngleChange rad)")
        }

        val totalSteps = positiveSteps + negativeSteps
        val directionConsistency = if (totalSteps > 0) {
            max(positiveSteps, negativeSteps).toFloat() / totalSteps.toFloat()
        } else 0f

        if (directionConsistency < 0.70f) {
            return CircleRecognitionResult(false, 0f, centroidX, centroidY, meanRadius, "Direction reversals ($directionConsistency)")
        }

        // 6. Path Length vs Ideal Circumference
        val idealCircumference = (2 * PI * meanRadius).toFloat()
        val pathLengthRatio = totalPathLength / idealCircumference // Ideal 0.85 to 1.45

        // Calculate Sub-Scores (0.0 to 1.0)
        val aspectScore = ((aspect - 0.50f) / 0.50f).coerceIn(0f, 1f)
        val radiusScore = (1f - (radiusCoeffOfVariation / 0.35f)).coerceIn(0f, 1f)
        val closureScore = (1f - (closureRatio / 1.0f)).coerceIn(0f, 1f)
        val angleScore = (1f - (abs(absAngleChange - twoPi) / twoPi).toFloat()).coerceIn(0f, 1f)
        val pathScore = if (pathLengthRatio in 0.7f..1.8f) {
            1f - abs(pathLengthRatio - 1.1f).coerceIn(0f, 1f)
        } else 0f

        val weightedConfidence = (
            aspectScore * 0.20f +
            radiusScore * 0.30f +
            closureScore * 0.20f +
            angleScore * 0.20f +
            pathScore * 0.10f
        ) * directionConsistency

        val isCircle = weightedConfidence >= sensitivityThreshold

        return CircleRecognitionResult(
            isCircle = isCircle,
            confidence = weightedConfidence,
            centroidX = centroidX,
            centroidY = centroidY,
            meanRadius = meanRadius,
            reason = if (isCircle) "Circle recognized" else "Confidence $weightedConfidence below threshold $sensitivityThreshold"
        )
    }

    private fun resamplePoints(points: List<TouchPoint>, targetSpacing: Float): List<TouchPoint> {
        if (points.size <= 1) return points
        val result = mutableListOf<TouchPoint>()
        result.add(points.first())

        var accumulatedDist = 0f
        var lastPoint = points.first()

        for (i in 1 until points.size) {
            val curr = points[i]
            val dist = lastPoint.distanceTo(curr)
            if (dist <= 0f) continue

            if (accumulatedDist + dist >= targetSpacing) {
                val ratio = (targetSpacing - accumulatedDist) / dist
                val nx = lastPoint.x + ratio * (curr.x - lastPoint.x)
                val ny = lastPoint.y + ratio * (curr.y - lastPoint.y)
                val time = (lastPoint.timestampMs + ratio * (curr.timestampMs - lastPoint.timestampMs)).toLong()
                val interpolated = TouchPoint(nx, ny, time)
                result.add(interpolated)
                lastPoint = interpolated
                accumulatedDist = 0f
            } else {
                accumulatedDist += dist
                lastPoint = curr
            }
        }
        if (result.last() != points.last()) {
            result.add(points.last())
        }
        return result
    }
}
