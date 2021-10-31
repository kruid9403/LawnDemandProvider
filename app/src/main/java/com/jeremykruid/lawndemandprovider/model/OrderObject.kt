package com.jeremykruid.lawndemandprovider.model

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import com.jeremykruid.lawndemandprovider.dao.AbstractDao
import com.squareup.moshi.JsonClass
import kotlinx.coroutines.flow.Flow

@Entity
@JsonClass(generateAdapter = true)
data class OrderObject (
    var uid: String = "",
    @PrimaryKey var orderId: String = "",
    var imgUrl: String= "",
    var orderDate: Long = 0,
    var lotSize: Int = 0,
    var streetAddress: String = "",
    var city: String = "",
    var state: String = "",
    var zip: String = "",
    var topProvider: Boolean = false,
    var lat: Double = 0.0,
    var lon: Double = 0.0,
    var price: Double = 0.0,
    var status: String = "",
    var completed: String = "",
    var provider: String = ""
)

@Dao
interface OrderDao: AbstractDao<OrderObject> {

    @Query("SELECT * FROM OrderObject LIMIT 1")
    suspend fun getOrder(): OrderObject?

    @Query("SELECT * FROM OrderObject LIMIT 1")
    fun listenToOrder(): Flow<OrderObject?>

}
