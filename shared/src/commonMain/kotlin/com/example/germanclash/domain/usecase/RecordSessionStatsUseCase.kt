package com.example.germanclash.domain.usecase

import com.example.germanclash.core.contracts.DateProvider
import com.example.germanclash.domain.repository.LocalStatsRepository

data class SessionStatsResult(
    val dailyBestScore: Int?,
    val dailyPlayStreak: Int
)

class RecordSessionStatsUseCase(
    private val localStatsRepository: LocalStatsRepository,
    private val dateProvider: DateProvider
) {
    operator fun invoke(isDailyChallenge: Boolean, finalScore: Int, bestStreakThisSession: Int): SessionStatsResult {
        val dateKey = dateProvider.todayKey()
        val dailyBest = if (isDailyChallenge) localStatsRepository.recordDailyScore(dateKey, finalScore) else null
        localStatsRepository.recordStreak(bestStreakThisSession)
        val dailyPlayStreak = localStatsRepository.recordPlaySession(dateKey)
        return SessionStatsResult(dailyBestScore = dailyBest, dailyPlayStreak = dailyPlayStreak)
    }
}
