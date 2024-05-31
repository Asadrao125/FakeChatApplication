package com.android.app.fakechatapp.adapters

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.app.fakechatapp.R
import com.android.app.fakechatapp.activities.chat_screen.ChatActivity
import com.android.app.fakechatapp.database.Database
import com.android.app.fakechatapp.models.User
import com.squareup.picasso.Picasso
import java.io.File

class UserAdapter(private var context: Context) :
    RecyclerView.Adapter<UserAdapter.MyViewHolder>() {
    private lateinit var database: Database
    private var mList: ArrayList<User?>? = arrayListOf()
    private var filteredList: ArrayList<User?>? = arrayListOf()

    init {
        filteredList = mList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false)
        database = Database(context)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val userModel: User = mList?.get(position)!!
        holder.tvUserName.text = userModel.name
        holder.tvLastMessage.text = userModel.lastMessage
        holder.tvLastMessageTime.text = userModel.lastMsgTime

        Picasso.get().load(File(userModel.profileImage)).placeholder(R.drawable.ic_user).into(holder.profilePic)

        if (userModel.isVerified == 1) holder.imgVerified.visibility = VISIBLE
        else holder.imgVerified.visibility = GONE

        holder.itemView.setOnClickListener {
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra("user_id", userModel.userId)
            intent.putExtra("user_name", userModel.name)
            intent.putExtra("last_seen", userModel.lastSeen)
            intent.putExtra("date", userModel.date)
            intent.putExtra("enc_text", userModel.encryptedText)
            context.startActivity(intent)
        }
    }

    fun submitList(newData: ArrayList<User?>?) {
        mList = newData
        filteredList = mList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return mList!!.size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvUserName: TextView
        var tvLastMessage: TextView
        var tvLastMessageTime: TextView
        var profilePic: ImageView
        var imgVerified: ImageView

        init {
            tvUserName = itemView.findViewById(R.id.tvUserName)
            profilePic = itemView.findViewById(R.id.profilePic)
            imgVerified = itemView.findViewById(R.id.imgVVerified)
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage)
            tvLastMessageTime = itemView.findViewById(R.id.tvLastMessageTime)
        }
    }

    fun filter(query: String) {
        filteredList = if (query.isEmpty()) {
            mList
        } else {
            mList?.filter { it?.name?.contains(query, ignoreCase = true) == true } as ArrayList<User?>?
        }
        Log.d("query_result:1 ", "filter: $query")
        Log.d("query_result:2 ", "filter: ${mList.toString()}")
        Log.d("query_result:3 ", "filter: ${filteredList.toString()}")
        notifyDataSetChanged()
    }
}