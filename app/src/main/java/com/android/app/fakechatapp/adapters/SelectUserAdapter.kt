package com.android.app.fakechatapp.adapters

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
import com.squareup.picasso.Picasso
import java.io.File

class SelectUserAdapter(private var context: Context, private var list: ArrayList<User?>?) :
    RecyclerView.Adapter<SelectUserAdapter.MyViewHolder>() {
    private lateinit var database: Database

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.item_select_user, parent, false)
        database = Database(context)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val userModel: User = list?.get(position)!!
        holder.tvUserName.text = userModel.name
        Picasso.get().load(File(userModel.profileImage)).placeholder(R.drawable.ic_user)
            .into(holder.profilePic)
    }

    override fun getItemCount(): Int {
        return list!!.size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvUserName: TextView
        var profilePic: ImageView

        init {
            tvUserName = itemView.findViewById(R.id.tvUserName)
            profilePic = itemView.findViewById(R.id.profilePic)
        }
    }
}