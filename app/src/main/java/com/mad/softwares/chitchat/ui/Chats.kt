package com.mad.softwares.chitchat.ui

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.PullRefreshState
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mad.softwares.chitchat.data.Chats
import com.mad.softwares.chitchat.data.User
import com.mad.softwares.chitchat.data.uiState
import com.mad.softwares.chitchat.ui.theme.ChitChatTheme

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AllChats(
    backHandler: () -> Unit,
    appUiState: uiState,
    user: User,
    addChat: () -> Unit,
    refreshState: PullRefreshState,
    reloadChats:()->Unit,
    currentChat: (Chats) -> Unit,
){
    Log.d(TAG,"Naviaged to all chats")
    Box(modifier = Modifier
        .pullRefresh(refreshState)
        .padding(horizontal = 10.dp)){
        when (appUiState.isMyChatsLoading) {
            isChatsLoading.Success -> {
                AllChatsSuccess(
                    backHandler = backHandler,
                    appUiState = appUiState,
                    user = user,
                    addChat = addChat,
                    refreshState = refreshState,
                    currentChat = currentChat,

                )
            }

            isChatsLoading.Loading -> {
                AllChatsLoading(
                    backHandler = backHandler,
                    appUiState = appUiState,
                    user = user,
                    addChat = addChat,
                    refreshState = refreshState,
//                    currentChat = currentChat
                )
            }

            isChatsLoading.Failed -> {
                AllChatsFailed(reloadChats = reloadChats)
            }
        }
        PullRefreshIndicator(
            refreshing = appUiState.isRefreshing,
            state = refreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            backgroundColor = MaterialTheme.colorScheme.inverseOnSurface,
            contentColor = MaterialTheme.colorScheme.onBackground
        )
    }

//    AllChatsSuccess(
//        backHandler = backHandler,
//        appUiState = appUiState,
//        user = user,
//        addChat = addChat,
//        refreshState = refreshState,
//        currentChat = currentChat
//    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AllChatsSuccess(
    backHandler:()->Unit,
    appUiState:uiState,
    user: User,
    addChat:()->Unit,
    refreshState: PullRefreshState,
    currentChat: (Chats) -> Unit,
){
    var isCardEnabled by remember{
        mutableStateOf(true)
    }
    Scaffold(
        topBar = {},
        floatingActionButton = {
            FloatingActionButton(onClick = { addChat()}) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    contentDescription = "")
            }
        }
    ){
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .pullRefresh(refreshState)
        ){
//            Column{
//                Text(text = "Here all The chats wiil be displayed")
//                Text(text = "Current userId is : ${appUiState.myUserData.username}")
//            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
//                    .padding(bottom = 100.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ){
                items(appUiState.allAvailableChats){chat->
                    Chat(chat = chat,
                        currentChat = { currentChat(it)
                            isCardEnabled = !isCardEnabled},

                        isCardEnabled = isCardEnabled
                    )
                }
            }


        }
    }
    
    val context = LocalContext.current
    BackHandler {
        backHandler()
        onBackPressed(context)
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AllChatsLoading(
    backHandler:()->Unit,
    appUiState:uiState,
    user: User,
    addChat:()->Unit,
    refreshState: PullRefreshState,
//    currentChat: (String) -> Unit
){
    Scaffold(
        topBar = {},
//        floatingActionButton = {
//            FloatingActionButton(onClick = { addChat()}) {
//                Icon(
//                    imageVector = Icons.Rounded.Add,
//                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
//                    contentDescription = "")
//            }
//        }
    ){
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
//                .pullRefresh(state = refreshState)
        ){
//            Column{
//                Text(text = "Here all The chats wiil be displayed")
//                Text(text = "Current userId is : ${appUiState.myUserData.username}")
//            }
            LazyColumn(
                modifier = Modifier,
//                    .padding(bottom = 100.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ){
                items(8){chat->
                    ChatLoading()
                }
            }
//            PullRefreshIndicator(
//                refreshing = appUiState.isRefreshing,
//                state = refreshState,
//                modifier = Modifier.align(Alignment.TopCenter),
//                backgroundColor = MaterialTheme.colorScheme.inverseOnSurface,
//                contentColor = MaterialTheme.colorScheme.onBackground
//            )

        }
    }

    val context = LocalContext.current
    BackHandler {
        backHandler()
        onBackPressed(context)
    }
}

@Composable
fun ChatLoading(){
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .padding(5.dp)
//            .clickable(onClick = { currentChat(chat.chatId) }),
//        border = BorderStroke(5.dp,MaterialTheme.colorScheme.primary)
//        onClick = currentChat(chat.chatId)
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.onSecondary
                )
            )
             {}
            Spacer(Modifier.height(15.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.onSecondary
                )
            ) {}
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Chat(
    chat: Chats,
    currentChat:(Chats)->Unit,

    isCardEnabled:Boolean
){
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .padding(5.dp),
        onClick = {
            currentChat(chat)

        },
        enabled = isCardEnabled
//        border = BorderStroke(5.dp,MaterialTheme.colorScheme.primary)
//        onClick = currentChat(chat.chatId)
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            Text(
                text = chat.chatName,
                fontSize = 25.sp
            )
            Spacer(Modifier.height(15.dp))
            Text(
                text = "Chat id: ${chat.chatId}",
                fontSize = 25.sp
            )
        }
    }
}

@Composable
fun AllChatsFailed(
    reloadChats: () -> Unit
){
    Column {
        Text(text = "Unable to load the chats please refresh this page")
        Button(
            onClick = reloadChats
        ){
            Text(text = "Reload Chats")
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_NO or Configuration.UI_MODE_TYPE_NORMAL,
    device = "spec:width=1080px,height=2340px,dpi=440,isRound=true,chinSize=10px",
    wallpaper = Wallpapers.GREEN_DOMINATED_EXAMPLE
)
@Composable
fun PreviewAllChats(){
    ChitChatTheme(
        dynamicColor = false
    ) {
        AllChats(
            backHandler = {},
            appUiState = uiState(
                allAvailableChats = listOf(
                    Chats(chatName = "friend1"),
                    Chats(chatName = "friend1"),
                    Chats(chatName = "friend1"),
                    Chats(chatName = "friend1"),
                    Chats(chatName = "friend1"),
                    Chats(chatName = "friend1"),
                    Chats(chatName = "friend1"),
                    Chats(chatName = "friend1"),
                    Chats(chatName = "friend1"),
                    Chats(chatName = "friend1"),
                    Chats(chatName = "friend1"),

                ),
                isMyChatsLoading = isChatsLoading.Success
            ),
            user = User(),
            addChat = {},
            refreshState = rememberPullRefreshState(refreshing = false, onRefresh = { /*TODO*/ }),
            currentChat = {},
            reloadChats = {},

            )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
fun PreviewAChat(){
    ChitChatTheme {
        Chat(
            chat = Chats("","MyFriend"),
            currentChat = {},
            isCardEnabled = false
        )
    }
}

fun onBackPressed(context: Context){
    (context as? Activity)?.finishAffinity()
//    System.exit(0)
}