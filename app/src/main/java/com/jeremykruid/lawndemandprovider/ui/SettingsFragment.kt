package com.jeremykruid.lawndemandprovider.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.jeremykruid.lawndemandprovider.R
import com.jeremykruid.lawndemandprovider.databinding.FragmentSettingsBinding
import com.jeremykruid.lawndemandprovider.viewModel.SettingsViewModel


class SettingsFragment : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentSettingsBinding
    private lateinit var viewModel: SettingsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingsBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this).get(SettingsViewModel::class.java)


        viewModel.accountLink.observe(viewLifecycleOwner, {
            findNavController().navigate(R.id.action_settingsFragment_to_stripeOnboardingFragment, bundleOf("url" to it))
        })

        setClickListeners()

        return binding.root
    }

    private fun setClickListeners() {
        binding.settingsConnectStripe.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        if (v != null) {
            when(v.id){
                R.id.settings_connect_stripe -> {
                    viewModel.connectStripe()
                }
            }
        }
    }
}