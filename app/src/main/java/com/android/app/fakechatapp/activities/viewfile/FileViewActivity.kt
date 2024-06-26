package com.android.app.fakechatapp.activities.viewfile

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.android.app.fakechatapp.R
import com.android.app.fakechatapp.databinding.ActivityFileViewBinding

class FileViewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFileViewBinding
    private lateinit var imgBack: ImageView
    private lateinit var toolbarTitle: TextView
    private var pdfFilePath = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_file_view)

        imgBack = findViewById(R.id.imgBack)
        toolbarTitle = findViewById(R.id.toolbarTitle)

        imgBack.setOnClickListener { onBackPressed() }
        toolbarTitle.text = "File"

        pdfFilePath = intent.getStringExtra("file_path") ?: return
    }
}