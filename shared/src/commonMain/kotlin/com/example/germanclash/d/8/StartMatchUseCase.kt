package com.app.germanclash.domain.usecase

import com.app.germanclash.domain.repository.GameRepository

class StartMatchUseCase(
    private val gameRepository: GameRepository
) {
    suspend operator fun invoke(roomId: String) = gameRepository.startMatch(roomId)
}
