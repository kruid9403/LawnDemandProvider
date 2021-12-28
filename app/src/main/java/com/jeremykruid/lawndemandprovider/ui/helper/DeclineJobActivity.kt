package com.jeremykruid.lawndemandprovider.ui.helper

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import com.jeremykruid.lawndemandprovider.ui.MainActivity
import com.jeremykruid.lawndemandprovider.utilities.Constants.MessagingConst.Companion.ACCEPT_JOB
import com.jeremykruid.lawndemandprovider.utilities.Constants.MessagingConst.Companion.DECLINE_JOB
import com.jeremykruid.lawndemandprovider.utilities.Constants.MessagingConst.Companion.DECLINE_LOG_OUT

class DeclineJobActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val orderId = intent.getStringExtra(DECLINE_JOB)
        val i = Intent(this, MainActivity::class.java)
        i.putExtra(DECLINE_JOB, orderId)
        startActivity(i)
        finish()

    }
}