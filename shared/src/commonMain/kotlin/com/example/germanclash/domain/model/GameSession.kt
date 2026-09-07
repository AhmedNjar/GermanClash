package com.example.germanclash.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class GameSession(
    val roomId: String,
    val players: List<Player>,
    val currentQuestion: Question?,
    val timeRemainingMs: Long,
    val timeLimitMs: Long,
    val isFinished: Boolean = false,
    val currentQuestionNumber: Int = 0,
    val sessionLength: Int? = null, // null = unbounded; a number = finish after that many questions
    val category: String? = null,
    val connectionStatus: ConnectionStatus = ConnectionStatus.CONNECTED,
    val format: GameFormat = GameFormat.CLASSIC
)
