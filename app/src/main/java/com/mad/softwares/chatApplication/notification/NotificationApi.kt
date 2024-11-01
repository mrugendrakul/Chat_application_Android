package com.mad.softwares.chatApplication.notification

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

data class NotificationRequest(
    val fcmToken: String,
    val title: String,
    val body: String,
    val chatId: String
)

interface NotificationApi {
    @POST("sendnotfi")
    fun sendNotificationToDevice(@Body request: NotificationRequest):Call<Void>
}

val retrofit = Retrofit.Builder()
//    .baseUrl("http://10.0.2.2:10000/")
    .baseUrl("https://chat-application-notification-backend.onrender.com/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

val notificationApiSending = retrofit.create(NotificationApi::class.java)
