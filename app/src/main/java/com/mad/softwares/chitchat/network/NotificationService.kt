package com.mad.softwares.chitchat.network

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.mad.softwares.chitchat.MainActivity
import com.mad.softwares.chitchat.R

//import com.mad.softwares.ChatingNew.R



class NotificationService(

): FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
//        val TAG = "firebaseMsgService"
        Log.d(TAGNotf, "From: ${message.from}")

        if (message.data.isNotEmpty()) {
            Log.d(TAGNotf, "Message data payload: ${message.data.toList()}")
        }
        message.notification?.let {
            val title = it.title
            val body = it.body

            // Handle notification payload
            Log.d(TAGNotf, "Notification Title: $title")
            Log.d(TAGNotf, "Notification Body: $body")

            // Now, you can display the notification details as needed (e.g., in a notification UI)
            showNotification(title,body,message.data.toList())
            notificationReceived()
        }

    }
    fun notificationReceived():Boolean{
        return true
    }

    private fun showNotification(title: String?, body: String?,messages:List<Pair<String,String>>) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "Chats"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Chats", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.ic_stat_name)
            .setContentIntent(pendingIntent)
            .setStyle(
                NotificationCompat.BigTextStyle()
                .bigText(
                    messages.joinToString(separator = "\n") { message -> "${message.first} : ${message.second} " }
                ))
            .setAutoCancel(true)
            .build()

        notificationManager.notify(0, notification)
    }

    override fun onNewToken(token: String) {
        Log.d(TAGNotf, "Refreshed token: $token")

    }

    companion object {
        private const val TAGNotf = "MyFirebaseMessagingService"
    }

}