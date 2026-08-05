package com.example.germanclash.presentation.feature.room

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
        onIntent = viewModel::onIntent
    )
}

@Composable
fun RoomContent(
    state: RoomUiState,
    onIntent: (RoomIntent) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (state.isJoining) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Room ${state.roomId}", modifier = Modifier.padding(bottom = 16.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.players) { player ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (player.isReady) GameColors.CorrectGreen else GameColors.OptionDefault)
                                .padding(16.dp)
                        ) {
                            Text(text = player.displayName + if (player.isReady) " - ready" else "")
                        }
                    }
                }

                JuicyButton(
                    text = if (state.isLocalPlayerReady) "Not ready" else "Ready up",
                    state = if (state.isLocalPlayerReady) AnswerState.SELECTED else AnswerState.IDLE,
                    onClick = { onIntent(RoomIntent.ToggleReady) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
@Preview
fun RoomScreenPreview() {
    RoomContent(
        state = RoomUiState(
            roomId = "12345",
            players = listOf(
                Player("1", "Alice", 0, true),
                Player("2", "Bob", 0, false),
                Player("3", "Charlie", 0, false)
            ),
            isLocalPlayerReady = false,
            isJoining = false
        ),
        onIntent = {}
    )
}
