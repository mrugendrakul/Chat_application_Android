package com.mad.softwares.chatApplication.data

import android.content.Context
import androidx.lifecycle.asFlow
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.mad.softwares.chatApplication.workers.getApiTag
import com.mad.softwares.chatApplication.workers.sendAiMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull

val AI_RESPONSE_TAG = "AI_RESPONSE_TAG"
val AI_RESPONSE_NAME = "AI_RESPONSE_NAME"

val AI_MESSAGE = "AI_MESSAGE"

class AiWorkManagerRespository(context: Context): WorkRespository {
    private val workManager = WorkManager.getInstance(context)

    override val TagsInfo: Flow<WorkInfo> = workManager.getWorkInfosByTagLiveData(AI_RESPONSE_TAG).asFlow().mapNotNull {
        if(it.isNotEmpty()) it.first() else null
    }

    override val messageInfo: Flow<WorkInfo> = workManager.getWorkInfosByTagLiveData(AI_MESSAGE).asFlow().mapNotNull {
        if(it.isNotEmpty()) it.first() else null
    }
    override fun getAiTags() {
        val initialApiCall = OneTimeWorkRequestBuilder<getApiTag>()
            .addTag(AI_RESPONSE_TAG)
            .build()

        val starting = workManager.beginUniqueWork(
            AI_RESPONSE_NAME,
            ExistingWorkPolicy.REPLACE,
            initialApiCall
        )
        starting.enqueue()
    }

    override fun sendMessage(message: String,model: String) {
        val initialCall = OneTimeWorkRequestBuilder<sendAiMessage>()
            .setInputData(setInputDataForAiMessage(message,model))
            .addTag(AI_MESSAGE)
            .build()

        val starting = workManager.beginUniqueWork(
            AI_MESSAGE,
            ExistingWorkPolicy.REPLACE,
            initialCall
        )
        starting.enqueue()
    }

    private fun setInputDataForAiMessage (message:String,model: String): Data{
        val dataBuilder = Data.Builder()
        dataBuilder.put("AI_MESSAGE_USER",message)
        dataBuilder.put("AI_MODEL",model)
        return dataBuilder.build()
    }
}