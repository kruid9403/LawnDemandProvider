package com.jeremykruid.lawndemandprovider.dao

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import org.koin.core.KoinComponent

interface AbstractDao<T>
{
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg obj: T?)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(list: List<T>)

    @Delete
    suspend fun delete(vararg obj: T)

    @Delete
    suspend fun delete(list: List<T>)
}
