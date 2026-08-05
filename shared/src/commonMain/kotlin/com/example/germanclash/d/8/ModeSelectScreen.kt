package com.app.germanclash.presentation.feature.modeselect

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
import androidx.compose.ui.unit.dp
import com.app.germanclash.data.local.questionbank.PracticeFilter
import com.app.germanclash.data.local.questionbank.PracticeFilterState
import com.app.germanclash.data.local.questionbank.QuestionBank
import com.app.germanclash.presentation.common.AnswerState
import com.app.germanclash.presentation.common.CategoryChip
import com.app.germanclash.presentation.common.FilterChip
import com.app.germanclash.presentation.common.JuicyButton
import com.app.germanclash.presentation.common.SectionCard
import com.app.germanclash.presentation.common.colorForCategory
import com.app.germanclash.presentation.common.emojiForCategory

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ModeSelectScreen(
    practiceFilterState: PracticeFilterState,
    questionBank: QuestionBank,
    onPlaySolo: () -> Unit,
    onHostGame: () -> Unit,
    onJoinGame: () -> Unit
) {
    var selectedLevel by remember { mutableStateOf<String?>(null) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedSessionLength by remember { mutableStateOf(10) }

    // Pulled straight from the loaded content, not hardcoded - add a new
    // category to the JSON and it shows up here automatically.
    val levels = remember(questionBank) { listOf(null) + questionBank.availableLevels() }
    val categories = remember(questionBank) { listOf(null) + questionBank.availableCategories() }
    val sessionLengths = listOf(5 to "Quick (5)", 10 to "Standard (10)", 20 to "Marathon (20)")

    // No background modifier here - GermanClashBackground paints it once at
    // the root, and Surface there gives Text below the right default color.
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
            modifier = Modifier.padding(top = 32.dp, bottom = 32.dp)
        )

        SectionCard(title = "Level", modifier = Modifier.padding(bottom = 20.dp)) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                levels.forEach { value ->
                    FilterChip(
                        text = value ?: "All levels",
                        isSelected = selectedLevel == value,
                        onClick = { selectedLevel = value }
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
                    CategoryChip(
                        emoji = emojiForCategory(value),
                        label = value ?: "All categories",
                        accentColor = colorForCategory(value),
                        isSelected = selectedCategory == value,
                        onClick = { selectedCategory = value }
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
                practiceFilterState.current = PracticeFilter(
                    level = selectedLevel,
                    category = selectedCategory,
                    sessionLength = selectedSessionLength
                )
                onPlaySolo()
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
