package com.mad.softwares.chatApplication.network

import android.os.Message
import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Source
import com.google.firebase.messaging.FirebaseMessaging
import com.mad.softwares.chatApplication.data.AESKeyData
import com.mad.softwares.chatApplication.data.ChatOrGroup
import com.mad.softwares.chatApplication.data.ContentType
import com.mad.softwares.chatApplication.data.MessageReceived
import com.mad.softwares.chatApplication.data.PublicRSAKey
import com.mad.softwares.chatApplication.data.User
import com.mad.softwares.chatApplication.data.chatUser
import com.mad.softwares.chatApplication.data.lastMessage
import com.mad.softwares.chatApplication.data.messageStatus
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


val TAG = "ApiServiceLog"

interface FirebaseApi {
    suspend fun getFCMToken(): String


    suspend fun authenticateWithUniqueId(uniqueId: String): Boolean

    //    suspend fun getUserFromTokenInDatabase(Token:String):User
    suspend fun loginAndUpdateUserToDatabase(
        currUser: User,
        currFcmToken: String,
        docId: String
    ): User

    suspend fun registerUserToDatabase(
        currUser: User,
        docId: String
    ): User

    suspend fun logoutUser(
        currUser: User
    )

    suspend fun getUserFromUid(uid: String): User

    suspend fun resetPassword(
        username: String,
        uniqueId: String,
        newPassword: String
    ): List<String>

    suspend fun sendNotificationApi(token: String, title: String, body: String)

    suspend fun getAllUsers(): List<chatUser>

    suspend fun createNewChat(
        members: List<String>,
        chatName: String,
        chatId: String,
        profilePhoto: String = "",
        isGroup: Boolean = false,
        encryptedAESKeys: List<AESKeyData>,
        recentMessage: String
    )

    suspend fun getSearchUsers(searchUser: String): List<chatUser>

    suspend fun getChatsforMe(username: String): List<ChatOrGroup>

    suspend fun getGroupsForMe(username: String): List<ChatOrGroup>

    suspend fun sendNewMessage(
        message: MessageReceived,
        chatId: String,
        recentMessage: String
    ): String

//    suspend fun getTokenForMemebers(members:List<String>):List<String>

    suspend fun getMessagesForChat(currentChatId: String): List<MessageReceived>

    suspend fun deleteChat(chatId: String)

    suspend fun getUserChatData(username: String): chatUser

    suspend fun getChatData(chatId: String, username: String): ChatOrGroup

    suspend fun getLiveMessagesForChat(
        currentChatId: String,
        onChange: (MessageReceived) -> Unit,
        onAdd: (MessageReceived) -> Unit,
        onError: (e: Exception) -> Unit,
        onDelete: (MessageReceived) -> Unit
    )

    suspend fun getLiveChatsOrGroups(
        username: String,
        isGroup: Boolean,
        onAddChat: (ChatOrGroup) -> Unit,
        onModifiedChat: (ChatOrGroup) -> Unit,
        onDeleteChat: (ChatOrGroup) -> Unit,
        onError: (e: Exception) -> Unit
    )

    suspend fun stopLiveChats()

    suspend fun stopLiveMessages()

    suspend fun getPublicRSAKeyForMember(listOfMembers: List<String>): List<PublicRSAKey>

    suspend fun getEncryptedAESKeyForChatSpecialCase(chatId: String): String

    suspend fun getPrivateAESKey(chatId: String, username: String): String

    suspend fun deleteMessage(messageId: String, chatId: String, error: (e: Exception) -> Unit, newDeletedMessage:String)

}

class NetworkFirebaseApi(
    val db: FirebaseFirestore,
    val userCollection: CollectionReference,
    val chatsCollection: CollectionReference,
    val messagesCollection: CollectionReference,
) : FirebaseApi {
    override suspend fun getFCMToken(): String {
        return withContext(Dispatchers.IO) {
            return@withContext suspendCoroutine<String> { continuation ->
                FirebaseMessaging.getInstance().token
                    .addOnCompleteListener(OnCompleteListener { task ->
                        if (!task.isSuccessful) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                            continuation.resume("")
                            return@OnCompleteListener
                        }

                        val token = task.result
                        continuation.resume(token ?: "Invalid")
                        Log.d(TAG, "token: $token")
                    })
                    .addOnFailureListener {
                        Log.e(TAG, "error to get fcm token $it")
                    }

            }
        }
    }


    //deprecated
    override suspend fun authenticateWithUniqueId(uniqueId: String): Boolean {
        return try {
            val result = userCollection
                .whereEqualTo("uniqueId", uniqueId)
                .get(Source.SERVER)
                .await()
            Log.d(TAG, "${result.documents} login success")
            result.isEmpty
        } catch (e: Exception) {
            Log.d(TAG, "Backend problem")
            false
        }
    }

    override suspend fun getUserFromUid(uid: String): User {
        val resultUser = userCollection
            .document(uid)
            .get()

        lateinit var user: User
        try {
            val doc = resultUser.await()
            val username = doc.getString("username") ?: ""
            val profilePic = doc.getString("profilePic") ?: ""
            val uniqueId = doc.getString("uniqueId") ?: ""
            val publicRSAKey = doc.getString("publicRSAKey") ?: ""
            val privateEncryptedRSAKey = doc.getString("privateEncryptedRSAKey") ?: ""
            val salt = doc.getString("salt") ?: ""
            val docId = doc.id

            user = User(
                username = username,
                profilePic = profilePic,
                uniqueId = uniqueId,
                docId = docId,
                publicRSAKey = publicRSAKey,
//                privateEncryptedRSAKey = privateEncryptedRSAKey,
                salt = salt
            )

        } catch (e: Exception) {
            Log.e(TAG, "Unable to get user from uid : $e")
        }

//        resultUser.await().getString("username")
        return user
    }

    override suspend fun loginAndUpdateUserToDatabase(
        currUser: User,
        currFcmToken: String,
        docId: String
    ): User {

        if (docId == "garbage") {
            Log.d(TAG, "Doc id is garbage")
            return User()
        }

        Log.d(TAG, "Update started in api ")
        val updateUser = userCollection
            .document(docId)
            .update("fcmTokens", FieldValue.arrayUnion(currFcmToken))

        val getSecureKeys = userCollection
            .document(docId)
            .get()

        try {
            val importantData = getSecureKeys.await()
        } catch (e: Exception) {
            Log.e(TAG, "Unable to get the keys : $e")
        }

        try {
            updateUser.await()
        } catch (e: Exception) {
            Log.d(TAG, "Unable to add te user : $e")
        }
        Log.d(TAG, "Update ended in the api")
        return User(
            fcmToken = currFcmToken,
            profilePic = currUser.profilePic,
            uniqueId = currUser.uniqueId,
            username = currUser.username,
            publicRSAKey = getSecureKeys.await().getString("publicRSAKey") ?: "",
            privateEncryptedRSAKey = getSecureKeys.await().getString("privateEncryptedRSAKey")
                ?: "",
            salt = getSecureKeys.await().getString("salt") ?: "",
            docId = currUser.docId
        )


    }

    override suspend fun registerUserToDatabase(
        currUser: User,
        docId: String
    ): User {
        Log.d(TAG, "updateCalled here")
//        val docId = mutableListOf<String>()
        val sub = "no ID"
        val newTokens: ArrayList<String> = arrayListOf(currUser.fcmToken)

        if (docId == "garbage") {
            Log.d(TAG, "Doc id is garbage")
            return User()
        }

        val user = hashMapOf(
            "fcmTokens" to newTokens,
//            "password" to currUser.password,
            "profilePic" to currUser.profilePic,
            "uniqueId" to currUser.uniqueId,
            "username" to currUser.username,
            "publicRSAKey" to currUser.publicRSAKey,
            "privateEncryptedRSAKey" to currUser.privateEncryptedRSAKey,
            "salt" to currUser.salt
        )

        val newUserId = userCollection
            .document(docId)
            .set(user)
            .addOnSuccessListener {
                Log.d(TAG, "registration success doc id : ${currUser.docId}")
//                docId.add(it.)
            }
            .addOnFailureListener { e ->
                Log.d(TAG, "error adding because :", e)
                throw e
            }
        try {
            newUserId.await()
        } catch (e: Exception) {
            Log.d(TAG, "Unable to add the user : $e")
        }

        val newUser: User = User(
            currUser.fcmToken,
            currUser.profilePic,
            currUser.uniqueId,
            currUser.username,
            docId = docId,
            publicRSAKey = currUser.publicRSAKey,
            privateEncryptedRSAKey = currUser.privateEncryptedRSAKey,
            salt = currUser.salt
        )
        Log.d(TAG, "User details are : ${newUserId.await().toString()}")
        return newUser

    }

    override suspend fun logoutUser(currUser: User) {
        val fcmToken = getFCMToken()
        Log.d(TAG, "Logout started for userId: ${currUser.docId} with fcm: $fcmToken")
        val updateUser = userCollection
            .document(currUser.docId)
            .update("fcmTokens", FieldValue.arrayRemove(fcmToken))

        try {
            updateUser.await()
            Log.d(TAG, "Logout successful : ${updateUser.await()}")
        } catch (e: Exception) {
            Log.d(TAG, "Unable to logout the user : $e")
            throw e
        }
    }

    //deprecated
    override suspend fun resetPassword(
        username: String,
        uniqueId: String,
        newPassword: String
    ): List<String> {

        val list = mutableListOf<String>()
        Log.d(TAG, "reset Started api")
        try {

            val querySnapshot = userCollection
                .whereEqualTo("username", username)
                .whereEqualTo("uniqueId", uniqueId)
                .get()
//                .await()
            Log.d(TAG, "query : ${querySnapshot.await().documents}")
            for (document in querySnapshot.await()) {
                Log.d(TAG, "Updated user with id ${document.id}")
                userCollection
                    .document(document.id)
                    .update("password", newPassword, "fcmTokens", arrayListOf<String>())
                list.add(document.id)
            }
            Log.d(TAG, "reset Success api")
        } catch (e: Exception) {
            Log.d(TAG, "reset failed api")
            return listOf()
        }

        return list
    }

    //deprecated
    override suspend fun sendNotificationApi(token: String, title: String, body: String) {

        Log.d(TAG, "TOKEN IS $token")

        Log.d(TAG, "Notification trigger in api")

        val notificationRequest = NotificationRequest(
            to = token,
            notification = Notification(title, body)
        )

        try {
//            val response = service.sendNotification(notificationRequest)/

//            Log.d(TAG, "Successfully send notification api : ${response.code()}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send notification : ${e.message}")
        }
    }

    override suspend fun getAllUsers(): List<chatUser> {
        Log.d(TAG, "started getting the users")
        val users = mutableListOf<chatUser>()
        try {
            val allUsers = userCollection
//                .whereNotEqualTo("username", username)
                .get()
            for (doc in allUsers.await().documents) {
//                val fcmToken = doc.getString("fcmToken") ?: ""

                val profilePic = doc.getString("profielPic") ?: ""
                val usern = doc.getString("username") ?: ""

//            val docId = doc.id
//            val user = User(fcmToken, profilePic, "uniqueId", usern, docId)
                val user = chatUser(
                    username = usern, profilePic = profilePic,
//                    fcmToken = fcmToken
                )

                Log.d(TAG, "Got user with id : ${user.fcmToken}")
                users.add(user)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unable to get the users : $e")
            throw e
        }
        Log.d(TAG, "got users api")
        return users
    }

    override suspend fun getSearchUsers(searchUser: String): List<chatUser> {
        Log.d(TAG, "started getting the search users for you...")
        val users = mutableListOf<chatUser>()
        try {
            val allUsers = userCollection
//                .whereNotEqualTo("username", username)
                .whereGreaterThanOrEqualTo("username", searchUser)
                .whereLessThanOrEqualTo("username", "$searchUser\uf8ff")
                .limit(10)
                .get()
            for (doc in allUsers.await().documents) {
                val fcmToken = doc.getString("fcmToken") ?: ""

                val profilePic = doc.getString("profielPic") ?: ""
                val usern = doc.getString("username") ?: ""

//            val docId = doc.id
//            val user = User(fcmToken, profilePic, "uniqueId", usern, docId)
                val user = chatUser(
                    username = usern, profilePic = profilePic,
//                    fcmToken = fcmToken
                )

                Log.d(TAG, "Got user with id : ${user.fcmToken}")
                users.add(user)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unable to get the users : $e")
            throw e
        }
        Log.d(TAG, "got users api")
        return users
    }

    override suspend fun createNewChat(
        members: List<String>,
        chatName: String,
        chatId: String,
        profilePhoto: String,
        isGroup: Boolean,
        encryptedAESKeys: List<AESKeyData>,
        recentMessage: String
    ) {
//        val newMembers = members.toTypedArray()
        val newMembers: ArrayList<String> = ArrayList(members)
        Log.d(TAG, "Chat addition started api")
        val newAESKeys: List<HashMap<String, String>> =
            encryptedAESKeys.map { aesKey ->
                hashMapOf(
                    "username" to aesKey.username,
                    "key" to aesKey.key
                )
            }


        val newChat = hashMapOf(
            "chatId" to chatId,
            "chatName" to chatName,
            "profilePhoto" to profilePhoto,
            "isGroup" to isGroup,
            "members" to newMembers,
            "lastMessage" to listOf(recentMessage, Timestamp.now()),
            "encryptedAESKeys" to newAESKeys
        )

        chatsCollection
            .document(chatId)
            .set(newChat)
            .addOnSuccessListener {
                Log.d(TAG, "added chat successfully")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to add to chat : $e")
                throw e
            }
    }

    //Deprecated
    override suspend fun getChatsforMe(username: String): List<ChatOrGroup> {
        val chats = mutableListOf<ChatOrGroup>()
        Log.d(TAG, "Api the chats for usr : ${username}")
        val myChats = chatsCollection
            .whereArrayContains("members", username)
            .whereEqualTo("isGroup", false)
            .get()
            .addOnSuccessListener {
                Log.d(TAG, "Got the chats for usr : ${username}")

            }
            .addOnFailureListener { e ->
                Log.d(TAG, "Unable to fetch the chats : $e")
            }
        for (doc in myChats.await().documents) {
            val chatId = doc.getString("chatId") ?: ""
            val chatName = doc.getString("chatName") ?: ""
            val isGroup = doc.getBoolean("isGroup") ?: false
            val members: List<Any> = doc.get("members") as List<Any>
            val lastMsg: List<Any> = doc.get("lastMessage") as List<Any>
            val profilePhoto = doc.getString("profilePhoto") ?: ""
            val encryptedAESKeys = doc.get("encryptedAESKeys") as List<*>
            Log.d(TAG, "members are : $members")
            val singleChat = ChatOrGroup(
                chatId = chatId,
                chatName = chatName,
                isGroup = isGroup,
                members = members.map { it.toString() },
                lastMessage = lastMessage(
                    content = lastMsg[0].toString(),
                    timestamp = lastMsg[1] as Timestamp
                )
            )
            chats.add(singleChat)
        }

        Log.d(TAG, "Chats in api are ${chats.size}")
        return chats
    }

    //Deprecated
    override suspend fun getGroupsForMe(username: String): List<ChatOrGroup> {
        val chats = mutableListOf<ChatOrGroup>()
        Log.d(TAG, "Api the chats for usr : ${username}")
        val myChats = chatsCollection
            .whereArrayContains("members", username)
            .whereEqualTo("isGroup", true)
            .get()
            .addOnSuccessListener {
                Log.d(TAG, "Got the chats for usr : ${username}")

            }
            .addOnFailureListener { e ->
                Log.d(TAG, "Unable to fetch the chats : $e")
            }
        for (doc in myChats.await().documents) {
            val chatId = doc.getString("chatId") ?: ""
            val chatName = doc.getString("chatName") ?: ""
            val isGroup = doc.getBoolean("isGroup") ?: false
            val members: List<Any> = doc.get("members") as List<Any>
            val lastMsg: List<Any> = doc.get("lastMessage") as List<Any>
            Log.d(TAG, "members are : $members")
            val singleChat = ChatOrGroup(
                chatId = chatId,
                chatName = chatName,
                isGroup = isGroup,
                members = members.map { it.toString() },
                lastMessage = lastMessage(
                    content = lastMsg[0].toString(),
                    timestamp = lastMsg[1] as Timestamp
                )
            )
            chats.add(singleChat)
        }

        Log.d(TAG, "Chats in api are ${chats.size}")
        return chats
    }

    override suspend fun getUserChatData(username: String): chatUser {
        val userInfo = userCollection
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener {
                Log.d(TAG, "Got the data")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Unable to get the data")
                throw e
            }

        try {
            val result = userInfo.await()
            val document = result.documents[0]
//            val username = username
//            val fcmToken:List<Any> = result.documents[0].get("fcmTokens") as List<Any>?: listOf()
            val fcmTokens: List<String> = document.get("fcmTokens") as? List<String> ?: listOf()
            val profilePic = result.documents[0].getString("profilePic") ?: ""
            Log.d(TAG, "Got the user data, profilePic:${profilePic} fcmtokens : $fcmTokens")
            return chatUser(username, fcmTokens.map { it.toString() }, profilePic)
        } catch (e: Exception) {
            Log.e(TAG, "Unable to get the user data : $e")
            return chatUser(profilePic = e.message.toString())
//            throw e
        }
    }

    override suspend fun getChatData(chatId: String, username: String): ChatOrGroup {
        Log.d(TAG, "Getting chat data Started.")
        val chatData = chatsCollection.document(chatId).get()
            .addOnSuccessListener {
                Log.d(TAG, "Got the chat data")
            }
            .addOnFailureListener {
                Log.e(TAG, "Unable to get the chat data : $it")
//                throw it
                return@addOnFailureListener
            }
        val chat = chatData.await()
        val encryptedAESKey = chat.get("encryptedAESKeys") as List<HashMap<String, String>>
        val myEncryptedAESKey = encryptedAESKey?.filter { key ->
            key["username"] == username
        }?.get(0)
        return ChatOrGroup(
            chatId = chat.getString("chatId") ?: "",
            chatName = chat.getString("chatName") ?: "",
            isGroup = chat.getBoolean("isGroup") ?: false,
            members = chat.get("members") as List<String>,
            secureAESKey = myEncryptedAESKey?.get("key").toString()
        )
    }


    override suspend fun sendNewMessage(
        message: MessageReceived,
        chatId: String,
        recentMessage: String
    ): String {
        val newMessage = hashMapOf(
            "content" to message.content,
            "contentType" to message.contentType,
            "senderId" to message.senderId,
            "timeStamp" to message.timeStamp
        )
        var messageId = ""
        Log.d(TAG, "message sending started in api")
        val chatSuccess = CompletableDeferred<Boolean>()
//        val chatSuccess = CompletableDeferred(false)
        val result = withTimeoutOrNull(5000) {
            try {
                val lastMessage = arrayListOf(
                    recentMessage,
                    message.timeStamp,
                    message.senderId
                )


                val docREf = chatsCollection
                    .document(chatId).collection("Messages")

//                .get(timeout = 5000)
                    .add(newMessage)
                    .await()
                messageId = docREf.id
                chatsCollection.document(chatId)
                    .update("lastMessage", lastMessage)
                    .await()
//            .addOnSuccessListener {
//                Log.d(TAG,"message added successfully")
////                chatSuccess.complete(true)
//            }
//            .addOnFailureListener{e->
//                Log.e(TAG,"failed to add to the database : $e")
////                chatSuccess.complete(false)
//                return@addOnFailureListener
//            }
                Log.d(TAG, "message added successfully")

//        Log.d(TAG,"new message will be addes hope so..")


                Log.d(TAG, "Added lastMessage to: $chatId")

//            chatSuccess.complete(true)
                true
//            .addOnSuccessListener {
//                Log.d(TAG,"Added lastMessage to : $chatId")
//
////                chatSuccess.complete(true)
//            }
//            .addOnFailureListener { e->
//                Log.e(TAG,"Unable to add the last Message :$chatId  error : ${e}")
////                chatSuccess.complete(false)
//                return@addOnFailureListener
//             }
//        chatsCollection.document(message.chatId).collection("Messages")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send message or update lastMessage: $e")
                messageId = ""
//            chatSuccess.complete(false)
                false
            }
        }
        return messageId
//        return chatSuccess.await()
//        chatSuccess.await()
    }


    //Deprecated
    override suspend fun getMessagesForChat(currentChatId: String): List<MessageReceived> {
        val messages = mutableListOf<MessageReceived>()

        Log.d(TAG, "message getting started in api")
        val messageGet = chatsCollection.document(currentChatId).collection("Messages")
            .get(Source.SERVER)
            .addOnSuccessListener {
                Log.d(TAG, "message got successfully")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to get Messages : $e")
                return@addOnFailureListener
            }

        for (doc in messageGet.await()) {
            val content = doc.getString("content") ?: ""
//            val chatId = doc.getString("chatId")?:""
            val senderId = doc.getString("senderId") ?: ""
            val timeStamp = doc.getTimestamp("timeStamp") ?: Timestamp.now()

            val mess = MessageReceived(
                content = content,
                contentType = ContentType.text,
                senderId = senderId,
                timeStamp = timeStamp
            )
            messages.add(mess)
        }

        return messages
    }

    private var listenerRegistration: ListenerRegistration? = null

    override suspend fun getLiveMessagesForChat(
        currentChatId: String,
        onChange: (MessageReceived) -> Unit,
        onAdd: (MessageReceived) -> Unit,
        onError: (e: Exception) -> Unit,
        onDelete: (MessageReceived) -> Unit
    ) {
        listenerRegistration = chatsCollection.document(currentChatId).collection("Messages")
            .addSnapshotListener() { snapShot, e ->
                if (e != null) {
                    onError(e)
                    return@addSnapshotListener
                }

                if (snapShot != null && !snapShot.metadata.hasPendingWrites()) {
                    for (dc in snapShot!!.documentChanges) {
                        when (dc.type) {
                            DocumentChange.Type.ADDED -> {
                                Log.d(TAG, "New Message: ${dc.document.data}")
                                val messages = mutableListOf<MessageReceived>()
                                if (dc.document.data != null && dc.document.data.isNotEmpty()) {
//                                for (doc in dc.document.data ) {
//                                    val content = doc.getString("content") ?: ""
////            val chatId = doc.getString("chatId")?:""
//                                    val senderId = doc.getString("senderId") ?: ""
//                                    val timeStamp = doc.getTimestamp("timeStamp") ?: Timestamp.now()
//
//                                    val mess = MessageReceived(
//                                        content,
//                                        contentType = ContentType.text,
//                                        senderId,
//                                        timeStamp
//                                    )
////                        Log.d(TAG,"Got the message : ${mess.content}")
//                                    messages.add(mess)
//                                }
                                    val mess = MessageReceived(
                                        messageId = dc.document.id,
                                        content = dc.document.data["content"].toString(),
                                        contentType = when (dc.document.data["contentType"].toString()) {
                                            "text" -> ContentType.text
                                            "image" -> ContentType.image
                                            "document" -> ContentType.document
                                            "audio" -> ContentType.audio
                                            "video" -> ContentType.video
                                            "deleted" -> ContentType.deleted
                                            else -> {
                                                ContentType.default
                                            }
                                        },
                                        senderId = dc.document.data["senderId"].toString(),
                                        timeStamp = dc.document.data["timeStamp"] as Timestamp
                                            ?: Timestamp.now(),
                                        status = messageStatus.Send
                                    )
//                                onChange(messages.sortedBy { it.timeStamp })
                                    onAdd(mess)
                                }
                            }

                            DocumentChange.Type.MODIFIED -> {
                                Log.d(TAG, "Modified Message: ${dc.document.data}")
                                val mess = MessageReceived(
                                    messageId = dc.document.id,
                                    content = dc.document.data["content"].toString(),
                                    contentType = when (dc.document.data["contentType"].toString()) {
                                        "text" -> ContentType.text
                                        "image" -> ContentType.image
                                        "document" -> ContentType.document
                                        "audio" -> ContentType.audio
                                        "video" -> ContentType.video
                                        "deleted" -> ContentType.deleted
                                        else -> {
                                            ContentType.default
                                        }
                                    },
                                    senderId = dc.document.data["senderId"].toString(),
                                    timeStamp = dc.document.data["timeStamp"] as Timestamp
                                        ?: Timestamp.now(),
                                    status = messageStatus.Send
                                )
//                                onChange(messages.sortedBy { it.timeStamp })
                                onChange(mess)
                            }

                            DocumentChange.Type.REMOVED -> {
                                Log.d(
                                    TAG,
                                    "Removed Message: ${dc.document.data}"

                                )
                                val mess = MessageReceived(
                                    messageId = dc.document.id,
                                    content = dc.document.data["content"].toString(),
                                    contentType = when (dc.document.data["contentType"].toString()) {
                                        "text" -> ContentType.text
                                        "image" -> ContentType.image
                                        "document" -> ContentType.document
                                        "audio" -> ContentType.audio
                                        "video" -> ContentType.video
                                        "deleted" -> ContentType.deleted
                                        else -> {
                                            ContentType.default
                                        }
                                    },
                                    senderId = dc.document.data["senderId"].toString(),
                                    timeStamp = dc.document.data["timeStamp"] as Timestamp
                                        ?: Timestamp.now(),
                                    status = messageStatus.Send
                                )
                                onDelete(mess)
                            }
                        }
                    }
                }

//                val messages = mutableListOf<MessageReceived>()
//                if (snapShot!=null && !snapShot.isEmpty){
//                    for (doc in snapShot.documents) {
//                        val content = doc.getString("content") ?: ""
////            val chatId = doc.getString("chatId")?:""
//                        val senderId = doc.getString("senderId") ?: ""
//                        val timeStamp = doc.getTimestamp("timeStamp") ?: Timestamp.now()
//
//                        val mess = MessageReceived(
//                            content,
//                            contentType = ContentType.text,
//                            senderId,
//                            timeStamp
//                        )
////                        Log.d(TAG,"Got the message : ${mess.content}")
//                        messages.add(mess)
//                    }
//                    onChange(messages.sortedBy { it.timeStamp })
            }
    }


    private var listenChats: ListenerRegistration? = null

    override suspend fun getLiveChatsOrGroups(
        username: String,
        isGroup: Boolean,
        onAddChat: (ChatOrGroup) -> Unit,
        onModifiedChat: (ChatOrGroup) -> Unit,
        onDeleteChat: (ChatOrGroup) -> Unit,
        onError: (e: Exception) -> Unit
    ) {
        listenChats = null
        listenChats = chatsCollection
            .whereArrayContains("members", username)
            .whereEqualTo("isGroup", isGroup)
//            .get()
//            .addOnSuccessListener {
//                Log.d(TAG, "Got the chats for usr : ${username}")
//
//            }
//            .addOnFailureListener { e ->
//                Log.d(TAG, "Unable to fetch the chats : $e")
//            }
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e(TAG, "Error starting listener ${e}")
                    return@addSnapshotListener
                }

                for (dc in snapshot!!.documentChanges) {
                    when (dc.type) {
                        DocumentChange.Type.ADDED -> {
                            Log.d(TAG, "New Chat: ${dc.document.data}")
//                            for ()
                            if (dc.document.data != null && dc.document.data.isNotEmpty()) {
                                val lastMsg: List<Any> =
                                    dc.document.data["lastMessage"] as List<Any>
                                val encryptedAESKey =
                                    dc.document.data["encryptedAESKeys"] as List<HashMap<String, String>>
                                val myEncryptedAESKey = encryptedAESKey?.filter { key ->
                                    key["username"] == username
                                }?.get(0)
                                Log.d(TAG, "ency key for $username is $myEncryptedAESKey")
                                val newChat = ChatOrGroup(
                                    chatId = dc.document.data["chatId"].toString(),
                                    chatName = dc.document.data["chatName"].toString(),
                                    isGroup = dc.document.data["isGroup"].toString().toBoolean(),
                                    members = dc.document.data["members"] as List<String>,
                                    chatPic = dc.document.data["chatPic"].toString(),
                                    secureAESKey = myEncryptedAESKey?.get("key").toString(),
                                    lastMessage = lastMessage(
                                        content = lastMsg[0].toString(),
                                        timestamp = lastMsg[1] as Timestamp,
                                        sender = if (2 in lastMsg.indices) {
                                            lastMsg[2].toString()
                                        } else {
                                            ""
                                        },
                                    ),
                                )
                                onAddChat(newChat)
                            }
                        }

                        DocumentChange.Type.MODIFIED -> {

                            Log.d(
                                TAG,
                                "-------Modified Chat: ${dc.document.data["chatId"].toString()}"
                            )
                            if (dc.document.data != null && dc.document.data.isNotEmpty()) {
                                val lastMsg: List<Any> =
                                    dc.document.data["lastMessage"] as List<Any>
                                val encryptedAESKey =
                                    dc.document.data["encryptedAESKeys"] as List<HashMap<String, String>>
                                val myEncryptedAESKey = encryptedAESKey?.filter { key ->
                                    key["username"] == username
                                }?.get(0)
                                val updateChat = ChatOrGroup(
                                    chatId = dc.document.data["chatId"].toString(),
                                    chatName = dc.document.data["chatName"].toString(),
                                    isGroup = dc.document.data["isGroup"].toString().toBoolean(),
                                    members = dc.document.data["members"] as List<String>,
                                    chatPic = dc.document.data["chatPic"].toString(),
                                    secureAESKey = myEncryptedAESKey?.get("key").toString(),
                                    lastMessage = lastMessage(
                                        content = lastMsg[0].toString(),
                                        timestamp = lastMsg[1] as Timestamp,
                                        sender = if (2 in lastMsg.indices) {
                                            lastMsg[2].toString()
                                        } else {
                                            ""
                                        }
                                    )
                                )
                                onModifiedChat(updateChat)
                            }

                        }

                        DocumentChange.Type.REMOVED -> {
                            Log.d(TAG, "-------Removed Chat: ${dc.document.data}")
                            if (dc.document.data != null && dc.document.data.isNotEmpty()) {
                                val lastMsg: List<Any> =
                                    dc.document.data["lastMessage"] as List<Any>

                                val updateChat = ChatOrGroup(
                                    chatId = dc.document.data["chatId"].toString(),
                                    chatName = dc.document.data["chatName"].toString(),
                                    isGroup = dc.document.data["isGroup"].toString().toBoolean(),
                                    members = dc.document.data["members"] as List<String>,
                                    chatPic = dc.document.data["chatPic"].toString(),
                                    lastMessage = lastMessage(
                                        content = lastMsg[0].toString(),
                                        timestamp = lastMsg[1] as Timestamp,
                                        sender = if (2 in lastMsg.indices) {
                                            lastMsg[2].toString()
                                        } else {
                                            ""
                                        }
                                    )
                                )
                                onDeleteChat(updateChat)
                            }
                        }
                    }
                }
            }
//        val chats = mutableListOf<ChatOrGroup>()
//        for (doc in myChats.await().documents) {
//            val chatId = doc.getString("chatId") ?: ""
//            val chatName = doc.getString("chatName") ?: ""
//            val isGroup = doc.getBoolean("isGroup") ?: false
//            val members: List<Any> = doc.get("members") as List<Any>
//            val lastMsg: List<Any> = doc.get("lastMessage") as List<Any>
//            Log.d(TAG, "members are : $members")
//            val singleChat = ChatOrGroup(
//                chatId = chatId,
//                chatName = chatName,
//                isGroup = isGroup,
//                members = members.map { it.toString() },
//                lastMessage = lastMessage(
//                    content = lastMsg[0].toString(),
//                    timestamp = lastMsg[1] as Timestamp
//                )
//            )
//            chats.add(singleChat)
//        }

//        Log.d(TAG, "Chats in api are ${chats.size}")
//        onChange(chats)
    }

    override suspend fun stopLiveChats() {
        listenChats?.remove()
        Log.d(TAG, "Removed chat listner here!!!")
    }

    override suspend fun getPublicRSAKeyForMember(listOfMembers: List<String>): List<PublicRSAKey> {
        val membersBatch = listOfMembers.chunked(10)
        val RSAMemberKeys: MutableList<PublicRSAKey> = mutableListOf()
        membersBatch.forEach { batch ->
            val snapshot = userCollection.whereIn("username", batch)
                .get()
                .addOnSuccessListener {
                    Log.d(TAG, "Success getting the public RSA key")
//                    for(doc in it.documents){
//                        val username = doc.getString("username")?:""
//                        val key = doc.getString("publicRSAKey")?:""
//                        RSAMemberKeys.add(PublicRSAKey(username,key))
//                    }
                }
                .addOnFailureListener { err ->
                    Log.e(TAG, "Error getting the public keys : ${err}")
                    return@addOnFailureListener
                }
            val batch_documents = snapshot.await()
            for (doc in batch_documents.documents) {
                val username = doc.getString("username") ?: ""
                val key = doc.getString("publicRSAKey") ?: ""
                RSAMemberKeys.add(PublicRSAKey(username, key))
            }
        }
        return RSAMemberKeys
    }

    override suspend fun stopLiveMessages() {
        listenerRegistration?.remove()
        Log.d(TAG, "Removed the listener here!!")
    }

    override suspend fun deleteChat(chatId: String) {
        try {
            chatsCollection
                .document(chatId)
                .delete()
            Log.d(TAG, "Success to delete the chat : $chatId")
        } catch (e: Exception) {
            Log.e(TAG, "Error in api to delete chatId ${chatId} : ${e}")
            throw e
        }
    }

    override suspend fun getEncryptedAESKeyForChatSpecialCase(chatId: String): String {
        //TODO:Never to be implemented
        return ""
    }

    override suspend fun getPrivateAESKey(chatId: String, username: String): String {
        try {
            val chatData = chatsCollection
                .document(chatId)
                .get()
            val finalData = chatData.await()
            val encryptedAESKey =
                finalData.getString("encryptedAESKeys") as List<HashMap<String, String>>
            val myEncryptedAESKey = encryptedAESKey?.filter { key ->
                key["username"] == username
            }?.get(0)
            return myEncryptedAESKey?.get("key").toString()
            Log.d(TAG, "Got the api keys successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error getting the AES key : ${e}")
            return "error"
        }

    }

    override suspend fun deleteMessage(
        messageId: String,
        chatId: String,
        error: (Exception) -> Unit,
        NewDeletedMessage:String
    ) {
        val lastMessage = arrayListOf(
            NewDeletedMessage,
            Timestamp.now()
//            message.senderId
        )
        try {
            Log.d(TAG, "Deleting message: $messageId in chat: $chatId")
            chatsCollection
                .document(chatId)
                .collection("Messages")
                .document(messageId)
//                .update(
//                    "content","Deleted Message","contentType",ContentType.deleted
//                )
                .delete()
                .await()

            chatsCollection.document(chatId)
                .update("lastMessage", lastMessage)
                .await()

            Log.d(TAG, "Message Deleted Successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting message: ${e}")
            error(e)
        }
    }
}