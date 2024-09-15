package com.mad.softwares.chatApplication.ui.chats

import android.Manifest
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.firebase.Timestamp
import com.mad.softwares.chatApplication.R
import com.mad.softwares.chatApplication.data.ChatOrGroup
import com.mad.softwares.chatApplication.data.User
import com.mad.softwares.chatApplication.data.lastMessage
import com.mad.softwares.chatApplication.ui.ApptopBar
import com.mad.softwares.chatApplication.ui.GodViewModelProvider
import com.mad.softwares.chatApplication.ui.destinationData
import com.mad.softwares.chatApplication.ui.theme.ChitChatTheme
import kotlinx.coroutines.launch

object chatsScreenDestination : destinationData {
    override val route = "chats"
    override val title = R.string.chats
    override val canBack = false
    val toReloadChats = "reloadStatus"
    val routeWithReload = "$route/{$toReloadChats}"
}

@OptIn(ExperimentalPermissionsApi::class)
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun AllChatsAndGroups(
    viewModel: ChatsViewModel = viewModel(factory = GodViewModelProvider.Factory),
//    viewModel: ChatsViewModel,
    navitageToAddChats: (List<String>) -> Unit,
    navigateToAddGroup:(String)->Unit,
    navigateToWelcome: () -> Unit,
    navigateToCurrentChat: (String) -> Unit
) {


    val chatsUiState = viewModel.chatsUiState.collectAsState().value
    when (chatsUiState.currentChatStatus) {
        CurrentChatStatus.Success -> {
            UserChatsBody(
                chatsUiState = chatsUiState,
                navigateToCurrentChat = navigateToCurrentChat,
                isCardEnabled = true,
                navigateToAddChats = { navitageToAddChats(viewModel.getMembers()) },
                navigateToAddGroup = { navigateToAddGroup(chatsUiState.currentUser.username) },
                logOut = { viewModel.logoutUser() },
                permissionState =
            rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS)
            )
        }

        CurrentChatStatus.Loading -> {
            ShowChatsLoading()
        }

        CurrentChatStatus.Failed -> {
            ShowErrorAndRetry {
                viewModel.getChats(isForced = true)
            }
        }

        CurrentChatStatus.Logouted -> {
            viewModel.resetUserInside()
            navigateToWelcome()
        }
    }
}


@Preview
@Composable
fun ShowErrorChatsPreview() {
    ShowErrorAndRetry(retry = {})
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun UserChatsBody(
    chatsUiState: ChatsUiState,
    navigateToCurrentChat: (String) -> Unit,
    isCardEnabled: Boolean,
    navigateToAddChats: () -> Unit,
    navigateToAddGroup: () -> Unit,
    logOut: () -> Unit,
    permissionState :PermissionState?
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val listState = rememberLazyListState()
    val expandedFab by remember {
        derivedStateOf { listState.firstVisibleItemIndex == 0 }
    }
    val pagerState = rememberPagerState (0, pageCount = { 2 })
    val titleAndIcon = listOf(
        "Singles" to Icons.Default.Person,
        "Groups" to Icons.Default.Groups
    )
    val scope = rememberCoroutineScope()
    var expandDDMenu by remember {
        mutableStateOf(false)
    }
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        floatingActionButton = {
            AddChatFab(
                expanded = expandedFab,
                navigateToAddChat = { if(pagerState.currentPage==0) navigateToAddChats() else {
                    navigateToAddGroup()
                }
                                    },
                text = if(pagerState.currentPage==0) "Add Chat" else "Add Group"
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        },
        topBar = {
            Column(){
                ApptopBar(
                    destinationData = chatsScreenDestination,
                    scrollBehavior = scrollBehavior,
                    navigateUp = { /*TODO*/ },
                    action = {
                        IconButton(onClick = { expandDDMenu = !expandDDMenu }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More"
                            )
                        }
                        DropdownMenu(
                            expanded = expandDDMenu,
                            onDismissRequest = { expandDDMenu = !expandDDMenu }) {
                            DropdownMenuItem(
                                text = { Text(text = chatsUiState.currentUser.username) },
                                onClick = { /*TODO*/ }
                            )
                            DropdownMenuItem(
                                text = { Text(text = "Logout from here") },
                                onClick = logOut
                            )

                        }
                    }
                )
                PrimaryTabRow(
                    selectedTabIndex = pagerState.currentPage,
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    titleAndIcon.forEachIndexed { index, (text, icon) ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            text = { Text(text = text) },
                            icon = { Icon(imageVector = icon, contentDescription = null) }
                        )
                    }

                }
                LaunchedEffect(key1 = Unit) {
                    permissionState?.launchPermissionRequest()
                }
                if (permissionState?.status?.isGranted == true) {
//                Text(text = "Notification permission granted")
                } else {
                    Card(
                        modifier = Modifier
                            .padding(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            modifier = Modifier
                                .padding(10.dp),
                            text = "Notification permission missing, grant them from setting",
                            fontSize = 20.sp,
                            lineHeight = 20.sp,
                            textAlign = TextAlign.Center
                        )

                    }
                }
            }
        },

        ) { paddingValues ->
        HorizontalPager(state = pagerState){page->
            when(page){
                0->ShowChatsSuccessful(
                    chatsUiState = chatsUiState,
                    paddingValues = paddingValues,
                    listState = listState,
                    navigateToCurrentChat = navigateToCurrentChat,
                    isCardEnabled = isCardEnabled
                )
                1-> ShowGroupsSuccessful(
                    chatsUiState = chatsUiState,
                    paddingValues = paddingValues,
                    listState = listState,
                    navigateToCurrentChat = navigateToCurrentChat,
                    isCardEnabled = isCardEnabled
                )
            }
        }
    }

}

@Composable
fun ShowGroupsSuccessful(
    chatsUiState: ChatsUiState,
    paddingValues: PaddingValues,
    listState: LazyListState,
    navigateToCurrentChat: (String) -> Unit,
    isCardEnabled: Boolean
){
    if (chatsUiState.groups.isEmpty()) {
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "No Groups yet press below icon to start creating groups",
                fontSize = 20.sp,
                textAlign = TextAlign.Center
            )
        }
    } else {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            items(chatsUiState.groups.sortedBy { it.lastMessage.timestamp }.reversed()) {
                SingleChat(
                    chat = it,
                    navigateToCurrentChat = navigateToCurrentChat,
                    isCardEnabled = isCardEnabled,
                    currentUsername = chatsUiState.currentUser.username
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowChatsLoading(

) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            ApptopBar(
                destinationData = chatsScreenDestination,
//                scrollBehavior = scrollBehavior,
                navigateUp = { /*TODO*/ }
            )
        },
//        floatingActionButton = {
//            FloatingActionButton(onClick = { addChat()}) {
//                Icon(
//                    imageVector = Icons.Rounded.Add,
//                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
//                    contentDescription = "")
//            }
//        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
//                .pullRefresh(state = refreshState)
        ) {
//            Column{
//                Text(text = "Here all The chats wiil be displayed")
//                Text(text = "Current userId is : ${appUiState.myUserData.username}")
//            }
            LazyColumn(
                modifier = Modifier,
//                    .padding(bottom = 100.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(8) { chat ->
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
}

@Composable
fun ChatLoading() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .padding(5.dp),
        elevation = CardDefaults.elevatedCardElevation(3.dp)
//            .clickable(onClick = { currentChat(chat.chatId) }),
//        border = BorderStroke(5.dp,MaterialTheme.colorScheme.primary)
//        onClick = currentChat(chat.chatId)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.onSecondary
                )
            )
            {}
            Spacer(Modifier.height(15.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.onSecondary
                )
            ) {}
        }
    }
}

@Preview
@Composable
fun ChatLoadingPreview() {
    ShowChatsLoading()
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ShowChatsSuccessful(
    chatsUiState: ChatsUiState,
    paddingValues: PaddingValues,
    listState: LazyListState,
    navigateToCurrentChat: (String) -> Unit,
    isCardEnabled: Boolean
) {
    if (chatsUiState.chats.isEmpty()) {
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "No chats yet press below icon to start new chats",
                fontSize = 20.sp,
                textAlign = TextAlign.Center
            )
        }
    } else {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .padding(paddingValues)
                .padding(start = 5.dp, end = 5.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            items(chatsUiState.chats.sortedBy { it.lastMessage.timestamp }.reversed()) {
                SingleChat(
                    chat = it,
                    navigateToCurrentChat = navigateToCurrentChat,
                    isCardEnabled = isCardEnabled,
                    currentUsername = chatsUiState.currentUser.username
                )
            }
        }
    }

}

@Composable
fun SingleChat(
    chat: ChatOrGroup,
    navigateToCurrentChat: (String) -> Unit,
    isCardEnabled: Boolean,
    currentUsername:String
) {
    var expanded by remember {
        mutableStateOf(false)
    }
//    val lastMessageContent = chat.lastMessage.content.substring(20)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(125.dp)
            .padding(6.dp)
//            .padding(start = 8.dp, end = 8.dp)
        ,
        onClick = {
            navigateToCurrentChat("${chat.chatId},${currentUsername}")

        },
        enabled = isCardEnabled,
        elevation = CardDefaults.elevatedCardElevation(3.dp)
        //        border = BorderStroke(5.dp,MaterialTheme.colorScheme.primary)

    )
    {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Image(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier
                    .size(50.dp)
            )
//            Spacer(modifier = Modifier.weight(1f))
            Column(
                modifier = Modifier
                    .padding(10.dp)
            ) {
                Text(
                    text = chat.chatName,
                    fontSize = 22.sp,
                    lineHeight = 22.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(15.dp))
                Text(
                    text = if (chat.lastMessage.timestamp == Timestamp(0, 0)) {
                        "Not yet contacted"
                    } else {
                        "-> ${chat.lastMessage.content}"
                    },
                    fontSize = 18.sp,
                    lineHeight = 18.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
//            Spacer(modifier = Modifier.weight(1f))
//            Box(
//                modifier = Modifier
////                .fillMaxWidth()
//                    .wrapContentSize(Alignment.BottomEnd)
//            ) {
//                IconButton(onClick = { expanded = !expanded }) {
//                    Icon(
//                        imageVector = Icons.Default.MoreVert,
//                        contentDescription = "Delete Chat"
//                    )
//                }
//                DropdownMenu(
//                    expanded = expanded,
//                    onDismissRequest = { expanded = false },
//                )
//                {
////                    DropdownMenuItem(
////                        text = { Text(text = "Delete chat") },
////                        onClick = { chatDelete() })
//                }
//            }
        }
    }
}


@Composable
fun AddChatFab(
    expanded: Boolean = false,
    navigateToAddChat: () -> Unit,
    text:String
) {
    ExtendedFloatingActionButton(
        onClick = { navigateToAddChat() },
        icon = {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = stringResource(id = R.string.add_chats)
            )
        },
        text = { Text(text = text) },
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        expanded = expanded
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
@Preview(showSystemUi = false, showBackground = false)
fun UserChatsPreview() {
    ChitChatTheme {
        UserChatsBody(
            chatsUiState = ChatsUiState(
                currentUser = User(username = "mrg@123.com"),
                chats = listOf(
                    ChatOrGroup(
                        chatName = "mrg@123.com",
                        lastMessage = lastMessage(
                            content = "Big text here goed to test the message capacity and the other things",
                            timestamp = Timestamp(1, 1)
                        )
                    ),
                    ChatOrGroup(
                        chatName = "mew@test.com"
                    ),
                    ChatOrGroup(
                        chatName = "mrg@123.com"
                    ),
                    ChatOrGroup(
                        chatName = "mew@test.com"
                    ),
                    ChatOrGroup(
                        chatName = "mrg@123.com"
                    ),
                    ChatOrGroup(
                        chatName = "mew@test.com"
                    ),
                    ChatOrGroup(
                        chatName = "mrg@123.com"
                    ),
                    ChatOrGroup(
                        chatName = "mew@test.com"
                    ),
                    ChatOrGroup(
                        chatName = "mrg@123.com"
                    ),
                    ChatOrGroup(
                        chatName = "mew@test.com"
                    ),
                    ChatOrGroup(
                        chatName = "mrg@123.com"
                    ),
                    ChatOrGroup(
                        chatName = "mew@test.com"
                    ),
                    ChatOrGroup(
                        chatName = "mrg@123.com"
                    ),
                    ChatOrGroup(
                        chatName = "mew@test.com"
                    ),
                    ChatOrGroup(
                        chatName = "mrg@123.com"
                    ),
                    ChatOrGroup(
                        chatName = "mew@test.com"
                    ),
                    ChatOrGroup(
                        chatName = "mrg@123.com"
                    ),
                    ChatOrGroup(
                        chatName = "mew@test.com"
                    ),
                    ChatOrGroup(
                        chatName = "mrg@123.com"
                    ),
                    ChatOrGroup(
                        chatName = "mew@test.com"
                    ),
                    ChatOrGroup(
                        chatName = "mrg@123.com"
                    ),
                    ChatOrGroup(
                        chatName = "mew@test.com"
                    ),
                )
            ),
            navigateToCurrentChat = {},
            isCardEnabled = true,
            navigateToAddChats = {},
            navigateToAddGroup = {},
            logOut = {},
            permissionState = null
        )
    }

}