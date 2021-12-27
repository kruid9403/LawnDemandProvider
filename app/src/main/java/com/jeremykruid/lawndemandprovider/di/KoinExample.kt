package com.jeremykruid.lawndemandprovider.di

import androidx.room.Room
import com.jeremykruid.lawndemandprovider.CoroutineConfig
import com.jeremykruid.lawndemandprovider.CoroutineConfigImpl
import com.jeremykruid.lawndemandprovider.db.Database
import com.jeremykruid.lawndemandprovider.managers.OrderManager
import com.jeremykruid.lawndemandprovider.managers.ProviderManager
import com.jeremykruid.lawndemandprovider.repositories.OrderRepo
import com.jeremykruid.lawndemandprovider.repositories.ProviderRepo
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module


/**
* To initialize koin in the application add this method in the Application class in the onCreate() method.
*         startKoin {
*              androidContext(this@App)
*              modules(appModule)
*           }
*/

/*
    Use the module function to declare a module.
*/
const val PROVIDER_DB = "ProviderDb"
val appModule = module {
    single {
        Room.databaseBuilder(androidContext(),
        Database::class.java,
        PROVIDER_DB)
            .allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .build()
    }
    single { ProviderManager(get(), get(), get()) }
    single { OrderManager(get(), get()) }
    single { get<Database>().providerDao() }
    single { get<Database>().orderDao() }
    single<CoroutineConfig> { CoroutineConfigImpl() }
    factory { ProviderRepo() }
    factory { OrderRepo() }
}

