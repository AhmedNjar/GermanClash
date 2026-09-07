package com.example.germanclash.presentation.feature.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.germanclash.domain.model.*
import com.example.germanclash.domain.usecase.AdvanceToNextQuestionUseCase
import com.example.germanclash.domain.usecase.ObserveGameSessionUseCase
import com.example.germanclash.domain.usecase.RecordAnswerStatUseCase
import com.example.germanclash.domain.usecase.RecordSessionStatsUseCase
import com.example.germanclash.domain.usecase.SubmitAnswerUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class GameViewModel(
    private val roomId: String,
    private val observeGameSession: ObserveGameSessionUseCase,
    private val submitAnswer: SubmitAnswerUseCase,
    private val advanceToNextQuestion: AdvanceToNextQuestionUseCase,
    private val recordAnswerStat: RecordAnswerStatUseCase,
    private val recordSessionStats: RecordSessionStatsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(GameUiState())
    val state: StateFlow<GameUiState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<GameEffect>()
    val effect: SharedFlow<GameEffect> = _effect

    private var lastQuestionId: String? = null
    private var countdownJob: Job? = null
    private var isNavigatingToResults = false

    // Session progress - questionsSeen counts questions actually shown so far
    // (including the one currently in progress); correctAnswers counts only
    // the ones scored wasCorrect = true. Both drive the Results screen.
    private var correctAnswers = 0

    // Consecutive correct answers, in a row, resetting to 0 on any wrong
    // answer or timeout. Only exact milestone values (see streakBonusFor)
    // award a bonus, so the "Streak x5!" banner lines up with an actual
    // points bump rather than firing every single round after the first.
    private var currentStreak = 0

    // The HIGHEST currentStreak reached anywhere in this session - not the
    // same as currentStreak at session end, which could be 0 if the last
    // answer happened to be wrong even after a big earlier run. This is
    // what gets compared against the all-time best streak.
    private var bestStreakThisSession = 0

    fun onIntent(intent: GameIntent) {
        when (intent) {
            GameIntent.LoadGame -> loadGame()
            is GameIntent.SelectAnswer -> selectAnswer(intent.answerId)
            is GameIntent.SelectWord -> selectWord(intent.word)
            GameIntent.RemoveLastWord -> removeLastWord()
            is GameIntent.FlipCard -> flipCard(intent.cardId)
            GameIntent.TimerExpired -> lockRoundAsIncorrect()
            GameIntent.NextQuestion -> advanceQuestion()
            GameIntent.LeaveGame -> {
                if (!isNavigatingToResults) {
                    isNavigatingToResults = true
                    emitEffect(
                        GameEffect.NavigateToResults(
                            roomId = roomId,
                            finalScore = _state.value.localPlayerScore,
                            correctCount = correctAnswers,
                            totalCount = _state.value.currentQuestionNumber,
                            dailyBestScore = null, // Or fetch if relevant
                            players = _state.value.players
                        )
                    )
                }
            }
            GameIntent.RetryAfterError -> loadGame()
        }
    }

    private fun loadGame() {
        _state.value = _state.value.copy(isLoading = true, error = null)
        observeGameSession(roomId)
            .onEach { session ->
                if (session.isFinished) {
                    onIntent(GameIntent.LeaveGame)
                    return@onEach
                }
                
                val questionChanged = session.currentQuestion?.id != lastQuestionId
                lastQuestionId = session.currentQuestion?.id
                
                _state.value = _state.value.copy(
                    isLoading = false,
                    currentQuestion = session.currentQuestion,
                    gameType = session.currentQuestion?.type ?: _state.value.gameType,
                    players = session.players,
                    timeLimitMs = session.timeLimitMs,
                    selectedAnswerId = if (questionChanged) null else _state.value.selectedAnswerId,
                    isAnswerLocked = if (questionChanged) false else _state.value.isAnswerLocked,
                    roundResult = if (questionChanged) null else _state.value.roundResult,
                    wordBank = if (questionChanged) session.currentQuestion?.scrambledWords.orEmpty() else _state.value.wordBank,
                    assembledWords = if (questionChanged) emptyList() else _state.value.assembledWords,
                    matchCards = if (questionChanged) buildMatchDeck(session.currentQuestion) else _state.value.matchCards,
                    flippedCardIds = if (questionChanged) emptyList() else _state.value.flippedCardIds,
                    isEvaluatingMismatch = if (questionChanged) false else _state.value.isEvaluatingMismatch,
                    currentQuestionNumber = session.currentQuestionNumber,
                    sessionLength = session.sessionLength,
                    connectionStatus = session.connectionStatus,
                    format = session.format,
                    isFinished = session.isFinished
                )
                if (questionChanged && session.currentQuestion != null) {
                    // Reset selection state immediately on new question
                    _state.value = _state.value.copy(
                        selectedAnswerId = null,
                        isAnswerLocked = false,
                        roundResult = null
                    )
                    
                    if (session.format != com.example.germanclash.domain.model.GameFormat.TIME_ATTACK) {
                        startCountdown(session.timeLimitMs)
                    }
                }
                
                // If it IS Time Attack, we update timeRemainingMs directly from the session
                if (session.format == com.example.germanclash.domain.model.GameFormat.TIME_ATTACK) {
                    countdownJob?.cancel()
                    _state.value = _state.value.copy(timeRemainingMs = session.timeRemainingMs)
                }
            }
            .launchIn(viewModelScope)
    }

    /**
     * The client-side clock. Nothing else in the app was decrementing
     * timeRemainingMs - the ring looked frozen because there was no ticker.
     */
    private fun startCountdown(timeLimitMs: Long) {
        countdownJob?.cancel()
        _state.value = _state.value.copy(timeRemainingMs = timeLimitMs)
        countdownJob = viewModelScope.launch {
            var remaining = timeLimitMs
            while (remaining > 0) {
                delay(COUNTDOWN_TICK_MS)
                remaining = (remaining - COUNTDOWN_TICK_MS).coerceAtLeast(0)
                _state.value = _state.value.copy(timeRemainingMs = remaining)
            }
            onIntent(GameIntent.TimerExpired)
        }
    }

    private fun buildMatchDeck(question: Question?): List<MatchCard> {
        if (question == null || question.type != GameType.MATCH_PAIRS) return emptyList()
        return question.cardPairs.flatMap { pair: CardPair ->
            listOf(
                MatchCard(id = "${pair.id}_front", pairId = pair.id, content = pair.front),
                MatchCard(id = "${pair.id}_back", pairId = pair.id, content = pair.back)
            )
        }.shuffled()
    }

    private fun selectAnswer(answerId: String) {
        if (_state.value.isAnswerLocked) return   // no double-submit
        _state.value = _state.value.copy(selectedAnswerId = answerId, isAnswerLocked = true)
        submitAndReconcile(answerId)
    }

    private fun selectWord(word: String) {
        if (_state.value.isAnswerLocked) return
        val bank = _state.value.wordBank.toMutableList()
        val index = bank.indexOf(word)
        if (index == -1) return
        bank.removeAt(index)
        val assembled = _state.value.assembledWords + word
        _state.value = _state.value.copy(wordBank = bank, assembledWords = assembled)

        val question = _state.value.currentQuestion ?: return
        if (assembled.size == question.scrambledWords.size) {
            val sentence = assembled.joinToString(" ")
            _state.value = _state.value.copy(selectedAnswerId = sentence, isAnswerLocked = true)
            submitAndReconcile(sentence)
        }
    }

    private fun removeLastWord() {
        if (_state.value.isAnswerLocked) return
        val last = _state.value.assembledWords.lastOrNull() ?: return
        _state.value = _state.value.copy(
            assembledWords = _state.value.assembledWords.dropLast(1),
            wordBank = _state.value.wordBank + last
        )
    }

    private fun flipCard(cardId: String) {
        if (_state.value.isEvaluatingMismatch) return
        val cards = _state.value.matchCards
        val tapped = cards.find { it.id == cardId } ?: return
        if (tapped.isMatched || _state.value.flippedCardIds.contains(cardId)) return
        if (_state.value.flippedCardIds.size >= 2) return

        val flipped = _state.value.flippedCardIds + cardId
        _state.value = _state.value.copy(flippedCardIds = flipped)
        if (flipped.size < 2) return

        val first = cards.first { it.id == flipped[0] }
        val second = cards.first { it.id == flipped[1] }

        if (first.pairId == second.pairId) {
            val updated = cards.map {
                if (it.id == first.id || it.id == second.id) it.copy(isMatched = true) else it
            }
            _state.value = _state.value.copy(matchCards = updated, flippedCardIds = emptyList())
            emitEffect(GameEffect.PlayHaptic(HapticPattern.SUCCESS))
            emitEffect(GameEffect.ShowConfetti())
            if (updated.all { it.isMatched }) {
                submitAndReconcile("MATCH_PAIRS_COMPLETE")
            }
        } else {
            _state.value = _state.value.copy(isEvaluatingMismatch = true)
            emitEffect(GameEffect.PlayHaptic(HapticPattern.ERROR))
            viewModelScope.launch {
                delay(MISMATCH_REVEAL_MS)
                _state.value = _state.value.copy(flippedCardIds = emptyList(), isEvaluatingMismatch = false)
            }
        }
    }

    /**
     * The shared end-of-round path: score it, record the category stat,
     * show the result (with a streak-aware bonus and a floating score
     * popup), pause briefly so the player actually sees it, THEN either
     * move to the next question or - if this was the last one in the
     * session - go to Results instead.
     */
    private fun submitAndReconcile(answerId: String) {
        countdownJob?.cancel()
        val category = _state.value.currentQuestion?.category
        viewModelScope.launch {
            val result = submitAnswer(roomId, answerId)
            if (category != null) recordAnswerStat(category, result.wasCorrect)

            if (result.wasCorrect) {
                correctAnswers++
                currentStreak++
                if (currentStreak > bestStreakThisSession) bestStreakThisSession = currentStreak
                val bonus = streakBonusFor(currentStreak)
                val totalPoints = result.pointsAwarded + bonus

                _state.value = _state.value.copy(
                    roundResult = result,
                    localPlayerScore = _state.value.localPlayerScore + totalPoints
                )
                emitEffect(GameEffect.PlayHaptic(HapticPattern.SUCCESS))
                emitEffect(GameEffect.ShowScorePopup(points = totalPoints, answerId = answerId))

                val isMilestone = bonus > 0
                emitEffect(GameEffect.ShowConfetti(big = isMilestone))
                if (isMilestone) emitEffect(GameEffect.ShowStreakMilestone(currentStreak))
            } else {
                currentStreak = 0
                _state.value = _state.value.copy(
                    roundResult = result,
                    localPlayerScore = _state.value.localPlayerScore + result.pointsAwarded
                )
                emitEffect(GameEffect.PlayHaptic(HapticPattern.ERROR))
                emitEffect(GameEffect.PlaySound(SoundEffect.WRONG))
            }

            delay(ROUND_RESULT_PAUSE_MS)
            finishOrAdvance()
        }
    }

    private fun lockRoundAsIncorrect() {
        if (_state.value.isAnswerLocked) return
        currentStreak = 0
        val question = _state.value.currentQuestion
        question?.category?.let { recordAnswerStat(it, false) }
        _state.value = _state.value.copy(
            isAnswerLocked = true,
            roundResult = RoundResult(
                wasCorrect = false,
                correctAnswerId = question?.correctAnswerId.orEmpty(),
                pointsAwarded = 0
            )
        )
        emitEffect(GameEffect.PlayHaptic(HapticPattern.ERROR))
        viewModelScope.launch {
            delay(ROUND_RESULT_PAUSE_MS)
            finishOrAdvance()
        }
    }

    /**
     * Only exact milestone streak values award a bonus - not every answer
     * past that point - so the celebration lines up with a real points bump
     * rather than firing every single round once you're on a streak.
     */
    private fun streakBonusFor(streak: Int): Int = when {
        streak == 3 -> 20
        streak == 5 -> 50
        streak >= 10 && streak % 5 == 0 -> 100
        else -> 0
    }

    /**
     * sessionLength null (Firestore/Nearby today) means unbounded - always
     * advance, matching the old behavior. A real number means this is a
     * fixed-length session, so once questionsSeen reaches it, the session is
     * over instead of quietly fetching another question. Speed format adds
     * a second way to end early: any wrong answer (or timeout) stops the
     * run immediately, survival-style, regardless of sessionLength.
     */
    private fun finishOrAdvance() {
        val sessionLength = _state.value.sessionLength
        val format = _state.value.format
        val justAnsweredCorrectly = _state.value.roundResult?.wasCorrect == true
        val survivalEnded = format == GameFormat.SPEED && !justAnsweredCorrectly
        val reachedSessionLength = sessionLength != null && _state.value.currentQuestionNumber >= sessionLength

        if (survivalEnded || reachedSessionLength) {
            val statsResult = recordSessionStats(
                isDailyChallenge = format == GameFormat.DAILY,
                finalScore = _state.value.localPlayerScore,
                bestStreakThisSession = bestStreakThisSession
            )
            emitEffect(
                GameEffect.NavigateToResults(
                    roomId = roomId,
                    finalScore = _state.value.localPlayerScore,
                    correctCount = correctAnswers,
                    totalCount = _state.value.currentQuestionNumber,
                    dailyBestScore = statsResult.dailyBestScore,
                    players = _state.value.players
                )
            )
        } else {
            viewModelScope.launch {
                advanceToNextQuestion(roomId)
                advanceQuestion()
            }
        }
    }

    private fun advanceQuestion() {
        _state.value = _state.value.copy(
            selectedAnswerId = null,
            isAnswerLocked = false,
            roundResult = null
        )
    }

    private fun emitEffect(effect: GameEffect) {
        viewModelScope.launch { _effect.emit(effect) }
    }

    companion object {
        private const val COUNTDOWN_TICK_MS = 200L
        private const val ROUND_RESULT_PAUSE_MS = 1200L
        private const val MISMATCH_REVEAL_MS = 600L
    }
}
