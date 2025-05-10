package com.mad.softwares.chatApplication.data.onDevice.RSAKeys

import kotlinx.coroutines.flow.Flow


interface LocalKeysStorage {
    fun getAllKeys(): Flow<List<privateKeys>>

    fun getPrivateKey(keyId: Int): Flow<privateKeys?>

    suspend fun  savePrivateKey(key: privateKeys)

    suspend fun updatePrivateKey(key: privateKeys)

    suspend  fun deletePrivateKey(key: privateKeys)

    suspend fun cleanAfterLogout()
}

class OfflineLocalKeysStorage(private val keyDao: keyDao):LocalKeysStorage{
    override fun getAllKeys(): Flow<List<privateKeys>> = keyDao.getAllKeys()

    override fun getPrivateKey(keyId: Int): Flow<privateKeys?> = keyDao.getKeyById(keyId = keyId)

    override suspend fun savePrivateKey(key: privateKeys)= keyDao.insert(key)

    override suspend fun updatePrivateKey(key: privateKeys) = keyDao.update(key)

    override suspend fun deletePrivateKey(key: privateKeys) = keyDao.delete(key)

    override suspend fun cleanAfterLogout() = keyDao.cleanAfterLogout()
}