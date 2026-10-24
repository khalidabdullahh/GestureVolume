package com.gesturevolume.app.ui.test

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import androidx.compose.material.icons.filled.TouchApp
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gesturevolume.app.data.model.VolumeState
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
import kotlin.math.abs
import kotlinx.coroutines.delay

private const val INACTIVE_AFTER_SECONDS = 3

@Composable
fun TestGestureScreen(
    volumeController: VolumeController,
    onBack: () -> Unit
) {
    var currentVolumeState by remember { mutableStateOf(volumeController.refreshCurrentVolume()) }
    var accumulatedDeltaY by remember { mutableFloatStateOf(0f) }
    var isSwiping by remember { mutableStateOf(false) }
    var isActive by remember { mutableStateOf(false) }
    var activationId by remember { mutableIntStateOf(0) }
    var secondsLeft by remember { mutableIntStateOf(0) }
    var tapCount by remember { mutableIntStateOf(0) }
    var doubleTapCount by remember { mutableIntStateOf(0) }

    // Handle stays active for a few seconds after each tap / swipe, then goes inactive again
    LaunchedEffect(activationId) {
        if (activationId > 0) {
            isActive = true
            var remaining = INACTIVE_AFTER_SECONDS
            while (remaining > 0) {
                secondsLeft = remaining
                delay(1000)
                remaining--
            }
            secondsLeft = 0
            isActive = false
        }
    }

    val handleWidth by animateDpAsState(if (isActive || isSwiping) 26.dp else 8.dp, label = "handleWidth")
    val handleHeight by animateDpAsState(if (isActive || isSwiping) 120.dp else 44.dp, label = "handleHeight")
    val handleAlpha by animateFloatAsState(if (isActive || isSwiping) 0.96f else 0.55f, label = "handleAlpha")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
    ) {
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
                    text = "Handle Test Sandbox",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            IconButton(onClick = {
                currentVolumeState = volumeController.refreshCurrentVolume()
                tapCount = 0
                doubleTapCount = 0
                accumulatedDeltaY = 0f
                activationId = 0
                isActive = false
            }) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset",
                    tint = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isActive || isSwiping) PrimaryPurple else DividerColor
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
                        text = when {
                            isSwiping -> "Adjusting volume..."
                            isActive -> "Handle active - inactive in ${secondsLeft}s"
                            else -> "Tap the white handle on the right edge"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isActive || isSwiping) AccentCyan else TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Tap opens the real system volume bar. Drag the handle up or down to adjust directly.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isActive || isSwiping) PrimaryPurple.copy(alpha = 0.2f) else DarkSurfaceVariant)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${currentVolumeState.percentage}%",
                        color = if (isActive || isSwiping) PrimaryPurple else TextSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(20.dp))
                .background(DarkSurface)
                .border(
                    2.dp,
                    if (isActive || isSwiping) PrimaryPurple else DividerColor,
                    RoundedCornerShape(20.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                SimulatedVolumeHudCard(currentVolumeState)

                Spacer(modifier = Modifier.height(24.dp))

                Icon(
                    imageVector = Icons.Default.TouchApp,
                    contentDescription = null,
                    tint = if (isActive || isSwiping) AccentCyan else TextSecondary,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (tapCount == 0 && doubleTapCount == 0) "Tap the handle on the right edge\nDouble-tap = screenshot (works over other apps)" else "Taps: $tapCount  |  Double-taps: $doubleTapCount\n(Double-tap takes a real screenshot over other apps)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isActive || isSwiping) AccentCyan else TextSecondary,
                    textAlign = TextAlign.Center
                )
            }

            // Simulated edge handle (same behavior as the real overlay handle)
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 2.dp)
                    .size(width = 52.dp, height = 170.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = {
                                doubleTapCount++
                                activationId++
                            },
                            onTap = {
                                volumeController.showSystemVolumePanel()
                                currentVolumeState = volumeController.refreshCurrentVolume()
                                tapCount++
                                activationId++
                            }
                        )
                    }
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onDragStart = {
                                isSwiping = true
                                accumulatedDeltaY = 0f
                            },
                            onDragEnd = {
                                isSwiping = false
                                accumulatedDeltaY = 0f
                                activationId++
                            },
                            onDragCancel = {
                                isSwiping = false
                                accumulatedDeltaY = 0f
                            },
                            onVerticalDrag = { change, dragAmount ->
                                change.consume()
                                accumulatedDeltaY += -dragAmount // upward is positive
                                val thresholdPx = 30f
                                if (abs(accumulatedDeltaY) >= thresholdPx) {
                                    val steps = (accumulatedDeltaY / thresholdPx).toInt()
                                    if (steps != 0) {
                                        currentVolumeState = volumeController.adjustVolume(steps)
                                        accumulatedDeltaY -= (steps * thresholdPx)
                                    }
                                }
                            }
                        )
                    },
                contentAlignment = Alignment.CenterEnd
            ) {
                Box(
                    modifier = Modifier
                        .size(width = handleWidth, height = handleHeight)
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = handleAlpha))
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Button(
            onClick = {
                currentVolumeState = volumeController.refreshCurrentVolume()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Sync Current Volume",
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
                trackColor = DarkSurfaceVariant,
                strokeCap = StrokeCap.Round
            )
        }
    }
}


