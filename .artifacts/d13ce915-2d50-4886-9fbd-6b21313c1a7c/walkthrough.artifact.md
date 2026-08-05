# Walkthrough - Adding Compose Previews to Screens

I have added `@Preview` support to all main screens in the project. This involved refactoring some screens to separate their UI logic from their state management, enabling them to be rendered in the Android Studio Design tab with mock data.

## Changes Made

### Shared Module

#### [App.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/GermanClash/shared/src/commonMain/kotlin/com/example/germanclash/App.kt)
- Added `@Preview` to the `App` composable.

#### [HomeScreen.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/GermanClash/shared/src/commonMain/kotlin/com/example/germanclash/presentation/feature/home/HomeScreen.kt)
- Added `@Preview` and provided a default empty callback for `onJoinRoom`.

#### [ModeSelectScreen.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/GermanClash/shared/src/commonMain/kotlin/com/example/germanclash/presentation/feature/modeselect/ModeSelectScreen.kt)
- Added `@Preview` and provided default empty callbacks for navigation actions.

#### [RoomScreen.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/GermanClash/shared/src/commonMain/kotlin/com/example/germanclash/presentation/feature/room/RoomScreen.kt)
- Extracted the UI logic into a stateless `RoomContent` composable.
- Added `RoomScreenPreview` which renders `RoomContent` with a mock `RoomUiState`.

#### [GameScreen.kt](file:///C:/Users/Lenovo/AndroidStudioProjects/GermanClash/shared/src/commonMain/kotlin/com/example/germanclash/presentation/feature/game/GameScreen.kt)
- Extracted the core game UI into a stateless `GameContent` composable.
- Added `GameScreenPreview` which renders `GameContent` with a mock `GameUiState` for a `DER_DIE_DAS` question.

## Verification Results

### Automated Tests
- Ran `./gradlew :shared:compileAndroidMain` and the build was successful.

### Manual Verification
- All screens can now be previewed in the Android Studio Design tab.
