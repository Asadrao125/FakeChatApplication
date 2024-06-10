package com.android.app.fakechatapp.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.android.app.fakechatapp.R
import com.android.app.fakechatapp.database.Database
import com.android.app.fakechatapp.models.Status
import com.android.app.fakechatapp.activities.viewstatus.ViewStatusActivity
import com.bumptech.glide.Glide
import com.squareup.picasso.Picasso
import java.io.File


class StatusRecentAdapter(private var context: Context) :
    RecyclerView.Adapter<StatusRecentAdapter.MyViewHolder>() {
    private lateinit var database: Database
    private var mList: ArrayList<Status> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view: View =
            LayoutInflater.from(context).inflate(R.layout.item_status_recent, parent, false)
        database = Database(context)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val status: Status = mList[position]
        holder.tvUserName.text = status.userName

        if (status.isSeen == 1) holder.statusLayout.background =
            ContextCompat.getDrawable(context, R.drawable.grey_circle)
        else holder.statusLayout.background =
            ContextCompat.getDrawable(context, R.drawable.green_circle)

        if (status.statusType == 1) {
            holder.status.text = status.statusMessage
            holder.statusImage.visibility = GONE
            holder.status.visibility = VISIBLE
        } else if (status.statusType == 2) {
            holder.status.visibility = GONE
            holder.statusImage.visibility = VISIBLE

            Glide.with(context)
                .load(status.imagePath)
                .centerCrop()
                .placeholder(R.drawable.ic_user)
                .into(holder.statusImage)
        }

        holder.itemView.setOnClickListener {
            context.startActivity(
                Intent(context, ViewStatusActivity::class.java)
                    .putExtra("status_msg", status.statusMessage)
                    .putExtra("status_color", status.statusColor)
                    .putExtra("status_id", status.statusId)
                    .putExtra("statusType", status.statusType)
                    .putExtra("statusImagePath", status.imagePath)
            )
        }
    }

    fun submitList(newData: ArrayList<Status>) {
        mList = newData
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvUserName: TextView
        var status: TextView
        var statusLayout: RelativeLayout
        var statusImage: ImageView

        init {
            tvUserName = itemView.findViewById(R.id.tvUserName)
            status = itemView.findViewById(R.id.status)
            statusLayout = itemView.findViewById(R.id.statusLayout)
            statusImage = itemView.findViewById(R.id.statusImage)
        }
    }
}