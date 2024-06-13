package com.android.app.fakechatapp.activities.profile

import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.android.app.fakechatapp.R
import com.android.app.fakechatapp.activities.about.AboutActivity
import com.android.app.fakechatapp.activities.viewimage.ImageViewActivity
import com.android.app.fakechatapp.databinding.ActivityProfileBinding
import com.android.app.fakechatapp.utils.Constants
import com.android.app.fakechatapp.utils.SharedPref
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

private const val IMAGE_REQ_CODE = 124

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private lateinit var imgBack: ImageView
    private lateinit var toolbarTitle: TextView
    private lateinit var sharedPref: SharedPref
    private var filePath = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_profile)

        sharedPref = SharedPref(applicationContext)
        imgBack = findViewById(R.id.imgBack)
        toolbarTitle = findViewById(R.id.toolbarTitle)

        imgBack.setOnClickListener { onBackPressed() }
        toolbarTitle.text = "Profile"

        binding.cameraLayout.setOnClickListener {
            val photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            startActivityForResult(photoPickerIntent, IMAGE_REQ_CODE)
        }

        binding.profilePic.setOnClickListener {
            val profilePath = sharedPref.read(Constants.USER_PROFILE_PIC_PATH, "")
            if (profilePath?.isNotEmpty() == true) {
                startActivity(
                    Intent(this, ImageViewActivity::class.java)
                        .putExtra("image_path", profilePath)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                )
            } else {
                Toast.makeText(applicationContext, "No profile photo", Toast.LENGTH_SHORT).show()
            }
        }

        binding.aboutLayout.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        loadProfileData()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && data != null && requestCode == IMAGE_REQ_CODE) {
            try {
                val bmp = BitmapFactory.decodeStream(contentResolver.openInputStream(data.data!!))
                binding.profilePic.setImageBitmap(bmp)
                CoroutineScope(Dispatchers.IO).launch {
                    saveImageToCache(bmp)
                }
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
        }
    }

    private fun loadProfileData() {
        if (sharedPref.read(Constants.USER_PROFILE_PIC_PATH, "")?.isNotEmpty() == true) {
            Picasso.get()
                .load(File(sharedPref.read(Constants.USER_PROFILE_PIC_PATH, "")!!))
                .placeholder(R.drawable.ic_user)
                .into(binding.profilePic)
        }

        binding.tvAbout.text =
            sharedPref.read(Constants.USER_PROFILE_ABOUT, "Hi there, I am using Fake Chat!")
        binding.tvUsername.text = sharedPref.read(Constants.USER_PROFILE_NAME, "No username")
        binding.tvPhoneNumber.text =
            sharedPref.read(Constants.USER_PROFILE_PHONE, "No phone number")
    }

    private suspend fun saveImageToCache(bitmapImage: Bitmap): Boolean =
        withContext(Dispatchers.IO) {
            val cw = ContextWrapper(applicationContext)
            val directory = cw.getDir("profile", MODE_PRIVATE)
            if (!directory.exists()) {
                directory.mkdir()
            }
            val childPath = "user_Profile" + System.currentTimeMillis()
            val myPath = File(directory, "$childPath.png")
            filePath = directory.path.toString() + "/" + childPath + ".png"
            sharedPref.write(Constants.USER_PROFILE_PIC_PATH, filePath)
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