package com.android.app.fakechatapp.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.android.app.fakechatapp.R
import com.android.app.fakechatapp.activities.call.videoplay.VideoPlayActivity
import com.android.app.fakechatapp.activities.chat_screen.ChatActivity
import com.android.app.fakechatapp.activities.viewimage.ImageViewActivity
import com.android.app.fakechatapp.databinding.ItemChatLeftBinding
import com.android.app.fakechatapp.databinding.ItemChatLeftFileBinding
import com.android.app.fakechatapp.databinding.ItemChatLeftImageBinding
import com.android.app.fakechatapp.databinding.ItemChatLeftVideoBinding
import com.android.app.fakechatapp.databinding.ItemChatRightBinding
import com.android.app.fakechatapp.databinding.ItemChatRightFileBinding
import com.android.app.fakechatapp.databinding.ItemChatRightImageBinding
import com.android.app.fakechatapp.databinding.ItemChatRightVideoBinding
import com.android.app.fakechatapp.models.Chat
import com.bumptech.glide.Glide
import java.io.File

const val MSG_TYPE_LEFT: Int = 1
const val MSG_TYPE_RIGHT: Int = 2
const val MSG_TYPE_LEFT_IMAGE: Int = 3
const val MSG_TYPE_RIGHT_IMAGE: Int = 4
const val MSG_TYPE_LEFT_FILE: Int = 5
const val MSG_TYPE_RIGHT_FILE: Int = 6
const val MSG_TYPE_LEFT_VIDEO: Int = 7
const val MSG_TYPE_RIGHT_VIDEO: Int = 8

class ChatListAdapter(val context: Context) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var mList: ArrayList<Chat> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            MSG_TYPE_LEFT -> {
                val binding = ItemChatLeftBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                LeftViewHolder(binding)

            }

            MSG_TYPE_RIGHT -> {
                val binding = ItemChatRightBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                RightViewHolder(binding)
            }

            MSG_TYPE_LEFT_IMAGE -> {
                val binding = ItemChatLeftImageBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                LeftImageViewHolder(binding)
            }

            MSG_TYPE_RIGHT_IMAGE -> {
                val binding = ItemChatRightImageBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                RightImageViewHolder(binding)
            }

            MSG_TYPE_LEFT_FILE -> {
                val binding = ItemChatLeftFileBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                LeftFileViewHolder(binding)
            }

            MSG_TYPE_RIGHT_FILE -> {
                val binding = ItemChatRightFileBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                RightFileViewHolder(binding)
            }

            MSG_TYPE_LEFT_VIDEO -> {
                val binding = ItemChatLeftVideoBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                LeftVideoViewHolder(binding)
            }

            MSG_TYPE_RIGHT_VIDEO -> {
                val binding = ItemChatRightVideoBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                RightVideoViewHolder(binding)
            }

            else -> {
                val binding = ItemChatRightFileBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                RightFileViewHolder(binding)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (mList.size > 0) {
            return when (mList[position].viewType) {
                1 -> MSG_TYPE_RIGHT
                2 -> MSG_TYPE_LEFT
                3 -> MSG_TYPE_LEFT_IMAGE
                4 -> MSG_TYPE_RIGHT_IMAGE
                5 -> MSG_TYPE_LEFT_FILE
                6 -> MSG_TYPE_RIGHT_FILE
                7 -> MSG_TYPE_RIGHT_VIDEO
                8 -> MSG_TYPE_LEFT_VIDEO
                else -> MSG_TYPE_RIGHT_VIDEO
            }
        }
        return 0
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            MSG_TYPE_LEFT -> {
                val left = holder as LeftViewHolder
                with(left) {
                    with(mList[position]) {
                        val model = this
                        try {
                            binding.apply {
                                tvMessage.text = model.message
                                tvTime.text = model.time
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }

            MSG_TYPE_RIGHT -> {
                val right = holder as RightViewHolder
                with(right) {
                    with(mList[position]) {
                        val model = this
                        binding.apply {
                            tvMessage.text = model.message
                            tvTime.text = model.time
                        }
                    }
                }
            }

            MSG_TYPE_RIGHT_IMAGE -> {
                val right = holder as RightImageViewHolder
                with(right) {
                    with(mList[position]) {
                        val model = this
                        binding.apply {
                            Glide.with(context)
                                .load(model.imagePath)
                                .centerCrop()
                                .placeholder(R.drawable.ic_user)
                                .into(binding.imgChat)
                            tvTime.text = model.time
                            itemView.setOnClickListener {
                                context.startActivity(
                                    Intent(context, ImageViewActivity::class.java)
                                        .putExtra("image_path", imagePath)
                                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                )
                            }
                        }
                    }
                }
            }

            MSG_TYPE_LEFT_IMAGE -> {
                val right = holder as LeftImageViewHolder
                with(right) {
                    with(mList[position]) {
                        val model = this
                        binding.apply {
                            Glide.with(context)
                                .load(model.imagePath)
                                .centerCrop()
                                .placeholder(R.drawable.ic_user)
                                .into(binding.imgChat)
                            tvTime.text = model.time
                            itemView.setOnClickListener {
                                context.startActivity(
                                    Intent(context, ImageViewActivity::class.java)
                                        .putExtra("image_path", imagePath)
                                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                )
                            }
                        }
                    }
                }
            }

            MSG_TYPE_LEFT_FILE -> {
                val right = holder as LeftFileViewHolder
                with(right) {
                    with(mList[position]) {
                        val model = this
                        binding.apply {
                            tvTime.text = model.time
                            tvFileName.text = model.message
                        }
                    }
                }
            }

            MSG_TYPE_RIGHT_FILE -> {
                val right = holder as RightFileViewHolder
                with(right) {
                    with(mList[position]) {
                        val model = this
                        binding.apply {
                            tvTime.text = model.time
                            tvFileName.text = model.message
                        }
                    }
                }
            }

            MSG_TYPE_RIGHT_VIDEO -> {
                val right = holder as RightVideoViewHolder
                with(right) {
                    with(mList[position]) {
                        val model = this
                        binding.apply {
                            tvTime.text = model.time
                            Glide.with(context)
                                .load(model.filePath)
                                .centerCrop()
                                .placeholder(R.drawable.ic_user)
                                .into(binding.imgChat)
                            binding.imgPlayVideo.setOnClickListener {
                                context.startActivity(
                                    Intent(context, VideoPlayActivity::class.java)
                                        .putExtra("video_path", filePath)
                                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                )
                            }
                        }
                    }
                }
            }

            MSG_TYPE_LEFT_VIDEO -> {
                val right = holder as LeftVideoViewHolder
                with(right) {
                    with(mList[position]) {
                        val model = this
                        binding.apply {
                            tvTime.text = model.time
                            Glide.with(context)
                                .load(model.filePath)
                                .centerCrop()
                                .placeholder(R.drawable.ic_user)
                                .into(binding.imgChat)
                            binding.imgPlayVideo.setOnClickListener {
                                context.startActivity(
                                    Intent(context, VideoPlayActivity::class.java)
                                        .putExtra("video_path", filePath)
                                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    inner class RightViewHolder(val binding: ItemChatRightBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class LeftViewHolder(val binding: ItemChatLeftBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class RightImageViewHolder(val binding: ItemChatRightImageBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class LeftImageViewHolder(val binding: ItemChatLeftImageBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class RightFileViewHolder(val binding: ItemChatRightFileBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class LeftFileViewHolder(val binding: ItemChatLeftFileBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class RightVideoViewHolder(val binding: ItemChatRightVideoBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class LeftVideoViewHolder(val binding: ItemChatLeftVideoBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun getItemCount(): Int {
        return mList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(mList: ArrayList<Chat>) {
        this.mList.clear()
        this.mList.addAll(mList)
        notifyDataSetChanged()
        (context as ChatActivity).scrollToBottom()
    }

    fun addChat(chat: Chat) {
        mList.add(chat)
        notifyItemInserted(mList.size - 1)
        (context as ChatActivity).scrollToBottom()
    }

    fun addChats(chats: List<Chat>) {
        val initialSize = mList.size
        mList.addAll(chats)
        notifyItemRangeInserted(initialSize, chats.size)
        (context as ChatActivity).scrollToBottom()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clearList() {
        this.mList.clear()
        notifyDataSetChanged()
    }
}