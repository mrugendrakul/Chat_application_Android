package com.mad.softwares.chatApplication.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.mad.softwares.chatApplication.BuildConfig
import com.mad.softwares.chatApplication.data.models.AiResponse
import com.mad.softwares.chatApplication.data.models.ollamaResponse
import com.mad.softwares.chatApplication.data.models.tags
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import java.util.concurrent.TimeUnit


val baseUrl =
    if (BuildConfig.DEBUG){
        "http://10.0.2.2:11434"
    } else {
        "http://127.0.0.1:11434"
    }
val okHttpClient=  OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(2, TimeUnit.MINUTES)
    .writeTimeout(30, TimeUnit.SECONDS)
    .build()
val retroFitObject =
    Retrofit.Builder()
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(baseUrl)
        .build()

interface AiApiLocalhostService {
    @GET("/api/tags")
    suspend fun getTags(): tags
//@Headers("Read-Timeout: 120")
    @POST("api/chat")
    suspend fun sendMessage(@Body request: ollamaResponse): AiResponse
}
interface AiApiLocalhostInterface{

    val aiTakling: AiApiLocalhostService
}

class AiApiLocalhost: AiApiLocalhostInterface {
    override val aiTakling: AiApiLocalhostService by lazy {
        retroFitObject.create(AiApiLocalhostService::class.java)
    }
}