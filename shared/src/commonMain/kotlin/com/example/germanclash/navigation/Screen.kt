package com.example.germanclash.navigation

sealed class Screen(val route: String) {
    data object ModeSelect : Screen("mode_select")

    data object RoomGraph : Screen("room/{$ARG_ROOM_ID}") {
        fun createRoute(roomId: String) = "room/$roomId"
    }

    data object GameGraph : Screen("game/{$ARG_ROOM_ID}") {
        fun createRoute(roomId: String) = "game/$roomId"
    }

    companion object {
        const val ARG_ROOM_ID = "roomId"
    }
}