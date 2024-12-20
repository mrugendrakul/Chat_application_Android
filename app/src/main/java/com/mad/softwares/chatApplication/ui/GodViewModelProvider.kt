package com.mad.softwares.chatApplication.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mad.softwares.chatApplication.MyApplication
import com.mad.softwares.chatApplication.ui.ShareHandle.ShareHandlerViewModel
import com.mad.softwares.chatApplication.ui.chats.singles.AddChatViewModel
import com.mad.softwares.chatApplication.ui.chats.groups.AddGroupViewModel
import com.mad.softwares.chatApplication.ui.chats.ChatsViewModel
import com.mad.softwares.chatApplication.ui.messages.MessagesViewModel
import com.mad.softwares.chatApplication.ui.welcome.welcomeViewModel

object GodViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            welcomeViewModel(
                myApplication().container.dataRepository, myApplication().container.auth
            )
        }

        initializer {
            ChatsViewModel(
                dataRepository = myApplication().container.dataRepository,
                savedStateHandle = this.createSavedStateHandle(),
            )
        }

        initializer {
            AddChatViewModel(
                savedStateHandle = this.createSavedStateHandle(),
                dataRepository = myApplication().container.dataRepository
            )
        }

        initializer {
            MessagesViewModel(
                savedStateHandle = this.createSavedStateHandle(),
                dataRepository = myApplication().container.dataRepository
            )
        }

        initializer{
            AddGroupViewModel(
                savedStateHandle = this.createSavedStateHandle(),
                dataRepository = myApplication().container.dataRepository
            )
        }

        initializer {
            ShareHandlerViewModel(
                dataRepository =  myApplication().container.dataRepository
            )
        }
    }
}

fun CreationExtras.myApplication(): MyApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MyApplication)