package com.example.germanclash.presentation.feature.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.example.germanclash.presentation.theme.GameColors

@Composable
fun HomeScreen(
    onPlaySolo: () -> Unit = {},
    onHostGame: () -> Unit = {},
    onJoinGame: () -> Unit = {},
    onViewLeaderboard: () -> Unit = {}
) {
    var selectedLevel by remember { mutableStateOf("A1") }
    var selectedFormat by remember { mutableStateOf("Classic") }

    Scaffold(
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onHostGame,
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = MaterialTheme.shapes.large
                        ) {
                            Text("Host Game")
                        }
                        OutlinedButton(
                            onClick = onJoinGame,
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = MaterialTheme.shapes.large
                        ) {
                            Text("Join Game")
                        }
                    }
                    Button(
                        onClick = onPlaySolo,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = MaterialTheme.shapes.large,
                        colors = ButtonDefaults.buttonColors(containerColor = GameColors.Primary)
                    ) {
                        Text("Play Solo", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            HomeHeader(streak = 7, onLeaderboardClick = onViewLeaderboard)

            Spacer(modifier = Modifier.height(32.dp))

            Text("Select Level", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf("A1", "A2", "B1", "B2", "C1")) { level ->
                    FilterChip(
                        selected = selectedLevel == level,
                        onClick = { selectedLevel = level },
                        label = { Text(level) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GameColors.Primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Game Format", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf("Classic", "Speed", "Daily")) { format ->
                    FilterChip(
                        selected = selectedFormat == format,
                        onClick = { selectedFormat = format },
                        label = { Text(format) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text("Categories", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(
                    listOf(
                        CategoryItem("Basics", 1.0f, false),
                        CategoryItem("Animals", 0.4f, false, 50),
                        CategoryItem("Travel", 0.0f, true, 150),
                        CategoryItem("Tech", 0.0f, true, 300)
                    )
                ) { category ->
                    CategoryCard(category)
                }
            }
        }
    }
}

@Composable
private fun HomeHeader(streak: Int, onLeaderboardClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "GermanClash",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Willkommen zurück!",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onLeaderboardClick) {
                LeaderboardIcon(color = MaterialTheme.colorScheme.primary)
            }
            
            Spacer(modifier = Modifier.width(8.dp))

            AssistChip(
                onClick = {},
                label = { Text("$streak", fontWeight = FontWeight.Bold) },
                leadingIcon = { FireIcon(Modifier.size(16.dp)) },
                shape = CircleShape,
                colors = AssistChipDefaults.assistChipColors(
                    labelColor = Color.White
                )
            )
        }
    }
}

@Composable
private fun LeaderboardIcon(modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier = modifier.size(24.dp)) {
        val w = size.width
        val h = size.height
        // 3 bars
        drawRect(color, Offset(w * 0.1f, h * 0.6f), androidx.compose.ui.geometry.Size(w * 0.2f, h * 0.3f))
        drawRect(color, Offset(w * 0.4f, h * 0.3f), androidx.compose.ui.geometry.Size(w * 0.2f, h * 0.6f))
        drawRect(color, Offset(w * 0.7f, h * 0.5f), androidx.compose.ui.geometry.Size(w * 0.2f, h * 0.4f))
    }
}

@Composable
private fun CategoryCard(item: CategoryItem) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (item.isLocked) MaterialTheme.colorScheme.surface.copy(alpha = 0.5f) 
                             else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(GameColors.Primary.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (item.isLocked) {
                    LockIcon(Modifier.size(20.dp), color = Color.Gray)
                } else {
                    Text(item.name.take(1), color = GameColors.Primary, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (item.isLocked) Color.Gray else Color.White
                )
                if (item.isLocked && item.pointsNeeded != null) {
                    Text(
                        text = "${item.pointsNeeded} pts needed",
                        style = MaterialTheme.typography.labelSmall,
                        color = GameColors.TitleAccent
                    )
                } else if (!item.isLocked) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { item.progress },
                        modifier = Modifier.fillMaxWidth().height(4.dp),
                        color = GameColors.Primary,
                        trackColor = GameColors.Primary.copy(alpha = 0.1f),
                        strokeCap = StrokeCap.Round
                    )
                }
            }
        }
    }
}

private data class CategoryItem(
    val name: String,
    val progress: Float,
    val isLocked: Boolean,
    val pointsNeeded: Int? = null
)

@Composable
private fun FireIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val path = Path().apply {
            moveTo(size.width * 0.5f, size.height * 0.1f)
            quadraticTo(size.width * 0.8f, size.height * 0.4f, size.width * 0.5f, size.height * 0.9f)
            quadraticTo(size.width * 0.2f, size.height * 0.4f, size.width * 0.5f, size.height * 0.1f)
        }
        drawPath(path, color = Color(0xFFF97316)) // Orange-500
    }
}

@Composable
private fun LockIcon(modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier = modifier) {
        // Simple lock shape
        drawRect(
            color = color,
            topLeft = Offset(size.width * 0.2f, size.height * 0.5f),
            size = androidx.compose.ui.geometry.Size(size.width * 0.6f, size.height * 0.4f)
        )
        // Shackle
        drawArc(
            color = color,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(size.width * 0.3f, size.height * 0.2f),
            size = androidx.compose.ui.geometry.Size(size.width * 0.4f, size.height * 0.4f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
        )
    }
}

@Composable
@Preview
fun HomeScreenPreview() {
    com.example.germanclash.presentation.theme.GermanClashTheme {
        HomeScreen()
    }
}
