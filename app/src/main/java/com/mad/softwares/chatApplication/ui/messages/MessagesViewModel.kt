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
import com.mad.softwares.chatApplication.data.models.OllamaModel
import com.mad.softwares.chatApplication.data.models.ollamaResponse
import com.mad.softwares.chatApplication.data.models.tags
import com.mad.softwares.chatApplication.network.AiApiLocalhost
import com.mad.softwares.chatApplication.ui.updateElement
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

val TAGmess = "messagesViewModel"

class MessagesViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val dataRepository: DataRepository,
    private val aiApis : WorkRespository
) : ViewModel() {
    var messagesUiState = MutableStateFlow(MessagesUiState())
        private set

    init {
        val chatID_username =
            savedStateHandle.get<String>(messagesdestinationData.chatIDAndUsername)
        val chatID = chatID_username?.split(",")?.get(0)
        val chatName = chatID_username?.split(",")?.get(1)
        Log.d(TAGmess, "Message init : $chatName, $chatID")
//        getTags()

        messagesUiState.update {
            it.copy(
                chatID = chatID ?: "No Id here",
                currentUser = chatName ?: "No username here"
//                chatName = chatName ?: "No username here"
            )
        }
        getChatInfo()

//        getMessages()
//        getLiveMessages()
    }

    fun getTags(){
        viewModelScope.launch {
            try {
                aiApis.getAiTags()
                aiApis.sendMessage()
            }
            catch (e: Exception){
                Log.e(TAGmess,"Error getting tags",e)
            }
        }
    }

    val aiMessage: StateFlow<tags> = aiApis.TagsInfo
        .map{
            info ->
            val outputData = info.outputData.getString("AI_TAGS_RESPONSE")
            Log.d(TAGmess,"Output dta for the model $outputData")
//            val outputObject = Gson().fromJson(outputData, tags::class.java)
            when{
                outputData.isNullOrEmpty() ->{
                    tags(listOf(OllamaModel(model = "Loading...",name="loading...")))
                }
                info.state == WorkInfo.State.RUNNING -> {
                    Log.d(TAGmess,"Output dta for the model loading $outputData")
                    tags(listOf(OllamaModel(model = "Loading...",name="loading...")))
                }
                info.state.isFinished ->{
                    Log.d(TAGmess,"Output dta for the model Finished $outputData")
                    val outputObject = Gson().fromJson(outputData, tags::class.java)
                    outputObject
                }
                else ->{
                    tags(listOf(OllamaModel(
                        name = "Loading ...",
                        model = "Loading models...."
                    )))
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = tags(listOf(OllamaModel(name="Starting",model="Starting")))
        )


    fun getChatInfo() {
        messagesUiState.update {
            it.copy(
                messageScreen = MessageScreen.Loading
            )
        }
        try {
            viewModelScope.launch {
                val currChat: Deferred<ChatOrGroup> =
                    async {
                        dataRepository.getDataChat(
                            chatId = messagesUiState.value.chatID,
                            chatName = messagesUiState.value.currentUser
                        )
                    }
//                messagesUiState.update { it.copy(
//                    currChat = currChat.await(),
//                    messageScreen = MessageScreen.Success
//                ) }
                if (currChat.await().chatName == "") {
                    messagesUiState.update {
                        it.copy(
                            messageScreen = MessageScreen.Error,
                            isError = true,
                            errorMessage = currChat.await().chatPic
                        )
                    }
                    Log.e(
                        TAGmess,
                        "Unable to get the chat info : ${messagesUiState.value.errorMessage} , ${messagesUiState.value.currChat.chatName}"
                    )
                    return@launch
                } else {
                    messagesUiState.update {
                        it.copy(
                            currChat = currChat.await(),
                            messageScreen = MessageScreen.Success
                        )
                    }
                    getLiveMessages()
                    Log.d(TAGmess, "Got chat info success")
                }
//                return@launch
            }
        } catch (e: Exception) {
            Log.e(TAGmess, "Unable to get the chat info : $e")
            messagesUiState.update {
                it.copy(
                    messageScreen = MessageScreen.Error,
                    isError = true,
                    errorMessage = e.message.toString()
                )
            }
            return
        }
    }

    fun messageEdit(content: String) {
//        val newContent = extractAndFormatCode(content)?:content
        messagesUiState.update {
            it.copy(
                messageToSend = content
            )
        }
    }

    private val viewModelJob = SupervisorJob()
    private val backgroundScope = CoroutineScope(Dispatchers.IO + viewModelJob)

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch { dataRepository.stopLiveMessages() }
//        viewModelJob.cancel()
    }

    fun sendTextMessage() {
//        val sender: List<String> =
//            messagesUiState.value.currChat.members - messagesUiState.value.currChat.membersData.map { it.username }
//                .toSet()
//        Log.d(TAGmess,"members are in sender : ${sender}")
//        val senderUsername = sender.get(0)
        val tempMessage = messagesUiState.value.messageToSend
        val newMessage = MessageReceived(
            contentType = ContentType.text,
            content = messagesUiState.value.messageToSend,
            senderId = messagesUiState.value.currentUser,
            status = messageStatus.Sending,
            timeStamp = Timestamp.now()
        )
//        messagesUiState.value.messages.add(
//            newMessage
//        )

        messagesUiState.update {
            it.copy(
                messageToSend = "",
                messages = it.messages + newMessage
            )
        }
        Log.d(TAGmess, "Message status = ${messagesUiState.value.messages.last().status}")
//        messagesUiState.value.messages.set(
//            index= messagesUiState.value.messages.indexOf(messagesUiState.value.messageToSend),
//            element = messagesUiState.value.messageToSend.copy(status = messageStatus.Send)
//        )

//        viewModelScope.launch {
//            try{
//                Log.d(TAGmess, "Sending message")
//                Log.d(TAGmess, "Current chat : ${messagesUiState.value.currChat}")
//                val currCht = messagesUiState.value.currChat
//                val title = currCht.members - currCht.membersData.map { it.username }.toSet()
//                messagesUiState.value.currChat.membersData.forEach { charUser ->
//                    charUser.fcmToken.forEach { token ->
//                        Log.d(
//                            TAGmess,
//                            "Sending notification to : ${charUser.username}, chatname : ${title.first()}, token : ${token}"
//                        )
//                        dataRepository.sendNotificationToToken(
//                            token = token,
//                            title = title.first(),
//                            content = tempMessage
//                        )
//                    }
//                }
//            }
//            catch(e:Exception){
//                Log.e(TAGmess,"Error to send notification: $e")
//            }
//        }
        backgroundScope.launch() {

//            delay(500)
            try {

                val messageId = dataRepository.sendMessage(
                    message = newMessage,
                    chatId = messagesUiState.value.chatID,
                    secureAESKey = messagesUiState.value.currChat.secureAESKey,
                    fcmTokens = messagesUiState.value.currChat.membersData.map { it.fcmToken }
                        .flatten()
//                    chatId = "12345677"
                )


//                messagesUiState.value.messages.set(
//                    index = messagesUiState.value.messages.indexOf(newMessage),
//                    element = newMessage.copy(status = messageStatus.Send)
//                )
                Log.d(TAGmess, "Message id: $messageId")
                messagesUiState.update {
                    it.copy(
                        errorMessage = "No error : ${newMessage.timeStamp}",
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

                Log.d(TAGmess, "Message sent successfully ========>--------------->")
                return@launch
//                getMessages(true)

            } catch (e: Exception) {
                Log.e(TAGmess, "Unable to send the message : $e")
                try {
//                    messagesUiState.value.messages.set(
//                        index = messagesUiState.value.messages.indexOf(newMessage),
//                        element = newMessage.copy(status = messageStatus.Error)
//                    )
                    messagesUiState.update {
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
                    Log.e(TAGmess, "Unable to update the message status : $e")

//                    Log.e(TAGmess,"Unable to send the message")
//                throw Exception("Unable to send the message : ${status.await()}")
                    messagesUiState.update {
                        it.copy(
//                        messageScreen = MessageScreen.Error,
//                        isError = true,
                            errorMessage = "${e.message.toString()} : ${newMessage.timeStamp}"
                        )
                    }
                    Log.e(TAGmess, "Error sending message :$e")
                    return@launch
                }
            }
        }
    }

    fun sendMdMessage() {
//        val sender: List<String> =
//            messagesUiState.value.currChat.members - messagesUiState.value.currChat.membersData.map { it.username }
//                .toSet()
//        Log.d(TAGmess,"members are in sender : ${sender}")
//        val senderUsername = sender.get(0)
        val tempMessage = messagesUiState.value.messageToSend
        val newMessage = MessageReceived(
            contentType = ContentType.Md,
            content = messagesUiState.value.messageToSend,
            senderId = messagesUiState.value.currentUser,
            status = messageStatus.Sending,
            timeStamp = Timestamp.now()
        )
//        messagesUiState.value.messages.add(
//            newMessage
//        )

        messagesUiState.update {
            it.copy(
                messageToSend = "",
                messages = it.messages + newMessage
            )
        }
        Log.d(TAGmess, "Message status = ${messagesUiState.value.messages.last().status}")
//        messagesUiState.value.messages.set(
//            index= messagesUiState.value.messages.indexOf(messagesUiState.value.messageToSend),
//            element = messagesUiState.value.messageToSend.copy(status = messageStatus.Send)
//        )

//        viewModelScope.launch {
//            try{
//                Log.d(TAGmess, "Sending message")
//                Log.d(TAGmess, "Current chat : ${messagesUiState.value.currChat}")
//                val currCht = messagesUiState.value.currChat
//                val title = currCht.members - currCht.membersData.map { it.username }.toSet()
//                messagesUiState.value.currChat.membersData.forEach { charUser ->
//                    charUser.fcmToken.forEach { token ->
//                        Log.d(
//                            TAGmess,
//                            "Sending notification to : ${charUser.username}, chatname : ${title.first()}, token : ${token}"
//                        )
//                        dataRepository.sendNotificationToToken(
//                            token = token,
//                            title = title.first(),
//                            content = tempMessage
//                        )
//                    }
//                }
//            }
//            catch(e:Exception){
//                Log.e(TAGmess,"Error to send notification: $e")
//            }
//        }
        backgroundScope.launch() {

//            delay(500)
            try {

                val messageId = dataRepository.sendMessage(
                    message = newMessage,
                    chatId = messagesUiState.value.chatID,
                    secureAESKey = messagesUiState.value.currChat.secureAESKey,
                    fcmTokens = messagesUiState.value.currChat.membersData.map { it.fcmToken }
                        .flatten()
//                    chatId = "12345677"
                )


//                messagesUiState.value.messages.set(
//                    index = messagesUiState.value.messages.indexOf(newMessage),
//                    element = newMessage.copy(status = messageStatus.Send)
//                )
                Log.d(TAGmess, "Message id: $messageId")
                messagesUiState.update {
                    it.copy(
                        errorMessage = "No error : ${newMessage.timeStamp}",
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

                Log.d(TAGmess, "Message sent successfully ========>--------------->")
                return@launch
//                getMessages(true)

            } catch (e: Exception) {
                Log.e(TAGmess, "Unable to send the message : $e")
                try {
//                    messagesUiState.value.messages.set(
//                        index = messagesUiState.value.messages.indexOf(newMessage),
//                        element = newMessage.copy(status = messageStatus.Error)
//                    )
                    messagesUiState.update {
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
                    Log.e(TAGmess, "Unable to update the message status : $e")

//                    Log.e(TAGmess,"Unable to send the message")
//                throw Exception("Unable to send the message : ${status.await()}")
                    messagesUiState.update {
                        it.copy(
//                        messageScreen = MessageScreen.Error,
//                        isError = true,
                            errorMessage = "${e.message.toString()} : ${newMessage.timeStamp}"
                        )
                    }
                    Log.e(TAGmess, "Error sending message :$e")
                    return@launch
                }
            }
        }
    }

    //Deprecated
    //    fun getMessages(isForced: Boolean = false) {
//        if (!isForced && messagesUiState.value.messages.isNotEmpty()) {
//            return
//        }
//        messagesUiState.update {
//            it.copy(
//                messageScreen = MessageScreen.Loading
//            )
//        }
//        viewModelScope.launch {
//            try {
//                val messages = async {
//                    dataRepository.getMyMessages(
//                        currentChatId = messagesUiState.value.chatID
//                    )
//                }
//                if(messages.await().get(0).senderId == "ErrorSerious"){
////                    messagesUiState.update {
////                        it.copy(
////                            messageScreen = MessageScreen.Error,
////                            isError = true,
////                            errorMessage = messages.await().get(0).content)}
//                    throw Exception(messages.await().get(0).content)
//                }
//                messagesUiState.update {
//                    it.copy(
//                        messages = messages.await().toMutableList(),
//                        messageScreen = MessageScreen.Success
//                    )
//                }
//                Log.d(TAGmess, "Got the messages success")
//            } catch (e: Exception) {
//                Log.e(TAGmess, "Unable to get the messages : $e")
//                messagesUiState.update {
//                    it.copy(
//                        messageScreen = MessageScreen.Error,
//                        isError = true,
//                        errorMessage = e.message.toString()
//                    )
//                }
//            }
//        }
//    }

    private fun updateMessageList(
        messages: MutableList<MessageReceived>,
        newMessage: MessageReceived
    ): List<MessageReceived> {
        val index = messages.indexOfFirst { message -> message.messageId == newMessage.messageId }
        if (index != -1) {
            val oldMessage = messages[index]
            messages[index] = oldMessage.copy(
                content = newMessage.content,
                contentType = newMessage.contentType,
            )
            return messages
        } else {
            return messages
        }
    }

    private fun getLiveMessages() {
        viewModelScope.launch {
            Log.d(TAGmess, "Live messages started here")
            dataRepository.getLiveMessages(
                chatId = messagesUiState.value.chatID,
                secureAESKey = messagesUiState.value.currChat.secureAESKey,
                //Not in use.
                onMessagesChange = { messageUpdate ->
                    Log.d(TAGmess, "Update Message is: ${messageUpdate.messageId}")
                    messagesUiState.update {
                        it.copy(
                            messages = updateMessageList(
                                messages = messagesUiState.value.messages.toMutableList(),
                                newMessage = messageUpdate
                            )
                        )
                    }
                },
                onError = { e ->
                    Log.e(TAGmess, "Error getting messages : $e")
                    messagesUiState.update {
                        it.copy(
                            isError = true,
                            errorMessage = e.message.toString()
                        )
                    }
                },
                onAdd = { message ->
                    Log.d(TAGmess, "New Message added : ${message}")
                    messagesUiState.update {
                        it.copy(
                            messages =
                                if (it.messages.contains(message)) {
                                    Log.d(TAGmess, "Message already exists : ${message}")
                                    (it.messages + message).sortedBy { it.timeStamp }
                                } else {
                                    Log.d(TAGmess, "Message does not exists : ${message}")
                                    (it.messages + message).sortedBy { it.timeStamp }
                                }
                        )
                    }
                },
                onDelete = { message ->
                    Log.d(TAGmess, "Messages Delete : ${message.messageId}")
                    messagesUiState.update {
                        it.copy(
                            messages =
                                if (it.messages.contains(message)) {
                                    Log.d(
                                        TAGmess,
                                        "Message already exists delete : ${message.messageId}"
                                    )
                                    (it.messages - message).sortedBy { it.timeStamp }
                                } else {
                                    Log.d(
                                        TAGmess,
                                        "Message does not exists delete : ${message.messageId}"
                                    )
                                    (it.messages - message).sortedBy { it.timeStamp }
                                }
                        )
                    }
                }
            )
        }
    }

    fun deleteMessages() {
        val chatId = messagesUiState.value.chatID
        messagesUiState.value.selectedSentMessages.forEach { message ->
            Log.d(TAGmess, "Deleting message ${message.content}")
            messagesUiState.update {
                it.copy(
                    messages = updateElement(
                        it.messages,
                        index = it.messages.indexOf(message),
                        newElement = message.copy(

                            status = messageStatus.Deleting
                        )
                    )
                )
            }
        }
        viewModelScope.launch {
//            delay(5000)
            try {
                messagesUiState.value.selectedSentMessages.forEach { message ->
//                    messagesUiState.update { it.copy(
//                        messages = updateMessageList(
//                            messages = it.messages.toMutableList(),
//                            newMessage = message.copy(status = messageStatus.Deleting)
//                        )
//                    ) }
                    dataRepository.deleteAMessage(
                        chatId = chatId,
                        messageId = message.messageId,
                        error = {
                            Log.e(TAGmess, "We got error deleteting message inside Api: $it")
                            return@deleteAMessage
                        },
                        secureAESKey = messagesUiState.value.currChat.secureAESKey

                    )
                    messagesUiState.update {
                        it.copy(
                            messages = it.messages - message.copy(status = messageStatus.Deleting),
                            selectedSentMessages = it.selectedSentMessages - message,

                            )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAGmess, "We got error while deleting message : $e")
            }
        }
    }

    fun toggleMessageSelection(
        message: MessageReceived,
        statue: Boolean,
        isReceiver: Boolean = true
    ) {
        if (isReceiver) {
            if (statue) {
                messagesUiState.update {
                    it.copy(
                        selectedReceivedMessages = it.selectedReceivedMessages + message,
                        isSenderOnlySelected = false,
                        messageScreen = MessageScreen.SelectionMode
                    )
                }
            } else {
                if (messagesUiState.value.selectedReceivedMessages.size != 1) {
                    messagesUiState.update {
                        it.copy(
                            selectedReceivedMessages = it.selectedReceivedMessages - message,
                            isSenderOnlySelected = false,
                            messageScreen = MessageScreen.SelectionMode
                        )
                    }
                } else {
                    messagesUiState.update {
                        it.copy(
                            selectedReceivedMessages = listOf(),
                            isSenderOnlySelected = true,
                            messageScreen = if (it.selectedSentMessages.isEmpty() ) MessageScreen.Success else MessageScreen.SelectionMode
                        )
                    }
                }
            }
        } else {
            if (statue) {
                messagesUiState.update {
                    it.copy(
                        selectedSentMessages = it.selectedSentMessages + message,
                        isSenderOnlySelected = true,
                        messageScreen = MessageScreen.SelectionMode
                    )
                }


            } else {
                if (messagesUiState.value.selectedSentMessages.size != 1) {
                    messagesUiState.update {
                        it.copy(
                            selectedSentMessages = it.selectedSentMessages - message,
                            isSenderOnlySelected = false,
                            messageScreen = MessageScreen.SelectionMode
                        )
                    }
                } else {
                    messagesUiState.update {
                        it.copy(
                            selectedSentMessages = listOf(),
                            isSenderOnlySelected = false,
                            messageScreen = if (it.selectedReceivedMessages.isEmpty()) MessageScreen.Success else MessageScreen.SelectionMode
                        )
                    }
                }
            }


        }
    }

    fun deSelectAll(
    ) {
        messagesUiState.update {
            it.copy(
                selectedReceivedMessages = listOf(),
                selectedSentMessages = listOf(),
                isSenderOnlySelected = false,
                messageScreen = MessageScreen.Success
            )
        }
    }

    fun startSelectionMode() {
        messagesUiState.update {
            it.copy(
                messageScreen = MessageScreen.SelectionMode
            )
        }
    }

    fun setSelectedMessage(message: MessageReceived){
        messagesUiState.update {
            it.copy(
                selectedMessageForNextScreen = message
            )
        }
    }

}


data class MessagesUiState(
    val chatID: String = "",
    val currChat: ChatOrGroup = ChatOrGroup(),
    val chatName: String = "",
    val currentUser: String = "",
//    val isLoading:Boolean = false,
    val isError: Boolean = false,
    val messageScreen: MessageScreen = MessageScreen.Loading,
    val errorMessage: String = "",
    val messageToSend: String = "",
    val messages: List<MessageReceived> = listOf(),
    val selectedReceivedMessages: List<MessageReceived> = listOf(),
    val selectedSentMessages: List<MessageReceived> = listOf(),
    val isSenderOnlySelected: Boolean = false,
    val selectionMode: Boolean = false,
    val selectedMessageForNextScreen: MessageReceived = MessageReceived()
)


enum class MessageScreen() {
    Loading,
    Error,
    Success,
    SelectionMode
}