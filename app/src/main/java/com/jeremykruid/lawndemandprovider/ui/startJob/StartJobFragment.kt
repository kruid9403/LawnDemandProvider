package com.jeremykruid.lawndemandprovider.ui.startJob

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.jeremykruid.lawndemandprovider.R
import com.jeremykruid.lawndemandprovider.databinding.FragmentStartJobBinding

class StartJobFragment : Fragment() {

    private lateinit var binding: FragmentStartJobBinding
    private lateinit var viewModel: StartJobViewModel

    private var orderId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentStartJobBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this).get(StartJobViewModel::class.java)

        orderId = arguments?.get("orderId")?.toString() ?: ""

        if (orderId.equals("")){
            Toast.makeText(requireContext(), "An Error Has Occurred", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_startJobFragment_to_mapsFragment)
        }
        return binding.root
    }

}