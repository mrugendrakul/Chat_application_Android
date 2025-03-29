package com.mad.softwares.chatApplication.data

import android.util.Log
import com.google.firebase.auth.FirebaseUser
import com.mad.softwares.chatApplication.data.onDevice.RSAKeys.LocalKeysStorage
import com.mad.softwares.chatApplication.data.onDevice.RSAKeys.privateKeys
import com.mad.softwares.chatApplication.data.onDevice.chatASEKeys.ChatOrGroupAESKeys
import com.mad.softwares.chatApplication.data.onDevice.chatASEKeys.LocalAESkeys
import com.mad.softwares.chatApplication.encryption.Encryption
import com.mad.softwares.chatApplication.network.AuthenticationApi
import com.mad.softwares.chatApplication.network.FirebaseApi
import com.mad.softwares.chatApplication.notification.NotificationRequest
import com.mad.softwares.chatApplication.notification.notificationApiSending
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import retrofit2.Call

val TAG = "DataRepository_Logs"

interface DataRepository {
    suspend fun getToken(): String?

    //    suspend fun usernameExist(username:String):Boolean
    suspend fun registerUser(
        user: User,
        currFcmToken: String
    ): User

    suspend fun loginUser(
        user: User,
        currFcmToken: String
    ): User

    suspend fun getCurrentUser(): User

    suspend fun logoutUser(currUser: User): Boolean

    suspend fun resetDataPassword(username: String, newPassword: String, uniqueId: String): Boolean

    suspend fun getAllTheUsers(members: List<String>): List<chatUser>

    suspend fun getSearchUsers(members: List<String>, search: String): List<chatUser>

    suspend fun addChatToDatabase(
        currentUser: User,
        memberUsers: List<String>,
        chatName: String,
        chatId: String,
        profilePhoto: String,
        isGroup: Boolean
    )

    suspend fun getChats(myUsername: String): List<ChatOrGroup>

    suspend fun getGroups(myUsername: String): List<ChatOrGroup>

    suspend fun sendMessage(
        message: MessageReceived,
        chatId: String,
        secureAESKey: String,
        fcmTokens: List<String>
    )

    suspend fun sendNotificationToToken(token: String, title: String, content: String)

//    suspend fun getTokenForMemebers(members: List<String>, currentFcmToken: String): List<String>

    suspend fun getMyMessages(currentChatId: String): List<MessageReceived>

    suspend fun deleteChat(chatId: String)

    suspend fun getDataChat(chatId: String, chatName: String): ChatOrGroup

    suspend fun getLiveMessages(
        chatId: String,
        secureAESKey: String,
        onMessagesChange: (List<MessageReceived>) -> Unit,
        onError: (e: Exception) -> Unit,
        onAdd: (MessageReceived) -> Unit
    )

    suspend fun stopLiveMessages()

    suspend fun liveChatStore(
        myUsername: String,
        onChatAdd: (ChatOrGroup) -> Unit,
        onChatUpdate: (ChatOrGroup) -> Unit,
        onChatDelete: (ChatOrGroup) -> Unit,
        onError: (e: Exception) -> Unit
    )

    suspend fun getLiveGroupStore(
        myUsername: String,
        onChatAdd: (ChatOrGroup) -> Unit,
        onChatUpdate: (ChatOrGroup) -> Unit,
        onChatDelete: (ChatOrGroup) -> Unit,
        onError: (e: Exception) -> Unit
    )

    suspend fun stopLiveChat()

    suspend fun getAESKeyForChatID(chatId: String): String

    suspend fun getAllDeviceChats(): List<ChatOrGroup>?
}

class NetworkDataRepository(
    private val apiService: FirebaseApi,
    private val authServie: AuthenticationApi,
    private val encryptionService: Encryption,
    private val localKeyStorge: LocalKeysStorage,
    private val localChatKeysStorage: LocalAESkeys
) : DataRepository {

    override suspend fun getToken(): String? {
        Log.d(TAG, "Token in data started")
        val token: String = coroutineScope {
            val token = async { apiService.getFCMToken() }
            token.await()
        }
        Log.d(TAG, "Token from network is $token")
        return token
    }

    //    override suspend fun usernameExist(username: String): Boolean {
//
//        val exist:Boolean =  coroutineScope {
//            val exist = async{apiService.checkUsernameExist(username)}
//            exist.await()
//        }
//        return exist
//
    override suspend fun registerUser(
        user: User,
        currFcmToken: String
    ): User {
        val statusDeferred = CompletableDeferred<String>()
        val currentUser: FirebaseUser?
        var toReturn: Boolean
        val keypair = encryptionService.generateRSAKeyPair()
        val salt = encryptionService.generateRandomSalt()
        val newuser = user.copy(
            publicRSAKey = encryptionService.publicKeyToString(keypair.public),
            privateEncryptedRSAKey = encryptionService.byteArrayToString(
                encryptionService.encryptPrivateKeyWithPassword(
                    privateKey = keypair.private,
                    password = user.password,
                    salt = salt
                )
            ),
            salt = encryptionService.byteArrayToString(salt)
        )
        coroutineScope {
            try {
                localKeyStorge.savePrivateKey(
                    key = privateKeys(
                        keyId = 1,
                        privateKey = encryptionService.privateKeyToString(keypair.private)
                    )
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error adding key to device: ${e}")
            }
        }

        coroutineScope {
            currentUser =
                authServie.signUpUser(
                    email = user.username,
                    password = user.password,
                    status = { statusDeferred.complete(it) }
                )
            val status = statusDeferred.await()

            if (status == "success") {
                try {

                    apiService.registerUserToDatabase(
                        currUser = newuser,
                        docId = currentUser?.uid ?: "garbage"
                    )
                    Log.d(TAG, "Signup success")
                    toReturn = true

                } catch (e: Exception) {
                    Log.d(TAG, "Signup failed in data repo : $e")
                    toReturn = false
                }
            } else {
                toReturn = false
                Log.d(TAG, "Signup failed")
            }
        }
        return if (toReturn) {
            user
        } else {
            User(
                profilePic = statusDeferred.await()
            )
        }
    }

    override suspend fun loginUser(
        user: User,
        currFcmToken: String
    ): User {
        val statusDeferred = CompletableDeferred<String>()
        val currentUser: FirebaseUser?
        var toReturn: Boolean

        currentUser =
            authServie.loginUser(
                email = user.username,
                password = user.password,
                status = { statusDeferred.complete(it) }
            )
        val status = statusDeferred.await()

        if (status == "success") {
            try {
                Log.d(TAG, "Login success with addition of keys")
                val loggedUser = coroutineScope {
                    val loginUser = async {
                        apiService.loginAndUpdateUserToDatabase(
                            currUser = user,
                            currFcmToken = currFcmToken,
                            docId = currentUser?.uid ?: "garbage"
                        )
                    }
                    loginUser.await()
                }

                localKeyStorge.savePrivateKey(
                    key = privateKeys(
                        keyId = 1,
                        privateKey = encryptionService.privateKeyToString(
                            encryptionService.decryptPrivateKeyWithPassword(
                                encryptedPrivateKey = encryptionService.stringToByteArray(loggedUser.privateEncryptedRSAKey),
                                password = user.password,
                                salt = encryptionService.stringToByteArray(loggedUser.salt)
                            )
                        )
                    )
                )

                toReturn = true
                return loggedUser
            } catch (e: Exception) {
                Log.d(TAG, "Login failed in data repo : $e")
                toReturn = false
                return User(profilePic = e.message.toString())
            }
        } else {
            toReturn = false
            return User(profilePic = statusDeferred.await())
        }

//        coroutineScope {
//            try {
//                localKeyStorge.savePrivateKey(
//                    key = privateKeys(
//                        keyId = 0,
//                        privateKey = user.privateEncryptedRSAKey
//                    )
//                )
//            }
//            catch (e:Exception){
//                Log.e(TAG,"Error adding key to device: ${e}")
//            }
//        }

//        if(toReturn){
//
//        }
//        return if (toReturn) {
//            user.copy(
//                privateEncryptedRSAKey = encryptionService.privateKeyToString(
//                    encryptionService.decryptPrivateKeyWithPassword(
//                        encryptedPrivateKey = encryptionService.stringToByteArray(user.privateEncryptedRSAKey),
//                        password = user.password,
//                        salt = encryptionService.stringToByteArray(user.salt)
//                    )
//                ),
//            )
//
//        } else {
//            User(
//                profilePic = statusDeferred.await()
//            )
//        }
    }

    private var RSAPublicKeyCache: String = ""

    override suspend fun getCurrentUser(): User {

//        val curUser:User
        try {
            val uid = authServie.getCurrentUser()
//            val curUser = coroutineScope {
//                val currentUser = async { apiService.getUserFromUid(uid) }
//                currentUser.await()
//            }
            val curUser = User(
                docId = uid,
                username = authServie.getCurrentUsername()
            )
//            Log.d(TAG, "Current user is : ${curUser.username}")
            Log.d(TAG,"Current User got no api hit:${curUser.username}")
//            val key = localKeyStorge.getPrivateKey(keyId = 1)
//            val privateKey = key.collect()
            val keyFlow = localKeyStorge.getPrivateKey(keyId = 1)
            val privateKey = keyFlow.firstOrNull()?.privateKey

            if (privateKey == null) {
                return User(profilePic = "NoKeyFound")
            }
//            RSAPublicKeyCache = curUser.publicRSAKey
            return curUser.copy(
                devicePrivateRSAKey = encryptionService.stringToPrivateKey(privateKey ?: "")
//                privateEncryptedRSAKey = encryptionService.privateKeyToString(
//                    encryptionService.decryptPrivateKeyWithPassword(
//                        encryptedPrivateKey = encryptionService.stringToByteArray(curUser.privateEncryptedRSAKey),
//                        password = "123456",
//                        salt = encryptionService.stringToByteArray(curUser.salt)
//                    )
//                )
            )

        } catch (e: Exception) {
            Log.e(TAG, "Unable to get the current user : ${e.message}")
            return User(profilePic = "$e")
        }

//        return curUser
    }

    override suspend fun logoutUser(currUser: User): Boolean {
        try {
            Log.d(TAG, "Logout startd in data for usr : $currUser")
//            val curr = currUser.copy(fcmToken = "")
            apiService.logoutUser(currUser)
            authServie.logoutUser { throw Exception(it) }

            Log.d(TAG, "Logout successful")
            return true
        } catch (e: Exception) {
            Log.d(TAG, "unable to logout from datarepo : $e")
            return false
        }
    }

    //To Change which uses authentication api
    override suspend fun resetDataPassword(
        username: String,
        newPassword: String,
        uniqueId: String
    ): Boolean {
        return try {
            Log.d(TAG, "updateing user id and pass is :${username} , ${newPassword} , ${uniqueId}")
            apiService.resetPassword(
                username = username, newPassword = newPassword,
                uniqueId = uniqueId
            ).isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getAllTheUsers(members: List<String>): List<chatUser> {
        val Users: List<chatUser> = try {
            apiService.getAllUsers()
        } catch (e: Exception) {
            listOf<chatUser>()
            throw e
        }

        return Users.distinctBy { it.username }.sortedBy { it.username.lowercase() }
            .filter {
                !members.contains(it.username)
            }
//            .map { it.username } - members
    }

    override suspend fun getSearchUsers(members: List<String>, search: String): List<chatUser> {
        val Users: List<chatUser> = try {
            apiService.getSearchUsers(search)
        } catch (e: Exception) {
            listOf<chatUser>()
            throw e
        }

        return Users.distinctBy { it.username }.sortedBy { it.username.lowercase() }
            .filter {
                !members.contains(it.username)
            }
    }

    override suspend fun addChatToDatabase(
        currentUser: User,
        memberUsers: List<String>,
        chatName: String,
        chatId: String,
        profilePhoto: String,
        isGroup: Boolean
    ) {
        val AESKeys: MutableList<AESKeyData> = mutableListOf()
//
//        for (user in memberUsers) {
//            newMembers.add(user)
//        }
        val newMembers = memberUsers + currentUser.username
        Log.d(TAG, "Chat addition startd at data")

        val commonAESKey = encryptionService.generateAESKey()

        try {
            val keyData = coroutineScope {
                val keys = async { apiService.getPublicRSAKeyForMember(memberUsers) }
                keys.await()
            }
            val recentMessage = encryptionService.aesEncrypt(
                data = "No Message send, start chatting now!!".toByteArray(),
                secretKey = commonAESKey
            )
            Log.d(TAG, "key data is : ${keyData.size}")
            for (key in keyData) {
                Log.d(TAG, "Adding user for key : ${key.username}")
                AESKeys.add(
                    AESKeyData(
                        username = key.username,
                        key = encryptionService.byteArrayToString(
                            encryptionService.encryptAESKeyWithPublicKey(
                                secretKey = commonAESKey,
                                publicKey = encryptionService.stringToPublicKey(key.publicRSAKey)
                            )
                        )
                    )
                )
            }
            AESKeys.add(
                AESKeyData(
                    username = currentUser.username,
                    key = encryptionService.byteArrayToString(
                        encryptionService.encryptAESKeyWithPublicKey(
                            secretKey = commonAESKey,
                            publicKey = encryptionService.stringToPublicKey(currentUser.publicRSAKey)
                        )
                    )
                )
            )

            apiService.createNewChat(
                members = memberUsers + currentUser.username,
                chatName = chatName,
                chatId = chatId,
                profilePhoto = profilePhoto,
                isGroup = isGroup,
                encryptedAESKeys = AESKeys.toList(),
                recentMessage = encryptionService.byteArrayToString(recentMessage)
            )
            Log.d(TAG, "Successfully added chat")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add chat at data")
            throw e
        }

    }

    override suspend fun getChats(myUsername: String): List<ChatOrGroup> {
        val chatss = coroutineScope {
            Log.d(TAG, "Fetched chats in data")
            val chats = async {
                apiService.getChatsforMe(myUsername)
            }
            chats.await()
        }

        try {
            val myChats = chatss.map { t ->
                var tempUsername: String = "chat"
                for (mem in t.members) {
                    if (mem != myUsername) {
                        tempUsername = mem
                        val memInfo =
                            coroutineScope {
                                val info = async { apiService.getUserChatData(mem) }
                                info.await()
                            }
                        if (memInfo.username != "") {
                            t.membersData.add(memInfo)
                        }
                    }
                }
                t.copy(chatName = tempUsername)

            }
                .filter {
                    it.membersData.size == (it.members.size - 1)
                }
//            Log.d(
//                TAG,
//                "Chats are : ${myChats.map { it.membersData.size }} == ${myChats.map { it.members.size }}"
//            )

            return myChats
        } catch (e: Exception) {
            Log.e(TAG, "Unable to get the chats: $e")
            throw e
        }
    }

    override suspend fun getGroups(myUsername: String): List<ChatOrGroup> {
        val chatss = coroutineScope {
            Log.d(TAG, "Fetched chats in data")
            val chats = async {
                apiService.getGroupsForMe(myUsername)
            }
            chats.await()
        }

        try {
            val myChats = chatss.map { t ->
                var tempUsername: String = "chat"
                for (mem in t.members) {
                    if (mem != myUsername) {
                        val memInfo =
                            coroutineScope {
                                val info = async { apiService.getUserChatData(mem) }
                                info.await()
                            }
                        if (memInfo.username != "") {
                            t.membersData.add(memInfo)
                        }
                    }
                }
                t
            }
                .filter {
                    it.membersData.size == (it.members.size - 1)
                }
//            Log.d(
//                TAG,
//                "Groups are : ${myChats.map { it.membersData.size }} == ${myChats.map { it.members.size }}"
//            )

            return myChats
        } catch (e: Exception) {
            Log.e(TAG, "Unable to get the groups: $e")
            throw e
        }
    }

    override suspend fun liveChatStore(
        myUsername: String,
        onChatAdd: (ChatOrGroup) -> Unit,
        onChatUpdate: (ChatOrGroup) -> Unit,
        onChatDelete: (ChatOrGroup) -> Unit,
        onError: (e: Exception) -> Unit
    ) {
        val keyFlow = localKeyStorge.getPrivateKey(keyId = 1)
        val privateKey = keyFlow.firstOrNull()?.privateKey
        apiService.getLiveChatsOrGroups(
            username = myUsername,
            isGroup = false,
            onAddChat = { newChat ->
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val secureAESKeyFlow =
                            localChatKeysStorage.getAESKeyById(newChat.chatId.toInt())
                        var secureAES: String = ""

                        var tempUsername: String = "chat"
                        for (mem in newChat.members) {
                            if (mem != myUsername) {
                                tempUsername = mem
                                val memInfo =
                                    coroutineScope {
                                        val info = async { apiService.getUserChatData(mem) }
                                        info.await()
                                    }
                                if (memInfo.username != "") {
                                    newChat.membersData.add(memInfo)
                                }
                            }
                        }

                        if (secureAESKeyFlow.firstOrNull()?.decryptedASEKey == null) {
//                            Log.d(TAG,"Adding key to backend for all chats in all chats : ${newChat.secureAESKey}")
                            secureAES = encryptionService.aesKeyToString(
                                encryptionService.decryptAESKeyWithPrivateKey(
                                    encryptedAESKey = encryptionService.stringToByteArray(newChat.secureAESKey),
                                    privateKey = encryptionService.stringToPrivateKey(
                                        privateKey ?: ""
                                    )
                                )
                            )
                            localChatKeysStorage.saveAESKey(
                                ChatOrGroupAESKeys(
                                    chatId = newChat.chatId.toInt(),
                                    decryptedASEKey = secureAES,
                                    chatName = tempUsername
                                )
                            )
                        } else {
                            Log.d(TAG, "No key to backend for current chat")
                            secureAES = secureAESKeyFlow.firstOrNull()?.decryptedASEKey ?: ""
                        }


                        var contentEnc = ""
                        try {
                            contentEnc = String(
                                encryptionService.aesDecrypt(
                                    encryptedData = encryptionService.stringToByteArray(
                                        newChat.lastMessage.content
                                    ), secretKey = encryptionService.stringToAESKey(secureAES)
                                )
                            )
                        } catch (e: Exception) {
                            Log.e(TAG, "Unable to decrypt the message: $e")
                            contentEnc = "Encrypted Message, update application"
                        }
                        val latestChat = newChat.copy(chatName = tempUsername)
                        if (latestChat.members.size - 1 == latestChat.membersData.size) {
                            val laterLatestChat = latestChat.copy(
//                                lastMessage = lastMessage(
//                                    content = contentEnc,
//                                    timestamp = latestChat.lastMessage.timestamp
//                                )
                                lastMessage = latestChat.lastMessage.copy(
                                    content = contentEnc
                                )
                            )
                            Log.d(TAG, "Chat added in data")
                            onChatAdd(laterLatestChat)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error while getting the data : ${e}")
                    }
                }
            },
            onModifiedChat = { modifiedChat ->
                CoroutineScope(Dispatchers.IO).launch {
                    val secureAESKeyFlow =
                        localChatKeysStorage.getAESKeyById(modifiedChat.chatId.toInt())
                    var secureAES: String = ""
                    var tempUsername: String = "chat"
                    for (mem in modifiedChat.members) {
                        if (mem != myUsername) {
                            tempUsername = mem
//                            val memInfo =
//                                coroutineScope {
//                                    val info = async { apiService.getUserChatData(mem) }
//                                    info.await()
//                                }
//                            if (memInfo.username != "") {
//                                modifiedChat.membersData.add(memInfo)
//                            }
                        }
                    }
                    if (secureAESKeyFlow.firstOrNull()?.decryptedASEKey == null) {
                        Log.d(
                            TAG,
                            "Adding key to backend for all chats in all chats : ${modifiedChat.secureAESKey}"
                        )
                        secureAES = encryptionService.aesKeyToString(
                            encryptionService.decryptAESKeyWithPrivateKey(
                                encryptedAESKey = encryptionService.stringToByteArray(modifiedChat.secureAESKey),
                                privateKey = encryptionService.stringToPrivateKey(privateKey ?: "")
                            )
                        )
                        localChatKeysStorage.saveAESKey(
                            ChatOrGroupAESKeys(
                                chatId = modifiedChat.chatId.toInt(),
                                decryptedASEKey = secureAES,
                                chatName = tempUsername
                            )
                        )
                    } else {
                        Log.d(TAG, "No key to backend for current chat")
                        secureAES = secureAESKeyFlow.firstOrNull()?.decryptedASEKey ?: ""
                    }

                    var contentEnc = ""
                    try {
                        contentEnc = String(
                            encryptionService.aesDecrypt(
                                encryptedData = encryptionService.stringToByteArray(
                                    modifiedChat.lastMessage.content
                                ), secretKey = encryptionService.stringToAESKey(secureAES)
                            )
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Unable to decrypt the message: $e")
                        contentEnc = "Encrypted Message, update application"
                    }

                    val latestModifiedChat = modifiedChat.copy(
                        lastMessage = modifiedChat.lastMessage.copy(
                            content = contentEnc
                        )
                    )

                    onChatUpdate(latestModifiedChat)
                }

            },
            onDeleteChat = onChatDelete,
            onError = onError
        )
    }

    override suspend fun getLiveGroupStore(
        myUsername: String,
        onChatAdd: (ChatOrGroup) -> Unit,
        onChatUpdate: (ChatOrGroup) -> Unit,
        onChatDelete: (ChatOrGroup) -> Unit,
        onError: (e: Exception) -> Unit
    ) {
        val keyFlow = localKeyStorge.getPrivateKey(keyId = 1)
        val privateKey = keyFlow.firstOrNull()?.privateKey
        apiService.getLiveChatsOrGroups(
            username = myUsername,
            isGroup = true,
            onAddChat = { newChat ->
                CoroutineScope(Dispatchers.IO).launch {
                    val secureAESKeyFlow =
                        localChatKeysStorage.getAESKeyById(newChat.chatId.toInt())
                    var secureAES: String = ""
                    if (secureAESKeyFlow.firstOrNull()?.decryptedASEKey == null) {
//                            Log.d(TAG,"Adding key to backend for all chats in all chats : ${newChat.secureAESKey}")
                        secureAES = encryptionService.aesKeyToString(
                            encryptionService.decryptAESKeyWithPrivateKey(
                                encryptedAESKey = encryptionService.stringToByteArray(newChat.secureAESKey),
                                privateKey = encryptionService.stringToPrivateKey(privateKey ?: "")
                            )
                        )
                        localChatKeysStorage.saveAESKey(
                            ChatOrGroupAESKeys(
                                chatId = newChat.chatId.toInt(),
                                decryptedASEKey = secureAES,
                                chatName = newChat.chatName
                            )
                        )
                    } else {
                        Log.d(TAG, "No key to backend for current chat")
                        secureAES = secureAESKeyFlow.firstOrNull()?.decryptedASEKey ?: ""
                    }
                    var tempUsername: String = "chat"
                    for (mem in newChat.members) {
                        if (mem != myUsername) {
                            tempUsername = mem
                            val memInfo =
                                coroutineScope {
                                    val info = async { apiService.getUserChatData(mem) }
                                    info.await()
                                }
                            if (memInfo.username != "") {
                                newChat.membersData.add(memInfo)
                            }
                        }
                    }
//                    val latestChat = newChat.copy(chatName = tempUsername)
                    var contentEnc = ""
                    try {
                        contentEnc = String(
                            encryptionService.aesDecrypt(
                                encryptedData = encryptionService.stringToByteArray(
                                    newChat.lastMessage.content
                                ), secretKey = encryptionService.stringToAESKey(secureAES)
                            )
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Unable to decrypt the message: $e")
                        contentEnc = "Encrypted Message, update application"
                    }
                    if (newChat.members.size - 1 == newChat.membersData.size) {
                        val laterLatestChat = newChat.copy(
                            lastMessage = newChat.lastMessage.copy(
                                content = contentEnc
                            )
                        )
                        Log.d(TAG, "Chat added in data")
                        onChatAdd(laterLatestChat)
                    }
                }
            },
            onModifiedChat = { modifiedChat ->
                CoroutineScope(Dispatchers.IO).launch {
                    val secureAESKeyFlow =
                        localChatKeysStorage.getAESKeyById(modifiedChat.chatId.toInt())
                    var secureAES: String = ""
                    if (secureAESKeyFlow.firstOrNull()?.decryptedASEKey == null) {
                        Log.d(
                            TAG,
                            "Adding key to backend for all chats in all chats : ${modifiedChat.secureAESKey}"
                        )
                        secureAES = encryptionService.aesKeyToString(
                            encryptionService.decryptAESKeyWithPrivateKey(
                                encryptedAESKey = encryptionService.stringToByteArray(modifiedChat.secureAESKey),
                                privateKey = encryptionService.stringToPrivateKey(privateKey ?: "")
                            )
                        )
                        localChatKeysStorage.saveAESKey(
                            ChatOrGroupAESKeys(
                                chatId = modifiedChat.chatId.toInt(),
                                decryptedASEKey = secureAES,
                                chatName = modifiedChat.chatName
                            )
                        )
                    } else {
                        Log.d(TAG, "No key to backend for current chat")
                        secureAES = secureAESKeyFlow.firstOrNull()?.decryptedASEKey ?: ""
                    }

                    var contentEnc = ""
                    try {
                        contentEnc = String(
                            encryptionService.aesDecrypt(
                                encryptedData = encryptionService.stringToByteArray(
                                    modifiedChat.lastMessage.content
                                ), secretKey = encryptionService.stringToAESKey(secureAES)
                            )
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Unable to decrypt the message: $e")
                        contentEnc = "Encrypted Message, update application"
                    }

                    val latestModifiedChat = modifiedChat.copy(
                        lastMessage = modifiedChat.lastMessage.copy(
                            content = contentEnc
                        )
                    )

                    onChatUpdate(latestModifiedChat)
                }

            },
            onDeleteChat = onChatDelete,
            onError = onError
        )
    }

    override suspend fun sendMessage(
        message: MessageReceived,
        chatId: String,
        secureAESKey: String,
        fcmTokens: List<String>
    ) {
        try {
            val encryptedMessage = encryptionService.aesEncrypt(
                data = message.content.toByteArray(),
                secretKey = encryptionService.stringToAESKey(secureAESKey)
            )
//            for (token in fcmTokens) {
//                Log.d(TAG, "Token is : $token")
//                val notificationBody = NotificationRequest(
//                    fcmToken = token,
//                    title = message.senderId,
//                    body = encryptionService.byteArrayToString(encryptedMessage),
//                    chatId = chatId
//                )
////                notificationApiSending.sendNotificationToDevice(notificationBody)
////                    .enqueue(object : retrofit2.Callback<Void> {
////                        override fun onResponse(
////                            call: Call<Void>,
////                            response: retrofit2.Response<Void>
////                        ) {
////                            if (response.isSuccessful) {
////                                // Successfully sent the request
////                                Log.d(TAG, "Notification sent successfully.")
////                            } else {
////                                // Handle the error
////                                Log.e(
////                                    TAG,
////                                    "Failed to send notification. Error code: ${response.code()}"
////                                )
////                            }
////                        }
////
////                        override fun onFailure(call: Call<Void>, t: Throwable) {
////                            // Handle failure
////                            println("Error: ${t.message}")
////                        }
////                    })
//            }
//            notificationApiSending.sendNotification()
            val staus =
                apiService.sendNewMessage(
                    message.copy(content = encryptionService.byteArrayToString(encryptedMessage)),
                    chatId = chatId,
                    recentMessage = encryptionService.byteArrayToString(encryptedMessage)
                )

            if (!staus) {
                throw Exception("Unable to send message")
            }
            Log.d(TAG, "send successfully from data ")
//            return status
        } catch (e: Exception) {
            Log.e(TAG, "unabe to send message to database from data : $e")
//            return false
            throw e
        }
    }

    override suspend fun sendNotificationToToken(token: String, title: String, content: String) {
        try {
            apiService.sendNotificationApi(token = token, title = content, body = title)
            Log.d(TAG, "Send notification  Successfully from data")
        } catch (e: Exception) {
            Log.d(TAG, "Unable to send notification")
            throw e
        }
    }

//

    //Deprecated
    override suspend fun getMyMessages(currentChatId: String): List<MessageReceived> {
        try {
            val messages = coroutineScope {
                val mess = async { apiService.getMessagesForChat(currentChatId) }
                mess.await()
            }
            return messages.sortedBy { t -> t.timeStamp }
        } catch (e: Exception) {
            Log.e(TAG, "Unable to get the messages : $e")
//            throw e
            return listOf(
                MessageReceived(
                    contentType = ContentType.text,
                    content = e.message.toString(),
                    senderId = "ErrorSerious",
                    status = messageStatus.Error
                )
            )
        }

    }

    override suspend fun getLiveMessages(
        chatId: String,
        secureAESKey: String,
        onMessagesChange: (List<MessageReceived>) -> Unit,
        onError: (e: Exception) -> Unit,
        onAdd: (MessageReceived) -> Unit
    ) {
        apiService.getLiveMessagesForChat(
            chatId,
            onChange = { messages ->
                messages.map { t ->
                    t.copy(
                        content = String(
                            encryptionService.aesDecrypt(
                                encryptedData = encryptionService.stringToByteArray(
                                    t.content
                                ), secretKey = encryptionService.stringToAESKey(secureAESKey)
                            )
                        )
                    )
                }
                onMessagesChange(messages)
            },
            onAdd = { message ->
                val newMessage = message.copy(
                    content = String(
                        encryptionService.aesDecrypt(
                            encryptedData = encryptionService.stringToByteArray(
                                message.content
                            ), secretKey = encryptionService.stringToAESKey(secureAESKey)
                        )
                    )
                )
                onAdd(newMessage)
            },
            onError
        )
    }

    override suspend fun deleteChat(chatId: String) {
        try {
            apiService.deleteChat(chatId)
        } catch (e: Exception) {
            Log.d(TAG, "Error in data delete chatId: $chatId : $e")
        }
    }

    override suspend fun stopLiveMessages() {
        apiService.stopLiveMessages()
    }

    override suspend fun stopLiveChat() {
        apiService.stopLiveChats()
    }

    override suspend fun getDataChat(chatId: String, chatName: String): ChatOrGroup {
        try {
            //Adding this secureAes with chatID to device storage so if present then don't get the key from firestore
            val keyFlow = localKeyStorge.getPrivateKey(keyId = 1)
            val privateKey = keyFlow.firstOrNull()?.privateKey
            val chatData = coroutineScope {
                val cD = async { apiService.getChatData(chatId, chatName) }
                cD.await()
            }
            if (chatData.isGroup) {
                for (members in chatData.members) {
                    if (members != chatName) {
                        val userInfo = coroutineScope {
                            val info = async { apiService.getUserChatData(members) }
                            info.await()
                        }
                        chatData.membersData.add(userInfo)
                    }
                }
                Log.d(TAG, "Group data is : ${chatData.chatName} , ${chatData.chatId}")
                val secureAESKeyFlow = localChatKeysStorage.getAESKeyById(chatId.toInt())
                var secureAES: String = ""
                if (secureAESKeyFlow.firstOrNull()?.decryptedASEKey == null) {
                    Log.d(TAG, "Adding key to backend for current chat")
                    secureAES = encryptionService.aesKeyToString(
                        encryptionService.decryptAESKeyWithPrivateKey(
                            encryptedAESKey = encryptionService.stringToByteArray(chatData.secureAESKey),
                            privateKey = encryptionService.stringToPrivateKey(privateKey ?: "")
                        )
                    )
                    localChatKeysStorage.saveAESKey(
                        ChatOrGroupAESKeys(
                            chatId = chatData.chatId.toInt(),
                            decryptedASEKey = secureAES,
                            chatName = chatData.chatName
                        )
                    )
                } else {
                    Log.d(TAG, "No key to backend for current chat")
                    secureAES = secureAESKeyFlow.firstOrNull()?.decryptedASEKey ?: ""
                }


                return chatData.copy(
                    secureAESKey = secureAES
                )
            } else {
                var tempChatName = ""
                for (members in chatData.members) {
                    if (members != chatName) {
                        val userInfo = coroutineScope {
                            val info = async { apiService.getUserChatData(members) }
                            info.await()
                        }
//                        Log.d(TAG, "Chat data is : $userInfo")
                        chatData.membersData.add(userInfo)
                        tempChatName = userInfo.username
                    }
                }
                val secureAESKeyFlow = localChatKeysStorage.getAESKeyById(chatId.toInt())
                var secureAES: String = ""
                if (secureAESKeyFlow.firstOrNull()?.decryptedASEKey == null) {
                    Log.d(TAG, "Adding key to backend for chat : ${chatId}")
                    secureAES = encryptionService.aesKeyToString(
                        encryptionService.decryptAESKeyWithPrivateKey(
                            encryptedAESKey = encryptionService.stringToByteArray(chatData.secureAESKey),
                            privateKey = encryptionService.stringToPrivateKey(privateKey ?: "")
                        )
                    )
                    localChatKeysStorage.saveAESKey(
                        ChatOrGroupAESKeys(
                            chatId = chatData.chatId.toInt(),
                            decryptedASEKey = secureAES,
                            chatName = tempChatName
                        )
                    )
                } else {
                    Log.d(TAG, "No key to backend")
                    secureAES = secureAESKeyFlow.firstOrNull()?.decryptedASEKey ?: ""
                }
                Log.d(TAG, "Chat data is : ${chatData.chatName} , ${chatData.chatId}")
                return chatData.copy(
                    chatName = tempChatName,
                    secureAESKey = secureAES
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unable to get the chat data : $e")
            return ChatOrGroup(
                chatName = "",
                chatPic = e.message.toString()
            )
        }
    }

    override suspend fun getAESKeyForChatID(chatId: String): String {
        try {
            Log.d(
                TAG,
                "started getting data for chatId : ${chatId} and username ${authServie.getCurrentUsername()}"
            )
            if (localChatKeysStorage.getAESKeyById(chatId.toInt())
                    .firstOrNull()?.decryptedASEKey != null
            ) {
                Log.d(TAG, "Key for notification got from Device")
                return localChatKeysStorage.getAESKeyById(chatId.toInt())
                    .firstOrNull()?.decryptedASEKey.toString()
            } else {
                Log.d(TAG, "Adding Key to Device and getting key for you")
                val keyFlow = localKeyStorge.getPrivateKey(keyId = 1)
                val privateKey = keyFlow.firstOrNull()?.privateKey
                val currUsername = authServie.getCurrentUsername();
                val privateDate = coroutineScope {
                    val insideKey = async() {
                        apiService.getChatData(
                            chatId = chatId,
                            username = currUsername
                        )
                    }
                    insideKey.await()
                }
                Log.d(TAG, "Got the key : ${privateKey}")
//                            Log.d(TAG,"Adding key to backend for all chats in all chats : ${newChat.secureAESKey}")
                val secureAES = encryptionService.aesKeyToString(
                    encryptionService.decryptAESKeyWithPrivateKey(
                        encryptedAESKey = encryptionService.stringToByteArray(privateDate.secureAESKey),
                        privateKey = encryptionService.stringToPrivateKey(privateKey ?: "")
                    )
                )
                var chatName = privateDate.chatName
                if (!privateDate.isGroup) {
                    for (members in privateDate.members) {
                        if (members != currUsername) {
//                            val userInfo = coroutineScope {
//                                val info = async { apiService.getUserChatData(members) }
//                                info.await()
//                            }
////                        Log.d(TAG, "Chat data is : $userInfo")
//                            chatData.membersData.add(userInfo)
                            chatName = privateDate.chatName
                        }
                    }
                }
                localChatKeysStorage.saveAESKey(
                    ChatOrGroupAESKeys(
                        chatId = chatId.toInt(),
                        decryptedASEKey = secureAES,
                        chatName = chatName
                    )
                )
                return secureAES
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting the keys ${e}")
            return "error"
        }
    }

    override suspend fun getAllDeviceChats(): List<ChatOrGroup>? {
        val chats = localChatKeysStorage.getAllAESKeys().firstOrNull()
        return chats?.map { chat ->
            ChatOrGroup(
                chatId = chat.chatId.toString(),
                chatName = chat.chatName,
                secureAESKey = chat.decryptedASEKey
            )

        }
    }
}