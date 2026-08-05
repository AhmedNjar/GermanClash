package com.app.germanclash.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class GameSession(
    val roomId: String,
    val players: List<Player>,
    val currentQuestion: Question?,
    val timeRemainingMs: Long,
    val timeLimitMs: Long,
    val isFinished: Boolean = false,
    val sessionLength: Int? = null, // null = unbounded; a number = finish after that many questions
    val connectionStatus: ConnectionStatus = ConnectionStatus.CONNECTED
)
