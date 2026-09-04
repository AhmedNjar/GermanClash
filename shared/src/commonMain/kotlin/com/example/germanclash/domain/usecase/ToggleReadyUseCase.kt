package com.example.germanclash.domain.usecase

import com.example.germanclash.domain.repository.GameRepository

class ToggleReadyUseCase(
    private val gameRepository: GameRepository
) {
    suspend operator fun invoke(roomId: String, playerId: String, isReady: Boolean) {
        gameRepository.toggleReady(roomId, playerId, isReady)
    }
}
