package com.example.germanclash.presentation.feature.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.germanclash.domain.model.GameType
import com.example.germanclash.domain.model.Question
import com.example.germanclash.domain.usecase.ObserveGameSessionUseCase
import com.example.germanclash.domain.usecase.SubmitAnswerUseCase
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
    private val submitAnswer: SubmitAnswerUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(GameUiState())
    val state: StateFlow<GameUiState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<GameEffect>()
    val effect: SharedFlow<GameEffect> = _effect

    fun onIntent(intent: GameIntent) {
        when (intent) {
            GameIntent.LoadGame -> loadGame()
            is GameIntent.SelectAnswer -> selectAnswer(intent.answerId)
            is GameIntent.SelectWord -> selectWord(intent.word)
            GameIntent.RemoveLastWord -> removeLastWord()
            is GameIntent.FlipCard -> flipCard(intent.cardId)
            GameIntent.TimerExpired -> lockRoundAsIncorrect()
            GameIntent.NextQuestion -> advanceQuestion()
            GameIntent.LeaveGame -> emitEffect(GameEffect.NavigateToResults(roomId))
            GameIntent.RetryAfterError -> loadGame()
        }
    }

    private var lastQuestionId: String? = null

    private fun loadGame() {
        _state.value = _state.value.copy(isLoading = true, error = null)
        observeGameSession(roomId)
            .onEach { session ->
                val questionChanged = session.currentQuestion?.id != lastQuestionId
                lastQuestionId = session.currentQuestion?.id
                _state.value = _state.value.copy(
                    isLoading = false,
                    currentQuestion = session.currentQuestion,
                    gameType = session.currentQuestion?.type ?: _state.value.gameType,
                    players = session.players,
                    timeRemainingMs = session.timeRemainingMs,
                    timeLimitMs = session.timeLimitMs,
                    wordBank = if (questionChanged) session.currentQuestion?.scrambledWords.orEmpty() else _state.value.wordBank,
                    assembledWords = if (questionChanged) emptyList() else _state.value.assembledWords,
                    matchCards = if (questionChanged) buildMatchDeck(session.currentQuestion) else _state.value.matchCards,
                    flippedCardIds = if (questionChanged) emptyList() else _state.value.flippedCardIds,
                    isEvaluatingMismatch = if (questionChanged) false else _state.value.isEvaluatingMismatch
                )
            }
            .launchIn(viewModelScope)
    }

    private fun buildMatchDeck(question: Question?): List<MatchCard> {
        if (question == null || question.type != GameType.MATCH_PAIRS) return emptyList()
        return question.cardPairs.flatMap { pair ->
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
            emitEffect(GameEffect.ShowConfetti)
            if (updated.all { it.isMatched }) {
                submitAndReconcile("MATCH_PAIRS_COMPLETE")
            }
        } else {
            _state.value = _state.value.copy(isEvaluatingMismatch = true)
            emitEffect(GameEffect.PlayHaptic(HapticPattern.ERROR))
            viewModelScope.launch {
                delay(600)
                _state.value = _state.value.copy(flippedCardIds = emptyList(), isEvaluatingMismatch = false)
            }
        }
    }

    private fun submitAndReconcile(answerId: String) {
        viewModelScope.launch {
            val result = submitAnswer(roomId, answerId)
            _state.value = _state.value.copy(
                roundResult = result,
                localPlayerScore = _state.value.localPlayerScore + result.pointsAwarded
            )
            emitEffect(
                if (result.wasCorrect) GameEffect.PlayHaptic(HapticPattern.SUCCESS)
                else GameEffect.PlayHaptic(HapticPattern.ERROR)
            )
            emitEffect(if (result.wasCorrect) GameEffect.ShowConfetti else GameEffect.PlaySound(SoundEffect.WRONG))
        }
    }

    private fun lockRoundAsIncorrect() {
        if (_state.value.isAnswerLocked) return
        _state.value = _state.value.copy(isAnswerLocked = true)
        emitEffect(GameEffect.PlayHaptic(HapticPattern.ERROR))
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
}