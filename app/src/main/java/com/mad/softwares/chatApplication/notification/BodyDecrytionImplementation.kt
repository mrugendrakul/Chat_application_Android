package com.mad.softwares.chatApplication.notification

import android.util.Log
import com.mad.softwares.chatApplication.data.DataRepository
import com.mad.softwares.chatApplication.encryption.EncryptionImpl

interface NotificationAddon {
    suspend fun decryptBody(encryptedBody: String, chatId: String): String
}

val TAGNotf = "MyFirebaseMessagingService"

class BodyDecryptionImplementation(
    private val dataRepository: DataRepository,
    private val encryptionService: EncryptionImpl
) : NotificationAddon{
    override suspend fun decryptBody(encryptedBody: String, chatId: String): String {
            val secureAES = dataRepository.getAESKeyForChatID(chatId)
            var decryptedBody = ""
            try{
                decryptedBody = String(encryptionService.aesDecrypt(
                    encryptedData = encryptionService.stringToByteArray(
                        encryptedBody
                    ),
                    secretKey = encryptionService.stringToAESKey(secureAES)
                ))
            }catch (e: Exception){
                Log.e(TAGNotf,"Unable to decrypt the messages")
                    decryptedBody =encryptedBody
            }
        return decryptedBody
    }
}