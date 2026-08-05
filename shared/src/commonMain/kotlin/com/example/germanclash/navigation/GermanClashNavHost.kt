package com.example.germanclash.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.germanclash.data.local.questionbank.PracticeFilterState
import com.example.germanclash.data.local.questionbank.QuestionBank
import com.example.germanclash.presentation.common.SoundEffectPlayer
import com.example.germanclash.presentation.feature.game.GameScreen
import com.example.germanclash.presentation.feature.modeselect.ModeSelectScreen
import com.example.germanclash.presentation.feature.results.ResultsScreen
import com.example.germanclash.presentation.feature.room.RoomScreen
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

private const val HOST_ROOM_PREFIX = "LOCAL_HOST_"
private const val JOIN_ROOM_PREFIX = "LOCAL_JOIN_"

/**
 * Every screen's ViewModel comes from FeatureModule's parameterized `viewModel { (id) -> ... }`
 * factories, so navigating to a new roomId always gets a fresh ViewModel instance rather than
 * a stale one carried over from the previous room.
 *
 * Solo play skips the Room lobby entirely - a SOLO_-prefixed roomId routes straight to
 * GameScreen. Hosting/joining (LOCAL_HOST_/LOCAL_JOIN_-prefixed) both go through the Room
 * lobby: the host's Ready-up calls startMatch() and generates the first question; a guest's
 * Ready-up is a no-op and it just waits for the host's broadcast. Firestore (any other roomId)
 * still has no working host loop and isn't reachable from this UI today.
 */
@Composable
fun GermanClashNavHost(
    localPlayerId: String,
    soundPlayer: SoundEffectPlayer,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.ModeSelect.route
    ) {
        composable(route = Screen.ModeSelect.route) {
            ModeSelectScreen(
                practiceFilterState = koinInject<PracticeFilterState>(),
                questionBank = koinInject<QuestionBank>(),
                onPlaySolo = {
                    val soloRoomId = "SOLO_${generateRandomId()}"
                    navController.navigate(Screen.GameGraph.createRoute(soloRoomId))
                },
                onHostGame = {
                    val hostRoomId = "$HOST_ROOM_PREFIX${generateRandomId()}"
                    navController.navigate(Screen.RoomGraph.createRoute(hostRoomId))
                },
                onJoinGame = {
                    val joinRoomId = "$JOIN_ROOM_PREFIX${generateRandomId()}"
                    navController.navigate(Screen.RoomGraph.createRoute(joinRoomId))
                }
            )
        }

        composable(
            route = Screen.RoomGraph.route,
            arguments = listOf(navArgument(Screen.ARG_ROOM_ID) { type = NavType.StringType })
        ) { backStackEntry ->
            val roomId = backStackEntry.arguments?.getString(Screen.ARG_ROOM_ID).orEmpty()
            RoomScreen(
                viewModel = koinViewModel(parameters = { parametersOf(localPlayerId) }),
                roomId = roomId,
                localPlayerId = localPlayerId,
                onNavigateToGame = { targetRoomId ->
                    navController.navigate(Screen.GameGraph.createRoute(targetRoomId)) {
                        popUpTo(Screen.RoomGraph.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.GameGraph.route,
            arguments = listOf(navArgument(Screen.ARG_ROOM_ID) { type = NavType.StringType })
        ) { backStackEntry ->
            val roomId = backStackEntry.arguments?.getString(Screen.ARG_ROOM_ID).orEmpty()
            GameScreen(
                viewModel = koinViewModel(parameters = { parametersOf(roomId) }),
                soundPlayer = soundPlayer,
                onNavigateToResults = { finishedRoomId, score, correct, total ->
                    navController.navigate(
                        Screen.ResultsGraph.createRoute(finishedRoomId, score, correct, total)
                    ) {
                        // Drop Game (and Room, if present) from the backstack - back from
                        // Results should land on ModeSelect, not a finished game.
                        popUpTo(Screen.ModeSelect.route) { inclusive = false }
                    }
                }
            )
        }

        composable(
            route = Screen.ResultsGraph.route,
            arguments = listOf(
                navArgument(Screen.ARG_ROOM_ID) { type = NavType.StringType },
                navArgument(Screen.ARG_SCORE) { type = NavType.IntType },
                navArgument(Screen.ARG_CORRECT) { type = NavType.IntType },
                navArgument(Screen.ARG_TOTAL) { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val finishedRoomId = backStackEntry.arguments?.getString(Screen.ARG_ROOM_ID).orEmpty()
            val score = backStackEntry.arguments?.getInt(Screen.ARG_SCORE) ?: 0
            val correct = backStackEntry.arguments?.getInt(Screen.ARG_CORRECT) ?: 0
            val total = backStackEntry.arguments?.getInt(Screen.ARG_TOTAL) ?: 0
            ResultsScreen(
                score = score,
                correctCount = correct,
                totalCount = total,
                onPlayAgain = {
                    // Multiplayer replays through the lobby (joinRoom() resets to a clean
                    // session, Ready-up drives startMatch()/waiting again) - and preserves
                    // whichever role you had, since hosting and joining aren't interchangeable.
                    val nextRoomId = when {
                        finishedRoomId.startsWith(HOST_ROOM_PREFIX) -> "$HOST_ROOM_PREFIX${generateRandomId()}"
                        finishedRoomId.startsWith(JOIN_ROOM_PREFIX) -> "$JOIN_ROOM_PREFIX${generateRandomId()}"
                        else -> null
                    }
                    if (nextRoomId != null) {
                        navController.navigate(Screen.RoomGraph.createRoute(nextRoomId)) {
                            popUpTo(Screen.ModeSelect.route) { inclusive = false }
                        }
                    } else {
                        val soloRoomId = "SOLO_${generateRandomId()}"
                        navController.navigate(Screen.GameGraph.createRoute(soloRoomId)) {
                            popUpTo(Screen.ModeSelect.route) { inclusive = false }
                        }
                    }
                },
                onChangeCategory = {
                    navController.popBackStack(route = Screen.ModeSelect.route, inclusive = false)
                }
            )
        }
    }
}

private fun generateRandomId(): String = 
    (1..12).map { (('a'..'z') + ('A'..'Z') + ('0'..'9')).random() }.joinToString("")
