package com.example.germanclash.presentation.feature.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.germanclash.domain.model.*
import com.example.germanclash.presentation.common.*
import com.example.germanclash.presentation.theme.GameColors
import kotlin.math.roundToInt

private data class ActiveScorePopup(val id: Int, val points: Int, val anchor: Offset)

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    soundPlayer: SoundEffectPlayer,
    localPlayerId: String,
    onNavigateToResults: (
        roomId: String,
        score: Int,
        correctCount: Int,
        totalCount: Int,
        dailyBestScore: Int?,
        players: List<Player>
    ) -> Unit
) {
    val state by viewModel.state.collectAsState()
    var confettiTrigger by remember { mutableIntStateOf(0) }
    var confettiParticleCount by remember { mutableIntStateOf(24) }
    var streakBannerText by remember { mutableStateOf<String?>(null) }
    var errorFlashTrigger by remember { mutableIntStateOf(0) }
    val haptic = LocalHapticFeedback.current

    var pendingScorePopup by remember { mutableStateOf<GameEffect.ShowScorePopup?>(null) }

    LaunchedEffect(viewModel) {
        viewModel.onIntent(GameIntent.LoadGame)
        viewModel.effect.collect { effect ->
            when (effect) {
                is GameEffect.PlayHaptic -> {
                    haptic.performHapticFeedback(
                        when (effect.pattern) {
                            HapticPattern.SUCCESS -> HapticFeedbackType.LongPress
                            HapticPattern.ERROR -> HapticFeedbackType.TextHandleMove
                            HapticPattern.LIGHT_TICK -> HapticFeedbackType.TextHandleMove
                        }
                    )
                    if (effect.pattern == HapticPattern.ERROR) errorFlashTrigger++
                }
                is GameEffect.PlaySound -> soundPlayer.play(effect.sound)
                is GameEffect.ShowConfetti -> {
                    confettiParticleCount = if (effect.big) 60 else 24
                    confettiTrigger++
                }
                is GameEffect.ShowScorePopup -> pendingScorePopup = effect
                is GameEffect.ShowStreakMilestone -> streakBannerText = "\uD83D\uDD25 Streak x${effect.streak}!"
                is GameEffect.NavigateToResults -> onNavigateToResults(
                    effect.roomId, effect.finalScore, effect.correctCount, effect.totalCount,
                    effect.dailyBestScore, effect.players
                )
                is GameEffect.ShowToast -> Unit
            }
        }
    }

    GameContent(
        state = state,
        localPlayerId = localPlayerId,
        confettiTrigger = confettiTrigger,
        confettiParticleCount = confettiParticleCount,
        streakBannerText = streakBannerText,
        errorFlashTrigger = errorFlashTrigger,
        pendingScorePopup = pendingScorePopup,
        onScorePopupShown = { pendingScorePopup = null },
        onIntent = viewModel::onIntent
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GameContent(
    state: GameUiState,
    localPlayerId: String,
    confettiTrigger: Int,
    confettiParticleCount: Int,
    streakBannerText: String?,
    errorFlashTrigger: Int,
    pendingScorePopup: GameEffect.ShowScorePopup?,
    onScorePopupShown: () -> Unit,
    onIntent: (GameIntent) -> Unit
) {
    val shakeOffsetX = remember { Animatable(0f) }
    val errorFlashAlpha = remember { Animatable(0f) }

    val buttonPositions = remember { mutableStateMapOf<String, Offset>() }
    var containerPositionInRoot by remember { mutableStateOf(Offset.Zero) }
    
    val activePopups = remember { mutableStateListOf<ActiveScorePopup>() }
    var nextPopupId by remember { mutableIntStateOf(0) }

    LaunchedEffect(pendingScorePopup) {
        if (pendingScorePopup != null) {
            val anchor = buttonPositions[pendingScorePopup.answerId]?.let { it - containerPositionInRoot }
                ?: Offset(0f, 0f)
            
            activePopups.add(
                ActiveScorePopup(id = nextPopupId++, points = pendingScorePopup.points, anchor = anchor)
            )
            onScorePopupShown()
        }
    }

    LaunchedEffect(errorFlashTrigger) {
        if (errorFlashTrigger == 0) return@LaunchedEffect
        shakeOffsetX.snapTo(0f)
        shakeOffsetX.animateTo(
            targetValue = 0f,
            animationSpec = keyframes {
                durationMillis = 300
                0f at 0
                -14f at 50
                14f at 100
                -8f at 150
                8f at 200
                0f at 300
            }
        )
    }

    LaunchedEffect(errorFlashTrigger) {
        if (errorFlashTrigger == 0) return@LaunchedEffect
        errorFlashAlpha.snapTo(0.28f)
        errorFlashAlpha.animateTo(0f, animationSpec = tween(durationMillis = 300))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .onGloballyPositioned { containerPositionInRoot = it.positionInRoot() }
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(
                color = GameColors.Primary,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .offset { IntOffset(shakeOffsetX.value.roundToInt(), 0) },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header: Progress & Timer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (state.format == GameFormat.TIME_ATTACK) "Time Attack" else "Question",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.Gray
                        )
                        Text(
                            text = if (state.format == GameFormat.TIME_ATTACK) "Score: ${state.localPlayerScore}" 
                                   else "${state.currentQuestionNumber} of ${state.sessionLength ?: "?"}",
                            style = MaterialTheme.typography.titleMedium,
                            color = GameColors.TitleAccent
                        )
                    }

                    CountdownTimerRing(
                        progress = if (state.format == GameFormat.TIME_ATTACK) state.timeRemainingMs / 60000f else state.timerProgress,
                        secondsRemaining = (state.timeRemainingMs / 1000).toInt()
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Main Area: Target Card
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    state.currentQuestion?.let { question ->
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            shape = MaterialTheme.shapes.extraLarge
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 48.dp, horizontal = 24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = question.prompt,
                                    style = MaterialTheme.typography.headlineMedium,
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Bold
                                )
                                if (question.translation != null && state.roundResult?.wasCorrect == false) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = question.translation,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = GameColors.TitleAccent,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Answer Grid / Content
                state.currentQuestion?.let { question ->
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
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                maxItemsInEachRow = 2
                            ) {
                                question.options.forEach { option ->
                                    val answerState = when {
                                        state.roundResult != null && option.id == state.roundResult.correctAnswerId ->
                                            AnswerState.CORRECT
                                        state.roundResult != null && option.id == state.selectedAnswerId ->
                                            AnswerState.WRONG
                                        option.id == state.selectedAnswerId -> AnswerState.SELECTED
                                        else -> AnswerState.IDLE
                                    }
                                    QuizOptionButton(
                                        text = option.text,
                                        state = answerState,
                                        onClick = { onIntent(GameIntent.SelectAnswer(option.id)) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .minHeight(64.dp)
                                            .onGloballyPositioned { buttonPositions[option.id] = it.positionInRoot() }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (errorFlashAlpha.value > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(GameColors.WrongRed.copy(alpha = errorFlashAlpha.value))
            )
        }

        ConfettiOverlay(
            triggerKey = confettiTrigger,
            particleCount = confettiParticleCount,
            modifier = Modifier.fillMaxSize()
        )

        activePopups.forEach { popup ->
            key(popup.id) {
                ScorePopup(
                    points = popup.points,
                    anchor = popup.anchor,
                    onFinished = { activePopups.remove(popup) }
                )
            }
        }

        AnimatedVisibility(
            visible = streakBannerText != null,
            enter = fadeIn() + scaleIn(initialScale = 0.8f),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 100.dp)
        ) {
            Surface(
                color = GameColors.Primary,
                shape = CircleShape,
                shadowElevation = 4.dp
            ) {
                Text(
                    text = streakBannerText.orEmpty(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun QuizOptionButton(
    text: String,
    state: AnswerState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale = remember { Animatable(1f) }
    
    val backgroundColor by animateColorAsState(
        targetValue = when (state) {
            AnswerState.IDLE -> GameColors.OptionDefault
            AnswerState.SELECTED -> GameColors.Primary
            AnswerState.CORRECT -> GameColors.CorrectGreen
            AnswerState.WRONG -> GameColors.WrongRed
        },
        animationSpec = tween(300)
    )

    LaunchedEffect(state) {
        if (state == AnswerState.SELECTED) {
            scale.animateTo(0.95f, spring(dampingRatio = 0.6f))
            scale.animateTo(1f, spring(dampingRatio = 0.6f))
        }
    }

    Box(
        modifier = modifier
            .scale(scale.value)
            .clip(MaterialTheme.shapes.medium)
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}

private fun Modifier.minHeight(height: androidx.compose.ui.unit.Dp) = this.defaultMinSize(minHeight = height)

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
            timeLimitMs = 10000,
            players = listOf(Player("1", "You", 120)),
            currentQuestionNumber = 3,
            sessionLength = 10
        ),
        localPlayerId = "1",
        confettiTrigger = 0,
        confettiParticleCount = 24,
        streakBannerText = "\uD83D\uDD25 Streak x5!",
        errorFlashTrigger = 0,
        pendingScorePopup = null,
        onScorePopupShown = {},
        onIntent = {}
    )
}
