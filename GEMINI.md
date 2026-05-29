# Hopfield Android Project

## Project Overview
Hopfield is an Android application developed using **Kotlin** and **Jetpack Compose**. Based on the project name, it is likely intended for implementing or demonstrating **Hopfield Networks** (associative memory neural networks), although the current codebase is in an early, boilerplate state.

### Tech Stack
- **Language:** Kotlin 2.2.10
- **UI Framework:** Jetpack Compose with Material 3
- **Build System:** Gradle (Kotlin DSL) with Version Catalogs
- **Min SDK:** 26 (Android 8.0)
- **Target/Compile SDK:** 36 (Android 15+)

### Architecture
The project follows the standard Android module structure:
- `:app`: The main application module containing UI, business logic, and resources.
- `gradle/libs.versions.toml`: Centralized dependency management.

## Building and Running
Common Gradle commands for this project:

- **Build Debug APK:** `./gradlew assembleDebug`
- **Run Unit Tests:** `./gradlew test`
- **Run Instrumented Tests:** `./gradlew connectedAndroidTest`
- **Lint Check:** `./gradlew lint`
- **Clean Project:** `./gradlew clean`

> **Note:** To run or install the app (`./gradlew installDebug`), ensure an Android emulator is running or a physical device is connected via ADB.

## Development Conventions
- **UI:** Exclusively use Jetpack Compose for new UI components.
- **Dependencies:** Add new dependencies to `gradle/libs.versions.toml` and reference them in `build.gradle.kts`.
- **Coding Style:** Adhere to standard Kotlin coding conventions and modern Android development practices (Edge-to-Edge, Material 3, etc.).
- **Themes:** UI components should be defined within `HopfieldTheme` found in `ui.theme`.
