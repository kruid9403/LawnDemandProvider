package com.jeremykruid.lawndemandprovider.ui

import android.content.Intent
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.firebase.auth.FirebaseAuth
import com.jeremykruid.lawndemandprovider.R
import com.jeremykruid.lawndemandprovider.databinding.FragmentMapsBinding
import com.jeremykruid.lawndemandprovider.model.OrderObject
import com.jeremykruid.lawndemandprovider.viewModel.MapViewModel
import java.util.*

class MapsFragment : Fragment(), View.OnClickListener {

    private lateinit var viewModel: MapViewModel
    private lateinit var binding: FragmentMapsBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var fusedLocationProvider: FusedLocationProviderClient
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var map: GoogleMap

    private var lat: Double? = null
    private var lon: Double? = null
    private var nextJob: OrderObject? = null

    private val LOCATION_REQUEST_CODE = 1

    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest

    private val requestLocation =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
            } else {
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("Permission Required")
                builder.setMessage("Location permissions are required to use this feature")
                builder.setPositiveButton("Ok"){
                        _, _ ->
                    val myAppSettings = Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:" + "com.jeremykruid.lawndemandprovider")
                    )
                    myAppSettings.addCategory(Intent.CATEGORY_DEFAULT)
                    myAppSettings.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivityForResult(myAppSettings, LOCATION_REQUEST_CODE)
                }
                val dialog = builder.create()
                dialog.show()
            }
        }

    private val loadingObserver = Observer<Boolean> {
        if (it){
            binding.mapProgressBar.visibility = View.VISIBLE
        }else{
            if (viewModel.provider != null) {
                binding.mapProgressBar.visibility = View.GONE
            }
        }
    }

    private fun updateUI(driverState: MapViewModel.DriverState) {
        if (driverState.directions != null && driverState.currentJob != null) {
            Log.e("update ui", "not null")
            viewModel.updateMapJob(map)
            viewModel.drawPolyline(map)
        }else{
            viewModel.updateMapNoJob(map)
            Log.e("update ui", "null")
        }

        if(driverState.currentJob == null && driverState.availableLawns == null){
            viewModel.availableWork()
        }else if (driverState.currentJob != null && driverState.currentJob?.completed != "pending") {
            binding.mapJobCount.text = "${driverState.currentJob?.streetAddress.toString()} \n ${
                driverState.directions?.routes?.get(0)?.legs?.get(0)?.distance?.text}"
        }else{
            binding.mapJobCount.text = driverState.availableLawns
        }

        when (driverState.online){
            true -> {
                binding.mapGetWorkBtn.text = getString(R.string.go_offline)
                when(driverState.currentJob){
                    null -> {
                        Log.e("online", driverState.toString())
                        if (viewModel.provider?.nextJob == "") {
                            if (!viewModel.gettingJob) {
                                viewModel.getNextJob()
                            }
                        }
                    }
                    else -> {
                        when(driverState.currentJob!!.completed){
                            "pending" -> {
                                nextJobDialog()
                                binding.mapGoBtn.visibility = View.GONE
                            }
                            "accepted" -> {
                                binding.mapGoBtn.visibility = View.VISIBLE
                            }
                            else -> {
                                binding.mapGoBtn.visibility = View.GONE
                            }
                        }
                    }
                }
            }
            false -> binding.mapGetWorkBtn.text = getString(R.string.go_online)
        }
        when(driverState.menuVisibility){
            true -> binding.mapsMenuLayout.visibility = View.VISIBLE
            false -> binding.mapsMenuLayout.visibility = View.GONE
        }

    }

    private val callback = OnMapReadyCallback { mapReady ->
        map = mapReady
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        requestLocation.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)

        auth = FirebaseAuth.getInstance()

        binding = FragmentMapsBinding.inflate(layoutInflater)

        setupLocation()

        checkAuth()

        return binding.root
    }

    private fun checkAuth() {
        // send provider to login if auth is null
        if (auth.currentUser == null){
            findNavController().navigate(R.id.action_mapsFragment_to_login)
        }else{
            setClickListeners()
        }
    }

    private fun setClickListeners() {
        binding.mainFab.setOnClickListener(this)
        binding.mapGetWorkBtn.setOnClickListener(this)
        binding.mapGetSettingsBtn.setOnClickListener(this)
        binding.mapGoBtn.setOnClickListener(this)
    }

    private fun setupLocation() {
        locationCallback = object: LocationCallback(){
            override fun onLocationResult(result: LocationResult) {
                if (!result.lastLocation.isFromMockProvider) {
                    lat = result.lastLocation.latitude
                    lon = result.lastLocation.longitude

                    if (lat != null && lon != null) {
                        viewModel.provider?.lat = lat as Double
                        viewModel.provider?.lon = lon as Double

                    }else{
                        Toast.makeText(requireContext(), "Location Null", Toast.LENGTH_SHORT).show()
                    }
                }else{
                    Toast.makeText(requireContext(), "Fake Location Detected", Toast.LENGTH_SHORT).show()
                }
            }
        }

        locationRequest = LocationRequest.create()
            .setPriority(PRIORITY_BALANCED_POWER_ACCURACY)
            .setInterval(1000L)
            .setSmallestDisplacement(100f)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(callback)

        viewModel = ViewModelProviders.of(this).get(MapViewModel::class.java)
        viewModel.loading.observe(viewLifecycleOwner, loadingObserver)
        //viewModel.getProviderData(requireContext(), auth.uid.toString())
        viewModel.driverState.observe(viewLifecycleOwner){
            updateUI(it)
        }
    }

    override fun onClick(v: View?) {
        when(v){
            binding.mainFab -> {
                if (binding.mapsMenuLayout.visibility == View.VISIBLE){
                    binding.mapsMenuLayout.visibility = View.GONE
                }else{
                    binding.mapsMenuLayout.visibility = View.VISIBLE
                }
            }
            binding.mapGetWorkBtn -> {
                when (viewModel.provider?.isOnline){
                    false -> {
                        if (lat != null && lon != null) {
                            viewModel.providerOnline()
                        }else{
                            Toast.makeText(requireContext(), "Location Missing", Toast.LENGTH_SHORT).show()
                        }
                    }
                    true -> {
                        viewModel.providerOffline()
                    }
                }
            }
            binding.mapGoBtn -> {
                val uri: String = java.lang.String.format(Locale.ENGLISH, "geo:0,0?q=${
                    viewModel.driverState.value?.currentJob?.streetAddress} ${
                        viewModel.driverState.value?.currentJob?.city} ${
                            viewModel.driverState.value?.currentJob?.state}")
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                startActivity(intent)
            }

            binding.mapGetSettingsBtn -> {
                //TODO: SETTINGS FRAGMENT
                Log.e("settings", "click")
            }
        }
    }

    private fun nextJobDialog(){
        if (viewModel.driverState.value?.currentJob != null) {
            MaterialDialog(requireContext()).show {
                lifecycleOwner()
                title(R.string.next_job)
                message(
                    text = getString(
                        R.string.price,
                        viewModel.driverState.value?.currentJob?.price.toString()
                    )
                )


                positiveButton(R.string.accept) {
                    viewModel.acceptJob(this)
                }

                negativeButton(R.string.decline) {
                    viewModel.declineJob(this)
                }

            }
        }
    }

    override fun onResume() {
        super.onResume()
        setupLocation()
        checkAuth()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::fusedLocationProvider.isInitialized) {
            fusedLocationProvider.removeLocationUpdates(locationCallback)
        }
    }
}