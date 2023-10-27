package com.android.doctorapp.ui.notification

import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.android.doctorapp.repository.local.IS_NEW_USER_TOKEN
import com.android.doctorapp.repository.local.PreferenceDataStore
import com.android.doctorapp.repository.local.USER_TOKEN
import com.android.doctorapp.util.constants.ConstantKey.CHANNEL_ID
import com.android.doctorapp.util.constants.ConstantKey.MY_CHANNEL
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class PushNotificationService() : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        storePushToken(token)
    }

    private fun storePushToken(token: String) {
        val data = PreferenceDataStore(applicationContext)
        CoroutineScope(Dispatchers.Main).launch {
            val old = data.getString(USER_TOKEN)
            if (!old.equals(token)) {
                data.putString(USER_TOKEN, token)
                data.putBoolean(IS_NEW_USER_TOKEN, true)
            } else
                data.putBoolean(IS_NEW_USER_TOKEN, false)

        }

    }

    override fun onMessageReceived(message: RemoteMessage) {
        if (message.data["title"]?.isNotEmpty() == true && message.data["body"]?.isNotEmpty() == true) {
            sendNotification(message.data["title"]!!, message.data["body"]!!)
        }
    }

    private fun sendNotification(title: String, messageBody: String) {

        // Get the system's notification service
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create the notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(CHANNEL_ID, MY_CHANNEL, NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        // Create a notification builder
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_menu_camera) // Set your notification icon
            .setContentTitle(title) // Set the title of the notification
            .setContentText(messageBody) // Set the message of the notification
            .setAutoCancel(true) // Close the notification when tapped
            .build()


        // Build and display the notification
        notificationManager.notify(1, notificationBuilder)
    }
}