package com.gesturevolume.app.ui.test

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gesturevolume.app.data.model.VolumeState
import com.gesturevolume.app.domain.gesture.CircleGestureRecognizer
import com.gesturevolume.app.domain.gesture.TouchPoint
import com.gesturevolume.app.domain.volume.VolumeController
import com.gesturevolume.app.domain.volume.VolumeMapper
import com.gesturevolume.app.ui.theme.AccentCyan
import com.gesturevolume.app.ui.theme.DarkBackground
import com.gesturevolume.app.ui.theme.DarkSurface
import com.gesturevolume.app.ui.theme.DarkSurfaceVariant
import com.gesturevolume.app.ui.theme.DividerColor
import com.gesturevolume.app.ui.theme.PrimaryPurple
import com.gesturevolume.app.ui.theme.SuccessGreen
import com.gesturevolume.app.ui.theme.TextPrimary
import com.gesturevolume.app.ui.theme.TextSecondary
import com.gesturevolume.app.ui.theme.WarningAmber

@Composable
fun TestGestureScreen(
    volumeController: VolumeController,
    onBack: () -> Unit
) {
    val recognizer = remember { CircleGestureRecognizer() }
    val volumeMapper = remember { VolumeMapper() }

    val rawPoints = remember { mutableStateListOf<TouchPoint>() }
    var isCircleDetected by remember { mutableStateOf(false) }
    var confidenceScore by remember { mutableFloatStateOf(0f) }
    var statusMessage by remember { mutableStateOf("Draw a circle on the canvas below") }

    var currentVolumeState by remember { mutableStateOf(volumeController.refreshCurrentVolume()) }
    var gestureStartY by remember { mutableFloatStateOf(0f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Test Gesture Sandbox",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            IconButton(onClick = {
                rawPoints.clear()
                isCircleDetected = false
                confidenceScore = 0f
                statusMessage = "Draw a circle on the canvas below"
            }) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset Canvas",
                    tint = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isCircleDetected) SuccessGreen else DividerColor
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = statusMessage,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isCircleDetected) SuccessGreen else TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isCircleDetected) "Swipe UP/DOWN to adjust volume" else "Try drawing a smooth single loop",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                if (confidenceScore > 0f) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isCircleDetected) SuccessGreen.copy(alpha = 0.2f) else DarkSurfaceVariant)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${(confidenceScore * 100).toInt()}% match",
                            color = if (isCircleDetected) SuccessGreen else WarningAmber,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Interactive Drawing Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(20.dp))
                .background(DarkSurface)
                .border(2.dp, if (isCircleDetected) PrimaryPurple else DividerColor, RoundedCornerShape(20.dp))
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            if (isCircleDetected) {
                                gestureStartY = offset.y
                            } else {
                                rawPoints.clear()
                                rawPoints.add(TouchPoint(offset.x, offset.y, System.currentTimeMillis()))
                            }
                        },
                        onDrag = { change, _ ->
                            val currentPos = change.position
                            if (isCircleDetected) {
                                val deltaY = gestureStartY - currentPos.y
                                val steps = volumeMapper.calculateDeltaLevel(deltaY, 1.0f)
                                if (steps != 0) {
                                    currentVolumeState = volumeController.adjustVolume(steps)
                                    gestureStartY = currentPos.y
                                }
                            } else {
                                rawPoints.add(TouchPoint(currentPos.x, currentPos.y, System.currentTimeMillis()))
                                if (rawPoints.size >= 10) {
                                    val result = recognizer.evaluate(rawPoints, 0.70f)
                                    confidenceScore = result.confidence
                                    if (result.isCircle) {
                                        isCircleDetected = true
                                        statusMessage = "Circle Detected! (${(result.confidence * 100).toInt()}%)"
                                        gestureStartY = currentPos.y
                                    }
                                }
                            }
                        },
                        onDragEnd = {
                            if (!isCircleDetected) {
                                val result = recognizer.evaluate(rawPoints, 0.70f)
                                confidenceScore = result.confidence
                                if (result.isCircle) {
                                    isCircleDetected = true
                                    statusMessage = "Circle Detected! (${(result.confidence * 100).toInt()}%)"
                                } else {
                                    statusMessage = "Not recognized: ${result.reason}"
                                }
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            // Path Drawing
            Canvas(modifier = Modifier.fillMaxSize()) {
                if (rawPoints.size > 1) {
                    val path = Path().apply {
                        moveTo(rawPoints[0].x, rawPoints[0].y)
                        for (i in 1 until rawPoints.size) {
                            lineTo(rawPoints[i].x, rawPoints[i].y)
                        }
                    }
                    drawPath(
                        path = path,
                        color = if (isCircleDetected) AccentCyan else PrimaryPurple,
                        style = Stroke(
                            width = 6.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }

            // Simulated In-Screen Volume HUD when circle recognized
            if (isCircleDetected) {
                SimulatedVolumeHudCard(currentVolumeState)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Button(
            onClick = {
                rawPoints.clear()
                isCircleDetected = false
                confidenceScore = 0f
                statusMessage = "Draw a circle on the canvas below"
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Clear & Try Again",
                color = TextPrimary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun SimulatedVolumeHudCard(volumeState: VolumeState) {
    Card(
        modifier = Modifier
            .width(260.dp)
            .padding(16.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1F2E)),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF323854)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = null,
                        tint = AccentCyan,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Media Volume",
                        color = TextPrimary,
                        fontSize = 14.sp
                    )
                }
                Text(
                    text = "${volumeState.percentage}%",
                    color = PrimaryPurple,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { volumeState.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = PrimaryPurple,
                trackColor = DarkSurfaceVariant
            )
        }
    }
}

