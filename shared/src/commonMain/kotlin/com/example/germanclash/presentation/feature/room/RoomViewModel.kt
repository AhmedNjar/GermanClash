package com.example.germanclash.presentation.feature.room

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.germanclash.core.result.Result
import com.example.germanclash.domain.usecase.JoinRoomUseCase
import com.example.germanclash.domain.usecase.ObserveGameSessionUseCase
import com.example.germanclash.domain.usecase.ToggleReadyUseCase
import com.example.germanclash.domain.usecase.UpdateSettingsUseCase
import com.example.germanclash.data.local.questionbank.QuestionBank
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
    private val toggleReadyUseCase: ToggleReadyUseCase,
    private val updateSettingsUseCase: UpdateSettingsUseCase,
    private val questionBank: QuestionBank
) : ViewModel() {

    private val _state = MutableStateFlow(RoomUiState(availableCategories = questionBank.availableCategories()))
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
            is RoomIntent.ChangeFormat -> changeSettings(format = intent.format)
            is RoomIntent.ChangeTimeLimit -> changeSettings(timeLimitMs = intent.timeLimitMs)
            is RoomIntent.ChangeCategory -> changeSettings(category = intent.category)
        }
    }

    private fun changeSettings(
        format: com.example.germanclash.domain.model.GameFormat = _state.value.format,
        timeLimitMs: Long = _state.value.timeLimitMs,
        category: String? = _state.value.category
    ) {
        viewModelScope.launch {
            updateSettingsUseCase(_state.value.roomId, format, timeLimitMs, category)
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
                _state.value = _state.value.copy(
                    isJoining = false,
                    players = session.players,
                    format = session.format,
                    timeLimitMs = session.timeLimitMs,
                    category = session.category
                )
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
        
        viewModelScope.launch {
            toggleReadyUseCase(_state.value.roomId, localPlayerId, isNowReady)
        }
    }
}
