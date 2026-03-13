package com.mad.softwares.chatApplication.ui.userInfoAndPreferences

import androidx.compose.runtime.MutableState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mad.softwares.chatApplication.data.DataRepository
import com.mad.softwares.chatApplication.data.User
import com.mad.softwares.chatApplication.data.preferenceRepository.AiUrlPreferenceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
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
}

data class UserInfo (
    val currentUser : User = User(),
    val aiUrl : String = "",
)