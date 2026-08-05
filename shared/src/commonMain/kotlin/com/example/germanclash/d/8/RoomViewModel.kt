package com.app.germanclash.presentation.feature.room

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.germanclash.core.result.Result
import com.app.germanclash.domain.usecase.JoinRoomUseCase
import com.app.germanclash.domain.usecase.ObserveGameSessionUseCase
import com.app.germanclash.domain.usecase.StartMatchUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class RoomViewModel(
    private val localPlayerId: String,
    private val joinRoom: JoinRoomUseCase,
    private val observeGameSession: ObserveGameSessionUseCase,
    private val startMatch: StartMatchUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(RoomUiState())
    val state: StateFlow<RoomUiState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<RoomEffect>()
    val effect: SharedFlow<RoomEffect> = _effect

    fun onIntent(intent: RoomIntent) {
        when (intent) {
            is RoomIntent.JoinRoom -> join(intent.roomId, intent.playerId)
            RoomIntent.ToggleReady -> toggleReady()
            RoomIntent.LeaveRoom -> Unit // wired once the navigation graph exists
            RoomIntent.RetryJoin -> _state.value.roomId.takeIf { it.isNotEmpty() }
                ?.let { join(it, localPlayerId) }
        }
    }

    private fun join(roomId: String, playerId: String) {
        _state.value = _state.value.copy(roomId = roomId, isJoining = true, error = null)

        viewModelScope.launch {
            when (val result = joinRoom(roomId, playerId)) {
                is Result.Success -> observeRoom(roomId)
                is Result.Error -> _state.value = _state.value.copy(isJoining = false, error = result.message)
            }
        }
    }

    private fun observeRoom(roomId: String) {
        observeGameSession(roomId)
            .onEach { session ->
                _state.value = _state.value.copy(isJoining = false, players = session.players)
                // startMatch() (triggered by ToggleReady below) is what gives
                // the session a currentQuestion - once it has one, move to Game.
                if (session.currentQuestion != null) {
                    _effect.emit(RoomEffect.NavigateToGame(roomId))
                }
            }
            .launchIn(viewModelScope)
    }

    private fun toggleReady() {
        val isNowReady = !_state.value.isLocalPlayerReady
        _state.value = _state.value.copy(isLocalPlayerReady = isNowReady)
        // Real ready-state sync across multiple devices (so the match waits
        // for everyone) is still open work - for now, marking yourself ready
        // starts the match directly, matching what's actually reachable today.
        if (isNowReady) {
            viewModelScope.launch { startMatch(_state.value.roomId) }
        }
    }
}
