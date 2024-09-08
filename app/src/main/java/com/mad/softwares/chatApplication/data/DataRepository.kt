package com.mad.softwares.chatApplication.data

import android.util.Log
import com.google.firebase.auth.FirebaseUser
import com.mad.softwares.chatApplication.network.AuthenticationApi
import com.mad.softwares.chatApplication.network.FirebaseApi
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

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

    suspend fun getSearchUsers(members:List<String>, search:String):List<chatUser>

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

    suspend fun sendMessage(message: MessageReceived, chatId: String)

    suspend fun sendNotificationToToken(token: String, title: String, content: String)

//    suspend fun getTokenForMemebers(members: List<String>, currentFcmToken: String): List<String>

    suspend fun getMyMessages(currentChatId: String): List<MessageReceived>

    suspend fun deleteChat(chatId: String)

    suspend fun getDataChat(chatId: String, chatName: String): ChatOrGroup

    suspend fun getLiveMessages(
        chatId: String,
        onMessagesChange: (List<MessageReceived>) -> Unit,
        onError: (e: Exception) -> Unit
    )

    suspend fun stopLiveMessages()
}

class NetworkDataRepository(
    private val apiService: FirebaseApi,
    private val authServie: AuthenticationApi
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
                        currUser = user,
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
        coroutineScope {
            currentUser =
                authServie.loginUser(
                    email = user.username,
                    password = user.password,
                    status = { statusDeferred.complete(it) }
                )
            val status = statusDeferred.await()

            if (status == "success") {
                try {
                    Log.d(TAG, "Login success")
                    apiService.loginAndUpdateUserToDatabase(
                        currUser = user,
                        currFcmToken = currFcmToken,
                        docId = currentUser?.uid ?: "garbage"
                    )
                    toReturn = true
                } catch (e: Exception) {
                    Log.d(TAG, "Login failed in data repo : $e")
                    toReturn = false
                }
            } else {
                toReturn = false
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

    override suspend fun getCurrentUser(): User {

//        val curUser:User
        try {
            val uid = authServie.getCurrentUser()
            val curUser = coroutineScope {
                val currentUser = async { apiService.getUserFromUid(uid) }
                currentUser.await()
            }
            return curUser
        } catch (e: Exception) {
            Log.d(TAG, "Unable to get the current user")
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
        val newMembers = mutableListOf<String>(currentUser.username)

        for (user in memberUsers) {
            newMembers.add(user)
        }
        Log.d(TAG, "Chat addition startd at data")

        try {
            apiService.createNewChat(
                members = newMembers,
                chatName = chatName,
                chatId = chatId,
                profilePhoto = profilePhoto,
                isGroup = isGroup
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

    override suspend fun sendMessage(message: MessageReceived, chatId: String) {
        try {
            val staus =
                apiService.sendNewMessage(message, chatId = chatId)

            if (!staus) {
                throw Exception("Unable to send message")
            }
            Log.d(TAG, "send successfully from data ")
//            return status
        } catch (e: Exception) {
            Log.e(TAG, "unabe to send message to database from data")
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
        onMessagesChange: (List<MessageReceived>) -> Unit,
        onError: (e: Exception) -> Unit
    ) {
        apiService.getLiveMessagesForChat(chatId, onMessagesChange, onError)
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

    override suspend fun getDataChat(chatId: String, chatName: String): ChatOrGroup {
        try {
            val chatData = coroutineScope {
                val cD = async { apiService.getChatData(chatId) }
                cD.await()
            }
            if (chatData.isGroup) {
                for (members in chatData.members) {
                    if(members != chatName){
                        val userInfo = coroutineScope {
                            val info = async { apiService.getUserChatData(members) }
                            info.await()
                        }
                        chatData.membersData.add(userInfo)
                    }
                }
                Log.d(TAG, "Group data is : ${chatData.chatName} , ${chatData.chatId}")
                return chatData
            } else {
                var tempChatName = ""
                for (members in chatData.members) {
                    if (members == chatName) {
                        val userInfo = coroutineScope {
                            val info = async { apiService.getUserChatData(members) }
                            info.await()
                        }
                        Log.d(TAG, "Chat data is : $userInfo")
                        chatData.membersData.add(userInfo)
                        tempChatName = userInfo.username
                    }
                }
                Log.d(TAG, "Chat data is : ${chatData.chatName} , ${chatData.chatId}")
                return chatData.copy(chatName = tempChatName)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unable to get the chat data : $e")
            return ChatOrGroup(
                chatName = "",
                chatPic = e.message.toString()
            )
        }
    }
}