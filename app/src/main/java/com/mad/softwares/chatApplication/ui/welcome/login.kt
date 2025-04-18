package com.mad.softwares.chatApplication.ui.welcome

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mad.softwares.chatApplication.R
import com.mad.softwares.chatApplication.ui.ApptopBar
import com.mad.softwares.chatApplication.ui.GodViewModelProvider
import com.mad.softwares.chatApplication.ui.LoadingIndicator
import com.mad.softwares.chatApplication.ui.destinationData

object loginScreenDestination:destinationData {
    override val route = "login"
    override val title = R.string.login
    override val canBack = true
}

@Composable
fun LoginScreen(
    navigateUp: () -> Unit,
    navigateToChats:()->Unit,
    welcomeViewModel: welcomeViewModel = viewModel(factory = GodViewModelProvider.Factory)
) {
    val uiState = welcomeViewModel.startUiState.collectAsState().value
    if (uiState.isUserInside){
        welcomeViewModel.resetUserInside()
        navigateToChats()
    }
    LoginScreenBody(
        updatePassword = {welcomeViewModel.updatePassword(it)},
        updateUsername = {welcomeViewModel.updateUsername(it)},
        startUiState = uiState,
        navigateUp = navigateUp,
        login = {welcomeViewModel.loginWithTheInfo()},
        navigateToChats = navigateToChats
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreenBody(
    updateUsername:(String)->Unit,
    updatePassword:(String)->Unit,
    login:()->Unit,
    navigateToChats: () -> Unit,
    modifier:Modifier = Modifier,
    startUiState: StartUiState,
    navigateUp:()->Unit
){
//    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    var passwordVisible by remember {
        mutableStateOf(false)
    }
    Scaffold(
        topBar = { ApptopBar(
            destinationData = loginScreenDestination,
//            scrollBehavior = scrollBehavior,
            navigateUp = navigateUp
        ) },
        modifier = Modifier
            .imePadding()
    ){ it ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(it),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
//            if(!startUiState.isUserInside){
//                Text(
//                    text = stringResource(R.string.not_account_found),
//                    textAlign = TextAlign.Center,
//                    fontSize = 15.sp,
//                    color = MaterialTheme.colorScheme.error,
//                    modifier = modifier.fillMaxWidth()
//                )
//            }

            if(startUiState.isError){
                Text(
                    text = startUiState.errorMessage,
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.error)
            }
            OutlinedTextField(
                value = startUiState.username,
                onValueChange = { updateUsername(it) },
                label = {
                    if (startUiState.emptyUsername){
                        Text(text = stringResource(R.string.username_cannot_be_empty))
                    }else if (startUiState.usernameExist){
                        Text(text = stringResource(R.string.username_exist_choose_different))
                    }else{
                        Text(text = stringResource(R.string.username))
                    }
                },
                isError = startUiState.emptyUsername || startUiState.usernameExist,
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Email
                ),
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        tint = MaterialTheme.colorScheme.onBackground,
                        contentDescription = "")
                }
            )
            OutlinedTextField(
                value = startUiState.password,
                onValueChange = { updatePassword(it) },
                label = {
                    if (startUiState.emptyPassword) {
                        Text(text = stringResource(R.string.password_cannot_be_empty))
                    }
                    else{
                        Text(text = stringResource(R.string.password))
                    }
                },
                isError = startUiState.emptyPassword,
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done,
                    keyboardType = KeyboardType.Password
                ),
                visualTransformation = if(!passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
                trailingIcon = {
                    val icon = if(passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility
                    IconButton(onClick = { passwordVisible = !passwordVisible}){
                        Icon(
                            imageVector = icon,
                            tint = MaterialTheme.colorScheme.onBackground,
                            contentDescription = ""
                        )
                    }
                }
            )
            Spacer(modifier = modifier.height(20.dp))
            Button(onClick = {
                login()
            }) {
                Text(text = stringResource(R.string.login))
            }

            Text(
                modifier = modifier
                    .clickable { },

                text = stringResource(R.string.forgot_password),
                textDecoration = TextDecoration.Underline
            )



        }
    }
    LoadingIndicator(isLoading = startUiState.isLoading)
}

@Preview
@Composable
fun LoginScreenPreview(){
    LoginScreenBody(
        navigateUp = {},
        updatePassword = {},
        updateUsername = {},
        startUiState = StartUiState(
            isError = true,
            errorMessage = "some error to see"
        ),
        navigateToChats = {},
        login = {}
    )
}
