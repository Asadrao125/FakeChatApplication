package com.android.app.fakechatapp.activities.userprofile

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import com.android.app.fakechatapp.R
import com.android.app.fakechatapp.database.Database
import com.android.app.fakechatapp.databinding.ActivityUserProfileBinding
import com.android.app.fakechatapp.models.User
import com.android.app.fakechatapp.utils.SharedPref
import com.squareup.picasso.Picasso
import java.io.File

class UserProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserProfileBinding
    private lateinit var database: Database
    private lateinit var imgBack: ImageView
    private lateinit var toolbarTitle: TextView
    private lateinit var sharedPref: SharedPref
    private var userId: Int = 0
    private lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_user_profile)

        userId = intent.getIntExtra("user_id", 0)
        database = Database(applicationContext)
        sharedPref = SharedPref(applicationContext)
        imgBack = findViewById(R.id.imgBack)
        toolbarTitle = findViewById(R.id.toolbarTitle)

        user = database.getSingleUser(userId)

        imgBack.setOnClickListener { onBackPressed() }
        toolbarTitle.text = user.name
    }

    override fun onResume() {
        super.onResume()
        loadProfileData()
    }

    private fun loadProfileData() {
        Picasso.get().load(File(user.profileImage)).placeholder(R.drawable.ic_user)
            .into(binding.profilePic)
        binding.tvAbout.text = user.aboutInfo
        binding.tvUsername.text = user.name
        binding.tvPhoneNumber.text = user.phoneNo
    }
}