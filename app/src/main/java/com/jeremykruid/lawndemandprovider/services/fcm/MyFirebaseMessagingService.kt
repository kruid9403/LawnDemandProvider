package com.jeremykruid.lawndemandprovider.services.fcm

import android.app.Notification.VISIBILITY_PUBLIC
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.jeremykruid.lawndemandprovider.R
import com.jeremykruid.lawndemandprovider.managers.OrderManager
import com.jeremykruid.lawndemandprovider.model.*
import com.jeremykruid.lawndemandprovider.ui.MainActivity
import com.jeremykruid.lawndemandprovider.ui.helper.AcceptJobActivity
import com.jeremykruid.lawndemandprovider.ui.helper.DeclineJobActivity
import com.jeremykruid.lawndemandprovider.ui.helper.DeclineJobLogOutActivity
import com.jeremykruid.lawndemandprovider.utilities.Constants.MessagingConst.Companion.ACCEPT_JOB
import com.jeremykruid.lawndemandprovider.utilities.Constants.MessagingConst.Companion.CHANNEL_ID
import com.jeremykruid.lawndemandprovider.utilities.Constants.MessagingConst.Companion.DECLINE_JOB
import com.jeremykruid.lawndemandprovider.utilities.Constants.MessagingConst.Companion.DECLINE_LOG_OUT
import com.jeremykruid.lawndemandprovider.utilities.Constants.MessagingConst.Companion.MESSAGE_KEY
import com.jeremykruid.lawndemandprovider.utilities.Constants.MessagingConst.Companion.NOTIFICATION_INT
import com.jeremykruid.lawndemandprovider.utilities.Constants.MessagingConst.Companion.PENDING_ORDER_NOTIFICATION
import com.squareup.moshi.Json
import kotlinx.coroutines.CoroutineScope
import org.koin.android.ext.android.inject

class MyFirebaseMessagingService(): FirebaseMessagingService() {

    private val orderDao: OrderDao by inject()
    private val providerDao: ProviderDao by inject()

    private var functions = FirebaseFunctions.getInstance()


    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        val data = hashMapOf(
            "updateToken" to p0
        )
        functions.getHttpsCallable("updateToken").call(data).addOnCompleteListener {
            Log.e("FirebaseMessaging", p0)
        }
    }

    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)

        when(p0.data["title"].toString()){
            "providerObject" -> {
                val gson = Gson()
                val provider = gson.fromJson(p0.data["body"].toString(), Provider::class.java)
                providerDao.insert(provider)
                Log.e("FCMService", provider.toString())
            }
            "pendingOrder" -> {
                val gson = Gson()
                val pendingOrder = gson.fromJson(p0.data["body"].toString(), OrderObject::class.java)
                Log.e("FCMService", pendingOrder.toString())

                orderDao.insert(pendingOrder)

                val intent = Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    putExtra(ACCEPT_JOB, pendingOrder.orderId)
                    putExtra("status", "accepted")
                }
                val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

                val acceptIntent = Intent(this, AcceptJobActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    putExtra(ACCEPT_JOB, pendingOrder.orderId)
                    putExtra("status", "accepted")
                }
                val acceptPend: PendingIntent = PendingIntent.getActivity(this, 0, acceptIntent, 0)

                val declineIntent = Intent(this, DeclineJobActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    putExtra(DECLINE_JOB, pendingOrder.orderId)
                    putExtra("status", "declined")
                }
                val declinePend: PendingIntent = PendingIntent.getActivity(this, 0, declineIntent, 0)


                val logOutIntent = Intent(this, DeclineJobLogOutActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    putExtra(DECLINE_LOG_OUT, pendingOrder.orderId)
                }
                val logOutPend: PendingIntent = PendingIntent.getActivity(this, 0, logOutIntent, 0)


                var builder = NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle("New Lawn Available")
                    .setContentText("Accept Job for ${pendingOrder.price}")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setAutoCancel(true)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .addAction(R.drawable.ic_launcher_foreground, "Accept", acceptPend)
                    .addAction(R.drawable.ic_launcher_foreground, "Decline", declinePend)
                    .addAction(R.drawable.ic_launcher_foreground, "Decline/Quit", logOutPend)


                with(NotificationManagerCompat.from(this)) {
                    // notificationId is a unique int for each notification that you must define
                    notify(0, builder.build())
                }
            }
            "acceptedOrder" -> {
                val gson = Gson()
                val acceptedOrder = gson.fromJson(p0.data["body"].toString(), OrderObject::class.java)
                orderDao.insert(acceptedOrder)
                Log.e("FCMService", "accepted order")
            }
        }
    }
}

