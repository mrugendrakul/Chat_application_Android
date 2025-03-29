package com.mad.softwares.chatApplication.ui.messages

import android.content.Intent
import android.net.Uri
import android.os.Message
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowOutward
import androidx.compose.material.icons.filled.Attachment
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.UploadFile
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
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Shapes
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.Timestamp
import com.mad.softwares.chatApplication.R
import com.mad.softwares.chatApplication.data.ChatOrGroup
import com.mad.softwares.chatApplication.data.MessageReceived
import com.mad.softwares.chatApplication.data.messageStatus
import com.mad.softwares.chatApplication.ui.ApptopBar
import com.mad.softwares.chatApplication.ui.GodViewModelProvider
import com.mad.softwares.chatApplication.ui.destinationData
import com.mad.softwares.chatApplication.ui.theme.ChitChatTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
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
    var expandDDMenu by remember {
        mutableStateOf(false)
    }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var currentDate by remember {
        mutableStateOf("")
    }
    Scaffold(
        topBar = {
            ApptopBar(
                destinationData = messagesdestinationData,
                navigateUp = navigateUp,
                title = uiState.currChat.chatName,
                action = {
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
                            DropdownMenuItem(text = { Text(text = mem) }, onClick = { /*TODO*/ })
                        }

                    }
                }
            )
        },
        bottomBar = {
            BottomMessageSend(
                modifier = Modifier.statusBarsPadding(),
                appUistate = uiState,
                sendMessage = {
                    sendMessage()
                    coroutineScope.launch {
                        listState.animateScrollToItem(0)
                    }
                },
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
            .imePadding()
    ) { padding ->


        if (uiState.messages.isNotEmpty()) {
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
                    .fillMaxSize(),
//                    .background(MaterialTheme.colorScheme.background),
                reverseLayout = true,
                state = listState,
                verticalArrangement = Arrangement.Top,
//                contentPadding = PaddingValues(top = 100.dp)
            ) {


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
                    ) {

                        if (it.senderId != uiState.currentUser) {
                            if (uiState.currChat.isGroup == false) {
                                ReceiverChat(message = it)
                            } else {
                                ReceiverGroupChat(message = it)
                            }
                        } else {
                            SenderChat(message = it)
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

data class StyleRegex(
    val Regex: Regex = Regex(""),
    val style: SpanStyle = SpanStyle(),
    val symbol: String = ""
)

fun annotateMessage(
    msg: String
): AnnotatedString {
    val strideCodex = Regex("~(.*?)~", RegexOption.DOT_MATCHES_ALL)
    val italicCodex = Regex("_(.*?)_", RegexOption.DOT_MATCHES_ALL)

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
            )
        )
    )
    val annotedMessage = buildAnnotatedString {

        var currentIndex = 0
        val length = msg.length
        while (currentIndex < length) {
            var matchFound = false

            for ((regex, style, symbol) in styleMap) {
                val match = regex.find(msg, currentIndex)

                if (match != null && match.range.first == currentIndex) {
                    // Apply the style to the matched text
                    withStyle(style) {
                        append(match.groupValues[1])
                    }
                    currentIndex = match.range.last + 1
                    matchFound = true
                    break
                }
            }

            if (!matchFound) {
                // Append the current character and move to the next
                append(msg[currentIndex])
                currentIndex++
            }
        }
        append(msg.substring(currentIndex, msg.length))
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

enum class textTypeParse{
    text,
    code,
    link
}

data class containerMessage(
    val regex: Regex,
    val type: textTypeParse
)


fun parseMessage(message: String): Map<String, textTypeParse> {
    val result = mutableMapOf<String, textTypeParse>()

    val codeRegex = Regex("```(.*?)```", RegexOption.DOT_MATCHES_ALL)
//    val linkRegex = Regex("<([^\\s]+)>", RegexOption.DOT_MATCHES_ALL) // Ensure no spaces in links
    val linkRegex = Regex("\\<(.*?)\\>")
//    val linkRegex = Regex("(?:\\[(.*?)]?)\\<(.*?)\\>")

    // Collect matches from al regex patterns
    val matches = mutableListOf<Triple<IntRange, textTypeParse, String>>()

    codeRegex.findAll(message).forEach { matches.add(Triple(it.range, textTypeParse.code, it.groupValues[1])) }
    linkRegex.findAll(message).forEach { matches.add(Triple(it.range , textTypeParse.link, it.groupValues[1])) }

    // Sort matches by their start positions
    matches.sortBy { it.first.first }

    var lastIndex = 0

    // Iterate through sorted matches and build the map
    for ((range, type,content) in matches) {
        val start = range.first
        val end = range.last + 1

        // Add the text before the match as plain text
        if (lastIndex < start) {
            val textPart = message.substring(lastIndex, start)
//            val textPart = content
            result[textPart] = textTypeParse.text
        }

        // Add the matched text
//        var matchedPart = message.substring(start, end)
        var matchedPart = content
        when(type){
            textTypeParse.text -> {
            }
            textTypeParse.code -> {
//                matchedPart = matchedPart.removeSurrounding("```")
            }
            textTypeParse.link -> {
//                matchedPart = matchedPart.removeSurrounding("<",">")

            }
        }
        result[matchedPart] = type

        // Update last index
        lastIndex = end
    }

    // Add any remaining text as plain text
    if (lastIndex < message.length) {
        val remainingText = message.substring(lastIndex)
        result[remainingText] = textTypeParse.text
    }

    return result
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

@Composable
fun GetCodeMessage(msg: String) {
    val parsedMessage = parseMessage(msg)
    val handler = LocalUriHandler.current
    Column(
        modifier = Modifier
            .padding(10.dp)
    ) {
        val context = LocalContext.current
        parsedMessage.forEach {
            if (it.value == textTypeParse.code) {
                Card(
                    modifier = Modifier,
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    SelectionContainer {
                        Text(
                            text = it.key,
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
            else if(it.value == textTypeParse.link){
//                Card(
//                    modifier = Modifier,
//                    shape = RoundedCornerShape(10.dp),
//                    colors = CardDefaults.cardColors(
//                        containerColor = MaterialTheme.colorScheme.error
//                    )
//                ) {
                    SelectionContainer {
                        Text(
                            text = it.key,
                            modifier = Modifier
                                .padding(6.dp)
                                .clickable(enabled = true, onClick = {
//                                    val intend = Intent(Intent.ACTION_VIEW, Uri.parse(it.key))
//                                    context.startActivity(intend)
                                    handler.openUri(it.key)
                                })
                            ,
                            fontSize = 18.sp,
                            fontFamily = FontFamily.Monospace,
                            textDecoration = TextDecoration.Underline
//                    inlineContent = InlineStyles(msg = msg),
//                    onTextLayout = {}
                        )
                    }
//                }
            }
            else if (it.value == textTypeParse.text) {
                Text(
                    text = annotateMessage(it.key),
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
    var showTime by remember {
        mutableStateOf(false)
    }


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
    val IntrSource = remember {
        MutableInteractionSource()
    }
    Row(
        modifier = Modifier
            .fillMaxWidth(1f),
        horizontalArrangement = Arrangement.End
    ) {
        Column {
            Row {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        //                .weight(5f)
                        .wrapContentWidth(Alignment.End)
                        .padding(top = 10.dp)
                        .padding(horizontal = 5.dp)
                        .clickable(
                            enabled = true,
                            onClick = { showTime = !showTime },
                            interactionSource = IntrSource,
                            indication = null
                        ),
                    //                .height(60.dp),
                    shape = RoundedCornerShape(20.dp, 0.dp, 20.dp, 20.dp),
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
//                            }
                            GetCodeMessage(msg = msg)
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
                if (message.status == messageStatus.Send) {
                    Card(
                        modifier = Modifier
                            .padding(top = 10.dp),
                        shape = RoundedCornerShape(50)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowOutward,
                            contentDescription = null,
                            modifier = Modifier
                                .background(color = MaterialTheme.colorScheme.primaryContainer)
                                .padding(1.dp)
//                                .clip(shape = RoundedCornerShape(50.dp))
                        )
                    }
                } else if (message.status == messageStatus.Sending) {
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
                    }
                } else if (message.status == messageStatus.Error) {
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
                    textAlign = TextAlign.End
                )
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
    var showTime by remember {
        mutableStateOf(false)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth(0.8f),
//            .fillMaxWidth()
    ) {
        Column {
            Card(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .padding(horizontal = 5.dp)
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
                shape = RoundedCornerShape(0.dp, 20.dp, 20.dp, 20.dp),
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
                    GetCodeMessage(msg = message.content)

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

@Composable
fun ReceiverGroupChat(
    message: MessageReceived
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
    Row(
        modifier = Modifier
            .fillMaxWidth(0.8f),
//            .fillMaxWidth()
    ) {
        Column {
            Text(
                modifier = Modifier
                    .padding(horizontal = 5.dp)
                    .padding(top = 10.dp),
                text = message.senderId
            )
            Card(
                modifier = Modifier
                    .padding(bottom = 0.dp)
                    .padding(horizontal = 5.dp)
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
                shape = RoundedCornerShape(0.dp, 20.dp, 20.dp, 20.dp),
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
                    GetCodeMessage(msg = message.content)

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

@Composable
fun BottomMessageSend(
    modifier: Modifier = Modifier,
    appUistate: MessagesUiState,
    sendMessage: () -> Unit,
    updateMessage: (String) -> Unit,
    sendAttatchment: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    ElevatedCard(
        modifier= modifier
            .navigationBarsPadding()
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

                    //                trailingIcon =
//                {
//
//                }

                )
//

                IconButton(
                    onClick = {
                        if (appUistate.messageToSend.isNotEmpty()) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            sendMessage()
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

@Preview(device = "id:pixel_9_pro", showSystemUi = true, showBackground = true)
@Composable
fun PreviewMessagebodySuccess() {
    ChitChatTheme(
        dynamicColor = false,
//        darkTheme = true
    ) {
        MessagesBodySuccess(
            uiState = MessagesUiState(
                chatName = "ThereSelf",
                currChat = ChatOrGroup(
                    isGroup = false,
                    chatName = "ThereSelf"
                ),
                currentUser = "ThereSelf",
                messages = mutableListOf(

                    MessageReceived(
                        content = "Ok Bye",
                        senderId = "ThereSelf",
                        timeStamp = Timestamp(Date(2024 - 1900, 2, 26, 20, 15))
                    ),
                    MessageReceived(
                        content = "Hello *Friend*",
                        senderId = "mySelf",
                        timeStamp = Timestamp(Date(2024 - 1900, 1, 25, 20, 15))
                    ),

                    MessageReceived(
                        content = "good to say ```fda and we ate goin to teshio thaio``` nlhasfihobphbkjwahf aw;jhflkb aweklfb ",
                        senderId = "mySelf",
                        timeStamp = Timestamp(Date(2024 - 1900, 2, 5, 20, 15))
                    ),
                    MessageReceived(
                        content = "<Bold> \n Here is a code block\n ```_Italic_ and ~Strikethrough~``` and now ! Underlined !" +
                                "<https://google.com>",
                        senderId = "ThereSelf",
                        timeStamp = Timestamp(Date(2024 - 1900, 10, 2, 7, 45))
                    ),
                    MessageReceived(
                        content = "Here is a code block: ```val x = 42``` and a link: <https://example.com>",
                        senderId = "Myself",
                        timeStamp = Timestamp(Date(2024 - 1900, 10, 2, 7, 45))
                    ),
                            MessageReceived(
                            content = "<https://example.com>",
                    senderId = "Myself",
                    timeStamp = Timestamp(Date(2024 - 1900, 10, 2, 7, 45))
                )
                ),
            ),
            updateMessage = {},
            getMessagesAgain = { },
            sendMessage = {},
            navigateUp = {}
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
