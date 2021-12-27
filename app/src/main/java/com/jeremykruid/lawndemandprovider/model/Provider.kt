package com.jeremykruid.lawndemandprovider.model

import androidx.room.*
import com.jeremykruid.lawndemandprovider.dao.AbstractDao
import com.squareup.moshi.JsonClass
import kotlinx.coroutines.flow.Flow

@Entity
@JsonClass(generateAdapter = true)
data class Provider(
    @PrimaryKey var id: String,
    var name: String = "",
    var imgUrl: String = "",
    var lat: Double = 0.0,
    var lon: Double = 0.0,
    var isOnline: Boolean = false,
    var isAvailable: Boolean = false,
    var topProvider: Boolean = false,
    var approved: Boolean = false
)

@Dao
interface ProviderDao: AbstractDao<Provider> {

    @Query("SELECT * FROM provider LIMIT 1")
    fun getProvider(): Provider?

    @Query("SELECT * FROM provider LIMIT 1")
    fun listenToProvider(): Flow<Provider?>

}