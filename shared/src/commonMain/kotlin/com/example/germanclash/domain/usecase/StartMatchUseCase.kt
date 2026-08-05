package com.example.germanclash.domain.usecase

import com.example.germanclash.domain.repository.GameRepository

class StartMatchUseCase(
    private val gameRepository: GameRepository
) {
    suspend operator fun invoke(roomId: String) = gameRepository.startMatch(roomId)
}
