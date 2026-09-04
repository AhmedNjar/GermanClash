package com.example.germanclash.di

import com.example.germanclash.presentation.feature.game.GameViewModel
import com.example.germanclash.presentation.feature.room.RoomViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

// requires the koin-compose-viewmodel artifact for KMP ViewModel support
val featureModule = module {
    viewModel { (roomId: String) ->
        GameViewModel(
            roomId = roomId,
            observeGameSession = get(),
            submitAnswer = get(),
            advanceToNextQuestion = get(),
            recordAnswerStat = get(),
            recordSessionStats = get()
        )
    }
    viewModel { (localPlayerId: String) ->
        RoomViewModel(
            localPlayerId = localPlayerId,
            joinRoom = get(),
            observeGameSession = get(),
            toggleReadyUseCase = get()
        )
    }
}
