package com.android.app.fakechatapp.activities.settings

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import com.android.app.fakechatapp.R
import com.android.app.fakechatapp.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var viewModel: SettingsActivityViewModel
    private lateinit var imgBack: ImageView
    private lateinit var toolbarTitle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_settings)

        viewModel = SettingsActivityViewModel()

        imgBack = findViewById(R.id.imgBack)
        toolbarTitle = findViewById(R.id.toolbarTitle)

        imgBack.setOnClickListener { onBackPressed() }
        toolbarTitle.text = "Settings"
    }
}