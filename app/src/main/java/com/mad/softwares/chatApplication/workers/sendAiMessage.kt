package com.mad.softwares.chatApplication.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mad.softwares.chatApplication.data.models.messages
import com.mad.softwares.chatApplication.data.models.ollamaResponse
import com.mad.softwares.chatApplication.network.AiApiLocalhost
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class sendAiMessage(ctx: Context, params: WorkerParameters): CoroutineWorker(ctx,params) {
    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO){
            return@withContext try {
                val aiResponseChat = AiApiLocalhost().aiTakling.sendMessage(request = ollamaResponse(
                    model = "qwen2.5-coder:3b",
                    messages = listOf(messages(role = "user", content = "5 good works for your thought")),
                    stream = false
                ))
                Log.d("Workers","Ai response we got $aiResponseChat")
                Result.success()
            }
            catch (e: Throwable){
                Log.e("Workers","Error sending message to ai- ${e}")
                Result.failure()
            }
        }
    }
}