package com.example.germanclash.presentation.feature.game

import com.example.germanclash.domain.model.GameType
import com.example.germanclash.domain.model.Player
import com.example.germanclash.domain.model.Question
import com.example.germanclash.domain.model.RoundResult


/**
 * Single immutable snapshot of the Game screen at any point in time.
 * The ViewModel is the ONLY thing that constructs this.
 */
data class GameUiState(
    val isLoading: Boolean = true,
    val currentQuestion: Question? = null,
    val gameType: GameType = GameType.DER_DIE_DAS,
    val timeRemainingMs: Long = 0L,
    val timeLimitMs: Long = 10_000L,
    val players: List<Player> = emptyList(),
    val localPlayerScore: Int = 0,
    val selectedAnswerId: String? = null,   // optimistic UI highlight
    val isAnswerLocked: Boolean = false,     // prevents double-submit while awaiting result
    val roundResult: RoundResult? = null,    // null until server/peer confirms
    val wordBank: List<String> = emptyList(),       // SENTENCE_BUILDER: words not yet placed
    val assembledWords: List<String> = emptyList(), // SENTENCE_BUILDER: words placed so far, in order
    val matchCards: List<MatchCard> = emptyList(),  // MATCH_PAIRS: the shuffled deck
    val flippedCardIds: List<String> = emptyList(), // MATCH_PAIRS: currently face-up, unmatched
    val isEvaluatingMismatch: Boolean = false,      // MATCH_PAIRS: brief lock while a wrong pair is shown
    val error: String? = null
) {
    val timerProgress: Float
        get() = if (timeLimitMs == 0L) 0f else (timeRemainingMs.toFloat() / timeLimitMs).coerceIn(0f, 1f)
}

/**
 * One shuffled, flippable card. Built by pairing up each domain CardPair's
 * front/back into two cards sharing a pairId - purely a presentation-layer
 * shape, since shuffle order has no meaning outside the UI.
 */
data class MatchCard(
    val id: String,
    val pairId: String,
    val content: String,
    val isMatched: Boolean = false
)

/**
 * Every possible user action or system trigger. Sealed interface so the
 * `when` in the ViewModel is exhaustive and compiler-enforced.
 */
sealed interface GameIntent {
    data object LoadGame : GameIntent
    data class SelectAnswer(val answerId: String) : GameIntent
    data class SelectWord(val word: String) : GameIntent    // SENTENCE_BUILDER: tap a word into the sentence
    data object RemoveLastWord : GameIntent                  // SENTENCE_BUILDER: undo the last placed word
    data class FlipCard(val cardId: String) : GameIntent      // MATCH_PAIRS
    data object TimerExpired : GameIntent
    data object NextQuestion : GameIntent
    data object LeaveGame : GameIntent
    data object RetryAfterError : GameIntent
}

/**
 * One-shot, non-state events. Kept out of GameUiState because replaying
 * state (e.g. on rotation) must never re-trigger a haptic buzz or replay
 * a sound. Collected via SharedFlow with no replay.
 */
sealed interface GameEffect {
    data class PlayHaptic(val pattern: HapticPattern) : GameEffect
    data class PlaySound(val sound: SoundEffect) : GameEffect
    data object ShowConfetti : GameEffect
    data class NavigateToResults(val roomId: String) : GameEffect
    data class ShowToast(val message: String) : GameEffect
}

enum class HapticPattern { LIGHT_TICK, SUCCESS, ERROR }
enum class SoundEffect { CORRECT, WRONG, COUNTDOWN_TICK, VICTORY }
