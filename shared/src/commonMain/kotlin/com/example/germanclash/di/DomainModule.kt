package com.example.germanclash.di

import com.example.germanclash.domain.usecase.AdvanceToNextQuestionUseCase
import com.example.germanclash.domain.usecase.JoinRoomUseCase
import com.example.germanclash.domain.usecase.ObserveGameSessionUseCase
import com.example.germanclash.domain.usecase.StartMatchUseCase
import com.example.germanclash.domain.usecase.SubmitAnswerUseCase
import org.koin.dsl.module

val domainModule = module {
    factory { ObserveGameSessionUseCase(get()) }
    factory { SubmitAnswerUseCase(get()) }
    factory { JoinRoomUseCase(get()) }
    factory { AdvanceToNextQuestionUseCase(get()) }
    factory { StartMatchUseCase(get()) }
}
