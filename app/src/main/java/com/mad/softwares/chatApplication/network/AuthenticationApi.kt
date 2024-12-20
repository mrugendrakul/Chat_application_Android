package com.mad.softwares.chatApplication.network

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CompletableDeferred

var TAGauth:String = "Authentication"

interface AuthenticationApi {

    suspend fun loginUser(
        email: String,
        password: String,
        status: (String) -> Unit
    ):FirebaseUser?

    fun logoutUser(
        errorCallBack: (String) -> Unit
    )

    suspend fun signUpUser(
        email: String,
        password: String,
        status: (String) -> Unit
    ):FirebaseUser?

    suspend fun getCurrentUser():String

    suspend fun getCurrentUsername():String
}

class FirebaseAuthenticationApi(
    val auth: FirebaseAuth
):AuthenticationApi{
    override suspend fun signUpUser(
        email: String,
        password: String,
        status: (String) -> Unit
    ): FirebaseUser? {
        Log.d(TAGauth,"Create email = $email")
        val deferredUser = CompletableDeferred<FirebaseUser?>()
//        Log.d(TAGauth,"User is : ${user.email}")
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAGauth, "Create email = $email success")
//                        auth.currentUser?.let { status(it) }
                        auth.currentUser?.let {
                            deferredUser.complete(it)
                            status("success") }?:deferredUser.complete(null)
//                        setUser(auth.currentUser!!)
                        return@addOnCompleteListener
//                        return@addOnCompleteListener user = auth.currentUser

                    } else {
                        Log.e(TAGauth, "Create email = $email failed error = ${task.exception}")

                        if(task.exception.toString()
                                .contains("The email address is already in use by another account")) {
                            Log.d(TAGauth, "Email already in use")
                            status("Email already in use")

                        }
                        if(task.exception.toString()
                                .contains("The given password is invalid")){
                            Log.d(TAGauth, "Invalid password")
                            status("Invalid password")
                        }
                        if(task.exception.toString()
                                .contains("The email address is badly formatted")){
                            Log.d(TAGauth, "Invalid email")
                            status("Invalid email")
                        }
                        else{
                            status(task.exception?.message.toString())
                        }
                        deferredUser.complete(null)
//                        deferredUser.completeExceptionally(task.exception!!)
//                    throw task.exception!!
                    }
                }
//        Log.d(TAGauth,"User is : ${user.email}"
        return deferredUser.await()
    }

    override suspend fun loginUser(
        email: String,
        password: String,
        status: (String) -> Unit): FirebaseUser? {
        Log.d(TAGauth,"Login started ")

        val deferredUser = CompletableDeferred<FirebaseUser?>()
        auth.signInWithEmailAndPassword(email,password)
            .addOnCompleteListener(){task->
                if(task.isSuccessful){
                    Log.d(TAGauth,"Login success : $email")
                    val user = auth.currentUser
                    user?.let {
                        status("success")
                        deferredUser.complete(it)
                    }?:deferredUser.complete(null)
                }
                else{
                    Log.e(TAGauth,"Login failed , error : ${task.exception}")
                    if(task.exception.toString()
                        .contains("There is no user record corresponding to this identifier"))
                    {
                        Log.e(TAGauth,"User not found")
                        status("User not found")
                    }
                    else if(task.exception.toString()
                            .contains("The password is invalid or the user does not have a password"))
                    {
                        Log.e(TAGauth,"Invalid password")
                        status("Wrong password")
                    }
                    else{
                        Log.e(TAGauth,"Another Error")
                        status(task.exception?.message.toString())
                    }
                    deferredUser.complete(null)
//                    status("failed")
//                    deferredUser.completeExceptionally(task.exception!!)
                }
            }

        return deferredUser.await()
    }

    override fun logoutUser(
        errorCallBack: (String) -> Unit
    ) {
        try{
            auth.signOut()
            Log.d(TAGauth,"Logout Success")
        }
        catch (e:Exception){
            Log.e(TAGauth,"Logout failed : $e")
            errorCallBack("Logout Failed : $e")
        }
    }


    override suspend fun getCurrentUser(): String {
        val curUser = auth.currentUser

        return curUser?.uid?:""
    }

    override suspend fun getCurrentUsername(): String {
        val curUser = auth.currentUser

        return curUser?.email?:""
    }
}