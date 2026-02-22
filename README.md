# 🎬 Android Native Movies App

A native **Kotlin Android application** that integrates with a **Flutter module** to display a list of popular movies powered by the TMDB API. The native app handles launching the Flutter experience and rendering movie trailers natively via the YouTube Player API.

---

## 📋 Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [Flutter Module Integration](#flutter-module-integration)
- [Dynamic Path Configuration](#dynamic-path-configuration)
- [How It Works](#how-it-works)
- [Method Channel Contract](#method-channel-contract)
- [Related Repositories](#related-repositories)

---

## Overview

This repository is the **Android native host application** — one of three repositories that make up the full Movies Module project.

| Repository | Role |
|---|---|
| **This repo** | Native Kotlin Android app — launches Flutter, plays trailers |
| [flutter_movies_module](https://github.com/elzoghby/flutter_movies_module) | Flutter BLoC + TMDB movies module |
| [ios_movies](https://github.com/elzoghby/ios_movies) | Native Swift iOS host app |

The native app contains a single button: **"Show List of Movies"**. Pressing it launches the Flutter module inside the Android app. When the user selects a movie in Flutter, the module passes the trailer key back to the native layer, which plays it on a native `TrailerActivity` using the YouTube Player.

---

## Architecture

```
┌─────────────────────────────────────────────┐
│            Android Native App               │
│                                             │
│  ┌──────────────┐    ┌───────────────────┐  │
│  │ MainActivity │───▶│  FlutterActivity  │  │
│  │  (button)    │    │  (Flutter Engine) │  │
│  └──────────────┘    └────────┬──────────┘  │
│                               │             │
│                   Movie selected            │
│                               │             │
│                               ▼             │
│  ┌────────────────────────────────────────┐ │
│  │          TrailerActivity               │ │
│  │   YouTubePlayerView — native playback  │ │
│  └────────────────────────────────────────┘ │
└─────────────────────────────────────────────┘
```

**Communication flow:**
1. Native → Flutter: `FlutterActivity` is started via `Intent` — Flutter auto-initialises
2. Flutter → Native: `FlutterMethodChannel` sends `showTrailer` with `videoKey` + `movieId`
3. Native receives call → starts `TrailerActivity` with the YouTube video key

---

## Project Structure

```
android_movies/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/movies/android/
│   │       │   ├── MainActivity.kt          # "Show List of Movies" button + engine setup
│   │       │   ├── TrailerActivity.kt       # Native YouTube trailer screen
│   │       │   └── FlutterChannelHandler.kt # MethodChannel listener
│   │       ├── res/                         # Layouts, drawables, strings
│   │       └── AndroidManifest.xml
│   └── build.gradle.kts                     # App-level dependencies
├── gradle/                                  # Gradle wrapper files
├── build.gradle.kts                         # Project-level build config
├── settings.gradle.kts                      # Flutter module source inclusion
├── gradle.properties                        # JVM + Android properties
├── DYNAMIC_PATH_CONFIG.md                   # Guide for configuring Flutter module path
└── gradlew / gradlew.bat                    # Gradle wrapper scripts
```

---

## Prerequisites

Before running this project, make sure you have:

| Tool | Version | Notes |
|---|---|---|
| Android Studio | Hedgehog (2023.1.1)+ | [developer.android.com](https://developer.android.com/studio) |
| Android SDK | API 24+ (Android 7.0) | Install via SDK Manager |
| Kotlin | 1.9+ | Bundled with Android Studio |
| Flutter SDK | 3.0+ | [flutter.dev](https://flutter.dev/docs/get-started/install) |
| Java | 17+ | Required by Gradle 8+ |
| YouTube Android Player API | — | For native trailer playback |

---

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/elzoghby/android_movies.git
cd android_movies
```

### 2. Clone the Flutter module alongside it

The Flutter module must be cloned in the **same parent directory** as this repo:

```bash
# From the parent directory
git clone https://github.com/elzoghby/flutter_movies_module.git
```

Your folder structure should look like this:

```
parent_folder/
├── android_movies/        ← this repo
└── flutter_movies_module/ ← Flutter module
```

### 3. Verify the Flutter module path in `settings.gradle.kts`

```kotlin
// settings.gradle.kts
include(":flutter_movies_module")
project(":flutter_movies_module").projectDir =
    File(settingsDir, "../flutter_movies_module")
```

> See [Dynamic Path Configuration](#dynamic-path-configuration) if the Flutter module is located elsewhere on your machine.

### 4. Sync and build

Open the project in Android Studio and click **Sync Project with Gradle Files**, or run:

```bash
./gradlew assembleDebug
```

### 5. Run the app

Select a device or emulator in Android Studio and press **▶ Run**, or:

```bash
./gradlew installDebug
```

---

## Flutter Module Integration

The Flutter module is embedded using the **source dependency** approach via `settings.gradle.kts` — the recommended method by the Flutter team for Android.

### How the Flutter engine is initialised

```kotlin
// MainActivity.kt
class MainActivity : AppCompatActivity() {

    private lateinit var flutterEngine: FlutterEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Warm up the Flutter engine
        flutterEngine = FlutterEngine(this).apply {
            dartExecutor.executeDartEntrypoint(
                DartExecutor.DartEntrypoint.createDefault()
            )
        }
        FlutterEngineCache.getInstance().put("movies_engine", flutterEngine)

        // Register the method channel
        FlutterChannelHandler(flutterEngine, this).register()

        // Button launches Flutter
        findViewById<Button>(R.id.btnShowMovies).setOnClickListener {
            startActivity(
                FlutterActivity
                    .withCachedEngine("movies_engine")
                    .build(this)
            )
        }
    }
}
```

---

## Dynamic Path Configuration

Because the Flutter module path depends on where each developer clones the repo, the project includes a `DYNAMIC_PATH_CONFIG.md` guide.

If your Flutter module is **not** directly beside the Android project, update `settings.gradle.kts`:

```kotlin
// Option A — relative path (default, works when repos are siblings)
project(":flutter_movies_module").projectDir =
    File(settingsDir, "../flutter_movies_module")

// Option B — absolute path (use if repos are in different locations)
project(":flutter_movies_module").projectDir =
    File("/Users/yourname/projects/flutter_movies_module")
```

After changing the path, re-sync Gradle.

---

## How It Works

```
User taps "Show List of Movies"
              │
              ▼
   FlutterActivity launched with cached engine
              │
              ▼
   Flutter module fetches movies from TMDB API
              │
              ▼
   User taps a movie card
              │
              ▼
   Flutter sends "showTrailer" via MethodChannel
   { "videoKey": "yt_video_id", "movieId": 12345 }
              │
              ▼
   Native receives call → finishes FlutterActivity
              │
              ▼
   TrailerActivity started with videoKey
              │
              ▼
   YouTubePlayerView plays the trailer natively
```

---

## Method Channel Contract

**Channel name:** `com.movies.flutter/trailer`

### Flutter → Native

| Method | Arguments | Description |
|---|---|---|
| `showTrailer` | `{ "videoKey": String, "movieId": Int }` | Called when a movie is tapped and trailer is found. Native starts `TrailerActivity`. |

### Native listener (Kotlin)

```kotlin
// FlutterChannelHandler.kt
class FlutterChannelHandler(
    private val flutterEngine: FlutterEngine,
    private val activity: Activity
) {
    fun register() {
        MethodChannel(
            flutterEngine.dartExecutor.binaryMessenger,
            "com.movies.flutter/trailer"
        ).setMethodCallHandler { call, result ->
            when (call.method) {
                "showTrailer" -> {
                    val videoKey = call.argument<String>("videoKey")
                    val movieId = call.argument<Int>("movieId")
                    if (videoKey != null) {
                        activity.startActivity(
                            Intent(activity, TrailerActivity::class.java).apply {
                                putExtra("VIDEO_KEY", videoKey)
                                putExtra("MOVIE_ID", movieId)
                            }
                        )
                        result.success(null)
                    } else {
                        result.error("MISSING_KEY", "videoKey is null", null)
                    }
                }
                else -> result.notImplemented()
            }
        }
    }
}
```

---

## Related Repositories

| Repository | Description |
|---|---|
| [android_movies](https://github.com/elzoghby/android_movies) | ← You are here — Native Android host app |
| [flutter_movies_module](https://github.com/elzoghby/flutter_movies_module) | Flutter BLoC + TMDB movies module |
| [ios_movies](https://github.com/elzoghby/ios_movies) | Native Swift iOS host app |

---

## Built With

- **Kotlin** — Native Android development
- **Flutter Engine** — Embedded Flutter runtime via source dependency
- **FlutterMethodChannel** — Bi-directional native ↔ Flutter communication
- **YouTubePlayerView** — Native trailer playback
- **Gradle (Kotlin DSL)** — Build system
- **TMDB API** — Movie data and trailers (consumed by the Flutter module)
