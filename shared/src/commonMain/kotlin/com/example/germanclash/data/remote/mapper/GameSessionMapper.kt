package com.example.germanclash.data.remote.mapper

import com.example.germanclash.data.remote.dto.AnswerDto
import com.example.germanclash.data.remote.dto.GameSessionDto
import com.example.germanclash.data.remote.dto.RoundResultDto
import com.example.germanclash.domain.model.AnswerOption
import com.example.germanclash.domain.model.Question
import com.example.germanclash.domain.model.GameSession
import com.example.germanclash.domain.model.GameType
import com.example.germanclash.domain.model.Player
import com.example.germanclash.domain.model.RoundResult


class GameSessionMapper {

    fun toDomain(dto: GameSessionDto): GameSession = GameSession(
        roomId = dto.roomId,
        players = dto.players.map { Player(it.id, it.displayName, it.score, it.isReady) },
        currentQuestion = dto.currentQuestion?.let { q ->
            Question(
                id = q.id,
                type = GameType.valueOf(q.type),
                prompt = q.prompt,
                imageUrl = q.imageUrl,
                options = q.options.map { AnswerOption(it.id, it.text) },
                scrambledWords = q.scrambledWords,
                correctAnswerId = q.correctAnswerId
            )
        },
        timeRemainingMs = dto.timeRemainingMs,
        timeLimitMs = dto.timeLimitMs,
        isFinished = dto.isFinished
    )

    fun toAnswerDto(playerId: String, answerId: String): AnswerDto =
        AnswerDto(playerId = playerId, answerId = answerId)

    fun toRoundResult(dto: RoundResultDto): RoundResult =
        RoundResult(
            wasCorrect = dto.wasCorrect,
            correctAnswerId = dto.correctAnswerId,
            pointsAwarded = dto.pointsAwarded
        )
}
