package com.android.app.fakechatapp.activities.chat_screen

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.Window
import android.view.WindowManager
import android.webkit.MimeTypeMap
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.app.fakechatapp.R
import com.android.app.fakechatapp.activities.call.audio.CallActivity
import com.android.app.fakechatapp.activities.call.video.VideoCallActivity
import com.android.app.fakechatapp.activities.userprofile.UserProfileActivity
import com.android.app.fakechatapp.adapters.ChatListAdapter
import com.android.app.fakechatapp.database.MyDatabase
import com.android.app.fakechatapp.databinding.ActivityChatBinding
import com.android.app.fakechatapp.models.Call
import com.android.app.fakechatapp.models.Chat
import com.android.app.fakechatapp.models.User
import com.android.app.fakechatapp.utils.Constants
import com.android.app.fakechatapp.utils.Constants.Companion.CAMERA_PERMISSION_CODE_CAPTURE_IMAGE
import com.android.app.fakechatapp.utils.Constants.Companion.CAMERA_PERMISSION_CODE_VIDEO
import com.android.app.fakechatapp.utils.Constants.Companion.CAPTURE_IMAGE_REQ_CODE
import com.android.app.fakechatapp.utils.Constants.Companion.CHAT_VIDEO_REQ_CODE
import com.android.app.fakechatapp.utils.Constants.Companion.FILE_REQ_CODE
import com.android.app.fakechatapp.utils.Constants.Companion.IMAGE_REQ_CODE
import com.android.app.fakechatapp.utils.Constants.Companion.VIDEO_REQ_CODE
import com.android.app.fakechatapp.utils.Constants.Companion.showToast
import com.android.app.fakechatapp.utils.SharedPref
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private lateinit var chatViewModel: ChatActivityViewModel
    private lateinit var adapter: ChatListAdapter
    private lateinit var db: MyDatabase
    private lateinit var user: User
    private lateinit var sharedPref: SharedPref
    private var videoPath: String = ""
    private var imagePath = ""
    private var filePath = ""
    private var isMyMessage: Boolean = true
    private var scrollState: Parcelable? = null

    private lateinit var videoPickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var videoCallPickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private lateinit var pdfPickerLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat)

        registerLaunchers()

        chatViewModel = ChatActivityViewModel(applicationContext)
        binding.mainViewModel = chatViewModel

        db = MyDatabase(applicationContext)
        sharedPref = SharedPref(this)

        binding.tvUsername.text = intent.getStringExtra("user_name")

        if (intent.getStringExtra("last_seen")!!.contains("Hide last seen"))
            binding.tvLastSeen.visibility = GONE
        else binding.tvLastSeen.visibility = VISIBLE

        binding.tvLastSeen.text = intent.getStringExtra("last_seen")

        user = db.getSingleUser(intent.getIntExtra("user_id", 0))

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

        binding.imgCamera.setOnClickListener { showSendReceiveDialog(openDirectCam = true) }

        binding.imgBack.setOnClickListener { onBackPressed() }

        binding.imgSender.setOnClickListener {
            val chat = Chat(
                messageId = 0,
                receiverName = "You",
                message = binding.etMessage.text.toString().trim(),
                time = chatViewModel.getCurrentTime(),
                senderId = user.userId,
                receiverId = user.userId,
                viewType = 1,
                date = chatViewModel.getCurrentDate()
            )
            db.insertChat(chat)
            adapter.addChat(chat)

            db.updateLastMessageAndTime(
                user.userId,
                binding.etMessage.text.toString().trim(),
                chatViewModel.getCurrentTime()
            )

            binding.etMessage.setText("")
        }

        binding.imgReciever.setOnClickListener {
            val chat = Chat(
                messageId = 0,
                receiverName = user.name,
                message = binding.etMessage.text.toString().trim(),
                time = chatViewModel.getCurrentTime(),
                senderId = user.userId,
                receiverId = user.userId,
                viewType = 2,
                date = chatViewModel.getCurrentDate()
            )
            db.insertChat(chat)
            adapter.addChat(chat)

            db.updateLastMessageAndTime(
                user.userId,
                binding.etMessage.text.toString().trim(),
                chatViewModel.getCurrentTime()
            )
            binding.etMessage.setText("")
        }

        binding.imgAudioCall.setOnClickListener {
            val callId = db.insertCall(
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
                    .putExtra("profile_path", user.profileImage)
            )
        }

        binding.imgVideoCall.setOnClickListener {
            requestCameraPermissionVideo()
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
            requestCameraPermissionCaptureImage()
        }

        setChatsData()

        binding.imgMenu.setOnClickListener {
            showChatMenuDialog()
        }
    }

    private fun requestCameraPermissionVideo() {
        if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_CODE_VIDEO
            )
        } else {
            val res = sharedPref.read(Constants.VIDEO_PATH, "No")
            if (res == "No") videoCallPickerIntent()
            else startVideoCall()
        }
    }

    private fun requestCameraPermissionCaptureImage() {
        if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_CODE_CAPTURE_IMAGE
            )
        } else showSendReceiveDialog(openDirectCam = false)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_PERMISSION_CODE_VIDEO -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    val res = sharedPref.read(Constants.VIDEO_PATH, "No")
                    if (res == "No") videoCallPickerIntent()
                    else startVideoCall()
                } else showToast(applicationContext, "Camera permission required")
            }

            CAMERA_PERMISSION_CODE_CAPTURE_IMAGE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    showSendReceiveDialog(openDirectCam = false)
                } else {
                    showToast(applicationContext, "Camera permission required")
                }
            }
        }
    }

    private fun startVideoCall() {
        val callId = db.insertCall(
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
                .putExtra("profile_path", user.profileImage)
        )
    }

    private fun setChatsData() {
        binding.etMessage.setText("")
        val chats = db.getUserChats(intent.getIntExtra("user_id", 0))
        adapter.addChats(chats)
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
            db.deleteChat(user.userId)
            adapter.clearList()
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
                        val success = saveVideoToCache(File(selectedVideoPath))
                        withContext(Dispatchers.Main) {
                            if (success) startVideoCall()
                            else showToast(applicationContext, "Failed to upload video")
                        }
                    }
                }
            } else if (requestCode == IMAGE_REQ_CODE) {
                val imageData = data?.data
                if (imageData != null) {
                    val bmp = BitmapFactory.decodeStream(contentResolver.openInputStream(imageData))
                    CoroutineScope(Dispatchers.IO).launch {
                        val success = saveImageToCache(bmp)
                        withContext(Dispatchers.Main) {
                            if (success) saveImageMessage()
                            else showToast(applicationContext, "Failed to send image")
                        }
                    }
                }
            } else if (requestCode == CAPTURE_IMAGE_REQ_CODE) {
                val imageData = data?.data
                if (imageData != null) {
                    val bmp = BitmapFactory.decodeStream(contentResolver.openInputStream(imageData))
                    CoroutineScope(Dispatchers.IO).launch {
                        val success = saveImageToCache(bmp)
                        withContext(Dispatchers.Main) {
                            if (success) saveImageMessage()
                            else showToast(applicationContext, "Failed to send image")
                        }
                    }
                }
            } else if (requestCode == CHAT_VIDEO_REQ_CODE) {
                val selectedVideoUri = data?.data
                if (selectedVideoUri != null) {
                    val selectedVideoPath = getPath(selectedVideoUri)
                    if (selectedVideoPath != null) {
                        CoroutineScope(Dispatchers.IO).launch {
                            val success = saveChatVideoToCache(File(selectedVideoPath))
                            withContext(Dispatchers.Main) {
                                if (success) saveChatVideoMessage()
                                else showToast(applicationContext, "Failed to send video")
                            }
                        }
                    } else showToast(applicationContext, "Failed to send video")
                } else showToast(applicationContext, "Failed to send video")

            } else if (requestCode == FILE_REQ_CODE) {
                data?.data?.let { uri ->
                    CoroutineScope(Dispatchers.IO).launch {
                        val fileName = getFileName(uri)
                        val success = saveFileToCache(uri)
                        withContext(Dispatchers.Main) {
                            if (success) saveFileMessage(fileName)
                            else showToast(applicationContext, "Failed to send file")
                        }
                    }
                }
            }
        }
    }

    private fun getFileName(uri: Uri): String {
        var fileName: String? = null
        contentResolver?.let { contentResolver ->
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val displayNameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (displayNameIndex != -1) {
                        fileName = it.getString(displayNameIndex)
                    }
                }
            }
        }
        return fileName ?: "File"
    }

    private suspend fun saveFileToCache(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        val cw = ContextWrapper(applicationContext)
        val directory = cw.getDir("chat_files", Context.MODE_PRIVATE)
        if (!directory.exists()) {
            directory.mkdir()
        }
        val childPath = "FTG_CHAT_FILE_" + System.currentTimeMillis()
        val fileExtension = getFileExtension(uri)
        val myPath = File(directory, "$childPath.$fileExtension")
        filePath = "${directory.path}/$childPath.$fileExtension"

        var taskCompleted = false
        try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(myPath).use { outputStream ->
                    val buffer = ByteArray(1024)
                    var length: Int
                    while (inputStream.read(buffer).also { length = it } > 0) {
                        outputStream.write(buffer, 0, length)
                    }
                    outputStream.flush()
                    taskCompleted = true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            taskCompleted = false
        }
        return@withContext taskCompleted
    }

    private fun getFileExtension(uri: Uri): String {
        val mimeType = contentResolver.getType(uri)
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "pdf"
    }

    private fun saveImageMessage() {
        val chat = Chat(
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
        db.insertChat(chat)
        scrollState = null
        adapter.addChat(chat)

        db.updateLastMessageAndTime(
            user.userId,
            "Image",
            chatViewModel.getCurrentTime()
        )
    }

    private fun saveChatVideoMessage() {
        val chat = Chat(
            messageId = 0,
            receiverName = user.name,
            message = "Video",
            time = chatViewModel.getCurrentTime(),
            senderId = user.userId,
            receiverId = user.userId,
            viewType = if (isMyMessage) 7 else 8,
            date = chatViewModel.getCurrentDate(),
            imagePath = "",
            filePath = videoPath
        )
        db.insertChat(chat)
        scrollState = null
        adapter.addChat(chat)

        db.updateLastMessageAndTime(
            user.userId,
            "Video",
            chatViewModel.getCurrentTime()
        )
    }

    private fun saveFileMessage(fileName: String) {
        val chat = Chat(
            messageId = 0,
            receiverName = user.name,
            message = fileName,
            time = chatViewModel.getCurrentTime(),
            senderId = user.userId,
            receiverId = user.userId,
            viewType = if (isMyMessage) 6 else 5,
            date = chatViewModel.getCurrentDate(),
            imagePath = "",
            filePath = filePath
        )
        db.insertChat(chat)
        scrollState = null
        adapter.addChat(chat)

        db.updateLastMessageAndTime(
            user.userId,
            "File",
            chatViewModel.getCurrentTime()
        )
    }

    override fun onResume() {
        super.onResume()
        if (scrollState != null) {
            binding.chatRecyclerview.layoutManager?.onRestoreInstanceState(scrollState)
        }
    }

    @SuppressLint("Recycle")
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
        val fileLayout = dialog.findViewById<LinearLayout>(R.id.fileLayout)

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

        fileLayout.setOnClickListener {
            pdfFilesIntent()
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showSendReceiveDialog(openDirectCam: Boolean) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_send_and_recieve_layout)
        val receiveLayout = dialog.findViewById<LinearLayout>(R.id.receiveLayout)
        val sendLayout = dialog.findViewById<LinearLayout>(R.id.sendLayout)

        dialog.window?.setBackgroundDrawableResource(R.drawable.chatbg)
        dialog.window?.setDimAmount(0F)

        receiveLayout.setOnClickListener {
            isMyMessage = false
            if (openDirectCam) cameraIntent()
            else showAttachmentDialog()
            dialog.dismiss()
        }

        sendLayout.setOnClickListener {
            isMyMessage = true
            if (openDirectCam) cameraIntent()
            else showAttachmentDialog()
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
        galleryLauncher.launch(photoPickerIntent)
    }

    private fun cameraIntent() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(intent)
    }

    private fun videoIntent() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "video/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        videoPickerLauncher.launch(Intent.createChooser(intent, "Select Video"))
    }

    private fun videoCallPickerIntent() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "video/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        videoCallPickerLauncher.launch(Intent.createChooser(intent, "Select Video"))
    }

    private fun allFilesIntent() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        pdfPickerLauncher.launch(Intent.createChooser(intent, "Select a PDF file"))
    }

    private fun pdfFilesIntent() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "application/pdf"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        pdfPickerLauncher.launch(Intent.createChooser(intent, "Select a PDF file"))
    }

    override fun onPause() {
        super.onPause()
        scrollState = binding.chatRecyclerview.layoutManager?.onSaveInstanceState()
    }

    private fun registerLaunchers() {
        pdfPickerLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data = result.data
                    handlePdfResult(data)
                }
            }

        galleryLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data = result.data
                    handleGalleryResult(data)
                }
            }

        cameraLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data = result.data
                    handleCameraResult(data)
                }
            }

        videoPickerLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data = result.data
                    handleVideoResult(data)
                }
            }

        videoCallPickerLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data = result.data
                    handleVideoPickerResult(data)
                }
            }
    }

    private fun handleVideoResult(data: Intent?) {
        val selectedVideoUri = data?.data
        if (selectedVideoUri != null) {
            val selectedVideoPath = getPath(selectedVideoUri)
            if (selectedVideoPath != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    val success = saveChatVideoToCache(File(selectedVideoPath))
                    withContext(Dispatchers.Main) {
                        if (success) saveChatVideoMessage()
                        else showToast(applicationContext, "Failed to send video")
                    }
                }
            } else showToast(applicationContext, "Failed to send video")
        } else showToast(applicationContext, "Failed to send video")
    }

    private fun handleVideoPickerResult(data: Intent?) {
        val selectedVideoUri = data?.data
        if (selectedVideoUri != null) {
            val selectedVideoPath = getPath(selectedVideoUri)
            if (selectedVideoPath != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    val success = saveVideoToCache(File(selectedVideoPath))
                    withContext(Dispatchers.Main) {
                        if (success) startVideoCall()
                        else showToast(applicationContext, "Failed to upload video")
                    }
                }
            } else showToast(applicationContext, "Failed to send video")
        } else showToast(applicationContext, "Failed to send video")
    }

    private fun handleCameraResult(data: Intent?) {
        val extras = data?.extras
        val bmp = extras?.get("data") as? Bitmap
        if (bmp != null) {
            CoroutineScope(Dispatchers.IO).launch {
                val success = saveImageToCache(bmp)
                withContext(Dispatchers.Main) {
                    if (success) saveImageMessage()
                    else showToast(applicationContext, "Failed to send image")
                }
            }
        } else showToast(applicationContext, "Failed to send image")
    }

    private fun handleGalleryResult(data: Intent?) {
        val imageData = data?.data
        if (imageData != null) {
            val bmp = BitmapFactory.decodeStream(contentResolver.openInputStream(imageData))
            CoroutineScope(Dispatchers.IO).launch {
                val success = saveImageToCache(bmp)
                withContext(Dispatchers.Main) {
                    if (success) saveImageMessage()
                    else showToast(applicationContext, "Failed to send image")
                }
            }
        }
    }

    private fun handlePdfResult(data: Intent?) {
        data?.data?.let { uri ->
            CoroutineScope(Dispatchers.IO).launch {
                val fileName = getFileName(uri)
                val success = saveFileToCache(uri)
                withContext(Dispatchers.Main) {
                    if (success) saveFileMessage(fileName)
                    else showToast(applicationContext, "Failed to send file")
                }
            }
        }
    }
}