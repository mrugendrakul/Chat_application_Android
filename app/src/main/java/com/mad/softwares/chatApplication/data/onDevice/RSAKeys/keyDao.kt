package com.mad.softwares.chatApplication.data.onDevice.RSAKeys

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface keyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(key: privateKeys)

    @Update
    suspend fun update(key: privateKeys)

    @Delete
    suspend fun delete(key: privateKeys)

    @Query("SELECT * FROM privateKeys")
     fun getAllKeys(): Flow<List<privateKeys>>

    @Query("SELECT * FROM privateKeys WHERE keyId = :keyId")
     fun getKeyById(keyId: Int): Flow<privateKeys?>

     @Query("Delete from privateKeys")
     fun cleanAfterLogout()
}

