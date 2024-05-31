package com.android.app.fakechatapp.models

data class Chat(
    val messageId: Int = 0,
    val receiverName: String,
    val message: String,
    val time: String,
    val senderId: Int,
    val receiverId: Int,
    val viewType: Int,
    val date: String,
    val imagePath: String = "",
    val filePath: String = ""
)

/* viewType
*  1 My Plain Message
*  2 Sender Plain Message
*  3 Sender Image Message
*  4 Sender Image Message
*  5 Sender File Message
*  6 Sender File Message */