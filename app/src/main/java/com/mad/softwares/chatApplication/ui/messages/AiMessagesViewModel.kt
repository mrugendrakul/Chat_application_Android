package com.mad.softwares.chatApplication.ui.messages

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import com.google.firebase.Timestamp
import com.google.gson.Gson
import com.mad.softwares.chatApplication.data.ChatOrGroup
import com.mad.softwares.chatApplication.data.ContentType
import com.mad.softwares.chatApplication.data.DataRepository
import com.mad.softwares.chatApplication.data.MessageReceived
import com.mad.softwares.chatApplication.data.WorkRespository
import com.mad.softwares.chatApplication.data.messageStatus
import com.mad.softwares.chatApplication.data.models.AiResponse
import com.mad.softwares.chatApplication.data.models.OllamaModel
import com.mad.softwares.chatApplication.data.models.messages
import com.mad.softwares.chatApplication.data.models.tags
import com.mad.softwares.chatApplication.ui.updateElement
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.collections.plus


const val AiMessagesTag = "AiMessageLogs"

class AiMessagesViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val dataRepository: DataRepository,
    private val aiApis: WorkRespository
) : ViewModel() {
    val aiMessagesUiState = MutableStateFlow(AiMessagesUiState())
    val lastIndexAdded = MutableStateFlow(0)

    init {
        val chatId_username =
            savedStateHandle.get<String>(AiMessagesDestinationData.chatIDAndUsername)
        val chatId = chatId_username?.split(",")?.get(0)
        val chatName = chatId_username?.split(",")?.get(1)

        aiMessagesUiState.update {
            it.copy(
                chatId = chatId ?: "No id found",
                currentUser = chatName ?: "No user name found"
            )
        }

        Log.d(AiMessagesTag, "getting data as $chatId $chatName")

        getChatInfo()
    }

    val aiMessageUUID = MutableStateFlow<String>("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val aiMessage: StateFlow<AiResponseUiState> = aiMessageUUID.flatMapLatest { uuid ->
        return@flatMapLatest aiApis.getMessageInfoById(uuid)
            .mapNotNull { info ->
                if (info == null) {
                    AiResponseUiState()
                } else {
                    mapInfoByUIState(info)
                }
            }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AiResponseUiState(
                AiResponse(message = messages("assistant", "Initial State"), false), AiState.Success
            )
        )

    val modelState: StateFlow<ModelState> = aiApis.TagsInfo
        .map {
            info ->
            val currentUserName = aiMessagesUiState.value.currentUser
            val model =
                aiMessagesUiState.value.currChatInfo.members.firstOrNull { it -> it != currentUserName }
                    ?: ""
            val tagData = info.outputData.getString("AI_TAGS_RESPONSE")
            when{
                model == ""->{
                    Log.d(AiMessagesTag,"No model init")
                    ModelState.Loading
                }
                tagData == null ->{
                    ModelState.Offline
                }
                info.state == WorkInfo.State.RUNNING ->{
                    ModelState.Loading
                }
                info.state == WorkInfo.State.SUCCEEDED ->{
                    val tagObj = Gson().fromJson(tagData, tags::class.java)
                    Log.d(AiMessagesTag,"Got the tags -> $tagObj, model -> $model, got the model ->${tagObj.models.firstOrNull { it.model == model }}")
                    if (tagObj.models.firstOrNull { it.model == model } !=null){
                        Log.d(AiMessagesTag,"ONline sending from here...")
                        ModelState.Online
                    }
                    else{
                        ModelState.NotFound
                    }
                }
                else -> {
                    ModelState.Offline
                }
            }
        }
        .stateIn(scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ModelState.Loading)
    fun mapInfoByUIState(info: WorkInfo): AiResponseUiState {
        Log.d(AiMessagesTag, "Getting the info as $info")
//        if(info == null){
//            Log.d(AiMessagesTag,"Getting the info null")
//            AiResponseUiState(
//                AiResponse(
//                    message = messages("assistant", "NoData here"),
//                    false
//                ), AiState.Error
//            )
//        }
//        else {


        val outputData = info.outputData.getString("AI_EXPENSIVE_RESPONSE")

        val streamData = info.progress.getString("AI_STREAM_FULL")
        Log.d(AiMessagesTag, "Output dta for the model streaming $streamData")
//            val outputObject = Gson().fromJson(outputData, tags::class.java)
        return when {
            streamData?.isNotEmpty() == true -> {
//                Log.d(AiMessagesTag, "Output dta for the model chuncking $streamData")
                Log.d(AiMessagesTag,"last index in streaming ${lastIndexAdded.value} ${aiMessagesUiState.value.aiMessages.size }")
                aiMessagesUiState.update {
                    it.copy(
                        aiMessages = updateElement(
                            list = it.aiMessages, index = lastIndexAdded.value,
                            newElement = AiResponseUiState
                                (
                                aiResponse = AiResponse(
                                    messages("assistant", streamData),
                                    done = true
                                ), aiState = AiState.Streaming
                            )
                        )
                    )
                }
                AiResponseUiState(
                    AiResponse(
                        message = messages("assistant", streamData ?: "No stream"),
                        false
                    ), AiState.Streaming
                )
            }

            outputData.isNullOrEmpty() -> {
                Log.d(AiMessagesTag, "This is getting called initially.")
                AiResponseUiState(
                    AiResponse(message = messages("assistant", "No data so let's start!"), false),
                    AiState.Loading
                )
            }

            info.state == WorkInfo.State.RUNNING -> {
//                Log.d(AiMessagesTag, "Output dta for the model loading $outputData")
                AiResponseUiState(
                    AiResponse(
                        message = messages("assistant", streamData ?: "No stream"),
                        false
                    ), AiState.Loading
                )
            }

            info.state.isFinished -> {
                Log.d(AiMessagesTag, "Output dta for the model Finished $outputData")

                try {
                    val outputObject = Gson().fromJson(outputData, AiResponse::class.java)
                    val currentUserName = aiMessagesUiState.value.currentUser
                    val newMessage = MessageReceived(
                        contentType = ContentType.Md,
                        content = outputObject.message.content,
                        senderId = aiMessagesUiState.value.currChatInfo.members.firstOrNull { it -> it != currentUserName }
                            ?: "",
                        status = messageStatus.Send,
                        timeStamp = Timestamp.now()
                    )
//                    backgroundScope.launch() {
//
////            delay(500)
//                        try {
//
//                            val messageId = dataRepository.sendMessage(
//                                message = newMessage,
//                                chatId = messagesUiState.value.chatID,
//                                secureAESKey = messagesUiState.value.currChat.secureAESKey,
//                                fcmTokens = messagesUiState.value.currChat.membersData.map { it.fcmToken }
//                                    .flatten()
////                    chatId = "12345677"
//                            )
//
//
////                messagesUiState.value.messages.set(
////                    index = messagesUiState.value.messages.indexOf(newMessage),
////                    element = newMessage.copy(status = messageStatus.Send)
////                )
//                            Log.d(TAGmess, "Message id: $messageId")
//                            messagesUiState.update {
//                                it.copy(
//                                    errorMessage = "No error : ${newMessage.timeStamp}",
//                                    messages = it.messages + newMessage
////                        messages = it.messages - newMessage
//                                )
//                            }
//
//                            Log.d(TAGmess, "Message sent successfully for ai ========>--------------->")

//                            return@launch
////                getMessages(true)
//
//                        } catch (e: Exception) {
//                            Log.e(TAGmess, "Unable to send the message : $e")
//                            try {
////                    messagesUiState.value.messages.set(
////                        index = messagesUiState.value.messages.indexOf(newMessage),
////                        element = newMessage.copy(status = messageStatus.Error)
////                    )
//                                messagesUiState.update {
//                                    it.copy(
//                                        messages = updateElement(
//                                            it.messages,
//                                            index = it.messages.indexOf(newMessage),
//                                            newElement = newMessage.copy(status = messageStatus.Error)
//                                        )
//                                    )
//                                }
//                                return@launch
//                            } catch (e: Exception) {
//                                Log.e(TAGmess, "Unable to update the message status : $e")
//
////                    Log.e(TAGmess,"Unable to send the message")
////                throw Exception("Unable to send the message : ${status.await()}")
//                                messagesUiState.update {
//                                    it.copy(
////                        messageScreen = MessageScreen.Error,
////                        isError = true,
//                                        errorMessage = "${e.message.toString()} : ${newMessage.timeStamp}"
//                                    )
//                                }
//                                Log.e(TAGmess, "Error sending message :$e")
//                                return@launch
//                            }
//                        }
//                    }
                    Log.d(AiMessagesTag,"last index in finished. ${lastIndexAdded.value} ${aiMessagesUiState.value.aiMessages.size}")
                    val model =
                        aiMessagesUiState.value.currChatInfo.members.firstOrNull { it -> it != currentUserName }
                            ?: ""
                    sendMessageCloud(messageTextToSend = outputObject.message.content, senderId = model)
                    aiMessagesUiState.update {
                        it.copy(
                            aiMessages = updateElement(
                                list = it.aiMessages, index = lastIndexAdded.value,
                                newElement = AiResponseUiState
                                    (
                                    aiResponse = AiResponse(
                                        messages("assistant", outputObject.message.content),
                                        done = true
                                    ), aiState = AiState.Success
                                )
                            )
                        )
                    }
                    AiResponseUiState(
                        AiResponse(
                            message = messages(
                                "assistant",
                                content = outputObject.message.content
                            ), true
                        ), AiState.Success
                    )
                } catch (e: Exception) {
                    Log.e(AiMessagesTag, "Got error in the output data $e")
                    aiMessagesUiState.update {
                        it.copy(
                            aiMessages = updateElement(
                                list = it.aiMessages, index = lastIndexAdded.value,
                                newElement = AiResponseUiState
                                    (
                                    aiResponse = AiResponse(
                                        messages("assistant", outputData),
                                        done = true
                                    ), aiState = AiState.Success
                                )
                            )
                        )
                    }
                    AiResponseUiState(
                        AiResponse(
                            message = messages(
                                "assistant",
                                content = outputData
                            ), true
                        ), AiState.Success
                    )
                }
            }

            else -> {
                val streamData = info.outputData.getString("AI_STREAM_FULL")
                Log.d(AiMessagesTag, "Output dta for the model loading $outputData")
                AiResponseUiState(
                    AiResponse(
                        message = messages("assistant", streamData ?: "No stream"),
                        false
                    ), AiState.Loading
                )
            }
        }
//        }
    }

    private fun syncMessages (){
        viewModelScope.launch {
            Log.d(AiMessagesTag,"Ai Live messages started getting here.")
            dataRepository.getLiveMessages(
                chatId = aiMessagesUiState.value.chatId,
                secureAESKey = aiMessagesUiState.value.currChatInfo.secureAESKey,
                onMessagesChange = {

                },
                onError = {},
                onAdd = { message->
                    val newMessageList = (aiMessagesUiState.value.messages + message).sortedBy { it.timeStamp }
                    aiMessagesUiState.update { state ->
                        state.copy(
                            messages = newMessageList,
                            aiMessages = newMessageList.map { mess->
//                                Log.d(AiMessagesTag,"Mapping this message text- ${mess.content}")
                                val role = if(mess.senderId == aiMessagesUiState.value.currentUser){
                                    "user"
                                }else{
                                    "assistant"
                                }
                                AiResponseUiState(aiResponse = AiResponse(message = messages(role,mess.content),true),
                                    AiState.Success)
                            })
                    }
//                    Log.d(AiMessagesTag,"Storing message text -  ${message.content}")

                },
                onDelete = {}
            )
        }
    }

    fun sendMessageToAi() {
        Log.d(AiMessagesTag, "Calling ai messate to send")
        val tempMessage = aiMessagesUiState.value.messageToSend
        aiMessagesUiState.update {
            it.copy(messageToSend = "")
        }
        val currentUserName = aiMessagesUiState.value.currentUser
        val model =
            aiMessagesUiState.value.currChatInfo.members.firstOrNull { it -> it != currentUserName }
                ?: ""
        if (aiMessagesUiState.value.currChatInfo.isAiChat) {
            val returnUuid = aiApis.sendMessage(tempMessage, model)
            Log.d(AiMessagesTag, "Got the UUId $returnUuid")
            aiMessageUUID.value = returnUuid
            aiMessagesUiState.update {
                it.copy(
                    aiMessages = aiMessagesUiState.value.aiMessages.plus(
                        AiResponseUiState
                            (
                            aiResponse = AiResponse(
                                messages("user", tempMessage),
                                done = true
                            ), aiState = AiState.Success
                        )
                    )
                )
            }
            lastIndexAdded.value = aiMessagesUiState.value.aiMessages.size
        }
    }

    fun sendStreamingMessageToAi() {
        Log.d(AiMessagesTag, "Calling ai steaming message")
        val tempMessage = aiMessagesUiState.value.messageToSend
        aiMessagesUiState.update {
            it.copy(
                messageToSend = ""
            )
        }
        val currentUserName = aiMessagesUiState.value.currentUser
        val model =
            aiMessagesUiState.value.currChatInfo.members.firstOrNull { it -> it != currentUserName }
                ?: ""
        if (aiMessagesUiState.value.currChatInfo.isAiChat) {
            val contextMessage:List<messages> = aiMessagesUiState.value.aiMessages.map { it-> messages(it.aiResponse.message.role,it.aiResponse.message.content) }.takeLast(7)
            val returnUuid = aiApis.sendStreamMessage(tempMessage, model, context =contextMessage)
            Log.d(AiMessagesTag, "Got the UUId $returnUuid")
            aiMessageUUID.value = returnUuid
            aiMessagesUiState.update {
                it.copy(
                    aiMessages = aiMessagesUiState.value.aiMessages.plus(
                        AiResponseUiState
                            (
                            aiResponse = AiResponse(
                                messages("user", tempMessage),
                                done = true
                            ), aiState = AiState.Success
                        )
                    )
                )
            }
            lastIndexAdded.value = aiMessagesUiState.value.aiMessages.size
            Log.d(AiMessagesTag,"last index ${lastIndexAdded.value} ${aiMessagesUiState.value.aiMessages.size}")
            aiMessagesUiState.update {
                it.copy(
                    aiMessages = aiMessagesUiState.value.aiMessages.plus(
                        AiResponseUiState
                            (
                            aiResponse = AiResponse(
                                messages("assistant", ""),
                                done = true
                            ), aiState = AiState.Loading
                        )
                    )
                )
            }
            sendMessageCloud(messageTextToSend = tempMessage, senderId = aiMessagesUiState.value.currentUser)
            Log.d(AiMessagesTag,"last index  after dummy addition${lastIndexAdded.value} ${aiMessagesUiState.value.aiMessages.size}")
        }
    }

    private fun sendMessageCloud(messageTextToSend:String,senderId: String) {

//        val tempMessageOutside = messagesUiState.value.messageToSend
        viewModelScope.launch() {
//            val tempMessage = messagesUiState.value.messageToSend
//            val parsedContent = parseMessage(tempMessage)
            val newMessage = MessageReceived(
                contentType = ContentType.text,
//                parsedContent = "",
                content = messageTextToSend,
                senderId = senderId,
                status = messageStatus.Sending,
                timeStamp = Timestamp.now()
            )
//        messagesUiState.value.messages.add(
//            newMessage
//        )

//            aiMessagesUiState.update {
//                it.copy(
//                    messageToSend = "",
//                    messages = it.messages + newMessage
//                )
//            }
            Log.d(AiMessagesTag, "Message status sending from here....")

//            delay(500)
            try {

                val messageId = dataRepository.sendMessage(
                    message = newMessage,
                    chatId = aiMessagesUiState.value.chatId,
                    secureAESKey = aiMessagesUiState.value.currChatInfo.secureAESKey,
                    fcmTokens = aiMessagesUiState.value.currChatInfo.membersData.map { it.fcmToken }
                        .flatten()
//                    chatId = "12345677"
                )


//                messagesUiState.value.messages.set(
//                    index = messagesUiState.value.messages.indexOf(newMessage),
//                    element = newMessage.copy(status = messageStatus.Send)
//                )
                Log.d(AiMessagesTag, "Message id: $messageId")
                aiMessagesUiState.update {
                    it.copy(
//                        errorMessage = "No error : ${newMessage.timeStamp}",
                        messages = updateElement(
                            it.messages,
                            index = it.messages.indexOf(newMessage),
                            newElement = newMessage.copy(
                                messageId = messageId,
                                status = messageStatus.Send
                            )
                        )
//                        messages = it.messages - newMessage
                    )
                }

                Log.d(AiMessagesTag, "Message sent successfully ========>--------------->")
                return@launch
//                getMessages(true)

            } catch (e: Exception) {
                Log.e(AiMessagesTag, "Unable to send the message : $e")
                try {
//                    messagesUiState.value.messages.set(
//                        index = messagesUiState.value.messages.indexOf(newMessage),
//                        element = newMessage.copy(status = messageStatus.Error)
//                    )
                    aiMessagesUiState.update {
                        it.copy(
                            messages = updateElement(
                                it.messages,
                                index = it.messages.indexOf(newMessage),
                                newElement = newMessage.copy(status = messageStatus.Error)
                            )
                        )
                    }
                    return@launch
                } catch (e: Exception) {
                    Log.e(AiMessagesTag, "Unable to update the message status : $e")

//                    Log.e(TAGmess,"Unable to send the message")
//                throw Exception("Unable to send the message : ${status.await()}")
                    aiMessagesUiState.update {
                        it.copy(
//                        messageScreen = MessageScreen.Error,
//                        isError = true,
//                            errorMessage = "${e.message.toString()} : ${newMessage.timeStamp}"
                        )
                    }
                    Log.e(AiMessagesTag, "Error sending message :$e")
                    return@launch
                }
            }
        }
    }

    fun getChatInfo() {
//        aiMessagesUiState.update {
//            it.copy(
//                messageScreen = MessageScreen.Loading
//            )
//        }
        try {
            viewModelScope.launch {
                val currChat: Deferred<ChatOrGroup> =
                    async {
                        dataRepository.getDataChat(
                            chatId = aiMessagesUiState.value.chatId,
                            chatName = aiMessagesUiState.value.currentUser
                        )
                    }
//                messagesUiState.update { it.copy(
//                    currChat = currChat.await(),
//                    messageScreen = MessageScreen.Success
//                ) }
                if (currChat.await().chatName == "") {
//                    aiMessagesUiState.update {
//                        it.copy(
//                            messageScreen = MessageScreen.Error,
//                            isError = true,
//                            errorMessage = currChat.await().chatPic
//                        )
//                    }
//                    Log.e(
//                        TAGmess,
//                        "Unable to get the chat info : ${messagesUiState.value.errorMessage} , ${messagesUiState.value.currChat.chatName}"
//                    )
                    return@launch
                } else {
                    aiMessagesUiState.update {
                        it.copy(
                            currChatInfo = currChat.await(),
//                            messageScreen = MessageScreen.Success
                        )
                    }
                    getModelOnlineStatus()
                    syncMessages()
                    Log.d(TAGmess, "Got chat info success")
                }
//                return@launch
            }
        } catch (e: Exception) {
            Log.e(TAGmess, "Unable to get the chat info : $e")
//            messagesUiState.update {
//                it.copy(
//                    messageScreen = MessageScreen.Error,
//                    isError = true,
//                    errorMessage = e.message.toString()
//                )
//            }
            return
        }
    }

    private fun getModelOnlineStatus(){
        aiApis.getAiTags()
    }

    fun editMessageToSend(msg: String) {
        aiMessagesUiState.update {
            it
                .copy(
                    messageToSend = msg
                )
        }
    }
}

enum class AiState() {
    Loading,
    Error,
    Success,
    Streaming
}

enum class ModelState(){
    Online,
    Offline,
    NotFound,
    Loading,
}

data class AiResponseUiState(
    val aiResponse: AiResponse = AiResponse(
        message = messages("assistant", "Initial state!!"),
        true
    ),
    val aiState: AiState = AiState.Success
)

data class AiMessagesUiState(
    val chatId: String = "",
    val currChatInfo: ChatOrGroup = ChatOrGroup(),
    val currentUser: String = "",
    val messageToSend: String = "",
    val messages: List<MessageReceived> = listOf(),
    val aiMessages: List<AiResponseUiState> = listOf()
)