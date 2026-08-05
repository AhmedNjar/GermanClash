package com.example.germanclash.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class GameSession(
    val roomId: String,
    val players: List<Player>,
    val currentQuestion: Question?,
    val timeRemainingMs: Long,
    val timeLimitMs: Long,
    val isFinished: Boolean = false
)