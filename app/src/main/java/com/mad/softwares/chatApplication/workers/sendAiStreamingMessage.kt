package com.mad.softwares.chatApplication.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mad.softwares.chatApplication.data.models.AiResponse
import com.mad.softwares.chatApplication.data.models.messages
import com.mad.softwares.chatApplication.data.models.ollamaResponse
import com.mad.softwares.chatApplication.dataStore
import com.mad.softwares.chatApplication.network.AiApiLocalhost
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class sendAiStreamingMessage(ctx: Context, params: WorkerParameters): CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                val userMessage = inputData.getString("AI_MESSAGE_USER") ?: ""
                val model = inputData.getString("AI_MODEL") ?: ""
                val chatContext = inputData.getString("AI_CONTEXT")
//                val isStream = inputData.getBoolean("AI_STREAM", false) // Pass this flag from UI
                val parsedObjectContext:List<messages> = if (!chatContext.isNullOrEmpty()) {
                    val type = object : TypeToken<List<messages>>() {}.type
                    Gson().fromJson(chatContext, type)
                } else {
                    emptyList()
                }
                Log.d("Workers", "Got input:- $userMessage")

                val preferences = applicationContext.dataStore.data.first()
                val currentUrl = preferences[API_URL_ENDPOINT] ?: ""
                val api = AiApiLocalhost(currentUrl).aiTakling // Assuming this returns the Retrofit interface

                val requestBody = ollamaResponse(
                    model = model,
                    messages = parsedObjectContext + messages("user",userMessage),
                    stream = true
                )

                val gson = Gson()

//                if (isStream) {
                    // --- STREAMING LOGIC ---
                    val responseBody = api.sendMessageStream(request = requestBody)
                    var fullResponseContent = ""

                    // Read the stream line by line
                    responseBody
                        .byteStream()
                        .bufferedReader().useLines { lines ->
                        lines.forEach { line ->
                            if (line.isNotBlank()) {
                                // Ollama streams JSON objects line by line.
                                // You may need to replace 'AiResponseChat' with your specific chunk data class
                                val chunk = gson.fromJson(line, AiResponse::class.java)
                                val contentChunk = chunk.message?.content ?: ""

                                fullResponseContent += contentChunk
                                Log.d("Workers","Chuck message - $fullResponseContent")
                                // Emit progress to the UI!
                                setProgress(workDataOf(
                                    "AI_STREAM_CHUNK" to contentChunk,
                                    "AI_STREAM_FULL" to fullResponseContent
                                ))
                            }
                        }
                    }

                    Log.d("Workers", "Finished streaming.")
                    val completeResponseString = gson.toJson(AiResponse(
                        message = messages("assistant",fullResponseContent),
                        done = true
                    ))
                    // Return the final combined string when done
                    val outputData = workDataOf("AI_EXPENSIVE_RESPONSE" to completeResponseString)
                    return@withContext Result.success(outputData)

//                } else {
//                    // --- ORIGINAL NON-STREAMING LOGIC ---
//                    val aiResponseChat = api.sendMessage(request = requestBody)
//                    Log.d("Workers", "Ai response we got $aiResponseChat")
//
//                    val aiExpensiveResponse = gson.toJson(aiResponseChat)
//                    val outputData = workDataOf("AI_EXPENSIVE_RESPONSE" to aiExpensiveResponse)
//                    return@withContext Result.success(outputData)
//                }

            } catch (e: Throwable) {
                Log.e("Workers", "Error sending message to ai: ${e.message}", e)
                return@withContext Result.failure()
            }
        }
    }
}