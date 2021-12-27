package com.jeremykruid.lawndemandprovider.managers

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.HttpsCallableReference
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jeremykruid.lawndemandprovider.CoroutineConfig
import com.jeremykruid.lawndemandprovider.model.Provider
import com.jeremykruid.lawndemandprovider.model.ProviderDao
import org.koin.core.KoinComponent

class ProviderManager(
    private val orderManager: OrderManager,
    private val providerDao: ProviderDao,
    private val coroutineConfig: CoroutineConfig
): KoinComponent {

    companion object{
        const val PROVIDER_MANAGER = "providerManager"
        const val GET_PROVIDER = "getProvider"
        const val PROVIDER_OFFLINE = "providerOffline"
        const val PROVIDER_ONLINE = "providerOnline"
        const val PROVIDER_DELCINED = "providerDeclined"
        const val ORDERS = "orders"
        const val AVAILABLE = "available"
        const val WAITING_PROVIDER = "waiting_provider"
    }

    private var provider: Provider = Provider("1", "joe", "www.google.com", 0.0,
        0.0, isOnline = false, isAvailable = false, topProvider = false, false)

    private val uid = FirebaseAuth.getInstance().uid.toString()
    private val firestore = FirebaseFirestore.getInstance()
    private val functions = FirebaseFunctions.getInstance()


    fun setProviderListener(): DocumentReference {
        return firestore.collection("providerData").document(uid)
    }

    fun providerToObject(data: MutableMap<String, Any>): Provider {
        return Provider(
            data["id"].toString(),
            data["name"].toString(),
            data["imgUrl"].toString(),
            data["lat"] as Double,
            data["lon"] as Double,
            data["isOnline"] as Boolean,
            data["isAvailable"] as Boolean,
            data["topProvider"] as Boolean,
            data["approved"] as Boolean
        )
    }

    fun stripeListener(): DocumentReference {
        return firestore.collection("providerStripeData").document(uid)
    }

    fun providerOffline(): HttpsCallableReference {
        return functions.getHttpsCallable(PROVIDER_OFFLINE)
    }

    fun providerOnline(): HttpsCallableReference {
        return functions.getHttpsCallable(PROVIDER_ONLINE)
    }

    fun getProvider(){
        Log.e("ProviderManager", uid)
        if (FirebaseAuth.getInstance().uid != null) {
            functions.getHttpsCallable(GET_PROVIDER).call(hashMapOf("uid" to uid))
                .addOnSuccessListener {
                    if (it.data != null) {
                        val stuff = it.data as HashMap<*, *>
                        provider = Provider(
                            stuff["id"].toString(),
                            stuff["name"].toString(),
                            stuff["imgUrl"].toString(),
                            stuff["lat"].toString().toDouble(),
                            stuff["lon"].toString().toDouble(),
                            stuff["isOnline"].toString().toBoolean(),
                            stuff["isAvailable"].toString().toBoolean(),
                            stuff["topProvider"].toString().toBoolean(),
                            stuff["approved"].toString().toBoolean(),
                        )
                        coroutineConfig.applicationLaunchOnIO {
                            providerDao.insert(provider)
                            Log.e("ProviderManager",provider.toString())
                        }
                        Log.e("ProviderManager","New provider")
                    }
                }.addOnFailureListener {
                        Log.e("ProviderManager", it.localizedMessage!!)

                }
            }
        }


    fun setProvider(newProvider: Provider){

        val gson = newProvider.asHashMap(gson = Gson())

        functions.getHttpsCallable("setProvider").call(gson).addOnSuccessListener {
            Log.e("success", "success")
        }.addOnFailureListener {
            Log.e("failure", it.localizedMessage.toString() )
        }
    }

    fun <T> T.asHashMap(gson: Gson): HashMap<String, Any> {
        return convert(gson)
    }

    private inline fun <I, reified O> I.convert(gson: Gson): O {
        val json = gson.toJson(this)
        return gson.fromJson(json, object : TypeToken<O>() {}.type)
    }
}
