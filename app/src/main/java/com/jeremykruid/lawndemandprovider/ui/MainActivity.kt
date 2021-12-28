package com.jeremykruid.lawndemandprovider.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.jeremykruid.lawndemandprovider.R
import com.jeremykruid.lawndemandprovider.utilities.Constants.MessagingConst.Companion.ACCEPT_JOB
import com.jeremykruid.lawndemandprovider.utilities.Constants.MessagingConst.Companion.CHANNEL_ID
import com.jeremykruid.lawndemandprovider.utilities.Constants.MessagingConst.Companion.DECLINE_JOB
import com.jeremykruid.lawndemandprovider.utilities.Constants.MessagingConst.Companion.DECLINE_LOG_OUT

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var notificationManagerCompat: NotificationManagerCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        createNotificationChannel()

        navController = Navigation.findNavController(this, R.id.nav_host_fragment)

        if (!intent.getStringExtra(ACCEPT_JOB).equals(null)){
            clearNotification()
            navController.navigate(R.id.mapsFragment, bundleOf(ACCEPT_JOB to intent.getStringExtra(
                ACCEPT_JOB)))
        }

        if (!intent.getStringExtra(DECLINE_JOB).equals(null)){
            clearNotification()
            navController.navigate(R.id.mapsFragment, bundleOf(DECLINE_JOB to intent.getStringExtra(
                DECLINE_JOB)))
        }

        if (!intent.getStringExtra(DECLINE_LOG_OUT).equals(null)){
            clearNotification()
            navController.navigate(R.id.mapsFragment, bundleOf(DECLINE_LOG_OUT to intent.getStringExtra(
                DECLINE_LOG_OUT)))
        }
    }

    private fun clearNotification() {
        notificationManagerCompat = NotificationManagerCompat.from(this)
        notificationManagerCompat.cancel(0)
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

}