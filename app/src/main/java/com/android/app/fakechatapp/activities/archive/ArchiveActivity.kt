package com.android.app.fakechatapp.activities.archive

import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.app.fakechatapp.R
import com.android.app.fakechatapp.adapters.UserAdapter
import com.android.app.fakechatapp.database.Database
import com.android.app.fakechatapp.databinding.ActivityArchiveBinding
import com.android.app.fakechatapp.models.User
import com.android.app.fakechatapp.utils.RecyclerItemClickListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ArchiveActivity : AppCompatActivity() {
    private lateinit var binding: ActivityArchiveBinding
    private lateinit var imgBack: ImageView
    private lateinit var toolbarTitle: TextView
    private lateinit var database: Database
    private lateinit var userAdapter: UserAdapter
    private lateinit var viewModel: ArchiveViewModel
    private var dialogShown = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_archive)

        viewModel = ArchiveViewModel(applicationContext)
        binding.lifecycleOwner = this

        imgBack = findViewById(R.id.imgBack)
        toolbarTitle = findViewById(R.id.toolbarTitle)

        imgBack.setOnClickListener { onBackPressed() }
        toolbarTitle.text = "Archived Chats"

        database = Database(applicationContext)
        binding.usersRv.layoutManager = LinearLayoutManager(applicationContext)
        binding.usersRv.setHasFixedSize(true)
        userAdapter = UserAdapter(this)
        binding.usersRv.adapter = userAdapter
    }

    override fun onStart() {
        super.onStart()
        getDataAndSetAdapter()
    }

    private fun getDataAndSetAdapter() {
        CoroutineScope(Dispatchers.IO).launch {
            val users = withContext(Dispatchers.IO) {
                database.getAllUsers(1)
            }

            withContext(Dispatchers.Main) {
                if (users != null)
                    userAdapter.submitList(users)

                binding.usersRv.addOnItemTouchListener(
                    RecyclerItemClickListener(
                        applicationContext,
                        binding.usersRv,
                        object : RecyclerItemClickListener.OnItemClickListener {
                            override fun onItemClick(view: View?, position: Int) {}

                            override fun onItemLongClick(view: View?, position: Int) {
                                val selectedUser = users[position]
                                showCustomDialog(selectedUser)
                                dialogShown = true
                            }
                        }
                    )
                )
            }
        }
    }

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
            database.updateUserArchiveStatus(selectedUser.userId, 0)
            getDataAndSetAdapter()
            dialog.dismiss()
        }
        delete.setOnClickListener {
            database.deleteChat(selectedUser.userId)
            getDataAndSetAdapter()
            dialog.dismiss()
        }
        dialog.setOnDismissListener { dialogShown = false }
        dialog.show()
    }
}