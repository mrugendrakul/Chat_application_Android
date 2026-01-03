package com.mad.softwares.chatApplication.data.models

import kotlinx.serialization.Serializable

@Serializable
data class tags(
    val models: List<OllamaModel>
)

@Serializable
data class OllamaModel(
    val name: String,
    val model: String,
)
