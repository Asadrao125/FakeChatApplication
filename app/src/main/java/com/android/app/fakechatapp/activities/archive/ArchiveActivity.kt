package com.android.app.fakechatapp.activities.archive

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.app.fakechatapp.R
import com.android.app.fakechatapp.activities.chat_screen.ChatActivity
import com.android.app.fakechatapp.activities.viewimage.ImageViewActivity
import com.android.app.fakechatapp.adapters.UserAdapter
import com.android.app.fakechatapp.database.MyDatabase
import com.android.app.fakechatapp.databinding.ActivityArchiveBinding
import com.android.app.fakechatapp.models.User
import com.android.app.fakechatapp.utils.RecyclerItemClickListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ArchiveActivity : AppCompatActivity(), UserAdapter.OnItemClickListener, UserAdapter.OnImageClickListener,
    UserAdapter.OnItemLongClickListener {
    private lateinit var binding: ActivityArchiveBinding
    private lateinit var imgBack: ImageView
    private lateinit var toolbarTitle: TextView
    private lateinit var db: MyDatabase
    private lateinit var userAdapter: UserAdapter
    private lateinit var viewModel: ArchiveViewModel
    private var dialogShown = false

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_archive)

        viewModel = ArchiveViewModel(applicationContext)
        binding.lifecycleOwner = this

        imgBack = findViewById(R.id.imgBack)
        toolbarTitle = findViewById(R.id.toolbarTitle)

        imgBack.setOnClickListener { onBackPressed() }
        toolbarTitle.text = "Archived Chats"

        db = MyDatabase(applicationContext)
        binding.usersRv.layoutManager = LinearLayoutManager(applicationContext)
        binding.usersRv.setHasFixedSize(true)
        userAdapter = UserAdapter(this)
        binding.usersRv.adapter = userAdapter

        userAdapter.setOnItemClickListener(this)
        userAdapter.setOnImageClickListener(this)
        userAdapter.setOnItemLongClickListener(this)
    }

    override fun onStart() {
        super.onStart()
        getDataAndSetAdapter()
    }

    private fun getDataAndSetAdapter() {
        CoroutineScope(Dispatchers.IO).launch {
            val users = withContext(Dispatchers.IO) {
                db.getAllUsers(1)
            }

            withContext(Dispatchers.Main) {
                userAdapter.submitList(users)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showCustomDialog(selectedUser: User) {
        val dialogView =
            LayoutInflater.from(this).inflate(R.layout.popup_layout, null)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
        val dialog = builder.create()
        val archive = dialogView.findViewById<LinearLayout>(R.id.archive)
        val tvArchive = dialogView.findViewById<TextView>(R.id.tvArchive)
        val delete = dialogView.findViewById<LinearLayout>(R.id.delete)
        val tvUsername = dialogView.findViewById<TextView>(R.id.tvUsername)

        tvArchive.text = "Un-Archive"
        tvUsername.text = selectedUser.name

        archive.setOnClickListener {
            db.updateUserArchiveStatus(selectedUser.userId, 0)
            getDataAndSetAdapter()
            dialog.dismiss()
        }
        delete.setOnClickListener {
            db.deleteChat(selectedUser.userId)
            db.deleteUser(selectedUser.userId)
            getDataAndSetAdapter()
            dialog.dismiss()
        }
        dialog.setOnDismissListener { dialogShown = false }
        dialog.show()
    }

    override fun onItemClick(user: User) {
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("user_id", user.userId)
        intent.putExtra("user_name", user.name)
        intent.putExtra("last_seen", user.lastSeen)
        intent.putExtra("date", user.date)
        intent.putExtra("enc_text", user.encryptedText)
        startActivity(intent)
    }

    override fun onImageClick(user: User) {
        startActivity(
            Intent(this, ImageViewActivity::class.java)
                .putExtra("image_path", user.profileImage)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        )
    }

    override fun onItemLongClick(user: User) {
        if (!dialogShown) {
            showCustomDialog(user)
            dialogShown = true
        }
    }
}