package com.example.germanclash.domain.usecase

import com.example.germanclash.domain.model.GameSession
import com.example.germanclash.domain.repository.GameRepository
import kotlinx.coroutines.flow.Flow

class ObserveGameSessionUseCase(
    private val gameRepository: GameRepository
) {
    operator fun invoke(roomId: String): Flow<GameSession> =
        gameRepository.observeSession(roomId)
}