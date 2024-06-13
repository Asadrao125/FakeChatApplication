package com.android.app.fakechatapp

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.Window
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.app.fakechatapp.activities.add_status.AddStatusActivity
import com.android.app.fakechatapp.activities.adduser.AddUserActivity
import com.android.app.fakechatapp.activities.profile.ProfileActivity
import com.android.app.fakechatapp.adapters.SelectUserAdapter
import com.android.app.fakechatapp.bottomnav.adapter.ViewPagerAdapter
import com.android.app.fakechatapp.bottomnav.calls.CallsFragment
import com.android.app.fakechatapp.bottomnav.chats.ChatsFragment
import com.android.app.fakechatapp.bottomnav.communities.CommunityFragment
import com.android.app.fakechatapp.bottomnav.status.StatusFragment
import com.android.app.fakechatapp.database.MyDatabase
import com.android.app.fakechatapp.databinding.ActivityMainBinding
import com.android.app.fakechatapp.models.Status
import com.android.app.fakechatapp.models.User
import com.android.app.fakechatapp.utils.Constants
import com.android.app.fakechatapp.utils.RecyclerItemClickListener
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var mainViewModel: MainViewModel
    private lateinit var db: MyDatabase
    private var imagePath = ""
    private lateinit var selectedUser: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        mainViewModel = MainViewModel()
        binding.mainViewModel = mainViewModel

        db = MyDatabase(applicationContext)
        //db.createDatabase()

        val adapter = ViewPagerAdapter(supportFragmentManager, lifecycle)
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
                showSelectUserDialog()
            }
        }

        binding.camera.setOnClickListener {
            showSelectUserDialog()
        }

        binding.fabAddStatus.setOnClickListener {
            startActivity(Intent(this, AddStatusActivity::class.java))
        }

        binding.menu.setOnClickListener {
            showChatMenuDialog()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == Constants.IMAGE_REQ_CODE) {
                try {
                    val bmp =
                        BitmapFactory.decodeStream(contentResolver.openInputStream(data?.data!!))
                    CoroutineScope(Dispatchers.IO).launch {
                        saveImageToCache(bmp)
                    }
                    addStatus(selectedUser)
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun showSelectUserDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_users_layout)
        val userRv = dialog.findViewById<RecyclerView>(R.id.userRv)

        dialog.window?.setBackgroundDrawableResource(R.drawable.chatbg)
        dialog.window?.setDimAmount(0F)

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        userRv.layoutManager = LinearLayoutManager(this)
        userRv.setHasFixedSize(true)

        val users = db.getAllUsers(-1)
        userRv.adapter = SelectUserAdapter(this, users)

        userRv.addOnItemTouchListener(
            RecyclerItemClickListener(
                this,
                userRv,
                object : RecyclerItemClickListener.OnItemClickListener {
                    override fun onItemClick(view: View?, position: Int) {
                        selectedUser = users[position]
                        galleryIntent()
                        dialog.dismiss()
                    }

                    override fun onItemLongClick(view: View?, position: Int) {
                        selectedUser = users[position]
                        galleryIntent()
                        dialog.dismiss()
                    }
                })
        )
        dialog.show()

        val window = dialog.window
        val layoutParams = window?.attributes
        layoutParams?.width = (resources.displayMetrics.widthPixels * 0.9).toInt()
        window?.attributes = layoutParams
    }

    private fun addStatus(user: User) {
        db.insertStatus(
            Status(
                statusId = 0,
                statusUploaderId = user.userId,
                userName = user.name,
                statusMessage = "",
                date = mainViewModel.getCurrentDate(),
                time = mainViewModel.getCurrentTime(),
                isSeen = 0,
                statusUploaderProfile = user.profileImage,
                statusColor = 0,
                isMute = 0,
                statusType = 2, // Image
                imagePath = imagePath
            )
        )
        updateStatusList()
    }

    private fun updateStatusList() {
        val fragmentManager = supportFragmentManager
        val fragment = fragmentManager.findFragmentById(R.id.container) as? StatusFragment
        fragment?.getDataAndSetAdapter()
    }

    private fun galleryIntent() {
        val photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type = "image/*"
        startActivityForResult(photoPickerIntent, Constants.IMAGE_REQ_CODE)
    }

    private suspend fun saveImageToCache(bitmapImage: Bitmap): Boolean =
        withContext(Dispatchers.IO) {
            val cw = ContextWrapper(applicationContext)
            val directory = cw.getDir("status_images", MODE_PRIVATE)
            if (!directory.exists()) {
                directory.mkdir()
            }
            val childPath = "FTG_STATUS_IMAGE" + System.currentTimeMillis()
            val myPath = File(directory, "$childPath.png")
            imagePath = directory.path.toString() + "/" + childPath + ".png"
            var taskCompleted = false
            try {
                var fos: FileOutputStream? = null
                try {
                    fos = FileOutputStream(myPath)
                    bitmapImage.compress(Bitmap.CompressFormat.JPEG, 50, fos)
                    fos.flush()
                    taskCompleted = true
                } catch (e: Exception) {
                    e.printStackTrace()
                    taskCompleted = false
                } finally {
                    try {
                        assert(fos != null)
                        fos!!.close()

                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
                return@withContext taskCompleted
            } catch (e: Exception) {
                return@withContext taskCompleted
            }
        }

    fun hideBottomNav(isShowBottom: Boolean) {
        if (isShowBottom) {
            binding.bottomNav.visibility = VISIBLE
            binding.fab.visibility = VISIBLE
            binding.toolbar.visibility = VISIBLE
        } else {
            binding.bottomNav.visibility = GONE
            binding.fab.visibility = GONE
            binding.toolbar.visibility = GONE
        }
    }

    @SuppressLint("SetTextI18n")
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

    @SuppressLint("SetTextI18n")
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

        binding.fab.setImageResource(R.drawable.ic_camera)
        binding.fab.visibility = VISIBLE

        binding.fabAddStatus.visibility = VISIBLE

        binding.imgChats.setImageResource(R.drawable.ic_chat)
        binding.imgStatus.setImageResource(R.drawable.ic_status_tinted)
        binding.imgCommunity.setImageResource(R.drawable.ic_communities)
        binding.imgCalls.setImageResource(R.drawable.ic_phone)
    }

    @SuppressLint("SetTextI18n")
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

    @SuppressLint("SetTextI18n")
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