package com.example.germanclash.di

import com.example.germanclash.core.contracts.P2PConnectionClient
import com.example.germanclash.data.local.nearby.AndroidP2PConnectionClient
import com.example.germanclash.data.local.questionbank.AndroidAssetQuestionBank
import com.example.germanclash.data.local.questionbank.QuestionBank
import com.example.germanclash.presentation.common.AndroidSoundEffectPlayer
import com.example.germanclash.presentation.common.SoundEffectPlayer
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val androidModule = module {
    single<P2PConnectionClient> { AndroidP2PConnectionClient(context = androidContext()) }
    single<SoundEffectPlayer> { AndroidSoundEffectPlayer(context = androidContext()) }
    single<QuestionBank> {
        AndroidAssetQuestionBank(
            context = androidContext(),
            assetFileNames = listOf("vocabulary_a1.json", "vocabulary_a2.json")
        )
    }
}