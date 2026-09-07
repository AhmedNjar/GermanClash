package com.example.germanclash.domain.usecase

import com.example.germanclash.domain.model.GameFormat
import com.example.germanclash.domain.repository.GameRepository

class UpdateSettingsUseCase(
    private val gameRepository: GameRepository
) {
    suspend operator fun invoke(roomId: String, format: GameFormat, timeLimitMs: Long, category: String?) {
        gameRepository.updateSettings(roomId, format, timeLimitMs, category)
    }
}
