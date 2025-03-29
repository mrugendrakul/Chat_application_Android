package com.mad.softwares.chatApplication.ui.ShareHandle

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Attachment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.Timestamp
import com.mad.softwares.chatApplication.R
import com.mad.softwares.chatApplication.data.ChatOrGroup
import com.mad.softwares.chatApplication.ui.ApptopBar
import com.mad.softwares.chatApplication.ui.GodViewModelProvider
import com.mad.softwares.chatApplication.ui.LinearLoadingIndicator
import com.mad.softwares.chatApplication.ui.destinationData
import com.mad.softwares.chatApplication.ui.messages.BottomMessageSend
import com.mad.softwares.chatApplication.ui.theme.ChitChatTheme

@Composable
fun HandleSharedContent(
    intent: Intent,
    shareHandlerViewModel: ShareHandlerViewModel = viewModel(factory = GodViewModelProvider.Factory),
    completed: () -> Unit
) {
    // Determine the MIME type of the shared content
    val mimeType = intent.type
    val uiState = shareHandlerViewModel.shareHandlerUiState.collectAsState().value
    val sharedText = remember {
        if (mimeType == "text/plain") {
            intent.getStringExtra(Intent.EXTRA_TEXT)
        } else null
    }

    val sharedUrl = remember {
        if (mimeType == "text/uri-list") {
            intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)?.toString()
        } else null
    }
    if (sharedText?.startsWith("http") == true) {
        shareHandlerViewModel.updateShareDate("<$sharedText>",true)
    } else if (sharedText?.startsWith("http") == false) {
        shareHandlerViewModel.updateShareDate(sharedText,true)
    } else {
        shareHandlerViewModel.updateShareDate("<$sharedUrl>",true)
    }

    val context = LocalContext.current
    HandleSharedContentScreen(
        sharedText = sharedText,
        sharedUrl = sharedUrl,
        uiState = uiState,
        updateShareDate = { shareHandlerViewModel.updateShareDate(it,false) },
        addToSelection = { value, chat -> shareHandlerViewModel.toggleChats(value, chat) },
        sendMessage = {
            shareHandlerViewModel.sendMessage(
                onError = {

//                    Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                },
                onSuccess = {
                    completed()
                }
            )
//            completed()
        }
    )
}

object shareDestination : destinationData {
    override val canBack: Boolean = false
    override val route: String = "shareing"
    override val title: Int = R.string.share_the_data
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HandleSharedContentScreen(
    sharedText: String?,
    sharedUrl: String?,
    uiState: ShareHandlerUiState,
    updateShareDate: (String) -> Unit,
    addToSelection: (Boolean, ChatOrGroup) -> Unit,
    sendMessage: () -> Unit
) {
    Scaffold(
        topBar = {
            ApptopBar(
                destinationData = shareDestination,
                navigateUp = {}
            )
        },
        bottomBar = {
            BottomMessageShare(
                appUistate = uiState,
                sendMessage = sendMessage,
                updateMessage = updateShareDate
            )
        },
        modifier = Modifier
            .imePadding()
            .background(MaterialTheme.colorScheme.background)
    )
    {

        if (
            !uiState.isError
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
//                    .padding(16.dp)
            ) {
                items(
                    items = uiState.toShareChats
                ) { chat ->
                    SingleChatToShare(
                        chat = chat,    
                        currentUsername = "",
                        addToSelection = addToSelection,
                        isCardEnabled = true,
                        isCardSelected = uiState.selectedChats.contains(chat)
                    )
                }

            }
        } else {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(it)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Text(
                    modifier = Modifier
                        .padding(6.dp),
                    text = "Error Getting chats...."
                )
            }
        }
        LinearLoadingIndicator(isLoading = uiState.isLoading)
    }

}

@Composable
fun SingleChatToShare(
    chat: ChatOrGroup,
    currentUsername: String,
    addToSelection: (Boolean, ChatOrGroup) -> Unit,
    isCardEnabled: Boolean,
    isCardSelected: Boolean,
) {
    var expanded by remember {
        mutableStateOf(false)
    }
//    val lastMessageContent = chat.lastMessage.content.substring(20)
    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .combinedClickable(
//                onClick = {
//                    navigateToCurrentChat("${chat.chatId},${currentUsername}")
//                },
//                onLongClick = {
//                    setSelectionChats()
//                }
//            )
        modifier = Modifier
            .toggleable(value = isCardSelected,
                onValueChange = {
                    addToSelection(it, chat)
                })
//            .border(
//                width = 1.dp,
//                color = MaterialTheme.colorScheme.onSurface,
//                shape = RoundedCornerShape(0.dp)
//            )
//            .height(125.dp)
//            .padding(1.dp)
//            .padding(start = 8.dp, end = 8.dp)

//        onClick = {
////            navigateToCurrentChat("${chat.chatId},${currentUsername}")
//
//        },
        ,
        shape = RoundedCornerShape(0.dp),
//        enabled = isCardEnabled,
        elevation = CardDefaults.elevatedCardElevation(0.dp),
        colors = if (isCardSelected) {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        } else {
            CardDefaults.cardColors(
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        }
//                border = BorderStroke(5.dp,MaterialTheme.colorScheme.primary)

    )
    {
//        Switch(checked = selectedStatus, onCheckedChange = {
//            addToSelection(it, chat)
//        })
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Image(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
                modifier = Modifier
                    .size(50.dp)
            )
//            Spacer(modifier = Modifier.weight(1f))
            Column(
                modifier = Modifier
                    .padding(10.dp)
            ) {
                Text(
                    text = chat.chatName,
                    fontSize = 22.sp,
                    lineHeight = 22.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(15.dp))
//                Row(
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//
//                    if (chat.lastMessage.sender == "") {
//                        Icon(
//                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
//                            contentDescription =
//                            stringResource(R.string.default_here)
//                        )
//                    } else if (chat.lastMessage.sender == currentUsername) {
//                        Icon(
//                            imageVector = Icons.Default.ArrowUpward,
//                            contentDescription = stringResource(
//                                R.string.send_message
//                            )
//                        )
//                    } else if (chat.lastMessage.sender != currentUsername) {
//                        Icon(
//                            imageVector = Icons.Default.ArrowDownward,
//                            contentDescription = stringResource(
//                                R.string.receive_message
//                            )
//                        )
//                    }
//
//
//                    Text(
//                        text = if (chat.lastMessage.timestamp == Timestamp(0, 0)) {
//                            "Not yet contacted"
//                        } else {
//                            chat.lastMessage.content
//                        },
//                        fontSize = 18.sp,
//                        lineHeight = 18.sp,
//                        maxLines = 1,
//                        overflow = TextOverflow.Ellipsis
//                    )
//                }
            }
        }

    }
}

@Composable
fun BottomMessageShare(
    appUistate: ShareHandlerUiState,
    sendMessage: () -> Unit,
    updateMessage: (String) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    ElevatedCard(
        Modifier
//            .animateContentSize(
//
//                animationSpec = spring(
//                    dampingRatio = Spring.DampingRatioNoBouncy,
//                    stiffness = Spring.StiffnessLow,
//                )

//            )
            .navigationBarsPadding()
            .fillMaxWidth()
            .padding(8.dp)
            .background(MaterialTheme.colorScheme.background),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,

            ),
        shape = RoundedCornerShape(30.dp),
        elevation = CardDefaults.cardElevation(35.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
//                .padding(vertical = 16.dp, horizontal = 16.dp)
        )
        {
//            Text("Chat is secure with aes key : ${appUistate.currChat.secureAESKey}")
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
//                .height(height)
                verticalAlignment = Alignment.Bottom,

                ) {
                val text = appUistate.shareData


//                val annotatedText = buildAnnotatedString {
//                    val codeRegex = Regex("<.(.*?).>")
//                    var currentIndex= 0
//                    codeRegex.findAll(text).forEach { match ->
//                        append(text.substring(currentIndex, match.range.first))
//                        withStyle(style = SpanStyle(color = Color.Blue, fontWeight = FontWeight.Bold)) {
//                            append(match.value)
//                            Log.d("messages",match.value)
//                        }
//                        currentIndex = match.range.last + 1
//                    }
//                    append(text.substring(currentIndex, text.length))
//                }

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

//                    .padding(end = 80.dp),
                    value = appUistate.shareData,
//                    value = appUistate.messageToSend,
//                    onValueChange = { updateMessage(it) },
//                    value = "",
                    onValueChange = { updateMessage(it) },
                    textStyle = TextStyle.Default.copy(
                        fontSize = 20.sp,

                        ),
                    maxLines = 4,
                    placeholder = { Text(text = stringResource(R.string.send_text)) },
//                    placeholder = {"message..."},
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0, 0, 0, alpha = 0),
                        unfocusedBorderColor = Color(0, 0, 0, alpha = 0),
                        disabledBorderColor = Color(0, 0, 0, alpha = 0),
//                        focusedTextColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),

                    //                trailingIcon =
//                {
//
//                }

                )
//
                AnimatedVisibility(
                    visible = appUistate.shareData.isNotEmpty() && !appUistate.isLoading && !appUistate.isError && appUistate.selectedChats.isNotEmpty(),
                    enter = slideInHorizontally(initialOffsetX = {it}),
                    exit = slideOutHorizontally(targetOffsetX = {it})
                ) {
                    IconButton(
                        onClick = {

                            sendMessage()

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
                            imageVector = Icons.AutoMirrored.Filled.Send,
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

@Composable
@Preview
fun HandleSharedContentPreview() {
    ChitChatTheme {
        HandleSharedContentScreen(
            "Hello", null, uiState = ShareHandlerUiState(
//                isError = true,
//                isLoading = true,
                shareData = "http://somelink.com",
                toShareChats = listOf(
                    ChatOrGroup(chatName = "Testing 1"),
                    ChatOrGroup(chatName = "Testing 1"),
                    ChatOrGroup(chatName = "Testing 1"),
                    ChatOrGroup(chatName = "Testing 1"),
                    ChatOrGroup(chatName = "Testing 1"),
                )
            ),
            addToSelection = { _, _ -> },
            updateShareDate = {},
            sendMessage = {}
        )
    }
}
