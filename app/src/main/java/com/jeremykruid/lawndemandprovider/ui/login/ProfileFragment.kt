package com.jeremykruid.lawndemandprovider.ui.login

import android.app.Application
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.jeremykruid.lawndemandprovider.R
import com.jeremykruid.lawndemandprovider.databinding.FragmentProfileBinding
import com.jeremykruid.lawndemandprovider.model.ProviderObject
import com.jeremykruid.lawndemandprovider.viewModel.ProfileViewModel

class ProfileFragment : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var viewModel: ProfileViewModel
    private lateinit var auth: FirebaseAuth

    private val uriObserver = Observer<Uri> { uri ->
        loadPhoto(uri)
    }

    private val providerObserver = Observer<ProviderObject?> { provider ->
        if (provider != null) {
            Glide.with(requireActivity()).load(provider.imgUrl).into(binding.profileImg)
        }
        if (provider?.name != null){
            binding.profileNameEt.setText(provider.name)
        }
    }
    
    private val loadingObserver = Observer<Boolean> { loading ->
        if (loading){
            binding.profileProgress.visibility = View.VISIBLE
        }else{
            binding.profileProgress.visibility = View.GONE
        }
    }
    
    private val loadingErrorObserver = Observer<Boolean> { error ->
        if (error){
            Toast.makeText(requireContext(), "An Error Has Occurred", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ProfileViewModel(Application(), requireActivity().activityResultRegistry, viewLifecycleOwner)
        viewModel.provider.observe(viewLifecycleOwner, providerObserver)
        viewModel.loading.observe(viewLifecycleOwner, loadingObserver)
        viewModel.loadingError.observe(viewLifecycleOwner, loadingErrorObserver)
        viewModel.uri.observe(viewLifecycleOwner, uriObserver)

        binding = FragmentProfileBinding.inflate(layoutInflater)
        binding.profileImg.setOnClickListener(this)
        binding.profileDoneBtn.setOnClickListener(this)

        auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null){
            Navigation.findNavController(binding.root).navigate(R.id.action_profileFragment_to_mapsFragment)
        }

        viewModel.getFirebaseProvider(requireContext(), auth.uid.toString())

        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onClick(v: View?) {
        when (v){
            binding.profileImg -> {
                getImage()
            }
            binding.profileDoneBtn -> {
                val provider = ProviderObject(auth.uid, binding.profileNameEt.text.toString(),
                    null, null, null, false, null, false, null)
                viewModel.updateProvider(auth.uid.toString(), provider)
                findNavController().navigate(R.id.action_profileFragment_to_mapsFragment)
            }
        }
    }

    private fun getImage() {
        viewModel.selectImage(viewLifecycleOwner, requireActivity().activityResultRegistry)

    }

    private fun loadPhoto(uri: Uri) {
        Glide.with(this).load(uri).into(binding.profileImg)
        viewModel.updateProviderImage(requireContext(),uri, auth.uid.toString())
    }
}