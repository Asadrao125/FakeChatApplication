package com.android.app.fakechatapp

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.android.app.fakechatapp.activities.adduser.AddUserActivity
import com.android.app.fakechatapp.bottomnav.adapter.ViewPagerAdapterNew
import com.android.app.fakechatapp.bottomnav.calls.CallsFragment
import com.android.app.fakechatapp.bottomnav.chats.ChatsFragment
import com.android.app.fakechatapp.bottomnav.communities.CommunityFragment
import com.android.app.fakechatapp.bottomnav.status.StatusFragment
import com.android.app.fakechatapp.database.Database
import com.android.app.fakechatapp.databinding.ActivityMainBinding
import com.android.app.fakechatapp.activities.add_status.AddStatusActivity
import com.android.app.fakechatapp.activities.profile.ProfileActivity
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var mainViewModel: MainViewModel
    private lateinit var database: Database

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        mainViewModel = MainViewModel()
        binding.mainViewModel = mainViewModel

        database = Database(applicationContext)
        database.createDatabase()

        val adapter = ViewPagerAdapterNew(supportFragmentManager, lifecycle)
        adapter.addFragment(ChatsFragment())
        adapter.addFragment(StatusFragment())
        adapter.addFragment(CommunityFragment())
        adapter.addFragment(CallsFragment())

        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Chats"
                1 -> tab.text = "Updates"
                2 -> tab.text = "Calls"
                else -> tab.text = "Communities"
            }
        }.attach()

        binding.chatLayout.setOnClickListener {
            binding.tabLayout.getTabAt(0)?.select()
        }

        binding.statusLayout.setOnClickListener {
            binding.tabLayout.getTabAt(1)?.select()
        }

        binding.communitiesLayout.setOnClickListener {
            binding.tabLayout.getTabAt(2)?.select()
        }

        binding.callLayout.setOnClickListener {
            binding.tabLayout.getTabAt(3)?.select()
        }

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    when (it.position) {
                        0 -> setChatView()
                        1 -> setStatusView()
                        2 -> setCommunitiesView()
                        else -> setCallsView()
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        binding.fab.setOnClickListener {
            if (binding.tabLayout.selectedTabPosition == 0) {
                startActivity(Intent(this, AddUserActivity::class.java))
            } else if (binding.tabLayout.selectedTabPosition == 1) {
                startActivity(Intent(this, AddStatusActivity::class.java))
            }
        }

        binding.fabAddStatus.setOnClickListener {
            startActivity(Intent(this, AddStatusActivity::class.java))
        }

        binding.menu.setOnClickListener {
            showChatMenuDialog()
        }
    }

    fun setChatView() {
        binding.imgChats.setBackgroundDrawable(R.drawable.bottom_nav_bg)
        binding.imgStatus.background = null
        binding.imgCalls.background = null
        binding.imgCommunity.background = null

        binding.tvChats.setTypeface(null, Typeface.BOLD)
        binding.tvStatus.setTypeface(null, Typeface.NORMAL)
        binding.tvCommunity.setTypeface(null, Typeface.NORMAL)
        binding.tvCalls.setTypeface(null, Typeface.NORMAL)

        binding.toolbarTitle.text = "Fake Chat"
        binding.toolbarTitle.setTypeface(null, Typeface.BOLD)

        binding.fab.setImageResource(R.drawable.ic_chat)
        binding.fab.visibility = VISIBLE

        binding.fabAddStatus.visibility = GONE

        binding.imgChats.setImageResource(R.drawable.ic_chat_tinted)
        binding.imgStatus.setImageResource(R.drawable.ic_status)
        binding.imgCommunity.setImageResource(R.drawable.ic_communities)
        binding.imgCalls.setImageResource(R.drawable.ic_phone)
    }

    fun setStatusView() {
        binding.imgChats.background = null
        binding.imgStatus.setBackgroundDrawable(R.drawable.bottom_nav_bg)
        binding.imgCalls.background = null
        binding.imgCommunity.background = null

        binding.tvChats.setTypeface(null, Typeface.NORMAL)
        binding.tvStatus.setTypeface(null, Typeface.BOLD)
        binding.tvCommunity.setTypeface(null, Typeface.NORMAL)
        binding.tvCalls.setTypeface(null, Typeface.NORMAL)

        binding.toolbarTitle.text = "Updates"
        binding.toolbarTitle.setTypeface(null, Typeface.NORMAL)

        binding.fab.setImageResource(R.drawable.ic_status)
        binding.fab.visibility = VISIBLE

        binding.fabAddStatus.visibility = VISIBLE

        binding.imgChats.setImageResource(R.drawable.ic_chat)
        binding.imgStatus.setImageResource(R.drawable.ic_status_tinted)
        binding.imgCommunity.setImageResource(R.drawable.ic_communities)
        binding.imgCalls.setImageResource(R.drawable.ic_phone)
    }

    fun setCommunitiesView() {
        binding.imgChats.background = null
        binding.imgStatus.background = null
        binding.imgCalls.background = null
        binding.imgCommunity.setBackgroundDrawable(R.drawable.bottom_nav_bg)

        binding.tvChats.setTypeface(null, Typeface.NORMAL)
        binding.tvStatus.setTypeface(null, Typeface.NORMAL)
        binding.tvCommunity.setTypeface(null, Typeface.BOLD)
        binding.tvCalls.setTypeface(null, Typeface.NORMAL)

        binding.toolbarTitle.text = "Communities"
        binding.toolbarTitle.setTypeface(null, Typeface.NORMAL)
        binding.fab.visibility = GONE

        binding.fabAddStatus.visibility = GONE

        binding.imgChats.setImageResource(R.drawable.ic_chat)
        binding.imgStatus.setImageResource(R.drawable.ic_status)
        binding.imgCommunity.setImageResource(R.drawable.ic_communities_tinted)
        binding.imgCalls.setImageResource(R.drawable.ic_phone)
    }

    fun setCallsView() {
        binding.imgChats.background = null
        binding.imgStatus.background = null
        binding.imgCommunity.background = null
        binding.imgCalls.setBackgroundDrawable(R.drawable.bottom_nav_bg)

        binding.tvChats.setTypeface(null, Typeface.NORMAL)
        binding.tvStatus.setTypeface(null, Typeface.NORMAL)
        binding.tvCommunity.setTypeface(null, Typeface.NORMAL)
        binding.tvCalls.setTypeface(null, Typeface.BOLD)

        binding.toolbarTitle.text = "Calls"
        binding.toolbarTitle.setTypeface(null, Typeface.NORMAL)
        binding.fab.setImageResource(R.drawable.ic_call)
        binding.fab.visibility = VISIBLE

        binding.fabAddStatus.visibility = GONE

        binding.imgChats.setImageResource(R.drawable.ic_chat)
        binding.imgStatus.setImageResource(R.drawable.ic_status)
        binding.imgCommunity.setImageResource(R.drawable.ic_communities)
        binding.imgCalls.setImageResource(R.drawable.ic_phone_tinted)
    }

    private fun ImageView.setBackgroundDrawable(bg: Int) {
        background = ContextCompat.getDrawable(applicationContext, bg)
    }

    private fun showChatMenuDialog() {
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.chat_menu_dialog_layout, null)
        val popupWindow = PopupWindow(view, 500, ConstraintLayout.LayoutParams.WRAP_CONTENT, true)
        popupWindow.showAsDropDown(binding.menu, 0, 0)
        val tvSettings = view.findViewById<TextView>(R.id.tvSettings)

        tvSettings.setOnClickListener { _: View? ->
            popupWindow.dismiss()
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (binding.tabLayout.selectedTabPosition != 0)
            binding.tabLayout.getTabAt(0)?.select()
        else super.onBackPressed()
    }
}