package com.android.app.fakechatapp.models

data class Call(
    val callId: Int = 0,
    val callReceiverId: Int = 0,
    val userName: String,
    val callDirection: String,
    val date: String,
    val time: String,
    val callType: String,
    val callDuration: String,
    val callProfileImage: String
)