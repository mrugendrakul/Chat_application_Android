package com.mad.softwares.chatApplication.data

import android.content.Context
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.mad.softwares.chatApplication.data.onDevice.InventoryKeysDatabase
import com.mad.softwares.chatApplication.data.onDevice.RSAKeys.LocalKeysStorage
import com.mad.softwares.chatApplication.data.onDevice.RSAKeys.OfflineLocalKeysStorage
import com.mad.softwares.chatApplication.data.onDevice.chatASEKeys.LocalAESkeys
import com.mad.softwares.chatApplication.data.onDevice.chatASEKeys.OfflineLocalAESKeys
import com.mad.softwares.chatApplication.encryption.EncryptionImpl
import com.mad.softwares.chatApplication.network.AuthenticationApi
import com.mad.softwares.chatApplication.network.FirebaseApi
import com.mad.softwares.chatApplication.network.FirebaseAuthenticationApi
import com.mad.softwares.chatApplication.network.NetworkFirebaseApi
import com.mad.softwares.chatApplication.notification.BodyDecryptionImplementation
import com.mad.softwares.chatApplication.notification.NotificationAddon

interface AppContainer {
    val dataRepository:DataRepository
    val apiService:FirebaseApi
    val auth: FirebaseAuth
    val authApi:AuthenticationApi
    val localKeyStorge: LocalKeysStorage
    val localAESKeyStorage: LocalAESkeys
    val notificationAddon: NotificationAddon

    val aiWorkManagerRespository : WorkRespository
}

class DefaultAppContainer(private val context : Context) : AppContainer{
    private val db = Firebase.firestore
    private val userCollection = db.collection("Users")
    private val chatsCollection = db.collection("Chats")
    private val messagesCollection = db.collection("Messages")
    private val keyCollection = db.collection("KeyStore")

    override val localKeyStorge: LocalKeysStorage by lazy {
        OfflineLocalKeysStorage(InventoryKeysDatabase.getDatabase(context = context).keyDao())
    }
    override val localAESKeyStorage: LocalAESkeys by lazy {
        OfflineLocalAESKeys(InventoryKeysDatabase.getDatabase(context = context).aesKeysDao())
    }

    private val encryption = EncryptionImpl()

    override val apiService: FirebaseApi = NetworkFirebaseApi(
        db,
        userCollection,
        chatsCollection,
        messagesCollection,
        keyCollection
    )


    override val auth: FirebaseAuth = Firebase.auth
    override val authApi: AuthenticationApi = FirebaseAuthenticationApi(auth)
    override val dataRepository: DataRepository = NetworkDataRepository(
        apiService,
        authServie = authApi,
        encryption,
        localKeyStorge = localKeyStorge,
        localChatKeysStorage = localAESKeyStorage
    )

    override val notificationAddon: NotificationAddon = BodyDecryptionImplementation(
        dataRepository = dataRepository,
        encryptionService =   encryption
    )

    override val aiWorkManagerRespository: WorkRespository = AiWorkManagerRespository(context)
}