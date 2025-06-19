package com.mad.softwares.chatApplication.ui.migration

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mad.softwares.chatApplication.R
import com.mad.softwares.chatApplication.ui.ApptopBar

import com.mad.softwares.chatApplication.ui.destinationData

object migrationScreenDestination : destinationData{
    override val route = "migration"
    override val title = R.string.migration
    override val canBack = true

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MigrationScreen(
    getBack:()->Unit
){
    Scaffold(
        topBar = {
            ApptopBar(
                destinationData = migrationScreenDestination,

                navigateUp = getBack,
                canGoBack = true,
                goBack = getBack
            )
        }
    ) { padding ->
        Text(modifier = Modifier.padding(padding),
            text = "Migration Screen"
        )


    }
}