package com.mad.softwares.chitchat.network

import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.google.firebase.messaging.FirebaseMessaging
import com.mad.softwares.chitchat.data.Chats
import com.mad.softwares.chitchat.data.MessageReceived
import com.mad.softwares.chitchat.data.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine



val TAG = "ApiServiceLog"

interface FirebaseApi {
    suspend fun getFCMToken():String

    suspend fun registerUserToDatabase(
        fcmToken:String,
        password:String,
        profilePic:String,
        uniqueId:String,
        username:String
    ):String
    suspend fun checkUsernameExist(username: String):Boolean
    suspend fun loginUserAndPasswordSuccess(username: String,password: String):List<User>
    suspend fun authenticateWithUniqueId(uniqueId: String):Boolean
    suspend fun getUserFromTokenInDatabase(Token:String):User
    suspend fun updateUserToDatabase(
        fcmToken:String,
        password:String,
        profilePic:String,
        uniqueId:String,
        username:String,
        docId:String
    )

    suspend fun updateUserToDatabase(
        currUser:User
    ):User

    suspend fun registerUserToDatabase(
        currUser: User
    ):User

    suspend fun logoutUser(
        currUser: User
    )

    suspend fun resetPassword(
        username: String,
        uniqueId: String,
        newPassword:String
    ):List<String>

    suspend fun sendNotificationApi(token:String, title:String, body:String)

    suspend fun getAllUsers(username: String):List<User>

    suspend fun createNewChat(
        members:List<String>,
        chatName:String,
        chatId:String,
        profilePhoto:String = "",
        isGroup:Boolean = false,
        )

    suspend fun getChatsforMe(username:String):List<Chats>

    suspend fun sendNewMessage(message:MessageReceived)

    suspend fun getTokenForMemebers(members:List<String>):List<String>

    suspend fun getMessagesForChat(currentChatId: String):List<MessageReceived>
}

class NetworkFirebaseApi(
    val db:FirebaseFirestore,
    val userCollection:CollectionReference,
    val chatsCollection:CollectionReference,
    val messagesCollection:CollectionReference,
):FirebaseApi{
    override suspend fun getFCMToken():String {
        return withContext(Dispatchers.IO) {
            return@withContext suspendCoroutine<String> {
                    continuation->
                FirebaseMessaging.getInstance().token
                    .addOnCompleteListener(OnCompleteListener { task ->
                        if (!task.isSuccessful) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                            continuation.resume("")
                            return@OnCompleteListener
                        }

                        val token = task.result
                        continuation.resume(token?:"")
                        Log.d(TAG, "token: $token")
                    })

            }
        }
    }

    override suspend fun registerUserToDatabase(
        fcmToken:String,
        password:String,
        profilePic:String,
        uniqueId:String,
        username:String
    ):String{
        Log.d(TAG,"updateCalled here")
        val docId = mutableListOf<String>()
        val sub = "no ID"
        val user = hashMapOf(
            "fcmToken" to fcmToken,
            "password" to password,
            "profilePic" to profilePic,
            "uniqueId" to uniqueId,
            "username" to username
        )

        val newUser = userCollection
            .add(user)
            .addOnSuccessListener {
                Log.d(TAG,"registration success doc id : ${it.id}")
                docId.add(it.id)
            }
            .addOnFailureListener{e->
                Log.d(TAG,"error adding because :" ,e)
                throw e
            }

        newUser.await().id
       return if (docId.isNotEmpty()) {
           newUser.await().id
        }
        else{
            sub
        }

    }

    override suspend fun updateUserToDatabase(
        fcmToken:String,
        password:String,
        profilePic:String,
        uniqueId:String,
        username:String,
        docId:String
    ){

        val newData = hashMapOf(
            "fcmToken" to fcmToken,
            "password" to password,
            "profilePic" to profilePic,
            "uniqueId" to uniqueId,
            "username" to username
        )
        userCollection
            .document(docId)
            .update(newData as Map<String, Any>)
            .addOnSuccessListener {
                Log.d(TAG,"Successfully updated : ${docId}")

            }
            .addOnFailureListener{e->
                Log.d(TAG,"Failure to update")
                throw e
            }

    }

    override suspend fun checkUsernameExist(username: String): Boolean {
        return try{
            val result = userCollection
                .whereEqualTo("username",username)
                .get(Source.SERVER)
                .await()
            Log.d(TAG,"username found ${result.documents}")
            result.isEmpty
        }
        catch(e:Exception){
            Log.d(TAG,"Backend problem")
            false
        }
    }

    override suspend fun loginUserAndPasswordSuccess(username: String,password: String):List<User>{
        val Users = mutableListOf<User>()
        try{
            val querySnap = userCollection
                .whereEqualTo("username",username,)
                .whereEqualTo("password",password)
                .get(Source.SERVER)
                .await()

            for(doc in querySnap.documents){
                val fcmToken = doc.getString("fcmToken")?:""
                val pass = doc.getString("password")?:""
                val profilePic = doc.getString("profielPic")?:""
                val uniqueId = doc.getString("uniqueId")?:""
                val usern = doc.getString("username")?:""

                val docId = doc.id
                val user = User(fcmToken,pass,profilePic,uniqueId, usern,docId)
                Log.d(TAG,"Login Found user with id : ${user.uniqueId} and doc id : ${doc.id}")
                Users.add(user)

            }

        }
        catch(e:Exception){
            Log.d(TAG,"Backend problem")
            throw e
        }

        return if(Users.isNotEmpty()){
            Users
        }
        else{
            listOf(User(fcmToken = "not valid"))
        }
    }

    override suspend fun authenticateWithUniqueId(uniqueId: String): Boolean {
        return try{
            val result = userCollection
                .whereEqualTo("uniqueId",uniqueId)
                .get(Source.SERVER)
                .await()
            Log.d(TAG,"${result.documents} login success")
            result.isEmpty
        }
        catch(e:Exception){
            Log.d(TAG,"Backend problem")
            false
        }
    }

    override suspend fun getUserFromTokenInDatabase(Token: String): User {
        val Users = mutableListOf<User>()

        try {
            val querySnapshop = userCollection
                .whereEqualTo("fcmToken",Token)
                .get()
                .await()
            for(doc in querySnapshop.documents){
                val fcmToken = doc.getString("fcmToken")?:""
                val password = doc.getString("password")?:""
                val profilePic = doc.getString("profielPic")?:""
                val uniqueId = doc.getString("uniqueId")?:""
                val username = doc.getString("username")?:""

                val docId = doc.id
                val user = User(fcmToken,password,profilePic,uniqueId, username,docId)
                Log.d(TAG,"Got user with id : ${user.uniqueId} and doc id : ${doc.id}")
                Users.add(user)
            }

        }
        catch (e:Exception){
            Log.d(TAG,"Failed to get the data")
            throw e
        }
        return if (Users.isNotEmpty()) {
                Users[0]
            }
            else{
                User()
            }

    }

    override suspend fun updateUserToDatabase(
        currUser: User
    ):User{

        val newData = hashMapOf(
            "fcmToken" to currUser.fcmToken,
            "password" to currUser.password,
            "profilePic" to currUser.profilePic,
            "uniqueId" to currUser.uniqueId,
            "username" to currUser.username
        )

        val updateUser = userCollection
            .document(currUser.docId)
            .update(newData as Map<String, Any>)
            .addOnSuccessListener {
                Log.d(TAG,"Successfully updated : ${currUser.docId}")
//                docIds.add(it.id)
            }
            .addOnFailureListener{e->
                Log.d(TAG,"Failure to update")
                throw e
            }
        return User(
            fcmToken = currUser.fcmToken,
            password = currUser.password,
            profilePic = currUser.profilePic,
            uniqueId = currUser.uniqueId,
            username = currUser.username,
            docId = currUser.docId
        )
    }

    override suspend fun registerUserToDatabase(
        currUser: User
    ):User{
        Log.d(TAG,"updateCalled here")
        val docId = mutableListOf<String>()
        val sub = "no ID"

        val user = hashMapOf(
            "fcmToken" to currUser.fcmToken,
            "password" to currUser.password,
            "profilePic" to currUser.profilePic,
            "uniqueId" to currUser.uniqueId,
            "username" to currUser.username
        )

        val newUserId = userCollection
            .add(user)
            .addOnSuccessListener {
                Log.d(TAG,"registration success doc id : ${it.id}")
                docId.add(it.id)
            }
            .addOnFailureListener{e->
                Log.d(TAG,"error adding because :" ,e)
                throw e
            }

        val newUser:User = User(
                currUser.fcmToken,
                currUser.password,
                currUser.profilePic,
                currUser.uniqueId,
                currUser.username,
                docId = newUserId.await().id?:"not valid"
            )

        return newUser

    }

    override suspend fun logoutUser(currUser: User){
        val newData = hashMapOf(
            "fcmToken" to currUser.fcmToken,
            "password" to currUser.password,
            "profilePic" to currUser.profilePic,
            "uniqueId" to currUser.uniqueId,
            "username" to currUser.username
        )

        userCollection
            .document(currUser.docId)
            .update(newData as Map<String, Any>)
            .addOnSuccessListener {
                Log.d(TAG,"Logout for user id success : ${currUser.uniqueId}")
            }
            .addOnFailureListener{e->
                Log.d(TAG,"Failure to Logout")
                throw e
            }
    }

    override suspend fun resetPassword(
        username: String,
        uniqueId: String,
        newPassword: String
    ): List<String> {

        val list = mutableListOf<String>()
        Log.d(TAG,"reset Started api")
        try{
            Log.d(TAG,"reset Success api")
            val querySnapshot = userCollection
                .whereEqualTo("username", username)
                .whereEqualTo("uniqueId", uniqueId)
                .get()
//                .await()
            Log.d(TAG,"query : ${querySnapshot.await().documents}")
            for (document in querySnapshot.await()) {
                Log.d(TAG,"Updated user with id ${document.id}")
                userCollection
                    .document(document.id)
                    .update("password", newPassword,"fcmToken","")
                list.add(document.id)
            }
        }catch (e:Exception){
            Log.d(TAG,"reset failed api")
            return listOf()
        }

        return list
    }

    override suspend fun sendNotificationApi(token: String, title: String, body: String) {
//        val jsonObject = JSONObject().apply {
//            put("to","c4JAaCjSQZaCxuZLOx_B45:APA91bEUtXCmupogMhD1T5a7pZerIWp3nP0kNy6PYgox_gvps4WEaO4NihkJqANt5OJmxVixx95ACjHZq6AWdFxyRxiaQzneyKBNboB-deoP4g1EqVCQesQuFuoBu9xxrKL1m41OduaM")
//            put("notification", JSONObject().apply {
//                put("body", body)
//                put("title", title)
//            })
//        }
        Log.d(TAG,"TOKEN IS $token")

        Log.d(TAG,"Notification trigger in api")
//        try {
////            service.sendNotification(jsonObject.toString())
//            val response = service.sendNotification(jsonObject.toString())
//            Log.d(TAG,"Notification sent successfully")
//        } catch (e: Exception) {
//            Log.d(TAG,"Error sending notification: ${e.message} ")
//
//            throw e
//        }

        val notificationRequest = NotificationRequest(
            to = token,
            notification = Notification(title, body)
        )

        try{
//            val response = service.sendNotification(notificationRequest).awaitResponse()
            val response = service.sendNotification(notificationRequest)

            Log.d(TAG,"Successfully send notification api ${response.toString()}")
        }catch (e:Exception){
            Log.d(TAG,"Failed to send ${e.message}")
        }
    }

    override suspend fun getAllUsers(username: String): List<User> {
        Log.d(TAG,"started getting the users")
        val Users = mutableListOf<User>()
        val allUsers =userCollection
            .whereNotEqualTo("username",username)
            .get()
        for(doc in allUsers.await().documents){
            val fcmToken = doc.getString("fcmToken")?:""

            val profilePic = doc.getString("profielPic")?:""
            val usern = doc.getString("username")?:""

            val docId = doc.id
            val user = User(fcmToken,"password",profilePic,"uniqueId", usern,docId)
            Log.d(TAG,"Got user with id : ${user.uniqueId} and doc id : ${doc.id}")
            Users.add(user)
        }
        Log.d(TAG,"got users api")
        return Users
    }

    override suspend fun createNewChat(
        members: List<String>,
        chatName: String,
        chatId: String,
        profilePhoto: String,
        isGroup: Boolean
    ) {
//        val newMembers = members.toTypedArray()
        val newMembers:ArrayList<String> = ArrayList(members)
        Log.d(TAG,"Chat addition started api")

        val newChat = hashMapOf(
            "chatId" to chatId,
            "chatName" to chatName,
            "profilePhoto" to  profilePhoto,
            "isGroup" to isGroup,
            "members" to newMembers
        )

        chatsCollection
            .add(newChat)
            .addOnSuccessListener {
                Log.d(TAG,"added chat successfully")
            }
            .addOnFailureListener { e ->
                Log.d(TAG,"Failed to add to chat : $e")
            }
    }

    override suspend fun getChatsforMe(username: String): List<Chats> {
        val chats = mutableListOf<Chats>()
        Log.d(TAG,"Api the chats for usr : ${username}")
        val myChats = chatsCollection
            .whereArrayContains("members",username)
            .get()
            .addOnSuccessListener {
                Log.d(TAG,"Got the chats for usr : ${username}")

            }
            .addOnFailureListener{e->
                Log.d(TAG,"Unable to fetch the chats : $e")
            }

        for(doc in myChats.await().documents){
            val chatId = doc.getString("chatId")?:""
            val chatName = doc.getString("chatName")?:""
            val isGroup = doc.getBoolean("isGroup")?:false
            val members:List<Any> = doc.get("members") as List<Any>
//
            val singleChat = Chats(
                chatId = chatId,
                chatName = chatName,
                isGroup = isGroup,
                members = members.map { it.toString() }
            )
            chats.add(singleChat)
        }

        Log.d(TAG,"Chats in api are $chats")
        return chats
    }

    override suspend fun sendNewMessage(message: MessageReceived) {
        val newMessage = hashMapOf(
            "content" to message.content,
            "chatId" to message.chatId,
            "senderId" to message.senderId,
            "timeStamp" to FieldValue.serverTimestamp()
        )

        messagesCollection
            .add(newMessage)
            .addOnSuccessListener {
                Log.d(TAG,"message added successfully")
            }
            .addOnFailureListener{e->
                Log.d(TAG,"failed to add to the database : $e")
            }
    }

    override suspend fun getTokenForMemebers(members: List<String>): List<String> {
        val tokenList = mutableListOf<String>()

        for(member in members){
           val querySnap = userCollection
                .whereEqualTo("username",member)
                .get()
               .addOnSuccessListener{
                   Log.d(TAG,"Successfully got token for $member")
               }
               .addOnFailureListener { e->
                   Log.d(TAG,"Failed to get the token for $member")
               }
//            val currToken = querySnap.await().documents
            for(doc in querySnap.await().documents){
                tokenList.add(doc.getString("fcmToken")?:"")
            }
        }

//        val queries = members.map { member ->
//            userCollection.whereEqualTo("username", member).get().addOnSuccessListener { querySnap ->
//                Log.d(TAG, "Successfully got token for $member")
//                val currToken:List<String> = querySnap.documents.mapNotNull { doc ->
//                    doc.getString("fcmToken")
//                }
//                tokenList.addAll(currToken)
//            }
//        }
//        queries.awaitAll()

        return tokenList
    }

    override suspend fun getMessagesForChat(currentChatId: String): List<MessageReceived> {
        val messages = mutableListOf<MessageReceived>()

        Log.d(TAG,"message getting started in api")
        val messageGet = messagesCollection
            .whereEqualTo("chatId",currentChatId)
            .get()
            .addOnSuccessListener {
                Log.d(TAG,"message got successfully")
            }
            .addOnFailureListener{e->
                Log.d(TAG,"Failed to get messages : $e")
                throw e
            }

        for( doc in messageGet.await()) {
            val content = doc.getString("content") ?: ""
            val chatId = doc.getString("chatId")?:""
            val senderId = doc.getString("senderId")?:""
            val timeStamp = doc.getTimestamp("timeStamp")?: Timestamp.now()

            val mess = MessageReceived(content, chatId, senderId,timeStamp)
            messages.add(mess)
        }

        return messages
    }
}