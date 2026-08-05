package com.example.germanclash.di

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.dsl.module

val coreModule = module {
    single { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
}
