package com.jeremykruid.lawndemandprovider.ui.startJob

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.jeremykruid.lawndemandprovider.R
import com.jeremykruid.lawndemandprovider.databinding.FragmentStartJobBinding
import com.jeremykruid.lawndemandprovider.model.adapter.StartJobPhotoAdapter

class StartJobFragment : Fragment(), StartJobPhotoAdapter.PhotoClicked {

    private lateinit var binding: FragmentStartJobBinding
    private lateinit var viewModel: StartJobViewModel
    private lateinit var adapter: StartJobPhotoAdapter

    private var orderId: String = ""
    private var photoList: ArrayList<Bitmap> = ArrayList()

    val photoPermission =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                val i = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                photoResult.launch(i)
            } else {
                shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)
            }
        }


    var photoResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes
            val data: Intent? = result.data
            val bitmap = data?.extras?.get("data") as Bitmap
            photoList.add(bitmap)

            adapter.notifyItemRangeChanged(0, photoList.size)
            Toast.makeText(requireContext(), photoList.size.toString(), Toast.LENGTH_SHORT).show()
            takeStartingPhotos()


        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentStartJobBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this).get(StartJobViewModel::class.java)

        orderId = arguments?.get("orderId")?.toString().toString()


        if (orderId != ""){
            checkAccessibility()
        }else{
            Toast.makeText(requireContext(), "An Error Has Occurred", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_startJobFragment_to_mapsFragment)
        }

        binding.startJobRecycler.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        adapter = StartJobPhotoAdapter(requireContext(), photoList, this)
        binding.startJobRecycler.adapter = adapter

        return binding.root
    }

    private fun checkAccessibility() {
        val dialog = AlertDialog.Builder(requireContext())
        dialog.setTitle("Is the back yard accessible?")
        dialog.setPositiveButton("YES") { d, i ->
            d.dismiss()
            checkOvergrown()
        }
        dialog.setNegativeButton("NO") { d, i ->
            d.dismiss()
            cancelOrder()
            findNavController().navigate(R.id.action_startJobFragment_to_mapsFragment)
        }
        dialog.show()
    }

    private fun checkOvergrown() {
        val dialog = AlertDialog.Builder(requireContext())
        dialog.setTitle("Is the grass overgrown?")

        dialog.setPositiveButton("YES") { d, i ->
            d.dismiss()
            cancelOrder()
        }

        dialog.setNegativeButton("NO") { d, i ->
            d.dismiss()
            takeStartingPhotos()
        }
        dialog.show()
    }

    private fun takeStartingPhotos() {
        val dialog = AlertDialog.Builder(requireContext())
        if (photoList.size == 0) {
            dialog.setMessage("Take before photos")
            dialog.setCancelable(false)
            dialog.setPositiveButton("Start Photos") { d, i ->
                photoPermission.launch(Manifest.permission.CAMERA)
            }
            dialog.show()
        }else if (photoList.size == 1){
            dialog.setMessage("Add more photos")
            dialog.setCancelable(false)
            dialog.setPositiveButton("Next Photo") { d, i ->
                photoPermission.launch(Manifest.permission.CAMERA)
            }
            dialog.show()
        }else if (photoList.size in 2..3){
            dialog.setMessage("Would you like to add additional photos")
            dialog.setCancelable(false)
            dialog.setPositiveButton("Additional Photos") { d, i ->
                photoPermission.launch(Manifest.permission.CAMERA)
            }
            dialog.setNegativeButton("NO") {d,i ->
                d.dismiss()
            }
            dialog.show()
        }else{

        }
    }

    private fun cancelOrder() {
        //TODO CANCEL ORDER AND CHARGE THE CUSTOMER $10. $5 FOR THE PROVIDER
    }

    override fun photoClicked(bitmap: Bitmap, position: Int) {
        val dialog = AlertDialog.Builder(requireContext())
        dialog.setMessage("Replace Photo?")
        dialog.setPositiveButton("YES"){ d, i ->
            photoList.remove(bitmap)
            adapter.notifyItemRemoved(position)
            photoPermission.launch(Manifest.permission.CAMERA)
        }
        dialog.setNegativeButton("NO"){ d, i ->
            d.dismiss()
        }
        dialog.show()
    }
}