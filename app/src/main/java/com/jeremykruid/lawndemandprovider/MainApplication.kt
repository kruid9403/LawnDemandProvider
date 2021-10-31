package com.jeremykruid.lawndemandprovider

import android.app.Application
import com.jeremykruid.lawndemandprovider.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.core.module.Module

class MainApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        val modules: List<Module> = listOf(appModule)

        startKoin{
            androidLogger(Level.ERROR)
            androidContext(this@MainApplication)
            modules(modules)
        }
    }
}