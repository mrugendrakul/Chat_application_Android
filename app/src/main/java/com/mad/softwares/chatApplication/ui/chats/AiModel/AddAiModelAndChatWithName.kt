package com.mad.softwares.chatApplication.ui.chats.AiModel

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mad.softwares.chatApplication.R
import com.mad.softwares.chatApplication.data.models.OllamaModel
import com.mad.softwares.chatApplication.ui.ApptopBar
import com.mad.softwares.chatApplication.ui.GodViewModelProvider
import com.mad.softwares.chatApplication.ui.destinationData
import com.mad.softwares.chatApplication.ui.theme.ChitChatTheme

object addAiModelAndChatWithName : destinationData {
    override val canBack: Boolean = true
    override val route: String = "AddAiModelAndChatWithName"
    override val title: Int = R.string.add_ai_model_with_name
}

@Composable
fun AddAiModelAndChatWithName(
    viewModel: AddAiModelAndChatViewModel = viewModel(factory = GodViewModelProvider.Factory),
    navigateWithReload:(Boolean)->Unit,
    navigateUp: () -> Unit
){
    val uiState = viewModel.aiChatUiState.collectAsState().value
    if(uiState.addChatSuccess){
        navigateWithReload(false)
    }
    AddAiWithNameBody(
        navigateUp = navigateUp,
        uiState = uiState,
        modifyName = viewModel::onChatNameChange,
        addChat = viewModel::addAiChat
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAiWithNameBody(
    navigateUp:()->Unit,
    uiState: AiModelChat,
    modifyName : (name:String)-> Unit,
    addChat:(name:String,model:String)->Unit,
){

    Scaffold(
        topBar = { ApptopBar(
            destinationData = addAiModelAndChatWithName,
            navigateUp = navigateUp
        ) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    addChat(uiState.chatName,uiState.selectedModel.name)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Accept the model name",
//                    modifier = TODO(),
//                    tint = TODO()
                )
            }
        },
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
            .navigationBarsPadding()
    ) {
        paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues = paddingValues)
                .fillMaxSize(),

        ){
            TextField(
                value = uiState.chatName,
                onValueChange = modifyName,
                placeholder = {Text("Give model name")},
                modifier = Modifier
                    .padding(top = 25.dp, bottom = 10.dp, start = 10.dp, end = 10.dp)
                    .fillMaxWidth()
            )
            Divider()

            Column(
                modifier = Modifier.
                padding(5.dp)
            ) {
                Text(
                    uiState.selectedModel.name,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    uiState.selectedModel.model
                )
            }
        }
    }
}

@Composable
@Preview
fun AddAiWithNamePreview(){
    ChitChatTheme() {
        AddAiWithNameBody(
            navigateUp = {},
            uiState = AiModelChat(
                selectedModel = OllamaModel(
                    "New modle",
                    "Chat gpt 3.4"
                )
            ),
            modifyName = {},
            addChat = {_,_-> Unit}
        )
    }
}