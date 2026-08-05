package com.app.germanclash.di

import com.app.germanclash.domain.usecase.AdvanceToNextQuestionUseCase
import com.app.germanclash.domain.usecase.JoinRoomUseCase
import com.app.germanclash.domain.usecase.ObserveGameSessionUseCase
import com.app.germanclash.domain.usecase.StartMatchUseCase
import com.app.germanclash.domain.usecase.SubmitAnswerUseCase
import org.koin.dsl.module

val domainModule = module {
    factory { ObserveGameSessionUseCase(get()) }
    factory { SubmitAnswerUseCase(get()) }
    factory { JoinRoomUseCase(get()) }
    factory { AdvanceToNextQuestionUseCase(get()) }
    factory { StartMatchUseCase(get()) }
}
