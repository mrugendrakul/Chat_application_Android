package com.mad.softwares.chatApplication.ui.chats

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mad.softwares.chatApplication.data.ChatOrGroup
import com.mad.softwares.chatApplication.data.DataRepository
import com.mad.softwares.chatApplication.data.User
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

val TAGchat = "ChatsViewModel"
//val TAGaddChat = "AddChatsViewModel"

class ChatsViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val dataRepository: DataRepository,
) : ViewModel() {
    var chatsUiState = MutableStateFlow(ChatsUiState())
        private set

    init {
        getChats()
        Log.d(TAGchat,"init called here .......")
        Log.d(TAGchat,"To Reload chat : ${savedStateHandle.get<Boolean>(chatsScreenDestination.toReloadChats)}")
    }

//    private fun getCurrentUserData() {
//        chatsUiState.update {
//            it.copy(
//                isLoading = true,
//
//                )
//        }
//        viewModelScope.launch {
//            val currentUser: Deferred<User> = async { dataRepository.getCurrentUser() }
//
//            val user = currentUser.await()
//
//            if (user.username == "") {
//                chatsUiState.update {
//                    it.copy(
//                        isError = true,
//                        errorMessage = user.profilePic,
//                        isLoading = false
//                    )
//                }
//                Log.e(TAGchat, "Unable to get the current user: ${user.profilePic}")
//            } else {
//                Log.d(TAGchat, "Current user is : ${user.username}")
//                chatsUiState.update {
//                    it.copy(
//                        currentUser = user,
//                        isLoading = false
//                    )
//                }
//            }
//        }
//    }

    fun getChats(
        isForced: Boolean = false
    ) {
        if (chatsUiState.value.chats.isNotEmpty() && !isForced) {
            return
        }
        Log.d(TAGchat,"Get Chats started here.....")
        chatsUiState.update {
            it.copy(
                isLoading = true,
                currentChatStatus = CurrentChatStatus.Loading
            )
        }
        viewModelScope.launch {
//            delay(1000)
            val currentUser: Deferred<User> = async { dataRepository.getCurrentUser() }

            val user = currentUser.await()

            if (user.username == "") {
                chatsUiState.update {
                    it.copy(
                        isError = true,
                        errorMessage = user.profilePic,
//                        isLoading = false,
                        currentChatStatus = CurrentChatStatus.Failed
                    )
                }
                Log.e(
                    TAGchat,
                    "Unable to get the current user: ${user.profilePic} username : ${user.username}"
                )
            } else {
                Log.d(TAGchat, "Current user is : ${user.username}")
                chatsUiState.update {
                    it.copy(
                        currentUser = user,
                        isError = false,
                        errorMessage = "",
                        currentChatStatus = CurrentChatStatus.Loading
//                        isLoading = false,
//                        currentChatStatus = CurrentChatStatus.Success
                    )
                }
            }

            if (chatsUiState.value.isError) {
                return@launch
            }
            chatsUiState.update { it.copy(isLoading = true) }

            try {
                Log.d(
                    TAGchat,
                    "Chats are getting started for user : ${chatsUiState.value.currentUser.username}"
                )
                val userChats =
                    async { dataRepository.getChats(chatsUiState.value.currentUser.username) }
//            chats.await()
//                if (userChats.await().get(0).chatId == "")

                val userGroups =
                    async{dataRepository.getGroups(chatsUiState.value.currentUser.username)}
                val singlesChats = userChats.await()
                val groupsChats = userGroups.await()
//                val singlesChats = currentUserChats.filter { !it.isGroup }
//                val groupsChats = currentUserChats.filter { it.isGroup }
                chatsUiState.update {
                    it.copy(
                        chats = singlesChats,
                        groups = groupsChats,
                        isLoading = false,
                        currentChatStatus = CurrentChatStatus.Success
                    )
                }
                Log.d(TAGchat, "Chats are success : ${chatsUiState.value.chats}")
            } catch (e: Exception) {
                Log.e(TAGchat, "Unable to get the chats : $e")
                chatsUiState.update {
                    it.copy(
                        isError = true,
                        errorMessage = e.message.toString(),
                        isLoading = false,
                        currentChatStatus = CurrentChatStatus.Failed
                    )
                }
            }
        }
    }

    fun logoutUser() {
        chatsUiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val logOutStatus: Deferred<Boolean> =
                async { dataRepository.logoutUser(chatsUiState.value.currentUser) }
            val status = logOutStatus.await()
            if (status) {
                chatsUiState.update {
                    it.copy(
                        isLoading = false,
                        currentChatStatus = CurrentChatStatus.Logouted
                    )
                }
            } else {
                chatsUiState.update {
                    it.copy(
                        isLoading = false,
                        currentChatStatus = CurrentChatStatus.Success,
                        isError = true,
                        errorMessage = "Unable to logout"
                    )
                }
            }
        }
    }

    fun getMembers(): List<String> {
        Log.d(
            TAGchat,
            "members : ${chatsUiState.value.chats.map { it.chatName } + chatsUiState.value.currentUser.username}"
        )
        return chatsUiState.value.chats.map { it.chatName } + chatsUiState.value.currentUser.username
    }

    fun deleteChat(chatId:String){
        viewModelScope.launch {
            dataRepository.deleteChat(chatId)
        }
    }

    fun resetUserInside() {
        chatsUiState.update {
            it.copy(
                currentChatStatus = CurrentChatStatus.Loading
            )
        }
    }

//
}

data class ChatsUiState(
    val username: String = "",
    val currentUser: User = User(),
    val chats: List<ChatOrGroup> = listOf(
//        Chats(chatName = "sad@33.com")
    ),
    val groups:List<ChatOrGroup> = listOf(),
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String = "",
    val currentChatStatus: CurrentChatStatus = CurrentChatStatus.Loading,
//    val logoutSuccess: Boolean = false
)



enum class CurrentChatStatus() {
    Loading,
    Success,
    Failed,
    Logouted
}