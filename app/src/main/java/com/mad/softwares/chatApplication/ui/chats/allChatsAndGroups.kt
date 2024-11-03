@file:OptIn(ExperimentalFoundationApi::class)

package com.mad.softwares.chatApplication.ui.chats

import android.Manifest
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Deselect
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
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
import com.google.accompanist.permissions.shouldShowRationale
import com.google.firebase.Timestamp
import com.mad.softwares.chatApplication.R
import com.mad.softwares.chatApplication.data.ChatOrGroup
import com.mad.softwares.chatApplication.data.User
import com.mad.softwares.chatApplication.data.lastMessage
import com.mad.softwares.chatApplication.ui.ApptopBar
import com.mad.softwares.chatApplication.ui.GodViewModelProvider
import com.mad.softwares.chatApplication.ui.destinationData
import com.mad.softwares.chatApplication.ui.theme.ChitChatTheme
import kotlinx.coroutines.CoroutineScope
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
    navigateToAddGroup: (String) -> Unit,
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
                rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS),
                addToSelection = viewModel::toggleChatOrGroup,
                setSelect = viewModel::setSelect,
                unSetSelect = viewModel::unSelect,
                selectAll = viewModel::selectAll,
                deSelectAll = viewModel::deSelectAll,
                deleteChats = viewModel::deleteChats
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

        CurrentChatStatus.NoOfflineKey->{
            Text(text = ("No key found on device try reinstalling"))
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
    permissionState: PermissionState?,
    addToSelection: (Boolean, ChatOrGroup) -> Unit,
    setSelect: () -> Unit,
    unSetSelect: () -> Unit,
    selectAll: () -> Unit,
    deSelectAll: () -> Unit,
    deleteChats: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val listState = rememberLazyListState()
    val expandedFab by remember {
        derivedStateOf { listState.firstVisibleItemIndex == 0 }
    }
    val pagerState = rememberPagerState(0, pageCount = { 2 })
    val titleAndIcon = listOf(
        "Chats" to Icons.Default.Person,
        "Groups" to Icons.Default.Groups
    )
    val scope = rememberCoroutineScope()
    var expandDDMenu by remember {
        mutableStateOf(false)
    }
    var dialogState by remember {
        mutableStateOf(false)
    }
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        floatingActionButton = {
            AddChatFab(
                expanded = expandedFab,
                navigateToAddChat = {
                    if (pagerState.currentPage == 0) navigateToAddChats() else {
                        navigateToAddGroup()
                    }
                },
                text = if (pagerState.currentPage == 0) "Add Chat" else "Add Group"
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        },
        topBar = {
            Column() {
                ApptopBar(
                    destinationData = chatsScreenDestination,
                    scrollBehavior = scrollBehavior,
                    navigateUp = { /*TODO*/ },
                    canGoBack = chatsUiState.selectStatus,
                    goBack = unSetSelect,
                    action = {
                        if (!chatsUiState.selectStatus) {
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
                        } else {
                            IconButton(onClick = { dialogState = true }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete"
                                )
                            }
                            if (chatsUiState.selectedAll) {
                                IconButton(onClick = deSelectAll) {
                                    Icon(
                                        imageVector = Icons.Default.Deselect,
                                        contentDescription = "SelectAll"
                                    )
                                }
                            } else {
                                IconButton(onClick = selectAll) {
                                    Icon(
                                        imageVector = Icons.Default.SelectAll,
                                        contentDescription = "SelectAll"
                                    )
                                }
                            }
                        }
                    }
                )
                PrimaryTabRow(
                    selectedTabIndex = pagerState.currentPage,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
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
//                            icon = { Icon(imageVector = icon, contentDescription = null) }
                        )
                    }

                }
                if (permissionState?.status?.isGranted == true) {
//                Text(text = "Notification permission granted")
                } else {
                    LaunchedEffect(key1 = Unit) {
                        permissionState?.launchPermissionRequest()
                    }
                    Row(modifier = Modifier
                        .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ){
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
                                text = "Notification disabled, enable them from setting",
                                fontSize = 20.sp,
                                lineHeight = 20.sp,
                                textAlign = TextAlign.Center
                            )

                        }
                    }
                }
            }
        },

        ) { paddingValues ->
        if (dialogState) {
            AlertDialog(
                onDismissRequest = { dialogState = false },
                confirmButton = {
                    Button(onClick = {
                        deleteChats()
                        dialogState = false
                        unSetSelect()
                    }) {
                        Text(text = "Delete")
                    }
                },
                title = {
                    Text(text = "Delete Selected Chats")
                },
                text = { Text(text = stringResource(R.string.delete_alert_message)) },
                dismissButton = {
                    TextButton(onClick = { dialogState = false }) { Text(text = "Cancel") }
                },
            )
        }
        HorizontalPager(state = pagerState) { page ->
            when (page) {
                0 -> ShowChatsSuccessful(
                    chatsUiState = chatsUiState,
                    paddingValues = paddingValues,
                    listState = listState,
                    navigateToCurrentChat = navigateToCurrentChat,
                    isCardEnabled = isCardEnabled,
                    setSelectionChats = setSelect,
                    addToSelection = addToSelection
                )

                1 -> ShowGroupsSuccessful(
                    chatsUiState = chatsUiState,
                    paddingValues = paddingValues,
                    listState = listState,
                    navigateToCurrentChat = navigateToCurrentChat,
                    isCardEnabled = isCardEnabled,
                    setSelectionChats = setSelect,
                    addToSelection = addToSelection
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
    isCardEnabled: Boolean,
    setSelectionChats: () -> Unit,
    addToSelection: (Boolean, ChatOrGroup) -> Unit
) {
    val haptic = LocalHapticFeedback.current
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
            if (!chatsUiState.selectStatus) {
                items(
                    items = chatsUiState.groups.sortedBy { it.lastMessage.timestamp }.reversed(),
                    key = { group -> group.chatId }
                ) {
                    SingleChat(
                        chat = it,
                        navigateToCurrentChat = navigateToCurrentChat,
                        isCardEnabled = isCardEnabled,
                        currentUsername = chatsUiState.currentUser.username,
                        setSelectionChats = setSelectionChats,
                        cardModifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    navigateToCurrentChat("${it.chatId},${chatsUiState.currentUser.username}")

                                },
                                onLongClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    setSelectionChats()
                                    addToSelection(true, it)
                                }
                            ),
                        isCardSelected = chatsUiState.selectedChatsOrGroups.contains(it)
//                    addToSelection = addToSelection,
//                    selectedStatus = chatsUiState.selectedChatsOrGroups.contains(it),
                    )

                }
            } else {
                items(chatsUiState.groups.sortedBy { it.lastMessage.timestamp }
                    .reversed()) { chat ->
                    SingleChat(
                        chat = chat,
                        navigateToCurrentChat = navigateToCurrentChat,
                        isCardEnabled = isCardEnabled,
                        currentUsername = chatsUiState.currentUser.username,
                        setSelectionChats = setSelectionChats,
                        isCardSelected = chatsUiState.selectedChatsOrGroups.contains(chat),
//                        isCardSelected = true,
                        cardModifier = Modifier
                            .fillMaxWidth()
                            .toggleable(

                                value = chatsUiState.selectedChatsOrGroups.contains(chat),
                                onValueChange = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    addToSelection(it, chat)
                                }
                            )

//                    addToSelection = addToSelection,
//                    selectedStatus = chatsUiState.selectedChatsOrGroups.contains(it)
                    )
                }
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
//        elevation = CardDefaults.elevatedCardElevation(3.dp),
        shape = RoundedCornerShape(0.dp),
//        enabled = isCardEnabled,
        elevation = CardDefaults.elevatedCardElevation(0.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
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
    isCardEnabled: Boolean,
    setSelectionChats: () -> Unit,
    addToSelection: (Boolean, ChatOrGroup) -> Unit
) {
    val haptic = LocalHapticFeedback.current
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
//                .padding(start = 5.dp, end = 5.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            if (!chatsUiState.selectStatus) {
                items(
                    items = chatsUiState.chats.sortedBy { it.lastMessage.timestamp }.reversed(),
                    key = {chat->chat.chatId}
                ) {
                    SingleChat(
                        chat = it,
                        navigateToCurrentChat = navigateToCurrentChat,
                        isCardEnabled = isCardEnabled,
                        currentUsername = chatsUiState.currentUser.username,
                        setSelectionChats = setSelectionChats,
                        cardModifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    navigateToCurrentChat("${it.chatId},${chatsUiState.currentUser.username}")
                                },
                                onLongClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    setSelectionChats()
                                    addToSelection(true, it)
                                }
                            ),
                        isCardSelected = chatsUiState.selectedChatsOrGroups.contains(it)
//                    addToSelection = addToSelection,
//                    selectedStatus = chatsUiState.selectedChatsOrGroups.contains(it),
                    )

                }
            } else {
                items(chatsUiState.chats.sortedBy { it.lastMessage.timestamp }.reversed()) { chat ->
                    SingleChat(
                        chat = chat,
                        navigateToCurrentChat = navigateToCurrentChat,
                        isCardEnabled = isCardEnabled,
                        currentUsername = chatsUiState.currentUser.username,
                        setSelectionChats = setSelectionChats,
                        isCardSelected = chatsUiState.selectedChatsOrGroups.contains(chat),
//                        isCardSelected = true,
                        cardModifier = Modifier
                            .fillMaxWidth()
                            .toggleable(
                                value = chatsUiState.selectedChatsOrGroups.contains(chat),
                                onValueChange = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    addToSelection(it, chat)
                                }
                            )

//                    addToSelection = addToSelection,
//                    selectedStatus = chatsUiState.selectedChatsOrGroups.contains(it)
                    )
                }
            }
        }
    }

}


@Composable
fun SingleChat(
    chat: ChatOrGroup,
    navigateToCurrentChat: (String) -> Unit,
    isCardEnabled: Boolean,
    currentUsername: String,
    setSelectionChats: () -> Unit,
    cardModifier: Modifier = Modifier,
    isCardSelected: Boolean
) {
    var expanded by remember {
        mutableStateOf(false)
    }
//    val lastMessageContent = chat.lastMessage.content.substring(20)
    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .combinedClickable(
//                onClick = {
//                    navigateToCurrentChat("${chat.chatId},${currentUsername}")
//                },
//                onLongClick = {
//                    setSelectionChats()
//                }
//            )
        modifier = cardModifier
//            .toggleable(value = selectedStatus,
//                onValueChange = {
//                    addToSelection(it, chat)
//                })

//            .border(width = 1.dp, color = MaterialTheme.colorScheme.onSurface, shape = RoundedCornerShape(0.dp))
        ,
//            .height(125.dp)
//            .padding(1.dp)
//            .padding(start = 8.dp, end = 8.dp)

//        onClick = {
////            navigateToCurrentChat("${chat.chatId},${currentUsername}")
//
//        },
        shape = RoundedCornerShape(0.dp),
//        enabled = isCardEnabled,
        elevation = CardDefaults.elevatedCardElevation(0.dp),
        colors = if (isCardSelected) {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        } else {
            CardDefaults.cardColors(
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        }
//                border = BorderStroke(5.dp,MaterialTheme.colorScheme.primary)

    )
    {
//        Switch(checked = selectedStatus, onCheckedChange = {
//            addToSelection(it, chat)
//        })
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
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
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
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    if (chat.lastMessage.sender == "") {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription =
                            stringResource(R.string.default_here)
                        )
                    } else if (chat.lastMessage.sender == currentUsername) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = stringResource(
                                R.string.send_message
                            )
                        )
                    } else if (chat.lastMessage.sender != currentUsername) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = stringResource(
                                R.string.receive_message
                            )
                        )
                    }


                    Text(
                        text = if (chat.lastMessage.timestamp == Timestamp(0, 0)) {
                            "Not yet contacted"
                        } else {
                            chat.lastMessage.content
                        },
                        fontSize = 18.sp,
                        lineHeight = 18.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
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


@Composable
fun AddChatFab(
    expanded: Boolean = false,
    navigateToAddChat: () -> Unit,
    text: String
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
@Preview(showSystemUi = false, showBackground = true, backgroundColor = 0xFFCA1C1C)
fun UserChatsPreview() {
    ChitChatTheme(
//        darkTheme = true
    ) {
        UserChatsBody(
            chatsUiState = ChatsUiState(
                selectStatus = true,
                currentUser = User(username = "mrg@123.com"),
                chats = listOf(
                    ChatOrGroup(
                        chatName = "mrg@123.com",
                        lastMessage = lastMessage(
                            content = "Big text here goed to test the message capacity and the other things",
                            timestamp = Timestamp(1, 1),
                            sender = "mrg@123.com"
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
                ),
                groups = listOf(
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
            permissionState = null,
            addToSelection = { _, _ -> },
            setSelect = {},
            unSetSelect = {},
            selectAll = {},
            deSelectAll = { },
            deleteChats = {}
        )
    }

}