package com.mad.softwares.chatApplication.data.onDevice

import android.content.Context
import androidx.room.Database
import androidx.room.Room

import androidx.room.RoomDatabase
import com.mad.softwares.chatApplication.data.onDevice.RSAKeys.keyDao
import com.mad.softwares.chatApplication.data.onDevice.RSAKeys.privateKeys
import com.mad.softwares.chatApplication.data.onDevice.chatASEKeys.ChatOrGroupAESKeys
import com.mad.softwares.chatApplication.data.onDevice.chatASEKeys.aesKeysDao

@Database(entities = [privateKeys::class,ChatOrGroupAESKeys::class], version = 2, exportSchema = false)
abstract class InventoryKeysDatabase:RoomDatabase() {
    abstract fun keyDao() : keyDao

    abstract fun aesKeysDao():aesKeysDao

    companion object {
        @Volatile
        private var INSTANCE: InventoryKeysDatabase? = null

        fun getDatabase(context: Context): InventoryKeysDatabase {
            return INSTANCE ?:synchronized(this) {
                Room.databaseBuilder(context, InventoryKeysDatabase::class.java, "keys_database")
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}