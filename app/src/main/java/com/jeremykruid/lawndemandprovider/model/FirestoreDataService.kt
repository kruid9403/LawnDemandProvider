package com.jeremykruid.lawndemandprovider.model

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage

class FirestoreDataService {

    val providerChecked by lazy { MutableLiveData<Boolean>() }
    private val firestore = FirebaseFirestore.getInstance()

    //retrieve provider data from the database
    fun getProviderData(context: Context, uid: String): ProviderObject? {
        var provider: ProviderObject? = null
        firestore.collection("providerData").document(uid).get()
            .addOnSuccessListener { snap ->
                provider = snap.toObject(ProviderObject::class.java)
                Toast.makeText(context, provider?.id.toString(), Toast.LENGTH_SHORT).show()
                providerChecked.value = true
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, e.localizedMessage, Toast.LENGTH_SHORT).show()
                providerChecked.value = true
            }

        return provider
    }

    // add provider data to the database
    fun setProvider(uid: String, providerObject: ProviderObject) {

        FirebaseFirestore.getInstance().collection("providerData")
            .document(uid).set(providerObject, SetOptions.merge())
    }

    // save profile photo to firebase storage
    fun savePhoto(context: Context, uri: Uri, uid: String): String {
        var imgUrl = ""

        val storage = FirebaseStorage.getInstance().reference
        storage.child(uid).putFile(uri).addOnCompleteListener {
            if (it.isSuccessful) {
                storage.child(uid).downloadUrl.addOnCompleteListener { url ->
                    imgUrl = url.result.toString()
                }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, e.localizedMessage, Toast.LENGTH_SHORT).show()
                    }
            }
        }
            .addOnFailureListener { e2 ->
                Toast.makeText(context, e2.localizedMessage, Toast.LENGTH_SHORT).show()
            }

        return imgUrl
    }
}