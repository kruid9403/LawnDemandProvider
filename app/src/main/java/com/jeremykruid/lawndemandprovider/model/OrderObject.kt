package com.jeremykruid.lawndemandprovider.model

import androidx.room.*
import com.jeremykruid.lawndemandprovider.dao.AbstractDao
import com.squareup.moshi.JsonClass
import kotlinx.coroutines.flow.Flow

@Entity
@JsonClass(generateAdapter = true)
data class OrderObject @Ignore constructor(
    @PrimaryKey var orderId: String = "",
    var uid: String = "",
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
){
    constructor() : this("0", "")
}

@Dao
interface OrderDao: AbstractDao<OrderObject> {

    @Query("SELECT * FROM OrderObject LIMIT 1")
    fun getOrder(): OrderObject?

    @Query("SELECT * FROM OrderObject LIMIT 1")
    fun listenToOrder(): Flow<OrderObject?>

    @Query("DELETE FROM OrderObject")
    fun nukeOrder()
}
