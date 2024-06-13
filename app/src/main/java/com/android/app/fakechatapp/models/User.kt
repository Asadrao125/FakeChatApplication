package com.android.app.fakechatapp.models

data class User(
    val userId: Int = 0,
    val profileImage: String = "",
    val name: String = "",
    val aboutInfo: String = "",
    val lastSeen: String = "",
    val phoneNo: String = "",
    val date: String = "",
    val isVerified: Int = 0,
    val isArchive: Int = 0,
    val encryptedText: String = "",
    val lastMessage: String = "No message",
    val lastMsgTime: String = ""
)