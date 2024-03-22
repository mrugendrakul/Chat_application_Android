package com.mad.softwares.chitchat.data

data class Chats(
    val chatId:String = "",
    val chatName:String = "",
    val isGroup:Boolean = false,
    val members:List<String> = listOf()
)
