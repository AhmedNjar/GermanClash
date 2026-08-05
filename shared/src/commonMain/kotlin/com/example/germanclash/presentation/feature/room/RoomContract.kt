package com.example.germanclash.presentation.feature.room

import com.example.germanclash.domain.model.Player

data class RoomUiState(
    val roomId: String = "",
    val players: List<Player> = emptyList(),
    val isLocalPlayerReady: Boolean = false,
    val isJoining: Boolean = true,
    val error: String? = null
)

sealed interface RoomIntent {
    data class JoinRoom(val roomId: String, val playerId: String) : RoomIntent
    data object ToggleReady : RoomIntent
    data object LeaveRoom : RoomIntent
    data object RetryJoin : RoomIntent
}

sealed interface RoomEffect {
    data class NavigateToGame(val roomId: String) : RoomEffect
    data class ShowToast(val message: String) : RoomEffect
}
