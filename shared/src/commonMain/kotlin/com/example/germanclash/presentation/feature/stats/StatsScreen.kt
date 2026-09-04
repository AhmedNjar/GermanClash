package com.example.germanclash.presentation.feature.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.germanclash.domain.model.CategoryStats
import com.example.germanclash.domain.usecase.StatsSummary
import com.example.germanclash.presentation.theme.GameColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(statsSummary: StatsSummary, onBack: () -> Unit) {
    val favoriteCategory = statsSummary.categoryStats.maxByOrNull { it.value.total }?.key

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Performance", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        BackArrow(color = MaterialTheme.colorScheme.onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                item {
                    StatCard("Best Streak", statsSummary.bestStreak.toString())
                }
                item {
                    StatCard("Play Streak", "${statsSummary.dailyPlayStreak}d")
                }
                item {
                    StatCard("Correct", statsSummary.totalCorrectAnswers.toString())
                }
                item {
                    StatCard("Global Rank", "Top 15%")
                }
                item {
                    StatCard("Top Category", favoriteCategory ?: "N/A")
                }
            }

            Text(
                text = "Accuracy by Category",
                style = MaterialTheme.typography.titleMedium,
                color = GameColors.TitleAccent,
                modifier = Modifier.align(Alignment.Start).padding(bottom = 16.dp)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
            ) {
                statsSummary.categoryStats.entries
                    .sortedByDescending { it.value.total }
                    .forEach { (category, stats) ->
                        val accuracyPercent = if (stats.total > 0) stats.correct * 100 / stats.total else 0
                        CategoryAccuracyRow(category, accuracyPercent, "${stats.correct}/${stats.total}")
                    }
            }
        }
    }
}

@Composable
private fun BackArrow(color: Color) {
    Canvas(modifier = Modifier.size(24.dp)) {
        val width = size.width
        val height = size.height
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(width * 0.8f, height * 0.5f),
            end = androidx.compose.ui.geometry.Offset(width * 0.2f, height * 0.5f),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(width * 0.4f, height * 0.3f),
            end = androidx.compose.ui.geometry.Offset(width * 0.2f, height * 0.5f),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(width * 0.4f, height * 0.7f),
            end = androidx.compose.ui.geometry.Offset(width * 0.2f, height * 0.5f),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun StatCard(label: String, value: String) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun CategoryAccuracyRow(category: String, percentage: Int, ratio: String) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = category, style = MaterialTheme.typography.bodyLarge)
                Text(text = ratio, style = MaterialTheme.typography.labelLarge, color = Color.Gray)
            }
            Text(
                text = "$percentage%",
                style = MaterialTheme.typography.titleMedium,
                color = if (percentage > 70) GameColors.CorrectGreen else GameColors.TitleAccent
            )
        }
    }
}

@Composable
@Preview
fun StatsScreenPreview() {
    StatsScreen(
        statsSummary = StatsSummary(
            bestStreak = 12,
            dailyPlayStreak = 5,
            totalCorrectAnswers = 145,
            categoryStats = mapOf(
                "Food" to CategoryStats(correct = 25, total = 30),
                "Animals" to CategoryStats(correct = 15, total = 20),
                "Body" to CategoryStats(correct = 40, total = 50)
            )
        ),
        onBack = {}
    )
}
