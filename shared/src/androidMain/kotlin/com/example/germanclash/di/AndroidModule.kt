package com.example.germanclash.di

import com.example.germanclash.core.contracts.AndroidDateProvider
import com.example.germanclash.core.contracts.DateProvider
import com.example.germanclash.core.contracts.P2PConnectionClient
import com.example.germanclash.data.local.nearby.AndroidP2PConnectionClient
import com.example.germanclash.data.local.platform.AndroidLocalStatsRepository
import com.example.germanclash.data.local.questionbank.AndroidAssetQuestionBank
import com.example.germanclash.data.local.questionbank.QuestionBank
import com.example.germanclash.domain.repository.LocalStatsRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val androidModule = module {
    single<P2PConnectionClient> { AndroidP2PConnectionClient(context = androidContext()) }
    single<DateProvider> { AndroidDateProvider() }
    single<LocalStatsRepository> { AndroidLocalStatsRepository(context = androidContext()) }
    single<QuestionBank> {
        AndroidAssetQuestionBank(
            context = androidContext(),
            assetFilesByLevel = mapOf("A1" to "vocabulary_a1.json", "A2" to "vocabulary_a2.json")
        )
    }
}
