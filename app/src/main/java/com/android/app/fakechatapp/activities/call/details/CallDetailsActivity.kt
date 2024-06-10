package com.android.app.fakechatapp.activities.call.details

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.app.fakechatapp.R
import com.android.app.fakechatapp.adapters.CallDetailsAdapter
import com.android.app.fakechatapp.activities.call.audio.CallActivity
import com.android.app.fakechatapp.activities.call.video.VideoCallActivity
import com.android.app.fakechatapp.activities.viewimage.ImageViewActivity
import com.android.app.fakechatapp.database.Database
import com.android.app.fakechatapp.databinding.ActivityCallDetailsBinding
import com.android.app.fakechatapp.models.Call
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class CallDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCallDetailsBinding
    private lateinit var imgBack: ImageView
    private lateinit var toolbarTitle: TextView
    private lateinit var database: Database
    private lateinit var callAdapter: CallDetailsAdapter
    private lateinit var viewModel: CallDetailsActivityViewModel
    private var userId: Int = 0
    private var userName: String = ""
    private var profilePath: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_call_details)

        viewModel = CallDetailsActivityViewModel(applicationContext)
        binding.lifecycleOwner = this

        userId = intent.getIntExtra("user_id", 0)
        userName = intent.getStringExtra("user_name") ?: ""
        profilePath = intent.getStringExtra("profile_path") ?: ""
        imgBack = findViewById(R.id.imgBack)
        toolbarTitle = findViewById(R.id.toolbarTitle)
        database = Database(applicationContext)

        imgBack.setOnClickListener { onBackPressed() }

        binding.callsRv.layoutManager = LinearLayoutManager(applicationContext)
        binding.callsRv.setHasFixedSize(true)
        callAdapter = CallDetailsAdapter(this)
        binding.callsRv.adapter = callAdapter

        toolbarTitle.text = "Call Info"
        binding.tvUserName.text = userName

        Picasso.get().load(File(profilePath)).placeholder(R.drawable.ic_user)
            .into(binding.profilePic)

        binding.profilePic.setOnClickListener {
            startActivity(
                Intent(applicationContext, ImageViewActivity::class.java)
                    .putExtra("image_path", profilePath)
                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            )
        }

        binding.imgAudioCall.setOnClickListener {
            val callId = database.insertCall(
                Call(
                    callId = 0,
                    callReceiverId = userId,
                    userName = userName,
                    callDirection = "OUTGOING",
                    date = viewModel.getCurrentDate(),
                    time = viewModel.getCurrentTime(),
                    callType = "AUDIO",
                    callDuration = "0",
                    callProfileImage = profilePath
                )
            )
            startActivity(
                Intent(this, CallActivity::class.java)
                    .putExtra("user_name", userName)
                    .putExtra("call_id", callId)
                    .putExtra("user_id", userId)
                    .putExtra("profile_path", profilePath)
            )
        }

        binding.imgVideoCall.setOnClickListener {
            val callId = database.insertCall(
                Call(
                    callId = 0,
                    callReceiverId = userId,
                    userName = userName,
                    callDirection = "OUTGOING",
                    date = viewModel.getCurrentDate(),
                    time = viewModel.getCurrentTime(),
                    callType = "VIDEO",
                    callDuration = "0",
                    callProfileImage = profilePath
                )
            )
            startActivity(
                Intent(this, VideoCallActivity::class.java)
                    .putExtra("user_name", userName)
                    .putExtra("call_id", callId)
                    .putExtra("user_id", userId)
                    .putExtra("profile_path", profilePath)
            )
        }
    }

    override fun onStart() {
        super.onStart()
        getDataAndSetAdapter()
    }

    private fun getDataAndSetAdapter() {
        CoroutineScope(Dispatchers.IO).launch {
            val calls = withContext(Dispatchers.IO) {
                database.getAllCallsByUser(userId)
            }

            withContext(Dispatchers.Main) {
                if (calls != null)
                    callAdapter.submitList(calls)
            }
        }
    }
}