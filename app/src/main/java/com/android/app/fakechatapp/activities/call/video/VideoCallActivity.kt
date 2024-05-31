package com.android.app.fakechatapp.activities.call.video

import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.hardware.Camera
import android.view.SurfaceHolder
import android.view.View
import android.widget.ImageView
import android.widget.MediaController
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.android.app.fakechatapp.R
import com.android.app.fakechatapp.database.Database
import com.android.app.fakechatapp.databinding.ActivityVideoCallBinding
import com.android.app.fakechatapp.models.User
import com.android.app.fakechatapp.utils.Constants
import com.android.app.fakechatapp.utils.SharedPref
import com.squareup.picasso.Picasso
import java.io.File

class VideoCallActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVideoCallBinding
    private lateinit var viewModel: VideoCallActivityViewModel
    private lateinit var database: Database
    private var callId: Long = 0
    private lateinit var user: User
    private lateinit var mediaController: MediaController
    private lateinit var sharedPref: SharedPref

    // Timer
    private var secondsElapsed = 0
    private var isTimerRunning = false
    private lateinit var timerRunnable: Runnable
    private lateinit var handler: Handler

    // Bottom Layout
    private var isSpeakerSelected = false
    private var isChatSelected = false
    private var isMicSelected = false

    // Front Camera Preview
    private var camera: Camera? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_video_call)

        viewModel = VideoCallActivityViewModel(applicationContext)
        database = Database(applicationContext)
        sharedPref = SharedPref(this)
        mediaController = MediaController(applicationContext)
        binding.videoView.setMediaController(mediaController)
        binding.videoView.setMediaController(null)

        callId = intent.getLongExtra("call_id", 0)
        user = database.getSingleUser(intent.getIntExtra("user_id", 0))

        handler = Handler()

        binding.endCallLayout.setOnClickListener {
            if (isTimerRunning)
                stopTimer()
            onBackPressed()
        }

        database.updateCallTime(viewModel.getCurrentTime(), callId)

        startTimerWithDelay()
        binding.tUserName.text = user.name
        Picasso.get().load(File(user.profileImage)).placeholder(R.drawable.ic_user)
            .into(binding.profilePic)

        setBottomListeners()

        if (checkCameraHardware()) {
            camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT)
            camera?.setDisplayOrientation(90)

            binding.surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
                override fun surfaceCreated(holder: SurfaceHolder) {
                    try {
                        camera?.setPreviewDisplay(holder)
                        camera?.startPreview()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun surfaceChanged(
                    holder: SurfaceHolder,
                    format: Int,
                    width: Int,
                    height: Int
                ) {
                }

                override fun surfaceDestroyed(holder: SurfaceHolder) {
                    releaseCamera()
                }
            })
        }
    }

    private fun setBottomListeners() {
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

    private fun checkCameraHardware(): Boolean {
        return packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
    }

    private fun releaseCamera() {
        camera?.stopPreview()
        camera?.release()
        camera = null
    }

    private fun startTimerWithDelay() {
        handler.postDelayed({
            startTimer()
            startVideo()
            setFrontViewWidth()
        }, 3000)
    }

    private fun setFrontViewWidth() {
        val layoutParams = binding.surfaceView.layoutParams as RelativeLayout.LayoutParams
        // Set Width and Height
        layoutParams.width = dpToPx(120F)
        layoutParams.height = dpToPx(200F)
        // Add margins
        val rightMargin = dpToPx(10F)
        val bottomMargin = dpToPx(10F)
        layoutParams.setMargins(0, 0, rightMargin, bottomMargin)
        // Set Align Parent Right = true
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE)
        binding.surfaceView.layoutParams = layoutParams
        binding.top.visibility = View.GONE
    }

    private fun startVideo() {
        val res = sharedPref.read(Constants.VIDEO_PATH, "No")
        if (res != "No") {
            val videoUri = Uri.parse(res)
            binding.videoView.setVideoURI(videoUri)
            binding.videoView.start()

            binding.videoView.setOnCompletionListener {
                binding.videoView.start()
            }
        }
    }

    private fun dpToPx(dp: Float): Int {
        val scale = resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }

    private fun startTimer() {
        if (!isTimerRunning) {
            isTimerRunning = true
            timerRunnable = object : Runnable {
                override fun run() {
                    secondsElapsed++
                    val minutes = (secondsElapsed % 3600) / 60
                    val seconds = secondsElapsed % 60
                    binding.tvTimer.text = String.format("%02d:%02d", minutes, seconds)
                    database.updateCallDuration(
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