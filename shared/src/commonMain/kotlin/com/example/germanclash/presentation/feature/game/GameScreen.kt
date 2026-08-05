package com.example.germanclash.presentation.feature.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.germanclash.domain.model.AnswerOption
import com.example.germanclash.domain.model.GameType
import com.example.germanclash.domain.model.Question
import com.example.germanclash.presentation.common.AnswerState
import com.example.germanclash.presentation.common.ConfettiOverlay
import com.example.germanclash.presentation.common.CountdownTimerRing
import com.example.germanclash.presentation.common.JuicyButton
import com.example.germanclash.presentation.common.SoundEffectPlayer

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    soundPlayer: SoundEffectPlayer,
    onNavigateToResults: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    var confettiTrigger by remember { mutableIntStateOf(0) }
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(viewModel) {
        viewModel.onIntent(GameIntent.LoadGame)
        viewModel.effect.collect { effect ->
            when (effect) {
                is GameEffect.PlayHaptic -> haptic.performHapticFeedback(
                    when (effect.pattern) {
                        HapticPattern.SUCCESS -> HapticFeedbackType.LongPress
                        HapticPattern.ERROR -> HapticFeedbackType.TextHandleMove
                        HapticPattern.LIGHT_TICK -> HapticFeedbackType.TextHandleMove
                    }
                )
                is GameEffect.PlaySound -> soundPlayer.play(effect.sound)
                GameEffect.ShowConfetti -> confettiTrigger++
                is GameEffect.NavigateToResults -> onNavigateToResults(effect.roomId)
                is GameEffect.ShowToast -> Unit // wire to a Snackbar/Toast host at the app level
            }
        }
    }

    GameContent(
        state = state,
        confettiTrigger = confettiTrigger,
        onIntent = viewModel::onIntent
    )
}

@Composable
fun GameContent(
    state: GameUiState,
    confettiTrigger: Int,
    onIntent: (GameIntent) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CountdownTimerRing(
                    progress = state.timerProgress,
                    secondsRemaining = (state.timeRemainingMs / 1000).toInt(),
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                state.currentQuestion?.let { question ->
                    Text(text = question.prompt, modifier = Modifier.padding(bottom = 32.dp))

                    when (state.gameType) {
                        GameType.SENTENCE_BUILDER -> {
                            SentenceBuilderContent(
                                wordBank = state.wordBank,
                                assembledWords = state.assembledWords,
                                isLocked = state.isAnswerLocked,
                                onWordTapped = { word -> onIntent(GameIntent.SelectWord(word)) },
                                onRemoveLastTapped = { onIntent(GameIntent.RemoveLastWord) }
                            )
                        }
                        GameType.MATCH_PAIRS -> {
                            MatchPairsContent(
                                cards = state.matchCards,
                                flippedCardIds = state.flippedCardIds,
                                onCardTapped = { cardId -> onIntent(GameIntent.FlipCard(cardId)) }
                            )
                        }
                        else -> {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(question.options) { option ->
                                    val answerState = when {
                                        state.roundResult != null && option.id == state.roundResult.correctAnswerId ->
                                            AnswerState.CORRECT
                                        state.roundResult != null && option.id == state.selectedAnswerId ->
                                            AnswerState.WRONG
                                        option.id == state.selectedAnswerId -> AnswerState.SELECTED
                                        else -> AnswerState.IDLE
                                    }
                                    JuicyButton(
                                        text = option.text,
                                        state = answerState,
                                        onClick = { onIntent(GameIntent.SelectAnswer(option.id)) },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        ConfettiOverlay(
            triggerKey = confettiTrigger,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
@Preview
fun GameScreenPreview() {
    GameContent(
        state = GameUiState(
            isLoading = false,
            currentQuestion = Question(
                id = "q1",
                type = GameType.DER_DIE_DAS,
                prompt = "___ Apfel",
                options = listOf(
                    AnswerOption("der", "der"),
                    AnswerOption("die", "die"),
                    AnswerOption("das", "das")
                )
            ),
            timeRemainingMs = 7500,
            timeLimitMs = 10000
        ),
        confettiTrigger = 0,
        onIntent = {}
    )
}
