# AGENTS.md - SBuddy Android App

## Project Overview
SBuddy is an Android application designed for badminton players to score matches and organize tournaments. It features real-time scoring with AI-based server highlighting and single-elimination tournament fixture generation.

## Environment Setup
To build this project in this environment, you must verify the Android SDK location and set the environment variable.

*   **SDK Location**: `/usr/lib/android-sdk`
*   **Command**:
    ```bash
    export ANDROID_HOME=/usr/lib/android-sdk
    ```

## Build Instructions
The project uses Gradle with Kotlin DSL.

*   **Build Debug APK**:
    ```bash
    gradle assembleDebug
    ```
    *Note: If you encounter license issues, ensure licenses are accepted or try running with `sudo` permissions on the SDK directory if necessary (though strictly not recommended for production).*

## Testing
Unit tests are located in `app/src/test`.

*   **Run Unit Tests**:
    ```bash
    gradle test
    ```

## Architecture & Code Organization
The code follows a standard layered architecture:

*   **`com.sbuddy.app.data`**:
    *   `model`: Data classes (`User`, `Match`, `Tournament`).
    *   `repository`: Data access layer (e.g., `MatchRepository`). Currently contains placeholder logic for Firebase.
*   **`com.sbuddy.app.ui`**:
    *   Activities and UI logic, organized by feature (`scoring`, `tournament`, `login`).
*   **`com.sbuddy.app.utils`**:
    *   `GameLogic.kt`: Core business logic for scoring and server positioning.
    *   `TournamentManager.kt`: Logic for generating fixtures.

## Configuration Notes
*   **Firebase**: The project is configured for Firebase (Auth, Firestore).
*   **`google-services.json`**: A **MOCK** version of this file exists in `app/` to allow the build to pass. For the app to function with a real backend, this file **MUST** be replaced with a valid configuration file from the Firebase Console.
