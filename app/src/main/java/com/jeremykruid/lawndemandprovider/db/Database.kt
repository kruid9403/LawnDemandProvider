package com.jeremykruid.lawndemandprovider.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.jeremykruid.lawndemandprovider.model.*

@Database(
    entities = [Provider::class, OrderObject::class],
    version = 8,
    exportSchema = false
)


abstract class Database: RoomDatabase() {
    abstract fun providerDao(): ProviderDao
    abstract fun orderDao(): OrderDao
}