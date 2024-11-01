package com.mad.softwares.chatApplication

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.mad.softwares.chatApplication.ui.ApplicationScreen
import com.mad.softwares.chatApplication.ui.chats.chatsScreenDestination
import com.mad.softwares.chatApplication.ui.theme.ChitChatTheme
import com.mad.softwares.chatApplication.ui.welcome.welcomeDestination

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChitChatTheme(
                dynamicColor = false
            ) {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
//                    val appViewModel:chitChatViewModel = viewModel(factory = chitChatViewModel.Factory)
//                    val appUiState = appViewModel.extUiState.collectAsState().value
//                   ApplicationScreen(
//                       navController = rememberNavController(),
//                       modifier = Modifier.fillMaxSize()
////                       appUiState = appUiState,
////                       appViewModel = appViewModel
//                   )
                }
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onStart() {
        super.onStart()
        val currUser = Firebase.auth.currentUser

        if (currUser != null) {
            setContent {
                ChitChatTheme(
                    dynamicColor = true
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    )
                    {
                        ApplicationScreen(
                            navController = rememberNavController(),
                            modifier = Modifier.fillMaxSize(),
                            startDestination = chatsScreenDestination.routeWithReload
                        )
                    }
                }
            }
        } else {
            setContent {
                ChitChatTheme(
                    dynamicColor = true
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ){
                        ApplicationScreen(
                            navController = rememberNavController(),
                            modifier = Modifier.fillMaxSize(),
                            startDestination = welcomeDestination.route
                        )
                    }
                }
            }
        }
    }
}

