package com.mad.softwares.chatApplication.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.mad.softwares.chatApplication.BuildConfig
import com.mad.softwares.chatApplication.data.models.AiResponse
import com.mad.softwares.chatApplication.data.models.ollamaResponse
import com.mad.softwares.chatApplication.data.models.tags
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Streaming
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


interface AiApiLocalhostService {
    @GET("/api/tags")
    suspend fun getTags(): tags
//@Headers("Read-Timeout: 120")
    @POST("api/chat")
    suspend fun sendMessage(@Body request: ollamaResponse): AiResponse

    @Streaming
    @POST("api/chat")
    suspend fun sendMessageStream(@Body request: ollamaResponse): ResponseBody
}
interface AiApiLocalhostInterface{

    val aiTakling: AiApiLocalhostService
}

class AiApiLocalhost(
    private val aiEndPoint:String
): AiApiLocalhostInterface {
    companion object{
        private var cachedUrl:String = if (BuildConfig.DEBUG){
            "http://10.0.2.2:11434"
        } else {
            "http://127.0.0.1:11434"
        }
        private var cachedRetrofitService: AiApiLocalhostService? = null
    }
    override val aiTakling: AiApiLocalhostService by lazy {
        if(cachedUrl !=aiEndPoint || cachedRetrofitService == null){
            val retroFitObject =
                Retrofit.Builder()
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl("http://$aiEndPoint")
                    .build()
            cachedRetrofitService = retroFitObject.create(AiApiLocalhostService::class.java)
            cachedUrl = aiEndPoint
        }

        return@lazy cachedRetrofitService!!
    }
}