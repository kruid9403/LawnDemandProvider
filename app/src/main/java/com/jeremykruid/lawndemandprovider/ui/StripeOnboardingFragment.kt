package com.jeremykruid.lawndemandprovider.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.lifecycle.ViewModelProvider
import com.jeremykruid.lawndemandprovider.R
import com.jeremykruid.lawndemandprovider.databinding.FragmentStripeOnboardingBinding
import com.jeremykruid.lawndemandprovider.viewModel.StripeOnboardingViewModel

class StripeOnboardingFragment : Fragment() {

    private lateinit var binding: FragmentStripeOnboardingBinding
    private lateinit var viewModel: StripeOnboardingViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStripeOnboardingBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this).get(StripeOnboardingViewModel::class.java)

        val url: String = arguments?.getString("url")!!

        binding.stripeOnboardingWebview.webViewClient = WebViewClient()
        binding.stripeOnboardingWebview.loadUrl(url)
        binding.stripeOnboardingWebview.settings.javaScriptEnabled = true


        // Inflate the layout for this fragment
        return binding.root
    }
}