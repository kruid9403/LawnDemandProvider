package com.jeremykruid.lawndemandprovider.services.firebase

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.functions.FirebaseFunctions
import com.jeremykruid.lawndemandprovider.model.OrderObject
import com.jeremykruid.lawndemandprovider.model.ProviderObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

class FirestoreDataService {

    private val firestore = FirebaseFirestore.getInstance()
    private val functions = FirebaseFunctions.getInstance()

    // add provider data to the database
    fun setProvider(uid: String, providerObject: ProviderObject) {
        val ref =firestore.collection("providerData")
            .document(uid)

        if (providerObject.id != null){
            val data = hashMapOf(
                "uid" to providerObject.id
            )
            ref.set(data, SetOptions.merge())
        }

        if (providerObject.imgUrl != null){
            val data = hashMapOf(
                "imgUrl" to providerObject.imgUrl
            )
            ref.set(data, SetOptions.merge())
        }

        if (providerObject.name != null){
            val data = hashMapOf(
                "name" to providerObject.name
            )
            ref.set(data, SetOptions.merge())
        }

        if (providerObject.lat != null){
            val data = hashMapOf(
                "lat" to providerObject.lat
            )
            ref.set(data, SetOptions.merge())
        }

        if (providerObject.lon != null){
            val data = hashMapOf(
                "lon" to providerObject.lon
            )
            ref.set(data, SetOptions.merge())
        }

        if (providerObject.topProvider != null) {
            val data = hashMapOf(
                "topProvider" to (providerObject.topProvider)
            )
            ref.set(data, SetOptions.merge())
        }

        if (providerObject.isOnline!!){
            Log.e("updateProvider", providerObject.isOnline.toString())
            val data = hashMapOf(
                "isOnline" to (true)
            )
            ref.set(data, SetOptions.merge())
        }else{
            Log.e("updateProvider", providerObject.isOnline.toString())
            val data = hashMapOf(
                "isOnline" to (false)
            )
            ref.set(data, SetOptions.merge())
        }

        if (providerObject.approved){
            val data = hashMapOf(
                "approved" to (true)
            )
            ref.set(data, SetOptions.merge())
        }
    }

    fun updateOrder(orderObject: OrderObject){
        firestore.collection("orders").document(orderObject.orderId!!).set(orderObject)
    }
}