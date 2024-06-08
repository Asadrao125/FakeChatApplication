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

/* View Type
const val MSG_TYPE_LEFT: Int = 1
const val MSG_TYPE_RIGHT: Int = 2
const val MSG_TYPE_LEFT_IMAGE: Int = 3
const val MSG_TYPE_RIGHT_IMAGE: Int = 4
const val MSG_TYPE_LEFT_FILE: Int = 5
const val MSG_TYPE_RIGHT_FILE: Int = 6
* */