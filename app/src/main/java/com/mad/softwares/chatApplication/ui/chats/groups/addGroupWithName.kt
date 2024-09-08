package com.mad.softwares.chatApplication.ui.chats.groups

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.mad.softwares.chatApplication.R
import com.mad.softwares.chatApplication.ui.destinationData

object addGroupWithNameDestination : destinationData {
    override val route = "AddGroup"
    override val title = R.string.create_group
    override val canBack = true

    //    val members = listOf<String>("")
//    val memberString = members?.joinToString(",") ?: ""
//
//    //    val routeWithArgs = "$route/${memberString}"
//    fun routeWithArgs(User: String): String {
////        val membersString = members.joinToString(",")
//        return "$route/$User"
//    }
}

@Composable
fun AddGroupWithName(
    viewModel: AddGroupViewModel ,
    navigateUp: () -> Unit,
    navigateToChats :(Boolean)->Unit,
){
    val uiState = viewModel.addGroupUiState.collectAsState().value
    if (uiState.addGroupSuccess){
        navigateToChats(false)
    }
    AddGroupWithNameBody (
        navigateUp = navigateUp,
        uiState = uiState,
        grpNameChange = {viewModel.changeGroupName(it)},
        createGroup = {
            viewModel.createGroup()
//            navigateToChats(true)
        }
    )
}

@Composable
fun AddGroupWithNameBody(
    navigateUp: () -> Unit,
    uiState : AddGroupUiState,
    grpNameChange :(String)->Unit,
    createGroup:()->Unit
){
    Column{
        Text(text = "In this page you will see the members to add the users.")
        Text(text = "I am not implementing further ui, so just text will be there")
        uiState.newMembers.forEach{
            Text(text = "[$it]")
        }
        TextField(
            value = uiState.newGroupName,
            onValueChange = grpNameChange,
            label = { Text(text = stringResource(R.string.new_group_name))}
            )
        Button(onClick = createGroup) {
            Text(text = stringResource(id = R.string.create_group))
        }
    }

}

@Preview
@Composable
fun AddGroupWithNamePreview(){
    AddGroupWithNameBody(
        navigateUp = {},
        uiState = AddGroupUiState(),
        grpNameChange = {},
        createGroup = {}
    )
}