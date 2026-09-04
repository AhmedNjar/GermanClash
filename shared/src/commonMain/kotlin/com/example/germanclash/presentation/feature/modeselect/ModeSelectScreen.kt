package com.example.germanclash.presentation.feature.modeselect

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.germanclash.data.local.questionbank.PracticeFilter
import com.example.germanclash.data.local.questionbank.PracticeFilterState
import com.example.germanclash.data.local.questionbank.QuestionBank
import com.example.germanclash.domain.model.GameFormat
import com.example.germanclash.domain.policy.UnlockPolicy
import com.example.germanclash.domain.usecase.GetStatsSummaryUseCase
import com.example.germanclash.domain.usecase.StatsSummary
import com.example.germanclash.presentation.common.AnswerState
import com.example.germanclash.presentation.common.CategoryChip
import com.example.germanclash.presentation.common.FilterChip
import com.example.germanclash.presentation.common.JuicyButton
import com.example.germanclash.presentation.common.SectionCard
import com.example.germanclash.presentation.common.colorForCategory
import com.example.germanclash.presentation.common.colorForLockedChip
import com.example.germanclash.presentation.common.emojiForCategory
import com.example.germanclash.presentation.theme.GameColors

@Composable
fun ModeSelectScreen(
    practiceFilterState: PracticeFilterState,
    questionBank: QuestionBank,
    getStatsSummary: GetStatsSummaryUseCase,
    onPlaySolo: () -> Unit,
    onHostGame: () -> Unit,
    onJoinGame: () -> Unit,
    onViewStats: () -> Unit
) {
    // Fetched once per visit - stats only change during gameplay, not while
    // sitting on this screen, so a live-updating read isn't needed here.
    val statsSummary = remember { getStatsSummary() }

    ModeSelectContent(
        statsSummary = statsSummary,
        availableLevels = questionBank.availableLevels(),
        availableCategories = questionBank.availableCategories(),
        onStartSolo = { filter ->
            practiceFilterState.current = filter
            onPlaySolo()
        },
        onHostGame = onHostGame,
        onJoinGame = onJoinGame,
        onViewStats = onViewStats
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ModeSelectContent(
    statsSummary: StatsSummary,
    availableLevels: List<String>,
    availableCategories: List<String>,
    onStartSolo: (PracticeFilter) -> Unit,
    onHostGame: () -> Unit,
    onJoinGame: () -> Unit,
    onViewStats: () -> Unit
) {
    var selectedLevel by remember { mutableStateOf<String?>(null) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedSessionLength by remember { mutableStateOf(10) }
    var selectedFormat by remember { mutableStateOf(GameFormat.CLASSIC) }

    val levels = remember(availableLevels) { listOf(null) + availableLevels }
    val categories = remember(availableCategories) { listOf(null) + availableCategories }
    
    val sessionLengths = listOf(5 to "Quick (5)", 10 to "Standard (10)", 20 to "Marathon (20)")
    val formats = listOf(
        GameFormat.CLASSIC to "Classic",
        GameFormat.MIXED to "Mixed",
        GameFormat.SPEED to "Speed",
        GameFormat.DAILY to "Daily"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "GermanClash",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 32.dp, bottom = 8.dp)
        )

        if (statsSummary.dailyPlayStreak > 0) {
            Text(
                text = "\uD83D\uDD25 ${statsSummary.dailyPlayStreak} day streak",
                color = GameColors.TitleAccent,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        JuicyButton(
            text = "View stats",
            state = AnswerState.IDLE,
            onClick = onViewStats,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        )

        SectionCard(title = "Format", modifier = Modifier.padding(bottom = 20.dp)) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                formats.forEach { (value, label) ->
                    FilterChip(
                        text = label,
                        isSelected = selectedFormat == value,
                        onClick = { selectedFormat = value }
                    )
                }
            }
            if (selectedFormat == GameFormat.DAILY) {
                Text(
                    text = "Same 10 questions for everyone today - level, category, and session length below are ignored.",
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }

        SectionCard(title = "Level", modifier = Modifier.padding(bottom = 20.dp)) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                levels.forEach { value ->
                    val isUnlocked = value == null ||
                        UnlockPolicy.isLevelUnlocked(value, statsSummary.totalCorrectAnswers)
                    val requirement = value?.let { UnlockPolicy.correctAnswersNeededForLevel(it) }
                    FilterChip(
                        text = when {
                            value == null -> "All levels"
                            isUnlocked -> value
                            else -> "\uD83D\uDD12 $value ($requirement)"
                        },
                        isSelected = selectedLevel == value,
                        onClick = { if (isUnlocked) selectedLevel = value }
                    )
                }
            }
        }

        SectionCard(title = "Category", modifier = Modifier.padding(bottom = 32.dp)) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { value ->
                    val isUnlocked = value == null ||
                        UnlockPolicy.isCategoryUnlocked(value, availableCategories, statsSummary.totalCorrectAnswers)
                    val requirement = value?.let {
                        UnlockPolicy.correctAnswersNeededForCategory(it, availableCategories)
                    }
                    CategoryChip(
                        emoji = if (isUnlocked) emojiForCategory(value) else "\uD83D\uDD12",
                        label = when {
                            value == null -> "All categories"
                            isUnlocked -> value
                            else -> "$value ($requirement)"
                        },
                        accentColor = if (isUnlocked) colorForCategory(value) else colorForLockedChip(),
                        isSelected = selectedCategory == value,
                        onClick = { if (isUnlocked) selectedCategory = value }
                    )
                }
            }
        }

        SectionCard(title = "Session length", modifier = Modifier.padding(bottom = 32.dp)) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                sessionLengths.forEach { (value, label) ->
                    FilterChip(
                        text = label,
                        isSelected = selectedSessionLength == value,
                        onClick = { selectedSessionLength = value }
                    )
                }
            }
        }

        JuicyButton(
            text = "Play solo",
            state = AnswerState.IDLE,
            onClick = {
                onStartSolo(
                    PracticeFilter(
                        level = selectedLevel,
                        category = selectedCategory,
                        sessionLength = if (selectedFormat == GameFormat.DAILY) 10 else selectedSessionLength,
                        format = selectedFormat
                    )
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )
        JuicyButton(
            text = "Host a game",
            state = AnswerState.IDLE,
            onClick = onHostGame,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )
        JuicyButton(
            text = "Join a game",
            state = AnswerState.IDLE,
            onClick = onJoinGame,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
@Preview
fun ModeSelectScreenPreview() {
    ModeSelectContent(
        statsSummary = StatsSummary(
            bestStreak = 5,
            dailyPlayStreak = 2,
            totalCorrectAnswers = 30,
            categoryStats = emptyMap()
        ),
        availableLevels = listOf("A1", "A2"),
        availableCategories = listOf("Food", "Animals", "City", "Nature"),
        onStartSolo = {},
        onHostGame = {},
        onJoinGame = {},
        onViewStats = {}
    )
}
