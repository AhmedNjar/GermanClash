package com.example.germanclash.domain.usecase

import com.example.germanclash.domain.repository.GameRepository

class AdvanceToNextQuestionUseCase(
    private val gameRepository: GameRepository
) {
    suspend operator fun invoke(roomId: String) = gameRepository.advanceToNextQuestion(roomId)
}
