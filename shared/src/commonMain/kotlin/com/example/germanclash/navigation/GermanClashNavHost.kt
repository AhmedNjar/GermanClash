package com.example.germanclash.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.germanclash.presentation.common.SoundEffectPlayer
import com.example.germanclash.presentation.feature.game.GameScreen
import com.example.germanclash.presentation.feature.modeselect.ModeSelectScreen
import com.example.germanclash.presentation.feature.room.RoomScreen
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Every screen's ViewModel comes from FeatureModule's parameterized `viewModel { (id) -> ... }`
 * factories, so navigating to a new roomId always gets a fresh ViewModel instance rather than
 * a stale one carried over from the previous room.
 */
@Composable
fun GermanClashNavHost(
    friendsRoomId: String,
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
                onPlaySolo = {
                    val soloRoomId = "SOLO_${(0..99999).random()}"
                    navController.navigate(Screen.GameGraph.createRoute(soloRoomId))
                },
                onPlayWithFriends = {
                    navController.navigate(Screen.RoomGraph.createRoute(friendsRoomId))
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
                onNavigateToResults = {
                    navController.popBackStack(route = Screen.ModeSelect.route, inclusive = false)
                }
            )
        }
    }
}
