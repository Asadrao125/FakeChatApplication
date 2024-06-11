package com.android.app.fakechatapp.bottomnav.chats

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.app.fakechatapp.MainActivity
import com.android.app.fakechatapp.R
import com.android.app.fakechatapp.adapters.UserAdapter
import com.android.app.fakechatapp.activities.archive.ArchiveActivity
import com.android.app.fakechatapp.activities.chat_screen.ChatActivity
import com.android.app.fakechatapp.activities.viewimage.ImageViewActivity
import com.android.app.fakechatapp.database.Database
import com.android.app.fakechatapp.databinding.FragmentChatsBinding
import com.android.app.fakechatapp.models.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatsFragment : Fragment(), UserAdapter.OnItemClickListener, UserAdapter.OnImageClickListener,
    UserAdapter.OnItemLongClickListener {
    private lateinit var binding: FragmentChatsBinding
    private lateinit var chatViewModel: ChatViewModel
    private lateinit var database: Database
    private lateinit var userAdapter: UserAdapter
    private var dialogShown = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatsBinding.inflate(inflater, container, false)

        chatViewModel = ChatViewModel(requireContext())
        binding.lifecycleOwner = this

        database = Database(requireContext())
        binding.usersRv.layoutManager = LinearLayoutManager(context)
        binding.usersRv.setHasFixedSize(true)
        userAdapter = UserAdapter(requireContext())

        userAdapter.setOnItemClickListener(this)
        userAdapter.setOnImageClickListener(this)
        userAdapter.setOnItemLongClickListener(this)

        binding.usersRv.adapter = userAdapter
        binding.edtSearch.isFocusableInTouchMode = true

        binding.archiveLayout.setOnClickListener {
            startActivity(Intent(requireContext(), ArchiveActivity::class.java))
        }

        binding.edtSearch.setOnTouchListener { _, event ->
            (context as MainActivity).hideBottomNav(isShowBottom = false)
            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP -> {
                    binding.searchIcon.setImageResource(R.drawable.ic_back)
                    showKeyboard()
                    true
                }

                else -> false
            }
        }

        binding.searchIcon.setOnClickListener {
            setInitialSearchState()
        }

        binding.edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                userAdapter.filter(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        return binding.root
    }

    private fun setInitialSearchState() {
        if (binding.edtSearch.hasFocus()) {
            hideKeyboard()
            binding.edtSearch.clearFocus()
            binding.edtSearch.setText("")
            binding.searchIcon.setImageResource(R.drawable.ic_search)
            (context as MainActivity).hideBottomNav(isShowBottom = true)
        }
    }

    private fun showKeyboard() {
        val inputMethodManager =
            binding.edtSearch.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        binding.edtSearch.requestFocus()
        inputMethodManager.showSoftInput(binding.edtSearch, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideKeyboard() {
        val inputMethodManager =
            requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        val currentFocus = activity?.currentFocus
        if (currentFocus != null) {
            inputMethodManager.hideSoftInputFromWindow(currentFocus.windowToken, 0)
        }
    }

    override fun onStart() {
        super.onStart()
        getDataAndSetAdapter()
    }

    private fun getDataAndSetAdapter() {
        CoroutineScope(Dispatchers.IO).launch {
            val users = withContext(Dispatchers.IO) {
                database.getAllUsers(0)
            }

            val count = withContext(Dispatchers.IO) {
                database.getArchiveCount(1)
            }

            withContext(Dispatchers.Main) {
                if (users != null)
                    userAdapter.submitList(users)
                binding.tvArchive.text = count.toString()
            }
        }
    }

    private fun showCustomDialog(selectedUser: User) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.popup_layout, null)
        val builder = AlertDialog.Builder(requireContext())
        builder.setView(dialogView)
        val dialog = builder.create()
        val archive = dialogView.findViewById<LinearLayout>(R.id.archive)
        val delete = dialogView.findViewById<LinearLayout>(R.id.delete)
        val tvUsername = dialogView.findViewById<TextView>(R.id.tvUsername)

        tvUsername.text = selectedUser.name

        archive.setOnClickListener {
            database.updateUserArchiveStatus(selectedUser.userId, 1)
            getDataAndSetAdapter()
            dialog.dismiss()
        }
        delete.setOnClickListener {
            database.deleteChat(selectedUser.userId)
            database.deleteUser(selectedUser.userId)
            getDataAndSetAdapter()
            dialog.dismiss()
        }

        dialog.setOnDismissListener { dialogShown = false }
        dialog.show()
    }

    override fun onResume() {
        super.onResume()
        setInitialSearchState()
    }

    override fun onItemClick(user: User) {
        val intent = Intent(context, ChatActivity::class.java)
        intent.putExtra("user_id", user.userId)
        intent.putExtra("user_name", user.name)
        intent.putExtra("last_seen", user.lastSeen)
        intent.putExtra("date", user.date)
        intent.putExtra("enc_text", user.encryptedText)
        startActivity(intent)
    }

    override fun onImageClick(user: User) {
        startActivity(
            Intent(context, ImageViewActivity::class.java)
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