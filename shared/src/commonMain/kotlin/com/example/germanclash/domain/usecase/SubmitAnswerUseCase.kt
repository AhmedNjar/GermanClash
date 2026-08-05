package com.example.germanclash.domain.usecase

import com.example.germanclash.domain.model.RoundResult
import com.example.germanclash.domain.repository.GameRepository

class SubmitAnswerUseCase(
    private val gameRepository: GameRepository
) {
    suspend operator fun invoke(roomId: String, answerId: String): RoundResult =
        gameRepository.submitAnswer(roomId, answerId)
}