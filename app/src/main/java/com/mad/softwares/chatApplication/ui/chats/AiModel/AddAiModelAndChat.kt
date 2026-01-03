package com.mad.softwares.chatApplication.ui.chats.AiModel

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mad.softwares.chatApplication.R
import com.mad.softwares.chatApplication.data.models.OllamaModel
import com.mad.softwares.chatApplication.data.models.tags
import com.mad.softwares.chatApplication.ui.ApptopBar
import com.mad.softwares.chatApplication.ui.GodViewModelProvider
import com.mad.softwares.chatApplication.ui.LoadingIndicator
import com.mad.softwares.chatApplication.ui.destinationData
import com.mad.softwares.chatApplication.ui.theme.ChitChatTheme

object addAiModelAndChatDestination : destinationData{
    override val canBack = true
    override val route = "AddAiModel"
    override val title = R.string.add_ai_model
    val nestedGraph="AddAiModelNestedGraph"
}

@Composable
fun AiTagFromOllama(
    viewModel: AddAiModelAndChatViewModel = viewModel(factory = GodViewModelProvider.Factory),
    navigateWithReload : (Boolean) ->Unit,
    navigateUp: () -> Unit,
    navigateToNaming : ()->Unit,
){
    val uiState = viewModel.aiChatUiState.collectAsState().value
//    if(uiState.addChatSuccess){
//        navigateWithReload(false)
//    }
    AiTagBody(
        tagState = viewModel.aiTags.collectAsState().value,
        navigateUp= navigateUp,
        onChatButtonClick = {
            viewModel.setCurrentModel(it)
            navigateToNaming()
        },
        retryButton = viewModel::getTags
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiTagBody (
    tagState: TagsUiState,
    navigateUp:()->Unit,
    onChatButtonClick:(model: OllamaModel)->Unit,
    retryButton:()->Unit,
){
    Scaffold(
        topBar = { ApptopBar(
            destinationData = addAiModelAndChatDestination,
            navigateUp =navigateUp
        ) }
    ) { paddingValues ->

        LazyColumn(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 5.dp)
        ) {
            if(tagState.fetchStatus == AiModelStatus.Loading){
                item{ Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 50.dp),

                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                )
                { LoadingIndicator(isLoading = true) }
                }
            }
            else if (tagState.fetchStatus == AiModelStatus.Error){
                item{ Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 50.dp),

                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                )
                { Icon(
                    Icons.Default.ErrorOutline,
                    "error getting the data"
                )
                    Text("Unable to fetch models from ollama api or it no models are installed.",
                        textAlign = TextAlign.Center)
                    Button(onClick = retryButton) {
                        Text("Retry fetching")
                    }
                }
                }
            }
            else{
                items(tagState.tags.models) {
                    SingleModel(
                        model = it.model,
                        name = it.name,
                        onClick ={onChatButtonClick(it)}
                    )
                }
            }
        }
    }
}

@Composable
fun SingleModel(
    model:String,
    name:String,
    onClick: ()->Unit,
){
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            Text(name,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold)
            Text(model)
        }
    }
}

@Composable
@Preview
fun AiTagBodyPreview(){
    ChitChatTheme( ) {
        AiTagBody(
            tagState = TagsUiState(
                tags = tags(listOf(OllamaModel("Loading...","loaingd.."),
                    OllamaModel("Loading...","loaingd.."),
                    OllamaModel("Loading...","loaingd.."))),
                fetchStatus = AiModelStatus.Error
            ),
            navigateUp = {},
            onChatButtonClick = {},
            retryButton = {}
        )
    }
}