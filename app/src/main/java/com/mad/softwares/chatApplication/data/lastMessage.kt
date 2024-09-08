package com.mad.softwares.chatApplication.data

import com.google.firebase.Timestamp

data class lastMessage(
    val sender:String="",
    val content:String = "",
    val timestamp: Timestamp
)
