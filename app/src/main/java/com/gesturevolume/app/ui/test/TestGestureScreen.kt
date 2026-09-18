package com.gesturevolume.app.ui.test

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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

@Composable
fun TestGestureScreen(
    volumeController: VolumeController,
    onBack: () -> Unit
) {
    val volumeMapper = remember { VolumeMapper() }
    var currentVolumeState by remember { mutableStateOf(volumeController.refreshCurrentVolume()) }
    var accumulatedDeltaY by remember { mutableFloatStateOf(0f) }
    var swipeCount by remember { mutableIntStateOf(0) }
    var isSwiping by remember { mutableStateOf(false) }

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
                    text = "Gesture Test Sandbox",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            IconButton(onClick = {
                currentVolumeState = volumeController.refreshCurrentVolume()
                swipeCount = 0
                accumulatedDeltaY = 0f
            }) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset",
                    tint = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isSwiping) PrimaryPurple else DividerColor
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
                        text = if (isSwiping) "Adjusting Volume..." else "Swipe Up/Down on Sandbox Below",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isSwiping) AccentCyan else TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Slide finger upward to raise volume, downward to lower.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSwiping) PrimaryPurple.copy(alpha = 0.2f) else DarkSurfaceVariant)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${currentVolumeState.percentage}%",
                        color = if (isSwiping) PrimaryPurple else TextSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Interactive Swipe Test Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(20.dp))
                .background(DarkSurface)
                .border(2.dp, if (isSwiping) PrimaryPurple else DividerColor, RoundedCornerShape(20.dp))
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragStart = {
                            isSwiping = true
                            accumulatedDeltaY = 0f
                        },
                        onDragEnd = {
                            isSwiping = false
                            accumulatedDeltaY = 0f
                        },
                        onDragCancel = {
                            isSwiping = false
                            accumulatedDeltaY = 0f
                        },
                        onVerticalDrag = { change, dragAmount ->
                            change.consume()
                            val deltaY = -dragAmount // Upward swipe is positive
                            accumulatedDeltaY += deltaY

                            val thresholdPx = 30f
                            if (abs(accumulatedDeltaY) >= thresholdPx) {
                                val steps = (accumulatedDeltaY / thresholdPx).toInt()
                                if (steps != 0) {
                                    currentVolumeState = volumeController.adjustVolume(steps)
                                    swipeCount++
                                    accumulatedDeltaY -= (steps * thresholdPx)
                                }
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Simulated In-Screen Volume HUD
                SimulatedVolumeHudCard(currentVolumeState)

                Spacer(modifier = Modifier.height(24.dp))

                Icon(
                    imageVector = Icons.Default.TouchApp,
                    contentDescription = null,
                    tint = if (isSwiping) AccentCyan else TextSecondary,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isSwiping) "Swiping Active" else "Drag / Swipe Up & Down Anywhere Here",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isSwiping) AccentCyan else TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Button(
            onClick = {
                currentVolumeState = volumeController.refreshCurrentVolume()
                swipeCount = 0
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


