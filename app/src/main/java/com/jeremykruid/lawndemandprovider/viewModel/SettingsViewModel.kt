package com.jeremykruid.lawndemandprovider.viewModel

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.navigation.Navigation
import com.jeremykruid.lawndemandprovider.R
import com.jeremykruid.lawndemandprovider.managers.ProviderManager
import com.squareup.kotlinpoet.SET
import org.koin.core.inject

class SettingsViewModel(application: Application) : BaseViewModel(application)  {

    companion object {
        const val SETTINGS_VIEW_MODEL = "SettingsViewModel"
    }

    val accountLink by lazy {MutableLiveData<String>()}

    private val providerManager: ProviderManager by inject()

    init {
        providerManager.stripeListener().addSnapshotListener { value, error ->
            if (error != null){
                Log.e("SettingsViewModel", error.toString())
            }

            if (value != null){
//                Log.e("SettingsViewModel", value.toString())
            }
        }
    }

    fun connectStripe() {
        val userData = hashMapOf(
            "uid" to uid
        )
        functions.getHttpsCallable("setUpStripeProvider").call(userData)
            .addOnSuccessListener {
            setStripeAccountListener()

            }
            .addOnFailureListener {
                Log.e(SETTINGS_VIEW_MODEL, it.toString())
            }
    }

    private fun setStripeAccountListener() {
        firestore.collection("providerStripeData").document(uid).addSnapshotListener { value, error ->
            if (error != null){
                Log.e("SettingsViewModel", error.toString())
            }
            if (value != null){
                Log.e("SettingsViewModel", value.get("id").toString())

                val accountNumber = hashMapOf(
                    "accountNumber" to value.get("id").toString(),
                    "uid" to uid
                )
                functions.getHttpsCallable("stripeAccountLink").call(accountNumber).addOnSuccessListener {
                    val info = it.data as HashMap<*,*>
                    Log.e("SettingsViewModelHASH", info.toString())
                    val link = info.get("url").toString()
                    accountLink.postValue(link)
                }
            }
        }
    }
}