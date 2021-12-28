package com.jeremykruid.lawndemandprovider.ui

import android.app.Dialog
import android.content.Intent
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.jeremykruid.lawndemandprovider.R
import com.jeremykruid.lawndemandprovider.databinding.FragmentMapsBinding
import com.jeremykruid.lawndemandprovider.model.OrderObject
import com.jeremykruid.lawndemandprovider.utilities.Constants.MapsFragmentConst.Companion.MAPS_FRAGMENT
import com.jeremykruid.lawndemandprovider.utilities.Constants.MessagingConst.Companion.ACCEPT_JOB
import com.jeremykruid.lawndemandprovider.utilities.Constants.MessagingConst.Companion.DECLINE_JOB
import com.jeremykruid.lawndemandprovider.utilities.Constants.MessagingConst.Companion.DECLINE_LOG_OUT
import com.jeremykruid.lawndemandprovider.viewModel.MapViewModel
import com.squareup.kotlinpoet.NUMBER
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
    
    private lateinit var driverStateFrag: MapViewModel.DriverState
    private var notificationOrderId: String = ""
    private var jobDialog: MaterialDialog? = null

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
        Log.e(MAPS_FRAGMENT, driverState.toString())
        if(driverState.provider?.isOnline!!){
            binding.mapGetWorkBtn.text = getString(R.string.go_offline)
        }else{
            binding.mapGetWorkBtn.text = getString(R.string.go_online)
        }

        if (driverState.pendingJob?.completed.equals("pending") && arguments == null){
            nextJobDialog()
        } else {
            if (jobDialog != null){
                jobDialog?.dismiss()
            }
        }
        
        if (driverState.currentJob?.completed.equals("accepted") &&
            driverState.directions == null){
                viewModel.updateMapNoJob(map)
        }

        if (driverState.currentJob?.completed == "accepted" && driverState.directions != null){
            viewModel.updateMapJob(map)
            binding.mapJobCount.text = driverState.currentJob?.streetAddress + "\n" +
                    driverState.currentJob?.city + ", " + driverState.currentJob?.state + "\n" +
                    (driverState.directions?.routes?.get(0)?.legs?.get(0)?.distance?.text)

            if (driverState.directions?.routes?.get(0)?.legs?.get(0)?.distance?.value!! <= 25){
                binding.mapGoBtn.visibility = View.VISIBLE
                binding.mapGoBtn.setOnClickListener {
                    val bundle = bundleOf(
                        "orderId" to driverState.currentJob?.orderId
                    )
                    findNavController().navigate(R.id.action_mapsFragment_to_startJobFragment, bundle)
                }
            }else{
                binding.mapGoBtn.visibility = View.GONE
            }
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

        checkAuth()

        setupLocation()

        return binding.root
    }

    private fun checkArguments() {
        Log.e("MapsFrag", arguments.toString())
        if (arguments?.get(ACCEPT_JOB) != null){
            Handler(Looper.getMainLooper()).postDelayed( {
                viewModel.acceptJobFromNotification(arguments?.getString(ACCEPT_JOB).toString())
                Log.e("MapsFrag", arguments?.getString(ACCEPT_JOB).toString()+ "ACCEPT")
            }, 1000)
        }

        if (arguments?.get(DECLINE_JOB) != null){
            Handler(Looper.getMainLooper()).postDelayed( {
                viewModel.declineJobNotification(arguments?.getString(DECLINE_JOB).toString())

                Log.e("MapsFrag", arguments?.getString(DECLINE_JOB).toString() + "DECLINE")
            }, 1000)

        }

        if (arguments?.get(DECLINE_LOG_OUT) != null){
            Handler(Looper.getMainLooper()).postDelayed( {
                viewModel.declineLogOut(arguments?.getString(DECLINE_LOG_OUT).toString())
            }, 1000)
        }
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

                        if (viewModel.driverState.value?.directions != null){
                            viewModel.getDirections(map)
                        }

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
            .setInterval(10000L)
            .setSmallestDisplacement(25f)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(callback)

        viewModel = ViewModelProvider(this).get(MapViewModel::class.java)

        viewModel.startOrderListener()
        viewModel.refreshProvider()
        viewModel.updateToken()
        viewModel.loading.observe(viewLifecycleOwner, loadingObserver)
        //viewModel.getProviderData(requireContext(), auth.uid.toString())
        viewModel.driverState.observe(viewLifecycleOwner){
            updateUI(it)
        }


        checkArguments()

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
                        if (viewModel.provider?.lat != null && viewModel.provider?.lon != null) {
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
            binding.mapGetSettingsBtn -> {
                findNavController().navigate(R.id.action_mapsFragment_to_settingsFragment)
            }
        }
    }

    private fun nextJobDialog(){
        if (viewModel.driverState.value?.pendingJob != null) {
            jobDialog = MaterialDialog(requireContext()).show {
                lifecycleOwner()
                title(R.string.next_job)
                message(
                    text = getString(
                        R.string.price,
                        viewModel.driverState.value?.pendingJob?.price.toString()
                    )
                )


                positiveButton(R.string.accept) {
                    viewModel.acceptJob(this)
                }

                negativeButton(R.string.decline) {
                    //TODO FIX 2 CLICK PROBLEM
                    viewModel.declineJob()
                    dismiss()
                }

            }
        }
    }

    override fun onResume() {
        super.onResume()
        auth = FirebaseAuth.getInstance()
        viewModel = ViewModelProvider(this).get(MapViewModel::class.java)
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