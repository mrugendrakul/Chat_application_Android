package com.mad.softwares.chatApplication.data

import com.google.firebase.Timestamp

data class ChatOrGroup(
    val chatId:String = "",
    val chatName:String = "",
    val isGroup:Boolean = false,
    val members:List<String> = listOf(),
    val chatPic:String = "",
        val membersData:MutableList<chatUser> = mutableListOf(),
    val lastMessage:lastMessage = lastMessage(timestamp = Timestamp(0,0)),
    val encryptedAESKeys:Map<String,String> = mapOf()
)
