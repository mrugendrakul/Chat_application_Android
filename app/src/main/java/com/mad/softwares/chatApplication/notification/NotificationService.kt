package com.mad.softwares.chatApplication.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.mad.softwares.chatApplication.R
import com.mad.softwares.chatApplication.MainActivity
import com.mad.softwares.chatApplication.MyApplication
import com.mad.softwares.chatApplication.data.AppContainer
import com.mad.softwares.chatApplication.data.DataRepository
import com.mad.softwares.chatApplication.data.NetworkDataRepository
import com.mad.softwares.chatApplication.data.onDevice.chatASEKeys.LocalAESkeys
import com.mad.softwares.chatApplication.encryption.EncryptionImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch


//import com.mad.softwares.ChatingNew.R



class NotificationService(): FirebaseMessagingService() {

    private lateinit var notificationAddon : NotificationAddon

    override fun onCreate() {
        super.onCreate()
        notificationAddon = (application as MyApplication).container.notificationAddon
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
//        val TAG = "firebaseMsgService"
        Log.d(TAGNotf, "From: ${message.from}")

        if (message.data.isNotEmpty()) {
            Log.d(TAGNotf, "Message data payload: ${message.data.toList()}")
        }
//        val dataRepository: DataRepository
//         val encryptionService: EncryptionImpl
//        CoroutineScope(Dispatchers.IO).launch{
//            val secureAES = dataRepository.getAESKeyForChatID(message.data.get("chatId") ?: "")
//            var decryptedBody = ""
//                try{
//                decryptedBody = String(encryptionService.aesDecrypt(
//                    encryptedData = encryptionService.stringToByteArray(
//                        message.data.get("body") ?: "no body"
//                    ),
//                    secretKey = encryptionService.stringToAESKey(secureAES)
//                ))
//            }catch (e: Exception){
//                Log.e(TAGNotf,"Unable to decrypt the messages")
//                    decryptedBody = message.data.get("body") ?: "no body"
//            }
        message.data.let {
            val title = it.get("title")
            val body = it.get("body")
            val chatId = it.get("chatId")
            CoroutineScope(Dispatchers.IO).launch{
                val decryptedBody = coroutineScope{
                    val insideData = async(){
                        notificationAddon.decryptBody(
                            encryptedBody = body.toString(),
                            chatId = chatId.toString()
                        )
                    }
                    insideData.await()
                }

                Log.d(TAGNotf, "Notification Title: $title")
            Log.d(TAGNotf, "Notification Body: ${decryptedBody}")
                Log.d(TAGNotf, "Notification ChatId : $chatId")

                // Now, you can display the notification details as needed (e.g., in a notification UI)
                showNotification(title, decryptedBody, message.data.toList())
                notificationReceived()
            }
        }
    }



    fun notificationReceived(): Boolean {
        return true
    }

    private fun showNotification(
        title: String?,
        body: String?,
        messages: List<Pair<String, String>>
    ) {
        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "Chats"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(channelId, "Chats", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.ic_stat_name)
            .setContentIntent(pendingIntent)
//            .setStyle(
//                NotificationCompat.BigTextStyle()
//                .bigText(
//                    messages.joinToString(separator = "\n") { message -> "${message.first} : ${message.second} " }
//                ))
            .setAutoCancel(true)
            .build()

        notificationManager.notify(0, notification)
    }

    override fun onNewToken(token: String) {
        Log.d(TAGNotf, "Refreshed token: $token")
//        val FirebaseApi: FirebaseApi = NetworkFirebaseApi(
//            Firebase.firestore,
//            Firebase.firestore.collection("Users"),
//            Firebase.firestore.collection("Chats"),
//            Firebase.firestore.collection("Messages"),
//            EncryptionImpl
//        )


    }

    companion object {
            private const val TAGNotf = "MyFirebaseMessagingService"
    }

}