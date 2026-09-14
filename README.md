# Gesture Volume — Android On-Screen Volume Gesture Utility

<p align="center">
  <b>The ultimate lightweight, privacy-first Android system utility for on-screen volume control via intuitive circle gestures. Designed for devices with broken, unreliable, or inaccessible physical volume buttons.</b>
</p>

<p align="center">
  <a href="https://github.com/khalidabdullahh/GestureVolume/releases"><img src="https://img.shields.io/badge/Platform-Android_8.0+_(API_26+)-brightgreen.svg?style=for-the-badge&logo=android" alt="Platform"></a>
  <a href="https://github.com/khalidabdullahh/GestureVolume/releases"><img src="https://img.shields.io/badge/Target_SDK-35_(Android_15)-blue.svg?style=for-the-badge" alt="Target SDK"></a>
  <a href="https://buymeacoffee.com/khalidabdullahh"><img src="https://img.shields.io/badge/Buy_Me_A_Coffee-Support-FFDD00?style=for-the-badge&logo=buy-me-a-coffee&logoColor=black" alt="Buy Me A Coffee"></a>
  <a href="https://github.com/sponsors/khalidabdullahh"><img src="https://img.shields.io/badge/Sponsor-GitHub_Sponsors-EA4AAA?style=for-the-badge&logo=github-sponsors&logoColor=white" alt="GitHub Sponsors"></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-Apache_2.0-blue.svg?style=for-the-badge" alt="License"></a>
</p>

<p align="center">
  🌐 <b>Live Web Landing Page & Simulator:</b> <a href="https://khalidabdullahh.github.io/GestureVolume/">https://khalidabdullahh.github.io/GestureVolume/</a>
</p>

---

## 📌 Problem & Solution

**The Problem:**
Physical volume buttons wear out, break, get stuck, or are awkwardly placed on tablets and large phones. Existing apps either force permanent screen-blocking floating buttons, annoying edge swipes that trigger accidentally, or require opening an app every time you want to change volume.

**The Gesture Volume Solution:**
1. **Draw a Circle (⭕)** anywhere on your screen over any app (YouTube, Netflix, Chrome, Games, Social Media).
2. **Gesture Mode Activates Temporarily** — no permanent buttons, no edge activation.
3. **Swipe Vertically (↑ / ↓)** to smoothly adjust media volume.
4. **Modern Temporary HUD** displays volume level, icon, and numeric percentage.
5. **Auto-Dismiss** — after 3 seconds of inactivity, everything smoothly fades away into zero memory usage.

---

## ☕ Support & Sponsor This Project

Gesture Volume is 100% free, offline, ad-free, and open-source. If this project helps you or you want to support continuous maintenance and new features, please consider supporting!

<p align="center">
  <a href="https://buymeacoffee.com/khalidabdullahh" target="_blank">
    <img src="https://cdn.buymeacoffee.com/buttons/v2/default-yellow.png" alt="Buy Me A Coffee" width="180">
  </a>
  &nbsp;&nbsp;&nbsp;&nbsp;
  <a href="https://github.com/sponsors/khalidabdullahh" target="_blank">
    <img src="https://img.shields.io/badge/Sponsor_on_GitHub-EA4AAA?style=for-the-badge&logo=github-sponsors&logoColor=white" alt="Sponsor on GitHub" height="48">
  </a>
  &nbsp;&nbsp;&nbsp;&nbsp;
  <a href="https://ko-fi.com/khalidabdullahh" target="_blank">
    <img src="https://img.shields.io/badge/Support_on_Ko--fi-FF5E5B?style=for-the-badge&logo=ko-fi&logoColor=white" alt="Ko-fi" height="48">
  </a>
</p>

---

## 🚀 Key Features

- **Zero Permanent Screen Clutter**: No persistent floating bubbles or edges.
- **Global In-App Activation**: Works seamlessly across any foreground Android application.
- **High-Precision Geometric Engine**: Distinguishes intentional circles from straight lines, taps, and random scribbles using multi-factor verification (aspect ratio, radius coefficient of variation, perimeter closure, angular winding sum, and directional consistency).
- **Smooth AudioManager Integration**: Native `STREAM_MUSIC` control respecting real device limits.
- **Subtle Haptic Feedback**: Tactile vibration clicks on gesture activation and 0%/100% boundaries.
- **Modern Jetpack Compose UI**: Material 3 dark aesthetic, onboarding flow, live dashboard, interactive sandbox, and full settings customization.
- **100% Offline & Privacy First**: Zero internet permissions, zero analytics, zero trackers, and zero ads.

---

## 🛡️ Privacy & Permissions Model

Gesture Volume adheres strictly to Google Play's Accessibility and User Data policies.

| Permission / Capability | Purpose |
| :--- | :--- |
| `android.permission.BIND_ACCESSIBILITY_SERVICE` | Used solely to detect the circle gesture over active applications and display the temporary volume HUD overlay via `WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY`. |
| `android.permission.VIBRATE` | Provides subtle, tactile click sensations upon gesture recognition and volume step changes. |

### 🔒 Privacy Commitments
- **No Text / Screen Scraping**: `canRetrieveWindowContent` is explicitly set to `false`. The app does **not** read screen contents, messages, keystrokes, or passwords.
- **No Internet Access**: The application does **not** request `android.permission.INTERNET` and functions completely offline.
- **Zero Idle Battery Drain**: Event-driven architecture with zero background loops or wake-locks.

---

## 🛠️ Architecture

```
com.gesturevolume.app/
├── data/
│   ├── model/               # GestureConfig, VolumeState, HudPosition
│   └── preferences/         # DataStore preferences
├── domain/
│   ├── gesture/             # CircleGestureRecognizer, GestureStateMachine, TouchPoint
│   └── volume/              # VolumeController interface, VolumeMapper
├── system/
│   ├── accessibility/       # GestureAccessibilityService, ServiceStateHolder
│   ├── overlay/             # OverlayManager, VolumeHudView
│   └── volume/              # AndroidVolumeController (AudioManager STREAM_MUSIC)
├── ui/
│   ├── dashboard/           # DashboardScreen
│   ├── onboarding/          # OnboardingScreen
│   ├── settings/            # SettingsScreen
│   ├── test/                # TestGestureScreen
│   ├── theme/               # Material 3 Color, Type, Theme
│   └── MainActivity.kt      # Single-Activity entry point
└── util/
    ├── AccessibilityHelper.kt
    └── HapticFeedbackHelper.kt
```

---

## 📦 Building & Running

### Requirements
- **JDK**: Java 17 or Java 21 (e.g. Android Studio JBR)
- **Android SDK**: Build Tools 34.0.0+, Platform SDK 35 (Android 15)

```bash
# Run unit tests
./gradlew test

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease
```

---

## 📄 License & Attribution

Licensed under the [Apache License, Version 2.0](LICENSE).  
Created with ❤️ by [Khalid Abdullah](https://github.com/khalidabdullahh).
