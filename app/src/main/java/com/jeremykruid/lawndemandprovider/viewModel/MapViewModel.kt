package com.jeremykruid.lawndemandprovider.viewModel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.jeremykruid.lawndemandprovider.model.FirestoreDataService
import com.jeremykruid.lawndemandprovider.model.ProviderObject
import io.reactivex.disposables.CompositeDisposable

class MapViewModel(application: Application): AndroidViewModel(application) {
    val provider by lazy { MutableLiveData<ProviderObject?>() }
    val loadingError by lazy { MutableLiveData<Boolean>() }
    val loading by lazy { MutableLiveData<Boolean>() }

    private val disposable = CompositeDisposable()
    private val firebaseService = FirestoreDataService()

    fun getProviderData(context: Context, uid: String){
        provider.value = firebaseService.getProviderData(context, uid)
    }

    fun setProviderData(uid: String, providerObject: ProviderObject){
        firebaseService.setProvider(uid, providerObject)
    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }
}