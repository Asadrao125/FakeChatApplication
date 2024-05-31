package com.android.app.fakechatapp.utils

import android.content.Context
import androidx.core.content.ContextCompat
import com.android.app.fakechatapp.R
import kotlin.random.Random

class Constants {
    companion object {
        const val VIDEO_PATH = "videoPath"
        const val USER_PROFILE_PIC_PATH = "userProfilePicPath"
        const val USER_PROFILE_ABOUT = "userProfileAbout"
        const val USER_PROFILE_NAME = "userProfileName"
        const val USER_PROFILE_PHONE = "userProfilePhone"
        const val VIDEO_REQ_CODE = 108
        const val IMAGE_REQ_CODE = 111
        const val CAPTURE_IMAGE_REQ_CODE = 333
        const val CHAT_VIDEO_REQ_CODE = 444
        const val FILE_REQ_CODE = 222

        fun getColorList(context: Context): List<Int> {
            return listOf(
                ContextCompat.getColor(context, R.color.r_1),
                ContextCompat.getColor(context, R.color.r_2),
                ContextCompat.getColor(context, R.color.r_3),
                ContextCompat.getColor(context, R.color.r_4),
                ContextCompat.getColor(context, R.color.r_5),
                ContextCompat.getColor(context, R.color.r_6),
                ContextCompat.getColor(context, R.color.r_7),
                ContextCompat.getColor(context, R.color.r_8),
                ContextCompat.getColor(context, R.color.r_9),
                ContextCompat.getColor(context, R.color.r_10),
                ContextCompat.getColor(context, R.color.r_11),
                ContextCompat.getColor(context, R.color.r_12),
                ContextCompat.getColor(context, R.color.r_13),
                ContextCompat.getColor(context, R.color.r_14),
                ContextCompat.getColor(context, R.color.r_15),
            )
        }

        fun getRandomColor(context: Context): Int {
            val colors = getColorList(context)
            return Random.nextInt(colors.size)
        }

        fun getAboutList(): List<String> {
            return listOf(
                "Available",
                "Busy",
                "At school",
                "At the movies",
                "At work",
                "Battery about to die",
                "Can't talk, Fake Chat only",
                "In a meeting",
                "At the gym",
                "Sleeping",
                "Urgent calls only",
            )
        }
    }
}