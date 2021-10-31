package com.jeremykruid.lawndemandprovider.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.jeremykruid.lawndemandprovider.model.OrderDao
import com.jeremykruid.lawndemandprovider.model.OrderObject
import com.jeremykruid.lawndemandprovider.model.Provider
import com.jeremykruid.lawndemandprovider.model.ProviderDao

@Database(
    entities = [Provider::class, OrderObject::class],
    version = 6,
    exportSchema = false
)


abstract class Database: RoomDatabase() {
    abstract fun providerDao(): ProviderDao
    abstract fun orderDao(): OrderDao
}