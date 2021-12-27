package com.jeremykruid.lawndemandprovider.viewModel

import android.app.Application
import android.graphics.Color
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.google.maps.android.PolyUtil
import com.jeremykruid.lawndemandprovider.R
import com.jeremykruid.lawndemandprovider.managers.OrderManager
import com.jeremykruid.lawndemandprovider.managers.ProviderManager
import com.jeremykruid.lawndemandprovider.model.OrderObject
import com.jeremykruid.lawndemandprovider.model.Provider
import com.jeremykruid.lawndemandprovider.model.ProviderDao
import com.jeremykruid.lawndemandprovider.repositories.OrderRepo
import com.jeremykruid.lawndemandprovider.repositories.ProviderRepo
import com.jeremykruid.lawndemandprovider.services.api.DirectionResponses
import com.jeremykruid.lawndemandprovider.utilities.Constants.MapViewModelConst.Companion.MAPS_VIEW_MODEL
import com.jeremykruid.lawndemandprovider.utilities.Constants.MapViewModelConst.Companion.NEXT_JOB
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.core.inject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MapViewModel(application: Application) : BaseViewModel(application) {

    val loadingError by lazy { MutableLiveData<Boolean>() }
    val loading by lazy { MutableLiveData<Boolean>() }
    val driverState by lazy { MutableLiveData<DriverState>() }

    private var newState = DriverState(provider = null, pendingJob = null, currentJob = null, directions = null)

    var provider: Provider? = null
    var gettingJob: Boolean = false

    private val providerRepo: ProviderRepo by inject()
    private val providerManager: ProviderManager by inject()
    private val providerDao: ProviderDao by inject()
    private val orderManager: OrderManager by inject()
    private val orderRepo: OrderRepo by inject()

    private val auth = FirebaseAuth.getInstance()

    var order: OrderObject? = null

    init {
        viewModelScope.launch {
          providerRepo.listenToRepo()
              .catch { error -> Log.e("MapViewModel",error.toString()) }
              .collect {
                  when(it.isSuccessful){
                      true -> {
                          provider = it.data!!

                          newState.provider = it.data
                          driverState.postValue(newState)

                          Log.e(MAPS_VIEW_MODEL, provider.toString())
                      }
                      false -> Log.e("MapViewModel","nullish")
                  }
              }
        }
    }

    fun startOrderListener(){
        viewModelScope.launch {
            orderRepo.listenToRepo()
                .catch { error -> Log.e("MapViewModel", error.toString()) }
                .collect {
                    when (it.isSuccessful) {
                        true -> {
                            if (it.data != null) {
                                order = it.data
                                when(order!!.completed){
                                    "accepted" -> {
                                        newState.currentJob = order
                                        newState.pendingJob = null
                                        driverState.postValue(newState)
                                        Log.e(MAPS_VIEW_MODEL, "accepted")
                                    }

                                    "pending" -> {
                                        newState.pendingJob = order
                                        driverState.postValue(newState)
                                        Log.e(MAPS_VIEW_MODEL, "pending")
                                    }
                                }
                            }
                        }
                    }
                }
        }
    }

    fun refreshProvider(){
        val data = hashMapOf(
            "uid" to uid
        )
        functions.getHttpsCallable("refreshProvider").call(data).continueWith {
            if (it.isSuccessful) {
//                val result = it.result.data as MutableMap<String, Any>
//                newState.provider = providerManager.providerToObject(result)
//                providerDao.insert(newState.provider)
//                driverState.postValue(newState)
                Log.e("MapViewModel", "Provider Refreshed")
            }

        }.addOnFailureListener {
            Log.e("MapViewModel", it.toString())
        }
    }

    fun providerOffline(){
        Log.e("MapViewModel", "ProviderOffline")
        loading.postValue(true)
        providerManager.providerOffline().call().continueWith {
            Log.e("MapViewModel", "ProviderOffline " + it.result.toString())
            loading.postValue(false)
        }
    }

    fun providerOnline(){
        Log.e("MapViewModel", "ProviderOnline")
        loading.postValue(true)
        val data = hashMapOf(
            "lat" to provider?.lat,
            "lon" to provider?.lon,
            "topProvider" to provider?.topProvider,
            "available" to provider?.isAvailable
        )
        providerManager.providerOnline().call(data).continueWith {
            Log.e("MapViewModel", "ProviderOnline " + it.result.toString())
            loading.postValue(false)
        }
    }

    fun updateMapJob(mapFragment: GoogleMap){
        mapFragment.clear()
        val boundsBuilder = LatLngBounds.builder()
        val myLatLng = LatLng(provider!!.lat, provider!!.lon)
        val markerOptions = MarkerOptions()
            .position(myLatLng)
            .title("Me")
        val customerLatLon  = LatLng(driverState.value?.currentJob!!.lat, driverState.value?.currentJob!!.lon)
        val customerMarker = MarkerOptions()
            .position(customerLatLon)
            .title("Customer Location")

        mapFragment.addMarker(markerOptions)
        mapFragment.addMarker(customerMarker)

        boundsBuilder.include(myLatLng)
        boundsBuilder.include(customerLatLon)

        val builder = boundsBuilder.build()

        mapFragment.moveCamera(CameraUpdateFactory.newLatLngBounds(builder, 500))
        val marker = LatLng(driverState.value?.currentJob?.lat!!, driverState.value?.currentJob?.lon!!)
        mapFragment.addMarker(MarkerOptions().position(marker))

        drawPolyline(mapFragment)
    }

    fun updateMapNoJob(mapFragment: GoogleMap) {
        mapFragment.clear()
        //TODO: add pin for locations
        if (provider?.lat != null && provider?.lon != null) {
            moveCamera(mapFragment)
        } else {
            loading.postValue(true)
        }

        if (driverState.value?.currentJob != null && driverState.value?.directions == null) {
            getDirections(mapFragment)
        }
    }

    fun getDirections(mapFragment: GoogleMap) {
        val origin = "${provider!!.lat},${provider!!.lon}"
        val destination = "${driverState.value?.currentJob!!.lat},${driverState.value?.currentJob!!.lon}"

        Log.e(MAPS_VIEW_MODEL, "Get directions")
        viewModelScope.launch {
            apiServices(context).getDirections(origin, destination, context.getString(R.string.google_maps_key))
                .enqueue(object: Callback<DirectionResponses> {
                    override fun onResponse(
                        call: Call<DirectionResponses>,
                        response: Response<DirectionResponses>
                    ) {
                        if (response.body()?.routes?.size!! > 0) {
                            if (driverState.value != null){
                                newState = driverState.value!!
                            }
                            newState.directions = response.body()
                            driverState.postValue(newState)
                        }else{
                            if (driverState.value != null){
                                newState = driverState.value!!
                            }
                            newState.directions = null
                            driverState.postValue(newState)

                        }
                    }

                    override fun onFailure(call: Call<DirectionResponses>, t: Throwable) {
                        Log.e("response", t.localizedMessage)
                        if (driverState.value != null){
                        }
                        newState.directions = null
                        driverState.postValue(newState)

                    }

                })
        }

    }

    fun drawPolyline(map: GoogleMap) {
        val shape = driverState.value?.directions?.routes?.get(0)?.overviewPolyline?.points
        val polyline = PolylineOptions()
            .addAll(PolyUtil.decode(shape))
            .width(8f)
            .color(Color.BLUE)
        map.addPolyline(polyline)


    }

    private fun moveCamera(mapFragment: GoogleMap) {
        val marker = LatLng(provider?.lat!!, provider?.lon!!)
        mapFragment.addMarker(MarkerOptions().position(marker))
        mapFragment.moveCamera(CameraUpdateFactory.newLatLng(marker))
        mapFragment.setMinZoomPreference(12f)
    }

//    fun getNextJob() {
//        gettingJob = true
//
//        val data = hashMapOf(
//            "lat" to provider?.lat,
//            "lon" to provider?.lon,
//            "topProvider" to provider?.topProvider
//        )
//
//        functions.getHttpsCallable(NEXT_JOB).call(data).continueWith {
//            val result = it.result.data as MutableMap<String, Any>
//            newState.currentJob = orderManager.orderToObject(result)
//            driverState.postValue(newState)
//
//            gettingJob = false
//        }
//    }

    fun acceptJob(materialDialog: MaterialDialog) {
        orderManager.clearOrder(driverState.value?.pendingJob)
        newState.pendingJob = null
        val data = hashMapOf(
            "uid" to uid,
            "orderId" to driverState.value?.pendingJob?.orderId
        )
        orderManager.acceptJob().call(data).continueWith {
            if (it.isSuccessful){
                viewModelScope.launch {
//                    pendingRepo.remove(driverState.value?.pendingJob!!)
                }
                materialDialog.dismiss()
            }else{
                Log.e("MapViewModel", it.exception.toString())
                materialDialog.dismiss()
            }
        }
    }

    fun declineJob() {
        if (driverState.value?.pendingJob != null) {
            orderManager.clearPendingOrder(driverState.value?.pendingJob!!)

            Log.e("MapViewModel", driverState.value?.pendingJob?.orderId!!)

            val data = hashMapOf(
                "uid" to uid,
                "orderId" to driverState.value?.pendingJob?.orderId,
                "lat" to provider?.lat,
                "lon" to provider?.lon
            )
            orderManager.declineJob().call(data).addOnSuccessListener {
                Log.e("MapViewModel", "Order Declined")
            }.addOnFailureListener {
                Toast.makeText(context, "${it.localizedMessage}", Toast.LENGTH_SHORT).show()
            }

            newState.pendingJob = null
            driverState.postValue(newState)
        }
    }

    fun updateToken(){
        Log.e("OnCrate", "token")
        FirebaseMessaging.getInstance().token.addOnCompleteListener { token ->
            val data = hashMapOf(
                "updateToken" to token.result,
                "uid" to auth.uid.toString()
            )
            functions.getHttpsCallable("updateToken").call(data).addOnCompleteListener {
                Log.e("FirebaseMessaging", token.result)
            }
        }
    }

    data class DriverState(
        var provider: Provider? = null,
        var pendingJob: OrderObject? = null,
        var currentJob: OrderObject? = null,
        var directions: DirectionResponses? = null
    )
}