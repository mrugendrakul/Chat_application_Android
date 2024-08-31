package com.mad.softwares.chitchat.ui.chats.groups

//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.remember
//import androidx.lifecycle.viewmodel.compose.viewModel
//import androidx.navigation.NavHostController
//import androidx.navigation.NavType
//import androidx.navigation.compose.NavHost
//import androidx.navigation.compose.composable
//import androidx.navigation.compose.rememberNavController
//import androidx.navigation.navArgument
//import com.mad.softwares.chitchat.ui.GodViewModelProvider
//import com.mad.softwares.chitchat.ui.chats.chatsScreenDestination
//
//
//fun AddGroupNavGraph(
//    argument: String,
//    navigateWithReload:()->Unit,
//    navController:NavHostController,
////    addGroupViewModel:AddGroupViewModel = viewModel(factory = GodViewModelProvider.Factory)
//) {
////    val navController = rememberNavController()
////    val factor = remember{GodViewModelProvider.Factory}
////    val addGroupViewModel:AddGroupViewModel = viewModel(factory = factor)
//    composable(
//        route = "${addGroupDestination.route}/{User}",
//        arguments = listOf(
//            navArgument("User") {
//                type = NavType.StringType
//                defaultValue = ""
//            }
//        )
//    ) {
//
//        AddGroup(navigateUp = {
//            navController.popBackStack(
//                chatsScreenDestination.routeWithReload,
//                false
//            )
//        },
//            navigateWithReload = {
//                navigateWithReload()
//            },
//            navigateToAddGroupWithName = {
//                navController.navigate(addGroupWithNameDestination.route)
//            },
//            viewModel =  viewModel(factory = GodViewModelProvider.Factory)
//        )
//    }
//    composable(
//        route = addGroupWithNameDestination.route
//    ){
//        AddGroupWithName(
//            viewModel =  viewModel(factory = GodViewModelProvider.Factory),
//            navigateUp = { navController.navigateUp() }
//        )
//    }
//}