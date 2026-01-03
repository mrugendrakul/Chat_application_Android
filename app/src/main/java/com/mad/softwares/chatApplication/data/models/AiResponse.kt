package com.mad.softwares.chatApplication.data.models

import kotlinx.serialization.Serializable

@Serializable
data class AiResponse(
    val message: messages,
    val done: Boolean,
)
