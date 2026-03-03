package com.mad.softwares.chatApplication.ui.messages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mad.softwares.chatApplication.R
import com.mad.softwares.chatApplication.data.models.AiResponse
import com.mad.softwares.chatApplication.data.models.messages
import com.mad.softwares.chatApplication.ui.GodViewModelProvider
import com.mad.softwares.chatApplication.ui.LoadingIndicator
import com.mad.softwares.chatApplication.ui.destinationData
import com.mad.softwares.chatApplication.ui.theme.ChitChatTheme

object AiMessagesDestinationData: destinationData{
    override val route: String = "AiMessages"
    override val title: Int= R.string.ai_messages
    override val canBack: Boolean = true

    val chatIDAndUsername = "chatID,username"
    val routeWithArgs = "$route/{$chatIDAndUsername}"
    val nestedGraphMessages = "NestedGraphAiMessages"
}

@Composable
fun AiMessages(
    viewModel: AiMessagesViewModel = viewModel(factory = GodViewModelProvider.Factory)
){
    val uiState = viewModel.aiMessagesUiState.collectAsState().value
    AiMessageBody(
        uiState = uiState,
        liveAiUiState = viewModel.aiMessage.collectAsState().value,
        updateMesssage = viewModel::editMessageToSend,
        sendTestMessage = viewModel::sendMessageToAi,
        sendStreamTest = viewModel::sendStreamingMessageToAi
    )
}

@Composable
fun AiMessageBody(
    uiState: AiMessagesUiState,
    liveAiUiState: AiResponseUiState,
    updateMesssage:(String)->Unit,
    sendTestMessage: ()-> Unit,
    sendStreamTest:()->Unit,
){
    Column(
        modifier = Modifier
            .padding(5.dp)
    ){
        TextField(
            value = uiState.messageToSend,
            onValueChange = updateMesssage
        )
        Button(onClick = sendTestMessage) {
            Text("Send message to Ai")
        }
        Row(){
            Button(onClick = sendStreamTest) {
                Text("Send Steaming to Ai")
            }
            if(liveAiUiState.aiState == AiState.Streaming){ CircularProgressIndicator() }
        }

        Text(text= liveAiUiState.aiResponse.message.content)

    }
}

@Preview
@Composable
fun AiMessagePreview(){
    ChitChatTheme(dynamicColor = false) {
        AiMessageBody(
            uiState = AiMessagesUiState(),
            liveAiUiState = AiResponseUiState(
                AiResponse(message = messages("assistant","Hello from ai"), done = false),
                aiState = AiState.Streaming
            ),
            sendTestMessage = {},
            updateMesssage = {},
            sendStreamTest = {}
        )
    }
}