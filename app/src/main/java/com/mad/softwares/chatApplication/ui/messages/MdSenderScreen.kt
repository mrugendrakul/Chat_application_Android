package com.mad.softwares.chatApplication.ui.messages

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.layout.SupportingPaneScaffold
import androidx.compose.material3.adaptive.layout.SupportingPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.rememberPaneExpansionState
import androidx.compose.material3.adaptive.navigation.rememberSupportingPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mad.softwares.chatApplication.R
import com.mad.softwares.chatApplication.ui.ApptopBar
import com.mad.softwares.chatApplication.ui.destinationData
import com.mad.softwares.chatApplication.ui.theme.ChitChatTheme
import com.mikepenz.markdown.m3.Markdown
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object MdSenderScreenDestination : destinationData {
    override val route: String = "MdScreenViewer"
    override val title: Int = R.string.markdown_viewer
    override val canBack: Boolean = true

}

@Composable
fun MdSenderScreen(
    viewModel: MessagesViewModel,
    navigateUp: () -> Unit,
) {
    MdMainScreenSender(
        uiState = viewModel.messagesUiState.collectAsState().value,
        modifyMessage = viewModel::messageEdit,
        navigateUp = navigateUp,
        sendMessage = {
            viewModel.sendMdMessage()
            navigateUp()
        }
    )
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MdMainScreenSender(
    uiState: MessagesUiState,
    modifyMessage: (text: String) -> Unit,
    navigateUp: () -> Unit,
    sendMessage:()->Unit,
) {
    val paneNavigator = rememberSupportingPaneScaffoldNavigator()
    val scope = rememberCoroutineScope()
    val paneExpansionState = rememberPaneExpansionState()
    Scaffold(
        topBar = { ApptopBar(
            destinationData = MdSenderScreenDestination,
            navigateUp = navigateUp,
            action = {
                if(paneNavigator.scaffoldValue[SupportingPaneScaffoldRole.Supporting] == PaneAdaptedValue.Hidden ||
                    paneNavigator.scaffoldValue[SupportingPaneScaffoldRole.Main] == PaneAdaptedValue.Hidden){
                    IconButton(onClick = {
                        if (paneNavigator.currentDestination?.pane == SupportingPaneScaffoldRole.Main)
                            scope.launch { paneNavigator.navigateTo(SupportingPaneScaffoldRole.Supporting) }
                        else {
                            scope.launch { paneNavigator.navigateTo(SupportingPaneScaffoldRole.Main) }
                        }

                    }) {
                        Icon(
                            Icons.Default.Preview,
                            "Preview the text in Md",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = sendMessage) {
                Icon(
                    Icons.Default.Send,
                    "Send Message"
                )

            }
        },
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
            .navigationBarsPadding()
    )
    {
        SupportingPaneScaffold(
            modifier = Modifier
                .padding(it),
            directive = paneNavigator.scaffoldDirective.copy(
                maxHorizontalPartitions = 2,
                maxVerticalPartitions = 0
            ),
            value = paneNavigator.scaffoldValue,
            mainPane = {
                MdSenderText(
                    uiState = uiState,
                    modifyMessage = modifyMessage
                )
            },
            supportingPane = {
                MdSenderBody(
                    uiState = uiState
                )
            },
//        modifier = TODO(),
//        extraPane = TODO(),
        paneExpansionDragHandle = {
            val interactionSource = remember { MutableInteractionSource() }
            Box(
                modifier = Modifier
                    .paneExpansionDraggable(
                        state = paneExpansionState,
                        interactionSource = interactionSource,
                        minTouchTargetSize = 20.dp,
//                        semanticsProperties = TODO()
                    )
                    .rotate(90f)
            ) {
                Card(modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(50) // <--- MAKES IT ROUND (Capsule shape)
                    )
                    .height(12.dp)
                    .width(64.dp)
                    ,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(25.dp)
                ){}
            }
        },
        paneExpansionState = paneExpansionState
        )
    }
}

@Preview(device = "spec:width=1280dp,height=800dp,dpi=240")
@Composable
fun MdMainScreenPreview() {
    ChitChatTheme() {
        MdMainScreenSender(
            uiState = MessagesUiState(
                messageToSend = """
    # Hello there we are relly good...
    This is a simple markdown example with:

    - Bullet points
    - **Bold text**
    - *Italic text*

    [Check out this link](https://github.com/mikepenz/multiplatform-markdown-renderer)
     # Hello Markdown

    This is a simple markdown example with:

    - Bullet points
    - **Bold text**
    - *Italic text*

    [Check out this link](https://github.com/mikepenz/multiplatform-markdown-renderer)
     # Hello Markdown

    This is a simple markdown example with:

    - Bullet points
    - **Bold text**
    - *Italic text*

    [Check out this link](https://github.com/mikepenz/multiplatform-markdown-renderer)
     # Hello Markdown

    This is a simple markdown example with:

    - Bullet points
    - **Bold text**
    - *Italic text*

    [Check out this link](https://github.com/mikepenz/multiplatform-markdown-renderer)
     # Hello Markdown

    This is a simple markdown example with:

    - Bullet points
    - **Bold text**
    - *Italic text*

    [Check out this link](https://github.com/mikepenz/multiplatform-markdown-renderer)
     # Hello Markdown

    This is a simple markdown example with:

    - Bullet points
    - **Bold text**
    - *Italic text*

    [Check out this link](https://github.com/mikepenz/multiplatform-markdown-renderer)
     # Hello Markdown

    This is a simple markdown example with:

    - Bullet points
    - **Bold text**
    - *Italic text*

    [Check out this link](https://github.com/mikepenz/multiplatform-markdown-renderer)
                    
                """.trimIndent()
            ),
            modifyMessage = {},
            navigateUp = {},
            sendMessage = {}
        )
    }
}

@Composable
fun MdSenderText(
    uiState: MessagesUiState,
    modifyMessage: (text: String) -> Unit,
) {
    OutlinedTextField(
        modifier = Modifier
            .padding(5.dp)
            .fillMaxSize(),
        value = uiState.messageToSend,
        onValueChange = modifyMessage
    )
}

@Composable
@Preview
fun MdSenderTextPreview() {
    ChitChatTheme() {
        MdSenderText(
            uiState = MessagesUiState(
                messageToSend = "Some text to add in the preview"
            ),
            modifyMessage = {}
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MdSenderBody(
    uiState: MessagesUiState
) {
    var debounceValue by remember{ mutableStateOf("") }
    val scrollState = rememberScrollState()
    LaunchedEffect(uiState.messageToSend) {
        delay(400)
        debounceValue = uiState.messageToSend
    }
//    Scaffold(
//        topBar = { ApptopBar(
//            destinationData = MdSenderScreenDestination,
//            navigateUp = navigateUp
//        ) }
//    ) {
    Column(
        Modifier.verticalScroll(scrollState)
    )
    {
        Markdown(
            modifier = Modifier
//                    .padding(it)
                .padding(5.dp),
            content = debounceValue

        )
    }
//    }
}

@Preview
@Composable
fun MdScreenPreview() {
    ChitChatTheme() {
        MdSenderBody(
            uiState = MessagesUiState(
                messageToSend = """
    # Hello there we are relly good... 
    This is a simple markdown example with:

    - Bullet points
    - **Bold text**
    - *Italic text*

    [Check out this link](https://github.com/mikepenz/multiplatform-markdown-renderer)
     # Hello Markdown

    This is a simple markdown example with:

    - Bullet points
    - **Bold text**
    - *Italic text*

    [Check out this link](https://github.com/mikepenz/multiplatform-markdown-renderer)
     # Hello Markdown

    This is a simple markdown example with:

    - Bullet points
    - **Bold text**
    - *Italic text*

    [Check out this link](https://github.com/mikepenz/multiplatform-markdown-renderer)
     # Hello Markdown

    This is a simple markdown example with:

    - Bullet points
    - **Bold text**
    - *Italic text*

    [Check out this link](https://github.com/mikepenz/multiplatform-markdown-renderer)
     # Hello Markdown

    This is a simple markdown example with:

    - Bullet points
    - **Bold text**
    - *Italic text*

    [Check out this link](https://github.com/mikepenz/multiplatform-markdown-renderer)
     # Hello Markdown

    This is a simple markdown example with:

    - Bullet points
    - **Bold text**
    - *Italic text*

    [Check out this link](https://github.com/mikepenz/multiplatform-markdown-renderer)
     # Hello Markdown

    This is a simple markdown example with:

    - Bullet points
    - **Bold text**
    - *Italic text*

    [Check out this link](https://github.com/mikepenz/multiplatform-markdown-renderer)
                """.trimIndent()
            )
        )
    }
}