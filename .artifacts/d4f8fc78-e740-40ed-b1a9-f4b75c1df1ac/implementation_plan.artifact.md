# Implementation Plan: Advanced Multiplayer Settings & Game Formats

This plan adds granular controls for the Host in the multiplayer lobby, including custom question timers, category selection, and two new competitive game formats.

## Proposed Changes

### [Component] Domain & Data Models

#### [MODIFY] [GameFormat.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/GermanClash/shared/src/commonMain/kotlin/com/example/germanclash/domain/model/GameFormat.kt)
- Add `BUZZER` (First correct answer wins the round) and `TIME_ATTACK` (1-minute sprint).

#### [MODIFY] [GameSession.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/GermanClash/shared/src/commonMain/kotlin/com/example/germanclash/domain/model/GameSession.kt)
- Add `val category: String? = null` to track the selected category for the match.

#### [MODIFY] [P2PMessage.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/GermanClash/shared/src/commonMain/kotlin/com/example/germanclash/data/local/serialization/P2PMessage.kt)
- Add `data class GameSettingsChanged(val format: GameFormat, val timeLimitMs: Long, val category: String?) : P2PMessage`.

---

### [Component] Multiplayer DataSource Logic

#### [MODIFY] [NearbyP2PDataSource.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/GermanClash/shared/src/commonMain/kotlin/com/example/germanclash/data/local/datasource/NearbyP2PDataSource.kt)
- **Settings Synchronization**:
    - Implement a way for the host to change format, time limit, and category in the `GameSession` and broadcast it.
- **Buzzer Logic**:
    - In `submitAnswer` (Host): If `format == BUZZER` and the answer is correct, immediately call `advanceToNextQuestion` and broadcast, even if the guest hasn't answered.
- **Time Attack Logic**:
    - The host will start a global 60-second timer.
    - `isFinished` will be set to `true` once the 60s expires, regardless of question count.
- **Category Selection**:
    - Pass the selected category into the `PracticeFilter` when fetching the next question from the `QuestionBank`.

---

### [Component] Lobby (Room) UI

#### [MODIFY] [RoomScreen.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/GermanClash/shared/src/commonMain/kotlin/com/example/germanclash/presentation/feature/room/RoomScreen.kt)
- Add a **"Match Settings"** section (Host only):
    - **Time Limit Row**: 1s, 3s, 5s, 10s (Filter chips).
    - **Game Format Row**: Classic, Buzzer, Time Attack.
    - **Category Row**: Horizontal scrollable chips of available categories.
- Update `RoomViewModel` and `RoomUiState` to handle these interactions and call the data source to sync settings.

---

### [Component] Game Logic

#### [MODIFY] [GameViewModel.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/GermanClash/shared/src/commonMain/kotlin/com/example/germanclash/presentation/feature/game/GameViewModel.kt)
- If `format == TIME_ATTACK`, display a total match countdown (60s) instead of per-question timers.

## Verification Plan

### Manual Verification
1.  **Host Settings**:
    - Change the time limit to 3s. Verify the Guest device immediately sees the "3s" limit in their UI.
    - Change the format to "Buzzer".
2.  **Buzzer Test**:
    - Play a match. Host answers correctly. Verify the Guest's screen immediately jumps to the next question.
3.  **Time Attack Test**:
    - Select "Time Attack". Verify the game ends after 1 minute and shows correct final scores.
4.  **Category Test**:
    - Select "Animals". Verify all questions in the match are from the Animals category.
