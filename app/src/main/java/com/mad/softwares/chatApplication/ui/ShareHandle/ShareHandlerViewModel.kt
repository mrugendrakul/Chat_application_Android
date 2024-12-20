package com.mad.softwares.chatApplication.ui.ShareHandle

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.mad.softwares.chatApplication.data.ChatOrGroup
import com.mad.softwares.chatApplication.data.ContentType
import com.mad.softwares.chatApplication.data.DataRepository
import com.mad.softwares.chatApplication.data.MessageReceived
import com.mad.softwares.chatApplication.data.User
import com.mad.softwares.chatApplication.data.messageStatus
import com.mad.softwares.chatApplication.ui.chats.CurrentChatStatus
import com.mad.softwares.chatApplication.ui.chats.TAGchat
import com.mad.softwares.chatApplication.ui.messages.TAGmess
import com.mad.softwares.chatApplication.ui.updateElement
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

val TAGShare = "ShareHandler_TAG"

class ShareHandlerViewModel(
    private val dataRepository: DataRepository
): ViewModel() {
    var shareHandlerUiState = MutableStateFlow(ShareHandlerUiState())
        private set

    init {
        startProcess()
    }
    private fun startProcess(){
        shareHandlerUiState.update {
            it.copy(
                isLoading = true
            )
        }
        viewModelScope.launch{
//            delay(2000)
            val currentUser:Deferred<User> = async { dataRepository.getCurrentUser() }
            val user = currentUser.await()
            if (user.username == "") {
                shareHandlerUiState.update {
                    it.copy(
                        isError = true,
//                        errorMessage = user.profilePic,
                        isLoading = false,
//                        currentChatStatus = CurrentChatStatus.Failed
                    )
                }
                Log.e(
                    TAGShare,
                    "Unable to get the current user: ${user.profilePic} username : ${user.username}"
                )
            } else {
                Log.d(TAGShare, "Current user is : ${user.username}")
                shareHandlerUiState.update {
                    it.copy(
                        currentUser = user.username,
                        isError = false,
//                        errorMessage = "",
//                        currentChatStatus = CurrentChatStatus.Loading
//                        isLoading = false,
//                        currentChatStatus = CurrentChatStatus.Success
                    )
                }
            }

            if (shareHandlerUiState.value.isError){
                return@launch
            }
            val toShareChats = async{
                dataRepository.getAllDeviceChats()
            }
            if(toShareChats.await() != null){
                Log.d(TAGShare,"Chats are gathere")
                shareHandlerUiState.update {
                    it.copy(
                        toShareChats = toShareChats.await() ?: listOf(),
                        isLoading = false
                    )
                }
            }
            else{
                Log.e(TAGShare,"Error getting the chats")
                shareHandlerUiState.update {
                    it.copy(
                        isLoading = false,
                        isError = true
                    )
                }
            }
        }
    }

    fun updateShareDate(text:String, isInit:Boolean){
        if(isInit && shareHandlerUiState.value.isFirst){
            Log.d(TAGShare,"First edit")
            shareHandlerUiState.update {
                it.copy(
                    shareData = text,
                    isFirst = false
                )
            }
        }
        else if(!isInit){
            Log.d(TAGShare,"Second edit")
            shareHandlerUiState.update {
                it.copy(
                    shareData = text
                )
            }
        }
    }

    fun toggleChats(value:Boolean,chat:ChatOrGroup){
        if (value){
            shareHandlerUiState.update {
                it.copy(
                    selectedChats = it.selectedChats + chat
                )
            }
        }
        else{
            shareHandlerUiState.update {
                it.copy(
                    selectedChats = it.selectedChats - chat
                )
            }
        }
    }

    private val viewModelJob = SupervisorJob()
    private val backgroundScope = CoroutineScope(Dispatchers.IO + viewModelJob)

    fun sendMessage(
        onError:(String)->Unit,
        onSuccess:()->Unit
    ){
        val newMessage = MessageReceived(
            contentType = ContentType.text,
            content = shareHandlerUiState.value.shareData,
            senderId = shareHandlerUiState.value.currentUser,
            status = messageStatus.Sending,
            timeStamp = Timestamp.now()
        )

        backgroundScope.launch() {

//            delay(5000)
            try {
                shareHandlerUiState.update {
                    it.copy(
                        isLoading = true
                    )
                }
                shareHandlerUiState.value.selectedChats.forEach{chat->
                    val currChattokens = async{dataRepository.getDataChat(
                        chatId = chat.chatId,
                        chatName = shareHandlerUiState.value.currentUser,
                    )}
                    dataRepository.sendMessage(
                        message = newMessage,
                        chatId = chat.chatId,
                        secureAESKey = chat.secureAESKey,
                        fcmTokens = currChattokens.await().membersData.map { it.fcmToken }
                            .flatten()
//                    chatId = "12345677"
                    )
                    Log.d(TAGShare,"Message send successfully to ${chat.chatId}")
                    delay(2000)
                    onSuccess()
                }


//                messagesUiState.value.messages.set(
//                    index = messagesUiState.value.messages.indexOf(newMessage),
//                    element = newMessage.copy(status = messageStatus.Send)
//                )


            } catch (e: Exception) {
                Log.e(TAGShare, "Unable to send the message : $e")
                try {
//                    messagesUiState.value.messages.set(
//                        index = messagesUiState.value.messages.indexOf(newMessage),
//                        element = newMessage.copy(status = messageStatus.Error)
//                    )
                    onError(e.message.toString())

                    return@launch
                } catch (e: Exception) {
                    Log.e(TAGShare, "Unable to update the message status : $e")

//                    Log.e(TAGmess,"Unable to send the message")
                    onError(e.message.toString())
//
                    Log.e(TAGShare, "Error sending message :$e")
                    return@launch
                }
            }
        }
    }
}

data class ShareHandlerUiState(
    val shareData:String = "",
    val toShareChats: List<ChatOrGroup> = listOf(),
    val selectedChats:List<ChatOrGroup> = listOf(),
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val currentUser:String="",
    val isFirst:Boolean = true
)