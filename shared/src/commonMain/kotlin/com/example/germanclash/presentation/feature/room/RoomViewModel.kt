package com.example.germanclash.presentation.feature.room

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.germanclash.core.result.Result
import com.example.germanclash.domain.usecase.JoinRoomUseCase
import com.example.germanclash.domain.usecase.ObserveGameSessionUseCase
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
    private val observeGameSession: ObserveGameSessionUseCase
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
                // Game feature owns question flow - once the host starts the
                // round, the session gets a non-null question and everyone moves on.
                if (session.currentQuestion != null) {
                    _effect.emit(RoomEffect.NavigateToGame(roomId))
                }
            }
            .launchIn(viewModelScope)
    }

    private fun toggleReady() {
        _state.value = _state.value.copy(isLocalPlayerReady = !_state.value.isLocalPlayerReady)
        // Persisting this back (e.g. GameRepository.setReady(roomId, playerId, Boolean))
        // is a small, deliberate interface addition left for the next pass.
    }
}
