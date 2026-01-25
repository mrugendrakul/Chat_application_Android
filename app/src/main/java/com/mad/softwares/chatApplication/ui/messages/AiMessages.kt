package com.mad.softwares.chatApplication.ui.messages

import com.mad.softwares.chatApplication.R
import com.mad.softwares.chatApplication.ui.destinationData

object AiMessagesDestinationData: destinationData{
    override val route: String = "AiMessages"
    override val title: Int= R.string.ai_messages
    override val canBack: Boolean = true
}

