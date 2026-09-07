package com.example.germanclash.di

import com.example.germanclash.domain.usecase.*
import org.koin.dsl.module

val domainModule = module {
    factory { ObserveGameSessionUseCase(get()) }
    factory { SubmitAnswerUseCase(get()) }
    factory { JoinRoomUseCase(get()) }
    factory { AdvanceToNextQuestionUseCase(get()) }
    factory { StartMatchUseCase(get()) }
    factory { ToggleReadyUseCase(get()) }
    factory { UpdateSettingsUseCase(get()) }
    factory { RecordSessionStatsUseCase(get(), get()) }
    factory { RecordAnswerStatUseCase(get()) }
    factory { GetStatsSummaryUseCase(get()) }
}
