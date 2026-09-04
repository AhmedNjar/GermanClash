package com.example.germanclash.data.local.platform

/*import android.content.Context*/
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.germanclash.domain.model.CategoryStats
import com.example.germanclash.domain.repository.LocalStatsRepository
import java.time.LocalDate
import kotlinx.serialization.json.Json
import kotlin.collections.toMutableMap

class AndroidLocalStatsRepository(context: Context) : LocalStatsRepository {

    private val prefs = context.getSharedPreferences("germanclash_stats", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    override fun getDailyBestScore(dateKey: String): Int? {
        val key = dailyBestKey(dateKey)
        return if (prefs.contains(key)) prefs.getInt(key, 0) else null
    }

    override fun recordDailyScore(dateKey: String, finalScore: Int): Int {
        val previousBest = getDailyBestScore(dateKey)
        val newBest = if (previousBest == null || finalScore > previousBest) finalScore else previousBest
        prefs.edit().putInt(dailyBestKey(dateKey), newBest).apply()
        return newBest
    }

    override fun getBestStreak(): Int = prefs.getInt(KEY_BEST_STREAK, 0)

    override fun recordStreak(streak: Int) {
        if (streak > getBestStreak()) {
            prefs.edit().putInt(KEY_BEST_STREAK, streak).apply()
        }
    }

    override fun getTotalCorrectAnswers(): Int = prefs.getInt(KEY_TOTAL_CORRECT, 0)

    override fun recordAnswer(category: String, wasCorrect: Boolean) {
        val stats = getCategoryStats().toMutableMap()
        val current = stats[category] ?: CategoryStats()
        stats[category] = current.copy(
            correct = current.correct + if (wasCorrect) 1 else 0,
            total = current.total + 1
        )
        val editor = prefs.edit().putString(KEY_CATEGORY_STATS, json.encodeToString(stats.toMap()))
        if (wasCorrect) {
            editor.putInt(KEY_TOTAL_CORRECT, getTotalCorrectAnswers() + 1)
        }
        editor.apply()
    }

    override fun getCategoryStats(): Map<String, CategoryStats> {
        val raw = prefs.getString(KEY_CATEGORY_STATS, null) ?: return emptyMap()
        return try {
            json.decodeFromString<Map<String, CategoryStats>>(raw)
        } catch (e: Exception) {
            emptyMap()
        }
    }

    override fun getDailyPlayStreak(): Int = prefs.getInt(KEY_DAILY_PLAY_STREAK, 0)

    @RequiresApi(Build.VERSION_CODES.O)
    override fun recordPlaySession(dateKey: String): Int {
        val lastPlayed = prefs.getString(KEY_LAST_PLAYED_DATE, null)
        val currentStreak = getDailyPlayStreak()

        val newStreak = when {
            lastPlayed == dateKey -> currentStreak.coerceAtLeast(1) // already played today
            lastPlayed == null -> 1 // first time ever
            else -> {
                val yesterday = LocalDate.parse(dateKey).minusDays(1).toString()
                if (lastPlayed == yesterday) currentStreak + 1 else 1
            }
        }

        prefs.edit()
            .putString(KEY_LAST_PLAYED_DATE, dateKey)
            .putInt(KEY_DAILY_PLAY_STREAK, newStreak)
            .apply()
        return newStreak
    }

    private fun dailyBestKey(dateKey: String) = "daily_best_$dateKey"

    companion object {
        private const val KEY_BEST_STREAK = "best_streak"
        private const val KEY_TOTAL_CORRECT = "total_correct"
        private const val KEY_CATEGORY_STATS = "category_stats"
        private const val KEY_DAILY_PLAY_STREAK = "daily_play_streak"
        private const val KEY_LAST_PLAYED_DATE = "last_played_date"
    }
}