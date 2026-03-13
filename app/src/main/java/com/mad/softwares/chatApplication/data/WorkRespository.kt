package com.mad.softwares.chatApplication.data

import androidx.work.WorkInfo
import kotlinx.coroutines.flow.Flow

interface WorkRespository {
    val TagsInfo: Flow<WorkInfo>
    val messageInfo:Flow<WorkInfo>
    fun getAiTags()
    fun sendMessage()
}