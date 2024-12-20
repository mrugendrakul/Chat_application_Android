package com.mad.softwares.chatApplication.ui.chats.singles

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mad.softwares.chatApplication.data.DataRepository
import com.mad.softwares.chatApplication.data.chatUser
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

val TAGaddChat = "AddChatsViewModel"


class AddChatViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val dataRepository: DataRepository
) : ViewModel() {

    //
    var addChatUiState = MutableStateFlow(AddChatUiState())
        private set

    init {
        val members = savedStateHandle.get<String>("members")
        if (members != null) {
            addChatUiState.update {
                it.copy(
                    members = members.split(",")
                )
            }
        }
//        getMembers()
    }


//    fun getMembers() {
//        Log.d(TAGaddChat, "getMembers called")
//        addChatUiState.update {
//            it.copy(
//                isLoading = true,
////                members = chatsUiState.value.chats.map { it.chatID } + chatsUiState.value.currentUser.username
//            )
//        }
//        viewModelScope.launch {
//            try {
//                val users =
//                    async { dataRepository.getAllTheUsers(members = addChatUiState.value.members) }
//                addChatUiState.update {
//                    it.copy(
//                        chatUsers = users.await(),
//                        isLoading = false
//                    )
//                }
//                Log.d(TAGaddChat, "Members are loaded Successfully")
//            } catch (e: Exception) {
//                Log.e(TAGaddChat, "Unable to get the members : $e")
//                addChatUiState.update {
//                    it.copy(
//                        isError = true,
//                        errorMessage = e.message.toString(),
//                        isLoading = false
//                    )
//                }
//            }
//        }
//    }

    fun getSearchMembers(MemberToSearch: String = "") {
        Log.d(TAGaddChat, "getSearchMembers called")
//        addChatUiState.update {
//            it.copy(
//                isLoading = true,
////                members = chatsUiState.value.chats.map { it.chatID } + chatsUiState.value.currentUser.username
//            )
//        }
        if (addChatUiState.value.searchQuery.isNotEmpty() && addChatUiState.value.searchQuery.length >= 3) {
            addChatUiState.update {
                it.copy(
                    isLoading = true,
                    searched = true
                )
            }
            viewModelScope.launch {
                delay(500)
                try {
                    val users =
                        async {
                            dataRepository.getSearchUsers(
                                members = addChatUiState.value.members,
                                search = addChatUiState.value.searchQuery
                            )
                        }
                    addChatUiState.update {
                        it.copy(
                            chatUsers = users.await(),
                            isLoading = false
                        )
                    }
                    Log.d(TAGaddChat, "Searched Members are loaded Successfully")
                } catch (e: Exception) {
                    Log.e(TAGaddChat, "Unable to get the Searched members : $e")
                    addChatUiState.update {
                        it.copy(
                            isError = true,
                            errorMessage = e.message.toString(),
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    fun onSearchQuery(search: String) {
        var searchJob: Job? = null
        val searchDelay = 5000L
        addChatUiState.update {
            it.copy(
                searchQuery = search
            )
        }
    }

    private fun generateSixDigitUUID(n: Int): String {
        val randomUUID = UUID.randomUUID()
        val hashCode = Math.abs(randomUUID.hashCode()).toString()
        return hashCode.take(n).padStart(6, '0')
    }

    fun addSingleChat(
        secondMember: String
    ) {
        Log.d(TAGaddChat, "addSingleChat called")
        addChatUiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                dataRepository.addChatToDatabase(
                    currentUser = dataRepository.getCurrentUser(),
                    memberUsers = listOf(secondMember),
                    chatName = generateSixDigitUUID(6),
                    chatId = generateSixDigitUUID(24),
                    profilePhoto = "",
                    isGroup = false
                )
                addChatUiState.update {
                    it.copy(
                        addChatSuccess = true,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                Log.e(TAGaddChat, "Unable to add chat : $e")
                addChatUiState.update {
                    it.copy(
                        isError = true,
                        isLoading = false,
                        errorMessage = e.message.toString()
                    )
                }
            }
        }
    }

    fun resetAddChatSuccess() {
        addChatUiState.update { it.copy(
            addChatSuccess = false
        ) }
    }
}

data class AddChatUiState(
    val members: List<String> = listOf(),
    val chatUsers: List<chatUser> = listOf(),
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String = "",
    val addChatSuccess: Boolean = false,
    val searchQuery: String = "",
    val searched:Boolean = false
)