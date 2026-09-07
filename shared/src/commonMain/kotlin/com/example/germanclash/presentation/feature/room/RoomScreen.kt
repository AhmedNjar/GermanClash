package com.example.germanclash.presentation.feature.room

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.germanclash.domain.model.GameFormat
import com.example.germanclash.domain.model.Player
import com.example.germanclash.presentation.common.AnswerState
import com.example.germanclash.presentation.common.JuicyButton
import com.example.germanclash.presentation.theme.GameColors

@Composable
fun RoomScreen(
    viewModel: RoomViewModel,
    roomId: String,
    localPlayerId: String,
    onNavigateToGame: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(roomId) {
        viewModel.onIntent(RoomIntent.JoinRoom(roomId, localPlayerId))
        viewModel.effect.collect { effect ->
            when (effect) {
                is RoomEffect.NavigateToGame -> onNavigateToGame(effect.roomId)
                is RoomEffect.ShowToast -> Unit // wire to a Snackbar host at the app level
            }
        }
    }

    RoomContent(
        state = state,
        onToggleReady = { viewModel.onIntent(RoomIntent.ToggleReady) },
        onChangeFormat = { viewModel.onIntent(RoomIntent.ChangeFormat(it)) },
        onChangeTimeLimit = { viewModel.onIntent(RoomIntent.ChangeTimeLimit(it)) },
        onChangeCategory = { viewModel.onIntent(RoomIntent.ChangeCategory(it)) }
    )
}

@Composable
fun RoomContent(
    state: RoomUiState,
    onToggleReady: () -> Unit,
    onChangeFormat: (GameFormat) -> Unit = {},
    onChangeTimeLimit: (Long) -> Unit = {},
    onChangeCategory: (String?) -> Unit = {}
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (state.isJoining) {
            CircularProgressIndicator(
                color = GameColors.Primary,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            val isHost = state.roomId.contains("_HOST_")
            
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val statusText = when {
                    isHost && state.players.size <= 1 -> "Waiting for a friend to join..."
                    isHost -> "A friend connected! Set the rules and Ready up."
                    state.players.size <= 1 -> "Searching for a host nearby..."
                    else -> "Connected! Wait for host to start."
                }
                Text(
                    text = statusText, 
                    style = MaterialTheme.typography.titleMedium,
                    color = GameColors.TitleAccent,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Match Settings (Host only editable, Guest view only)
                MatchSettingsSection(
                    isHost = isHost,
                    currentFormat = state.format,
                    currentTimeLimitMs = state.timeLimitMs,
                    currentCategory = state.category,
                    availableCategories = state.availableCategories,
                    onFormatChange = onChangeFormat,
                    onTimeLimitChange = onChangeTimeLimit,
                    onCategoryChange = onChangeCategory
                )

                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Players", 
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.align(Alignment.Start).padding(bottom = 8.dp)
                )

                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.players) { player ->
                        PlayerRow(player)
                    }
                }

                JuicyButton(
                    text = if (state.isLocalPlayerReady) "Not ready" else "Ready up",
                    state = if (state.isLocalPlayerReady) AnswerState.SELECTED else AnswerState.IDLE,
                    onClick = onToggleReady,
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun MatchSettingsSection(
    isHost: Boolean,
    currentFormat: GameFormat,
    currentTimeLimitMs: Long,
    currentCategory: String?,
    availableCategories: List<String>,
    onFormatChange: (GameFormat) -> Unit,
    onTimeLimitChange: (Long) -> Unit,
    onCategoryChange: (String?) -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Match Settings", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Format
            Text("Format", style = MaterialTheme.typography.bodySmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(GameFormat.CLASSIC, GameFormat.BUZZER, GameFormat.TIME_ATTACK).forEach { format ->
                    FilterChip(
                        selected = currentFormat == format,
                        onClick = { if (isHost) onFormatChange(format) },
                        label = { Text(format.name.lowercase().capitalize()) },
                        enabled = isHost || currentFormat == format
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Time Limit
            Text("Time Limit", style = MaterialTheme.typography.bodySmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(1000L, 3000L, 5000L, 10000L).forEach { limit ->
                    FilterChip(
                        selected = currentTimeLimitMs == limit,
                        onClick = { if (isHost) onTimeLimitChange(limit) },
                        label = { Text("${limit / 1000}s") },
                        enabled = isHost || currentTimeLimitMs == limit
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Category
            Text("Category", style = MaterialTheme.typography.bodySmall)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = currentCategory == null,
                        onClick = { if (isHost) onCategoryChange(null) },
                        label = { Text("Mixed") },
                        enabled = isHost || currentCategory == null
                    )
                }
                items(availableCategories) { category ->
                    FilterChip(
                        selected = currentCategory == category,
                        onClick = { if (isHost) onCategoryChange(category) },
                        label = { Text(category) },
                        enabled = isHost || currentCategory == category
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayerRow(player: Player) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                when {
                    !player.isConnected -> GameColors.WrongRed.copy(alpha = 0.35f)
                    player.isReady -> GameColors.CorrectGreen.copy(alpha = 0.2f)
                    else -> GameColors.OptionDefault
                }
            )
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val suffix = when {
                !player.isConnected -> " (disconnected)"
                player.isReady -> " - Ready"
                else -> ""
            }
            Text(
                text = player.displayName + suffix,
                fontWeight = if (player.isReady) FontWeight.Bold else FontWeight.Normal,
                color = if (player.isReady) GameColors.CorrectGreen else Color.White
            )
        }
    }
}

private fun String.capitalize() = this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

@Composable
@Preview
fun RoomScreenPreview() {
    RoomContent(
        state = RoomUiState(
            roomId = "LOCAL_HOST_123",
            players = listOf(
                Player(id = "1", displayName = "You", isReady = true, isConnected = true),
                Player(id = "2", displayName = "Friend", isReady = false, isConnected = true)
            ),
            isLocalPlayerReady = true,
            isJoining = false,
            availableCategories = listOf("Basics", "Animals", "Travel")
        ),
        onToggleReady = {}
    )
}
