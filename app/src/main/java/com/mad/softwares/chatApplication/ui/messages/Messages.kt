package com.mad.softwares.chatApplication.ui.messages

import StyledTextVisualTransformation
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Attachment
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Deselect
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.Timestamp
import com.mad.softwares.chatApplication.R
import com.mad.softwares.chatApplication.data.ChatOrGroup
import com.mad.softwares.chatApplication.data.ContentType
import com.mad.softwares.chatApplication.data.MessageReceived
import com.mad.softwares.chatApplication.data.messageStatus
import com.mad.softwares.chatApplication.data.models.AiResponse
import com.mad.softwares.chatApplication.data.models.messages
import com.mad.softwares.chatApplication.ui.ApptopBar
import com.mad.softwares.chatApplication.ui.GodViewModelProvider
import com.mad.softwares.chatApplication.ui.destinationData
import com.mad.softwares.chatApplication.ui.theme.ChitChatTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.util.Date

object messagesdestinationData : destinationData {
    override val route = "Messages"
    val chatIDAndUsername = "chatID,username"
    override val title = R.string.chats
    override val canBack = true
    val routeWithArgs = "$route/{$chatIDAndUsername}"
    val nestedGraphMessages = "NestedGRaphMessages"
}

enum class messagePosition() {
    Top,
    Middle,
    Bottom,
    Alone
}

@Composable
fun CheckInternetAndRun(onConnected: () -> Unit) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val isConnected = withTimeoutOrNull(2000L) {
            checkInternet(context)
        } ?: false

        if (isConnected) {
            onConnected()
        } else {
            Toast.makeText(context, "No Internet Connection", Toast.LENGTH_SHORT).show()
        }
    }
}

suspend fun checkInternet(context: Context): Boolean = withContext(Dispatchers.IO) {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return@withContext false
    val capabilities =
        connectivityManager.getNetworkCapabilities(network) ?: return@withContext false
    return@withContext capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}



fun copyToClipboardTextConversion(Messages: List<MessageReceived>):String{
    val clipboardText:String = Messages.joinToString(separator = "\n" ){"${it.senderId}: ${it.content}"}
    return clipboardText
}

@Composable
fun Messages(
    viewModel: MessagesViewModel = viewModel(factory = GodViewModelProvider.Factory),
    navigateUp: () -> Unit,
    navigateToMdSending: () -> Unit,
    navigateToMdPreview: ()->Unit,
) {
    val uiState = viewModel.messagesUiState.collectAsState().value
//    val aiTagsMessage = viewModel.aiMessage.collectAsState().value
//     val currentScreen = uiState.messageScreen
//    when (uiState.messageScreen) {
//        MessageScreen.Loading -> {
//            MessageBodyLoading()
//        }
//
//        MessageScreen.Error -> {
//            ShowMessageError {
////                viewModel.getMessages(isForced = true)
//            }
//        }
//
//        MessageScreen.Success -> {
//
//            MessagesBodySuccess(
//                uiState = uiState,
//                updateMessage = { viewModel.messageEdit(it) },
//                getMessagesAgain = {
////                    viewModel.getMessages(isForced = true)
//                },
//                sendMessage = { viewModel.sendTextMessage() },
//                toggleMessageSelection = viewModel::toggleMessageSelection,
//
//                navigateUp = navigateUp
//            )
//        }
//    }

    MessagesBodySuccess(
        uiState = uiState,
        updateMessage = { viewModel.messageEdit(it) },
        getMessagesAgain = {
//                    viewModel.getMessages(isForced = true)
        },
        sendMessage = { viewModel.sendTextMessage() },
        toggleMessageSelection = viewModel::toggleMessageSelection,
        selectionBoth = viewModel::startSelectionMode,
        navigateUp = navigateUp,
        deselectAll = viewModel::deSelectAll,
        deleteMessages = viewModel::deleteMessages,
        navigateToMdSending = navigateToMdSending,
        setAndNavigateMdMessage = {
            viewModel.setSelectedMessage(it)
            navigateToMdPreview()
        },
//        sendExpensiveAiMessage = viewModel::sendAiChatMessage,
//        aiMessage = viewModel.aiMessage.collectAsState().value
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MessagesBodySuccess(
    uiState: MessagesUiState,
    updateMessage: (String) -> Unit,
    deselectAll: () -> Unit,
    getMessagesAgain: () -> Unit,
    sendMessage: () -> Unit,
    navigateUp: () -> Unit,
    toggleMessageSelection: (MessageReceived, Boolean, Boolean) -> Unit = { _, _, _ -> },
    selectionBoth: () -> Unit,
    deleteMessages: () -> Unit,
    navigateToMdSending: () -> Unit,
    setAndNavigateMdMessage:(currentMessage:MessageReceived)-> Unit,
//    sendExpensiveAiMessage:()->Unit,
//    aiMessage : AiResponseUiState,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(
    )
    val codeSheetState = rememberModalBottomSheetState()
    var codeSheetVisible by remember {
        mutableStateOf(false)
    }
    var sheetVisible by remember {
        mutableStateOf(false)
    }
    val snackbarHostState = remember { SnackbarHostState() }
    var expandDDMenu by remember {
        mutableStateOf(false)
    }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var currentDate by remember {
        mutableStateOf("")
    }
    val haptic = LocalHapticFeedback.current

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    Scaffold(
        topBar = {
            ApptopBar(
                destinationData = messagesdestinationData,
                navigateUp = navigateUp,
                goBack = {
                    if (uiState.messageScreen == MessageScreen.SelectionMode){
                        deselectAll()
                    }
                    else{
                        navigateUp()
                    }
                },
                canGoBack = uiState.messageScreen == MessageScreen.SelectionMode,
                title = {
                    AnimatedContent(
//                        modifier = Modifier.fillMaxWidth(),
                        targetState = uiState.messageScreen,
                        label = "Top title Animation",
                        transitionSpec = {

                           (
                                slideInVertically(
                                    initialOffsetY = { it / 2 }
                                ) + fadeIn() togetherWith
                                        slideOutVertically() + fadeOut()
                            ).using(
                                SizeTransform(clip = false)
                            )
                        }
                    ) { NameTargetState ->
                        when (NameTargetState) {
                            MessageScreen.Success -> Text(
                                text = uiState.currChat.chatName,
                                color = MaterialTheme.colorScheme.onPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            MessageScreen.Loading -> Text(
                                "Loading...",
                                color = MaterialTheme.colorScheme.onPrimary
                            )

                            MessageScreen.Error -> Text(
                                "Error",
                                color = MaterialTheme.colorScheme.onPrimary
                            )

                            MessageScreen.SelectionMode -> {
                                AnimatedContent(
                                    targetState = (uiState.selectedSentMessages + uiState.selectedReceivedMessages).size,
                                    transitionSpec = {
                                        // Compare the incoming number with the previous number.
                                        if (targetState > initialState) {
                                            // If the target number is larger, it slides up and fades in
                                            // while the initial (smaller) number slides up and fades out.
                                            slideInVertically { height -> height } + fadeIn() togetherWith
                                                    slideOutVertically { height -> -height } + fadeOut()
                                        } else {
                                            // If the target number is smaller, it slides down and fades in
                                            // while the initial number slides down and fades out.
                                            slideInVertically { height -> -height } + fadeIn() togetherWith
                                                    slideOutVertically { height -> height } + fadeOut()
                                        }.using(
                                            // Disable clipping since the faded slide-in/out should
                                            // be displayed out of bounds.
                                            SizeTransform(clip = false)
                                        )
                                    }, label = "animated content"
                                ) {targetNumberState->
                                    Text(
                                        text = "$targetNumberState",
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                },
                action = {
                    if (uiState.selectedReceivedMessages.isEmpty() && uiState.selectedSentMessages.isEmpty()) {
                        IconButton(onClick = {
                            expandDDMenu = true
                        }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "refresh"
                            )
                        }
                        DropdownMenu(
                            expanded = expandDDMenu,
                            onDismissRequest = { expandDDMenu = !expandDDMenu }) {
                            uiState.currChat.members.forEach { mem ->
                                DropdownMenuItem(
                                    text = { Text(text = mem) },
                                    onClick = { /*TODO*/ })
                            }

                        }
                    } else {
                        if (uiState.selectedReceivedMessages.isEmpty()) {
                            IconButton(onClick = {
                                coroutineScope.launch {
                                    val isConnected = withTimeoutOrNull(2000L) {
                                        checkInternet(context)
                                    } ?: false

                                    if (isConnected) {

                                        deleteMessages()
                                        deselectAll()
                                    } else {
                                        snackbarHostState.currentSnackbarData?.dismiss()
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = context.getString(R.string.please_connect_to_internet_and_try_again),
                                                withDismissAction = true
                                            )
                                        }
                                    }
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "delete Message"
                                )
                            }
                        }
                        IconButton(onClick = {
                            val clipboardText = copyToClipboardTextConversion(uiState.selectedReceivedMessages + uiState.selectedSentMessages)

                            clipboardManager.setText(AnnotatedString(clipboardText))
                            deselectAll()
                        }){
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy message"
                            )
                        }
                        IconButton(onClick = {
                            deselectAll()
                        }) {
                            Icon(
                                imageVector = Icons.Default.Deselect,
                                contentDescription = "deselect All"
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            BottomMessageSend(

                appUistate = uiState,
                sendMessage = {
                    sendMessage()
                    coroutineScope.launch {
                        listState.animateScrollToItem(0)
                    }
                },
//                sendExpensiveAiMessage = sendExpensiveAiMessage,
                updateMessage = updateMessage,
                sendAttatchment = {
                    sheetVisible = true
                },
                isLoading = uiState.messageScreen == MessageScreen.Loading,
//                aiMessage = aiMessage
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        },

        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
            .navigationBarsPadding()
    ) { padding ->


        if (uiState.messages.isNotEmpty() && uiState.messageScreen == MessageScreen.Success || uiState.messageScreen == MessageScreen.SelectionMode) {
            val groupedMessages = uiState.messages.sortedBy { message ->
                message.timeStamp
            }.reversed().groupBy { message ->
                message.timeStamp.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
            }
            //                .entries.toList()
            //                .foldIndexed(mutableListOf<Pair<LocalDate?, List<MessageReceived>>>()) { index, acc, entry ->
            //                    val date = if (index == 0) null else entry.key // Assign null to the first group
            //                    acc.apply { add(date to entry.value) }
            //                }
            //            LaunchedEffect(listState) {
            //                listState.scrollToItem(index =listState )
            //            }

            LazyColumn(
                modifier = modifier
                    .padding(padding)
                    .background(Color.Transparent)
                    .fillMaxSize(),
                //                    .background(MaterialTheme.colorScheme.background),
                reverseLayout = true,
                state = listState,
                verticalArrangement = Arrangement.Top,
                //                contentPadding = PaddingValues(top = 100.dp)
            ) {
//                item(){
//                    AnimatedContent(targetState = aiMessage.aiState,
//                        transitionSpec = ({ fadeIn()+slideInVertically { -it } togetherWith fadeOut() })
//                    ){targetState->
//                        when(targetState) {
//                            AiState.Loading -> Text(
//                                text = "Loading Respone From Ai"
//                            )
//                            AiState.Error -> Text(
//                                text = "Error Communicating to Ai"
//                            )
//                            AiState.Success -> {
//                                Box(){}
//                            }
//                        }
//
//                    }
//                }


                groupedMessages.forEach { (date, messages) ->
                    //                    stickyHeader {

                    //
                    //                    }
                    //                    stickyHeader(
                    //
                    //                    ) {
                    //                        Text(
                    //                            modifier = Modifier.fillMaxWidth(),
                    //                            text = date.toString(),
                    //                            textAlign = TextAlign.Center
                    //                        )
                    //
                    //                    }
                    items(
                        //                        items = uiState.messages.reversed(),
                        items = messages,
                        //                        key = { message ->
                        //                            message.timeStamp
                        //                        }
//                            key = {
//                                message -> message.messageId
//                            }
                    ) { message ->
                        val index = messages.indexOf(message)
                        val prevMessage = messages.getOrNull(index + 1)
                        val nextMessage = messages.getOrNull(index - 1)

                        val prevWithin5Min = prevMessage?.let {
                            (message.timeStamp.toDate().time - it.timeStamp.toDate().time) / (1000 * 60) <= 2
                                    && it.senderId == message.senderId
                        } ?: false

                        val nextWithin5Min = nextMessage?.let {
                            (it.timeStamp.toDate().time - message.timeStamp.toDate().time) / (1000 * 60) <= 2
                                    && it.senderId == message.senderId
                        } ?: false

                        val msgPosition = when {
                            prevWithin5Min && nextWithin5Min -> messagePosition.Middle
                            prevWithin5Min -> messagePosition.Bottom
                            nextWithin5Min -> messagePosition.Top
                            else -> messagePosition.Alone
                        }

                        if (message.senderId != uiState.currentUser) {
                            if (uiState.currChat.isGroup == false) {
                                ReceiverChat(
                                    message = message,
                                    msgPosition = msgPosition,
                                    modifier = if (uiState.selectedReceivedMessages.isEmpty() && uiState.messageScreen != MessageScreen.SelectionMode) {
                                        Modifier
                                            .combinedClickable(
                                                interactionSource = null,
                                                indication = null,
                                                onClick = { },
                                                onLongClick = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    Log.d("Messages", "Long press detected")
                                                    selectionBoth()
                                                    toggleMessageSelection(
                                                        message,
                                                        true,
                                                        true
                                                    )
                                                }
                                            )
                                    } else {
                                        Modifier
                                            .toggleable(
                                                value = uiState.selectedReceivedMessages.contains(
                                                    message
                                                ),
                                                onValueChange = {
                                                    toggleMessageSelection(
                                                        message,
                                                        it,
                                                        true
                                                    )
                                                }
                                            )
                                    },
                                    isCardSelected = uiState.selectedReceivedMessages.contains(
                                        message
                                    ),
                                    setAndNavigateMdMessage = setAndNavigateMdMessage
                                )
                            } else {
                                ReceiverGroupChat(
                                    message = message,
                                    msgPosition = msgPosition,
                                    modifier = if (uiState.selectedReceivedMessages.isEmpty() && uiState.messageScreen != MessageScreen.SelectionMode) {
                                        Modifier
                                            .combinedClickable(
                                                interactionSource = null,
                                                indication = null,
                                                onClick = { },
                                                onLongClick = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    Log.d("Messages", "Long press detected")
                                                    selectionBoth()
                                                    toggleMessageSelection(
                                                        message,
                                                        true,
                                                        true
                                                    )
                                                }
                                            )
                                    } else {
                                        Modifier
                                            .toggleable(
                                                value = uiState.selectedReceivedMessages.contains(
                                                    message
                                                ),
                                                onValueChange = {
                                                    toggleMessageSelection(
                                                        message,
                                                        it,
                                                        true
                                                    )
                                                }
                                            )
                                    },
                                    isCardSelected = uiState.selectedReceivedMessages.contains(
                                        message
                                    ),
                                    setAndNavigateMdMessage = setAndNavigateMdMessage
                                )
                            }
                        } else {
                            SenderChat(
                                modifier = if (uiState.selectedSentMessages.isEmpty() && uiState.messageScreen != MessageScreen.SelectionMode) {
                                    Modifier
                                        .combinedClickable(
                                            interactionSource = null,
                                            indication = null,
                                            onClick = { },
                                            onLongClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                Log.d("Messages", "Long press detected")
                                                selectionBoth()
                                                toggleMessageSelection(
                                                    message,
                                                    true,
                                                    false
                                                )
                                            }
                                        )
                                } else {
                                    Modifier
                                        .toggleable(
                                            value = uiState.selectedSentMessages.contains(message),
                                            onValueChange = {
                                                toggleMessageSelection(
                                                    message,
                                                    it,
                                                    false
                                                )
                                            }
                                        )
                                },
                                message = message,
                                msgPosition = msgPosition,
                                isCardSelected = uiState.selectedSentMessages.contains(message),
                                setAndNavigateMdMessage = setAndNavigateMdMessage
                            )

                        }
                    }
                    item() {
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = date.toString(),
                            textAlign = TextAlign.Center
                        )
                    }

                }
                item() {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) { SecureMessageTag() }
                }

            }

            if (sheetVisible) {
                ModalBottomSheet(
                    onDismissRequest = {
                        sheetVisible = false

                    },
                    sheetState = sheetState,
//                    modifier = Modifier.height(400.dp)
                ) {

                    AttatchmentContent(
                        hideSheet = {
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                if (!sheetState.isVisible) {
                                    sheetVisible = false
                                }
                            }
                        },
                        sendCode = {
                            snackbarHostState.currentSnackbarData?.dismiss()
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = context.getString(R.string.this_function_is_not_implemented_yet),
                                    withDismissAction = true
                                )
                            }
                        },
                        navigateToMdSending = navigateToMdSending
                    )

                }
            }
        } else if (uiState.messageScreen == MessageScreen.Loading) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background)
                    .fillMaxSize(),
//                    .background(MaterialTheme.colorScheme.background),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth(),
                    text = stringResource(R.string.loading_messages),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth(),
                    text = stringResource(R.string.start_chating_now),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
            }
        }

    }
}

@Composable
fun AttachmentButton(
    onClick: () -> Unit,
    image: ImageVector,
    text: String
) {
    Column(
        modifier = Modifier
            .padding(10.dp)
//                .fillMaxWidth()
        ,
        verticalArrangement = Arrangement.Center
    ) {
        IconButton(
            onClick = onClick,
//                shape = CircleShape,
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier
//                        .fillMaxWidth()
//                    .fillMaxWidth()
                .padding(bottom = 10.dp)
                .size(60.dp)

                .align(Alignment.CenterHorizontally)
//                    .clip(CircleShape)
        ) {
            Icon(
                imageVector = image,
                contentDescription = null,
//                    modifier = Modifier
//                        .size(60.dp)
            )
        }
        Text(text = text)
    }
}


@Composable
fun AttatchmentContent(
    hideSheet: () -> Unit,
    sendCode: () -> Unit,
    navigateToMdSending: ()->Unit,
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 15.dp, bottom = 100.dp),
        horizontalArrangement = Arrangement.Absolute.Center
    ) {

        AttachmentButton(onClick = {
            hideSheet()
            navigateToMdSending()
        }, image = ImageVector.vectorResource(R.drawable.outline_markdown_24), text = "Send Markdown text")
        AttachmentButton(onClick = {
            hideSheet()
            sendCode()
        }, image = Icons.Default.Image, text = "Send Image")

//        AttachmentButton(onClick = {
//            hideSheet()
//            sendCode()
//        }, image = Icons.Default.UploadFile, text = "Send Document")
    }
}

@Composable
fun MessagesBodyError(
    uiState: MessagesUiState
) {
    Text(text = "Error : ${uiState.errorMessage}")
}

@Composable
fun MessageBodyLoading() {
    Text(text = "Loading")
}

data class StyleRegex(
    val Regex: Regex = Regex(""),
    val style: SpanStyle = SpanStyle(),
    val symbol: String = ""
)

val styleMap: List<StyleRegex> = listOf(
    StyleRegex(
        Regex = Regex("~(.*?)~", RegexOption.DOT_MATCHES_ALL),
        style = SpanStyle(
            textDecoration = TextDecoration.LineThrough
        ),
        symbol = "~"
    ),
    StyleRegex(
        Regex = Regex("_(.*?)_", RegexOption.DOT_MATCHES_ALL),
        style = SpanStyle(
            fontStyle = FontStyle.Italic,
//                fontWeight = FontWeight.Bold
        ),
        symbol = "_"
    ),
    StyleRegex(
        Regex = Regex("\\*(.*?)\\*", RegexOption.DOT_MATCHES_ALL),
        style = SpanStyle(
            fontWeight = FontWeight.Bold
        ),
        symbol = "*"
    ),
    StyleRegex(
        Regex = Regex("!(.*?)!", RegexOption.DOT_MATCHES_ALL),
        style = SpanStyle(
            textDecoration = TextDecoration.Underline
        ),
        symbol = "!"

    )
)

data class StyleMatch(
    val style: SpanStyle,
    val range: IntRange,
    val content: String,
)

fun annotateMessage(
    msg: String
): AnnotatedString {
    val annotedMessage = buildAnnotatedString {

        var currentIndex = 0
        val length = msg.length

        val matches = mutableListOf<StyleMatch>()

        for((regex,style,_)in styleMap){
            regex.findAll(msg,).forEach{ matchResult ->
                matches.add(
                    StyleMatch(style,
                        matchResult.range,
                        matchResult.groupValues[1]))
            }
        }

        matches.sortBy { it.range.first }

        for (match in matches){
            if(match.range.first < currentIndex){
                continue
            }
            if(currentIndex< match.range.first){
                append(msg.substring(currentIndex,match.range.first))
            }

            withStyle(match.style){
                append(match.content)
            }
            currentIndex += match.range.last +1
        }

        if(currentIndex< length){
            append(msg.substring(currentIndex,length))
        }

    }



    return annotedMessage
}

//@Composable
//fun InlineStyles(
//    msg: String
//): Map<String, InlineTextContent> {
//
//    val inlineContentMap = codeRegex.findAll(msg).associate { match ->
//        val cardText = match.value.removeSurrounding("'''")
//        val density = LocalDensity.current
//        val textWidth = with(density) {
//            // Estimate width based on the character count of the card content
//            (cardText.length * 15).toSp()
//        }
//        val textHeight = with(density) {
//            // Fixed height for the card
//            120.sp
//        }
//        "card_${match.value}" to
//                InlineTextContent(
//                    placeholder = Placeholder(
//                        height = 6.em,
//                        width = textWidth,
//                        placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter
//                    ),
//                    children = {
////                        Card(
////                            modifier = Modifier
////                                .fillMaxWidth()
////                                .padding(10.dp),
////                            colors = CardDefaults.cardColors(
////                                contentColor = MaterialTheme.colorScheme.error
////                            ),
////                            shape = RoundedCornerShape(15.dp)
////                        ) {
////                            Text(
////                                modifier = Modifier
////                                    .padding(4.dp),
////                                text = match.value.removeSurrounding("'''"),
////                                fontSize = 22.sp
////                            )
////                        }
//
//                        SubcomposeLayout { constraints ->
//                            val cardContent = subcompose("card_${match.value}") {
//                                Card(
//                                    modifier = Modifier.wrapContentWidth(),
//                                    colors = CardDefaults.cardColors(
//                                        contentColor = MaterialTheme.colorScheme.error
//                                    )
//                                ) {
//                                    Text(
//                                        text = cardText,
//                                        modifier = Modifier.padding(4.dp),
//                                        fontSize = 20.sp
//                                    )
//                                }
//                            }
//
//                            val placeable = cardContent[0].measure(constraints)
//                            layout(placeable.width, placeable.height) {
//                                placeable.placeRelative(0, 45)
//                            }
//                        }
//                    }
//                )
//
//    }
//
//    return inlineContentMap
//}

fun checkForCode(message: String): Boolean {
    val codeRegex = Regex("```(.*?)```", RegexOption.DOT_MATCHES_ALL)
    return codeRegex.containsMatchIn(message)
}

fun checkForLink(message: String): Boolean {
    val codeRegex = Regex("<([^\\s])>", RegexOption.DOT_MATCHES_ALL)
    return codeRegex.containsMatchIn(message)
}


/*fun parseLinkInMessage(message: String): Map<String, Boolean> {
    val result = mutableMapOf<String, Boolean>()
    val codeRegex = Regex("<([^\\s])>", RegexOption.DOT_MATCHES_ALL)
    var lastIndex = 0

    // Function to add text to map
    fun addTextToMap(text: String, isCode: Boolean) {
        if (text.isNotBlank()) {
            result[text] = isCode
        }
    }

    // Iterate over matches
    codeRegex.findAll(message).forEach { match ->
        val (codeBlock) = match.destructured
        val start = match.range.first
        val end = match.range.last + 1

        // Add text before the code block
        addTextToMap(message.substring(lastIndex, start), isCode = false)
        // Add the code block
        addTextToMap(codeBlock.trimIndent(), isCode = true)

        // Update last index
        lastIndex = end
    }

    // Add any remaining text after the last code block
    addTextToMap(message.substring(lastIndex), isCode = false)

    return result
}*/

//@Composable
//fun GetLinkBlock(msg: String){
//    val parsedMessage = parseLinkInMessage(msg)
//    Column(
//        modifier = Modifier
//            .padding(10.dp)
//    ) {
//        parsedMessage.forEach {
//            if (it.value) {
//                Card(
//                    modifier = Modifier,
//                    shape = RoundedCornerShape(10.dp)
//                ) {
//                    SelectionContainer {
//                        Text(
//                            text = it.key,
//                            modifier = Modifier
//                                .padding(6.dp),
//                            fontSize = 20.sp,
//                            fontFamily = FontFamily.Monospace
//
////                    inlineContent = InlineStyles(msg = msg),
////                    onTextLayout = {}
//                        )
//                    }
//                }
//            } else {
//                Text(
//                    text =  annotateMessage(it.key),
//                    modifier = Modifier,
//                    fontSize = 20.sp,
//                )
//            }
//        }
//    }
//}

fun groupSegments(msg: List<MessageSegement>): List<DisplayBlock> {
    val blocks = mutableListOf<DisplayBlock>()
    var currentTextGroup = mutableListOf<MessageSegement>()

    for (segment in msg) {
        if (segment.type == textTypeParse.code) {
            if (currentTextGroup.isNotEmpty()) {
                blocks.add(DisplayBlock(isCodeBlock = false, segments = currentTextGroup.toList()))
                currentTextGroup.clear()
            }
            blocks.add(DisplayBlock(isCodeBlock = true, segments = listOf(segment)))
        } else {
            currentTextGroup.add(segment)
        }
    }

    if (currentTextGroup.isNotEmpty()) {
        blocks.add(DisplayBlock(isCodeBlock = false, segments = currentTextGroup.toList()))
    }

    return blocks
}
data class DisplayBlock(
    val isCodeBlock: Boolean,
    val segments: List<MessageSegement>
)

@Composable
fun GetCodeMessage(msg: List<MessageSegement>) {
//    val parsedMessage = parseMessage(msg)
    val handler = LocalUriHandler.current
    Column(
        modifier = Modifier
            .padding(10.dp)
    ) {
        val context = LocalContext.current
        msg.forEach {
            when (it.type) {
                textTypeParse.code -> {
                    Card(
                        modifier = Modifier,
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        SelectionContainer {
                            Text(
                                text = it.content,
                                modifier = Modifier
                                    .padding(6.dp),
                                fontSize = 20.sp,
                                fontFamily = FontFamily.Monospace

        //                    inlineContent = InlineStyles(msg = msg),
        //                    onTextLayout = {}
                            )
                        }
                    }
                }
                textTypeParse.link -> {
        //                Card(
        //                    modifier = Modifier,
        //                    shape = RoundedCornerShape(10.dp),
        //                    colors = CardDefaults.cardColors(
        //                        containerColor = MaterialTheme.colorScheme.error
        //                    )
        //                ) {
                    SelectionContainer {
                        Text(
                            text = it.content,
                            modifier = Modifier
                                .padding(6.dp)
                                .clickable(enabled = true, onClick = {
        //                                    val intend = Intent(Intent.ACTION_VIEW, Uri.parse(it.key))
        //                                    context.startActivity(intend)
                                    handler.openUri(it.content)
                                }),
                            fontSize = 18.sp,
                            fontFamily = FontFamily.Monospace,
                            textDecoration = TextDecoration.Underline
        //                    inlineContent = InlineStyles(msg = msg),
        //                    onTextLayout = {}
                        )
                    }
        //                }
                }
                textTypeParse.text -> {
                    val annotatedText = remember (it.content){ annotateMessage(it.content) }
                    Text(
                        text = annotatedText,
                        modifier = Modifier,
                        fontSize = 20.sp,
                    )
                }
            }
        }
    }
}

val aloneModifier = Modifier
    .padding(top = 3.dp)
    .padding(horizontal = 5.dp)

val topModifier = Modifier
    .padding(top = 3.dp, bottom = 0.5.dp)
    .padding(horizontal = 5.dp)

val bottomModifier = Modifier
    .padding(vertical = 0.5.dp)
    .padding(horizontal = 5.dp)

val middleModifier = Modifier
    .padding(vertical = 1.dp)
    .padding(horizontal = 5.dp)

@Composable
fun MdMessageChat(msg: String,setMsg:()->Unit){
    Card(
        modifier = Modifier
            .padding(8.dp),
        shape = RoundedCornerShape(12.dp),
        onClick = setMsg,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .padding(4.dp)
        ) {
            Text("Markdown File")
            Text(msg,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis)
//            Text(msg)
        }
    }
}

@Composable
fun SenderChat(
    modifier: Modifier = Modifier,
    message: MessageReceived,
    msgPosition: messagePosition = messagePosition.Alone,
    isCardSelected: Boolean,
    setAndNavigateMdMessage : (currentMessage:MessageReceived)->Unit,
) {
    val date = message.timeStamp.toDate()
    //    val sdf  = SimpleDateFormat("HH:mm")
    val currentDate = Timestamp.now().toDate()
    val difference = (currentDate.time - date.time) / (1000 * 60 * 60)
    val msg = message.content
    val parsedMessage = message.parsedContent
    var showTime by remember {
        mutableStateOf(false)
    }
    val aloneShape = RoundedCornerShape(20.dp, 5.dp, 20.dp, 20.dp)
    val topShape = RoundedCornerShape(20.dp, 20.dp, 5.dp, 20.dp)
    val middleShape = RoundedCornerShape(20.dp, 5.dp, 5.dp, 20.dp)
    val bottomShape = RoundedCornerShape(20.dp, 5.dp, 20.dp, 20.dp)



    //    val inlineContentMap = codeRegex.findAll(msg).associate { match->
    //        "card_${match.value}" to InlineTextContent(
    //            placeholder = Placeholder(
    ////                width =with(LocalDensity.current) {
    ////                    (match.value.length * 8).toSp()// Estimate the width based on character count
    ////                },
    //                width = 400.sp,
    //                height = 100.sp,
    //                placeholderVerticalAlign = PlaceholderVerticalAlign.Center
    //            )
    //            ,
    //            children = {
    //                Card(modifier = Modifier,
    //                    colors = CardDefaults.cardColors(
    //                        containerColor = MaterialTheme.colorScheme.error
    //                    )
    //
    //                    ){ Text(text = match.value.removeSurrounding("'''")) }
    //            }
    //        )
    //    }


    val sdf = if (difference <= 24) {
        SimpleDateFormat("hh:mm a")
    } else {
        SimpleDateFormat("YYYY/MM/dd hh:mm a")
    }
    val fDate = sdf.format(date)
//    val parsedMessage = parseMessage(msg)
    val IntrSource = remember {
        MutableInteractionSource()
    }


    Row(
        modifier = modifier
            .padding(
                top = when (msgPosition) {
                    messagePosition.Alone -> 5.dp
                    messagePosition.Top -> 5.dp
                    messagePosition.Middle -> 0.dp
                    messagePosition.Bottom -> 0.dp
                },


                )
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth(1f)
                .background(
                    color = if (isCardSelected) {
                        MaterialTheme.colorScheme.outline
                    } else {
                        Color.White.copy(alpha = 0f)
                    }
                )
                .padding(
                    bottom = when (msgPosition) {
                        messagePosition.Alone -> 5.dp
                        messagePosition.Top -> 0.dp
                        messagePosition.Middle -> 0.dp
                        messagePosition.Bottom -> 5.dp
                    },
                ),
            horizontalArrangement = Arrangement.End
        ) {
            Column(
                modifier = Modifier
                //                .clickable(
                //                    enabled = true,
                //                    onClick = { showTime = !showTime },
                //                    interactionSource = IntrSource,
                //                    indication = null
                //                )
            ) {
                Row {
                    Card(
                        modifier = when (msgPosition) {
                            messagePosition.Alone -> aloneModifier
                            messagePosition.Top -> topModifier
                            messagePosition.Middle -> middleModifier
                            messagePosition.Bottom -> bottomModifier
                        }
                            .fillMaxWidth(0.8f)
                            //                .weight(5f)
                            .wrapContentWidth(Alignment.End)

                            .clickable(
                                enabled = true,
                                onClick = { showTime = !showTime },
                                interactionSource = IntrSource,
                                indication = null
                            ),
                        //                .height(60.dp),
                        shape = when (msgPosition) {
                            messagePosition.Alone -> aloneShape
                            messagePosition.Top -> topShape
                            messagePosition.Middle -> middleShape
                            messagePosition.Bottom -> bottomShape
                        },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row {

                            Column(
                                modifier = Modifier
                                    .width(IntrinsicSize.Max)
                            ) {
                                //                            if (checkForCode(msg)) {
                                //                                GetCodeMessage(msg = msg)
                                //                            } else {
                                //                                Text(
                                //                                    text = annotateMessage(message.content),
                                //                                    modifier = Modifier
                                //                                        .padding(10.dp),
                                //                                    fontSize = 20.sp,
                                //                                )
                                //                            }GetCodeMessage(msg = msg)

                                when (message.contentType) {
                                    ContentType.text -> GetCodeMessage(parsedMessage)
                                    ContentType.image -> GetCodeMessage(parsedMessage)
                                    ContentType.document -> GetCodeMessage(parsedMessage)
                                    ContentType.audio -> GetCodeMessage(parsedMessage)
                                    ContentType.video -> GetCodeMessage(parsedMessage)
                                    ContentType.deleted -> GetCodeMessage(parsedMessage)
                                    ContentType.default -> GetCodeMessage(parsedMessage)
                                    ContentType.Md -> MdMessageChat(msg) {
                                        setAndNavigateMdMessage(message)
                                    }
                                }
                            }
                            //                if (message.status == messageStatus.Send) {
                            //                            Icon(
                            //                                imageVector = Icons.Default.ArrowOutward,
                            //                                contentDescription = null
                            //                            )
                            //                        } else if (message.status == messageStatus.Sending) {
                            //                            Icon(
                            //                                imageVector = Icons.Default.CloudQueue,
                            //                                contentDescription = null
                            //                            )
                            //                        } else if (message.status == messageStatus.Error) {
                            //                            Icon(
                            //                                imageVector = Icons.Default.Error,
                            //                                contentDescription = null
                            //                            )
                            //                        }

                            //                Card(
                            //                    modifier = Modifier
                            //                        //                    .wrapContentWidth(unbounded = true)
                            //                        .fillMaxWidth()
                            ////                        .padding(0.dp),
                            //
                            //                ) {
                            //                    Row(
                            //                        modifier = Modifier
                            //                            .fillMaxWidth(),
                            //                        horizontalArrangement = Arrangement.End,
                            //                    ) {
                            //                        Text(
                            //                            modifier = Modifier
                            //                                //                            .fillMaxWidth()
                            //                                .padding(horizontal = 15.dp)
                            //                                .padding(vertical = 2.dp),
                            //                            text = fDate.toString(),
                            //                            //                    text = difference.toString(),
                            //                            //                text = message.timeStamp.toDate().toString(),
                            //                            textAlign = TextAlign.Start
                            //                        )
                            //                        if (message.status == messageStatus.Send) {
                            //                            Icon(
                            //                                imageVector = Icons.Default.ArrowOutward,
                            //                                contentDescription = null
                            //                            )
                            //                        } else if (message.status == messageStatus.Sending) {
                            //                            Icon(
                            //                                imageVector = Icons.Default.CloudQueue,
                            //                                contentDescription = null
                            //                            )
                            //                        } else if (message.status == messageStatus.Error) {
                            //                            Icon(
                            //                                imageVector = Icons.Default.Error,
                            //                                contentDescription = null
                            //                            )
                            //                        }
                            //                    }
                            //                }

                        }
                    }
                    //                if (message.status == messageStatus.Send) {
                    //                    Card(
                    //                        modifier = Modifier
                    //                            .padding(top = 10.dp),
                    //                        shape = RoundedCornerShape(50)
                    //                    ) {
                    //                        Icon(
                    //                            imageVector = Icons.Default.ArrowOutward,
                    //                            contentDescription = null,
                    //                            modifier = Modifier
                    //                                .background(color = MaterialTheme.colorScheme.primaryContainer)
                    //                                .padding(1.dp)
                    ////                                .clip(shape = RoundedCornerShape(50.dp))
                    //                        )
                    //                    }
                    //                } else if (message.status == messageStatus.Sending) {
                    AnimatedVisibility(
                        label = "message Status not send ${message.content}",
                        visible = message.status != messageStatus.Send,
                        enter = fadeIn(tween(0)) ,
//                        enter = expandHorizontally(),
                        exit = shrinkHorizontally(
                            animationSpec = tween(durationMillis = 1300, delayMillis = 800)
                        ) + slideOutHorizontally(
                            targetOffsetX = { it },
                            animationSpec = tween(durationMillis = 1000, delayMillis = 500)
                        )
                    ) {
                        AnimatedContent(
                            label = "Message Status - ${message.content}",
                            transitionSpec = {
                                if (targetState == messageStatus.Sending) {
                                    (fadeIn(animationSpec = tween(0))
                                            togetherWith
                                            slideOutHorizontally(
                                                animationSpec = tween(
                                                    100,
                                                    delayMillis = 200
                                                )
                                            ) + fadeOut()
                                            )
                                        .using(SizeTransform(clip = false))
                                } else {
                                    (expandHorizontally(
                                        animationSpec = tween(
                                            200,
                                            delayMillis = 200
                                        )
                                    ) + fadeIn()
                                            togetherWith
                                            slideOutHorizontally(
                                                targetOffsetX = { it },
                                                animationSpec = tween(
                                                    durationMillis = 200,
                                                    delayMillis = 200
                                                )
                                            )
                                            )
                                        .using(SizeTransform(clip = false))
                                }

                            },
                            targetState = message.status
                        ) { targetStatue ->
//                            if (targetStatue == messageStatus.Send) {
//
//                                Card(
//                                    modifier = Modifier
//                                        .padding(top = 10.dp),
//                                    shape = RoundedCornerShape(50)
//                                ) {
//                                    Icon(
//                                        imageVector = Icons.AutoMirrored.Default.ArrowForward,
//                                        contentDescription = null,
//                                        modifier = Modifier
//                                            .background(color = MaterialTheme.colorScheme.primaryContainer)
//                                            .padding(1.dp)
//                                        //                                .clip(shape = RoundedCornerShape(50.dp))
//                                    )
//                                }
//
//                            } else
//                                if (targetStatue == messageStatus.Sending) {
//
//                                    Card(
//                                        modifier = Modifier
//                                            .padding(top = 10.dp),
//                                        shape = RoundedCornerShape(50)
//                                    ) {
//                                        Icon(
//                                            imageVector = Icons.Default.CloudQueue,
//                                            contentDescription = null,
//                                            modifier = Modifier
//                                                .background(color = MaterialTheme.colorScheme.primaryContainer)
//                                                .padding(1.dp)
//                                            //                                .clip(shape = RoundedCornerShape(50.dp))
//                                        )
//                                    }
//
//                                } else if (targetStatue == messageStatus.Error) {
//                                    Card(
//                                        modifier = Modifier
//                                            .padding(top = 10.dp),
//                                        shape = RoundedCornerShape(50)
//                                    ) {
//                                        Icon(
//                                            imageVector = Icons.Default.Error,
//                                            contentDescription = null,
//                                            modifier = Modifier
//                                                .background(color = MaterialTheme.colorScheme.primaryContainer)
//                                                .padding(1.dp)
//                                            //                                .clip(shape = RoundedCornerShape(50.dp))
//                                        )
//                                    }
//                                }
                            when (targetStatue) {
                                messageStatus.Send -> {
                                    Card(
                                        modifier = Modifier
                                            .padding(top = 10.dp),
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Default.ArrowForward,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .background(color = MaterialTheme.colorScheme.primaryContainer)
                                                .padding(1.dp)
                                            //                                .clip(shape = RoundedCornerShape(50.dp))
                                        )
                                    }
                                }

                                messageStatus.Sending -> {

//
                                    Card(
                                        modifier = Modifier
                                            .padding(top = 10.dp),
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CloudQueue,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .background(color = MaterialTheme.colorScheme.primaryContainer)
                                                .padding(1.dp)
                                            //                                .clip(shape = RoundedCornerShape(50.dp))
                                        )
//
                                    }
                                }

                                messageStatus.Delivered -> TODO()
                                messageStatus.Read -> TODO()
                                messageStatus.Error ->
                                    Card(
                                        modifier = Modifier
                                            .padding(top = 10.dp),
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Error,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .background(color = MaterialTheme.colorScheme.primaryContainer)
                                                .padding(1.dp)
                                            //                                .clip(shape = RoundedCornerShape(50.dp))
                                        )
                                    }

                                messageStatus.Deleting ->
                                    Card(
                                    modifier = Modifier
                                        .padding(top = 10.dp),
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .background(color = MaterialTheme.colorScheme.primaryContainer)
                                            .padding(1.dp)
                                        //                                .clip(shape = RoundedCornerShape(50.dp))
                                    )
                                }
                            }
                        }
                    }


                }
                AnimatedVisibility(
                    modifier = Modifier
                        //                    .padding(horizontal = 5.dp)
                        //                    .padding(vertical = 2.dp)
                        .align(alignment = Alignment.End)
                    //                        .fillMaxWidth(0.7f)
                    ,
                    label = "Message Time",
                    visible = showTime,
                    enter = expandVertically(
                        //                    initialOffsetY = { -it/2 },

                        animationSpec = tween(500)
                    ) + fadeIn(animationSpec = tween(200)),
                    exit = shrinkVertically(
                        //                    targetOffsetY = { -it },
                        animationSpec = tween(500)
                    ) + fadeOut(animationSpec = tween(300))
                ) {
                    Row(

                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            modifier = Modifier
                                .padding(end = 10.dp),
                            text = fDate.toString(),
                            //                    text = difference.toString(),
                            //                text = message.timeStamp.toDate().toString(),
                            textAlign = TextAlign.End
                        )
                    }
                }

            }

        }
    }
}

@Composable
fun ReceiverChat(
    message: MessageReceived,
    msgPosition: messagePosition = messagePosition.Alone,
    modifier: Modifier = Modifier,
    isCardSelected: Boolean,
    setAndNavigateMdMessage:(setMessage: MessageReceived) ->Unit
) {
    val date = message.timeStamp.toDate()
//    val sdf  = SimpleDateFormat("HH:mm")
    val currentDate = Timestamp.now().toDate()
    val difference = (currentDate.time - date.time) / (1000 * 60 * 60)
    val sdf = if (difference <= 24) {
        SimpleDateFormat("hh:mm a")
    } else {
        SimpleDateFormat("YYYY/MM/dd hh:mm a")
    }
    val fDate = sdf.format(date)
    var showTime by remember {
        mutableStateOf(false)
    }
    val parsedMessage = message.parsedContent

    val aloneShape = RoundedCornerShape(5.dp, 20.dp, 20.dp, 20.dp)
    val topShape = RoundedCornerShape(20.dp, 20.dp, 20.dp, 5.dp)
    val middleShape = RoundedCornerShape(5.dp, 20.dp, 20.dp, 5.dp)
    val bottomShape = RoundedCornerShape(5.dp, 20.dp, 20.dp, 20.dp)

    Row(
        modifier = modifier
            .padding(
                top = when (msgPosition) {
                    messagePosition.Alone -> 5.dp
                    messagePosition.Top -> 5.dp
                    messagePosition.Middle -> 0.dp
                    messagePosition.Bottom -> 0.dp
                },


                )
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .fillMaxWidth()
                .background(
                    color = if (isCardSelected) {
                        MaterialTheme.colorScheme.outline
                    } else {
                        Color.White.copy(alpha = 0f)
                    }
                )
                .padding(
                    bottom = when (msgPosition) {
                        messagePosition.Alone -> 5.dp
                        messagePosition.Top -> 0.dp
                        messagePosition.Middle -> 0.dp
                        messagePosition.Bottom -> 5.dp
                    },
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.8f),
                //            .fillMaxWidth()
            ) {
                Column {
                    Card(
                        modifier = when (msgPosition) {
                            messagePosition.Alone -> aloneModifier
                            messagePosition.Top -> topModifier
                            messagePosition.Middle -> middleModifier
                            messagePosition.Bottom -> bottomModifier
                        }
                            .wrapContentWidth(Alignment.Start)
                            .clickable(
                                enabled = true,
                                onClick = { showTime = !showTime },
                                interactionSource = null,
                                indication = null
                            )
                        //                .fillMaxWidth(1f)
                        ,
                        //                .height(60.dp),
                        shape = when (msgPosition) {
                            messagePosition.Alone -> aloneShape
                            messagePosition.Top -> topShape
                            messagePosition.Middle -> middleShape
                            messagePosition.Bottom -> bottomShape
                        },
                        colors = CardDefaults.cardColors(
                            //                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .width(IntrinsicSize.Max)
                        ) {
                            //                    if (checkForCode(message.content)
                            //                    ) {
                            //                        GetCodeMessage(msg = message.content)
                            //                    } else {
                            //                        Text(
                            //                            text = annotateMessage(message.content),
                            //                            modifier = Modifier
                            //                                .padding(10.dp)
                            //                                .wrapContentWidth(),
                            //                            fontSize = 20.sp
                            //                        )
                            //                    }
//                            GetCodeMessage(msg = message.content)
                            when (message.contentType) {
                                ContentType.text -> GetCodeMessage(parsedMessage)
                                ContentType.image -> GetCodeMessage(parsedMessage)
                                ContentType.document -> GetCodeMessage(parsedMessage)
                                ContentType.audio -> GetCodeMessage(parsedMessage)
                                ContentType.video -> GetCodeMessage(parsedMessage)
                                ContentType.deleted -> GetCodeMessage(parsedMessage)
                                ContentType.default -> GetCodeMessage(parsedMessage)
                                ContentType.Md -> MdMessageChat(message.content) {
                                    setAndNavigateMdMessage(message)
                                }
                            }

                            //                Card(
                            //                    modifier = Modifier
                            //                        .fillMaxWidth()
                            //                        .background(color = MaterialTheme.colorScheme.inverseOnSurface)
                            //                ) {
                            //                    Text(
                            //                        modifier = Modifier
                            //                            .padding(horizontal = 15.dp)
                            //                            .padding(vertical = 2.dp),
                            //                        text = fDate.toString(),
                            ////                    text = difference.toString(),
                            ////                text = message.timeStamp.toDate().toString(),
                            //                        textAlign = TextAlign.End
                            //                    )
                            //                }
                        }


                    }
                    AnimatedVisibility(
                        visible = showTime,
                        enter = expandVertically(
                            //                    initialOffsetY = { -it/2 },

                            animationSpec = tween(500)
                        ) + fadeIn(animationSpec = tween(200)),
                        exit = shrinkVertically(
                            //                    targetOffsetY = { -it },
                            animationSpec = tween(500)
                        ) + fadeOut(animationSpec = tween(300))
                    ) {
                        Text(
                            modifier = Modifier
                                .padding(horizontal = 15.dp)
                                .padding(vertical = 2.dp)
                                .fillMaxWidth(0.8f),
                            text = fDate.toString(),
                            //                    text = difference.toString(),
                            //                text = message.timeStamp.toDate().toString(),
                            textAlign = TextAlign.Start
                        )
                    }
                }
                //        Spacer(modifier = Modifier.weight(1f))

            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReceiverGroupChat(
    modifier: Modifier = Modifier,
    message: MessageReceived,
    msgPosition: messagePosition,
    isCardSelected: Boolean,
    setAndNavigateMdMessage:(setMessage: MessageReceived)->Unit,
) {
    val date = message.timeStamp.toDate()
//    val sdf  = SimpleDateFormat("HH:mm")
    val currentDate = Timestamp.now().toDate()
//    val difference = (currentDate.time - date.time) / (1000 * 60 * 60)
    val sdf = if (currentDate == date) {
        SimpleDateFormat("hh:mm a")
    } else {
        SimpleDateFormat("dd/MM/YYYY hh:mm a")
    }
    val fDate = sdf.format(date)
    var showTime by remember {
        mutableStateOf(false)
    }
    val parsedMessage = message.parsedContent

    val aloneShape = RoundedCornerShape(5.dp, 20.dp, 20.dp, 20.dp)
    val topShape = RoundedCornerShape(20.dp, 20.dp, 20.dp, 5.dp)
    val middleShape = RoundedCornerShape(5.dp, 20.dp, 20.dp, 5.dp)
    val bottomShape = RoundedCornerShape(5.dp, 20.dp, 20.dp, 20.dp)

    Row(
        modifier = Modifier
            .padding(
                top = when (msgPosition) {
                    messagePosition.Alone -> 5.dp
                    messagePosition.Top -> 5.dp
                    messagePosition.Middle -> 0.dp
                    messagePosition.Bottom -> 0.dp
                },


                )
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .background(
                    color = if (isCardSelected) {
                        MaterialTheme.colorScheme.outline
                    } else {
                        Color.White.copy(alpha = 0f)
                    }
                )
                .padding(
                    bottom = when (msgPosition) {
                        messagePosition.Alone -> 5.dp
                        messagePosition.Top -> 0.dp
                        messagePosition.Middle -> 0.dp
                        messagePosition.Bottom -> 5.dp
                    },
                )

        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.8f),
                //            .fillMaxWidth()
            ) {
                Column {
                    if (msgPosition == messagePosition.Alone || msgPosition == messagePosition.Top) {
                        Text(
                            modifier = Modifier
                                .padding(horizontal = 5.dp)
                                .padding(top = 4.dp),
                            text = message.senderId
                        )
                    }
                    Card(
                        modifier = when (msgPosition) {
                            messagePosition.Alone -> aloneModifier
                            messagePosition.Top -> topModifier
                            messagePosition.Middle -> middleModifier
                            messagePosition.Bottom -> bottomModifier
                        }
                            .wrapContentWidth(Alignment.Start)
                            .clickable(
                                enabled = true,
                                onClick = { showTime = !showTime },
                                interactionSource = null,
                                indication = null
                            )
                        //                .fillMaxWidth(1f)
                        ,
                        //                .height(60.dp),
                        shape = when (msgPosition) {
                            messagePosition.Alone -> aloneShape
                            messagePosition.Top -> topShape
                            messagePosition.Middle -> middleShape
                            messagePosition.Bottom -> bottomShape
                        },
                        colors = CardDefaults.cardColors(
                            //                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .width(IntrinsicSize.Max)
                        ) {
                            //                    if (checkForCode(message.content)
                            //                    ) {
                            //                        GetCodeMessage(msg = message.content)
                            //                    } else {
                            //                        Text(
                            //                            text = annotateMessage(message.content),
                            //                            modifier = Modifier
                            //                                .padding(10.dp)
                            //                                .wrapContentWidth(),
                            //                            fontSize = 20.sp
                            //                        )
                            //                    }
//                            GetCodeMessage(msg = message.content)
                            when (message.contentType) {
                                ContentType.text -> GetCodeMessage(parsedMessage)
                                ContentType.image -> GetCodeMessage(parsedMessage)
                                ContentType.document -> GetCodeMessage(parsedMessage)
                                ContentType.audio -> GetCodeMessage(parsedMessage)
                                ContentType.video -> GetCodeMessage(parsedMessage)
                                ContentType.deleted -> GetCodeMessage(parsedMessage)
                                ContentType.default -> GetCodeMessage(parsedMessage)
                                ContentType.Md -> MdMessageChat(message.content) {
                                    setAndNavigateMdMessage(message)
                                }
                            }

                            //                    Card(
                            //                        modifier = Modifier
                            //                            .fillMaxWidth()
                            //                            .background(color = MaterialTheme.colorScheme.inverseOnSurface)
                            //                    ) {
                            //                        Text(
                            //                            modifier = Modifier
                            //                                .padding(horizontal = 15.dp)
                            //                                .padding(vertical = 2.dp),
                            //                            text = fDate.toString(),
                            ////                    text = difference.toString(),
                            ////                text = message.timeStamp.toDate().toString(),
                            //                            textAlign = TextAlign.End
                            //                        )
                            //                    }
                        }


                    }
                    AnimatedVisibility(
                        visible = showTime,
                        enter = expandVertically(
                            //                    initialOffsetY = { -it/2 },

                            animationSpec = tween(500)
                        ) + fadeIn(animationSpec = tween(200)),
                        exit = shrinkVertically(
                            //                    targetOffsetY = { -it },
                            animationSpec = tween(500)
                        ) + fadeOut(animationSpec = tween(300))
                    ) {
                        Text(
                            modifier = Modifier
                                .padding(horizontal = 15.dp)
                                .padding(vertical = 2.dp),
                            text = fDate.toString(),
                            //                    text = difference.toString(),
                            //                text = message.timeStamp.toDate().toString(),
                            textAlign = TextAlign.Start
                        )
                    }


                }
                //        Spacer(modifier = Modifier.weight(1f))

            }
        }
    }
}

@Composable
fun BottomMessageSend(
    modifier: Modifier = Modifier,
    appUistate: MessagesUiState,
    sendMessage: () -> Unit,
//    sendExpensiveAiMessage:()->Unit,
    updateMessage: (String) -> Unit,
    sendAttatchment: () -> Unit,
    isLoading: Boolean,
//    aiMessage : AiResponseUiState,
) {
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val visualTransformation = remember { StyledTextVisualTransformation(styleMap) }
    Box {
        ElevatedCard(
            modifier = modifier
                //            .navigationBarsPadding()
                //            .animateContentSize(
                //
                //                animationSpec = spring(
                //                    dampingRatio = Spring.DampingRatioNoBouncy,
                //                    stiffness = Spring.StiffnessLow,
                //                )

                //            )

                .fillMaxWidth()
                .padding(8.dp)
                .background(Color.Transparent),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,

                ),
            shape = RoundedCornerShape(30.dp),
//            elevation = CardDefaults.cardElevation(35.dp)
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
                    val text = appUistate.messageToSend


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
                        value = appUistate.messageToSend,
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
                        visualTransformation = visualTransformation
                        //                trailingIcon =
                        //                {
                        //
                        //                }

                    )
                    //

                    if (!isLoading) {
                        AnimatedVisibility(
                            visible = true,
                            enter = slideInHorizontally(initialOffsetX = {it}),
                            exit = slideOutHorizontally(targetOffsetX = {it})
                        )
                        {
                            IconButton(
                                onClick = {
                                    if (appUistate.messageToSend.isNotEmpty()) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
//                                    if(appUistate.currChat.isAiChat){
//                                        sendExpensiveAiMessage()
//                                    }else{
                                        sendMessage()
//                                    }
                                    } else {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        sendAttatchment()
                                    }
                                },

                                modifier = Modifier
                                    //                        .fillMaxWidth()
                                    .padding(bottom = 10.dp, end = 10.dp)
                                    .size(45.dp),
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primary

                                )
                            ) {
                                AnimatedVisibility(
                                    visible = appUistate.messageToSend.isNotEmpty(),
                                    enter = scaleIn(),
                                    exit = scaleOut()
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
                                AnimatedVisibility(
                                    visible = appUistate.messageToSend.isEmpty(),
                                    enter = scaleIn(),
                                    exit = scaleOut()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Attachment,
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
    }

}

@Composable
fun SecureMessageTag(
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .padding(10.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Text(
            text = "\uD83D\uDD12 Encrypted and protected! Not even Sherlock can crack this\uD83D\uDD76\uFE0F. Only the chosen ones can read \uD83D\uDCAA!",
            modifier = Modifier
                .padding(5.dp),
            textAlign = TextAlign.Center
        )
    }
}

//@Preview
//@Composable
//fun PreviewSecureMessage() {
//    ChitChatTheme(
//        dynamicColor = false
//
//    ) {
//        SecureMessageTag()
//    }
//}

@Preview(
    device = "spec:width=1280dp,height=800dp,dpi=240,orientation=portrait", showSystemUi = true, showBackground = false,
    wallpaper = Wallpapers.GREEN_DOMINATED_EXAMPLE, backgroundColor = 0xFFA42626
)
@Composable
fun PreviewMessagebodySuccess() {
    ChitChatTheme(
        dynamicColor = false,
//        darkTheme = true
    ) {
        MessagesBodySuccess(
            uiState = MessagesUiState(
                messageScreen = MessageScreen.Success,
                chatName = "ThereSelf",
                currChat = ChatOrGroup(
                    isGroup = false,
                    chatName = "ChatName@123"
                ),
                currentUser = "ThereSelf",
                messages = mutableListOf(
                    MessageReceived(
                        messageId = "1",
                        content = "Hello Friend",
                        parsedContent = listOf(MessageSegement("Hello bro ~strid~ style *bold*", textTypeParse.text)),
                        timeStamp = Timestamp(Date(2024 - 1900, 1, 25, 20, 15)),
                        senderId = "ThereSelf",
                        status = messageStatus.Send
                    ),
                    MessageReceived(
                        messageId = "2",
                        content = "Hello Friend How are you doing",
                        parsedContent = listOf(MessageSegement("Hello !bro! how are you doing *another* this is at end here.", textTypeParse.text)),
                        timeStamp = Timestamp(Date(2024 - 1900, 1, 25, 20, 18)),
                        senderId = "ThereSelf",
                        status = messageStatus.Send
                    ),
                    MessageReceived(
                        messageId = "4",
                        content = "Link here https://www.google.com",
                        parsedContent = listOf(MessageSegement("https://www.google.com", textTypeParse.link)),
                        timeStamp = Timestamp(Date(2024 - 1900, 1, 25, 20, 18)),
                        senderId = "ThereSelf",
                        status = messageStatus.Send
                    ),
                    MessageReceived(
                        messageId = "4",
                        content = "Link here https://www.google.com",
                        parsedContent = listOf(MessageSegement("AI Tools Directory: Video, Image, 3D, and Local AI\n" +
                                "### \uD83C\uDFA5 Text-to-Video Generators (Cloud-Based)\n" +
                                "Note: These tools run on cloud servers. Your local GPU, Mac, or phone specs do not affect their generation speed, only your web browser experience. Even the oldest chips survive here!\n" +
                                " * Pika (pika.art) – Cinematic, stylized motion from text prompts.\n" +
                                " * Runway Gen-2 / Gen-3 Alpha ([suspicious link removed]) – Industry-leading photorealistic temporal consistency.\n" +
                                " * Luma Dream Machine (lumalabs.ai/dream-machine) – Fast transformer model for realistic physics.\n" +
                                " * Kling AI (klingai.com) – Framework capable of holding long (up to 2 mins) consistent shots.\n" +
                                " * Kaiber AI (kaiber.ai) – Audio-reactive diffusion models for music synchronization.\n" +
                                "Performance across all Cloud Video tools:\n" +
                                " * Desktop 5070 Ti (16GB): ⭐⭐⭐⭐⭐\n" +
                                " * Laptop 5070 Ti (12GB): ⭐⭐⭐⭐⭐\n" +
                                " * RTX 5060: ⭐⭐⭐⭐⭐\n" +
                                " * Mac M4 (16GB): ⭐⭐⭐⭐⭐\n" +
                                " * MX450: ⭐⭐⭐⭐⭐\n" +
                                " * Snapdragon 8 Elite Gen 5: ⭐⭐⭐⭐⭐ (Runs perfectly in mobile Chrome/Brave)\n" +
                                " * Snapdragon 8s Gen 3: ⭐⭐⭐⭐⭐\n" +
                                " * Snapdragon 7s Gen 2: ⭐⭐⭐⭐⭐\n" +
                                "\uD83D\uDD13 Open-Source Video Models (Local / Self-Hosted)\n" +
                                "Note: Local video generation is extremely VRAM hungry. Android devices running Linux PRoot environments lack dedicated VRAM and rely on CPU rendering with high overhead. Don't expect to run heavy video generation on a phone.\n" +
                                " * Open-Sora 2.0 (GitHub) – 11B parameter DiT mapping text to video latents.\n" +
                                " * Desktop 5070 Ti (16GB): ⭐⭐⭐⭐\n" +
                                " * Laptop 5070 Ti (12GB): ⭐⭐ (Requires heavy quantization)\n" +
                                " * RTX 5060: ⭐ (VRAM bottleneck)\n" +
                                " * Mac M4 (16GB): ⭐ (Will struggle with unified memory limits)\n" +
                                " * MX450: ❌ (Instant Crash)\n" +
                                " * Snapdragon 8 Elite Gen 5 (Termux PRoot): ❌ (Linux PRoot overhead + RAM limits will cause an instant OOM kill)\n" +
                                " * Snapdragon 8s Gen 3 (Termux PRoot): ❌\n" +
                                " * Snapdragon 7s Gen 2 (Termux PRoot): ❌\n" +
                                " * LTX-Video (GitHub) – Highly optimized, fast-stepping diffusion model.\n" +
                                " * Desktop 5070 Ti (16GB): ⭐⭐⭐⭐⭐\n" +
                                " * Laptop 5070 Ti (12GB): ⭐⭐⭐⭐⭐ (Perfect fit)\n" +
                                " * RTX 5060: ⭐⭐⭐⭐\n" +
                                " * Mac M4 (16GB): ⭐⭐⭐⭐\n" +
                                " * MX450: ❌\n" +
                                " * Snapdragon 8 Elite Gen 5 (Termux PRoot): ⭐ (Might theoretically boot a quantized version overnight, but practically unusable)\n" +
                                " * Snapdragon 8s Gen 3 (Termux PRoot): ❌\n" +
                                " * Snapdragon 7s Gen 2 (Termux PRoot): ❌\n" +
                                " * CogVideoX (GitHub) – 3D RoPE and 3D VAE architecture.\n" +
                                " * Desktop 5070 Ti (16GB): ⭐⭐⭐⭐⭐\n" +
                                " * Laptop 5070 Ti (12GB): ⭐⭐⭐⭐⭐ (For 2B Model)\n" +
                                " * RTX 5060: ⭐⭐⭐⭐⭐ (For 2B Model)\n" +
                                " * Mac M4 (16GB): ⭐⭐⭐⭐\n" +
                                " * MX450: ⭐\n" +
                                " * Snapdragon 8 Elite Gen 5 (Termux PRoot): ❌ (No native PyTorch/CUDA support via PRoot makes 3D attention layers fail)\n" +
                                " * Snapdragon 8s Gen 3 (Termux PRoot): ❌\n" +
                                " * Snapdragon 7s Gen 2 (Termux PRoot): ❌\n" +
                                "### \uD83C\uDFA8 Specialized & Design Video (Cloud)\n" +
                                "Note: All cloud-based. Hardware does not matter.\n" +
                                " * Reeroll (reeroll.com) – AI motion graphics templates.\n" +
                                " * Golpo AI (video.golpoai.com) – Whiteboard explainers.\n" +
                                " * Boba AI (boba.video) – Anime style with lip-sync.\n" +
                                " * Hera (hera.video) – AI motion designer.\n" +
                                " * Bazaar (bazaar.it) – App screenshots to demo videos.\n" +
                                " * Lumen5 (lumen5.com) – Blog/Article to video.\n" +
                                "Performance across all Cloud Design tools:\n" +
                                " * Desktop 5070 Ti (16GB): ⭐⭐⭐⭐⭐\n" +
                                " * Laptop 5070 Ti (12GB): ⭐⭐⭐⭐⭐\n" +
                                " * RTX 5060: ⭐⭐⭐⭐⭐\n" +
                                " * Mac M4 (16GB): ⭐⭐⭐⭐⭐\n" +
                                " * MX450: ⭐⭐⭐⭐⭐\n" +
                                " * Snapdragon 8 Elite Gen 5: ⭐⭐⭐⭐⭐\n" +
                                " * Snapdragon 8s Gen 3: ⭐⭐⭐⭐⭐\n" +
                                " * Snapdragon 7s Gen 2: ⭐⭐⭐⭐⭐\n" +
                                "### \uD83E\uDDCA 3D Generation & Spatial AI\n" +
                                " * Marble (marble.worldlabs.ai) – Cloud-based NeRF / Gaussian Splatting from text.\n" +
                                " * Desktop 5070 Ti (16GB): ⭐⭐⭐⭐⭐\n" +
                                " * Laptop 5070 Ti (12GB): ⭐⭐⭐⭐⭐\n" +
                                " * RTX 5060: ⭐⭐⭐⭐⭐\n" +
                                " * Mac M4 (16GB): ⭐⭐⭐⭐⭐\n" +
                                " * MX450: ⭐⭐⭐⭐\n" +
                                " * Snapdragon 8 Elite Gen 5: ⭐⭐⭐⭐⭐ (Handles WebGL rendering natively in browser flawlessly)\n" +
                                " * Snapdragon 8s Gen 3: ⭐⭐⭐⭐\n" +
                                " * Snapdragon 7s Gen 2: ⭐⭐⭐\n" +
                                " * Shap-E (GitHub) – Local model outputting implicit 3D functions.\n" +
                                " * Desktop 5070 Ti (16GB): ⭐⭐⭐⭐⭐\n" +
                                " * Laptop 5070 Ti (12GB): ⭐⭐⭐⭐⭐\n" +
                                " * RTX 5060: ⭐⭐⭐⭐⭐\n" +
                                " * Mac M4 (16GB): ⭐⭐⭐⭐⭐\n" +
                                " * MX450: ⭐⭐\n" +
                                " * Snapdragon 8 Elite Gen 5 (Termux): ⭐⭐ (CPU inference works inside PRoot, but rendering is slow)\n" +
                                " * Snapdragon 8s Gen 3 (Termux): ⭐\n" +
                                " * Snapdragon 7s Gen 2 (Termux): ❌\n" +
                                "### \uD83D\uDCBB Local AI & Infrastructure (Launchers & LLMs) is this limit\n" +
                                "Note: This is where Android phones using Termux natively shine! By using compiled llama.cpp or the ollama package directly in Termux, you can bypass Linux PRoot overhead and run AI directly on the Snapdragon chip, taking advantage of fast LPDDR RAM.\n" +
                                " * Ollama / Llama.cpp (Local LLMs) (ollama.com) – C++ based local inference engine.\n" +
                                " * Desktop 5070 Ti (16GB): ⭐⭐⭐⭐⭐ (Handles 14B+ models easily)\n" +
                                " * Laptop 5070 Ti (12GB): ⭐⭐⭐⭐ (Handles 8B models beautifully)\n" +
                                " * RTX 5060: ⭐⭐⭐ (Limited by 8GB VRAM)\n" +
                                " * Mac M4 (16GB): ⭐⭐⭐⭐⭐ (Apple's Unified memory bandwidth is king here)\n" +
                                " * MX450: ⭐ (Painfully slow 2 tokens/sec)\n" +
                                " * Snapdragon 8 Elite Gen 5 (Termux Native): ⭐⭐⭐⭐⭐ (Has incredible memory bandwidth and Hexagon NPU. Runs 8B models at 25+ tokens/sec locally!)\n" +
                                " * Snapdragon 8s Gen 3 (Termux Native): ⭐⭐⭐ (Can run 3B/4B models comfortably; 8B models will be slower but usable)\n" +
                                " * Snapdragon 7s Gen 2 (Termux Native): ⭐ (Will struggle, memory bandwidth is too low. Expect 1-2 tokens/sec on tiny models)\n" +
                                "### \uD83D\uDEE0\uFE0F Open-Source Editors & Image Tools (Local)\n" +
                                " * ComfyUI (comfy.org) – Node-based GUI. On Android, requires Termux + PRoot Ubuntu + Termux-X11 + running in CPU mode (--cpu).\n" +
                                " * Desktop 5070 Ti (16GB): ⭐⭐⭐⭐⭐\n" +
                                " * Laptop 5070 Ti (12GB): ⭐⭐⭐⭐⭐\n" +
                                " * RTX 5060: ⭐⭐⭐⭐\n" +
                                " * Mac M4 (16GB): ⭐⭐⭐⭐\n" +
                                " * MX450: ⭐⭐\n" +
                                " * Snapdragon 8 Elite Gen 5 (Termux PRoot): ⭐⭐⭐ (With CPU mode, its massive raw CPU power can actually generate an SD 1.5 image in about 1-2 minutes. Usable for basic workflows!)\n" +
                                " * Snapdragon 8s Gen 3 (Termux PRoot): ⭐⭐ (Takes 5-8 minutes per image. Phone will get very hot)\n" +
                                " * Snapdragon 7s Gen 2 (Termux PRoot): ❌ (Android OS will violently kill the Termux process to save RAM)\n" +
                                " * Qwen Image Edit (HuggingFace) – Instruction-based VLM editing.\n" +
                                " * Desktop 5070 Ti (16GB): ⭐⭐⭐⭐⭐\n" +
                                " * Laptop 5070 Ti (12GB): ⭐⭐⭐⭐\n" +
                                " * RTX 5060: ⭐⭐⭐\n" +
                                " * Mac M4 (16GB): ⭐⭐⭐⭐\n" +
                                " * MX450: ❌\n" +
                                " * Snapdragon 8 Elite Gen 5 (Termux PRoot): ⭐ (Too heavy for Android PRoot emulation overhead)\n" +
                                " * Snapdragon 8s Gen 3 (Termux PRoot): ❌\n" +
                                " * Snapdragon 7s Gen 2 (Termux PRoot): ❌\n" +
                                "### \uD83D\uDC64 Portrait & Character Video (Local)\n" +
                                " * Flash Portrait / StoryMem / InfCam – Specialized ControlNet wrappers for talking heads.\n" +
                                " * Desktop 5070 Ti (16GB): ⭐⭐⭐⭐⭐\n" +
                                " * Laptop 5070 Ti (12GB): ⭐⭐⭐⭐\n" +
                                " * RTX 5060: ⭐⭐⭐\n" +
                                " * Mac M4 (16GB): ⭐⭐\n" +
                                " * MX450: ❌\n" +
                                " * Snapdragon 8 Elite Gen 5 (Termux PRoot): ❌ (Running multiple ControlNets simultaneously inside a Linux container will instantly nuke Android's RAM management)\n" +
                                " * Snapdragon 8s Gen 3 (Termux PRoot): ❌\n" +
                                " * Snapdragon 7s Gen 2 (Termux PRoot): ❌" +
                                "Ends here last....", textTypeParse.text)),
                        timeStamp = Timestamp(Date(2024 - 1900, 1, 25, 20, 18)),
                        senderId = "ThereSelf",
                        status = messageStatus.Send
                    ),
                    MessageReceived(
                        messageId = "3",
                        content = "**Hello** this is **new markdown** message",
                        contentType = ContentType.Md,
                        timeStamp = Timestamp(Date(2024 - 1900, 1, 25, 20, 20)),
                        senderId = "MyeSElf",
                        status = messageStatus.Send
                    ),
//                    MessageReceived(
//                        messageId = "3",
//                        content = "Hello Friend its nice to see you",
//                        timeStamp = Timestamp(Date(2024 - 1900, 1, 25, 20, 19)),
//                        senderId = "nys",
//                        status = messageStatus.Send
//                    ),
//                    MessageReceived(
//                        messageId = "4",
//                        content = "Hello Friend good day",
//                        timeStamp = Timestamp(Date(2024 - 1900, 1, 25, 20, 20)),
//                        senderId = "nys",
//                        status = messageStatus.Send
//                    ),
//                    MessageReceived(
//                        messageId = "5",
//                        content = "Hello Friend good day",
//                        timeStamp = Timestamp(Date(2024 - 1900, 2, 25, 20, 18)),
//                        senderId = "nys",
//                        status = messageStatus.Send
//                    ),
                ),
                messageToSend = "",
                selectedSentMessages = listOf(
                    MessageReceived(
                        messageId = "1",
                        content = "Hello Friend",
                        timeStamp = Timestamp(Date(2024 - 1900, 1, 25, 20, 15)),
                        senderId = "ThereSelf",
                        status = messageStatus.Send
                    ),
                ),
                selectedReceivedMessages = listOf(
                    MessageReceived(
                        messageId = "3",
                        content = "Hello Friend its nice to see you",
                        timeStamp = Timestamp(Date(2024 - 1900, 1, 25, 20, 19)),
                        senderId = "nys",
                        status = messageStatus.Send
                    ),
//                    MessageReceived(
//                        messageId = "4",
//                        content = "Hello Friend good day",
//                        timeStamp = Timestamp(Date(2024 - 1900, 1, 25, 20, 20)),
//                        senderId = "nys",
//                        status = messageStatus.Send
//                    ),
                )
            ),
            updateMessage = {},
            getMessagesAgain = { },
            sendMessage = {},
            navigateUp = {},
            deselectAll = {},
            deleteMessages = {},
            selectionBoth = {},
            navigateToMdSending = {},
            setAndNavigateMdMessage = {},
        )
    }
}

//
//@Preview(backgroundColor = 0xFFFFFFFF)
//@Composable
//fun PreviewSenderChat() {
//    ChitChatTheme(
//        dynamicColor = false,
////        darkTheme = true
//    ) {
//        SenderChat(
//            message = MessageReceived(
//                content = "Hello Friend "
//            )
//        )
//    }
//}
//
//
//@Preview
//@Composable
//fun PreviewReceiverChat() {
//    ChitChatTheme(
//        dynamicColor = false
//    ) {
//        ReceiverChat(
//            message = MessageReceived(
//                content = "Hello Friend \nThis is your friend",
//                timeStamp = Timestamp(Date(2024 - 1900, 1, 25, 20, 15))
//            )
//        )
//    }
//}
//
//
//@Preview
//@Composable
//fun PreviewAttatchmentContent() {
//    ChitChatTheme(
//        dynamicColor = false
//    ) {
//        AttatchmentContent(
//            hideSheet = {},
//            sendCode = {}
//        )
//    }
//}
