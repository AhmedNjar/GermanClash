package com.example.germanclash.navigation

sealed class Screen(val route: String) {
    data object ModeSelect : Screen("mode_select")
    
    data object Home : Screen("home")

    data object StatsGraph : Screen("stats")

    data object Leaderboard : Screen("leaderboard")

    data object RoomGraph : Screen("room/{$ARG_ROOM_ID}") {
        fun createRoute(roomId: String) = "room/$roomId"
    }

    data object GameGraph : Screen("game/{$ARG_ROOM_ID}") {
        fun createRoute(roomId: String) = "game/$roomId"
    }

    data object ResultsGraph : Screen(
        "results/{$ARG_ROOM_ID}/{$ARG_SCORE}/{$ARG_CORRECT}/{$ARG_TOTAL}/{$ARG_DAILY_BEST}?$ARG_PLAYERS={$ARG_PLAYERS}"
    ) {
        fun createRoute(
            roomId: String,
            score: Int,
            correct: Int,
            total: Int,
            dailyBestScore: Int?,
            playersEncoded: String
        ): String {
            // Manual character replacement for URL safety in KMP commonMain.
            // This ensures that the navigation arguments don't break the NavHost route matching.
            val safePlayers = playersEncoded
                .replace("%", "%25")
                .replace(" ", "%20")
                .replace("&", "%26")
                .replace("?", "%3F")
                .replace("=", "%3D")
                .replace("+", "%2B")
                .replace("#", "%23")
                .replace("/", "%2F")
            
            return "results/$roomId/$score/$correct/$total/${dailyBestScore ?: -1}?$ARG_PLAYERS=$safePlayers"
        }
    }

    companion object {
        const val ARG_ROOM_ID = "roomId"
        const val ARG_SCORE = "score"
        const val ARG_CORRECT = "correct"
        const val ARG_TOTAL = "total"
        const val ARG_DAILY_BEST = "dailyBest"
        const val ARG_PLAYERS = "players"
    }
}
