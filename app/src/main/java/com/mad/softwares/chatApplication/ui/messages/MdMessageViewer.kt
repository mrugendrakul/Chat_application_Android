package com.mad.softwares.chatApplication.ui.messages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.dp
import com.mad.softwares.chatApplication.R
import com.mad.softwares.chatApplication.data.MessageReceived
import com.mad.softwares.chatApplication.ui.ApptopBar
import com.mad.softwares.chatApplication.ui.destinationData
import com.mad.softwares.chatApplication.ui.theme.ChitChatTheme
import com.mikepenz.markdown.m3.Markdown

object MdMessageViewerDataDestination: destinationData{
    override val route: String = "MdMessageViewer"
    override val title: Int = R.string.md_message_viewer
    override val canBack: Boolean= true
}

@Composable
fun MdMessageViewer(
    viewModel: MessagesViewModel,
    navigateUp:()->Unit,
){
    MdMessageViewerBody(
        uiState = viewModel.messagesUiState.collectAsState().value,
        navigateUp = navigateUp
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MdMessageViewerBody(
    uiState: MessagesUiState,
    navigateUp: () -> Unit,
){
    val scrollState = rememberScrollState()
    Scaffold(
        topBar = {
            ApptopBar(
                destinationData = MdMessageViewerDataDestination,
                navigateUp = navigateUp
            )
        },
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primary),
    ) {
        Column(
            Modifier.verticalScroll(scrollState)
        )
        {
            SelectionContainer(){
                Markdown(
                    modifier = Modifier
                        .padding(it)
                        .padding(5.dp),
                    content = uiState.selectedMessageForNextScreen.content

                )
            }
        }
    }
}

@Composable
@Preview(wallpaper = Wallpapers.RED_DOMINATED_EXAMPLE, showBackground = true, showSystemUi = true,
    device = "spec:width=1080px,height=2340px,dpi=440,isRound=true"
)
fun MdMessageViewerPreview(){
    ChitChatTheme(
        darkTheme = true
    ) {
        MdMessageViewerBody(
            uiState = MessagesUiState(
                selectedMessageForNextScreen = MessageReceived(
                    content = """
                        # Text goes here..
                    """.trimIndent()
                )
            ),
            navigateUp = {}
        )
    }
}