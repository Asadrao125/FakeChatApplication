package com.android.app.fakechatapp.activities.viewstatus

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.android.app.fakechatapp.R
import com.android.app.fakechatapp.database.Database
import com.android.app.fakechatapp.databinding.ActivityViewStatusBinding
import com.squareup.picasso.Picasso
import java.io.File

class ViewStatusActivity : AppCompatActivity() {
    private lateinit var binding: ActivityViewStatusBinding
    private lateinit var database: Database
    private lateinit var handler: Handler
    private val backPressRunnable = Runnable { onBackPressed() }
    private var statusId = 0
    private var statusType = 0
    private var statusImagePath = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_view_status)
        database = Database(applicationContext)

        handler = Handler(Looper.getMainLooper())
        statusId = intent.getIntExtra("status_id", 0)
        statusType = intent.getIntExtra("statusType", 0)
        statusImagePath = intent.getStringExtra("statusImagePath") ?: ""
        database.updateStatus(1, statusId)

        if (statusType == 1) {
            binding.tvStatus.visibility = VISIBLE
            binding.statusImage.visibility = GONE

            binding.tvStatus.text = intent.getStringExtra("status_msg")

            val bgColor = intent.getIntExtra("status_color", R.color.whatsapp_green)
            binding.parentLayout.setBackgroundColor(bgColor)
            window.statusBarColor = bgColor
        } else if (statusType == 2) {
            binding.tvStatus.visibility = GONE
            binding.statusImage.visibility = VISIBLE

            Picasso.get().load(File(statusImagePath)).placeholder(R.drawable.ic_user)
                .into(binding.statusImage)
        }

        startTimerWithDelay()
    }

    private fun startTimerWithDelay() {
        handler.postDelayed(backPressRunnable, 2000)
    }

    override fun onBackPressed() {
        handler.removeCallbacks(backPressRunnable)
        super.onBackPressed()
    }
}