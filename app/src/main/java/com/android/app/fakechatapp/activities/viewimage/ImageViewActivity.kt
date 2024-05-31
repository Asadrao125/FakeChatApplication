package com.android.app.fakechatapp.activities.viewimage

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import com.android.app.fakechatapp.R
import com.android.app.fakechatapp.databinding.ActivityImageViewBinding
import com.bumptech.glide.Glide

class ImageViewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityImageViewBinding
    private lateinit var imgBack: ImageView
    private lateinit var toolbarTitle: TextView
    private var imagePath = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_image_view)

        imagePath = intent.getStringExtra("image_path")!!
        imgBack = findViewById(R.id.imgBack)
        toolbarTitle = findViewById(R.id.toolbarTitle)

        imgBack.setOnClickListener { onBackPressed() }
        toolbarTitle.text = "Add Contact"

        Glide.with(applicationContext)
            .load(imagePath)
            .centerCrop()
            .placeholder(R.drawable.ic_user)
            .into(binding.image)
    }
}