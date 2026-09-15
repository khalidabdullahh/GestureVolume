package com.gesturevolume.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gesturevolume.app.data.model.GestureConfig
import com.gesturevolume.app.data.model.HudPosition
import com.gesturevolume.app.ui.theme.AccentCyan
import com.gesturevolume.app.ui.theme.DarkBackground
import com.gesturevolume.app.ui.theme.DarkSurface
import com.gesturevolume.app.ui.theme.DarkSurfaceVariant
import com.gesturevolume.app.ui.theme.DividerColor
import com.gesturevolume.app.ui.theme.PrimaryPurple
import com.gesturevolume.app.ui.theme.TextPrimary
import com.gesturevolume.app.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
    config: GestureConfig,
    onUpdateConfig: (GestureConfig) -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Preferences & Settings",
                style = MaterialTheme.typography.titleLarge
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Edge Handle Section
        SettingsSectionHeader(title = "Edge Trigger Handle", icon = Icons.Default.Tune)

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Edge Side Selection (Right / Left)
                Text(
                    text = "Screen Edge Placement",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Choose which side of your screen displays the subtle volume handle.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    com.gesturevolume.app.data.model.EdgeSide.entries.forEach { side ->
                        val isSelected = config.edgeSide == side
                        FilterChip(
                            selected = isSelected,
                            onClick = { onUpdateConfig(config.copy(edgeSide = side)) },
                            label = { Text(if (side == com.gesturevolume.app.data.model.EdgeSide.RIGHT) "Right Edge" else "Left Edge") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryPurple,
                                selectedLabelColor = TextPrimary,
                                containerColor = DarkSurfaceVariant,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Handle Opacity
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Handle Transparency",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "${(config.edgeOpacity * 100).toInt()}%",
                        color = PrimaryPurple,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "Lower opacity makes the handle blend subtly with wallpapers and videos.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Slider(
                    value = config.edgeOpacity,
                    onValueChange = { onUpdateConfig(config.copy(edgeOpacity = it)) },
                    valueRange = 0.15f..1.0f,
                    steps = 5,
                    colors = SliderDefaults.colors(
                        thumbColor = PrimaryPurple,
                        activeTrackColor = PrimaryPurple,
                        inactiveTrackColor = DarkSurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Auto Timeout Selection
                Text(
                    text = "Inactivity Dismiss Timeout",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Time before the volume HUD fades away after finger release.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(2, 3, 5, 10).forEach { seconds ->
                        val isSelected = config.timeoutSeconds == seconds
                        FilterChip(
                            selected = isSelected,
                            onClick = { onUpdateConfig(config.copy(timeoutSeconds = seconds)) },
                            label = { Text("${seconds}s") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryPurple,
                                selectedLabelColor = TextPrimary,
                                containerColor = DarkSurfaceVariant,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Volume & HUD Section
        SettingsSectionHeader(title = "Volume & Display", icon = Icons.AutoMirrored.Filled.VolumeUp)

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Swipe Sensitivity
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Vertical Swipe Sensitivity",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "${(config.swipeSensitivity * 10).toInt() / 10f}x",
                        color = PrimaryPurple,
                        fontWeight = FontWeight.Bold
                    )
                }
                Slider(
                    value = config.swipeSensitivity,
                    onValueChange = { onUpdateConfig(config.copy(swipeSensitivity = it)) },
                    valueRange = 0.5f..2.0f,
                    steps = 5,
                    colors = SliderDefaults.colors(
                        thumbColor = PrimaryPurple,
                        activeTrackColor = PrimaryPurple,
                        inactiveTrackColor = DarkSurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // HUD Position
                Text(
                    text = "HUD Position on Screen",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HudPosition.entries.forEach { position ->
                        val isSelected = config.hudPosition == position
                        FilterChip(
                            selected = isSelected,
                            onClick = { onUpdateConfig(config.copy(hudPosition = position)) },
                            label = { Text(position.name.lowercase().replaceFirstChar { it.uppercase() }) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryPurple,
                                selectedLabelColor = TextPrimary,
                                containerColor = DarkSurfaceVariant,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Percentage Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Show Percentage",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Display exact numeric percentage in HUD.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Switch(
                        checked = config.showPercentage,
                        onCheckedChange = { onUpdateConfig(config.copy(showPercentage = it)) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = TextPrimary,
                            checkedTrackColor = PrimaryPurple
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Haptic Feedback Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Haptic Tactile Feedback",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Subtle vibration tick on gesture trigger and step adjustments.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Switch(
                        checked = config.hapticFeedback,
                        onCheckedChange = { onUpdateConfig(config.copy(hapticFeedback = it)) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = TextPrimary,
                            checkedTrackColor = PrimaryPurple
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // System Settings Section
        SettingsSectionHeader(title = "System & Permissions", icon = Icons.Default.AccessibilityNew)

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Android Accessibility Service",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Required to recognize the circle gesture globally and show the temporary HUD over other apps.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onOpenAccessibilitySettings,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "Open Accessibility Settings",
                        color = TextPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SettingsSectionHeader(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 10.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AccentCyan,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
    }
}
