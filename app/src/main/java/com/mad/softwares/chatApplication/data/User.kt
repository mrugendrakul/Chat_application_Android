package com.mad.softwares.chatApplication.data

import java.security.PublicKey

data class User(
    val fcmToken:String = "",
    val profilePic:String = "",
    val password:String = "",
    val uniqueId:String = "",
    val username:String = "",
    val docId:String = "",
    val publicRSAKey:String = "",
    val privateEncryptedRSAKey:String = "",
    val salt:ByteArray = byteArrayOf()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as User

        if (fcmToken != other.fcmToken) return false
        if (profilePic != other.profilePic) return false
        if (password != other.password) return false
        if (uniqueId != other.uniqueId) return false
        if (username != other.username) return false
        if (docId != other.docId) return false
        if (publicRSAKey != other.publicRSAKey) return false
        if (privateEncryptedRSAKey != other.privateEncryptedRSAKey) return false
        if (!salt.contentEquals(other.salt)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = fcmToken.hashCode()
        result = 31 * result + profilePic.hashCode()
        result = 31 * result + password.hashCode()
        result = 31 * result + uniqueId.hashCode()
        result = 31 * result + username.hashCode()
        result = 31 * result + docId.hashCode()
        result = 31 * result + publicRSAKey.hashCode()
        result = 31 * result + privateEncryptedRSAKey.hashCode()
        result = 31 * result + salt.contentHashCode()
        return result
    }
}
