package com.mad.softwares.chatApplication.ui.chats.groups

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mad.softwares.chatApplication.data.DataRepository
import com.mad.softwares.chatApplication.data.chatUser
import com.mad.softwares.chatApplication.ui.chats.singles.TAGaddChat
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

val TAGgrp = "AddGroupLogs"

class AddGroupViewModel (
    private val savedStateHandle: SavedStateHandle,
    private val dataRepository: DataRepository
):ViewModel(){
    var addGroupUiState = MutableStateFlow(AddGroupUiState())
        private set

    init {
        Log.d(TAGgrp, "init called FOR AddGroupLogs")
        val currentUser = savedStateHandle.get<String>("User")
        if (currentUser!=null){
            addGroupUiState.update {
                it.copy(
                    currentUser = currentUser
                )
            }
        }
    }

    fun onQueryChange(query:String){
        addGroupUiState.update { it.copy(
            searchQuery = query
        ) }
    }

    fun searchUserForGroup(){
        if(addGroupUiState.value.searchQuery.length>=3 ){
            addGroupUiState.update {
                it.copy(
                    isLoading = true
                )
            }
        }
        viewModelScope.launch {
            delay(500)
            try {
                val users =
                    async {
                        dataRepository.getSearchUsers(
                            members = listOf(addGroupUiState.value.currentUser),
                            search = addGroupUiState.value.searchQuery
                        )
                    }
                addGroupUiState.update {
                    it.copy(
                        chatUsers = users.await(),
                        isLoading = false,
                        searched = true
                    )
                }
                Log.d(TAGaddChat, "Searched Members are loaded Successfully")
            } catch (e: Exception) {
                Log.e(TAGaddChat, "Unable to get the Searched members : $e")
                addGroupUiState.update {
                    it.copy(
                        isError = true,
                        errorMessage = e.message.toString(),
                        isLoading = false,
                        searched = true
                    )
                }
            }
        }
    }

    private fun generateSixDigitUUID(n: Int): String {
        val randomUUID = UUID.randomUUID()
        val hashCode = Math.abs(randomUUID.hashCode()).toString()
        return hashCode.take(n).padStart(6, '0')
    }

    fun createGroup(){
        addGroupUiState.update {
            it.copy(
                isLoading = true
            )
        }
        viewModelScope.launch {
            try {
                dataRepository.addChatToDatabase(
                    currentUser = dataRepository.getCurrentUser(),
                    memberUsers = addGroupUiState.value.newMembers,
                    chatName = addGroupUiState.value.newGroupName,
                    chatId = generateSixDigitUUID(8),
                    profilePhoto = "",
                    isGroup = true
                )
                addGroupUiState.update {
                    it.copy(
                        addGroupSuccess = true,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                Log.e(TAGaddChat, "Unable to add chat : $e")
                addGroupUiState.update {
                    it.copy(
                        isError = true,
                        isLoading = false,
                        errorMessage = e.message.toString()
                    )
                }
            }
        }
    }

    fun addUserToTempList(toggle:Boolean,chatUser: String){
        if(toggle){
            addGroupUiState.update {
                it.copy(
                    newMembers = it.newMembers + chatUser
                )
            }
        }else{
            addGroupUiState.update {
                it.copy(
                    newMembers = it.newMembers - chatUser
                )
            }
        }
    }

    fun changeGroupName(grp:String){
        addGroupUiState.update {
            it.copy(
                newGroupName = grp
            )
        }
    }
}

data class AddGroupUiState(
    val currentUser : String = "",
    val chatUsers :List<chatUser> = listOf(),
    val isLoading : Boolean = false,
    val isError : Boolean = false,
    val errorMessage : String = "",
    val addGroupSuccess : Boolean = false,
    val searchQuery : String = "",
    val searched : Boolean = false,
    val newMembers:List<String> = listOf(),
    val newGroupName:String = ""
)