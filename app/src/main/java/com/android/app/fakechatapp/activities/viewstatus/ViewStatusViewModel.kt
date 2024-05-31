package com.android.app.fakechatapp.activities.viewstatus

import android.content.Context
import android.os.Build
import androidx.lifecycle.ViewModel
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

class ViewStatusViewModel(application: Context) : ViewModel() {

    fun getCurrentTime(): String {
        val currentTime = Calendar.getInstance().time
        val format = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return format.format(currentTime)
    }

    fun getCurrentDate(): String {
        val formatter = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH)
        } else {
            throw IllegalStateException("Unable to get date")
        }
        return LocalDate.now().format(formatter)
    }
}