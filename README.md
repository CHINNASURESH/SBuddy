# SBuddy
Badminton Scorer and Tournament Organizer
create an android app using Jules - github - android studio. front end, backend :

Here is a summary of the SBuddy application:
SBuddy is a modern and comprehensive application designed for badminton players and enthusiasts. Its primary purpose is to make scoring matches and organizing tournaments simple and efficient.
At its core, the app provides a user-friendly interface for real-time score tracking in both singles and doubles matches, using standard rally scoring rules and custom option. A unique feature is its AI-powered server highlighting, which automatically determines and visually indicates the correct server based on the current score.
Beyond single games, SBuddy includes robust features for managing player history and competition:
User Authentication: Users can create personal profiles to log in and save their match history and performance statistics. Match History: All completed games are recorded, allowing players to review past scores and analyze their performance over time. Tournament Creation: The app streamlines the process of setting up tournaments by automatically generating single-elimination fixtures. It offers flexibility with options for random pairings, manual setup, and player seeding.

## Troubleshooting

### Build Errors (redirect.txt)
If you encounter an error like `Error loading build artifacts from: ...\redirect.txt`, it indicates that the IDE's build cache is out of sync with the Gradle build output. This is common when switching branches or machines.

**Solution:**
1.  **Clean the Project**:
    *   In Android Studio: Go to **Build > Clean Project**.
    *   Command Line: Run `gradlew clean`.
2.  **Rebuild**:
    *   In Android Studio: Go to **Build > Rebuild Project**.
    *   Command Line: Run `gradlew assembleDebug`.
3.  **Invalidate Caches** (If the above fails):
    *   Go to **File > Invalidate Caches / Restart**.
