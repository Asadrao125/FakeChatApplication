package com.android.app.fakechatapp.activities.call.audio

import android.os.Bundle
import android.os.Handler
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.android.app.fakechatapp.R
import com.android.app.fakechatapp.database.MyDatabase
import com.android.app.fakechatapp.databinding.ActivityCallBinding
import com.squareup.picasso.Picasso
import java.io.File

class CallActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCallBinding
    private lateinit var callViewModel: CallActivityViewModel
    private lateinit var db: MyDatabase
    private var userName = ""
    private var callId: Long = 0
    private var userId: Int = 0
    private var profilePath: String = ""

    // Timer
    private var secondsElapsed = 0
    private var isTimerRunning = false
    private lateinit var timerRunnable: Runnable
    private lateinit var handler: Handler

    private var isSpeakerSelected = false
    private var isChatSelected = false
    private var isMicSelected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_call)

        callViewModel = CallActivityViewModel(applicationContext)
        db = MyDatabase(applicationContext)

        userName = intent.getStringExtra("user_name")!!
        callId = intent.getLongExtra("call_id", 0)
        userId = intent.getIntExtra("user_id", 0)
        profilePath = intent.getStringExtra("profile_path") ?: ""

        handler = Handler()

        binding.endCallLayout.setOnClickListener {
            if (isTimerRunning)
                stopTimer()
            onBackPressed()
        }

        startTimerWithDelay()

        Picasso.get().load(File(profilePath)).placeholder(R.drawable.ic_user)
            .into(binding.profilePic)

        binding.tUserName.text = userName

        binding.speaker.setOnClickListener {
            if (!isSpeakerSelected) {
                binding.imgSpeaker.setBackground(R.drawable.call_bottom_circle)
                isSpeakerSelected = true
            } else {
                isSpeakerSelected = false
                binding.imgSpeaker.background = null
            }
        }

        binding.chat.setOnClickListener {
            if (!isChatSelected) {
                binding.imgChat.setBackground(R.drawable.call_bottom_circle)
                isChatSelected = true
            } else {
                isChatSelected = false
                binding.imgChat.background = null
            }
        }

        binding.mic.setOnClickListener {
            if (!isMicSelected) {
                binding.imgMic.setBackground(R.drawable.call_bottom_circle)
                isMicSelected = true
            } else {
                isMicSelected = false
                binding.imgMic.background = null
            }
        }
    }

    private fun startTimerWithDelay() {
        handler.postDelayed({
            startTimer()
        }, 3000)
    }

    private fun startTimer() {
        if (!isTimerRunning) {
            isTimerRunning = true
            timerRunnable = object : Runnable {
                override fun run() {
                    secondsElapsed++
                    val hours = secondsElapsed / 3600
                    val minutes = (secondsElapsed % 3600) / 60
                    val seconds = secondsElapsed % 60
                    binding.tvTimer.text = String.format("%02d:%02d", minutes, seconds)
                    db.updateCallDuration(
                        String.format("%02d:%02d", minutes, seconds),
                        callId
                    )
                    handler.postDelayed(this, 1000)
                }
            }
            handler.post(timerRunnable)
        }
    }

    private fun ImageView.setBackground(drawableResId: Int) {
        background = ContextCompat.getDrawable(context, drawableResId)
    }

    private fun stopTimer() {
        if (isTimerRunning) {
            isTimerRunning = false
            handler.removeCallbacks(timerRunnable)
            secondsElapsed = 0
        }
    }
}