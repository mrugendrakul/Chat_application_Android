package com.mad.softwares.chatApplication.data.onDevice.chatASEKeys

import kotlinx.coroutines.flow.Flow

interface LocalAESkeys {

    fun getAllAESKeys(): Flow<List<ChatOrGroupAESKeys>>

    fun getAESKeyById(chatId: Int): Flow<ChatOrGroupAESKeys?>

    suspend fun saveAESKey(key: ChatOrGroupAESKeys)

    suspend fun updateAESKey(key: ChatOrGroupAESKeys)

    suspend fun deleteAESKey(key: ChatOrGroupAESKeys)
}

class OfflineLocalAESKeys(private val aesKeysDao: aesKeysDao):LocalAESkeys{
    override fun getAllAESKeys(): Flow<List<ChatOrGroupAESKeys>> = aesKeysDao.getAllAESKeys()

    override fun getAESKeyById(chatId: Int): Flow<ChatOrGroupAESKeys?> = aesKeysDao.getAESKeyById(chatId)

    override suspend fun saveAESKey(key: ChatOrGroupAESKeys) = aesKeysDao.insert(key)

    override suspend fun updateAESKey(key: ChatOrGroupAESKeys) = aesKeysDao.update(key)

    override suspend fun deleteAESKey(key: ChatOrGroupAESKeys) = aesKeysDao.delete(key)

}