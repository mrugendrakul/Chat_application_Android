package com.mad.softwares.chatApplication.ui.migration

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.mad.softwares.chatApplication.data.DataRepository

class MigrateViewmodel(
    private val savedStateHandle: SavedStateHandle,
    private val dataRepository: DataRepository
): ViewModel() {

}