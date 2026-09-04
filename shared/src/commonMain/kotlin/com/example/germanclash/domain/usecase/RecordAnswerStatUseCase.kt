package com.example.germanclash.domain.usecase

import com.example.germanclash.domain.repository.LocalStatsRepository


class RecordAnswerStatUseCase(
    private val localStatsRepository: LocalStatsRepository
) {
    operator fun invoke(category: String, wasCorrect: Boolean) {
        localStatsRepository.recordAnswer(category, wasCorrect)
    }
}