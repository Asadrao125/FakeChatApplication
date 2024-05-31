package com.android.app.fakechatapp.activities.adduser

import android.content.Context
import android.os.Build
import androidx.lifecycle.ViewModel
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

class AddUserViewModel(application: Context) : ViewModel() {

    init {

    }

    fun getCurrentDate(): String {
        val formatter = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH)
        } else {
            throw IllegalStateException("Unable to get date")
        }
        return LocalDate.now().format(formatter)
    }

    fun getCurrentTime(): String {
        val currentTime = Calendar.getInstance().time
        val format = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return format.format(currentTime)
    }
}