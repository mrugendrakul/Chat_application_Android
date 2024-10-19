package com.mad.softwares.chatApplication

import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.firestore
import com.mad.softwares.chatApplication.data.ChatOrGroup
import com.mad.softwares.chatApplication.data.ContentType
import com.mad.softwares.chatApplication.data.MessageReceived
import com.mad.softwares.chatApplication.data.NetworkDataRepository
import com.mad.softwares.chatApplication.data.User
import com.mad.softwares.chatApplication.network.AuthenticationApi
import com.mad.softwares.chatApplication.network.FirebaseAuthenticationApi
import com.mad.softwares.chatApplication.network.NetworkFirebaseApi

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Before

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.mad.softwares.chitchat", appContext.packageName)
    }


    private lateinit var firebaseApi: NetworkFirebaseApi
    private lateinit var authApi: AuthenticationApi
    private lateinit var authenticator: FirebaseAuth
    private lateinit var dataRepository: NetworkDataRepository
    var authState: FirebaseUser? = null

    @Before
    fun initApi() {
        val db = com.google.firebase.Firebase.firestore
        firebaseApi = NetworkFirebaseApi(
            com.google.firebase.Firebase.firestore,
            db.collection("Users"),
            db.collection("Chats"),
            db.collection("Messages"),
            EncryptionImpl
        )
//        Firebase.auth.useEmulator("10.0.2.2", 9099)
        authenticator = FirebaseAuth.getInstance()
        authApi = FirebaseAuthenticationApi(auth = authenticator)

        dataRepository = NetworkDataRepository(
            apiService = firebaseApi,
            authServie = authApi
        )

        runBlocking { authApi.loginUser(email = "good@456.com", password = "123456", status = {}) }
    }

    @After
    fun tearDown() {
        authenticator.currentUser?.getIdToken(false)
        runBlocking { authApi.logoutUser { } }
    }

//    fun status(status:FirebaseUser): FirebaseUser {
//        authState = status
//        return status
//    }

    @Test
    @Throws(Exception::class)
    fun authApi_signupUser_returnSuccess() =
        runTest {
//            lateinit var user:FirebaseUser
            Log.d("Authentication", "Starting test...")
            val statusDeferred = CompletableDeferred<String>()

            val email = "mruge@123.com"
            val currentUser: Deferred<FirebaseUser?> = async {
                authApi.signUpUser(
                    email = email,
                    password = "12345678",
                    status = { statusDeferred.complete(it) }
                )
            }

//            catch (e:Exception){
//                Log.d("Authentication","Error in creating user")
//            }
            val status = statusDeferred.await()
            assertNotNull(currentUser.await()?.email)
            assertEquals(email, currentUser.await()?.email)
//            assertEquals("abcf@123.com",authenticator.currentUser?.email)
            assertEquals("success", status)

            Log.d("Authentication", "User is for test: ${authenticator.currentUser?.email}")

//            assertEquals("abk@123.com",user.email)
        }

    @Test
    @Throws
    fun authApi_signInUser_returnSuccess() =
        runTest {
            Log.d("Authentication", "Starting login test...")
            val statusDeferred = CompletableDeferred<String>()

            val email = "mruge@123.com"
            val currentUser: Deferred<FirebaseUser?> = async {
                authApi.loginUser(
                    email = email,
                    password = "12345678",
                    status = { statusDeferred.complete(it) }
                )
            }

//            catch (e:Exception){
//                Log.d("Authentication","Error in creating user")
//            }
            val status = statusDeferred.await()
            assertNotNull(currentUser.await()?.email)
            assertEquals(email, currentUser.await()?.email)
//            assertEquals("abcf@123.com",authenticator.currentUser?.email)
            assertEquals("success", status)
        }


    @Test
    @Throws
    fun firebaseApi_addUserData_returnSuccess() =
        runTest {
            val addingUser = User(
                fcmToken = "test",
                profilePic = "test",
                uniqueId = "1531584",
                username = "test",
                docId = "iopj123ouihn"
            )

            val backendUser: Deferred<User> =
                async {
                    firebaseApi.registerUserToDatabase(
                        currUser = addingUser,
                        docId = addingUser.docId
                    )
                }

            assertEquals(addingUser.docId, backendUser.await().docId)
        }

    @Test
    @Throws
    fun firebaseApi_loginUser_returnSuccess() =
        runTest {
            val loginUser = User(
                fcmToken = "test1",
                profilePic = "test",
                uniqueId = "1531584",
                username = "test",
                docId = "iopj123ouihn"
            )

            val backendUser: Deferred<User> =
                async {
                    firebaseApi.loginAndUpdateUserToDatabase(
                        currUser = loginUser,
                        currFcmToken = "test1",
                        docId = loginUser.docId
                    )
                }
            assertEquals(loginUser.docId, backendUser.await().docId)
        }


    @Test
    @Throws(Exception::class)
    fun dataRepo_signUpUser_returnSuccess() =
        runTest {
            val newUser = User(
                fcmToken = "test1",
                profilePic = "test",
                uniqueId = "1531584",
                username = "test@123.com",
                docId = "iopj123ouihn"
            )

            val backendUser: Deferred<User> =
                async {
                    dataRepository.registerUser(
                        user = newUser,
                        currFcmToken = "Testing123"
                    )
                }

            assertEquals(newUser.username, backendUser.await().username)
        }

    @Test
    @Throws(Exception::class)
    fun dataRepo_loginUser_returnSuccess() =
        runTest {
            val loginUser = User(
                fcmToken = "test2",
                profilePic = "test",
                uniqueId = "1531584",
                username = "test@123.com",
                docId = "iopj123ouihn"
            )
            val backendUser: Deferred<User> =
                async {
                    dataRepository.loginUser(
                        user = loginUser,
                        currFcmToken = "Testing123"
                    )
                }

            assertEquals(loginUser.username, backendUser.await().username)
        }

    @Test
    @Throws(Exception::class)
    fun networkApi_addChat_Success() =
        runTest {
            firebaseApi.createNewChat(
                members = listOf("mrug@123.com", "happy@123.com"),
                chatName = "h1235",
                chatId = "123456",
                profilePhoto = "test",
                isGroup = false
            )
        }

    @Test
    @Throws(Exception::class)
    fun networkApi_getChatData_Success() =
        runTest {
            val chatId = "14772768"
            val chatData: Deferred<ChatOrGroup> = async {
                firebaseApi.getChatData(chatId)
            }
            assertEquals(chatId, chatData.await().chatId)
        }

    @Test
    @Throws(Exception::class)
    fun dataRepo_getDataChat_Success() =
        runTest {
            val chatId = "14772768"
            val username = "good@456.com"

            val currChat: Deferred<ChatOrGroup> = async {
                dataRepository.getDataChat(chatId, username)
            }

            assertEquals(chatId, currChat.await().chatId)
            assertEquals(username, currChat.await().chatName)
        }

    @Test
    @Throws(Exception::class)
    fun networkApi_sendMessage_Success() =
        runTest {
            val chatId = "14772767"
            val username = "good@456.com"
            val message = MessageReceived(
                content = "New Test message",
                contentType = ContentType.text,
                senderId = username,
            )

            val status: Deferred<Boolean> =
                async { firebaseApi.sendNewMessage(message, chatId) }
            delay(3000)
            assertEquals(true, status.await())


        }

    @Test
    @Throws(Exception::class)
    fun networkApi_getMessage_Success() =
        runTest {
            val chatId = "14772768"
            val username = "good@456.com"
            val message = MessageReceived(
                content = "New Test message",
                contentType = ContentType.text,
                senderId = username,
            )
            try {
                val messages: Deferred<List<MessageReceived>> =
                    async { firebaseApi.getMessagesForChat(currentChatId = chatId) }
                delay(3000)
                assertNotNull(messages.await())
                Log.d("testing", "messages : ${messages.await()}")
                assertTrue(true)
            } catch (e: Exception) {
                assertTrue(false)
            }
        }
    @Test
    @Throws(Exception::class)
    fun dataRepo_sendMessage_Success() =
        runTest {
            val chatId = "14772768"
            val username = "good@456.com"
            val message = MessageReceived(
                content = "New Test message",
                contentType = ContentType.text,
                senderId = username,
            )

            val status: Deferred<Unit> =
                async { dataRepository.sendMessage(message, chatId) }
            delay(3000)
            assertEquals(true, status.await())
        }
}
