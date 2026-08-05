# Relocate and Refactor Misplaced Files from `d/8`

I will move the files currently located in `shared/src/commonMain/kotlin/com/example/germanclash/d/8/` to their correct locations within the project structure. During this process, I will also update the package declarations and imports from `com.app.germanclash` to `com.example.germanclash` to ensure consistency with the rest of the project.

## Proposed Changes

### Relocation Mapping

I will move the files as follows, updating their package names to match the target directories:

| Source File (in `d/8`) | Target Directory | New Package |
| :--- | :--- | :--- |
| **Domain Layer** | | |
| `Player.kt` | `shared/src/commonMain/kotlin/com/example/germanclash/domain/model/` | `com.example.germanclash.domain.model` |
| `Question.kt` | `shared/src/commonMain/kotlin/com/example/germanclash/domain/model/` | `com.example.germanclash.domain.model` |
| `GameSession.kt` | `shared/src/commonMain/kotlin/com/example/germanclash/domain/model/` | `com.example.germanclash.domain.model` |
| `ConnectionStatus.kt` | `shared/src/commonMain/kotlin/com/example/germanclash/domain/model/` | `com.example.germanclash.domain.model` |
| `GameRepository.kt` | `shared/src/commonMain/kotlin/com/example/germanclash/domain/repository/` | `com.example.germanclash.domain.repository` |
| `AdvanceToNextQuestionUseCase.kt` | `shared/src/commonMain/kotlin/com/example/germanclash/domain/usecase/` | `com.example.germanclash.domain.usecase` |
| `StartMatchUseCase.kt` | `shared/src/commonMain/kotlin/com/example/germanclash/domain/usecase/` | `com.example.germanclash.domain.usecase` |
| **Data Layer** | | |
| `GameRepositoryImpl.kt` | `shared/src/commonMain/kotlin/com/example/germanclash/data/repository/` | `com.example.germanclash.data.repository` |
| `FirestoreDataSource.kt` | `shared/src/commonMain/kotlin/com/example/germanclash/data/remote/datasource/` | `com.example.germanclash.data.remote.datasource` |
| `SoloDataSource.kt` | `shared/src/commonMain/kotlin/com/example/germanclash/data/local/datasource/` | `com.example.germanclash.data.local.datasource` |
| `NearbyP2PDataSource.kt` | `shared/src/commonMain/kotlin/com/example/germanclash/data/local/datasource/` | `com.example.germanclash.data.local.datasource` |
| `QuestionBank.kt` | `shared/src/commonMain/kotlin/com/example/germanclash/data/local/questionbank/` | `com.example.germanclash.data.local.questionbank` |
| `SampleQuestionBank.kt` | `shared/src/commonMain/kotlin/com/example/germanclash/data/local/questionbank/` | `com.example.germanclash.data.local.questionbank` |
| `PracticeFilter.kt` | `shared/src/commonMain/kotlin/com/example/germanclash/data/local/questionbank/` | `com.example.germanclash.data.local.questionbank` |
| `VocabularyQuestionMapper.kt` | `shared/src/commonMain/kotlin/com/example/germanclash/data/local/questionbank/` | `com.example.germanclash.data.local.questionbank` |
| `AndroidAssetQuestionBank.kt` | `shared/src/androidMain/kotlin/com/example/germanclash/data/local/questionbank/` | `com.example.germanclash.data.local.questionbank` |
| **Presentation Layer** | | |
| `GameScreen.kt` | `shared/src/commonMain/kotlin/com/example/germanclash/presentation/feature/game/` | `com.example.germanclash.presentation.feature.game` |
| `GameContract.kt` | `shared/src/commonMain/kotlin/com/example/germanclash/presentation/feature/game/` | `com.example.germanclash.presentation.feature.game` |
| `GameViewModel.kt` | `shared/src/commonMain/kotlin/com/example/germanclash/presentation/feature/game/` | `com.example.germanclash.presentation.feature.game` |
| `RoomScreen.kt` | `shared/src/commonMain/kotlin/com/example/germanclash/presentation/feature/room/` | `com.example.germanclash.presentation.feature.room` |
| `RoomViewModel.kt` | `shared/src/commonMain/kotlin/com/example/germanclash/presentation/feature/room/` | `com.example.germanclash.presentation.feature.room` |
| `ModeSelectScreen.kt` | `shared/src/commonMain/kotlin/com/example/germanclash/presentation/feature/modeselect/` | `com.example.germanclash.presentation.feature.modeselect` |
| `ResultsScreen.kt` | `shared/src/commonMain/kotlin/com/example/germanclash/presentation/feature/results/` | `com.example.germanclash.presentation.feature.results` |
| `FilterChip.kt` | `shared/src/commonMain/kotlin/com/example/germanclash/presentation/common/` | `com.example.germanclash.presentation.common` |
| `CategoryChip.kt` | `shared/src/commonMain/kotlin/com/example/germanclash/presentation/common/` | `com.example.germanclash.presentation.common` |
| `SectionCard.kt` | `shared/src/commonMain/kotlin/com/example/germanclash/presentation/common/` | `com.example.germanclash.presentation.common` |
| `CategoryVisuals.kt` | `shared/src/commonMain/kotlin/com/example/germanclash/presentation/common/` | `com.example.germanclash.presentation.common` |
| `GameColors.kt` | `shared/src/commonMain/kotlin/com/example/germanclash/presentation/theme/` | `com.example.germanclash.presentation.theme` |
| `GermanClashTheme.kt` | `shared/src/commonMain/kotlin/com/example/germanclash/presentation/theme/` | `com.example.germanclash.presentation.theme` |
| `GermanClashBackground.kt` | `shared/src/commonMain/kotlin/com/example/germanclash/presentation/theme/` | `com.example.germanclash.presentation.theme` |
| **Dependency Injection** | | |
| `DataModule.kt` | `shared/src/commonMain/kotlin/com/example/germanclash/di/` | `com.example.germanclash.di` |
| `DomainModule.kt` | `shared/src/commonMain/kotlin/com/example/germanclash/di/` | `com.example.germanclash.di` |
| `FeatureModule.kt` | `shared/src/commonMain/kotlin/com/example/germanclash/di/` | `com.example.germanclash.di` |
| `AndroidModule.kt` | `shared/src/androidMain/kotlin/com/example/germanclash/di/` | `com.example.germanclash.di` |
| **Navigation & Main** | | |
| `GermanClashNavHost.kt` | `shared/src/commonMain/kotlin/com/example/germanclash/navigation/` | `com.example.germanclash.navigation` |
| `Screen.kt` | `shared/src/commonMain/kotlin/com/example/germanclash/navigation/` | `com.example.germanclash.navigation` |
| `MainActivity.kt` | `androidApp/src/main/kotlin/com/example/germanclash/` | `com.example.germanclash` |

### Key Steps

1.  **Relocate Files**: Use `read_file` to get the content of each file in `d/8`, then `write_file` to save it to the target location with the updated package name and imports (replacing `com.app.germanclash` with `com.example.germanclash`).
2.  **Overwrite mis-placed files**: For the original files in `d/8`, I will overwrite them with a placeholder comment or simply leave them (as I cannot delete).
3.  **Dependency Injection Setup**: Ensure that the Koin modules in the new `di` files are correctly used in `GermanClashApplication.kt` (if it exists).

## Verification Plan

### Automated Tests
- Run `./gradlew :shared:compileAndroidMain` to verify the build.
- Run `./gradlew :androidApp:assembleDebug`.

### Manual Verification
- Check the Project explorer to ensure all files are in their expected folders.
