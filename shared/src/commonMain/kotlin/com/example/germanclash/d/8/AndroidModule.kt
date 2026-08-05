package com.app.germanclash.di

import com.app.germanclash.core.contracts.P2PConnectionClient
import com.app.germanclash.data.local.nearby.AndroidP2PConnectionClient
import com.app.germanclash.data.local.questionbank.AndroidAssetQuestionBank
import com.app.germanclash.data.local.questionbank.QuestionBank
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val androidModule = module {
    single<P2PConnectionClient> { AndroidP2PConnectionClient(context = androidContext()) }
    single<QuestionBank> {
        AndroidAssetQuestionBank(
            context = androidContext(),
            assetFilesByLevel = mapOf("A1" to "vocabulary_a1.json", "A2" to "vocabulary_a2.json")
        )
    }
}
