package com.example.germanclash.domain.usecase

import com.example.germanclash.core.result.Result
import com.example.germanclash.domain.repository.GameRepository


class JoinRoomUseCase(
    private val gameRepository: GameRepository,
) {
    suspend operator fun invoke(roomId: String, playerId: String): Result<Unit> =
        gameRepository.joinRoom(roomId, playerId)
}