package com.android.app.fakechatapp.activities.add_status

import android.app.Dialog
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.app.fakechatapp.R
import com.android.app.fakechatapp.adapters.SelectUserAdapter
import com.android.app.fakechatapp.database.MyDatabase
import com.android.app.fakechatapp.databinding.ActivityAddStatusBinding
import com.android.app.fakechatapp.models.Status
import com.android.app.fakechatapp.models.User
import com.android.app.fakechatapp.utils.Constants
import com.android.app.fakechatapp.utils.RecyclerItemClickListener

class AddStatusActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddStatusBinding
    private lateinit var db: MyDatabase
    private lateinit var user: User
    private lateinit var viewModel: AddStatusViewModel
    private var color: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_status)

        db = MyDatabase(applicationContext)
        viewModel = AddStatusViewModel(applicationContext)

        window.statusBarColor = ContextCompat.getColor(this, R.color.whatsapp_green)

        color = R.color.r_15

        binding.imgColorPalette.setOnClickListener {
            color = Constants.getColorList(applicationContext)[Constants.getRandomColor(
                applicationContext
            )]
            binding.rootLayout.setBackgroundColor(color)
            window.statusBarColor = color
        }

        binding.imgCross.setOnClickListener { onBackPressed() }

        binding.imgStatus.setOnClickListener {
            val text = binding.etStatus.text.trim().toString()
            if (text.isNotEmpty()) {
                showDialog(text)
            }
        }
    }

    private fun showDialog(text: String) {
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
                        val selectedUser = users[position]
                        user = selectedUser
                        addStatus(text)
                        dialog.dismiss()
                    }

                    override fun onItemLongClick(view: View?, position: Int) {
                        val selectedUser = users[position]
                        user = selectedUser
                        addStatus(text)
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

    private fun addStatus(text: String) {
        db.insertStatus(
            Status(
                statusId = 0,
                statusUploaderId = user.userId,
                userName = user.name,
                statusMessage = text,
                date = viewModel.getCurrentDate(),
                time = viewModel.getCurrentTime(),
                isSeen = 0,
                statusUploaderProfile = user.profileImage,
                statusColor = color,
                isMute = 0,
                statusType = 1,
                imagePath = ""
            )
        )
        onBackPressed()
    }
}