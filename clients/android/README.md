# Arcane Android Client

Modern Android scaffold for the Arcane Android client.

## Stack

- Kotlin
- Jetpack Compose with Material 3
- Single `:app` Android application module
- MVVM-style placeholders (`ui/home/HomeViewModel.kt`)
- Hilt dependency injection (`di/NetworkModule.kt`)
- Ktor client + kotlinx serialization for networking
- Jetpack DataStore placeholder for settings/storage
- Version catalog in `gradle/libs.versions.toml`

## Package layout

```text
app/src/main/java/app/arcane/android/
  ArcaneApplication.kt          # Hilt application class
  MainActivity.kt               # Compose entry point
  data/api/                     # API client/service placeholders
  data/repository/              # Repository implementations
  data/settings/                # DataStore settings placeholder
  di/                           # Dependency injection modules
  domain/model/                 # Domain models
  domain/repository/            # Repository interfaces
  ui/home/                      # Placeholder home screen + ViewModel
  ui/theme/                     # Compose Material 3 theme
```

## Build requirements

- JDK 17
- Android SDK with platform `android-35` and Build Tools `35.0.0`

If Android Studio is installed, open this directory and let it sync Gradle. From a terminal:

```bash
./gradlew assembleDebug
```

If the Android SDK is not installed, install Android command-line tools and run:

```bash
sdkmanager "platform-tools" "platforms;android-35" "build-tools;35.0.0"
```

## Current app behavior

The app launches a Compose placeholder home screen showing project status cards for UI, API/data, domain, and settings layers.

## Verified locally

This scaffold was verified on the headless Hermes machine after installing Android command-line tools under `$HOME/Android/Sdk`:

```bash
./gradlew clean assembleDebug
./gradlew testDebugUnitTest lintDebug
```

Both commands completed successfully. Unit tests are currently `NO-SOURCE` because no test files have been added yet.
