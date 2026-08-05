package com.example.germanclash.data.remote.dto

import kotlinx.serialization.Serializable


@Serializable
data class GameSessionDto(
    val roomId: String = "",
    val players: List<PlayerDto> = emptyList(),
    val currentQuestion: QuestionDto? = null,
    val timeRemainingMs: Long = 0L,
    val timeLimitMs: Long = 0L,
    val isFinished: Boolean = false,
    val lastResults: Map<String, RoundResultDto> = emptyMap() // playerId -> their most recent scored answer
)

@Serializable
data class RoundResultDto(
    val questionId: String = "",   // lets the client ignore a stale result from the previous question
    val wasCorrect: Boolean = false,
    val correctAnswerId: String = "",
    val pointsAwarded: Int = 0
)

@Serializable
data class PlayerDto(
    val id: String = "",
    val displayName: String = "",
    val score: Int = 0,
    val isReady: Boolean = false
)

@Serializable
data class QuestionDto(
    val id: String = "",
    val type: String = "",   // GameType enum name
    val prompt: String = "",
    val imageUrl: String? = null,
    val options: List<AnswerOptionDto> = emptyList(),
    val scrambledWords: List<String> = emptyList(),
    val correctAnswerId: String? = null
    // cardPairs field added here once MATCH_PAIRS is built
)

@Serializable
data class AnswerOptionDto(
    val id: String = "",
    val text: String = ""
)

@Serializable
data class AnswerDto(
    val playerId: String = "",
    val answerId: String = ""
)
