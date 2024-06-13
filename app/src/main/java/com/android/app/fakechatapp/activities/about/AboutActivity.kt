package com.android.app.fakechatapp.activities.about

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.app.fakechatapp.R
import com.android.app.fakechatapp.adapters.AboutAdapter
import com.android.app.fakechatapp.database.MyDatabase
import com.android.app.fakechatapp.databinding.ActivityAboutBinding
import com.android.app.fakechatapp.utils.Constants
import com.android.app.fakechatapp.utils.RecyclerItemClickListener
import com.android.app.fakechatapp.utils.SharedPref

class AboutActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAboutBinding
    private lateinit var db: MyDatabase
    private lateinit var imgBack: ImageView
    private lateinit var toolbarTitle: TextView
    private lateinit var sharedPref: SharedPref
    private lateinit var adapter: AboutAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_about)

        db = MyDatabase(this)
        sharedPref = SharedPref(this)
        imgBack = findViewById(R.id.imgBack)
        toolbarTitle = findViewById(R.id.toolbarTitle)

        imgBack.setOnClickListener { onBackPressed() }
        toolbarTitle.text = "About"

        binding.aboutRv.layoutManager = LinearLayoutManager(this)
        binding.aboutRv.setHasFixedSize(true)
        adapter = AboutAdapter(this, Constants.getAboutList())
        binding.aboutRv.adapter = adapter

        binding.aboutRv.addOnItemTouchListener(
            RecyclerItemClickListener(
                this,
                binding.aboutRv,
                object : RecyclerItemClickListener.OnItemClickListener {
                    override fun onItemClick(view: View?, position: Int) {
                        val selectedOption = Constants.getAboutList()[position]
                        sharedPref.write(Constants.USER_PROFILE_ABOUT, selectedOption)
                        binding.tvAbout.text = selectedOption
                        onBackPressed()
                    }

                    override fun onItemLongClick(view: View?, position: Int) {}
                })
        )
    }
}