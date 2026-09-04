package com.example.germanclash.presentation.feature.leaderboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.example.germanclash.domain.model.LeaderboardEntry
import com.example.germanclash.presentation.theme.GameColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(onBack: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Daily", "Weekly", "All Time")

    val mockData = remember(selectedTab) {
        listOf(
            LeaderboardEntry(1, "Hans_Klaus", 2450),
            LeaderboardEntry(2, "Petra_99", 2310),
            LeaderboardEntry(3, "DeutschLover", 2100),
            LeaderboardEntry(4, "SpeedyGonzales", 1950),
            LeaderboardEntry(5, "LearningMaster", 1800),
            LeaderboardEntry(6, "You", 1750, isCurrentUser = true),
            LeaderboardEntry(7, "BerlinVoyager", 1600),
            LeaderboardEntry(8, "GrammarGuru", 1450)
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Leaderboard", style = MaterialTheme.typography.titleMedium) },
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
        bottomBar = {
            UserRankFooter(mockData.find { it.isCurrentUser }!!)
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = GameColors.Primary,
                indicator = { tabPositions ->
                    if (selectedTab < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = GameColors.Primary
                        )
                    }
                },
                divider = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        OfflineIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    items(mockData) { entry ->
                        LeaderboardRow(entry)
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp)) // Padding for footer
                    }
                }
            }
        }
    }
}

@Composable
private fun LeaderboardRow(entry: LeaderboardEntry) {
    val backgroundColor = if (entry.isCurrentUser) GameColors.Primary.copy(alpha = 0.1f) 
                          else MaterialTheme.colorScheme.surface
    
    val modifier = if (entry.isCurrentUser) {
        Modifier
            .fillMaxWidth()
            .border(1.dp, GameColors.Primary, MaterialTheme.shapes.medium)
    } else {
        Modifier.fillMaxWidth()
    }

    ElevatedCard(
        modifier = modifier,
        colors = CardDefaults.elevatedCardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RankIndicator(entry.rank)
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = entry.username,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (entry.isCurrentUser) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.weight(1f)
            )
            
            Text(
                text = entry.score.toString(),
                style = MaterialTheme.typography.titleMedium,
                color = GameColors.TitleAccent,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun RankIndicator(rank: Int) {
    val color = when (rank) {
        1 -> Color(0xFFFFD700) // Gold
        2 -> Color(0xFFC0C0C0) // Silver
        3 -> Color(0xFFCD7F32) // Bronze
        else -> Color.Transparent
    }

    Box(
        modifier = Modifier
            .size(32.dp)
            .background(color, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = rank.toString(),
            style = MaterialTheme.typography.labelLarge,
            color = if (rank <= 3) Color.Black else Color.Gray,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun UserRankFooter(userEntry: LeaderboardEntry) {
    Surface(
        tonalElevation = 8.dp,
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RankIndicator(userEntry.rank)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Your Rank", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Text(userEntry.username, style = MaterialTheme.typography.titleMedium)
            }
            Text(
                text = "Top 15%",
                style = MaterialTheme.typography.labelLarge,
                color = GameColors.CorrectGreen
            )
        }
    }
}

@Composable
private fun OfflineIndicator() {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Mocked global data (Offline Mode)",
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            modifier = Modifier.padding(8.dp),
            textAlign = TextAlign.Center
        )
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
@Preview
fun LeaderboardScreenPreview() {
    com.example.germanclash.presentation.theme.GermanClashTheme {
        LeaderboardScreen(onBack = {})
    }
}
