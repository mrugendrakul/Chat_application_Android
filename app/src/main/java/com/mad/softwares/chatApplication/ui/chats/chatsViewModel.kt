package com.mad.softwares.chatApplication.ui.chats

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mad.softwares.chatApplication.data.ChatOrGroup
import com.mad.softwares.chatApplication.data.DataRepository
import com.mad.softwares.chatApplication.data.User
import com.mad.softwares.chatApplication.data.lastMessage
import com.mad.softwares.chatApplication.ui.updateElement
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
//        getChats()
        getLiveChatsOrGroup()
        Log.d(TAGchat, "init called here .......")
        Log.d(
            TAGchat,
            "To Reload chat : ${savedStateHandle.get<Boolean>(chatsScreenDestination.toReloadChats)}"
        )
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch { dataRepository.stopLiveChat() }
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

    //Deprecated
    fun getChats(
        isForced: Boolean = false
    ) {
        if (chatsUiState.value.chats.isNotEmpty() && !isForced) {
            return
        }
        Log.d(TAGchat, "Get Chats started here.....")
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
                if(user.profilePic == "NoKeyFound"){
                    chatsUiState.update {
                        it.copy(
                            isError = true,
                            errorMessage = user.profilePic,
//                        isLoading = false,
                            currentChatStatus = CurrentChatStatus.NoOfflineKey
                        )
                    }
                }
                else{
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
                }
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
                    async { dataRepository.getGroups(chatsUiState.value.currentUser.username) }
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

    fun deleteChat(chatId: String) {
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

    fun toggleChatOrGroup(status: Boolean, chatOrGroup: ChatOrGroup) {
        if (status) {

            chatsUiState.update {
                it.copy(
                    selectedChatsOrGroups = it.selectedChatsOrGroups + chatOrGroup,
                    selectedAll = it.selectedChatsOrGroups.size == it.chats.size + it.groups.size,
                )
            }
        } else {
            if(chatsUiState.value.selectedChatsOrGroups.size ==1){
                chatsUiState.update {
                    it.copy(
                        selectedChatsOrGroups = it.selectedChatsOrGroups - chatOrGroup,
                        selectStatus = false
                    )
                }
            }
            else{
                chatsUiState.update {
                    it.copy(
                        selectedChatsOrGroups = it.selectedChatsOrGroups - chatOrGroup
                    )
                }
            }
        }
    }

    fun setSelect(){
        chatsUiState.update {
            it.copy(
                selectStatus = true
            )
        }
    }

    fun unSelect() {
        chatsUiState.update {
            it.copy(
                selectStatus = false,
                selectedChatsOrGroups = listOf()
            )
        }
    }

    fun selectAll(){
        chatsUiState.update {
            it.copy(
                selectedChatsOrGroups = it.chats + it.groups,
                selectedAll = true
            )
        }
    }

    fun deSelectAll(){
        chatsUiState.update {
            it.copy(
                selectedChatsOrGroups = listOf(),
                selectedAll = false
            )
        }
    }

    fun deleteChats(){
        for(chat in chatsUiState.value.selectedChatsOrGroups){
            viewModelScope.launch{
                try {
                    dataRepository.deleteChat(chat.chatId)
                    Log.d(TAGchat,"Delete chat successful")
                }catch (e:Exception){
                    Log.e(TAGchat,"Unable to delete chat : $e")
                }

            }
        }
    }

    private fun getLiveChatsOrGroup(){
        Log.d(TAGchat,"Live chat started here ==========>++++++++..")
        chatsUiState.update {
            it.copy(
                isLoading = true,
                currentChatStatus = CurrentChatStatus.Loading
            )
        }
        viewModelScope.launch {
            val currentUser:Deferred<User> = async { dataRepository.getCurrentUser() }
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

            if (chatsUiState.value.isError){
                return@launch
            }
            try {
                Log.d(TAGchat,"Inside the second try block for the live starting")
                chatsUiState.update {
                    it.copy(
//                        chats = listOf(),
                        isLoading = false,
                        currentChatStatus = CurrentChatStatus.Success
                    )
                }
                //Chats
                dataRepository.liveChatStore(
                    myUsername = chatsUiState.value.currentUser.username,
                    onChatAdd = { newChat->
                        Log.d(TAGchat,"New chat added Added!!!")
                        chatsUiState.update { it.copy(
                            chats = it.chats + newChat,
                            isLoading = false,
                            currentChatStatus = CurrentChatStatus.Success
                        ) }
                    },
                    onChatUpdate = { updatedChat->
                        val element = chatsUiState.value.chats.filter { chat->
                            chat.chatId == updatedChat.chatId
                        }
                        val index = chatsUiState.value.chats.indexOf(element[0])
                        val newChat = element[0].copy(lastMessage = lastMessage(
                            content = updatedChat.lastMessage.content,
                            timestamp = updatedChat.lastMessage.timestamp,
                            sender = updatedChat.lastMessage.sender
                        ))
                        chatsUiState.update {
                            it.copy(
                                chats = updateElement(it.chats,newChat,index),
                                isLoading = false,
                                currentChatStatus = CurrentChatStatus.Success)}
                        Log.d(TAGchat,"Chat is update with new element : ${newChat}")

                    },
                    onChatDelete = { deleteChat->
                        Log.d(TAGchat,"Chat is deleted : $deleteChat")
                        val element = chatsUiState.value.chats.filter { chat->
                            chat.chatId == deleteChat.chatId
                        }
                        val deletedChat = element[0]
                        chatsUiState.update {
                            it.copy(
                                chats = it.chats - deletedChat,
                                isLoading = false,
                                currentChatStatus = CurrentChatStatus.Success
                            )
                        }
                        Log.d(TAGchat,"Chat is deleted : $deleteChat")
                    },
                    onError = {
                        chatsUiState.update {
                            it.copy(
                                isError = true,
                                errorMessage = it.errorMessage,
                                isLoading = false,
                                currentChatStatus = CurrentChatStatus.Failed
                            )
                    }
                    }
                )

                //Groups
                dataRepository.getLiveGroupStore(
                    myUsername = chatsUiState.value.currentUser.username,
                    onChatAdd = { newGroup->
                        Log.d(TAGchat,"New chat added Added!!!")
                        chatsUiState.update { it.copy(
                            groups = it.groups + newGroup,
                            isLoading = false,
                            currentChatStatus = CurrentChatStatus.Success
                        ) }
                    },
                    onChatUpdate = { updatedGroup->
                        val element = chatsUiState.value.groups.filter { group->
                            group.chatId == updatedGroup.chatId
                        }
                        val index = chatsUiState.value.groups.indexOf(element[0])
                        val newGroup = element[0].copy(lastMessage = lastMessage(
                            content = updatedGroup.lastMessage.content,
                            timestamp = updatedGroup.lastMessage.timestamp,
                            sender = updatedGroup.lastMessage.sender
                        ))
                        chatsUiState.update {
                            it.copy(
                                groups = updateElement(it.groups,newGroup,index),
                                isLoading = false,
                                currentChatStatus = CurrentChatStatus.Success)}
                        Log.d(TAGchat,"Chat is update with new element : ${newGroup}")

                    },
                    onChatDelete = { deleteGroup->
//                        Log.d(TAGchat,"Group is deleted : $deleteGroup")
                        val element = chatsUiState.value.groups.filter { group->
                            group.chatId == deleteGroup.chatId
                        }
                        val deletedGroup = element[0]
                        chatsUiState.update {
                            it.copy(
                                groups = it.groups - deletedGroup,
                                isLoading = false,
                                currentChatStatus = CurrentChatStatus.Success
                            )
                        }
                        Log.d(TAGchat,"Group is deleted : $deletedGroup")
                    },
                    onError = {
                        chatsUiState.update {
                            it.copy(
                                isError = true,
                                errorMessage = it.errorMessage,
                                isLoading = false,
                                currentChatStatus = CurrentChatStatus.Failed
                            )
                        }
                    }
                )
            }catch (
                e:Exception
            ){
                Log.e(TAGchat,"Error starting live messages : $e")
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
//
}

data class ChatsUiState(
    val username: String = "",
    val currentUser: User = User(),
    val chats: List<ChatOrGroup> = listOf(
//        Chats(chatName = "sad@33.com")
    ),
    val groups: List<ChatOrGroup> = listOf(),
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String = "",
    val currentChatStatus: CurrentChatStatus = CurrentChatStatus.Loading,
    val selectedChatsOrGroups: List<ChatOrGroup> = listOf(),
    val selectStatus:Boolean = false,
    val selectedAll:Boolean = false
//    val logoutSuccess: Boolean = false
)


enum class CurrentChatStatus() {
    Loading,
    Success,
    Failed,
    Logouted,
    NoOfflineKey
}