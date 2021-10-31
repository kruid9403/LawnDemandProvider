package com.jeremykruid.lawndemandprovider.services.firebase

import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
import com.jeremykruid.lawndemandprovider.R

class FirebaseAuthService {
    private val auth = FirebaseAuth.getInstance()

    //TODO: create check auth function here

    fun loginProvider(context: Context, email: String, password: String, v: View){
        // login provider
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
            Navigation.findNavController(v).navigate(R.id.action_login_to_mapsFragment)
            }
            .addOnFailureListener {
                Toast.makeText(context, it.localizedMessage, Toast.LENGTH_SHORT).show()
            }

    }

    // register provider
    fun registerProvider(context: Context, email: String, password: String, v: View){
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
            loginProvider(context, email, password, v)
            }
            .addOnFailureListener {
                Toast.makeText(context, it.localizedMessage, Toast.LENGTH_SHORT).show()
            }
    }

    //send forgot password email
    fun forgotPassword(context: Context, email: String){
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
            Toast.makeText(context, "Email Sent", Toast.LENGTH_SHORT).show() }
            .addOnFailureListener {
                Toast.makeText(context, it.localizedMessage, Toast.LENGTH_SHORT).show() }
    }
}