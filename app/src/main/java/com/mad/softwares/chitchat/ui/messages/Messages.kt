package com.mad.softwares.chitchat.ui.messages

import android.graphics.drawable.shapes.Shape
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.TextField
import androidx.compose.material3.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowOutward
import androidx.compose.material.icons.filled.Attachment
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Dock
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.PlatformSpanStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.fido.fido2.api.common.Attachment
import com.google.firebase.Timestamp
import com.mad.softwares.chitchat.R
import com.mad.softwares.chitchat.data.MessageReceived
import com.mad.softwares.chitchat.data.messageStatus
import com.mad.softwares.chitchat.ui.ApptopBar
import com.mad.softwares.chitchat.ui.GodViewModelProvider
import com.mad.softwares.chitchat.ui.destinationData
import com.mad.softwares.chitchat.ui.theme.ChitChatTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date

object messagesdestinationData : destinationData {
    override val route = "Messages"
    val chatIDAndUsername = "chatID,username"
    override val title = R.string.chats
    override val canBack = true
    val routeWithArgs = "$route/{$chatIDAndUsername}"
}

@Composable
fun Messages(
    viewModel: MessagesViewModel = viewModel(factory = GodViewModelProvider.Factory),
    navigateUp: () -> Unit
) {
    val uiState = viewModel.messagesUiState.collectAsState().value
//    val currentScreen = uiState.messageScreen
    when (uiState.messageScreen) {
        MessageScreen.Loading -> {
            MessageBodyLoading()
        }

        MessageScreen.Error -> {
            ShowMessageError {
//                viewModel.getMessages(isForced = true)
            }
        }

        MessageScreen.Success -> {

            MessagesBodySuccess(
                uiState = uiState,
                updateMessage = { viewModel.messageEdit(it) },
                getMessagesAgain = {
//                    viewModel.getMessages(isForced = true)
                },
                sendMessage = { viewModel.sendTextMessage() },
                navigateUp = navigateUp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesBodySuccess(
    uiState: MessagesUiState,
    updateMessage: (String) -> Unit,
    getMessagesAgain: () -> Unit,
    sendMessage: () -> Unit,
    navigateUp: () -> Unit,
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
    Scaffold(
        topBar = {
            ApptopBar(
                destinationData = messagesdestinationData,
                navigateUp = navigateUp,
                title = uiState.chatName,
//                action = {
//                    IconButton(onClick = { getMessagesAgain() }) {
//                        Icon(
//                            imageVector = Icons.Default.Refresh,
//                            contentDescription = "refresh"
//                        )}
//                }
            )
        },
        bottomBar = {
            BottomMessageSend(
                appUistate = uiState,
                sendMessage = sendMessage,
                updateMessage = updateMessage,
                sendAttatchment = {
                    sheetVisible = true
                }
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        },

        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
    ) { padding ->

        if (uiState.messages.isNotEmpty()) {
            LazyColumn(
                modifier = modifier
                    .padding(padding)
                    .fillMaxSize(),
//                    .background(MaterialTheme.colorScheme.background),
                reverseLayout = true,
                verticalArrangement = Arrangement.Bottom
            ) {
                items(uiState.messages.reversed()) {
                    if (it.senderId == uiState.chatName) {
                        ReceiverChat(message = it)
                    } else {
                        SenderChat(message = it)
                    }
                }
            }

//            if (codeSheetVisible){
//                ModalBottomSheet(onDismissRequest = { codeSheetVisible = false }) {
////                    AttachCode(code = "", changeCodeName ={} , changeCode = {}){
////                        //Hide the Bottom Sheet
////                        scope.launch { codeSheetState.hide() }.invokeOnCompletion {
////                            if (!codeSheetState.isVisible) {
////                                codeSheetVisible = false
////                            }
////                        }
////
////                        //Show the snack bar
////                        snackbarHostState.currentSnackbarData?.dismiss()
////                        scope.launch {
////                            snackbarHostState.showSnackbar(
////                                message = "This function is not implemented yet",
////                                withDismissAction = true
////                            )
////                        }
////                    }
//
//                    Text(text = "Featured Removed and will be added in the text filed itself.")
//                }
//            }

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
                                    message = "This function is not implemented yet",
                                    withDismissAction = true
                                )
                            }
                        }
                    )

                }
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
//                    .background(MaterialTheme.colorScheme.background),
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
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 15.dp, bottom = 100.dp),
        horizontalArrangement = Arrangement.Absolute.Center
    ) {

        AttachmentButton(onClick = {
            hideSheet()
            sendCode()
        }, image = Icons.Default.LocationOn, text = "Send Location")
        AttachmentButton(onClick = {
            hideSheet()
            sendCode()
        }, image = Icons.Default.Image, text = "Send Image")
        AttachmentButton(onClick = {
            hideSheet()
            sendCode()
        }, image = Icons.Default.UploadFile, text = "Send Document")
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

val codeRegex = Regex("'''(.*?)'''")

fun annotateMessage(
    msg: String
): AnnotatedString {

    val annotedMessage = buildAnnotatedString {

        var currentIndex = 0
        codeRegex.findAll(msg).forEach { match ->
            append(msg.substring(currentIndex, match.range.first))
            withStyle(
                style = SpanStyle(
                    //                color = Color.Red,
                    fontFamily = FontFamily.Monospace,
                    background = Color.LightGray,
                    fontSize = 20.sp,
                    platformStyle = PlatformSpanStyle()
                )
            ) {
                append("\n")
                //                append(match.value.removeSurrounding("'''"))
                appendInlineContent(
                    id = "card_${match.value}",
                    alternateText = match.value.removeSurrounding("'''")
                )
//                append("\n")
            }
            currentIndex = match.range.last + 1
        }
        append(msg.substring(currentIndex, msg.length))
    }

    return annotedMessage
}

@Composable
fun InlineStyles(
    msg: String
): Map<String, InlineTextContent> {

    val inlineContentMap = codeRegex.findAll(msg).associate { match ->
        val cardText = match.value.removeSurrounding("'''")
        val density = LocalDensity.current
        val textWidth = with(density) {
            // Estimate width based on the character count of the card content
            (cardText.length * 15).toSp()
        }
        val textHeight = with(density) {
            // Fixed height for the card
            120.sp
        }
        "card_${match.value}" to
                InlineTextContent(
                    placeholder = Placeholder(
                        height = 6.em,
                        width = textWidth,
                        placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter
                    ),
                    children = {
//                        Card(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(10.dp),
//                            colors = CardDefaults.cardColors(
//                                contentColor = MaterialTheme.colorScheme.error
//                            ),
//                            shape = RoundedCornerShape(15.dp)
//                        ) {
//                            Text(
//                                modifier = Modifier
//                                    .padding(4.dp),
//                                text = match.value.removeSurrounding("'''"),
//                                fontSize = 22.sp
//                            )
//                        }

                        SubcomposeLayout { constraints ->
                            val cardContent = subcompose("card_${match.value}") {
                                Card(
                                    modifier = Modifier.wrapContentWidth(),
                                    colors = CardDefaults.cardColors(
                                        contentColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Text(
                                        text = cardText,
                                        modifier = Modifier.padding(4.dp),
                                        fontSize = 20.sp
                                    )
                                }
                            }

                            val placeable = cardContent[0].measure(constraints)
                            layout(placeable.width, placeable.height) {
                                placeable.placeRelative(0, 45)
                            }
                        }
                    }
                )

    }

    return inlineContentMap
}



fun parseMessage(message: String): Map<String, Boolean> {
    val result = mutableMapOf<String, Boolean>()
    val codeRegex = Regex("'''(.*?)'''", RegexOption.DOT_MATCHES_ALL)
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
}

@Composable
fun GetCodeMessage(msg:String)
{
    val parsedMessage= parseMessage(msg)
    Column(
        modifier = Modifier
            .padding(10.dp)
    ) {
        parsedMessage.forEach {
            if (it.value) {
                Card(
                    modifier = Modifier,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = it.key,
                        modifier = Modifier
                            .padding(6.dp),
                        fontSize = 20.sp,

//                    inlineContent = InlineStyles(msg = msg),
//                    onTextLayout = {}
                    )
                }
            } else {
                Text(
                    text = it.key,
                    modifier = Modifier,
                    fontSize = 20.sp,
                )
            }
        }
    }
}

@Composable
fun SenderChat(
    message: MessageReceived
) {
    val date = message.timeStamp.toDate()
    //    val sdf  = SimpleDateFormat("HH:mm")
    val currentDate = Timestamp.now().toDate()
    val difference = (currentDate.time - date.time) / (1000 * 60 * 60)
    val msg = message.content


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
    val parsedMessage = parseMessage(msg)
    Row(
        modifier = Modifier
            .fillMaxWidth(1f),
        horizontalArrangement = Arrangement.End
    ) {

        Card(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                //                .weight(5f)
                .wrapContentWidth(Alignment.End)
                //                .padding(start = 80.dp)
                .padding(vertical = 10.dp, horizontal = 5.dp),
            //                .height(60.dp),
            shape = RoundedCornerShape(20.dp, 0.dp, 20.dp, 20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            GetCodeMessage(msg = msg)
            Column(
                modifier = Modifier
                    .width(IntrinsicSize.Max)
            ) {

                Card(
                    modifier = Modifier
                        //                    .wrapContentWidth(unbounded = true)
                        .fillMaxWidth()
                        .padding(0.dp),

                    ) {
                    Row(
                        modifier = Modifier,
                        //                        .fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        Text(
                            modifier = Modifier
                                //                            .fillMaxWidth()
                                .padding(horizontal = 15.dp)
                                .padding(vertical = 2.dp),
                            text = fDate.toString(),
                            //                    text = difference.toString(),
                            //                text = message.timeStamp.toDate().toString(),
                            textAlign = TextAlign.Start
                        )
                        if (message.status == messageStatus.Send) {
                            Icon(
                                imageVector = Icons.Default.ArrowOutward,
                                contentDescription = null
                            )
                        } else if (message.status == messageStatus.Sending) {
                            Icon(
                                imageVector = Icons.Default.CloudQueue,
                                contentDescription = null
                            )
                        } else if (message.status == messageStatus.Error) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null
                            )
                        }
                    }
                }
            }
        }


    }
}

@Composable
fun ReceiverChat(
    message: MessageReceived
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
    Row(
        modifier = Modifier
            .fillMaxWidth(0.8f),
//            .fillMaxWidth()
    ) {
        Card(
            modifier = Modifier
                .padding(vertical = 10.dp, horizontal = 5.dp)
                .wrapContentWidth(Alignment.Start)
//                .fillMaxWidth(1f)
            ,
//                .height(60.dp),
            shape = RoundedCornerShape(0.dp, 20.dp, 20.dp, 20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .width(IntrinsicSize.Max)
            ) {
//                Text(
//                    text = annotateMessage(message.content),
//                    modifier = Modifier
//                        .padding(10.dp)
//                        .wrapContentWidth(),
//                    fontSize = 20.sp
//                )
                GetCodeMessage(msg = message.content)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = MaterialTheme.colorScheme.inverseOnSurface)
                ) {
                    Text(
                        modifier = Modifier
                            .padding(horizontal = 15.dp)
                            .padding(vertical = 2.dp),
                        text = fDate.toString(),
//                    text = difference.toString(),
//                text = message.timeStamp.toDate().toString(),
                        textAlign = TextAlign.End
                    )
                }
            }


        }
//        Spacer(modifier = Modifier.weight(1f))

    }
}


@Composable
fun BottomMessageSend(
    appUistate: MessagesUiState,
    sendMessage: () -> Unit,
    updateMessage: (String) -> Unit,
    sendAttatchment: () -> Unit
) {
    val scope = rememberCoroutineScope()
    ElevatedCard(
        Modifier
//            .animateContentSize(
//
//                animationSpec = spring(
//                    dampingRatio = Spring.DampingRatioNoBouncy,
//                    stiffness = Spring.StiffnessLow,
//                )

//            )

            .fillMaxWidth()
            .padding(8.dp)
            .background(MaterialTheme.colorScheme.background),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,

            ),
        shape = RoundedCornerShape(30.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
//                .padding(vertical = 16.dp, horizontal = 16.dp)
        )
        {
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
                    placeholder = { Text(text = "Start typing...") },
//                    placeholder = {"message..."},
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0, 0, 0, alpha = 0),
                        unfocusedBorderColor = Color(0, 0, 0, alpha = 0),
                        disabledBorderColor = Color(0, 0, 0, alpha = 0)
                    ),

                    //                trailingIcon =
//                {
//
//                }

                )
//

                IconButton(
                    onClick = {
                        if (appUistate.messageToSend.isNotEmpty()) {
                            sendMessage()
                        } else {
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


@Preview
@Composable
fun PreviewMessagebodySuccess() {
    ChitChatTheme(
        dynamicColor = false,
//        darkTheme = true
    ) {
        MessagesBodySuccess(
            uiState = MessagesUiState(
                chatName = "ThereSelf",
                messages = mutableListOf(
                    MessageReceived(
                        content = "Hello Friend",
                        senderId = "mySelf",
                        timeStamp = Timestamp(Date(2024 - 1900, 1, 25, 20, 15))
                    ),
                    MessageReceived(
                        content = "Hey there How are you",
                        senderId = "ThereSelf",
                        timeStamp = Timestamp(Date(2024 - 1900, 3, 25, 20, 17))
                    ),
                    MessageReceived(
                        content = "good to say This is a long message and we are gonint ot test this too",
                        senderId = "mySelf",
                        timeStamp = Timestamp(Date(2024 - 1900, 2, 25, 20, 15))
                    ),
                    MessageReceived(
                        content = "Ok Bye",
                        senderId = "ThereSelf",
                        timeStamp = Timestamp(Date(2024 - 1900, 2, 25, 20, 15))
                    ),
                    MessageReceived(
                        content = "Hello Friend",
                        senderId = "mySelf",
                        timeStamp = Timestamp(Date(2024 - 1900, 1, 25, 20, 15))
                    ),
                    MessageReceived(
                        content = "Hey there How are you",
                        senderId = "ThereSelf",
                        timeStamp = Timestamp(Date(2024 - 1900, 1, 25, 20, 17))
                    ),
                    MessageReceived(
                        content = "Customize Toolbar... 124",
                        senderId = "mySelf",
                        timeStamp = Timestamp(Date(2024 - 1900, 2, 25, 20, 15))
                    ),
                    MessageReceived(
                        content = "This is a long message an \n'''this is goinh yo\n occupy whole screen and we have to avoid''' that now.",
                        senderId = "ThereSelf",
                        timeStamp = Timestamp(Date(2024 - 1900, 2, 25, 20, 15))
                    ),
                    MessageReceived(
                        content = "Hello Friend",
                        senderId = "mySelf",
                        timeStamp = Timestamp(Date(2024 - 1900, 1, 25, 20, 15))
                    ),
                    MessageReceived(
                        content = "Hey there How are you",
                        senderId = "ThereSelf",
                        timeStamp = Timestamp(Date(2024 - 1900, 1, 25, 20, 17))
                    ),
                    MessageReceived(
                        content = "good to say '''fda and we ate goin to teshio thaio''' nlhasfihobphbkjwahf aw;jhflkb aweklfb ",
                        senderId = "mySelf",
                        timeStamp = Timestamp(Date(2024 - 1900, 2, 25, 20, 15))
                    ),
                    MessageReceived(
                        content = "Ok Bye",
                        senderId = "ThereSelf",
                        timeStamp = Timestamp(Date(2024 - 1900, 2, 25, 20, 15))
                    ),
                ),
            ),
            updateMessage = {},
            getMessagesAgain = { },
            sendMessage = {},
            navigateUp = {}
        )
    }
}

@Preview(backgroundColor = 0xFFFFFFFF)
@Composable
fun PreviewSenderChat() {
    ChitChatTheme(
        dynamicColor = false,
//        darkTheme = true
    ) {
        SenderChat(
            message = MessageReceived(
                content = "Hello Friend "
            )
        )
    }
}


@Preview
@Composable
fun PreviewReceiverChat() {
    ChitChatTheme(
        dynamicColor = false
    ) {
        ReceiverChat(
            message = MessageReceived(
                content = "Hello Friend \nThis is your friend",
                timeStamp = Timestamp(Date(2024 - 1900, 1, 25, 20, 15))
            )
        )
    }
}

@Preview
@Composable
fun PreviewAttatchmentContent() {
    ChitChatTheme(
        dynamicColor = false
    ) {
        AttatchmentContent(
            hideSheet = {},
            sendCode = {}
        )
    }
}
