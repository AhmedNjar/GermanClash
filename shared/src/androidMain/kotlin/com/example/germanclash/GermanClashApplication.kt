package com.example.germanclash

import android.app.Application
import com.example.germanclash.di.androidModule
import com.example.germanclash.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class GermanClashApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@GermanClashApplication)
            modules(appModules + androidModule)
        }
    }
}
