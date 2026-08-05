package com.example.germanclash.di

import com.example.germanclash.core.contracts.MultiplayerDataSource
import com.example.germanclash.data.local.datasource.NearbyP2PDataSource
import com.example.germanclash.data.local.datasource.SoloDataSource
import com.example.germanclash.data.local.questionbank.PracticeFilterState
import com.example.germanclash.data.local.serialization.P2PMessageCodec
import com.example.germanclash.data.remote.datasource.FirestoreDataSource
import com.example.germanclash.data.remote.mapper.GameSessionMapper
import com.example.germanclash.data.repository.GameRepositoryImpl
import com.example.germanclash.domain.repository.GameRepository
import org.koin.core.qualifier.named
import org.koin.dsl.module

// Qualifiers - FirestoreDataSource, NearbyP2PDataSource, and SoloDataSource
// all implement MultiplayerDataSource, so Koin needs a way to tell them apart.
val ONLINE_SOURCE = named("online")
val OFFLINE_SOURCE = named("offline")
val SOLO_SOURCE = named("solo")

val dataModule = module {
    single { GameSessionMapper() }
    single { P2PMessageCodec() }
    single { PracticeFilterState() }
    // QuestionBank is bound in androidModule (AndroidAssetQuestionBank) since
    // loading the bundled JSON assets needs a Context.

    single<MultiplayerDataSource>(ONLINE_SOURCE) { FirestoreDataSource(mapper = get()) }
    single<MultiplayerDataSource>(OFFLINE_SOURCE) {
        NearbyP2PDataSource(connectionClient = get(), codec = get(), questionBank = get(), scope = get())
    }
    single<MultiplayerDataSource>(SOLO_SOURCE) {
        SoloDataSource(questionBank = get(), practiceFilterState = get())
    }

    single<GameRepository> {
        GameRepositoryImpl(
            onlineSource = get(ONLINE_SOURCE),
            offlineSource = get(OFFLINE_SOURCE),
            soloSource = get(SOLO_SOURCE)
        )
    }
}
