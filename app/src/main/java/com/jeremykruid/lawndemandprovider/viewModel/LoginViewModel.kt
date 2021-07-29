package com.jeremykruid.lawndemandprovider.viewModel

import android.app.Application
import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.auth.FirebaseAuth
import com.jeremykruid.lawndemandprovider.model.FirebaseAuthService

class LoginViewModel(application: Application): AndroidViewModel(application) {

    private val loginService = FirebaseAuthService()

    //Login provider
    fun loginProvider(context: Context, email: String, password: String, v: View){
        if (email != "" && password != ""){
            loginService.loginProvider(context, email, password, v)
        }else {
            Toast.makeText(context, "Email and Password are Required", Toast.LENGTH_SHORT).show()
        }
    }

    //Register provider
    fun registerProvider(context: Context, email: String, password: String, v: View) {
        if (email != "" && password != "") {
            loginService.registerProvider(context, email, password, v)
        }else{
            Toast.makeText(context, "Email and Password are Required", Toast.LENGTH_SHORT).show()
        }
    }
    
    fun sendForgotPassword(context: Context, email: String){
        if (email != ""){
            loginService.forgotPassword(context, email)
        }else{
            Toast.makeText(context, "Enter Your Email", Toast.LENGTH_SHORT).show()
        }
    }
}