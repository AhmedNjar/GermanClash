package com.example.germanclash.di

import com.example.germanclash.presentation.feature.game.GameViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

// requires the koin-compose-viewmodel artifact for KMP ViewModel support
val featureModule = module {
    viewModel { (roomId: String) ->
        GameViewModel(roomId = roomId, observeGameSession = get(), submitAnswer = get())
    }
    // TODO: Create RoomViewModel
    /*
    viewModel { (localPlayerId: String) ->
        RoomViewModel(localPlayerId = localPlayerId, joinRoom = get(), observeGameSession = get())
    }
    */
}
