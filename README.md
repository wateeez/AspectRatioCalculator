# FrameUP

A modern Android application (Kotlin, Material 3) that helps calculate and visualize aspect ratios, with custom presets and dark mode. It also converts newspaper advertisement sizes (column centimeters, cc) into centimeters and inches and previews the ad visually.

## Features

### Aspect ratio calculator
- Calculate aspect ratios from width and height
- Visual preview of the aspect ratio, with animations
- Common presets (16:9, 4:3, 21:9, 1:1, 3:2, 2.39:1, 5:4, 9:16, 1.85:1, 8:5)
- Custom presets: long-press **Calculate** to save the current size, long-press a custom chip to delete it
- Settings icon in the toolbar with a theme choice (system, light, dark), remembered between launches (Material You dynamic colors on Android 12+)

### Newspaper ad size converter
Open it from the **Newspaper ad** tab in the bottom navigation bar. Each tab keeps its values when you switch.

- Enter the ad size in column centimeters (cc)
- Choose an aspect ratio: Custom, 1:1, 2:1, 1:2, 4:3, 3:4
- Page setup (columns and column width) defaults to 8 columns × 4.5 cm. Change it, along with the theme, from the gear icon in the toolbar; the values are remembered
- Results in cm and inches, plus a scaled visual preview for print production. Tapping a ratio chip recalculates immediately

One cc is one column wide and 1 cm high, so an ad of *N* columns and *h* cm height is *N × h* cc.

| Ratio | Columns | Width | Height |
|-------|---------|-------|--------|
| Custom | all page columns | columns × column width | cc ÷ columns |
| Fixed (e.g. 1:1) | whole number from 1 to the page columns, closest to the ratio | n × column width | cc ÷ n |

Because columns are whole, a fixed ratio is approximate; the result card shows the ratio actually achieved. Very large ads are capped at the page width. Gutters between columns are not included.

Example: 60 cc on the default page gives 36 × 7.5 cm (14.17 × 2.95 in) in Custom mode, or 18 × 15 cm with 1:1 (4 columns).

## Installation

1. Download the latest APK from the [Releases](../../releases) page
2. Enable "Install from unknown sources" in your Android settings
3. Open the downloaded APK to install

## Building from source

Requires JDK 17 and the Android SDK (`sdk.dir` in `local.properties`).

```bash
export JAVA_HOME=/path/to/jdk17
./gradlew :app:assembleDebug        # APK: app/build/outputs/apk/debug/app-debug.apk
./gradlew :app:testDebugUnitTest    # unit tests
```

Install on a connected device or emulator:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

CI (`.github/workflows/android.yml`) builds the debug APK on every push to `main`.

## Requirements

- Android 7.0 (API level 24) or higher
- No special permissions; custom presets are stored in app-private preferences

## Technologies used

- Kotlin, View Binding
- Material Components for Android (Material 3)
- Gson

## Project layout

- `MainActivity.kt`: toolbar, bottom navigation, settings dialog (theme)
- `CalculatorFragment.kt`: aspect ratio calculator and presets
- `PresetManager.kt`: custom preset storage (Gson)
- `AdSizeFragment.kt` / `AdSizeCalculator.kt`: newspaper ad converter UI and logic
- `Motion.kt`: shared Material motion and color helpers
- `AspectRatioApp.kt`: enables dynamic colors
