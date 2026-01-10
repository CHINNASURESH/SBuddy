# AGENTS.md - SBuddy Android App (Complete Version)

## Project Overview
SBuddy (also known as Score Ace) is a comprehensive Android application for badminton players. It facilitates real-time match scoring, tournament organization, and player history tracking.

**Latest Version Features:**
*   **Robust Architecture**: Uses View Binding for type-safe UI interaction and a custom `Theme.SBuddy` for stability.
*   **UI/UX**: Refined layouts including a Navigation Drawer Dashboard and Card-based interfaces.
*   **Backend**: Integrated with Firebase (Auth/Firestore) with placeholder repositories for immediate runnability.

## Key Features

### 1. User Authentication
*   **Login**: Clean UI with Email/Password fields.
*   **Safety**: Uses View Binding to prevent NullPointerExceptions.

### 2. Dashboard
*   **Navigation**: Drawer menu ("Ladder menu") and large Quick Access Tiles.
*   **Modules**: New Game, History, Tournaments.

### 3. Match Setup & Scoring
*   **Setup**: Configure Singles/Doubles and Custom Points to Win.
*   **Scoring**: Large interactive score tiles.
*   **AI Server Highlight**: Visual indication of the serving team based on score logic.
*   **Game Over**: Dialog with options for Rematch or New Game.

### 4. Tournament Management
*   **Fixture Generation**: Create lists of participants and automatically generate single-elimination brackets (handling Byes).

### 5. Match History
*   **Log**: View past matches with details (Date, Players, Score).

## Build & Run Instructions

**Environment Setup**:
```bash
export ANDROID_HOME=/usr/lib/android-sdk
```

**Build Command**:
```bash
gradle assembleDebug
```
*Note: A mock `google-services.json` is included. Replace it with your real Firebase config for production use.*

**Run Tests**:
```bash
gradle test
```

## Architecture
*   **Package**: `com.sbuddy.app`
*   **UI Layer**: `ui.login`, `ui.scoring`, `ui.history`, `ui.tournament`
*   **Data Layer**: `data.repository`, `data.model`
*   **Utils**: `GameLogic`, `TournamentManager`
