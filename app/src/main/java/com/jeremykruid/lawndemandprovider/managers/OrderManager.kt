package com.jeremykruid.lawndemandprovider.managers

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.HttpsCallableReference
import com.jeremykruid.lawndemandprovider.CoroutineConfig
import com.jeremykruid.lawndemandprovider.model.OrderDao
import com.jeremykruid.lawndemandprovider.model.OrderObject
import com.jeremykruid.lawndemandprovider.utilities.Constants.MapViewModelConst.Companion.ACCEPT_JOB
import com.jeremykruid.lawndemandprovider.utilities.Constants.MapViewModelConst.Companion.DECLINE_JOB
import com.jeremykruid.lawndemandprovider.viewModel.MapViewModel
import org.koin.core.KoinComponent

class OrderManager(
    private val coroutineConfig: CoroutineConfig,
    private val orderDao: OrderDao
): KoinComponent {

    companion object{
        const val ORDERS = "orders"
        const val ORDER_MANAGER = "ORDER MANAGER"
        const val AVAILABLE_LAWNS = "getAvailableLawns"
    }

    var order: OrderObject? = null
    var orderCount: Int? = null

    private val firestore = FirebaseFirestore.getInstance()
    private val functions = FirebaseFunctions.getInstance()

    fun getOrder(orderId: String){
        firestore.collection(ORDERS).document(orderId).get().addOnSuccessListener {
            if (it != null){
                val map = it.toObject(OrderObject::class.java)
                coroutineConfig.applicationLaunchOnIO {
                    orderDao.insert(map!!)
                }

            }
        }
    }

    fun clearOrder(updateOrder: OrderObject?) {
        if (updateOrder != null) {
            orderDao.delete(updateOrder!!)
            order = null
        }
    }

    fun clearPendingOrder(pendingOrder: OrderObject){
        orderDao.delete(pendingOrder)
    }

    fun orderListener(orderId: String){
        firestore.collection(ORDERS).document(orderId).addSnapshotListener { value, error ->
            if (error != null){
                Log.e("OrderManager",error.toString())
            }

            if (value != null && value.exists()){
                orderToObject(value.data!!)

                coroutineConfig.applicationLaunchOnIO {
                    if (order != null) {
                        orderDao.insert(order!!)

                        Log.e("OrderManager", order.toString())
                    }
                }
            }
        }
    }

    fun acceptJob(): HttpsCallableReference {
        return functions.getHttpsCallable(ACCEPT_JOB)
    }

    fun declineJob(): HttpsCallableReference {
        return functions.getHttpsCallable(DECLINE_JOB)
    }

    fun orderToObject(data: MutableMap<String, Any>):OrderObject{
        return OrderObject(
             data["uid"].toString(), data["orderId"].toString(),
            data["imgUrl"].toString(), data["orderDate"].toString().toLong(),
            data["lotSize"].toString().toInt(), data["streetAddress"].toString(),
            data["city"].toString(), data["state"].toString(), data["zip"].toString(),
            data["topProvider"].toString().toBoolean(), data["lat"].toString().toDouble(),
            data["lon"].toString().toDouble(), data["price"].toString().toDouble(),
            data["status"].toString(), data["completed"].toString(),
            data["provider"].toString())
    }

}