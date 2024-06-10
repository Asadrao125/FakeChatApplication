package com.android.app.fakechatapp.models

data class Status(
    val statusId: Int = 0,
    val statusUploaderId: Int = 0,
    val userName: String,
    val statusMessage: String,
    val date: String,
    val time: String,
    val isSeen: Int,
    val statusUploaderProfile: String,
    val statusColor: Int,
    val isMute: Int,
    val statusType: Int, // 1 Text, 2 Image
    val imagePath: String
)