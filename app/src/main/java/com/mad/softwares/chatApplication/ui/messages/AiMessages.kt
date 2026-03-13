package com.mad.softwares.chatApplication.ui.messages

import StyledTextVisualTransformation
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Attachment
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mad.softwares.chatApplication.R
import com.mad.softwares.chatApplication.data.ChatOrGroup
import com.mad.softwares.chatApplication.data.MessageReceived
import com.mad.softwares.chatApplication.data.models.AiResponse
import com.mad.softwares.chatApplication.data.models.messages
import com.mad.softwares.chatApplication.ui.ApptopBar
import com.mad.softwares.chatApplication.ui.GodViewModelProvider
import com.mad.softwares.chatApplication.ui.LoadingIndicator
import com.mad.softwares.chatApplication.ui.destinationData
import com.mad.softwares.chatApplication.ui.theme.ChitChatTheme
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.model.rememberMarkdownState
import kotlinx.coroutines.delay

object AiMessagesDestinationData : destinationData {
    override val route: String = "AiMessages"
    override val title: Int = R.string.ai_messages
    override val canBack: Boolean = true

    val chatIDAndUsername = "chatID,username"
    val routeWithArgs = "$route/{$chatIDAndUsername}"
    val nestedGraphMessages = "NestedGraphAiMessages"
}

@Composable
fun AiMessages(
    viewModel: AiMessagesViewModel = viewModel(factory = GodViewModelProvider.Factory),
    navigateUp: () -> Unit,
) {
    val uiState = viewModel.aiMessagesUiState.collectAsState().value
    AiMessageBody(
        uiState = uiState,
        liveAiUiState = viewModel.aiMessage.collectAsState().value,
        updateMesssage = viewModel::editMessageToSend,
        sendTestMessage = viewModel::sendMessageToAi,
        sendStreamTest = viewModel::sendStreamingMessageToAi,
        navigateUp = navigateUp
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiMessageBody(
    uiState: AiMessagesUiState,
    liveAiUiState: AiResponseUiState,
    updateMesssage: (String) -> Unit,
    sendTestMessage: () -> Unit,
    sendStreamTest: () -> Unit,
    navigateUp: () -> Unit,
) {
//    Column(
//        modifier = Modifier
//            .padding(5.dp)
//    ){
//        TextField(
//            value = uiState.messageToSend,
//            onValueChange = updateMesssage
//        )
//        Button(onClick = sendTestMessage) {
//            Text("Send message to Ai")
//        }
//        Row(){
//            Button(onClick = sendStreamTest) {
//                Text("Send Steaming to Ai")
//            }
//            if(liveAiUiState.aiState == AiState.Streaming){ CircularProgressIndicator() }
//        }
//
//        Text(text= liveAiUiState.aiResponse.message.content)
//
//    }

    Scaffold(
        topBar = {
            ApptopBar(
                destinationData = AiMessagesDestinationData,
                navigateUp = navigateUp,
                title = { Text(uiState.currChatInfo.chatName) }
            )
        },
        bottomBar = {AiBottomMessageSend(uiState = uiState, sendMessage = sendStreamTest, updateMessage = updateMesssage, isLoading = false, liveAiUiState = liveAiUiState)},
        modifier = Modifier
            .imePadding()
            .navigationBarsPadding()
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(
                    paddingValues
                )
                .fillMaxSize(),
            reverseLayout = true
        ) {
            items(items = uiState.aiMessages.reversed()) { message ->
                if(message.aiResponse.message.role === "user") {
                    UserChat(message = message)
                }
                else{
                    AiResponseChat(
                        message = message,
                    )
                }
            }
        }
    }
}

@Composable
fun AiResponseChat(message: AiResponseUiState){
    val markdownState = rememberMarkdownState(
        content = message.aiResponse.message.content,
        retainState = true // This is the magic flag that stops the flashing
    )
    Markdown(
        modifier = Modifier
            .padding(5.dp)
            .fillMaxWidth(),
        markdownState = markdownState
    )
    AnimatedVisibility(
        visible = message.aiState == AiState.Streaming || message.aiState == AiState.Loading,
        enter = slideInVertically { it } + fadeIn(),
        exit = slideOutVertically (
            targetOffsetY = {it},
            animationSpec = tween(delayMillis = 300)
        ) + fadeOut()
    ){
        Row(verticalAlignment = Alignment.CenterVertically){
            CircularProgressIndicator(
                modifier = Modifier
                    .padding(5.dp)
                    .width(24.dp)
                    .height(24.dp),
                strokeWidth = 3.dp
            )
            AnimatedContent(
                targetState = message.aiState,
                transitionSpec = {
                    (slideInVertically { it } togetherWith slideOutVertically {  -it })
                }
            ) { targetState ->
                when (targetState) {
                    AiState.Loading -> {
                        Text("Loading Message...")
                    }

                    AiState.Error -> {}
                    AiState.Success -> {Text("Success Got Response")}
                    AiState.Streaming -> {
                        Text("Getting Tokens response")
                    }
                }
            }
        }
    }
}

@Composable
fun UserChat(
    message: AiResponseUiState
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Card(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(0.9f)
                .wrapContentWidth(Alignment.End)
            ,
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            Text(
                message.aiResponse.message.content,
                modifier = Modifier
                    .padding(8.dp, 4.dp),
                fontSize = 20.sp
            )
        }
    }
}

@Composable
fun AiBottomMessageSend(
    modifier: Modifier = Modifier,
    uiState: AiMessagesUiState,
    sendMessage: () -> Unit,
//    sendExpensiveAiMessage:()->Unit,
    updateMessage: (String) -> Unit,
    isLoading: Boolean,
    liveAiUiState: AiResponseUiState
) {
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    Box {
        ElevatedCard(
            modifier = modifier
                .fillMaxWidth()
                .padding(8.dp)
                .background(Color.Transparent),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,

                ),
            shape = RoundedCornerShape(30.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            )
            {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    //                .height(height)
                    verticalAlignment = Alignment.Bottom,

                    ) {
                    OutlinedTextField(
                        modifier = Modifier
                            .weight(1f)
                            //                    .padding(end=40.dp)
                            .fillMaxWidth()
                            .padding(4.dp)
                            .animateContentSize(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessLow,
                                )
                            ),
                        value = uiState.messageToSend,
                        onValueChange = { updateMessage(it) },
                        textStyle = TextStyle.Default.copy(
                            fontSize = 20.sp,

                            ),
                        maxLines = 4,
                        placeholder = { Text(text = "Send Message to AI") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0, 0, 0, alpha = 0),
                            unfocusedBorderColor = Color(0, 0, 0, alpha = 0),
                            disabledBorderColor = Color(0, 0, 0, alpha = 0),
                        ),
                    )
                    //
                    AnimatedVisibility(
                        visible = (liveAiUiState.aiState !== AiState.Loading) && (liveAiUiState.aiState !== AiState.Error),
                        enter = slideInHorizontally { it },
                        exit = slideOutHorizontally { it }
                    ) {
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
//
                                sendMessage()
//

                            },

                            modifier = Modifier
                                //                        .fillMaxWidth()
                                .padding(bottom = 10.dp, end = 10.dp)
                                .size(45.dp),
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary

                            )
                        ) {
                            Icon(
                                imageVector = if (liveAiUiState.aiState === AiState.Success) {
                                    Icons.AutoMirrored.Filled.Send
                                } else {
                                    Icons.Default.Stop
                                },
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .padding(10.dp)
                                    .size(80.dp),
                                tint = MaterialTheme.colorScheme.onPrimary,
                                contentDescription = "send"
                            )

                        }
                    }
                }
            }

        }
    }
}

@Preview
@Composable
fun AiMessagePreview() {
    ChitChatTheme(dynamicColor = false) {
        AiMessageBody(
            uiState = AiMessagesUiState(
                currChatInfo = ChatOrGroup(
                    chatName = "Testing Ai"
                ),
                aiMessages = listOf(
                    AiResponseUiState(
                        AiResponse(message = messages("user","helloSir"),done = true),
                        aiState = AiState.Success
                    ),
                    AiResponseUiState(
                        AiResponse(message = messages("assistant","Completed Ai response"),done = true),
                        aiState = AiState.Loading
                    ),
                    AiResponseUiState(
                        AiResponse(message = messages("user","helloSir"),done = true),
                        aiState = AiState.Success
                    ),
                    AiResponseUiState(
                        AiResponse(message = messages("assistant","Ai Response on going..."),done = true),
                        aiState = AiState.Streaming
                    )
                )
            ),
            liveAiUiState = AiResponseUiState(
                AiResponse(message = messages("assistant", "Hello from ai ... typing more..."), done = false),
                aiState = AiState.Streaming
            ),
            sendTestMessage = {},
            updateMesssage = {},
            sendStreamTest = {},
            navigateUp = {}
        )
    }
}