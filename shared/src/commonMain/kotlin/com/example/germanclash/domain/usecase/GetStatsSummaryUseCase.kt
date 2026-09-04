package com.example.germanclash.domain.usecase


import com.example.germanclash.domain.model.CategoryStats
import com.example.germanclash.domain.repository.LocalStatsRepository

data class StatsSummary(
    val bestStreak: Int,
    val dailyPlayStreak: Int,
    val totalCorrectAnswers: Int,
    val categoryStats: Map<String, CategoryStats>
)

class GetStatsSummaryUseCase(
    private val localStatsRepository: LocalStatsRepository
) {
    operator fun invoke(): StatsSummary = StatsSummary(
        bestStreak = localStatsRepository.getBestStreak(),
        dailyPlayStreak = localStatsRepository.getDailyPlayStreak(),
        totalCorrectAnswers = localStatsRepository.getTotalCorrectAnswers(),
        categoryStats = localStatsRepository.getCategoryStats()
    )
}
