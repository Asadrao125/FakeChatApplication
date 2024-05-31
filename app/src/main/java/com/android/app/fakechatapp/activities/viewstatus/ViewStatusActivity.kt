package com.android.app.fakechatapp.activities.viewstatus

import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.android.app.fakechatapp.R
import com.android.app.fakechatapp.database.Database
import com.android.app.fakechatapp.databinding.ActivityViewStatusBinding

class ViewStatusActivity : AppCompatActivity() {
    private lateinit var binding: ActivityViewStatusBinding
    private lateinit var database: Database
    private lateinit var handler: Handler
    private var statusId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_view_status)
        database = Database(applicationContext)

        handler = Handler()
        binding.tvStatus.text = intent.getStringExtra("status_msg")
        statusId = intent.getIntExtra("status_id", 0)
        database.updateStatus(1, statusId)

        val bgColor = intent.getIntExtra("status_color", R.color.whatsapp_green)
        binding.parentLayout.setBackgroundColor(bgColor)
        window.statusBarColor = bgColor

        startTimerWithDelay()
    }

    private fun startTimerWithDelay() {
        handler.postDelayed({
            onBackPressed()
        }, 2000)
    }
}