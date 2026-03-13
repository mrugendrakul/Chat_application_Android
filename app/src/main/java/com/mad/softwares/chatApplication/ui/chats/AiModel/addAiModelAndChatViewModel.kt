package com.mad.softwares.chatApplication.ui.chats.AiModel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.gson.Gson
import com.mad.softwares.chatApplication.data.DataRepository
import com.mad.softwares.chatApplication.data.WorkRespository
import com.mad.softwares.chatApplication.data.models.OllamaModel
import com.mad.softwares.chatApplication.data.models.tags
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

val TAGai = "AitAgs"

class AddAiModelAndChatViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val dataRepository: DataRepository,
    private val aiApis : WorkRespository
): ViewModel(){

    var aiChatUiState = MutableStateFlow(AiModelChat())
        private set

    init {
     getTags()
    }

    fun getTags(){
        viewModelScope.launch {
            try{
                Log.d(TAGai,"Getting tags for ai started...")
                aiApis.getAiTags()
            }
            catch (e: Exception){
                Log.e(TAGai,"Error getting tags- $e")
            }
        }
    }

    val aiTags: StateFlow<TagsUiState> = aiApis.TagsInfo
        .map {  info ->
            val tagsData = info.outputData.getString("AI_TAGS_RESPONSE")
            Log.d(TAGai,"Tags data getting -> $tagsData")
            when{
                tagsData == null->{
                    TagsUiState(
                        tags = tags(listOf(OllamaModel(name = "null","null"))),
                        fetchStatus = AiModelStatus.Error
                    )
                }
                info.state == WorkInfo.State.RUNNING ->{
                    TagsUiState(
                        tags = tags(listOf(OllamaModel("Loading...","Loading..."))),
                        fetchStatus = AiModelStatus.Loading
                    )
                }
                info.state.isFinished ->{
                    val tagsObject = Gson().fromJson(tagsData,tags::class.java)
//                    tags(listOf(OllamaModel("done","done")))
                    TagsUiState(
                        tags = tagsObject,
                        fetchStatus = AiModelStatus.Success
                    )
                }
                info.state == WorkInfo.State.FAILED ->{
                    TagsUiState(
                        tags=tags(listOf(OllamaModel("Error","error"))),
                        fetchStatus = AiModelStatus.Error
                    )
                }
                else->{
                    TagsUiState(
                        tags = tags(listOf(OllamaModel("Loading...","Loading..."))),
                        fetchStatus = AiModelStatus.Loading
                    )
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TagsUiState(
                tags = tags(listOf(OllamaModel(name="initial", model = "initial"))),
                fetchStatus = AiModelStatus.Loading
            )
        )

    private fun generateSixDigitUUID(n: Int): String {
        val randomUUID = UUID.randomUUID()
        val hashCode = Math.abs(randomUUID.hashCode()).toString()
        return hashCode.take(n).padStart(6, '0')
    }

    override fun onCleared() {
            super.onCleared()
        aiChatUiState.update {
            it.copy(addChatSuccess = false)
        }
    }

    fun addAiChat(
        selectedModel:String,
        modelName:String,
    ){
        aiChatUiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try{
                dataRepository.addChatToDatabase(
                    currentUser = dataRepository.getCurrentUser(),
                    memberUsers = listOf(aiChatUiState.value.selectedModel.model),
                    chatName = aiChatUiState.value.chatName,
                    chatId = generateSixDigitUUID(24),
                    profilePhoto = "",
                    isGroup = false,
                    isAiChat = true,
                )
                aiChatUiState.update { it.copy(
                    isLoading = false,
                    addChatSuccess = true
                ) }
            }catch (e:Exception){
                Log.e(TAGai,"Error adding chat -> $e")
                aiChatUiState.update {
                    it.copy(
                        isLoading = false,
                        addChatSuccess = false
                    )
                }
            }
        }
    }

    fun onChatNameChange(name:String){
        aiChatUiState.update { it.copy(
            chatName = name
        ) }
    }

    fun setCurrentModel ( model: OllamaModel){
        Log.d(TAGai,"Model selecting -> $model")
        aiChatUiState.update { it.copy(
            selectedModel = model
        ) }
    }
}

enum class AiModelStatus(){
    Loading,
    Error,
    Success
}

data class TagsUiState(
    val tags: tags,
    val fetchStatus: AiModelStatus,
)

data class AiModelChat(
    val isLoading: Boolean = false,
    val addChatSuccess :Boolean = false,
    val chatName : String = "",
    val selectedModel: OllamaModel = OllamaModel("Error Selecting...","")
)