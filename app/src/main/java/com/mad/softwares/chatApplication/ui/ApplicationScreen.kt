package com.mad.softwares.chatApplication.ui

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.mad.softwares.chatApplication.R
import com.mad.softwares.chatApplication.ui.chats.AiModel.AddAiModelAndChatViewModel
import com.mad.softwares.chatApplication.ui.chats.AiModel.AddAiModelAndChatWithName
import com.mad.softwares.chatApplication.ui.chats.AiModel.AiTagFromOllama
import com.mad.softwares.chatApplication.ui.chats.AiModel.addAiModelAndChatDestination
import com.mad.softwares.chatApplication.ui.chats.AiModel.addAiModelAndChatWithName
import com.mad.softwares.chatApplication.ui.chats.singles.AddChat
import com.mad.softwares.chatApplication.ui.chats.groups.AddGroup
import com.mad.softwares.chatApplication.ui.chats.AllChatsAndGroups
import com.mad.softwares.chatApplication.ui.chats.singles.addChatDestination
import com.mad.softwares.chatApplication.ui.chats.groups.addGroupDestination
import com.mad.softwares.chatApplication.ui.chats.chatsScreenDestination
import com.mad.softwares.chatApplication.ui.chats.groups.AddGroupViewModel
import com.mad.softwares.chatApplication.ui.chats.groups.AddGroupWithName
import com.mad.softwares.chatApplication.ui.chats.groups.addGroupWithNameDestination
import com.mad.softwares.chatApplication.ui.messages.MdMessageViewer
import com.mad.softwares.chatApplication.ui.messages.MdMessageViewerDataDestination
import com.mad.softwares.chatApplication.ui.messages.MdSenderScreen
import com.mad.softwares.chatApplication.ui.messages.MdSenderScreenDestination
import com.mad.softwares.chatApplication.ui.messages.Messages
import com.mad.softwares.chatApplication.ui.messages.MessagesViewModel
import com.mad.softwares.chatApplication.ui.theme.ChitChatTheme
import com.mad.softwares.chatApplication.ui.welcome.LoginScreen
import com.mad.softwares.chatApplication.ui.welcome.SignUpScreen
import com.mad.softwares.chatApplication.ui.welcome.loginScreenDestination
import com.mad.softwares.chatApplication.ui.welcome.signupScreenDestination
import com.mad.softwares.chatApplication.ui.welcome.welcomeDestination
import com.mad.softwares.chatApplication.ui.welcome.WelcomeScreen
import com.mad.softwares.chatApplication.ui.messages.messagesdestinationData
import com.mad.softwares.chatApplication.ui.migration.MigrationScreen
import com.mad.softwares.chatApplication.ui.migration.migrationScreenDestination

val TAGnav = "navLog"

@SuppressLint("UnrememberedGetBackStackEntry")
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun ApplicationScreen(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier,

    ) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(loginScreenDestination.route) {
            LoginScreen(
                navigateUp = {
                    navController.popBackStack(
                        welcomeDestination.route,
                        inclusive = false
                    )
                },
                navigateToChats = {
//                    navController.popBackStack()
//                    navController.navigate(chatsScreenDestination.route) {
//                        popUpTo(0)
//                    }
                    navController.navigate("${chatsScreenDestination.route}/${false}") {
                        popUpTo(0)
                    }
                }
            )
        }
        composable(signupScreenDestination.route) {
            SignUpScreen(
                navigateUp = { navController.popBackStack(welcomeDestination.route, false) },
                navigateToChats = {
//                    navController.popBackStack()
//                    navController.navigate(chatsScreenDestination.route) {
//                        popUpTo(0)
//                    }
                    navController.navigate("${chatsScreenDestination.route}/${false}") {
                        popUpTo(0)
                    }
                }
            )
        }
        composable(welcomeDestination.route) {
            WelcomeScreen(
                navigateToLogin = {
                    navController.navigate(loginScreenDestination.route)
                },
                navigateToSignUp = {
                    navController.navigate(signupScreenDestination.route)
                }
            )
        }

        composable(
            route = chatsScreenDestination.routeWithReload,
            arguments = listOf(navArgument(chatsScreenDestination.toReloadChats) {
                type = NavType.BoolType
                defaultValue = false
            })
        ) {
//            Log.d(TAGnav,"chats : ${addChatDestination.routeWithArgs()}")
            AllChatsAndGroups(

                navitageToAddChats = {
                    Log.d(TAGnav, "chats : ${addChatDestination.routeWithArgs(it)}")
                    navController.navigate(addChatDestination.routeWithArgs(it))
                },
                navigateToWelcome = {
                    navController.navigate(welcomeDestination.route) {
                        popUpTo(0)
                    }
                },
                navigateToCurrentChat = {
                    navController.navigate("${messagesdestinationData.route}/${it}")
                },
                navigateToAddGroup = {
                    Log.d(TAGnav, addGroupDestination.routeWithArgs(it))
//                    navController.navigate(addGroupDestination.routeWithArgs(it))
                    navController.navigate("${addGroupDestination.route}/${it}")
                },
                navigateToMigration = {
                    navController.navigate(migrationScreenDestination.route)
                },
                navigateToAddAiModel = {
                    navController.navigate(addAiModelAndChatDestination.route)
                }
            )
        }

        navigation(
            startDestination = addAiModelAndChatDestination.route,
            route = addAiModelAndChatDestination.nestedGraph
        ) {
            composable(
                route = addAiModelAndChatDestination.route
            ) { backStackEntry->

                val parentEntry = remember {
                    navController.getBackStackEntry(addAiModelAndChatDestination.nestedGraph)
                }
                val addGroupViewModel = viewModel<AddAiModelAndChatViewModel>(
                    parentEntry,
                    factory = GodViewModelProvider.Factory
                )
                AiTagFromOllama(
                    viewModel =  addGroupViewModel,
                    navigateWithReload = {
                        navController.navigate("${chatsScreenDestination.route}/$it") {
                            popUpTo(0)
                        }
                    },
                    navigateUp = {
                        navController.popBackStack(
                            chatsScreenDestination.routeWithReload,
                            false
                        )
                    },
                    navigateToNaming = {
                        navController.navigate(addAiModelAndChatWithName.route)
                    }
                )
            }

            composable(
                route = addAiModelAndChatWithName.route
            ) {
                    backStackEntry->

                val parentEntry = remember {
                    navController.getBackStackEntry(addAiModelAndChatDestination.nestedGraph)
                }
                val addGroupViewModel = viewModel<AddAiModelAndChatViewModel>(
                    parentEntry,
                    factory = GodViewModelProvider.Factory
                )
                AddAiModelAndChatWithName(
                    viewModel = addGroupViewModel,
                    navigateWithReload = {
                        navController.navigate("${chatsScreenDestination.route}/$it") {
                            popUpTo(0)
                        }
                    },
                    navigateUp = {
                        navController.navigateUp()
                    }
                )
            }
        }

        composable(
            route = "${addChatDestination.route}/{members}",
            arguments = listOf(
                navArgument("members") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        )
        {
            AddChat(
                navigateUp = {
                    navController.popBackStack(
                        chatsScreenDestination.routeWithReload,
                        false
                    )
                },
                navigateWithReload = {
                    navController.navigate("${chatsScreenDestination.route}/$it") {
                        popUpTo(0)
                    }
                }
            )
        }

        navigation(
            startDestination = "${addGroupDestination.route}/{User}",
            route = addGroupDestination.nestedGraph
        ) {
            composable(
                route = "${addGroupDestination.route}/{User}",
                arguments = listOf(
                    navArgument("User") {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )
            ) { backStackEntry ->
                val parentEntry = remember {
                    navController.getBackStackEntry(addGroupDestination.nestedGraph)
                }
                val addGroupViewModel = viewModel<AddGroupViewModel>(
                    parentEntry,
                    factory = GodViewModelProvider.Factory
                )
                AddGroup(
                    navigateUp = {
                        navController.popBackStack(
                            chatsScreenDestination.routeWithReload,
                            false
                        )
                    },
                    navigateWithReload = {
                        navController.navigate("${chatsScreenDestination.route}/$it") {
                            popUpTo(0)
                        }
                    },
                    navigateToAddGroupWithName = {
                        navController.navigate(addGroupWithNameDestination.route)
                    },
                    viewModel = addGroupViewModel
                )
            }
            composable(
                route = addGroupWithNameDestination.route
            ) {
                val parentEntry = remember {
                    navController.getBackStackEntry(addGroupDestination.nestedGraph)
                }
                val addGroupViewModel = viewModel<AddGroupViewModel>(
                    parentEntry,
                    factory = GodViewModelProvider.Factory
                )
                AddGroupWithName(
                    viewModel = addGroupViewModel,
                    navigateUp = { navController.navigateUp() },
                    navigateToChats = {
                        navController.navigate("${chatsScreenDestination.route}/$it") {
                            popUpTo(0)
                        }
                    }
                )
            }

//            AddGroupNavGraph()
        }


//        composable(ChatsNavGraphDestinationData.route){
//            ChatsNavGraph(navController)+
//        }
        navigation(
            startDestination = messagesdestinationData.routeWithArgs,
            route = messagesdestinationData.nestedGraphMessages
        )
        {
            composable(
                route = messagesdestinationData.routeWithArgs,
                arguments = listOf(
                    navArgument(messagesdestinationData.chatIDAndUsername) {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )
            ) {
                val parentEntry = remember {
                    navController.getBackStackEntry(messagesdestinationData.nestedGraphMessages)
                }
                val messagesViewModel = viewModel<MessagesViewModel>(
                    parentEntry,
                    factory = GodViewModelProvider.Factory
                )
                Messages(
                    viewModel = messagesViewModel,
                    navigateToMdSending = {navController.navigate(MdSenderScreenDestination.route)},
                    navigateToMdPreview = {navController.navigate(MdMessageViewerDataDestination.route)},
                    navigateUp = {navController.navigateUp()}
                )
            }

            composable(
                route = MdSenderScreenDestination.route

            ) {
                val parentEntry = remember {
                    navController.getBackStackEntry(messagesdestinationData.nestedGraphMessages)
                }
                val messagesViewModel = viewModel<MessagesViewModel>(
                    parentEntry,
                    factory = GodViewModelProvider.Factory
                )
                MdSenderScreen(
                    messagesViewModel,
                    navigateUp = {navController.navigateUp()}
                )
            }

            composable(
                route = MdMessageViewerDataDestination.route

            ) {
                val parentEntry = remember {
                    navController.getBackStackEntry(messagesdestinationData.nestedGraphMessages)
                }
                val messagesViewModel = viewModel<MessagesViewModel>(
                    parentEntry,
                    factory = GodViewModelProvider.Factory
                )
                MdMessageViewer(
                    messagesViewModel,
                    navigateUp = {navController.navigateUp()}
                )
            }


        }

        composable(
            route = migrationScreenDestination.route,

        ) {
            MigrationScreen(
                 getBack = { navController.navigateUp() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApptopBar(
    destinationData: destinationData,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    title: @Composable (() ->Unit) = {
        Text(
            text = stringResource(id = destinationData.title),
            color = MaterialTheme.colorScheme.onPrimary
        )
    },
    navigateUp: () -> Unit,
    action: @Composable (RowScope.() -> Unit) = {},
    modifier: Modifier = Modifier,
    canGoBack:Boolean =false,
    goBack:()->Unit = {}
) {
    TopAppBar(
//        title = {
//            if (title == "") {
//                Text(
//                    text = stringResource(id = destinationData.title),
////                color = MaterialTheme.colorScheme.onPrimary
//                )
//            } else {
//                Text(text = title)
//            }
//        },
        title = title,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
            scrolledContainerColor = MaterialTheme.colorScheme.primary
//            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        navigationIcon =
        {
            if(canGoBack){
                BackHandler (enabled = canGoBack){
                    goBack()
                }
                IconButton(onClick = goBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "back",
                        tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
            else if (destinationData.canBack) {

                IconButton(onClick = { navigateUp() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "back",
//                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }

            }


        },
        actions = action,
        scrollBehavior = scrollBehavior,

        )
}


object welcomeDestinationTest : destinationData {
    override val route = "welcome"
    override val title = R.string.welcome
    override val canBack = true
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun ApptopBarPreview() {
    ChitChatTheme(dynamicColor = false) {
        ApptopBar(
            welcomeDestinationTest,
            scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(),
            navigateUp = {},
            title = {Text("Title testing")},
//            canGoBack = true
        )
    }
}

interface destinationData {
    val route: String
    val title: Int
    val canBack: Boolean
}
