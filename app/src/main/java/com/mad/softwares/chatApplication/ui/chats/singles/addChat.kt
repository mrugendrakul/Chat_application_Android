package com.mad.softwares.chatApplication.ui.chats.singles

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mad.softwares.chatApplication.R
import com.mad.softwares.chatApplication.data.chatUser
import com.mad.softwares.chatApplication.ui.ApptopBar
import com.mad.softwares.chatApplication.ui.GodViewModelProvider
import com.mad.softwares.chatApplication.ui.LoadingIndicator
import com.mad.softwares.chatApplication.ui.chats.ShowErrorAndRetry
import com.mad.softwares.chatApplication.ui.destinationData
import com.mad.softwares.chatApplication.ui.theme.ChitChatTheme

object addChatDestination : destinationData {
    override val route = "AddChat"
    override val title = R.string.add_chats
    override val canBack = true

    //    val members = listOf<String>("")
//    val memberString = members?.joinToString(",") ?: ""
//
//    //    val routeWithArgs = "$route/${memberString}"
    fun routeWithArgs(members: List<String>): String {
        val membersString = members.joinToString(",")
        return "$route/$membersString"
    }
}

@Composable
fun AddChat(
    navigateUp: () -> Unit,
    viewModel: AddChatViewModel = viewModel(factory = GodViewModelProvider.Factory),
    navigateWithReload: (Boolean) -> Unit,
//    viewModel: ChatsViewModel
//    jugadViewModel:chatsViewModel = viewModel(factory = GodViewModelProvider.Factory)
) {
    val uiState = viewModel.addChatUiState.collectAsState().value
    if (uiState.addChatSuccess) {
//        jugadViewModel.getChats(true)
//        viewModel.getChats(isForced = true)
//        navigateUp()
//        viewModel.resetAddChatSuccess()
        navigateWithReload(false)

    }
    AddChatBody(
        navigateUp = navigateUp,
        addChatUiState = uiState,
        addSingleMember = { viewModel.addSingleChat(it) },
        searchChange = { viewModel.onSearchQuery(it) },
        searchUser = { viewModel.getSearchMembers() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddChatBody(
    navigateUp: () -> Unit,
    addChatUiState: AddChatUiState,
    addSingleMember: (String) -> Unit,
    searchChange: (String) -> Unit,
    searchUser: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    var expandedSearchBar by rememberSaveable { mutableStateOf(false) }
    Scaffold(
        topBar = {
            Column {
                ApptopBar(destinationData = addChatDestination, scrollBehavior = scrollBehavior,
                    navigateUp = { navigateUp() })

                SearchBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp),
                    inputField = {
                        SearchBarDefaults.InputField(
                            query = addChatUiState.searchQuery,
                            onQueryChange = { searchChange(it) },
                            onSearch = {
                                searchUser()
                                expandedSearchBar = false
                                       },
                            expanded = expandedSearchBar,
                            onExpandedChange = { expandedSearchBar = it},
                            placeholder = { Text(text = "Search User...") },
                            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null)}
                        )
                    },
                    expanded = expandedSearchBar,
                    onExpandedChange = {expandedSearchBar = it},
                ){
                    Text(modifier = Modifier
                        .fillMaxWidth(),
                        text = "No sugesstion available, Will be implemented in future updates",
                        textAlign = TextAlign.Center
                    )
                }

//                ElevatedCard(
//                    Modifier
//                        .fillMaxWidth()
//                        .padding(8.dp)
////                        .padding(bottom = 0.dp)
//                        .background(MaterialTheme.colorScheme.background),
//                    colors = CardDefaults.elevatedCardColors(
//                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
//
//                        ),
//                    shape = RoundedCornerShape(30.dp),
//                ) {
//                    Column(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                    )
//                    {
//                        Row(
//                            modifier = Modifier
//                                .fillMaxWidth(),
//                            verticalAlignment = Alignment.Bottom,
//
//                            ) {
//
//                            OutlinedTextField(
//                                modifier = Modifier
//                                    .weight(1f)
//                                    .fillMaxWidth()
//                                    .padding(4.dp)
//                                    .animateContentSize(
//                                        animationSpec = spring(
//                                            dampingRatio = Spring.DampingRatioNoBouncy,
//                                            stiffness = Spring.StiffnessLow,
//                                        )
//                                    ),
//                                value = addChatUiState.searchQuery,
//
//                                onValueChange = { searchChange(it) },
//                                textStyle = TextStyle.Default.copy(
//                                    fontSize = 20.sp,
//                                ),
//                                maxLines = 1,
//                                placeholder = { Text(text = "Search User...") },
////                    placeholder = {"message..."},
//                                colors = OutlinedTextFieldDefaults.colors(
//                                    focusedBorderColor = Color(0, 0, 0, alpha = 0),
//                                    unfocusedBorderColor = Color(0, 0, 0, alpha = 0),
//                                    disabledBorderColor = Color(0, 0, 0, alpha = 0),
//                                ),
//
//                                //                trailingIcon =
////                {
////
////                }
//
//                            )
////
//
//                            IconButton(
//                                onClick = {
//                                    searchUserForGroup()
//                                },
//
//                                modifier = Modifier
////                        .fillMaxWidth()
//                                    .padding(bottom = 10.dp, end = 10.dp)
//                                    .size(45.dp),
//                                colors = IconButtonDefaults.iconButtonColors(
//                                    containerColor = MaterialTheme.colorScheme.primary,
//                                    contentColor = MaterialTheme.colorScheme.onPrimary
//
//                                )
//                            ) {
//                                Icon(
//                                    imageVector = Icons.Default.Search,
//                                    contentDescription = stringResource(
//                                        R.string.search_user
//                                    )
//                                )
//                            }
//
//                        }
//                    }
//                }
            }
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) {
        if (addChatUiState.isLoading) {
            LoadingIndicator(isLoading = addChatUiState.isLoading)
        } else if (addChatUiState.isError) {
            ShowErrorAndRetry {
                searchChange("mrugen")
            }
        }else if(addChatUiState.searchQuery.length<=3 && addChatUiState.searchQuery.isEmpty()){
            Text(
                modifier = Modifier
                    .padding(it)
                    .padding(10.dp)
                    .fillMaxWidth()
                ,
                text = "Start typing letter more that 3 letters...",
                fontSize = 15.sp,
                textAlign = TextAlign.Center
            )
        }
        else if(addChatUiState.searchQuery.length>=3 && !addChatUiState.searched){
            Text(
                modifier = Modifier
                    .padding(it)
                    .padding(10.dp)
                    .fillMaxWidth()
                ,
                text = "Press the button to start search.",
                fontSize = 15.sp,
                textAlign = TextAlign.Center
            )
        }
        else if (addChatUiState.chatUsers.isEmpty()){
            Text(
                modifier = Modifier
                    .padding(it)
                    .padding(10.dp)
                    .fillMaxWidth(),
                text = "No User found try with different query"
            )
        }
        else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
            ) {
                items(addChatUiState.chatUsers) {
                    SingleChatPerson(
                        chatUser = it,
                        addSingleMember = addSingleMember,
                        isCardEnabled = true
                    )
                }
            }
        }

    }
}

@Composable
fun SingleChatPerson(
    chatUser: chatUser,
    addSingleMember: (String) -> Unit,
    isCardEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(5.dp),
        onClick = {
            addSingleMember(chatUser.username)
        },
        enabled = isCardEnabled
    ) {
        Row(
            modifier = modifier
                .fillMaxHeight()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = modifier
                    .size(40.dp),
                imageVector = Icons.Default.Person,
                contentDescription = ""
            )
            Text(
                modifier = modifier
                    .fillMaxWidth(),
                text = chatUser.username,
                fontSize = 25.sp,
                fontWeight = FontWeight(700),
                textAlign = TextAlign.Center
            )
        }

    }
}

@Preview
@Composable
fun AddChatPreview() {
    ChitChatTheme(darkTheme = false){
        AddChatBody(addChatUiState = AddChatUiState(
            chatUsers = listOf(
                chatUser(username = "Hello@123.com"),
                chatUser(username = "There@456.com"),
                chatUser(username = "getThis@790.com"),
                chatUser(username = "Hello@123.com"),
                chatUser(username = "There@456.com"),
                chatUser(username = "getThis@790.com"),
                chatUser(username = "Hello@123.com"),
                chatUser(username = "There@456.com"),
                chatUser(username = "getThis@790.com"),
                chatUser(username = "Hello@123.com"),
                chatUser(username = "There@456.com"),
                chatUser(username = "getThis@790.com"),
                chatUser(username = "Hello@123.com"),
                chatUser(username = "There@456.com"),
                chatUser(username = "getThis@790.com"),
                chatUser(username = "Hello@123.com"),
                chatUser(username = "There@456.com"),
                chatUser(username = "getThis@790.com"),
            ),
            searchQuery = "1234",
            searched = true
        ),
            navigateUp = {},
            addSingleMember = {},
            searchChange = {},
            searchUser = {}
        )
    }
}




