package com.android.doctorapp.ui.notification

import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModelProvider
import com.android.doctorapp.ui.doctordashboard.doctorfragment.AppointmentDoctorViewModel
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class PushNotificationService() : FirebaseMessagingService() {


    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val pushNotificationViewModel = ViewModelProvider.AndroidViewModelFactory(application)
            .create(AppointmentDoctorViewModel::class.java)
        pushNotificationViewModel.updateUserData(
            token,
            pushNotificationViewModel.resourceProvider,
            pushNotificationViewModel.session,
            pushNotificationViewModel.context,
            pushNotificationViewModel.appointmentRepository
        )
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
                NotificationChannel("channelId", "myChannel", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        // Create a notification builder
        val notificationBuilder = NotificationCompat.Builder(this, "channelId")
            .setSmallIcon(R.drawable.ic_menu_camera) // Set your notification icon
            .setContentTitle(title) // Set the title of the notification
            .setContentText(messageBody) // Set the message of the notification
            .setAutoCancel(true) // Close the notification when tapped
            .build()


        // Build and display the notification
        notificationManager.notify(1, notificationBuilder)
    }
}