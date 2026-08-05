package com.app.germanclash.di

import com.app.germanclash.presentation.feature.game.GameViewModel
import com.app.germanclash.presentation.feature.room.RoomViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

// requires the koin-compose-viewmodel artifact for KMP ViewModel support
val featureModule = module {
    viewModel { (roomId: String) ->
        GameViewModel(
            roomId = roomId,
            observeGameSession = get(),
            submitAnswer = get(),
            advanceToNextQuestion = get()
        )
    }
    viewModel { (localPlayerId: String) ->
        RoomViewModel(
            localPlayerId = localPlayerId,
            joinRoom = get(),
            observeGameSession = get(),
            startMatch = get()
        )
    }
}
