package com.mad.softwares.chatApplication.data

import java.security.PrivateKey

data class User(
    val fcmToken:String = "",
    val profilePic:String = "",
    val password:String = "",
    val uniqueId:String = "",
    val username:String = "",
    val docId:String = "",
    val publicRSAKey:String = "",
    val privateEncryptedRSAKey:String = "",
    val salt:String = "",
    val devicePrivateRSAKey:PrivateKey? = null
)

data class PublicRSAKey(
    val username:String = "",
    val publicRSAKey:String = ""
)
