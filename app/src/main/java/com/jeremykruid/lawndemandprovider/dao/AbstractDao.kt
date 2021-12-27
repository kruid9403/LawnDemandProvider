package com.jeremykruid.lawndemandprovider.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import org.koin.core.KoinComponent

@Dao
interface AbstractDao<T>
{
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg obj: T?)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(list: List<T>)

    @Delete
    fun delete(vararg obj: T)

    @Delete
    fun delete(list: List<T>)
}
