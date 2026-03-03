package com.mad.softwares.chatApplication.workers

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.google.gson.Gson
import com.mad.softwares.chatApplication.AI_PREFERENCE_NAME
import com.mad.softwares.chatApplication.R
import com.mad.softwares.chatApplication.dataStore
import com.mad.softwares.chatApplication.network.AiApiLocalhost
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

//val Context.workerDataStore by preferencesDataStore(
//    name = AI_PREFERENCE_NAME
//)
val API_URL_ENDPOINT = stringPreferencesKey("api_url_endpoint")
class getApiTag(ctx: Context, params: WorkerParameters): CoroutineWorker(ctx,params) {
    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO){
            return@withContext try{
                val preferences = applicationContext.dataStore.data.first()
                val currentUrl = preferences[API_URL_ENDPOINT]
                Log.d("Workers","Getting the endpoint $currentUrl")
                val modelTags = AiApiLocalhost(aiEndPoint = currentUrl?:"").aiTakling.getTags()
                Log.d("Workers",modelTags.toString())
                val outputString = Gson().toJson(modelTags)
                val outputData = workDataOf("AI_TAGS_RESPONSE" to outputString)
                Result.success(outputData)
            }
            catch (e: Throwable){
                Log.e("Workers","Error in worker $e")
                Result.failure()
            }
        }
    }
}