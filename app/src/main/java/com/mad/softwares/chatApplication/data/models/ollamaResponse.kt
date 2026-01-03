package com.mad.softwares.chatApplication.data.models

import kotlinx.serialization.Serializable

@Serializable
data class ollamaResponse(
    val model:String,
    val messages: List<messages>,
    val stream: Boolean
)

@Serializable
data class messages(
    val role:String,
    val content:String,
)
