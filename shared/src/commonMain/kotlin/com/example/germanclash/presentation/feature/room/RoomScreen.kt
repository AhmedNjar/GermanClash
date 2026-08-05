package com.example.germanclash.presentation.feature.room

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
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

    // No background modifier here - GermanClashBackground paints it once at
    // the root, and Surface there already gives Text below the right default color.
    Box(modifier = Modifier.fillMaxSize()) {
        if (state.isJoining) {
            CircularProgressIndicator(
                color = GameColors.TitleAccent,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val isHost = state.roomId.contains("_HOST_")
                val statusText = when {
                    isHost && state.players.size <= 1 -> "Waiting for a friend to join..."
                    isHost -> "A friend connected! Tap Ready to start."
                    state.players.size <= 1 -> "Searching for a host nearby..."
                    else -> "Connected! Tap Ready when you are."
                }
                Text(text = statusText, modifier = Modifier.padding(bottom = 16.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.players) { player ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    when {
                                        !player.isConnected -> GameColors.WrongRed.copy(alpha = 0.35f)
                                        player.isReady -> GameColors.CorrectGreen
                                        else -> GameColors.OptionDefault
                                    }
                                )
                                .padding(16.dp)
                        ) {
                            val suffix = when {
                                !player.isConnected -> " (disconnected)"
                                player.isReady -> " - ready"
                                else -> ""
                            }
                            Text(text = player.displayName + suffix)
                        }
                    }
                }

                JuicyButton(
                    text = if (state.isLocalPlayerReady) "Not ready" else "Ready up",
                    state = if (state.isLocalPlayerReady) AnswerState.SELECTED else AnswerState.IDLE,
                    onClick = { viewModel.onIntent(RoomIntent.ToggleReady) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
