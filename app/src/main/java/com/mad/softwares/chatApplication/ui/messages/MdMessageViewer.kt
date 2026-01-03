package com.mad.softwares.chatApplication.ui.messages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Scaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
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
        }
    ) {
        Column(
            Modifier.verticalScroll(scrollState)
        )
        {
            Markdown(
                modifier = Modifier
                    .padding(it)
                    .padding(5.dp),
                content = uiState.selectedMessageForNextScreen.content

            )
        }
    }
}

@Composable
@Preview
fun MdMessageViewerPreview(){
    ChitChatTheme() {
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