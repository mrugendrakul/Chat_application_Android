package com.mad.softwares.chatApplication.data.onDevice.chatASEKeys

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface aesKeysDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(key: ChatOrGroupAESKeys)

    @Update
    suspend fun update(key: ChatOrGroupAESKeys)

    @Delete
    suspend fun delete(key: ChatOrGroupAESKeys)

    @Query("SELECT * FROM ChatOrGroupAESKeys")
    fun getAllAESKeys(): Flow<List<ChatOrGroupAESKeys>>

    @Query("SELECT * FROM ChatOrGroupAESKeys WHERE chatId = :chatId")
    fun getAESKeyById(chatId: Int): Flow<ChatOrGroupAESKeys?>

}