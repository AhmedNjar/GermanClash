package com.app.germanclash.presentation.feature.game

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.app.germanclash.domain.model.ConnectionStatus
import com.app.germanclash.domain.model.GameType
import com.app.germanclash.presentation.common.AnswerState
import com.app.germanclash.presentation.common.ConfettiOverlay
import com.app.germanclash.presentation.common.CountdownTimerRing
import com.app.germanclash.presentation.common.JuicyButton
import com.app.germanclash.presentation.common.SoundEffectPlayer
import com.app.germanclash.presentation.theme.GameColors

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    soundPlayer: SoundEffectPlayer,
    onNavigateToResults: (roomId: String, score: Int, correctCount: Int, totalCount: Int) -> Unit
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
                        // Compose's built-in feedback types don't give us three
                        // distinct patterns yet - a Vibrator/VibrationEffect
                        // wrapper (same expect/actual pattern as P2PConnectionClient)
                        // is the next upgrade if these two need to feel more distinct.
                        HapticPattern.SUCCESS -> HapticFeedbackType.LongPress
                        HapticPattern.ERROR -> HapticFeedbackType.TextHandleMove
                        HapticPattern.LIGHT_TICK -> HapticFeedbackType.TextHandleMove
                    }
                )
                is GameEffect.PlaySound -> soundPlayer.play(effect.sound)
                GameEffect.ShowConfetti -> confettiTrigger++
                is GameEffect.NavigateToResults -> onNavigateToResults(
                    effect.roomId, effect.finalScore, effect.correctCount, effect.totalCount
                )
                is GameEffect.ShowToast -> Unit // wire to a Snackbar/Toast host at the app level
            }
        }
    }

    // No background modifier here anymore - GermanClashBackground paints it
    // once at the root (MainActivity), and Surface there already gives every
    // Text below the correct default (white) color via LocalContentColor.
    Box(modifier = Modifier.fillMaxSize()) {
        if (state.isLoading) {
            CircularProgressIndicator(
                color = GameColors.TitleAccent,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (state.connectionStatus == ConnectionStatus.RECONNECTING) {
                    Text(
                        text = "Reconnecting\u2026",
                        color = GameColors.WrongRed,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                if (state.sessionLength != null) {
                    Text(
                        text = "Question ${state.currentQuestionNumber} of ${state.sessionLength}",
                        color = GameColors.TitleAccent,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                CountdownTimerRing(
                    progress = state.timerProgress,
                    secondsRemaining = (state.timeRemainingMs / 1000).toInt(),
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                state.currentQuestion?.let { question ->
                    Text(text = question.prompt, modifier = Modifier.padding(bottom = 32.dp))

                    if (state.gameType == GameType.SENTENCE_BUILDER) {
                        SentenceBuilderContent(
                            wordBank = state.wordBank,
                            assembledWords = state.assembledWords,
                            isLocked = state.isAnswerLocked,
                            onWordTapped = { word -> viewModel.onIntent(GameIntent.SelectWord(word)) },
                            onRemoveLastTapped = { viewModel.onIntent(GameIntent.RemoveLastWord) }
                        )
                    } else if (state.gameType == GameType.MATCH_PAIRS) {
                        MatchPairsContent(
                            cards = state.matchCards,
                            flippedCardIds = state.flippedCardIds,
                            onCardTapped = { cardId -> viewModel.onIntent(GameIntent.FlipCard(cardId)) }
                        )
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(question.options) { option ->
                                val answerState = when {
                                    state.roundResult != null && option.id == state.roundResult?.correctAnswerId ->
                                        AnswerState.CORRECT
                                    state.roundResult != null && option.id == state.selectedAnswerId ->
                                        AnswerState.WRONG
                                    option.id == state.selectedAnswerId -> AnswerState.SELECTED
                                    else -> AnswerState.IDLE
                                }
                                JuicyButton(
                                    text = option.text,
                                    state = answerState,
                                    onClick = { viewModel.onIntent(GameIntent.SelectAnswer(option.id)) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    if (state.roundResult?.wasCorrect == false && question.translation != null) {
                        Text(
                            text = "\uD83D\uDCA1 ${question.translation}",
                            color = GameColors.TitleAccent,
                            modifier = Modifier.padding(top = 24.dp)
                        )
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
