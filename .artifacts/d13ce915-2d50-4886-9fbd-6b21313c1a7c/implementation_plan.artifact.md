# Restore Lost Polish and Enhance KMP Robustness

While the structural refactoring for Previews is complete, some "polish" features were simplified during the process. This plan restores those features in a more robust, KMP-compatible way.

## Proposed Changes

### UI & Animations

#### [MODIFY] [ConfettiOverlay.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/GermanClash/shared/src/commonMain/kotlin/com/example/germanclash/presentation/common/ConfettiOverlay.kt)
- Restore the `particleCount` parameter so that "big" milestones (like streaks) can trigger more intense confetti bursts.

#### [MODIFY] [GameScreen.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/GermanClash/shared/src/commonMain/kotlin/com/example/germanclash/presentation/feature/game/GameScreen.kt)
- Pass `confettiParticleCount` from the `ShowConfetti` effect down to `GameContent`.

### Navigation & Data Integrity

#### [MODIFY] [GermanClashNavHost.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/GermanClash/shared/src/commonMain/kotlin/com/example/germanclash/navigation/GermanClashNavHost.kt)
- **Robust Encoding**: Update `encodePlayers` and `decodePlayers` to handle special characters (like `:` or `,`) in player names. I will use a simple "hex-encoding" for the display names to ensure they don't break the URL structure.

#### [MODIFY] [Screen.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/GermanClash/shared/src/commonMain/kotlin/com/example/germanclash/navigation/Screen.kt)
- **URL Safety**: Improve the manual character replacement to be more exhaustive, ensuring that complex navigation arguments (like the encoded player list) don't crash the NavHost.

## Verification Plan

### Automated Tests
- Run `./gradlew :shared:compileAndroidMain` to ensure no syntax errors.

### Manual Verification
- **Visuals**: Trigger a streak milestone in the game and verify the "Big Confetti" burst.
- **Navigation**: Enter a player name with symbols (e.g., "Player:One,") and verify that the Results screen still loads correctly with their score.
