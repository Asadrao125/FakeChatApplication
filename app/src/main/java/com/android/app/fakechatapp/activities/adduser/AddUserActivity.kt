package com.android.app.fakechatapp.activities.adduser

import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.android.app.fakechatapp.R
import com.android.app.fakechatapp.database.Database
import com.android.app.fakechatapp.databinding.ActivityAddUserBinding
import com.android.app.fakechatapp.models.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

private const val REQ_CODE = 123

class AddUserActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddUserBinding
    private lateinit var addUserViewModel: AddUserViewModel
    private var lastSeen: String = "Hide last seen"
    private lateinit var database: Database
    private lateinit var imgBack: ImageView
    private lateinit var toolbarTitle: TextView
    private var filePath = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_user)

        addUserViewModel = AddUserViewModel(applicationContext)
        binding.mainViewModel = addUserViewModel

        database = Database(applicationContext)
        imgBack = findViewById(R.id.imgBack)
        toolbarTitle = findViewById(R.id.toolbarTitle)

        imgBack.setOnClickListener { onBackPressed() }
        toolbarTitle.text = "Add Contact"

        binding.edtDate.setText(addUserViewModel.getCurrentDate())

        binding.btnAddUser.setOnClickListener {
            val name = binding.edtUsername.text.toString().trim()
            val about = binding.edtAbout.text.toString().trim()
            val phoneNo = binding.edtPhoneNumber.text.toString().trim()

            if (name.isNotEmpty() && about.isNotEmpty() && phoneNo.isNotEmpty() && lastSeen.isNotEmpty() && filePath.isNotEmpty()) {
                lifecycleScope.launch {
                    val res = withContext(Dispatchers.IO) {
                        database.insertUser(
                            User(
                                userId = 0,
                                profileImage = filePath,
                                name = name,
                                aboutInfo = about,
                                lastSeen = lastSeen,
                                phoneNo = phoneNo,
                                date = addUserViewModel.getCurrentDate(),
                                isVerified = if (binding.cbVerified.isChecked) 1 else 0,
                                isArchive = 0,
                                encryptedText = getString(R.string.whatsapp_encryption_message),
                                lastMessage = "No message",
                                lastMsgTime = addUserViewModel.getCurrentTime()
                            )
                        )
                    }

                    if (res > 0) {
                        Toast.makeText(applicationContext, "User Added!", Toast.LENGTH_SHORT).show()
                        onBackPressed()
                    } else
                        Toast.makeText(applicationContext, "Failed to add user", Toast.LENGTH_SHORT)
                            .show()
                }
            } else {
                Toast.makeText(this, "Please fill all the details", Toast.LENGTH_SHORT).show()
            }
        }

        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val selectedRadioButton: RadioButton = findViewById(checkedId)
            lastSeen = selectedRadioButton.text.toString()
        }

        binding.profileImage.setOnClickListener {
            val photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            startActivityForResult(photoPickerIntent, REQ_CODE)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && data != null && requestCode == REQ_CODE) {
            try {

                val bmp = BitmapFactory.decodeStream(contentResolver.openInputStream(data.data!!))
                binding.profileImage.setImageBitmap(bmp)

                CoroutineScope(Dispatchers.IO).launch {
                    saveImageToCache(bmp)
                }

            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun saveImageToCache(bitmapImage: Bitmap): Boolean =
        withContext(Dispatchers.IO) {
            val cw = ContextWrapper(applicationContext)
            val directory = cw.getDir("profile", MODE_PRIVATE)
            if (!directory.exists()) {
                directory.mkdir()
            }
            val childPath = "FTG_" + System.currentTimeMillis()
            val myPath = File(directory, "$childPath.png")
            filePath = directory.path.toString() + "/" + childPath + ".png"
            var taskCompleted = false
            try {
                var fos: FileOutputStream? = null
                try {
                    fos = FileOutputStream(myPath)
                    bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fos)
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
}