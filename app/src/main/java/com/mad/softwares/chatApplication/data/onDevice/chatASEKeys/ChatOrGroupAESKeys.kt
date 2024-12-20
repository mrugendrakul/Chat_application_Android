package com.mad.softwares.chatApplication.data.onDevice.chatASEKeys

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ChatOrGroupAESKeys(
    @PrimaryKey(autoGenerate = false)
    val chatId:Int,
    val chatName:String,
    val decryptedASEKey:String
)
