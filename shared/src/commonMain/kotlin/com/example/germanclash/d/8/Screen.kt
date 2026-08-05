package com.app.germanclash.navigation

sealed class Screen(val route: String) {
    data object ModeSelect : Screen("mode_select")

    data object RoomGraph : Screen("room/{$ARG_ROOM_ID}") {
        fun createRoute(roomId: String) = "room/$roomId"
    }

    data object GameGraph : Screen("game/{$ARG_ROOM_ID}") {
        fun createRoute(roomId: String) = "game/$roomId"
    }

    data object ResultsGraph : Screen("results/{$ARG_ROOM_ID}/{$ARG_SCORE}/{$ARG_CORRECT}/{$ARG_TOTAL}") {
        fun createRoute(roomId: String, score: Int, correct: Int, total: Int) = "results/$roomId/$score/$correct/$total"
    }

    companion object {
        const val ARG_ROOM_ID = "roomId"
        const val ARG_SCORE = "score"
        const val ARG_CORRECT = "correct"
        const val ARG_TOTAL = "total"
    }
}
