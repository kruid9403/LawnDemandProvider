package com.jeremykruid.lawndemandprovider.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeremykruid.lawndemandprovider.CoroutineConfig
import com.jeremykruid.lawndemandprovider.services.api.ApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

open class BaseViewModel: ViewModel(), KoinComponent {

    companion object{
        const val MAP_DIRECTION = "https://maps.googleapis.com"

    }

    protected val context: Context by inject()
    protected val coroutineConfig: CoroutineConfig by inject()

    protected fun launchOnIO(block: suspend CoroutineScope.() -> Unit): Job {
        return viewModelScope.launch(coroutineConfig.ioDispatcher) {
            block.invoke(this)
        }
    }

    protected fun launchOnComp(block: suspend CoroutineScope.() -> Unit): Job {
        return viewModelScope.launch(coroutineConfig.compDispatcher) {
            block.invoke(this)
        }
    }

    fun apiServices(context: Context): ApiService {
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(MAP_DIRECTION)
            .build()

        return retrofit.create<ApiService>(ApiService::class.java)
    }

}