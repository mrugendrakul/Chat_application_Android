package com.mad.softwares.chatApplication.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.google.gson.Gson
import com.mad.softwares.chatApplication.data.models.messages
import com.mad.softwares.chatApplication.data.models.ollamaResponse
import com.mad.softwares.chatApplication.network.AiApiLocalhost
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class sendAiMessage(ctx: Context, params: WorkerParameters): CoroutineWorker(ctx,params) {
    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO){
            return@withContext try {
                val userMessage = inputData.getString("AI_MESSAGE_USER")
                val model = inputData.getString("AI_MODEL")
                val aiResponseChat = AiApiLocalhost().aiTakling.sendMessage(request = ollamaResponse(
                    model = model?:"",
                    messages = listOf(messages(role = "user", content = userMessage?:"")),
                    stream = false
                ))
                Log.d("Workers","Ai response we got $aiResponseChat")
                val AiExpensiveResponse = Gson().toJson(aiResponseChat)
                val outputData = workDataOf("AI_EXPENSIVE_RESPONSE" to AiExpensiveResponse)
                Result.success(outputData)
            }
            catch (e: Throwable){
                Log.e("Workers","Error sending message to ai- ${e}")
                Result.failure()
            }
        }
    }
}