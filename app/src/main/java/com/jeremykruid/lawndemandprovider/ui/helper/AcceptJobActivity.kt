package com.jeremykruid.lawndemandprovider.ui.helper

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.jeremykruid.lawndemandprovider.ui.MainActivity
import com.jeremykruid.lawndemandprovider.utilities.Constants

class AcceptJobActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val orderId = intent.getStringExtra(Constants.MessagingConst.ACCEPT_JOB)
        val i = Intent(this, MainActivity::class.java)
        i.putExtra(Constants.MessagingConst.ACCEPT_JOB, orderId)
        startActivity(i)
        finish()

    }
}