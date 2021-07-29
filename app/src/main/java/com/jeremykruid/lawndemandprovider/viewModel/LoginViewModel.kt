package com.jeremykruid.lawndemandprovider.viewModel

import android.app.Application
import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.auth.FirebaseAuth
import com.jeremykruid.lawndemandprovider.model.FirebaseAuthService

class LoginViewModel(application: Application): AndroidViewModel(application) {

    private lateinit var auth: FirebaseAuth
    private val loginService = FirebaseAuthService()

    fun loginProvider(context: Context, email: String, password: String, v: View){
        if (email != "" && password != ""){
            loginService.loginProvider(context, email, password, v)
        }else {
            Toast.makeText(context, "Email and Password are Required", Toast.LENGTH_SHORT).show()
        }
    }

    fun registerProvider(context: Context, email: String, password: String, v: View) {
        if (email != "" && password != "") {
            loginService.registerProvider(context, email, password, v)
        }else{
            Toast.makeText(context, "Email and Password are Required", Toast.LENGTH_SHORT).show()
        }
    }
}