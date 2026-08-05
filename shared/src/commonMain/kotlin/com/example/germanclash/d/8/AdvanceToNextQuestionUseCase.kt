package com.app.germanclash.domain.usecase

import com.app.germanclash.domain.repository.GameRepository

class AdvanceToNextQuestionUseCase(
    private val gameRepository: GameRepository
) {
    suspend operator fun invoke(roomId: String) = gameRepository.advanceToNextQuestion(roomId)
}
