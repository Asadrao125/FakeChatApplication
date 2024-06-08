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
import com.android.app.fakechatapp.models.User
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
    private lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_call_details)

        viewModel = CallDetailsActivityViewModel(applicationContext)
        binding.lifecycleOwner = this

        userId = intent.getIntExtra("user_id", 0)
        imgBack = findViewById(R.id.imgBack)
        toolbarTitle = findViewById(R.id.toolbarTitle)
        database = Database(applicationContext)

        imgBack.setOnClickListener { onBackPressed() }

        binding.callsRv.layoutManager = LinearLayoutManager(applicationContext)
        binding.callsRv.setHasFixedSize(true)
        callAdapter = CallDetailsAdapter(this)
        binding.callsRv.adapter = callAdapter

        user = database.getSingleUser(userId)
        toolbarTitle.text = "Call Info"
        binding.tvAbout.text = user.aboutInfo
        binding.tvUserName.text = user.name

        Picasso.get().load(File(user.profileImage)).placeholder(R.drawable.ic_user)
            .into(binding.profilePic)

        binding.profilePic.setOnClickListener {
            startActivity(
                Intent(applicationContext, ImageViewActivity::class.java)
                    .putExtra("image_path", user.profileImage)
                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            )
        }

        binding.imgAudioCall.setOnClickListener {
            val callId = database.insertCall(
                Call(
                    callId = 0,
                    callReceiverId = user.userId,
                    userName = user.name,
                    callDirection = "OUTGOING",
                    date = viewModel.getCurrentDate(),
                    time = viewModel.getCurrentTime(),
                    callType = "AUDIO",
                    callDuration = "0",
                    callProfileImage = user.profileImage
                )
            )
            startActivity(
                Intent(this, CallActivity::class.java)
                    .putExtra("user_name", user.name)
                    .putExtra("call_id", callId)
                    .putExtra("user_id", user.userId)
            )
        }

        binding.imgVideoCall.setOnClickListener {
            val callId = database.insertCall(
                Call(
                    callId = 0,
                    callReceiverId = user.userId,
                    userName = user.name,
                    callDirection = "OUTGOING",
                    date = viewModel.getCurrentDate(),
                    time = viewModel.getCurrentTime(),
                    callType = "VIDEO",
                    callDuration = "0",
                    callProfileImage = user.profileImage
                )
            )
            startActivity(
                Intent(this, VideoCallActivity::class.java)
                    .putExtra("user_name", user.name)
                    .putExtra("call_id", callId)
                    .putExtra("user_id", user.userId)
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