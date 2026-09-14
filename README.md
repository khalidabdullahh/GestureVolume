# Gesture Volume

<p align="center">
  <b>A lightweight, privacy-first Android system utility for on-screen volume control via intuitive circle gestures.</b>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android_8.0+_(API_26+)-brightgreen.svg" alt="Platform">
  <img src="https://img.shields.io/badge/Target_SDK-35_(Android_15)-blue.svg" alt="Target SDK">
  <img src="https://img.shields.io/badge/License-Apache_2.0-blue.svg" alt="License">
  <img src="https://img.shields.io/badge/Privacy-100%25_Offline_•_Zero_Tracking-success.svg" alt="Privacy">
</p>

---

## 📌 Overview

**Gesture Volume** is designed for users whose physical volume buttons are broken, unresponsive, inaccessible, or inconvenient. Inspired by media player controls (such as MX Player and VLC), Gesture Volume works **system-wide across any foreground application**:

1. **Draw a Circle (⭕)** anywhere on your screen over any app.
2. **Gesture Mode Activates Temporarily** — no permanent floating button, no permanent overlay, no edge triggers.
3. **Swipe Vertically (↑ / ↓)** to adjust media volume.
4. **Modern Temporary HUD** displays the current volume level and percentage.
5. **Auto-Dismiss** — after a brief moment of inactivity (default 3s), the HUD smoothly fades out and gesture mode resets to idle.

---

## 🚀 Key Features

- **No Permanent Floating Buttons**: Leaves your screen 100% clean during normal usage.
- **Global In-App Activation**: Works seamlessly over YouTube, Chrome, Games, Social Media, Gallery, and video players.
- **Robust Geometric Gesture Engine**: Differentiates intentional circles from swipes, taps, and random scribbles using multi-factor geometric verification (aspect ratio, radius variance, angular sweep, and closure).
- **Smooth AudioManager Integration**: Directly controls Android's native `STREAM_MUSIC` within authentic hardware bounds.
- **Tactile Haptic Feedback**: Delivers subtle vibration clicks when gestures trigger or reach volume boundaries.
- **Modern Jetpack Compose UI**: Features Material 3 dark aesthetics, an onboarding setup flow, a live dashboard, a settings screen, and an interactive test sandbox.
- **100% Offline & Private**: Zero network access, zero analytics, zero trackers, and zero ads.

---

## 🛡️ Privacy & Permissions Model

Gesture Volume adheres strictly to Google Play's Accessibility and User Data policies.

| Permission / Capability | Purpose |
| :--- | :--- |
| `android.permission.BIND_ACCESSIBILITY_SERVICE` | Used solely to detect the circle gesture over active applications and display the temporary volume HUD overlay via `WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY`. |
| `android.permission.VIBRATE` | Provides subtle, tactile click sensations upon gesture recognition and volume step changes. |

### 🔒 Privacy Commitments
- **No Text / Content Scraping**: `canRetrieveWindowContent` is explicitly set to `false`. The app does **not** read screen contents, messages, keystrokes, or passwords.
- **No Internet Access**: The application does **not** request the `android.permission.INTERNET` permission and functions entirely offline (including in Airplane Mode).
- **No Persistent Background Processing**: In idle mode, the app performs zero continuous computation or background wake-locks.

---

## 🛠️ Architecture

```
com.gesturevolume.app/
├── data/
│   ├── model/               # GestureConfig, VolumeState, HudPosition
│   └── preferences/         # DataStore preferences for sensitivity and HUD customization
├── domain/
│   ├── gesture/
│   │   ├── CircleGestureRecognizer.kt   # Mathematical circle detection engine
│   │   ├── TouchPoint.kt                # Point sampling & spatial metrics
│   │   ├── GestureState.kt              # State machine states & events
│   │   └── GestureStateMachine.kt       # State machine logic
│   └── volume/
│       ├── VolumeController.kt          # Interface for volume management
│       └── VolumeMapper.kt              # Vertical pixel displacement to volume steps
├── system/
│   ├── accessibility/
│   │   ├── GestureAccessibilityService.kt # Android AccessibilityService
│   │   └── ServiceStateHolder.kt          # Reactive service connection state
│   ├── overlay/
│   │   ├── OverlayManager.kt            # TYPE_ACCESSIBILITY_OVERLAY window manager
│   │   └── VolumeHudView.kt             # Animated temporary HUD
│   └── volume/
│       └── AndroidVolumeController.kt   # AudioManager wrapper (STREAM_MUSIC)
├── ui/
│   ├── dashboard/           # Main hub with service status and volume monitor
│   ├── onboarding/          # One-time guided setup and permission flow
│   ├── settings/            # Customization for sensitivity, timeout, HUD position
│   ├── test/                # Interactive gesture test sandbox
│   ├── theme/               # Material 3 dark palette, typography, shapes
│   └── MainActivity.kt      # Single-Activity entry point with Navigation Compose
└── util/
    ├── AccessibilityHelper.kt           # Verification of Accessibility settings
    └── HapticFeedbackHelper.kt          # Vibration effects
```

---

## 🧪 Circle Gesture Detection Algorithm

The `CircleGestureRecognizer` applies a multi-stage evaluation pipeline:
1. **Spatial Resampling**: Normalizes raw touch points to equidistant samples ($12\text{px}$ intervals).
2. **Bounding Box & Aspect Ratio**: Ensures dimensions $W \ge 60\text{px}, H \ge 60\text{px}$ with aspect ratio $\min(W,H)/\max(W,H) \ge 0.50$.
3. **Centroid & Radius Uniformity**: Computes centroid $(\bar{x}, \bar{y})$ and radius coefficient of variation $\sigma_r / R \le 0.35$.
4. **Perimeter Closure**: Measures distance from start to end point relative to mean radius ($D_{end} / R \le 0.70$).
5. **Angular Sweep (Winding Number)**: Evaluates cumulative signed angles $\sum \Delta \theta \approx \pm 2\pi$ ($4.2 \text{ rad} \le |\Theta| \le 9.0 \text{ rad}$) and verifies $>70\%$ rotational direction consistency.
6. **Path vs Circumference**: Compares stroke length $L$ to expected $2\pi R$.

---

## 📦 Building & Installation

### Requirements
- **JDK**: Java 17 or Java 21 (e.g. Android Studio JBR)
- **Android SDK**: Build Tools 34.0.0+, Platform SDK 35 (Android 15)

### Commands

#### Run Unit Tests
```bash
./gradlew test
```

#### Build Debug APK
```bash
./gradlew assembleDebug
```
Output APK: `app/build/outputs/apk/debug/app-debug.apk`

#### Build Release APK
```bash
./gradlew assembleRelease
```
Output APK: `app/build/outputs/apk/release/app-release-unsigned.apk`

---

## 📱 How to Use

1. **Install and Open Gesture Volume**.
2. **Grant Accessibility Permission**: Follow the onboarding prompt to enable the **Gesture Volume Service** under *Accessibility Settings*.
3. **Close the App**: Use your device normally.
4. **Draw a Circle (⭕)** anywhere on screen over any application.
5. **Swipe Up (↑) or Down (↓)** to adjust volume.
6. The HUD automatically disappears after 3 seconds of inactivity.
