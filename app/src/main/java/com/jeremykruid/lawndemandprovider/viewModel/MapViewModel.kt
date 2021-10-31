package com.jeremykruid.lawndemandprovider.viewModel

import android.content.Context
import android.graphics.Color
import android.util.Log
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
import com.google.firebase.functions.FirebaseFunctions
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
import com.jeremykruid.lawndemandprovider.viewModel.MapViewModel.Companion.MAP_VIEW_MODEL
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.core.inject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

class MapViewModel : BaseViewModel() {

    companion object{
        const val MAP_VIEW_MODEL = "MapViewModel"
        const val NEXT_JOB = "getNextJob"
        const val ACCEPT_JOB = "acceptJob"
        const val DECLINE_JOB = "declineJob"
    }

    val loadingError by lazy { MutableLiveData<Boolean>() }
    val loading by lazy { MutableLiveData<Boolean>() }
    val driverState by lazy { MutableLiveData<DriverState>() }

    private var newState = DriverState(false, null, false, null)

    var provider: Provider? = null
    var gettingJob: Boolean = false

    private val providerRepo: ProviderRepo by inject()
    private val providerManager: ProviderManager by inject()
    private val providerDao: ProviderDao by inject()
    private val orderManager: OrderManager by inject()
    private val orderRepo: OrderRepo by inject()

    private val functions = FirebaseFunctions.getInstance()

    private val uid = FirebaseAuth.getInstance().uid.toString()

    var order: OrderObject? = null

    init {

        providerManager.getProvider()

        startProviderListener()

        viewModelScope.launch {
          providerRepo.listenToRepo()
              .catch { error -> Timber.e(error.toString()) }
              .collect {
                  when(it.isSuccessful){
                      true -> {
                          provider = it.data!!

                          if (provider?.nextJob != null && driverState.value?.currentJob != null){
                              orderManager.clearOrder(driverState.value?.currentJob)
                              newState.currentJob = null
                          }
                          if (driverState.value != null) {
                              newState = driverState.value!!
                          }
                          newState.online = provider?.isOnline!!
                          if (provider?.nextJob != "") {
                              orderManager.getOrder(provider?.nextJob!!)
                          }
                          driverState.postValue(newState)
                      }
                      false -> Timber.e("nullish")
                  }
              }
        }

        viewModelScope.launch {
            orderRepo.listenToRepo()
                .catch { error -> Timber.e(error.toString()) }
                .collect {
                    when(it.isSuccessful){
                        true -> {
                            if (it.data != null) {
                                order = it.data
                                if (driverState.value != null) {
                                    newState = driverState.value!!
                                }
                                newState.currentJob = order
                                driverState.postValue(newState)
                            }
                        }
                    }
                }
        }
    }

    private fun startProviderListener() {
        Log.e(MAP_VIEW_MODEL, "start provider listener")
        providerManager.setProviderListener().addSnapshotListener { value, error ->
            if (error != null) {
                Timber.e(error.toString())
            }
            if (value != null && value.exists()){
                provider = providerManager.providerToObject(value.data!!)
                    if (provider != null) {
                        viewModelScope.launch { providerDao.insert(provider) }


                        if (provider?.nextJob != null && driverState.value?.currentJob != null) {
                            viewModelScope.launch { orderManager.clearOrder(driverState.value?.currentJob) }

                            newState.currentJob = null
                        }
                        if (driverState.value != null) {
                            newState = driverState.value!!
                        }
                        newState.online = provider?.isOnline!!
                        if (provider?.nextJob != "") {
                            orderManager.getOrder(provider?.nextJob!!)
                        }
                        driverState.postValue(newState)
                        Timber.e("Provider from manager")

                        if (provider!!.nextJob != "") {
                            orderManager.orderListener(provider!!.nextJob)
                        }
                    }

            }
        }
    }

    fun providerOffline(){
        loading.postValue(true)
        providerManager.providerOffline().call().continueWith { loading.postValue(false) }
    }

    fun providerOnline(){
        loading.postValue(true)
        val data = hashMapOf(
            "lat" to provider?.lat,
            "lon" to provider?.lon,
            "topProvider" to provider?.topProvider,
            "available" to provider?.isAvailable
        )
        providerManager.providerOnline().call(data).continueWith { loading.postValue(false) }
    }

    fun updateProvider(){
        providerManager.setProvider(provider!!)
        if (provider!!.isOnline && provider!!.nextJob == ""){
            getNextJob()
        }
    }

    fun availableWork(){
        if (driverState.value != null){
            newState = driverState.value!!
        }
        viewModelScope.launch {
            Timber.e("coroutine")
            val data = hashMapOf(
                "lat" to provider?.lat,
                "lon" to provider?.lon
            )
            functions.getHttpsCallable(OrderManager.AVAILABLE_LAWNS).call(data).addOnSuccessListener {
                newState.availableLawns = it.data.toString()
                driverState.postValue(newState)
            }

        }
    }

    fun updateMapJob(mapFragment: GoogleMap){
        val boundsBuilder = LatLngBounds.builder()
        val myLatLng = LatLng(provider!!.lat, provider!!.lon)
        val markerOptions = MarkerOptions()
            .position(myLatLng)
            .title("Me")
        val customerLatLon = LatLng(driverState.value?.currentJob!!.lat, driverState.value?.currentJob!!.lon)
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

    private fun getDirections(mapFragment: GoogleMap) {
        val origin = "${provider!!.lat},${provider!!.lon}"
        val destination = "${driverState.value?.currentJob!!.lat},${driverState.value?.currentJob!!.lon}"

        Log.e(MAP_VIEW_MODEL, "Get directions")
        viewModelScope.launch {
            apiServices(context).getDirections(origin, destination, context.getString(R.string.google_maps_key))
                .enqueue(object: Callback<DirectionResponses> {
                    override fun onResponse(
                        call: Call<DirectionResponses>,
                        response: Response<DirectionResponses>
                    ) {
                        if (response.body()?.routes?.size!! > 0) {
                            newState.directions = response.body()
                            driverState.postValue(newState)

                            Log.e("response", response.body().toString())
                        }else{
                            Log.e("response", response.body().toString())
                            newState.directions = null
                            driverState.postValue(newState)
                        }
                    }

                    override fun onFailure(call: Call<DirectionResponses>, t: Throwable) {
                        Log.e("response", t.localizedMessage)

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

    fun getNextJob() {
        gettingJob = true

        val data = hashMapOf(
            "lat" to provider?.lat,
            "lon" to provider?.lon,
            "topProvider" to provider?.topProvider
        )

        functions.getHttpsCallable(NEXT_JOB).call(data).continueWith {
            val result = it.result.data as MutableMap<String, Any>
            newState.currentJob = orderManager.orderToObject(result)
            driverState.postValue(newState)
            gettingJob = false
        }
    }

    fun acceptJob(materialDialog: MaterialDialog) {
        val data = hashMapOf(
            "uid" to uid,
            "orderId" to driverState.value?.currentJob?.orderId
        )
        orderManager.acceptJob().call(data).continueWith {
            if (it.isSuccessful){
                //TODO: UPDATE ACCEPT
                materialDialog.dismiss()
            }
        }
    }

    fun declineJob(materialDialog: MaterialDialog) {
        val data = hashMapOf(
            "uid" to uid,
            "orderId" to driverState.value?.currentJob?.orderId
        )
        orderManager.declineJob().call(data).continueWith {
            if (it.isSuccessful){
                provider?.nextJob = ""
                providerManager.setProvider(provider!!)
                materialDialog.dismiss()
            }
        }
    }

    data class DriverState(
        var online: Boolean = false,
        var currentJob: OrderObject? = null,
        var menuVisibility: Boolean = false,
        var availableLawns: String? = "",
        var directions: DirectionResponses? = null
    )
}