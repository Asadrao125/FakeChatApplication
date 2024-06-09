package com.android.app.fakechatapp.activities.call.videoplay

import android.net.Uri
import android.os.Bundle
import android.widget.MediaController
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.android.app.fakechatapp.R
import com.android.app.fakechatapp.databinding.ActivityVideoPlayBinding

class VideoPlayActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVideoPlayBinding
    private lateinit var mediaController: MediaController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_video_play)

        mediaController = MediaController(applicationContext)
        binding.videoView.setMediaController(mediaController)

        startVideo()
    }

    private fun startVideo() {
        val videoUri = Uri.parse(intent.getStringExtra("video_path"))
        binding.videoView.setVideoURI(videoUri)
        binding.videoView.start()

        binding.videoView.setOnCompletionListener {
            binding.videoView.start()
        }
    }
}