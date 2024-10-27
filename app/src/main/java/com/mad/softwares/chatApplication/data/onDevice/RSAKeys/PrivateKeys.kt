package com.mad.softwares.chatApplication.data.onDevice.RSAKeys

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class privateKeys(

    @PrimaryKey(autoGenerate = true)
    val keyId :Int,
    val privateKey:String
)
