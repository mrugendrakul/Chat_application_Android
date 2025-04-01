package com.mad.softwares.chatApplication.data

import com.google.firebase.Timestamp
import org.checkerframework.checker.signature.qual.IdentifierOrPrimitiveType

enum class ContentType(){
    text,
    image,
    document,
    audio,
    video,
    code
}

enum class messageStatus(){
    Sending,
    Send,
    Delivered,
    Read,
    Error
}

data class MessageReceived(
    val messageId:String = "",
    val content:String = "",
    val contentType:ContentType = ContentType.text,
    val senderId:String = "",
    val timeStamp:Timestamp = Timestamp.now(),
    val status:messageStatus = messageStatus.Sending,
)
