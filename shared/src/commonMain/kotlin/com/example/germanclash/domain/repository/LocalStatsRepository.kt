package com.example.germanclash.domain.repository

import com.example.germanclash.domain.model.CategoryStats

/**
 * Purely local, per-device - there's no reachable backend today to compare
 * against other players (see the Nearby/Firestore notes elsewhere), so
 * "Daily Challenge" means "the same question set as everyone, see if you
 * can beat your own best" rather than a real leaderboard.
 */
interface LocalStatsRepository {
    fun getDailyBestScore(dateKey: String): Int?

    /** Records finalScore for dateKey if it beats the existing best; returns the resulting best. */
    fun recordDailyScore(dateKey: String, finalScore: Int): Int

    fun getBestStreak(): Int

    /** Updates the all-time best streak if streak beats it. */
    fun recordStreak(streak: Int)

    fun getTotalCorrectAnswers(): Int

    /** Increments the category's correct/total counters, and the all-time correct counter if wasCorrect. */
    fun recordAnswer(category: String, wasCorrect: Boolean)

    fun getCategoryStats(): Map<String, CategoryStats>

    fun getDailyPlayStreak(): Int

    /** Call once per completed session. Returns the resulting daily play streak. */
    fun recordPlaySession(dateKey: String): Int
}