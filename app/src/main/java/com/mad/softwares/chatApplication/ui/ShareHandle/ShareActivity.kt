package com.mad.softwares.chatApplication.ui.ShareHandle

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.mad.softwares.chatApplication.ui.theme.ChitChatTheme

class ShareActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChitChatTheme(dynamicColor = true){ HandleSharedContent(intent, completed = {finish()}) }
        }
    }

//    override fun onNewIntent(intent: Intent?) {
//        super.onNewIntent(intent)
//        intent?.let {
//            setContent {
//                HandleSharedContent(it)
//            }
//        }
//    }
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent?.let {
            setContent {
                ChitChatTheme(dynamicColor = true){ HandleSharedContent(it, completed = {finish()}) }
            }

        }
    }
}
