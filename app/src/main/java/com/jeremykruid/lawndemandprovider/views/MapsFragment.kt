package com.jeremykruid.lawndemandprovider.views

import androidx.fragment.app.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.jeremykruid.lawndemandprovider.R
import com.jeremykruid.lawndemandprovider.databinding.FragmentMapsBinding
import com.jeremykruid.lawndemandprovider.model.FirestoreDataService
import com.jeremykruid.lawndemandprovider.model.ProviderObject
import com.jeremykruid.lawndemandprovider.viewModel.MapViewModel
import okio.ByteString.Companion.toByteString

class MapsFragment : Fragment(), View.OnClickListener {

    private lateinit var viewModel: MapViewModel
    private lateinit var binding: FragmentMapsBinding
    private lateinit var auth: FirebaseAuth
    private var provider: ProviderObject = ProviderObject("1", null, null, 1L, 2L)
    private val firestoreService = FirestoreDataService()

    private var providerChecked: Boolean = false

    private val providerCheckedObserver = Observer<Boolean>{ checked ->
        providerChecked = checked
        Toast.makeText(requireContext(), providerChecked.toString(), Toast.LENGTH_SHORT).show()
    }

    private val providerObserver = Observer<ProviderObject?>{ providerData ->
        if (providerData != null) {
            provider = providerData
            Toast.makeText(requireContext(), provider.name, Toast.LENGTH_SHORT).show()
        }else {
            findNavController().navigate(R.id.action_mapsFragment_to_profileFragment)
        }
    }

    private val callback = OnMapReadyCallback { googleMap ->
        //TODO: UPDATE MAP
        val sydney = LatLng(-34.0, 151.0)
        googleMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        auth = FirebaseAuth.getInstance()

        binding = FragmentMapsBinding.inflate(layoutInflater)

        checkAuth()

        return binding.root
    }

    private fun checkAuth() {
        if (auth.currentUser == null){
            findNavController().navigate(R.id.action_mapsFragment_to_login)
        }else{
            binding.mainFab.setOnClickListener(this)        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)

        viewModel = ViewModelProviders.of(this).get(MapViewModel::class.java)
        firestoreService.providerChecked.observe(viewLifecycleOwner, providerCheckedObserver)
        viewModel.provider.observe(viewLifecycleOwner, providerObserver)
        viewModel.getProviderData(requireContext(), auth.uid.toString())
    }

    override fun onClick(v: View?) {
        when(v){
            binding.mainFab -> {
                Toast.makeText(requireContext(), "Click", Toast.LENGTH_SHORT).show()
            }
        }
    }
}