package com.mad.softwares.chatApplication.ui.userInfoAndPreferences

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mad.softwares.chatApplication.R
import com.mad.softwares.chatApplication.data.User
import com.mad.softwares.chatApplication.ui.ApptopBar
import com.mad.softwares.chatApplication.ui.GodViewModelProvider
import com.mad.softwares.chatApplication.ui.destinationData
import com.mad.softwares.chatApplication.ui.theme.ChitChatTheme

object UserInfoAndPreferencesDestination: destinationData{
    override val route: String = "userInfoAndPref"
    override val title: Int = R.string.user_infos
    override val canBack: Boolean = true
}

@Composable
fun UserInfoAndPreference(
    viewModel: UserInfoViewModel = viewModel(factory = GodViewModelProvider.Factory),
    navigateUp:()-> Unit,
){
    val uiState = viewModel.infoUiState.collectAsState().value
    UserInfoAndPreferenceScreen(
        uiState = uiState,
        navigateUp,
        onAiUrlChange= viewModel::setAiUrl,
        setAiUrl = viewModel::setAiUrlOnPreference,
        aiPrefEndpoint = viewModel.aiEndpointFromPref.collectAsState().value
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserInfoAndPreferenceScreen(
    uiState: UserInfo,
    navigateUp:()->Unit,
    onAiUrlChange:(String)->Unit,
    setAiUrl:()->Unit,
    aiPrefEndpoint : String
){
    val currentUser = uiState.currentUser
    val scrollState = remember {
        ScrollState(0)
    }
    Scaffold(
        topBar = {
            ApptopBar(
                destinationData = UserInfoAndPreferencesDestination,
//                title = { Text("User Information",
//                    color = MaterialTheme.colorScheme.onPrimary,) },
//                scrollBehavior = TODO(),
                navigateUp = navigateUp,
//                action = TODO(),
//                modifier = TODO(),
//                canGoBack = TODO(),
//                goBack = TODO(),
            )
        }
    ) {
        paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)
            .verticalScroll(scrollState)
            .padding(8.dp),
        ) {
            Text("UserName : ${currentUser.username}",
                fontSize = 20.sp
                )
            Text("UID : ${currentUser.docId}",

                fontSize = 20.sp
            )
            Text("AI Endpoint Set : ${aiPrefEndpoint}",

                fontSize = 20.sp
            )
            OutlinedTextField(
                value = uiState.aiUrl,
                onValueChange = onAiUrlChange,
                label = {Text("AI Api URL")}
            )
            Button(onClick = setAiUrl) {
                Text("Update the AI End point")
            }
        }
    }
}

@Preview
@Composable
fun UserInfoAndPreferencesPreview(){
    ChitChatTheme(dynamicColor = false){ UserInfoAndPreferenceScreen(uiState = UserInfo(currentUser = User(
        username = "SomeUser@email.com",
        uniqueId = "Some id 1234svn;asdjn",
        docId = "aw;podif03122874ksn"
    )),
        navigateUp = {},
        onAiUrlChange = {},
        setAiUrl = {},
        aiPrefEndpoint = "123.84.993.dl:3345"
        )

    }
}