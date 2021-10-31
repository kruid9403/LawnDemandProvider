package com.jeremykruid.lawndemandprovider.viewModel

import android.app.Application
import android.content.Context
import android.net.Uri
import android.os.Parcel
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.jeremykruid.lawndemandprovider.model.ProviderObject
import com.jeremykruid.lawndemandprovider.services.firebase.FirestoreDataService

class ProfileViewModel(application: Application, registry: ActivityResultRegistry, owner: LifecycleOwner): AndroidViewModel(application) {
    val provider by lazy { MutableLiveData<ProviderObject>() }
    val loadingError by lazy { MutableLiveData<Boolean>() }
    val loading by lazy { MutableLiveData<Boolean>() }
    val uri by lazy { MutableLiveData<Uri>() }
    private val firestore = FirebaseFirestore.getInstance()

    private var getContent : ActivityResultLauncher<String> = registry.register("key", owner, ActivityResultContracts.GetContent()) { result ->
        uri.value = result
    }

    fun updateProviderImage(context: Context, uri: Uri, uid: String){
        loading.value = true
        savePhoto(context, uid, uri)
    }

    fun updateProvider(uid: String, providerObject: ProviderObject){
        FirestoreDataService().setProvider(uid, providerObject)
    }

    fun getFirebaseProvider(context: Context, uid: String){
        loading.value = true
        var newProvider: ProviderObject?
        firestore.collection("providerData").document(uid).get()
            .addOnSuccessListener { snap ->
                newProvider = snap.toObject(ProviderObject::class.java)
                checkProvider(newProvider)
                provider.value = newProvider
                loading.value = false
                loadingError.value = false
            }
            .addOnFailureListener { e ->
                provider.value = null
                loading.value = false
                loadingError.value = true
            }

    }

    private fun checkProvider(newProvider: ProviderObject?) {
        if (newProvider?.id != null){
            provider.value?.id = newProvider.id
        }else{
            provider.value?.id = ""
        }
        if (newProvider?.name != null){
            provider.value?.name = newProvider.name
        }else{
            provider.value?.name = ""
        }
        if (newProvider?.imgUrl != null){
            provider.value?.imgUrl = newProvider.imgUrl
        }else{
            provider.value?.imgUrl = ""
        }
        if (newProvider?.lat != null){
            provider.value?.lat = newProvider.lat
        }
    }

    private fun savePhoto(context: Context, uid: String, uri: Uri) {
        val storage = FirebaseStorage.getInstance().reference
        storage.child(uid).putFile(uri)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    storage.child(uid).downloadUrl
                        .addOnCompleteListener { url ->
                            val providerObject = ProviderObject(uid, null, url.result.toString(), null, null, false, null, false, null)
                            provider.value = providerObject
                            loading.value = false
                            loadingError.value = false
                            FirestoreDataService().setProvider(uid, providerObject)
                        }
                        .addOnFailureListener {
                            provider.value = null
                            loading.value = false
                            loadingError.value = true
                        }
                }
            }
            .addOnFailureListener { e2 ->
                Toast.makeText(context, e2.localizedMessage, Toast.LENGTH_SHORT).show()
            }
    }

    fun selectImage(owner: LifecycleOwner, registry : ActivityResultRegistry){
        getContent.launch("image/*")
    }
}