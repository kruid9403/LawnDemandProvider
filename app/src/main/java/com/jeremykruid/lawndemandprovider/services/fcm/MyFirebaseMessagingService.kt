package com.jeremykruid.lawndemandprovider.services.fcm

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.jeremykruid.lawndemandprovider.managers.OrderManager
import com.jeremykruid.lawndemandprovider.model.*
import com.squareup.moshi.Json
import kotlinx.coroutines.CoroutineScope
import org.koin.android.ext.android.inject

class MyFirebaseMessagingService(): FirebaseMessagingService() {

    private val orderDao: OrderDao by inject()
    private val providerDao: ProviderDao by inject()

    private var functions = FirebaseFunctions.getInstance()
    private var auth = FirebaseAuth.getInstance()

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        val data = hashMapOf(
            "updateToken" to p0
        )
        functions.getHttpsCallable("updateToken").call(data).addOnCompleteListener {
            Log.e("FirebaseMessaging", p0)
        }
    }

    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)

        when(p0.data["title"].toString()){
            "providerObject" -> {
                val gson = Gson()
                val provider = gson.fromJson(p0.data["body"].toString(), Provider::class.java)
                providerDao.insert(provider)
                Log.e("FCMService", provider.toString())
            }
            "pendingOrder" -> {
                val gson = Gson()
                val pendingOrder = gson.fromJson(p0.data["body"].toString(), OrderObject::class.java)
                Log.e("FCMService", pendingOrder.toString())

                orderDao.insert(pendingOrder)
            }
            "acceptedOrder" -> {
                val gson = Gson()
                val acceptedOrder = gson.fromJson(p0.data["body"].toString(), OrderObject::class.java)
                orderDao.insert(acceptedOrder)
                Log.e("FCMService", "accepted order")
            }
        }
    }
}