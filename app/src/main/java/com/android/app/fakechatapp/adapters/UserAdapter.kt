package com.android.app.fakechatapp.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.app.fakechatapp.R
import com.android.app.fakechatapp.database.Database
import com.android.app.fakechatapp.models.User
import com.bumptech.glide.Glide

class UserAdapter(private var context: Context) :
    RecyclerView.Adapter<UserAdapter.MyViewHolder>() {
    private lateinit var database: Database
    private var mList: ArrayList<User?>? = arrayListOf()
    private var filteredList: ArrayList<User?>? = arrayListOf()

    private var itemClickListener: OnItemClickListener? = null
    private var imageClickListener: OnImageClickListener? = null
    private var itemLongClickListener: OnItemLongClickListener? = null

    init {
        filteredList = mList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false)
        database = Database(context)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val userModel: User = filteredList?.get(position)!!
        holder.tvUserName.text = userModel.name
        holder.tvLastMessage.text = userModel.lastMessage
        holder.tvLastMessageTime.text = userModel.lastMsgTime

        Glide.with(context)
            .load(userModel.profileImage)
            .placeholder(R.drawable.ic_user)
            .into(holder.profilePic)

        holder.itemView.setOnClickListener {
            itemClickListener?.onItemClick(userModel)
        }

        holder.itemView.setOnLongClickListener {
            itemLongClickListener?.onItemLongClick(userModel)
            true
        }

        holder.profilePic.setOnClickListener {
            imageClickListener?.onImageClick(userModel)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newData: ArrayList<User?>?) {
        mList = newData
        filteredList = mList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return filteredList?.size ?: 0
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvUserName: TextView
        var tvLastMessage: TextView
        var tvLastMessageTime: TextView
        var profilePic: ImageView

        init {
            tvUserName = itemView.findViewById(R.id.tvUserName)
            profilePic = itemView.findViewById(R.id.profilePic)
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage)
            tvLastMessageTime = itemView.findViewById(R.id.tvLastMessageTime)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun filter(query: String) {
        filteredList = if (query.isEmpty()) {
            mList
        } else {
            mList?.filter { it?.name?.contains(query, ignoreCase = true) == true }
                ?.toCollection(ArrayList())
        }
        notifyDataSetChanged()
    }

    interface OnItemClickListener {
        fun onItemClick(userModel: User)
    }

    interface OnImageClickListener {
        fun onImageClick(userModel: User)
    }

    interface OnItemLongClickListener {
        fun onItemLongClick(userModel: User)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        itemClickListener = listener
    }

    fun setOnImageClickListener(listener: OnImageClickListener) {
        imageClickListener = listener
    }

    fun setOnItemLongClickListener(listener: OnItemLongClickListener) {
        itemLongClickListener = listener
    }
}