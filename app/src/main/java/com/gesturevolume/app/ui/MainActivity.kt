package com.gesturevolume.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gesturevolume.app.data.model.GestureConfig
import com.gesturevolume.app.data.preferences.GesturePreferences
import com.gesturevolume.app.system.accessibility.ServiceStateHolder
import com.gesturevolume.app.system.volume.AndroidVolumeController
import com.gesturevolume.app.ui.dashboard.DashboardScreen
import com.gesturevolume.app.ui.onboarding.OnboardingScreen
import com.gesturevolume.app.ui.settings.SettingsScreen
import com.gesturevolume.app.ui.test.TestGestureScreen
import com.gesturevolume.app.ui.theme.DarkBackground
import com.gesturevolume.app.ui.theme.GestureVolumeTheme
import com.gesturevolume.app.util.AccessibilityHelper
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var gesturePreferences: GesturePreferences
    private lateinit var volumeController: AndroidVolumeController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        gesturePreferences = GesturePreferences(this)
        volumeController = AndroidVolumeController(this)

        setContent {
            GestureVolumeTheme {
                val navController = rememberNavController()
                val coroutineScope = rememberCoroutineScope()

                var isServiceActive by remember { mutableStateOf(AccessibilityHelper.isAccessibilityServiceEnabled(this)) }
                val serviceConnectedState by ServiceStateHolder.isServiceConnected.collectAsState()

                val isOnboardingCompleted by gesturePreferences.isOnboardingCompleted.collectAsState(initial = false)
                val config by gesturePreferences.configFlow.collectAsState(initial = GestureConfig())
                val volumeState by volumeController.volumeState.collectAsState()

                // Refresh service status on Activity Resume
                val lifecycleOwner = LocalLifecycleOwner.current
                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_RESUME) {
                            isServiceActive = AccessibilityHelper.isAccessibilityServiceEnabled(this@MainActivity)
                            volumeController.refreshCurrentVolume()
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                    }
                }

                val effectiveServiceActive = isServiceActive || serviceConnectedState
                val startDestination = if (isOnboardingCompleted) "dashboard" else "onboarding"

                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding(),
                    color = DarkBackground
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = startDestination
                    ) {
                        composable("onboarding") {
                            OnboardingScreen(
                                isServiceActive = effectiveServiceActive,
                                onOpenAccessibilitySettings = {
                                    AccessibilityHelper.openAccessibilitySettings(this@MainActivity)
                                },
                                onCompleteOnboarding = {
                                    coroutineScope.launch {
                                        gesturePreferences.setOnboardingCompleted(true)
                                        navController.navigate("dashboard") {
                                            popUpTo("onboarding") { inclusive = true }
                                        }
                                    }
                                }
                            )
                        }

                        composable("dashboard") {
                            DashboardScreen(
                                isServiceActive = effectiveServiceActive,
                                volumeState = volumeState,
                                onOpenAccessibilitySettings = {
                                    AccessibilityHelper.openAccessibilitySettings(this@MainActivity)
                                },
                                onNavigateToTest = {
                                    navController.navigate("test")
                                },
                                onNavigateToSettings = {
                                    navController.navigate("settings")
                                }
                            )
                        }

                        composable("test") {
                            TestGestureScreen(
                                volumeController = volumeController,
                                onBack = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable("settings") {
                            SettingsScreen(
                                config = config,
                                onUpdateConfig = { updated ->
                                    coroutineScope.launch {
                                        gesturePreferences.updateConfig(updated)
                                    }
                                },
                                onOpenAccessibilitySettings = {
                                    AccessibilityHelper.openAccessibilitySettings(this@MainActivity)
                                },
                                onBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
