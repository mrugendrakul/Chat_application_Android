package com.mad.softwares.chatApplication.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.google.gson.Gson
import com.mad.softwares.chatApplication.R
import com.mad.softwares.chatApplication.network.AiApiLocalhost
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class getApiTag(ctx: Context, params: WorkerParameters): CoroutineWorker(ctx,params) {
    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO){
            return@withContext try{
                val modelTags = AiApiLocalhost().aiTakling.getTags()
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