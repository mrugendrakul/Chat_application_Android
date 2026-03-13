package com.mad.softwares.chatApplication.ui.userInfoAndPreferences

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mad.softwares.chatApplication.data.DataRepository
import com.mad.softwares.chatApplication.data.User
import com.mad.softwares.chatApplication.data.preferenceRepository.AiUrlPreferenceRepository
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UserInfoViewModel(
    private val dataRepository: DataRepository,
    private val prefRepository: AiUrlPreferenceRepository
): ViewModel() {
    var infoUiState= MutableStateFlow(UserInfo())
        private set
    init {
        getAndSetCurrentUser()
    }

    val aiEndpointFromPref: StateFlow<String> = prefRepository.aiUrlEndpoint
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ""
        )

    fun getAndSetCurrentUser(){
        viewModelScope.launch {
            val currentUser = dataRepository.getCurrentUser()
            infoUiState.update { it.copy(currentUser = currentUser) }
        }
    }

    fun setAiUrl (value: String){
        infoUiState.update { it.copy(
            aiUrl = value
        ) }
    }
    fun setAiUrlOnPreference (){
        viewModelScope.launch{
            prefRepository.saveUrlEndpoint(infoUiState.value.aiUrl)
        }
    }

    fun logoutUser() {
//        chatsUiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val logOutStatus: Deferred<Boolean> =
                async { dataRepository.logoutUser(infoUiState.value.currentUser) }
            val status = logOutStatus.await()
            if (status) {
                infoUiState.update {
                    it.copy(
                        logout = true
                    )
                }
            } else {
//                chatsUiState.update {
//                    it.copy(
//                        isLoading = false,
//                        currentChatStatus = CurrentChatStatus.Success,
//                        isError = true,
//                        errorMessage = "Unable to logout"
//                    )
//                }
            }
        }
    }
}

data class UserInfo (
    val currentUser : User = User(),
    val aiUrl : String = "",
    val logout: Boolean = false
)