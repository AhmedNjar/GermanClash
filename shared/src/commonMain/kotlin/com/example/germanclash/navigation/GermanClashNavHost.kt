package com.example.germanclash.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.germanclash.domain.model.Player
import com.example.germanclash.domain.usecase.GetStatsSummaryUseCase
import com.example.germanclash.presentation.feature.game.GameScreen
import com.example.germanclash.data.local.questionbank.PracticeFilterState
import com.example.germanclash.data.local.questionbank.QuestionBank
import com.example.germanclash.presentation.common.SoundEffectPlayer
import com.example.germanclash.presentation.feature.home.HomeScreen
import com.example.germanclash.presentation.feature.leaderboard.LeaderboardScreen
import com.example.germanclash.presentation.feature.modeselect.ModeSelectScreen
import com.example.germanclash.presentation.feature.results.ResultsScreen
import com.example.germanclash.presentation.feature.room.RoomScreen
import com.example.germanclash.presentation.feature.stats.StatsScreen
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

private const val HOST_ROOM_PREFIX = "LOCAL_HOST_"
private const val JOIN_ROOM_PREFIX = "LOCAL_JOIN_"

/**
 * Robustly encode a player list into a URL-safe string.
 * Uses a simple hex-encoding for names to prevent issues with characters like ':' or ','.
 */
private fun encodePlayers(players: List<Player>): String =
    players.joinToString(",") { player ->
        val hexName = player.displayName.encodeToByteArray().joinToString("") { it.toUByte().toString(16).padStart(2, '0') }
        "$hexName:${player.score}"
    }

/**
 * Robustly decode a player list from a URL-safe string.
 */
private fun decodePlayers(encoded: String): List<Pair<String, Int>> {
    if (encoded.isBlank()) return emptyList()
    
    return encoded.split(",").mapNotNull { entry ->
        val parts = entry.split(":")
        if (parts.size == 2) {
            val hexName = parts[0]
            val score = parts[1].toIntOrNull() ?: 0
            
            // Hex to String
            val name = try {
                val bytes = ByteArray(hexName.length / 2)
                for (i in 0 until hexName.length step 2) {
                    bytes[i / 2] = hexName.substring(i, i + 2).toInt(16).toByte()
                }
                bytes.decodeToString()
            } catch (e: Exception) {
                "Player"
            }
            
            name to score
        } else null
    }
}

/**
 * Every screen's ViewModel comes from FeatureModule's parameterized `viewModel { (id) -> ... }`
 * factories, so navigating to a new roomId always gets a fresh ViewModel instance rather than
 * a stale one carried over from the previous room.
 */
@Composable
fun GermanClashNavHost(
    localPlayerId: String,
    soundPlayer: SoundEffectPlayer,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(route = Screen.Home.route) {
            HomeScreen(
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
                },
                onViewLeaderboard = {
                    navController.navigate(Screen.Leaderboard.route)
                }
            )
        }

        composable(route = Screen.ModeSelect.route) {
            ModeSelectScreen(
                practiceFilterState = koinInject<PracticeFilterState>(),
                questionBank = koinInject<QuestionBank>(),
                getStatsSummary = koinInject<GetStatsSummaryUseCase>(),
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
                },
                onViewStats = {
                    navController.navigate(Screen.StatsGraph.route)
                }
            )
        }

        composable(route = Screen.StatsGraph.route) {
            val getStatsSummary = koinInject<GetStatsSummaryUseCase>()
            StatsScreen(
                statsSummary = getStatsSummary(),
                onBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.Leaderboard.route) {
            LeaderboardScreen(
                onBack = { navController.popBackStack() }
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
                localPlayerId = localPlayerId,
                onNavigateToResults = { finishedRoomId, score, correct, total, dailyBestScore, players ->
                    navController.navigate(
                        Screen.ResultsGraph.createRoute(
                            roomId = finishedRoomId,
                            score = score,
                            correct = correct,
                            total = total,
                            dailyBestScore = dailyBestScore,
                            playersEncoded = encodePlayers(players)
                        )
                    ) {
                        // Drop Game (and Room, if present) from the backstack - back from
                        // Results should land on Home, not a finished game.
                        popUpTo(Screen.Home.route) { inclusive = false }
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
                navArgument(Screen.ARG_TOTAL) { type = NavType.IntType },
                navArgument(Screen.ARG_DAILY_BEST) { type = NavType.IntType },
                navArgument(Screen.ARG_PLAYERS) {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val finishedRoomId = backStackEntry.arguments?.getString(Screen.ARG_ROOM_ID).orEmpty()
            val score = backStackEntry.arguments?.getInt(Screen.ARG_SCORE) ?: 0
            val correct = backStackEntry.arguments?.getInt(Screen.ARG_CORRECT) ?: 0
            val total = backStackEntry.arguments?.getInt(Screen.ARG_TOTAL) ?: 0
            val dailyBestArg = backStackEntry.arguments?.getInt(Screen.ARG_DAILY_BEST) ?: -1
            val playersArg = backStackEntry.arguments?.getString(Screen.ARG_PLAYERS).orEmpty()
            val isMultiplayer = finishedRoomId.startsWith(HOST_ROOM_PREFIX) || finishedRoomId.startsWith(JOIN_ROOM_PREFIX)

            ResultsScreen(
                score = score,
                correctCount = correct,
                totalCount = total,
                dailyBestScore = dailyBestArg.takeIf { it >= 0 },
                isMultiplayer = isMultiplayer,
                matchPlayers = decodePlayers(playersArg),
                onPlayAgain = {
                    // Multiplayer replays through the lobby (joinRoom() resets to a clean
                    // session, Ready-up drives startMatch()/waiting again) - and preserves
                    // whichever role you had, since hosting and joining aren't interchangeable.
                    
                    // REUSE THE SAME ROOM ID for LOCAL multiplayer to avoid discovery reset!
                    val nextRoomId = if (finishedRoomId.startsWith("LOCAL_")) finishedRoomId else null
                    
                    if (nextRoomId != null) {
                        navController.navigate(Screen.RoomGraph.createRoute(nextRoomId)) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                        }
                    } else {
                        val soloRoomId = "SOLO_${generateRandomId()}"
                        navController.navigate(Screen.GameGraph.createRoute(soloRoomId)) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                        }
                    }
                },
                onChangeCategory = {
                    navController.popBackStack(route = Screen.Home.route, inclusive = false)
                }
            )
        }
    }
}

private fun generateRandomId(): String = 
    (1..12).map { (('a'..'z') + ('A'..'Z') + ('0'..'9')).random() }.joinToString("")
