package com.mad.softwares.chatApplication.data

data class chatUser(
    val username:String="",
    val fcmToken:List<String> = listOf(),
    val profilePic:String=""
)
