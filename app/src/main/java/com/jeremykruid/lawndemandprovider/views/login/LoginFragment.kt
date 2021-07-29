package com.jeremykruid.lawndemandprovider.views.login

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import com.jeremykruid.lawndemandprovider.R
import com.jeremykruid.lawndemandprovider.databinding.FragmentLoginBinding
import com.jeremykruid.lawndemandprovider.model.FirebaseAuthService
import com.jeremykruid.lawndemandprovider.viewModel.LoginViewModel
import com.jeremykruid.lawndemandprovider.viewModel.MapViewModel

class LoginFragment : Fragment(), View.OnClickListener {

    private lateinit var viewModel: LoginViewModel
    private lateinit var binding: FragmentLoginBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentLoginBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setClickListeners()

        viewModel = ViewModelProviders.of(this).get(LoginViewModel::class.java)
    }

    private fun setClickListeners() {
        binding.loginLoginButton.setOnClickListener(this)
        binding.loginRegisterButton.setOnClickListener(this)
        binding.loginForgotPassword.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v){
            binding.loginLoginButton -> {
                viewModel.loginProvider(requireContext(), binding.loginEmail.text.toString(),
                binding.loginPassword.text.toString(), binding.loginLoginButton)
            }
            binding.loginRegisterButton -> {
                viewModel.registerProvider(requireContext(), binding.loginEmail.text.toString(),
                binding.loginPassword.text.toString(), binding.loginRegisterButton)
            }
            binding.loginForgotPassword -> {
                Toast.makeText(requireContext(), "Forgot", Toast.LENGTH_SHORT).show()
            }
        }
    }
}