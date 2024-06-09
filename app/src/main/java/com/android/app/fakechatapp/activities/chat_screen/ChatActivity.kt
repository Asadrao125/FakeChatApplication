package com.android.app.fakechatapp.activities.chat_screen

import android.app.AlertDialog
import android.app.Dialog
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.Window
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.app.fakechatapp.R
import com.android.app.fakechatapp.activities.call.audio.CallActivity
import com.android.app.fakechatapp.activities.call.video.VideoCallActivity
import com.android.app.fakechatapp.activities.profile.ProfileActivity
import com.android.app.fakechatapp.activities.userprofile.UserProfileActivity
import com.android.app.fakechatapp.activities.viewimage.ImageViewActivity
import com.android.app.fakechatapp.adapters.ChatListAdapter
import com.android.app.fakechatapp.database.Database
import com.android.app.fakechatapp.databinding.ActivityChatBinding
import com.android.app.fakechatapp.models.Call
import com.android.app.fakechatapp.models.Chat
import com.android.app.fakechatapp.models.User
import com.android.app.fakechatapp.utils.Constants
import com.android.app.fakechatapp.utils.Constants.Companion.CAPTURE_IMAGE_REQ_CODE
import com.android.app.fakechatapp.utils.Constants.Companion.CHAT_VIDEO_REQ_CODE
import com.android.app.fakechatapp.utils.Constants.Companion.FILE_REQ_CODE
import com.android.app.fakechatapp.utils.Constants.Companion.IMAGE_REQ_CODE
import com.android.app.fakechatapp.utils.Constants.Companion.VIDEO_REQ_CODE
import com.android.app.fakechatapp.utils.RecyclerItemClickListener
import com.android.app.fakechatapp.utils.SharedPref
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private lateinit var chatViewModel: ChatActivityViewModel
    private lateinit var adapter: ChatListAdapter
    private lateinit var database: Database
    private lateinit var user: User
    private lateinit var sharedPref: SharedPref
    private var videoPath: String = ""
    private var imagePath = ""
    private var filePath = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat)

        chatViewModel = ChatActivityViewModel(applicationContext)
        binding.mainViewModel = chatViewModel

        database = Database(applicationContext)
        sharedPref = SharedPref(this)

        binding.tvUsername.text = intent.getStringExtra("user_name")

        if (intent.getStringExtra("last_seen")!!.contains("Hide last seen"))
            binding.tvLastSeen.visibility = GONE
        else binding.tvLastSeen.visibility = VISIBLE

        binding.tvLastSeen.text = intent.getStringExtra("last_seen")

        user = database.getSingleUser(intent.getIntExtra("user_id", 0))

        Picasso.get().load(File(user.profileImage)).placeholder(R.drawable.ic_user)
            .into(binding.profilePic)

        adapter = ChatListAdapter(this)

        val linearLayoutManager = LinearLayoutManager(this)
        binding.chatRecyclerview.layoutManager = linearLayoutManager
        linearLayoutManager.stackFromEnd = true
        binding.chatRecyclerview.adapter = adapter

        binding.etMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val newText = s.toString()
                if (newText.isNotEmpty()) {
                    binding.imgSender.visibility = VISIBLE
                    binding.imgReciever.visibility = VISIBLE
                    binding.voiceLayout.visibility = GONE
                } else {
                    binding.imgSender.visibility = GONE
                    binding.imgReciever.visibility = GONE
                    binding.voiceLayout.visibility = VISIBLE
                }
            }

            override fun afterTextChanged(p0: Editable?) {}
        })

        binding.imgCamera.setOnClickListener { cameraIntent() }

        binding.imgBack.setOnClickListener { onBackPressed() }

        binding.imgSender.setOnClickListener {
            database.insertChat(
                Chat(
                    messageId = 0,
                    receiverName = "You",
                    message = binding.etMessage.text.toString().trim(),
                    time = chatViewModel.getCurrentTime(),
                    senderId = user.userId,
                    receiverId = user.userId,
                    viewType = 1,
                    date = chatViewModel.getCurrentDate()
                )
            )
            database.updateLastMessageAndTime(
                user.userId,
                binding.etMessage.text.toString().trim(),
                chatViewModel.getCurrentTime()
            )
            setChatsData()
        }

        binding.imgReciever.setOnClickListener {
            database.insertChat(
                Chat(
                    messageId = 0,
                    receiverName = user.name,
                    message = binding.etMessage.text.toString().trim(),
                    time = chatViewModel.getCurrentTime(),
                    senderId = user.userId,
                    receiverId = user.userId,
                    viewType = 2,
                    date = chatViewModel.getCurrentDate()
                )
            )
            database.updateLastMessageAndTime(
                user.userId,
                binding.etMessage.text.toString().trim(),
                chatViewModel.getCurrentTime()
            )
            setChatsData()
        }

        binding.imgAudioCall.setOnClickListener {
            val callId = database.insertCall(
                Call(
                    callId = 0,
                    callReceiverId = user.userId,
                    userName = user.name,
                    callDirection = "OUTGOING",
                    date = chatViewModel.getCurrentDate(),
                    time = chatViewModel.getCurrentTime(),
                    callType = "AUDIO",
                    callDuration = "0",
                    callProfileImage = user.profileImage
                )
            )
            startActivity(
                Intent(this, CallActivity::class.java)
                    .putExtra("user_name", user.name)
                    .putExtra("call_id", callId)
                    .putExtra("user_id", user.userId)
            )
        }

        binding.imgVideoCall.setOnClickListener {
            val res = sharedPref.read(Constants.VIDEO_PATH, "No")
            if (res == "No") {
                val intent = Intent()
                intent.type = "video/*"
                intent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(
                    Intent.createChooser(intent, "Select Video"),
                    VIDEO_REQ_CODE
                )
            } else startVideoCall()
        }

        binding.name.setOnClickListener {
            startActivity(
                Intent(
                    applicationContext,
                    UserProfileActivity::class.java
                ).putExtra("user_id", user.userId)
            )
        }

        binding.imgAttachment.setOnClickListener {
            showSendReceiveDialog()
        }

        setChatsData()

        binding.imgMenu.setOnClickListener {
            showChatMenuDialog()
        }
    }

    private fun startVideoCall() {
        val callId = database.insertCall(
            Call(
                callId = 0,
                callReceiverId = user.userId,
                userName = user.name,
                callDirection = "OUTGOING",
                date = chatViewModel.getCurrentDate(),
                time = chatViewModel.getCurrentTime(),
                callType = "VIDEO",
                callDuration = "0",
                callProfileImage = user.profileImage
            )
        )
        startActivity(
            Intent(this, VideoCallActivity::class.java)
                .putExtra("user_name", user.name)
                .putExtra("call_id", callId)
                .putExtra("user_id", user.userId)
        )
    }

    private fun setChatsData() {
        binding.etMessage.setText("")
        val chats = database.getUserChats(intent.getIntExtra("user_id", 0))
        if (chats != null) {
            adapter.setData(chats)
        } else binding.chatRecyclerview.visibility = GONE
    }

    fun scrollToBottom() {
        binding.chatRecyclerview.scrollToPosition(adapter.itemCount - 1)
    }

    private fun showChatMenuDialog() {
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.inner_chat_menu_dialog_layout, null)
        val popupWindow = PopupWindow(view, 500, ConstraintLayout.LayoutParams.WRAP_CONTENT, true)
        popupWindow.showAsDropDown(binding.imgMenu, 0, 0)
        val tvClearChat = view.findViewById<TextView>(R.id.tvClearChat)

        tvClearChat.setOnClickListener {
            popupWindow.dismiss()
            database.deleteChat(user.userId)
            setChatsData()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == VIDEO_REQ_CODE) {
                val selectedImageUri = data?.data
                val selectedVideoPath = getPath(selectedImageUri)
                if (selectedVideoPath != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        saveVideoToCache(File(selectedVideoPath))
                    }
                    startVideoCall()
                }
            } else if (requestCode == IMAGE_REQ_CODE) {
                try {
                    val bmp =
                        BitmapFactory.decodeStream(contentResolver.openInputStream(data?.data!!))
                    CoroutineScope(Dispatchers.IO).launch {
                        saveImageToCache(bmp)
                    }
                    saveImageMessage()
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }
            } else if (requestCode == CAPTURE_IMAGE_REQ_CODE) {
                try {
                    val bmp = data!!.extras!!["data"] as Bitmap
                    CoroutineScope(Dispatchers.IO).launch {
                        saveImageToCache(bmp)
                    }
                    saveImageMessage()
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }
            } else if (requestCode == CHAT_VIDEO_REQ_CODE) {
                val selectedImageUri = data?.data
                val selectedVideoPath = getPath(selectedImageUri)
                if (selectedVideoPath != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        saveChatVideoToCache(File(selectedVideoPath))
                    }
                    saveChatVideoMessage()
                }
            } else if (requestCode == FILE_REQ_CODE) {
                /*val selectedFileUri = data?.data
                val selectedFilePath = getPathFromUri(selectedFileUri!!)
                if (selectedFilePath != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        saveFileToCache(File(selectedFilePath))
                    }
                    saveFileMessage()
                }*/
            }
        }
    }

    private fun saveImageMessage() {
        database.insertChat(
            Chat(
                messageId = 0,
                receiverName = user.name,
                message = "Image",
                time = chatViewModel.getCurrentTime(),
                senderId = user.userId,
                receiverId = user.userId,
                viewType = if (isMyMessage) 4 else 3,
                date = chatViewModel.getCurrentDate(),
                imagePath = imagePath,
                filePath = ""
            )
        )
        database.updateLastMessageAndTime(
            user.userId,
            "Image",
            chatViewModel.getCurrentTime()
        )
        setChatsData()
    }

    private fun saveChatVideoMessage() {
        database.insertChat(
            Chat(
                messageId = 0,
                receiverName = user.name,
                message = "Video",
                time = chatViewModel.getCurrentTime(),
                senderId = user.userId,
                receiverId = user.userId,
                viewType = if (isMyMessage) 7 else 8,
                date = chatViewModel.getCurrentDate(),
                imagePath = imagePath,
                filePath = videoPath
            )
        )
        database.updateLastMessageAndTime(
            user.userId,
            "Video",
            chatViewModel.getCurrentTime()
        )
        setChatsData()
    }

    private fun saveFileMessage() {
        database.insertChat(
            Chat(
                messageId = 0,
                receiverName = user.name,
                message = "File",
                time = chatViewModel.getCurrentTime(),
                senderId = user.userId,
                receiverId = user.userId,
                viewType = 6,
                date = chatViewModel.getCurrentDate(),
                imagePath = "",
                filePath = filePath
            )
        )
        database.updateLastMessageAndTime(
            user.userId,
            "File",
            chatViewModel.getCurrentTime()
        )
        setChatsData()
    }

    override fun onResume() {
        super.onResume()
        setChatsData()
    }

    private fun getPath(uri: Uri?): String? {
        val projection = arrayOf(MediaStore.Video.Media.DATA)
        val cursor = contentResolver.query(uri!!, projection, null, null, null)
        return if (cursor != null) {
            val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            cursor.moveToFirst()
            cursor.getString(columnIndex)
        } else null
    }

    private suspend fun saveImageToCache(bitmapImage: Bitmap): Boolean =
        withContext(Dispatchers.IO) {
            val cw = ContextWrapper(applicationContext)
            val directory = cw.getDir("chat_images", MODE_PRIVATE)
            if (!directory.exists()) {
                directory.mkdir()
            }
            val childPath = "FTG_CHAT_IMAGE" + System.currentTimeMillis()
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

    private suspend fun saveVideoToCache(file: File): Boolean =
        withContext(Dispatchers.IO) {
            val cw = ContextWrapper(applicationContext)
            val directory = cw.getDir("media", MODE_PRIVATE)
            if (!directory.exists()) {
                directory.mkdir()
            }
            val childPath = "ftg_video"
            val myPath = File(directory, "$childPath.${getFileExtension(file)}")
            videoPath = directory.path.toString() + "/" + "$childPath.${getFileExtension(file)}"
            sharedPref.write(Constants.VIDEO_PATH, videoPath)

            var taskCompleted = false
            try {
                var fis: FileInputStream? = null
                var fos: FileOutputStream? = null
                try {
                    fis = FileInputStream(file)
                    fos = FileOutputStream(myPath)
                    val buffer = ByteArray(1024)
                    var length: Int
                    while (fis.read(buffer).also { length = it } > 0) {
                        fos.write(buffer, 0, length)
                    }
                    fos.flush()
                    taskCompleted = true
                } catch (e: Exception) {
                    e.printStackTrace()
                    taskCompleted = false
                } finally {
                    fis?.close()
                    fos?.close()
                }
                return@withContext taskCompleted
            } catch (e: Exception) {
                return@withContext taskCompleted
            }
        }

    private suspend fun saveFileToCache(file: File): Boolean =
        withContext(Dispatchers.IO) {
            val cw = ContextWrapper(applicationContext)
            val directory = cw.getDir("chat_files", MODE_PRIVATE)
            if (!directory.exists()) {
                directory.mkdir()
            }
            val childPath = "FTG_CHAT_FILE_" + System.currentTimeMillis()
            val myPath = File(directory, "$childPath.${getFileExtension(file)}")
            filePath = directory.path.toString() + "/" + "$childPath.${getFileExtension(file)}"
            Log.d("chat_file2", "saveFileToCache: $filePath")
            var taskCompleted = false
            try {
                var fis: FileInputStream? = null
                var fos: FileOutputStream? = null
                try {
                    fis = FileInputStream(file)
                    fos = FileOutputStream(myPath)
                    val buffer = ByteArray(1024)
                    var length: Int
                    while (fis.read(buffer).also { length = it } > 0) {
                        fos.write(buffer, 0, length)
                    }
                    fos.flush()
                    taskCompleted = true
                } catch (e: Exception) {
                    e.printStackTrace()
                    taskCompleted = false
                } finally {
                    fis?.close()
                    fos?.close()
                }
                return@withContext taskCompleted
            } catch (e: Exception) {
                return@withContext taskCompleted
            }
        }

    private suspend fun saveChatVideoToCache(file: File): Boolean =
        withContext(Dispatchers.IO) {
            val cw = ContextWrapper(applicationContext)
            val directory = cw.getDir("chat_video", MODE_PRIVATE)
            if (!directory.exists()) {
                directory.mkdir()
            }
            val childPath = "FTG_VIDEO_" + System.currentTimeMillis()
            val myPath = File(directory, "$childPath.${getFileExtension(file)}")
            videoPath = directory.path.toString() + "/" + "$childPath.${getFileExtension(file)}"

            var taskCompleted = false
            try {
                var fis: FileInputStream? = null
                var fos: FileOutputStream? = null
                try {
                    fis = FileInputStream(file)
                    fos = FileOutputStream(myPath)
                    val buffer = ByteArray(1024)
                    var length: Int
                    while (fis.read(buffer).also { length = it } > 0) {
                        fos.write(buffer, 0, length)
                    }
                    fos.flush()
                    taskCompleted = true
                } catch (e: Exception) {
                    e.printStackTrace()
                    taskCompleted = false
                } finally {
                    fis?.close()
                    fos?.close()
                }
                return@withContext taskCompleted
            } catch (e: Exception) {
                return@withContext taskCompleted
            }
        }

    private fun getFileExtension(file: File): String {
        val fileName = file.name
        val dotIndex = fileName.lastIndexOf('.')
        if (dotIndex != -1 && dotIndex < fileName.length - 1) {
            return fileName.substring(dotIndex + 1)
        }
        return ""
    }

    private fun showAttachmentDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_attachment_layout)
        val videoLayout = dialog.findViewById<LinearLayout>(R.id.videoLayout)
        val cameraLayout = dialog.findViewById<LinearLayout>(R.id.cameraLayout)
        val galleryLayout = dialog.findViewById<LinearLayout>(R.id.galleryLayout)

        dialog.window?.setBackgroundDrawableResource(R.drawable.chatbg)
        dialog.window?.setDimAmount(0F)

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenWidth = displayMetrics.widthPixels
        val dialogWidth = (screenWidth * 0.9).toInt()

        val screenHeight = displayMetrics.widthPixels
        val dialogHeight = (screenHeight * 0.3).toInt()

        dialog.window?.setLayout(
            dialogWidth,
            dialogHeight
        )
        val imageViewLocation = IntArray(2)
        binding.mainLayout.getLocationInWindow(imageViewLocation)
        val imageViewX = imageViewLocation[0]
        val imageViewY = imageViewLocation[1]
        val dialogY = imageViewY - dialogHeight - binding.mainLayout.height
        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(dialog.window?.attributes)
        layoutParams.width = dialogWidth
        layoutParams.height = dialogHeight
        layoutParams.gravity = Gravity.TOP or Gravity.CENTER
        layoutParams.x = imageViewX
        layoutParams.y = dialogY
        dialog.window?.attributes = layoutParams

        videoLayout.setOnClickListener {
            videoIntent()
            dialog.dismiss()
        }

        cameraLayout.setOnClickListener {
            cameraIntent()
            dialog.dismiss()
        }

        galleryLayout.setOnClickListener {
            galleryIntent()
            dialog.dismiss()
        }
        dialog.show()
    }

    private var isMyMessage: Boolean = true

    private fun showSendReceiveDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_send_and_recieve_layout)
        val receiveLayout = dialog.findViewById<LinearLayout>(R.id.receiveLayout)
        val sendLayout = dialog.findViewById<LinearLayout>(R.id.sendLayout)

        dialog.window?.setBackgroundDrawableResource(R.drawable.chatbg)
        dialog.window?.setDimAmount(0F)

        receiveLayout.setOnClickListener {
            isMyMessage = false
            showAttachmentDialog()
            dialog.dismiss()
        }

        sendLayout.setOnClickListener {
            isMyMessage = true
            showAttachmentDialog()
            dialog.dismiss()
        }

        dialog.show()

        val window = dialog.window
        val layoutParams = window?.attributes
        layoutParams?.width = (resources.displayMetrics.widthPixels * 0.9).toInt()
        window?.attributes = layoutParams
    }

    private fun galleryIntent() {
        val photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type = "image/*"
        startActivityForResult(photoPickerIntent, IMAGE_REQ_CODE)
    }

    private fun cameraIntent() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAPTURE_IMAGE_REQ_CODE)
    }

    private fun videoIntent() {
        val intent = Intent()
        intent.type = "video/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(
            Intent.createChooser(intent, "Select Video"),
            CHAT_VIDEO_REQ_CODE
        )
    }
}