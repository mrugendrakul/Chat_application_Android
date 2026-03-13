package com.mad.softwares.chatApplication.data

import androidx.work.WorkInfo
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import kotlin.uuid.Uuid

interface WorkRespository {
    val TagsInfo: Flow<WorkInfo>
    val messageInfo:Flow<WorkInfo>
    fun getAiTags()
    fun sendMessage(message: String,model:String): String

    fun sendStreamMessage(message:String,model: String):String

    fun getMessageInfoById(uuid:String):Flow<WorkInfo?>
}