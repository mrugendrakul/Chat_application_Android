package com.mad.softwares.chatApplication.data

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.mad.softwares.chatApplication.encryption.EncryptionImpl
import com.mad.softwares.chatApplication.network.AuthenticationApi
import com.mad.softwares.chatApplication.network.FirebaseApi
import com.mad.softwares.chatApplication.network.FirebaseAuthenticationApi
import com.mad.softwares.chatApplication.network.NetworkFirebaseApi

interface AppContainer {
    val dataRepository:DataRepository
    val apiService:FirebaseApi
    val auth: FirebaseAuth
    val authApi:AuthenticationApi
}

class DefaultAppContainer : AppContainer{
    private val db = Firebase.firestore
    private val userCollection = db.collection("Users")
    private val chatsCollection = db.collection("Chats")
    private val messagesCollection = db.collection("Messages")

    private val encryption = EncryptionImpl()

    override val apiService: FirebaseApi = NetworkFirebaseApi(
        db,
        userCollection,
        chatsCollection,
        messagesCollection
        )

    override val auth: FirebaseAuth = Firebase.auth
    override val authApi: AuthenticationApi = FirebaseAuthenticationApi(auth)
    override val dataRepository: DataRepository = NetworkDataRepository(
        apiService,
        authServie = authApi,
        encryption
    )
}